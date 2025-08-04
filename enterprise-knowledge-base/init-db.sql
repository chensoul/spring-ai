-- 创建必要的扩展
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 创建向量存储表（可选，Spring AI 会自动创建）
CREATE TABLE IF NOT EXISTS enterprise_kb_vectors (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1536)
);

-- 创建 HNSW 索引以提高查询性能
CREATE INDEX IF NOT EXISTS enterprise_kb_vectors_embedding_idx
ON enterprise_kb_vectors USING hnsw (embedding vector_cosine_ops);

-- 创建文档表
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    uploaded_by VARCHAR(255) NOT NULL,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_time TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PROCESSING',
    error_message TEXT
);

-- 创建查询表
CREATE TABLE IF NOT EXISTS queries (
    id BIGSERIAL PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT,
    user_id VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    query_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PROCESSING'
);

-- 创建索引
CREATE INDEX idx_documents_uploaded_by ON documents(uploaded_by);
CREATE INDEX idx_documents_category ON documents(category);
CREATE INDEX idx_queries_user_id ON queries(user_id);
CREATE INDEX idx_queries_query_time ON queries(query_time DESC);