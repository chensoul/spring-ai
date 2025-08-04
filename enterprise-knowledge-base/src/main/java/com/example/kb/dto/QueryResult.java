package com.example.kb.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 查询结果DTO
 */
public record QueryResult(
    String answer,
    String status,
    String error,
    Long responseTime,
    Integer sourceDocuments,
    Double similarityScore,
    List<String> sourceFiles,
    LocalDateTime queryTime
) {
    
    public static QueryResult success(String answer, Long responseTime, 
                                    Integer sourceDocuments, Double similarityScore,
                                    List<String> sourceFiles) {
        return new QueryResult(
            answer,
            "SUCCESS",
            null,
            responseTime,
            sourceDocuments,
            similarityScore,
            sourceFiles,
            LocalDateTime.now()
        );
    }
    
    public static QueryResult error(String error) {
        return new QueryResult(
            null,
            "ERROR",
            error,
            null,
            null,
            null,
            null,
            LocalDateTime.now()
        );
    }
    
    public static QueryResult timeout() {
        return new QueryResult(
            null,
            "TIMEOUT",
            "查询超时，请稍后重试",
            null,
            null,
            null,
            null,
            LocalDateTime.now()
        );
    }
}