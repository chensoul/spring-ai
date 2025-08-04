package com.example.kb.repository;

import com.example.kb.model.QueryEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryRepository extends JpaRepository<QueryEntity, Long> {
    
    List<QueryEntity> findByUserIdOrderByQueryTimeDesc(String userId, PageRequest pageRequest);
    
    List<QueryEntity> findByUserIdAndCategoryOrderByQueryTimeDesc(String userId, String category);
    
    @Query("SELECT q FROM QueryEntity q WHERE q.userId = :userId ORDER BY q.queryTime DESC")
    List<QueryEntity> findRecentQueriesByUser(@Param("userId") String userId, PageRequest pageRequest);
    
    @Query("SELECT COUNT(q) FROM QueryEntity q WHERE q.userId = :userId AND q.status = :status")
    long countByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
}