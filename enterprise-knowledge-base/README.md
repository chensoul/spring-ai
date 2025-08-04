# 企业知识库系统 (Enterprise Knowledge Base)

基于Spring AI框架构建的企业级RAG（检索增强生成）知识库系统，提供智能文档管理和问答服务。

## 📋 目录

- [功能特性](#-功能特性)
- [技术栈](#️-技术栈)
- [项目结构](#-项目结构)
- [快速开始](#-快速开始)
- [API使用指南](#-api使用指南)
- [配置说明](#-配置说明)
- [Docker部署](#-docker部署)
- [测试](#-测试)
- [监控运维](#-监控运维)
- [安全配置](#-安全配置)
- [性能优化](#-性能优化)
- [扩展指南](#-扩展指南)
- [贡献指南](#-贡献指南)

## 🚀 功能特性

### 核心功能
- **RAG智能问答**：基于向量检索的增强生成，提供准确的知识问答
- **文档管理**：支持PDF、Word、文本等多种格式的文档上传和处理
- **向量搜索**：基于语义相似度的智能搜索，替代传统关键词搜索
- **用户权限**：完善的用户认证和权限管理体系

### 技术特性
- **多模型支持**：OpenAI、Anthropic、Ollama等多种AI模型
- **向量存储**：Redis、Elasticsearch等向量数据库支持
- **异步处理**：文档处理和向量化的异步任务处理
- **安全防护**：内容安全检查、敏感信息脱敏
- **监控观测**：完整的日志记录和性能监控

## 🛠️ 技术栈

- **框架**：Spring Boot 3.5.4 + Spring AI 1.0.0
- **数据库**：H2 (开发) / PostgreSQL (生产)
- **向量存储**：Redis Vector Store
- **安全**：Spring Security
- **文档处理**：Apache Tika、PDF Reader
- **监控**：Spring Actuator + Prometheus

## 📁 项目结构

### 目录结构

```
enterprise-knowledge-base/
├── src/
│   ├── main/
│   │   ├── java/com/example/kb/
│   │   │   ├── KnowledgeBaseApplication.java      # 主应用类
│   │   │   ├── config/                            # 配置类
│   │   │   │   ├── AIConfiguration.java           # AI配置
│   │   │   │   ├── SecurityConfiguration.java     # 安全配置
│   │   │   │   └── AsyncConfiguration.java        # 异步配置
│   │   │   ├── model/                             # 数据模型
│   │   │   │   ├── DocumentEntity.java            # 文档实体
│   │   │   │   └── QueryEntity.java               # 查询实体
│   │   │   ├── dto/                               # 数据传输对象
│   │   │   │   ├── DocumentUploadResult.java      # 上传结果
│   │   │   │   ├── QueryRequest.java              # 查询请求
│   │   │   │   ├── QueryResult.java               # 查询结果
│   │   │   │   └── DocumentInfo.java              # 文档信息
│   │   │   ├── repository/                        # 数据访问层
│   │   │   │   ├── DocumentRepository.java        # 文档仓库
│   │   │   │   └── QueryRepository.java           # 查询仓库
│   │   │   ├── service/                           # 业务逻辑层
│   │   │   │   ├── DocumentService.java           # 文档服务
│   │   │   │   └── QueryService.java              # 查询服务
│   │   │   ├── controller/                        # 控制器层
│   │   │   │   ├── DocumentController.java        # 文档控制器
│   │   │   │   ├── QueryController.java           # 查询控制器
│   │   │   │   └── HomeController.java            # 主页控制器
│   │   │   └── advisor/                           # AI拦截器
│   │   │       ├── LoggingAdvisor.java            # 日志拦截器
│   │   │       └── SecurityAdvisor.java           # 安全拦截器
│   │   └── resources/
│   │       ├── application.yml                    # 主配置文件
│   │       └── templates/
│   │           └── index.html                     # 主页模板
│   └── test/
│       ├── java/com/example/kb/
│       │   ├── KnowledgeBaseApplicationTests.java # 应用测试
│       │   └── service/
│       │       └── DocumentServiceTest.java       # 服务测试
│       └── resources/
│           └── application-test.yml               # 测试配置
├── pom.xml                                        # Maven配置
├── Dockerfile                                     # Docker镜像配置
├── docker-compose.yml                            # Docker编排配置
├── start.sh                                      # 启动脚本
└── README.md                                      # 项目说明
```

### 架构设计

#### 分层架构
```
┌─────────────────────────────────────┐
│           Presentation Layer        │  ← Controllers, REST APIs
├─────────────────────────────────────┤
│            Service Layer            │  ← Business Logic, AI Integration
├─────────────────────────────────────┤
│         Data Access Layer           │  ← Repositories, JPA
├─────────────────────────────────────┤
│          Infrastructure             │  ← Database, Vector Store, AI Models
└─────────────────────────────────────┘
```

#### 核心组件

**1. AI集成层**
- **ChatClient**: Spring AI的核心聊天客户端
- **VectorStore**: 向量存储（Redis）
- **DocumentReader**: 文档读取器（PDF、Tika）
- **TextSplitter**: 文本分割器
- **Advisors**: AI请求拦截器链

**2. 业务服务层**
- **DocumentService**: 文档管理服务（上传、处理、向量化）
- **QueryService**: 查询服务（RAG问答、历史管理）

**3. 数据持久层**
- **DocumentEntity**: 文档元数据
- **QueryEntity**: 查询历史
- **DocumentRepository**: 文档数据访问
- **QueryRepository**: 查询数据访问

**4. 安全和监控**
- **SecurityConfiguration**: Spring Security配置
- **LoggingAdvisor**: 请求日志记录
- **SecurityAdvisor**: 内容安全检查
- **Actuator**: 健康检查和监控

### 数据流程

#### 文档上传流程
```
用户上传文档 → 文件验证 → 保存到磁盘 → 创建数据库记录 
     ↓
异步处理 → 文档解析 → 文本分割 → 向量化 → 存储到VectorStore
     ↓
更新处理状态 → 完成
```

#### 查询处理流程
```
用户提问 → 安全检查 → 向量检索 → 构建上下文 → AI生成回答
     ↓
日志记录 → 保存查询历史 → 返回结果
```

### 核心功能模块

#### 1. 文档管理模块
**功能特性:**
- 多格式文档上传（PDF、Word、文本等）
- 异步文档处理和向量化
- 文档分类和标签管理
- 文档搜索和过滤
- 批量操作支持

**API端点:**
- `POST /api/documents/upload` - 上传文档
- `GET /api/documents` - 获取文档列表
- `GET /api/documents/{id}` - 获取文档详情
- `DELETE /api/documents/{id}` - 删除文档
- `GET /api/documents/search` - 搜索文档

#### 2. 智能查询模块
**功能特性:**
- RAG（检索增强生成）问答
- 多轮对话支持
- 查询历史管理
- 相似度搜索
- 实时流式响应

**API端点:**
- `POST /api/query` - 执行查询
- `POST /api/query/quick` - 快速查询
- `GET /api/query/history` - 查询历史
- `GET /api/query/session/{id}` - 会话历史
- `GET /api/query/statistics` - 查询统计

#### 3. 用户管理模块
**功能特性:**
- 基于角色的访问控制
- 用户认证和授权
- 权限细粒度控制
- 安全审计日志

**默认用户:**
- 管理员: admin/admin123 (ADMIN, USER)
- 普通用户: user/user123 (USER)

#### 4. 监控运维模块
**功能特性:**
- 应用健康检查
- 性能指标监控
- 日志管理
- 错误追踪

**监控端点:**
- `/api/actuator/health` - 健康检查
- `/api/actuator/metrics` - 性能指标
- `/api/actuator/prometheus` - Prometheus指标

## 📋 系统要求

- Java 17+
- Maven 3.6+
- Redis 6.0+ (用于向量存储)
- PostgreSQL 12+ (生产环境)

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd enterprise-knowledge-base
```

### 2. 配置环境变量
```bash
# AI模型配置
export OPENAI_API_KEY=your-openai-api-key
export ANTHROPIC_API_KEY=your-anthropic-api-key

# 数据库配置（可选，默认使用H2）
export DATABASE_URL=jdbc:postgresql://localhost:5432/knowledge_base
export DATABASE_USERNAME=your-username
export DATABASE_PASSWORD=your-password

# Redis配置（可选，默认localhost:6379）
export REDIS_URL=redis://localhost:6379
```

### 3. 启动Redis（如果使用向量存储）
```bash
# 使用Docker启动Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# 或使用本地Redis
redis-server
```

### 4. 编译和运行
```bash
# 使用智能启动脚本（推荐）
./start.sh

# 或手动编译运行
mvn clean compile
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/enterprise-knowledge-base-1.0.0.jar
```

### 5. 访问应用
- **应用首页**：http://localhost:8080/api
- **健康检查**：http://localhost:8080/api/actuator/health
- **H2控制台**：http://localhost:8080/api/h2-console
- **API文档**：http://localhost:8080/api/swagger-ui.html

### 6. 默认账户
- **管理员**：admin / admin123
- **普通用户**：user / user123## 📖 API使用指南

### 文档管理

#### 上传文档
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -F "file=@document.pdf" \
  -F "category=技术文档"
```

#### 获取文档列表
```bash
curl -X GET http://localhost:8080/api/documents \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

#### 删除文档
```bash
curl -X DELETE http://localhost:8080/api/documents/1 \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### 智能查询

#### 执行RAG查询
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "什么是Spring AI？",
    "category": "技术文档",
    "useRag": true
  }'
```

#### 快速查询
```bash
curl -X POST "http://localhost:8080/api/query/quick?question=Spring AI的主要特性" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

#### 获取查询历史
```bash
curl -X GET http://localhost:8080/api/query/history?limit=10 \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

## 🔧 配置说明

### 主要配置项

```yaml
# AI模型配置
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

# 向量存储配置
    vectorstore:
      redis:
        uri: ${REDIS_URL:redis://localhost:6379}
        index: enterprise-kb-index

# 应用配置
app:
  knowledge-base:
    document:
      max-size: 50MB
      allowed-types: pdf,txt,docx,md
      storage-path: ./uploads
    
    query:
      max-results: 5
      similarity-threshold: 0.75
```

### 环境配置

#### 开发环境
- 使用H2内存数据库
- 启用详细日志
- 开放H2控制台

#### 生产环境
- 使用PostgreSQL数据库
- 配置Redis集群
- 启用安全配置
- 配置监控和告警

## 🐳 Docker部署

### 1. 构建镜像
```bash
# 构建应用镜像
docker build -t enterprise-kb:latest .
```

### 2. 使用Docker Compose
```bash
# 启动完整环境
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down
```

### 3. Docker Compose配置
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - DATABASE_URL=jdbc:postgresql://postgres:5432/knowledge_base
      - REDIS_URL=redis://redis:6379
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: knowledge_base
      POSTGRES_USER: kb_user
      POSTGRES_PASSWORD: kb_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

## 🧪 测试

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=DocumentServiceTest

# 运行集成测试
mvn verify
```

### 测试覆盖率
```bash
# 生成测试覆盖率报告
mvn jacoco:report

# 查看报告
open target/site/jacoco/index.html
```

### 测试策略

#### 单元测试
- Service层业务逻辑测试
- Repository层数据访问测试
- Utility类功能测试

#### 集成测试
- Controller层API测试
- 数据库集成测试
- AI模型集成测试

#### 端到端测试
- 完整业务流程测试
- 用户场景测试
- 性能测试

## 📊 监控运维

### 健康检查
```bash
# 应用健康状态
curl http://localhost:8080/api/actuator/health

# 详细健康信息
curl http://localhost:8080/api/actuator/health \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### 指标监控
```bash
# Prometheus指标
curl http://localhost:8080/api/actuator/prometheus

# 应用指标
curl http://localhost:8080/api/actuator/metrics
```

### 日志管理
```bash
# 查看应用日志
tail -f logs/application.log

# 调整日志级别
curl -X POST http://localhost:8080/api/actuator/loggers/com.example.kb \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

## 🔒 安全配置

### 认证和授权
- 基于Spring Security的用户认证
- 角色基础的访问控制（RBAC）
- API端点的细粒度权限控制

### 内容安全
- 敏感信息检测和脱敏
- 恶意内容过滤
- 文件类型和大小限制

### 数据保护
- 传输加密（HTTPS）
- 数据库连接加密
- API密钥安全管理

## 🚀 性能优化

### 应用层优化
- 异步文档处理
- 连接池配置
- 缓存策略
- 批量操作

### 数据库优化
- 索引优化
- 查询优化
- 连接池调优

### 向量存储优化
- 批量向量化
- 索引优化
- 相似度阈值调整

## 📚 扩展指南

### 添加新的AI模型
1. 在`AIConfiguration`中配置新模型
2. 更新`QueryService`支持模型选择
3. 添加相应的配置项

### 支持新的文档格式
1. 添加新的`DocumentReader`实现
2. 更新文件类型验证
3. 测试新格式的处理效果

### 集成新的向量存储
1. 添加新的`VectorStore`配置
2. 更新数据迁移脚本
3. 性能测试和优化

### 添加新的业务功能
1. 创建相应的Entity、DTO、Repository
2. 实现Service业务逻辑
3. 添加Controller API端点
4. 编写测试用例

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 📄 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 支持

如果您有任何问题或建议，请：

1. 查看 [FAQ](docs/FAQ.md)
2. 搜索 [Issues](../../issues)
3. 创建新的 [Issue](../../issues/new)
4. 联系维护者

## 🙏 致谢

感谢以下开源项目：

- [Spring AI](https://spring.io/projects/spring-ai) - AI应用开发框架
- [Spring Boot](https://spring.io/projects/spring-boot) - 应用框架
- [Redis](https://redis.io/) - 向量存储
- [Apache Tika](https://tika.apache.org/) - 文档处理

---

**企业知识库系统** - 让知识管理更智能 🚀