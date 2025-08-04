package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG (Retrieval Augmented Generation) 服务
 *
 * 结合向量检索和生成式AI的服务
 */
@Service
public class RAGService {

    private final VectorStore vectorStore;
    private final ChatClient ragChatClient;

    public RAGService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;

        // 配置带有RAG功能的ChatClient，使用QuestionAnswerAdvisor
        this.ragChatClient = chatClientBuilder
            .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
            .build();
    }

    /**
     * 添加文档到知识库
     */
    public void addDocument(String content, String title, String category) {
        Document document = new Document(content);
        document.getMetadata().put("title", title);
        document.getMetadata().put("category", category);
        document.getMetadata().put("timestamp", System.currentTimeMillis());
        
        vectorStore.add(List.of(document));
    }

    /**
     * 基于知识库的问答
     */
    public String askQuestion(String question) {
        return ragChatClient.prompt()
            .user(question)
            .call()
            .content();
    }

    /**
     * 搜索相关文档
     */
    public List<Document> searchDocuments(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(0.7)
            .build();
        
        return vectorStore.similaritySearch(request);
    }

    /**
     * 获取文档摘要
     */
    public String summarizeDocuments(String query) {
        List<Document> documents = searchDocuments(query, 3);
        
        if (documents.isEmpty()) {
            return "没有找到相关文档。";
        }

        StringBuilder context = new StringBuilder();
        for (Document doc : documents) {
            context.append(doc.getText()).append("\n\n");
        }

        return ragChatClient.prompt()
            .user("请基于以下内容生成摘要：\n" + context.toString())
            .call()
            .content();
    }
}