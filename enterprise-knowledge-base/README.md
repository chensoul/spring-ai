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
  -F "category=IT" \
  -F "userId=admin"
```

**获取文档列表**
```bash
# 获取用户所有文档
curl -X GET "http://localhost:8080/api/documents?userId=admin"

# 获取指定分类的文档
curl -X GET "http://localhost:8080/api/documents?userId=admin&category=IT"

# 获取文档详情
curl -X GET "http://localhost:8080/api/documents/8?userId=admin"
```

**删除文档**
```bash
curl -X DELETE "http://localhost:8080/api/documents/1?userId=admin"
```

**重新处理失败的文档**
```bash
curl -X POST "http://localhost:8080/api/documents/1/reprocess?userId=admin"
```

#### 2. 智能问答接口

**提交问题**
```bash
# 基础问答
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是 JVM？",
    "userId": "admin"
  }'

# 指定分类的问答
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "如何配置 PostgreSQL 数据库？",
    "category": "技术文档",
    "userId": "admin"
  }'
```

**获取查询历史**
```bash
# 获取最近的查询历史
curl -X GET "http://localhost:8080/api/query/history?userId=admin&limit=10"

# 获取指定分类的查询历史
curl -X GET "http://localhost:8080/api/query/history?userId=admin&category=技术文档&limit=5"
```

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
          model: text-embedding-3-small
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