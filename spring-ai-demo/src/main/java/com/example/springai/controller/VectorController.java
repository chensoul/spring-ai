package com.example.springai.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 向量存储控制器
 * 
 * 提供文档存储和检索接口
 */
@RestController
@RequestMapping("/api/vector")
@CrossOrigin(origins = "*")
public class VectorController {

    private final VectorStore vectorStore;

    public VectorController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 添加文档到向量存储
     */
    @PostMapping("/add")
    public String addDocument(@RequestBody DocumentRequest request) {
        Document document = new Document(request.getContent(), request.getMetadata());
        vectorStore.add(List.of(document));
        return "Document added successfully";
    }

    /**
     * 搜索相似文档
     */
    @PostMapping("/search")
    public List<Document> searchDocuments(@RequestBody SearchRequest request) {
        return vectorStore.similaritySearch(request);
    }

    /**
     * 简单搜索接口
     */
    @GetMapping("/search")
    public List<Document> searchDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(defaultValue = "0.7") double threshold) {
        
        SearchRequest request = SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(threshold)
            .build();
        
        return vectorStore.similaritySearch(request);
    }

    /**
     * 获取向量存储统计信息
     */
    @GetMapping("/stats")
    public String getStats() {
        // 这里可以添加统计信息的逻辑
        return "Vector store is operational";
    }

    // 请求 DTO
    public static class DocumentRequest {
        private String content;
        private Map<String, Object> metadata;

        public DocumentRequest() {}

        public DocumentRequest(String content, Map<String, Object> metadata) {
            this.content = content;
            this.metadata = metadata;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}