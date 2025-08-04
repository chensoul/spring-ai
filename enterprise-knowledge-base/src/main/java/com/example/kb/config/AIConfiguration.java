package com.example.kb.config;

import com.example.kb.advisor.LoggingAdvisor;
import com.example.kb.advisor.SecurityAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfiguration {

    private final KnowledgeBaseProperties kbProperties;

    public AIConfiguration(KnowledgeBaseProperties kbProperties) {
        this.kbProperties = kbProperties;
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        return builder
                .defaultAdvisors(
                        new LoggingAdvisor(),
                        new SecurityAdvisor(),
                        QuestionAnswerAdvisor.builder(vectorStore).build()
                )
                .build();
    }

    @Bean
    public TextSplitter textSplitter() {
        // 使用配置的 chunk-size 和 chunk-overlap
        return new TokenTextSplitter(
            kbProperties.getVectorization().getChunkSize(),
            kbProperties.getVectorization().getChunkOverlap(),
            0, // min chunk size
            0, // max chunk size
            false // recursive
        );
    }
}