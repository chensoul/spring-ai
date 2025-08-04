# Enterprise Knowledge Base System

基于 Spring AI 1.0 的企业知识库系统，支持文档上传、智能问答和知识管理。

## 功能特性

- 📚 **文档管理**: 支持 PDF、Word、TXT 等多种格式文档上传
- 🤖 **智能问答**: 基于 RAG (检索增强生成) 的智能问答系统
- 🔍 **向量搜索**: 使用 PostgreSQL 17 + pgvector 进行高效向量搜索
- 👥 **用户管理**: 完整的用户注册、登录和权限管理
- 📊 **查询历史**: 记录和管理用户查询历史
- 🔒 **安全控制**: 基于 Spring Security 的安全认证和授权
- 📈 **监控指标**: 集成 Actuator 和 Prometheus 监控

## 技术栈

- **Spring Boot 3.5.4**: 应用框架
- **Spring AI 1.0.1**: AI 集成框架
- **PostgreSQL 17 + pgvector**: 向量数据库
- **Spring Security**: 安全框架
- **Spring Data JPA**: 数据访问层
- **Docker**: 容器化部署

## 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- OpenAI API Key

### 1. 克隆项目

```bash
git clone <repository-url>
cd enterprise-knowledge-base
```

### 2. 配置环境变量

创建 `.env` 文件：

```bash
# OpenAI 配置
OPENAI_API_KEY=your-openai-api-key
OPENAI_CHAT_MODEL=gpt-4o
OPENAI_EMBEDDING_MODEL=text-embedding-3-large

# 数据库配置
DATABASE_URL=jdbc:postgresql://localhost:5432/enterprise_kb
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# 应用配置
JPA_DDL_AUTO=update
```

### 3. 启动数据库

```bash
docker-compose up -d postgres
```

### 4. 构建应用

```bash
mvn clean package
```

### 5. 启动应用

```bash
# 使用 Docker Compose
docker-compose up -d

# 或者直接运行
java -jar target/enterprise-knowledge-base-1.0.0.jar
```

### 6. 访问应用

- 应用地址: http://localhost:8080
- 健康检查: http://localhost:8080/actuator/health
- 监控指标: http://localhost:8080/actuator/metrics

## 接口示例

### 使用 curl 测试接口

#### 1. 文档管理接口

**上传文档**
```bash
# 上传 PDF 文档
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/Users/chensoul/Downloads/jvm.pdf" \
  -F "category=技术文档" \
  -F "userId=user123"

# 上传 Word 文档
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/your/document.docx" \
  -F "category=产品文档" \
  -F "userId=user123"
```

**获取文档列表**
```bash
# 获取用户所有文档
curl -X GET "http://localhost:8080/api/documents?userId=user123"

# 获取指定分类的文档
curl -X GET "http://localhost:8080/api/documents?userId=user123&category=技术文档"

# 获取文档详情
curl -X GET "http://localhost:8080/api/documents/1?userId=user123"
```

**删除文档**
```bash
curl -X DELETE "http://localhost:8080/api/documents/1?userId=user123"
```

**重新处理失败的文档**
```bash
curl -X POST "http://localhost:8080/api/documents/1/reprocess?userId=user123"
```

#### 2. 智能问答接口

**提交问题**
```bash
# 基础问答
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是 Spring AI？",
    "userId": "user123"
  }'

# 指定分类的问答
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "如何配置 PostgreSQL 数据库？",
    "category": "技术文档",
    "userId": "user123"
  }'
```

**获取查询历史**
```bash
# 获取最近的查询历史
curl -X GET "http://localhost:8080/api/query/history?userId=user123&limit=10"

# 获取指定分类的查询历史
curl -X GET "http://localhost:8080/api/query/history?userId=user123&category=技术文档&limit=5"
```

#### 3. 用户管理接口

**用户注册**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**用户登录**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

#### 4. 系统监控接口

**健康检查**
```bash
curl -X GET http://localhost:8080/actuator/health
```

**获取应用信息**
```bash
curl -X GET http://localhost:8080/actuator/info
```

**获取监控指标**
```bash
curl -X GET http://localhost:8080/actuator/metrics
```

**获取 Prometheus 格式指标**
```bash
curl -X GET http://localhost:8080/actuator/prometheus
```

### 测试脚本示例

创建一个测试脚本 `test-api.sh`：

```bash
#!/bin/bash

# 设置基础 URL
BASE_URL="http://localhost:8080"
USER_ID="testuser123"

echo "=== 企业知识库 API 测试 ==="

# 1. 健康检查
echo "1. 检查应用健康状态..."
curl -s -X GET "$BASE_URL/actuator/health" | jq '.'

# 2. 上传测试文档
echo -e "\n2. 上传测试文档..."
UPLOAD_RESPONSE=$(curl -s -X POST "$BASE_URL/api/documents/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@test-document.pdf" \
  -F "category=测试文档" \
  -F "userId=$USER_ID")

echo "上传响应: $UPLOAD_RESPONSE"

# 3. 获取文档列表
echo -e "\n3. 获取文档列表..."
curl -s -X GET "$BASE_URL/api/documents?userId=$USER_ID" | jq '.'

# 4. 提交测试问题
echo -e "\n4. 提交测试问题..."
QUERY_RESPONSE=$(curl -s -X POST "$BASE_URL/api/query" \
  -H "Content-Type: application/json" \
  -d "{
    \"question\": \"这是一个测试问题\",
    \"userId\": \"$USER_ID\"
  }")

echo "查询响应: $QUERY_RESPONSE"

# 5. 获取查询历史
echo -e "\n5. 获取查询历史..."
curl -s -X GET "$BASE_URL/api/query/history?userId=$USER_ID&limit=5" | jq '.'

echo -e "\n=== 测试完成 ==="
```

