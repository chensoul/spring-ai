package com.example.kb.repository;

import com.example.kb.model.QueryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 查询数据访问接口
 */
@Repository
public interface QueryRepository extends JpaRepository<QueryEntity, Long> {
    
    /**
     * 根据用户ID查找查询历史，按时间倒序
     */
    List<QueryEntity> findByUserIdOrderByQueryTimeDesc(String userId, Pageable pageable);
    
    /**
     * 分页查询用户查询历史
     */
    Page<QueryEntity> findByUserId(String userId, Pageable pageable);
    
    /**
     * 根据用户ID和分类查找查询历史
     */
    List<QueryEntity> findByUserIdAndCategory(String userId, String category);
    
    /**
     * 根据会话ID查找查询历史
     */
    List<QueryEntity> findBySessionIdOrderByQueryTimeAsc(String sessionId);
    
    /**
     * 查找指定时间范围内的查询
     */
    List<QueryEntity> findByQueryTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计用户查询次数
     */
    long countByUserId(String userId);
    
    /**
     * 统计指定状态的查询次数
     */
    long countByStatus(String status);
    
    /**
     * 查找包含指定关键词的查询
     */
    @Query("SELECT q FROM QueryEntity q WHERE q.question LIKE %:keyword% OR q.answer LIKE %:keyword%")
    List<QueryEntity> findByKeyword(@Param("keyword") String keyword);
    
    /**
     * 查找响应时间超过指定值的查询
     */
    List<QueryEntity> findByResponseTimeGreaterThan(Long responseTime);
    
    /**
     * 查找失败的查询
     */
    List<QueryEntity> findByStatusAndErrorMessageIsNotNull(String status);
    
    /**
     * 统计各分类查询次数
     */
    @Query("SELECT q.category, COUNT(q) FROM QueryEntity q GROUP BY q.category")
    List<Object[]> countByCategory();
    
    /**
     * 统计用户各分类查询次数
     */
    @Query("SELECT q.category, COUNT(q) FROM QueryEntity q WHERE q.userId = :userId GROUP BY q.category")
    List<Object[]> countByCategoryAndUser(@Param("userId") String userId);
    
    /**
     * 查询平均响应时间
     */
    @Query("SELECT AVG(q.responseTime) FROM QueryEntity q WHERE q.status = 'SUCCESS'")
    Double getAverageResponseTime();
    
    /**
     * 查询用户平均响应时间
     */
    @Query("SELECT AVG(q.responseTime) FROM QueryEntity q WHERE q.userId = :userId AND q.status = 'SUCCESS'")
    Double getAverageResponseTimeByUser(@Param("userId") String userId);
    
    /**
     * 删除指定时间之前的查询记录
     */
    void deleteByQueryTimeBefore(LocalDateTime cutoffTime);
}