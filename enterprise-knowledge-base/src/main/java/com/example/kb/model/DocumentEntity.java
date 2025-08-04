package com.example.kb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 文档实体类
 * 
 * 存储文档的基本信息和处理状态
 */
@Entity
@Table(name = "documents")
public class DocumentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    @Column(name = "filename", nullable = false)
    private String filename;
    
    @NotBlank(message = "文件路径不能为空")
    @Column(name = "file_path")
    private String filePath;
    
    @Size(max = 100, message = "分类长度不能超过100个字符")
    @Column(name = "category")
    private String category;
    
    @NotBlank(message = "上传用户不能为空")
    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;
    
    @NotNull
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;
    
    @Column(name = "processed_time")
    private LocalDateTime processedTime;
    
    @NotBlank(message = "状态不能为空")
    @Column(name = "status", nullable = false)
    private String status; // PROCESSING, COMPLETED, FAILED
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "content_type")
    private String contentType;
    
    @Column(name = "chunk_count")
    private Integer chunkCount;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // 构造函数
    public DocumentEntity() {
        this.uploadTime = LocalDateTime.now();
        this.status = "PROCESSING";
    }
    
    public DocumentEntity(String filename, String category, String uploadedBy) {
        this();
        this.filename = filename;
        this.category = category;
        this.uploadedBy = uploadedBy;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getUploadedBy() {
        return uploadedBy;
    }
    
    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public LocalDateTime getProcessedTime() {
        return processedTime;
    }
    
    public void setProcessedTime(LocalDateTime processedTime) {
        this.processedTime = processedTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public Integer getChunkCount() {
        return chunkCount;
    }
    
    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "DocumentEntity{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", category='" + category + '\'' +
                ", uploadedBy='" + uploadedBy + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}