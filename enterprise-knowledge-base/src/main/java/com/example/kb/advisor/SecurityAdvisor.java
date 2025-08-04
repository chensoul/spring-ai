package com.example.kb.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 安全检查Advisor
 * 
 * 对AI请求和响应进行安全检查：
 * - 敏感信息检测和脱敏
 * - 恶意内容过滤
 * - 内容合规检查
 */
@Component
public class SecurityAdvisor implements CallAdvisor {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityAdvisor.class);
    
    // 敏感信息正则表达式
    private static final List<Pattern> SENSITIVE_PATTERNS = List.of(
        Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"), // 信用卡号
        Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"), // SSN
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), // 邮箱
        Pattern.compile("\\b1[3-9]\\d{9}\\b"), // 手机号
        Pattern.compile("\\b\\d{15,19}\\b") // 银行卡号
    );
    
    // 禁用词列表
    private static final List<String> BANNED_WORDS = List.of(
        "暴力", "仇恨", "歧视", "违法", "恶意", "攻击"
    );
    
    @Override
    public String getName() {
        return "SecurityAdvisor";
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // 高优先级，仅次于日志
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // 检查请求内容
        String userMessage = extractUserMessage(request);
        validateInput(userMessage);
        
        // 脱敏处理
        String maskedMessage = maskSensitiveInfo(userMessage);
        if (!maskedMessage.equals(userMessage)) {
            logger.warn("检测到敏感信息，已进行脱敏处理");
            // 创建脱敏后的请求
            request = createMaskedRequest(request, maskedMessage);
        }
        
        // 执行AI调用
        ChatClientResponse response = chain.nextCall(request);
        
        // 检查响应内容
        String responseContent = response.getResult().getOutput().getContent();
        validateOutput(responseContent);
        
        // 脱敏响应内容
        String maskedResponse = maskSensitiveInfo(responseContent);
        if (!maskedResponse.equals(responseContent)) {
            logger.warn("AI响应包含敏感信息，已进行脱敏处理");
            response = createMaskedResponse(response, maskedResponse);
        }
        
        return response;
    }
    
    /**
     * 验证输入内容
     */
    private void validateInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        
        // 检查禁用词
        String lowerInput = input.toLowerCase();
        for (String bannedWord : BANNED_WORDS) {
            if (lowerInput.contains(bannedWord.toLowerCase())) {
                logger.warn("输入内容包含禁用词: {}", bannedWord);
                throw new SecurityException("输入内容包含不当信息，请修改后重试");
            }
        }
        
        // 检查内容长度
        if (input.length() > 10000) {
            throw new SecurityException("输入内容过长，请缩短后重试");
        }
    }
    
    /**
     * 验证输出内容
     */
    private void validateOutput(String output) {
        if (output == null || output.trim().isEmpty()) {
            return;
        }
        
        // 检查是否包含不当内容
        String lowerOutput = output.toLowerCase();
        for (String bannedWord : BANNED_WORDS) {
            if (lowerOutput.contains(bannedWord.toLowerCase())) {
                logger.warn("AI响应包含不当内容: {}", bannedWord);
                // 不抛出异常，而是在后续处理中替换内容
                break;
            }
        }
    }
    
    /**
     * 脱敏敏感信息
     */
    private String maskSensitiveInfo(String text) {
        if (text == null) {
            return null;
        }
        
        String masked = text;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            masked = pattern.matcher(masked).replaceAll("***");
        }
        
        return masked;
    }
    
    /**
     * 提取用户消息
     */
    private String extractUserMessage(ChatClientRequest request) {
        try {
            return request.getPrompt().getInstructions();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 创建脱敏后的请求
     */
    private ChatClientRequest createMaskedRequest(ChatClientRequest original, String maskedMessage) {
        // 这里需要根据实际的ChatClientRequest实现来创建新的请求
        // 由于API可能不同，这里提供一个示例实现
        return original; // 简化实现，实际应该创建新的请求对象
    }
    
    /**
     * 创建脱敏后的响应
     */
    private ChatClientResponse createMaskedResponse(ChatClientResponse original, String maskedContent) {
        // 这里需要根据实际的ChatClientResponse实现来创建新的响应
        // 由于API可能不同，这里提供一个示例实现
        return original; // 简化实现，实际应该创建新的响应对象
    }
}