package com.example.kb.dto;

import com.example.kb.model.DocumentEntity;
import java.time.LocalDateTime;

/**
 * 文档信息DTO
 */
public record DocumentInfo(
    Long id,
    String filename,
    String category,
    String uploadedBy,
    LocalDateTime uploadTime,
    LocalDateTime processedTime,
    String status,
    String errorMessage,
    Long fileSize,
    String contentType,
    Integer chunkCount,
    String description
) {
    
    public static DocumentInfo from(DocumentEntity entity) {
        return new DocumentInfo(
            entity.getId(),
            entity.getFilename(),
            entity.getCategory(),
            entity.getUploadedBy(),
            entity.getUploadTime(),
            entity.getProcessedTime(),
            entity.getStatus(),
            entity.getErrorMessage(),
            entity.getFileSize(),
            entity.getContentType(),
            entity.getChunkCount(),
            entity.getDescription()
        );
    }
    
    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
}