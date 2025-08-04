package com.example.kb.repository;

import com.example.kb.model.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文档数据访问接口
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    
    /**
     * 根据上传用户查找文档
     */
    List<DocumentEntity> findByUploadedBy(String uploadedBy);
    
    /**
     * 根据上传用户和分类查找文档
     */
    List<DocumentEntity> findByUploadedByAndCategory(String uploadedBy, String category);
    
    /**
     * 根据状态查找文档
     */
    List<DocumentEntity> findByStatus(String status);
    
    /**
     * 根据分类查找文档
     */
    List<DocumentEntity> findByCategory(String category);
    
    /**
     * 分页查询用户文档
     */
    Page<DocumentEntity> findByUploadedBy(String uploadedBy, Pageable pageable);
    
    /**
     * 分页查询用户指定分类的文档
     */
    Page<DocumentEntity> findByUploadedByAndCategory(String uploadedBy, String category, Pageable pageable);
    
    /**
     * 查找指定时间范围内的文档
     */
    List<DocumentEntity> findByUploadTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计用户文档数量
     */
    long countByUploadedBy(String uploadedBy);
    
    /**
     * 统计指定状态的文档数量
     */
    long countByStatus(String status);
    
    /**
     * 查找文件名包含指定关键词的文档
     */
    @Query("SELECT d FROM DocumentEntity d WHERE d.filename LIKE %:keyword% OR d.description LIKE %:keyword%")
    List<DocumentEntity> findByKeyword(@Param("keyword") String keyword);
    
    /**
     * 查找用户的文档，按上传时间倒序
     */
    List<DocumentEntity> findByUploadedByOrderByUploadTimeDesc(String uploadedBy);
    
    /**
     * 查找处理失败的文档
     */
    List<DocumentEntity> findByStatusAndErrorMessageIsNotNull(String status);
    
    /**
     * 根据文件路径查找文档
     */
    Optional<DocumentEntity> findByFilePath(String filePath);
    
    /**
     * 删除指定用户的指定文档
     */
    void deleteByIdAndUploadedBy(Long id, String uploadedBy);
    
    /**
     * 查询统计信息
     */
    @Query("SELECT d.category, COUNT(d) FROM DocumentEntity d GROUP BY d.category")
    List<Object[]> countByCategory();
    
    /**
     * 查询用户各分类文档统计
     */
    @Query("SELECT d.category, COUNT(d) FROM DocumentEntity d WHERE d.uploadedBy = :userId GROUP BY d.category")
    List<Object[]> countByCategoryAndUser(@Param("userId") String userId);
}