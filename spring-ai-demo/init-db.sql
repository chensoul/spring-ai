-- 创建必要的扩展
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 创建向量存储表（Spring AI 会自动创建，这里仅作为参考）
-- CREATE TABLE IF NOT EXISTS vector_store (
--     id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
--     content text,
--     metadata json,
--     embedding vector(1536)
-- );

-- 创建 HNSW 索引以提高查询性能（Spring AI 会自动创建）
-- CREATE INDEX IF NOT EXISTS vector_store_embedding_idx 
-- ON vector_store USING hnsw (embedding vector_cosine_ops);