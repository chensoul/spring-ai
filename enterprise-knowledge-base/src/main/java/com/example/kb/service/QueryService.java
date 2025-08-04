package com.example.kb.service;

import com.example.kb.dto.QueryRequest;
import com.example.kb.dto.QueryResult;
import com.example.kb.model.QueryEntity;
import com.example.kb.repository.QueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询服务类
 * 
 * 负责处理用户查询，包括RAG查询和普通AI对话
 */
@Service
@Transactional
public class QueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);
    
    private final ChatClient ragChatClient;
    private final ChatClient simpleChatClient;
    private final VectorStore vectorStore;
    private final QueryRepository queryRepository;
    
    @Value("${app.knowledge-base.query.max-results:5}")
    private int maxResults;
    
    @Value("${app.knowledge-base.query.similarity-threshold:0.75}")
    private double similarityThreshold;
    
    @Value("${app.knowledge-base.query.max-history:10}")
    private int maxHistory;
    
    public QueryService(ChatClient chatClient,
                       @Qualifier("simpleChatClient") ChatClient simpleChatClient,
                       VectorStore vectorStore,
                       QueryRepository queryRepository) {
        this.ragChatClient = chatClient;
        this.simpleChatClient = simpleChatClient;
        this.vectorStore = vectorStore;
        this.queryRepository = queryRepository;
    }
    
    /**
     * 处理用户查询
     */
    public QueryResult query(QueryRequest request, String userId) {
        long startTime = System.currentTimeMillis();
        
        // 创建查询记录
        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setQuestion(request.question());
        queryEntity.setUserId(userId);
        queryEntity.setCategory(request.category());
        queryEntity.setSessionId(request.sessionId());
        queryEntity.setStatus("PROCESSING");
        
        try {
            String answer;
            List<String> sourceFiles = null;
            Integer sourceDocuments = null;
            Double similarityScore = null;
            
            if (request.useRag()) {
                // 使用RAG查询
                logger.info("执行RAG查询: {}", request.question());
                
                // 检索相关文档
                List<Document> relevantDocs = retrieveRelevantDocuments(request);
                sourceDocuments = relevantDocs.size();
                
                if (!relevantDocs.isEmpty()) {
                    // 提取文件名
                    sourceFiles = relevantDocs.stream()
                        .map(doc -> (String) doc.getMetadata().get("filename"))
                        .distinct()
                        .collect(Collectors.toList());
                    
                    // 计算平均相似度
                    similarityScore = relevantDocs.stream()
                        .mapToDouble(doc -> {
                            Object score = doc.getMetadata().get("distance");
                            return score instanceof Number ? ((Number) score).doubleValue() : 0.0;
                        })
                        .average()
                        .orElse(0.0);
                }
                
                // 使用RAG ChatClient
                answer = ragChatClient.prompt()
                    .system(buildSystemPrompt(request.category()))
                    .user(request.question())
                    .call()
                    .content();
                    
            } else {
                // 普通AI对话
                logger.info("执行普通AI查询: {}", request.question());
                
                answer = simpleChatClient.prompt()
                    .system("你是一个有帮助的AI助手，请回答用户的问题。")
                    .user(request.question())
                    .call()
                    .content();
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 保存查询结果
            queryEntity.setAnswer(answer);
            queryEntity.setStatus("SUCCESS");
            queryEntity.setResponseTime(responseTime);
            queryEntity.setSourceDocuments(sourceDocuments);
            queryEntity.setSimilarityScore(similarityScore);
            queryRepository.save(queryEntity);
            
            logger.info("查询完成，耗时: {}ms", responseTime);
            
            return QueryResult.success(answer, responseTime, sourceDocuments, similarityScore, sourceFiles);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            logger.error("查询失败: {}", e.getMessage(), e);
            
            // 保存错误信息
            queryEntity.setStatus("ERROR");
            queryEntity.setErrorMessage(e.getMessage());
            queryEntity.setResponseTime(responseTime);
            queryRepository.save(queryEntity);
            
            return QueryResult.error("查询失败：" + e.getMessage());
        }
    }    
    /**
     * 检索相关文档
     */
    private List<Document> retrieveRelevantDocuments(QueryRequest request) {
        SearchRequest.Builder searchBuilder = SearchRequest.builder()
            .query(request.question())
            .topK(maxResults)
            .similarityThreshold(similarityThreshold);
        
        // 如果指定了分类，添加过滤条件
        if (request.category() != null && !request.category().trim().isEmpty()) {
            searchBuilder.filterExpression("category == '" + request.category() + "'");
        }
        
        SearchRequest searchRequest = searchBuilder.build();
        return vectorStore.similaritySearch(searchRequest);
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String category) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个企业知识库助手。请基于提供的文档内容回答用户问题。\n\n");
        prompt.append("回答要求：\n");
        prompt.append("1. 准确性：确保答案基于文档内容，不要编造信息\n");
        prompt.append("2. 完整性：提供全面的信息，包括相关的背景和细节\n");
        prompt.append("3. 可读性：使用清晰的语言和结构化的格式\n");
        prompt.append("4. 引用：在适当时候提及相关文档或来源\n");
        prompt.append("5. 诚实：如果文档中没有相关信息，请明确说明\n\n");
        
        if (category != null && !category.trim().isEmpty()) {
            prompt.append("专业领域：重点关注").append(category).append("领域的专业知识\n\n");
        }
        
        prompt.append("请用中文回答，保持专业和友好的语调。");
        
        return prompt.toString();
    }
    
    /**
     * 获取查询历史
     */
    public List<QueryEntity> getQueryHistory(String userId, int limit) {
        return queryRepository.findByUserIdOrderByQueryTimeDesc(
            userId, 
            PageRequest.of(0, Math.min(limit, maxHistory))
        );
    }
    
    /**
     * 获取会话查询历史
     */
    public List<QueryEntity> getSessionHistory(String sessionId) {
        return queryRepository.findBySessionIdOrderByQueryTimeAsc(sessionId);
    }
    
    /**
     * 搜索查询历史
     */
    public List<QueryEntity> searchQueryHistory(String userId, String keyword) {
        return queryRepository.findByKeyword(keyword).stream()
            .filter(query -> query.getUserId().equals(userId))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取查询统计信息
     */
    public QueryStatistics getStatistics(String userId) {
        long totalQueries = queryRepository.countByUserId(userId);
        long successQueries = queryRepository.countByStatus("SUCCESS");
        long errorQueries = queryRepository.countByStatus("ERROR");
        
        Double avgResponseTime = queryRepository.getAverageResponseTimeByUser(userId);
        
        List<Object[]> categoryStats = queryRepository.countByCategoryAndUser(userId);
        
        return new QueryStatistics(
            totalQueries,
            successQueries,
            errorQueries,
            avgResponseTime != null ? avgResponseTime : 0.0,
            categoryStats
        );
    }
    
    /**
     * 清理历史查询记录
     */
    @Transactional
    public void cleanupOldQueries(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        queryRepository.deleteByQueryTimeBefore(cutoffTime);
        logger.info("清理了{}天前的查询记录", daysToKeep);
    }
    
    /**
     * 获取热门查询
     */
    public List<String> getPopularQueries(int limit) {
        // 这里可以实现基于查询频率的热门查询统计
        // 简化实现，返回最近的查询
        return queryRepository.findAll().stream()
            .map(QueryEntity::getQuestion)
            .distinct()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * 查询统计信息记录类
     */
    public record QueryStatistics(
        long totalQueries,
        long successQueries,
        long errorQueries,
        double avgResponseTime,
        List<Object[]> categoryStats
    ) {}
}