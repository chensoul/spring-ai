package com.example.kb.repository;

import com.example.kb.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    
    List<DocumentEntity> findByUploadedBy(String uploadedBy);
    
    List<DocumentEntity> findByUploadedByAndCategory(String uploadedBy, String category);
    
    List<DocumentEntity> findByStatus(String status);
    
    @Query("SELECT d FROM DocumentEntity d WHERE d.uploadedBy = :userId AND d.category = :category")
    List<DocumentEntity> findUserDocumentsByCategory(@Param("userId") String userId, 
                                                    @Param("category") String category);
    
    @Query("SELECT DISTINCT d.category FROM DocumentEntity d WHERE d.uploadedBy = :userId")
    List<String> findUserCategories(@Param("userId") String userId);
    
    List<DocumentEntity> findByUploadedByAndStatus(String uploadedBy, String status);
    
    @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE d.uploadedBy = :userId AND d.status = :status")
    long countByUserAndStatus(@Param("userId") String userId, @Param("status") String status);
    
    @Query("SELECT d FROM DocumentEntity d WHERE d.uploadedBy = :userId ORDER BY d.uploadTime DESC")
    List<DocumentEntity> findRecentDocumentsByUser(@Param("userId") String userId, 
                                                   org.springframework.data.domain.Pageable pageable);
}