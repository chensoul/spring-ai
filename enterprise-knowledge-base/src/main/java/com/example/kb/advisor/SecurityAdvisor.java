package com.example.kb.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityAdvisor implements CallAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(SecurityAdvisor.class);

    private final List<String> sensitivePatterns = List.of(
            "\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b", // 信用卡号
            "\\b\\d{3}-\\d{2}-\\d{4}\\b", // SSN
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b" // 邮箱
    );

    @Override
    public String getName() {
        return "SecurityAdvisor";
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        return chain.nextCall(request);
    }

    private boolean containsSensitiveInfo(String text) {
        return sensitivePatterns.stream()
                .anyMatch(pattern -> text.matches(".*" + pattern + ".*"));
    }
}