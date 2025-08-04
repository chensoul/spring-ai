package com.example.kb.service;

import com.example.kb.model.QueryEntity;
import com.example.kb.model.Records.QueryResult;
import com.example.kb.repository.QueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueryService {

    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);

    private final ChatClient chatClient;
    private final QueryRepository queryRepository;

    public QueryService(ChatClient chatClient, QueryRepository queryRepository) {
        this.chatClient = chatClient;
        this.queryRepository = queryRepository;
    }

    public QueryResult query(String question, String userId, String category) {
        try {
            // 记录查询
            QueryEntity query = new QueryEntity(question, userId, category);

            // 构建查询上下文
            String systemPrompt = buildSystemPrompt(category);

            // 执行查询
            String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .call()
                .content();

            // 保存结果
            query.setAnswer(answer);
            query.setStatus("SUCCESS");
            queryRepository.save(query);

            return new QueryResult(answer, "SUCCESS", null);

        } catch (Exception e) {
            logger.error("查询失败", e);
            return new QueryResult(null, "ERROR", e.getMessage());
        }
    }

    public List<QueryEntity> getQueryHistory(String userId, int limit) {
        return queryRepository.findByUserIdOrderByQueryTimeDesc(userId, PageRequest.of(0, limit));
    }

    private String buildSystemPrompt(String category) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个企业知识库助手。请基于提供的文档内容回答用户问题。\n");
        prompt.append("回答要求：\n");
        prompt.append("1. 准确性：确保答案基于文档内容\n");
        prompt.append("2. 完整性：提供全面的信息\n");
        prompt.append("3. 可读性：使用清晰的语言和结构\n");
        prompt.append("4. 引用：在适当时候引用相关文档\n");

        if (category != null) {
            prompt.append("5. 专业性：重点关注").append(category).append("领域的专业知识\n");
        }

        prompt.append("\n如果文档中没有相关信息，请明确说明。");

        return prompt.toString();
    }
}