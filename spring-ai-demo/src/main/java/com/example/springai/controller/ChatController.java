package com.example.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 聊天控制器
 * 
 * 提供同步和流式聊天接口
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 同步聊天接口
     */
    @PostMapping("/sync")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String response = chatClient.prompt()
            .user(request.getMessage())
            .call()
            .content();
        
        return new ChatResponse(response);
    }

    /**
     * 流式聊天接口
     */
    @PostMapping(value = "/stream", produces = "text/plain")
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return chatClient.prompt()
            .user(request.getMessage())
            .stream()
            .content();
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public String health() {
        return "Spring AI Demo is running!";
    }

    // 请求和响应 DTO
    public static class ChatRequest {
        private String message;

        public ChatRequest() {}

        public ChatRequest(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class ChatResponse {
        private String response;

        public ChatResponse() {}

        public ChatResponse(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
}