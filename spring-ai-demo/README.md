# Spring AI Demo

这是一个基于 Spring AI 1.0 和 PostgreSQL 17 + pgvector 的演示应用。

## 功能特性

- ✅ Spring AI 1.0 集成
- ✅ PostgreSQL 17 + pgvector 向量存储
- ✅ OpenAI GPT-4 聊天功能
- ✅ RAG (检索增强生成) 功能
- ✅ 同步和流式聊天接口
- ✅ 文档向量化和检索
- ✅ RESTful API 接口

## 快速开始

### 1. 启动数据库

```bash
# 启动 PostgreSQL 17 + pgvector
docker-compose up -d
```

### 2. 配置环境变量

复制 `.env` 文件并设置你的 OpenAI API Key：

```bash
cp .env .env.local
# 编辑 .env.local 文件，设置 OPENAI_API_KEY
```

### 3. 运行应用

```bash
# 使用 Maven 运行
mvn spring-boot:run

# 或者编译后运行
mvn clean package
java -jar target/spring-ai-demo-1.0.0.jar
```

### 4. 测试接口

应用启动后，访问 http://localhost:8080

#### 聊天接口

```bash
# 同步聊天
curl -X POST http://localhost:8080/api/chat/sync \
  -H "Content-Type: application/json" \
  -d '{"message": "你好，请介绍一下Spring AI"}'

# 流式聊天
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "请解释什么是向量数据库"}'
```

#### RAG 接口

```bash
# 添加文档到知识库
curl -X POST http://localhost:8080/api/rag/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Spring AI是一个为Java开发者设计的AI应用框架，它将AI能力无缝集成到Spring生态系统中。Spring AI提供了统一的API来访问各种AI模型，包括OpenAI、Azure OpenAI、Anthropic Claude等。",
    "title": "Spring AI简介",
    "category": "技术文档"
  }'

# 基于知识库问答
curl -X POST http://localhost:8080/api/rag/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "什么是Spring AI？"}'

# 搜索相关文档
curl "http://localhost:8080/api/rag/search?query=Spring%20AI&topK=3"

# 文档摘要
curl -X POST http://localhost:8080/api/rag/summarize \
  -H "Content-Type: application/json" \
  -d '{"query": "Spring AI"}'
```

#### 向量存储接口

```bash
# 添加文档到向量存储
curl -X POST http://localhost:8080/api/vector/add \
  -H "Content-Type: application/json" \
  -d '{
    "content": "向量数据库是一种专门用于存储和检索向量数据的数据库系统。",
    "metadata": {
      "title": "向量数据库介绍",
      "category": "数据库技术"
    }
  }'

# 搜索相似文档
curl "http://localhost:8080/api/vector/search?query=vector&topK=3&threshold=0.1"

# 按分类搜索
curl "http://localhost:8080/api/vector/search?query=database&topK=3&category=%E6%95%B0%E6%8D%AE%E5%BA%93%E6%8A%80%E6%9C%AF&threshold=0.1"

```

## 项目结构

```
src/
├── main/
│   ├── java/com/example/springai/
│   │   ├── SpringAiDemoApplication.java    # 主应用类
│   │   ├── config/
│   │   │   └── AIConfiguration.java        # AI 配置
│   │   ├── controller/
│   │   │   ├── ChatController.java         # 聊天控制器
│   │   │   ├── RAGController.java          # RAG 控制器
│   │   │   └── VectorController.java       # 向量存储控制器
│   │   └── service/
│   │       └── RAGService.java             # RAG 服务
│   └── resources/
│       └── application.yml                 # 应用配置
└── test/
    └── java/com/example/springai/
        └── SpringAiDemoApplicationTests.java
```

## 技术栈

- **Spring Boot 3.5.4**
- **Spring AI 1.0.1**
- **PostgreSQL 17**
- **pgvector 扩展**
- **OpenAI GPT-4**
- **Maven**

## 环境要求

- Java 17+
- Docker & Docker Compose
- Maven 3.6+
- OpenAI API Key

## 配置说明

主要配置通过环境变量控制，详见 `.env` 文件：

- `OPENAI_API_KEY`: OpenAI API 密钥
- `DATABASE_URL`: PostgreSQL 连接URL
- `PGVECTOR_*`: 向量存储相关配置

## 故障排除

### 1. 数据库连接失败

确保 PostgreSQL 容器正在运行：
```bash
docker-compose ps
```

### 2. OpenAI API 调用失败

检查 API Key 是否正确设置：
```bash
echo $OPENAI_API_KEY
```

### 3. 向量存储初始化失败

检查 pgvector 扩展是否正确安装：
```bash
docker-compose exec postgres psql -U postgres -c "SELECT * FROM pg_extension WHERE extname = 'vector';"
```

### 4. 向量搜索无结果

如果向量搜索返回空结果，请按以下步骤排查：

1. **检查向量存储状态**：
```bash
curl "http://localhost:8080/api/vector/stats"
```

2. **添加测试文档**：
```bash
curl -X POST http://localhost:8080/api/vector/add \
  -H "Content-Type: application/json" \
  -d '{
    "content": "向量数据库是一种专门用于存储和检索向量数据的数据库系统。",
    "metadata": {
      "title": "向量数据库介绍",
      "category": "数据库技术"
    }
  }'
```

3. **使用低阈值搜索**：
```bash
curl "http://localhost:8080/api/vector/search?query=vector&topK=3&threshold=0.1"
```

4. **运行完整测试脚本**：
```bash
# 快速测试
./quick-test.sh

# 完整测试
./test-vector-search.sh
```

### 5. 常见问题解决

**问题**：`Invalid character found in the request target`
**解决**：URL中的中文字符需要正确编码，使用 `%E5%90%91%E9%87%8F` 等编码格式

**问题**：向量搜索返回空结果
**解决**：
- 确保已添加文档到向量存储
- 降低相似度阈值（如 0.1）
- 检查查询词是否与存储内容相关
- 验证向量化模型是否正常工作

## 许可证

MIT License