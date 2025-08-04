package com.example.kb.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 日志记录Advisor
 * 
 * 记录AI请求和响应的详细信息，用于：
 * - 调试和问题排查
 * - 性能监控
 * - 审计日志
 */
@Component
public class LoggingAdvisor implements CallAdvisor, StreamAdvisor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvisor.class);
    
    @Override
    public String getName() {
        return "LoggingAdvisor";
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // 最高优先级，确保记录所有请求
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String userMessage = extractUserMessage(request);
        logger.info("AI请求开始 - 用户消息: {}", truncateMessage(userMessage));
        
        long startTime = System.currentTimeMillis();
        
        try {
            ChatClientResponse response = chain.nextCall(request);
            long duration = System.currentTimeMillis() - startTime;
            
            String responseContent = response.getResult().getOutput().getContent();
            logger.info("AI请求完成 - 耗时: {}ms, 响应长度: {} 字符", 
                       duration, responseContent.length());
            
            // 记录token使用情况
            if (response.getResult().getMetadata() != null) {
                logger.debug("Token使用情况: {}", response.getResult().getMetadata());
            }
            
            return response;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("AI请求失败 - 耗时: {}ms, 错误: {}", duration, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        String userMessage = extractUserMessage(request);
        logger.info("AI流式请求开始 - 用户消息: {}", truncateMessage(userMessage));
        
        long startTime = System.currentTimeMillis();
        
        return chain.nextStream(request)
            .doOnNext(response -> {
                // 记录每个流式响应片段
                if (logger.isDebugEnabled()) {
                    String content = response.getResult().getOutput().getContent();
                    logger.debug("流式响应片段: {}", truncateMessage(content));
                }
            })
            .doOnComplete(() -> {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("AI流式请求完成 - 总耗时: {}ms", duration);
            })
            .doOnError(error -> {
                long duration = System.currentTimeMillis() - startTime;
                logger.error("AI流式请求失败 - 耗时: {}ms, 错误: {}", 
                           duration, error.getMessage(), error);
            });
    }
    
    /**
     * 提取用户消息内容
     */
    private String extractUserMessage(ChatClientRequest request) {
        try {
            return request.getPrompt().getInstructions();
        } catch (Exception e) {
            return "无法提取消息内容";
        }
    }
    
    /**
     * 截断长消息以避免日志过长
     */
    private String truncateMessage(String message) {
        if (message == null) {
            return "null";
        }
        
        final int maxLength = 200;
        if (message.length() <= maxLength) {
            return message;
        }
        
        return message.substring(0, maxLength) + "...";
    }
}