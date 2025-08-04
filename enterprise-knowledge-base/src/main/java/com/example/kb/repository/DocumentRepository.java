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

    @Query("SELECT DISTINCT d.category FROM DocumentEntity d WHERE d.uploadedBy = :userId")
    List<String> findUserCategories(@Param("userId") String userId);

    List<DocumentEntity> findByUploadedByAndStatus(String uploadedBy, String status);

    // 根据MD5查询（跨用户）
    List<DocumentEntity> findByMd5Hash(String md5Hash);

}