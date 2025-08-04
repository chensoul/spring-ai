package com.example.kb.dto;

import java.time.LocalDateTime;

/**
 * 文档上传结果DTO
 */
public record DocumentUploadResult(
    Long documentId,
    String filename,
    String status,
    String message,
    LocalDateTime uploadTime,
    Long fileSize
) {
    
    public static DocumentUploadResult success(Long documentId, String filename, Long fileSize) {
        return new DocumentUploadResult(
            documentId,
            filename,
            "SUCCESS",
            "文档上传成功，正在处理中",
            LocalDateTime.now(),
            fileSize
        );
    }
    
    public static DocumentUploadResult error(String filename, String message) {
        return new DocumentUploadResult(
            null,
            filename,
            "ERROR",
            message,
            LocalDateTime.now(),
            null
        );
    }
}