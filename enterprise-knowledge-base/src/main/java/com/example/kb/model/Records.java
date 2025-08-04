package com.example.kb.model;

import java.time.LocalDateTime;
import java.util.List;

public class Records {
    
    public record DocumentUploadResult(Long documentId, String status, String message) {}
    
    public record QueryResult(String answer, String status, String error) {}
    
    public record QueryRequest(String question, String category) {}
    
    public record DocumentAnalysisResult(
        String filename,
        DocumentSummary summary,
        DocumentClassification classification,
        String status,
        String error
    ) {}
    
    public record DocumentSummary(
        String overview,
        List<String> keyPoints,
        String conclusion,
        List<String> recommendations
    ) {}
    
    public record DocumentClassification(
        String documentType,
        String subject,
        String importance,
        String priority
    ) {}
}