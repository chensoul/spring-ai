package com.example.kb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 查询请求DTO
 */
public record QueryRequest(
    @NotBlank(message = "问题不能为空")
    @Size(max = 1000, message = "问题长度不能超过1000个字符")
    String question,
    
    String category,
    String sessionId,
    Boolean useRag
) {
    
    public QueryRequest {
        // 默认启用RAG
        if (useRag == null) {
            useRag = true;
        }
    }
    
    public static QueryRequest of(String question) {
        return new QueryRequest(question, null, null, true);
    }
    
    public static QueryRequest of(String question, String category) {
        return new QueryRequest(question, category, null, true);
    }
}