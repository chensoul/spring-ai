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
        // 优化分割参数以提高性能
        int chunkSize = Math.max(kbProperties.getVectorization().getChunkSize(), 800);
        int minChunkSizeChars = Math.max(kbProperties.getVectorization().getMinChunkSizeChars(), 350);
        int minChunkLengthToEmbed = Math.max(kbProperties.getVectorization().getMinChunkLengthToEmbed(), 5);
        int maxNumChunks = Math.max(kbProperties.getVectorization().getMaxNumChunks(), 10000);

        return new TokenTextSplitter(
                chunkSize,           // 增加块大小，减少总块数
                minChunkSizeChars,   // 增加最小块大小，避免过小的块
                minChunkLengthToEmbed, // 增加最小嵌入长度
                maxNumChunks,        // 增加最大块数限制
                false // recursive
        );
    }
}