package com.example.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Spring AI 配置类
 * <p>
 * 配置 ChatClient 和 VectorStore
 */
@Configuration
public class AIConfiguration {

    /**
     * 配置 ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是一个专业的AI助手，请用中文回答问题。")
                .build();
    }

    /**
     * 配置 PgVectorStore (生产环境)
     * <p>
     * 如果不使用自动配置，可以手动配置 VectorStore
     */
    @Bean
    @Profile("!test")
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1536)  // OpenAI embedding dimensions
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10000)
                .build();
    }

    /**
     * 配置 SimpleVectorStore (测试环境)
     * <p>
     * 使用内存向量存储，避免数据库依赖
     */
    @Bean
    @Profile("test")
    public VectorStore testVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}