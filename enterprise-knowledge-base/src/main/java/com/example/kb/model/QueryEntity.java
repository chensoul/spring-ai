package com.example.kb.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 查询实体类
 * 
 * 存储用户查询历史和AI回答
 */
@Entity
@Table(name = "queries")
public class QueryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "问题不能为空")
    @Size(max = 1000, message = "问题长度不能超过1000个字符")
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;
    
    @NotBlank(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "category")
    private String category;
    
    @NotNull
    @Column(name = "query_time", nullable = false)
    private LocalDateTime queryTime;
    
    @Column(name = "response_time")
    private Long responseTime; // 响应时间（毫秒）
    
    @NotBlank(message = "状态不能为空")
    @Column(name = "status", nullable = false)
    private String status; // SUCCESS, ERROR, TIMEOUT
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "source_documents")
    private Integer sourceDocuments; // 引用的文档数量
    
    @Column(name = "similarity_score")
    private Double similarityScore; // 相似度分数
    
    @Column(name = "session_id")
    private String sessionId; // 会话ID
    
    // 构造函数
    public QueryEntity() {
        this.queryTime = LocalDateTime.now();
        this.status = "PROCESSING";
    }
    
    public QueryEntity(String question, String userId) {
        this();
        this.question = question;
        this.userId = userId;
    }
    
    public QueryEntity(String question, String userId, String category) {
        this(question, userId);
        this.category = category;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public LocalDateTime getQueryTime() {
        return queryTime;
    }
    
    public void setQueryTime(LocalDateTime queryTime) {
        this.queryTime = queryTime;
    }
    
    public Long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
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
    
    public Integer getSourceDocuments() {
        return sourceDocuments;
    }
    
    public void setSourceDocuments(Integer sourceDocuments) {
        this.sourceDocuments = sourceDocuments;
    }
    
    public Double getSimilarityScore() {
        return similarityScore;
    }
    
    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    @Override
    public String toString() {
        return "QueryEntity{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", userId='" + userId + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}