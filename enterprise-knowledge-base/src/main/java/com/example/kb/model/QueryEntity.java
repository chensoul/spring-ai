package com.example.kb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queries")
public class QueryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;
    private String answer;
    private String userId;
    private String category;
    private LocalDateTime queryTime;
    private String status;

    // Constructors
    public QueryEntity() {}

    public QueryEntity(String question, String userId, String category) {
        this.question = question;
        this.userId = userId;
        this.category = category;
        this.queryTime = LocalDateTime.now();
        this.status = "PROCESSING";
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}