package com.example.kb.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class LoggingAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvisor.class);

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {

        logger.info("AI请求开始");
        long startTime = System.currentTimeMillis();

        try {
            ChatClientResponse response = chain.nextCall(request);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("AI请求完成，耗时: {}ms", duration);
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("AI请求失败，耗时: {}ms, 错误: {}", duration, e.getMessage());
            throw e;
        }
    }
}