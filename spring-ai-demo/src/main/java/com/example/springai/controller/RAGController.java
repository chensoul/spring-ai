package com.example.springai.controller;

import com.example.springai.service.RAGService;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RAG (Retrieval Augmented Generation) 控制器
 * 
 * 提供基于知识库的问答接口
 */
@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class RAGController {

    private final RAGService ragService;

    public RAGController(RAGService ragService) {
        this.ragService = ragService;
    }

    /**
     * 添加文档到知识库
     */
    @PostMapping("/documents")
    public String addDocument(@RequestBody AddDocumentRequest request) {
        ragService.addDocument(request.getContent(), request.getTitle(), request.getCategory());
        return "Document added to knowledge base successfully";
    }

    /**
     * 基于知识库的问答
     */
    @PostMapping("/ask")
    public QuestionResponse askQuestion(@RequestBody QuestionRequest request) {
        String answer = ragService.askQuestion(request.getQuestion());
        return new QuestionResponse(answer);
    }

    /**
     * 搜索相关文档
     */
    @GetMapping("/search")
    public List<Document> searchDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        return ragService.searchDocuments(query, topK);
    }

    /**
     * 获取文档摘要
     */
    @PostMapping("/summarize")
    public SummaryResponse summarizeDocuments(@RequestBody SummaryRequest request) {
        String summary = ragService.summarizeDocuments(request.getQuery());
        return new SummaryResponse(summary);
    }

    // DTO 类
    public static class AddDocumentRequest {
        private String content;
        private String title;
        private String category;

        public AddDocumentRequest() {}

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }

    public static class QuestionRequest {
        private String question;

        public QuestionRequest() {}

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }

    public static class QuestionResponse {
        private String answer;

        public QuestionResponse() {}

        public QuestionResponse(String answer) {
            this.answer = answer;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }

    public static class SummaryRequest {
        private String query;

        public SummaryRequest() {}

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    public static class SummaryResponse {
        private String summary;

        public SummaryResponse() {}

        public SummaryResponse(String summary) {
            this.summary = summary;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }
}