### 使用 jq 格式化 JSON 响应

如果安装了 `jq`，可以更好地格式化 JSON 响应：

```bash
# 安装 jq (macOS)
brew install jq

# 安装 jq (Ubuntu/Debian)
sudo apt-get install jq

# 使用 jq 格式化响应
curl -s -X GET "http://localhost:8080/api/documents?userId=user123" | jq '.'
```

### 环境变量配置

创建 `.env` 文件来管理测试环境：

```bash
# .env
API_BASE_URL=http://localhost:8080
TEST_USER_ID=testuser123
TEST_CATEGORY=技术文档
```

然后在测试脚本中使用：

```bash
#!/bin/bash
source .env

curl -X GET "$API_BASE_URL/api/documents?userId=$TEST_USER_ID"
```

### 快速测试

项目提供了完整的测试脚本，可以直接运行：

```bash
# 给脚本添加执行权限
chmod +x test-api.sh

# 运行测试脚本
./test-api.sh
```

测试脚本会自动：
1. 检查服务状态
2. 测试文档上传功能
3. 测试文档查询功能
4. 测试智能问答功能
5. 测试用户管理功能
6. 测试监控端点

### 环境变量配置

复制环境变量示例文件：

```bash
cp env.example .env
```

然后编辑 `.env` 文件，配置相应的参数。

#### 知识库配置说明

- `DOCUMENT_STORAGE_PATH`: 文档存储路径（默认: ./uploads）
- `DOCUMENT_MAX_SIZE`: 文档最大大小，单位字节（默认: 52428800 = 50MB）
- `DOCUMENT_ALLOWED_TYPES`: 允许的文件类型，逗号分隔（默认: pdf,txt,docx,md）
- `CHUNK_SIZE`: 文档分块大小（默认: 1000）
- `CHUNK_OVERLAP`: 文档分块重叠大小（默认: 200）
- `VECTORIZATION_BATCH_SIZE`: 向量化批处理大小（默认: 10）


## 项目结构

```
enterprise-knowledge-base/
├── src/main/java/com/example/kb/
│   ├── KnowledgeBaseApplication.java    # 主应用类
│   ├── config/
│   │   ├── AIConfiguration.java        # AI 配置
│   │   └── SecurityConfiguration.java  # 安全配置
│   ├── controller/
│   │   ├── DocumentController.java     # 文档控制器
│   │   ├── QueryController.java        # 查询控制器
│   │   └── UserController.java         # 用户控制器
│   ├── service/
│   │   ├── DocumentService.java        # 文档服务
│   │   ├── QueryService.java           # 查询服务
│   │   └── UserService.java            # 用户服务
│   ├── model/
│   │   ├── DocumentEntity.java         # 文档实体
│   │   ├── QueryEntity.java            # 查询实体
│   │   ├── User.java                   # 用户实体
│   │   └── Records.java                # 记录类型
│   └── repository/
│       ├── DocumentRepository.java     # 文档仓库
│       ├── QueryRepository.java        # 查询仓库
│       └── UserRepository.java         # 用户仓库
├── src/main/resources/
│   └── application.yml                 # 应用配置
├── Dockerfile                          # Docker 镜像
├── docker-compose.yml                  # Docker Compose
├── init-db.sql                         # 数据库初始化
└── README.md                           # 项目文档
```

## 配置说明

### 数据库配置

项目使用 PostgreSQL 17 + pgvector 扩展作为向量数据库：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/enterprise_kb
    username: postgres
    password: postgres
  ai:
    vectorstore:
      pgvector:
        dimensions: 1536
        distance-type: COSINE_DISTANCE
        index-type: HNSW
```

### AI 模型配置

支持多种 AI 模型提供商：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-3-large
```

## 部署指南

### Docker 部署

1. 构建镜像：

```bash
docker build -t enterprise-knowledge-base .
```

2. 启动服务：

```bash
docker-compose up -d
```

### 生产环境部署

1. 配置生产环境变量
2. 使用外部 PostgreSQL 数据库
3. 配置反向代理 (Nginx)
4. 启用 HTTPS
5. 配置监控和日志收集

## 监控和运维

### 健康检查

应用提供健康检查端点：

```bash
curl http://localhost:8080/actuator/health
```

### 监控指标

集成 Prometheus 监控：

```bash
curl http://localhost:8080/actuator/prometheus
```

### 日志配置

日志级别配置：

```yaml
logging:
  level:
    org.springframework.ai: INFO
    com.example.kb: DEBUG
```

## 开发指南

### 本地开发

1. 启动 PostgreSQL 数据库
2. 配置 OpenAI API Key
3. 运行应用：`mvn spring-boot:run`

### 测试

```bash
mvn test
```

### 代码规范

- 使用 Java 17 特性
- 遵循 Spring Boot 最佳实践
- 添加适当的日志和异常处理

## 故障排除

### 常见问题

1. **数据库连接失败**
    - 检查 PostgreSQL 服务状态
    - 验证数据库连接配置

2. **AI 模型调用失败**
    - 检查 OpenAI API Key 配置
    - 验证网络连接

3. **文档上传失败**
    - 检查文件大小限制
    - 验证文件格式支持

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 创建 Pull Request

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交 Issue 或联系开发团队。