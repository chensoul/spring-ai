package com.example.kb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.knowledge-base")
public class KnowledgeBaseProperties {

    private Document document = new Document();
    private Vectorization vectorization = new Vectorization();

    public static class Document {
        private String storagePath = "./uploads";
        private long maxSize = 52428800; // 50MB
        private List<String> allowedTypes = List.of("pdf", "txt", "docx", "md");

        public String getStoragePath() {
            return storagePath;
        }

        public void setStoragePath(String storagePath) {
            this.storagePath = storagePath;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public List<String> getAllowedTypes() {
            return allowedTypes;
        }

        public void setAllowedTypes(List<String> allowedTypes) {
            this.allowedTypes = allowedTypes;
        }
    }

    public static class Vectorization {
        private int chunkSize = 1000;
        private int chunkOverlap = 200;
        private int batchSize = 10;

        public int getChunkSize() {
            return chunkSize;
        }

        public void setChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
        }

        public int getChunkOverlap() {
            return chunkOverlap;
        }

        public void setChunkOverlap(int chunkOverlap) {
            this.chunkOverlap = chunkOverlap;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Vectorization getVectorization() {
        return vectorization;
    }

    public void setVectorization(Vectorization vectorization) {
        this.vectorization = vectorization;
    }
} 