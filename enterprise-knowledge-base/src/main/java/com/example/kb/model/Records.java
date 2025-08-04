package com.example.kb.model;

public class Records {

    public record DocumentUploadResult(Long documentId, String status, String message) {
    }

    public record QueryResult(String answer, String status, String error) {
    }

    public record QueryRequest(String question, String category, String userId) {
    }
}