# Spring AI框架全面技术指南

## 概述

本指南基于Spring AI框架的最新功能特性，提供从基础概念到高级应用的完整技术指南。包括框架核心原理、开发环境搭建、实际开发指南、进阶技能和完整的实战项目示例，帮助开发者快速掌握Spring AI并构建生产级AI应用。

## 目录

1. [框架概述与核心原理](#1-框架概述与核心原理)
2. [核心技术实现原理](#2-核心技术实现原理)
3. [开发环境搭建与配置](#3-开发环境搭建与配置)
4. [实际开发指南](#4-实际开发指南)
5. [进阶开发技能](#5-进阶开发技能)
6. [实战项目示例](#6-实战项目示例)
7. [最佳实践和生产部署](#7-最佳实践和生产部署)

## 1. 框架概述与核心原理

### 1.1 Spring AI 1.0 新特性概览

Spring AI 1.0 正式版带来了许多重要的改进和新特性：

**核心改进：**
- **稳定的 API**：1.0 版本标志着 API 的稳定，向后兼容性得到保证
- **增强的向量存储支持**：新增对 Oracle、Neo4j、MariaDB 等数据库的支持
- **改进的自动配置**：更细粒度的自动配置模块，支持按需加载
- **观察性增强**：改进的日志记录和监控配置选项
- **MCP 协议支持**：新增 Model Context Protocol 支持，增强模型互操作性

**依赖管理改进：**

| 组件类型 | 旧版本依赖 | Spring AI 1.0 依赖 |
|----------|------------|-------------------|
| Redis 向量存储 | `spring-ai-redis-store-spring-boot-starter` | `spring-ai-starter-vector-store-redis` |
| PostgreSQL 向量存储 | `spring-ai-pgvector-store` | `spring-ai-starter-vector-store-pgvector` |
| OpenAI 模型 | `spring-ai-openai-spring-boot-starter` | `spring-ai-starter-model-openai` |
| Anthropic 模型 | `spring-ai-anthropic-spring-boot-starter` | `spring-ai-starter-model-anthropic` |
| Ollama 模型 | `spring-ai-ollama-spring-boot-starter` | `spring-ai-starter-model-ollama` |

- 新的 starter 命名规范：`spring-ai-starter-vector-store-*` 和 `spring-ai-starter-model-*`
- 更精确的依赖管理，减少不必要的传递依赖
- 支持 Spring Boot 3.3+ 和 Java 17+

**向量存储增强：**
- **PostgreSQL 17 + pgvector**：完整支持最新的 PostgreSQL 17 和 pgvector 扩展
- **Oracle AI Vector Search**：企业级向量搜索支持
- **Neo4j Vector Index**：图数据库向量搜索集成
- **MariaDB Vector**：开源关系型数据库向量支持

**配置属性重命名：**
```properties
# 旧版本（已废弃）
spring.ai.chat.observations.include-prompt
spring.ai.chat.observations.include-completion

# 新版本（1.0+）
spring.ai.chat.observations.log-prompt
spring.ai.chat.observations.log-completion
```

### 1.2 Spring AI架构设计理念

Spring AI是一个为Java开发者设计的AI应用框架，它将AI能力无缝集成到Spring生态系统中。其核心设计理念包括：

**设计原则：**
- **模型可移植性**：统一API支持多种AI模型提供商，避免供应商锁定
- **Spring生态集成**：与Spring Boot、Spring Cloud无缝集成
- **企业级特性**：内置安全、监控、扩展性支持
- **开发者友好**：自动配置、依赖注入、类型安全

**架构优势：**
- 降低AI应用开发门槛
- 提供生产级的稳定性和性能
- 支持快速原型和迭代开发
- 丰富的生态系统和社区支持

### 1.2 核心组件架构

Spring AI框架包含以下核心组件：

```mermaid
graph TB
    A[ChatClient API] --> B[ChatModel]
    A --> C[Advisors API]
    B --> D[OpenAI/Anthropic/Ollama]
    C --> E[Logging/Security/RAG]
    F[VectorStore] --> G[Redis/Elasticsearch/PGVector]
    H[EmbeddingModel] --> I[OpenAI/Azure/Ollama]
    J[Function Calling] --> K[@Tool Annotations]
    L[Auto Configuration] --> M[Spring Boot Starters]
```

**核心组件说明：**

1. **ChatClient API**：流式API，类似于WebClient/RestClient
2. **ChatModel**：模型抽象层，支持同步和流式调用
3. **VectorStore**：向量存储抽象，支持20+种数据库
4. **EmbeddingModel**：嵌入模型集成
5. **Advisors API**：拦截器模式，支持请求/响应处理
6. **Function Calling**：工具调用机制
7. **Auto Configuration**：Spring Boot自动配置

### 1.3 与传统AI开发方式的区别

**传统AI开发方式：**
- 直接调用AI模型API
- 手动处理HTTP请求和响应
- 自行实现重试、错误处理
- 缺乏统一的抽象层

**Spring AI开发方式：**
- 统一的ChatClient API
- 自动配置和依赖注入
- 内置重试、错误处理机制
- 丰富的扩展点和拦截器

**对比示例：**

```java
// 传统方式
public class TraditionalAIService {
    private final RestTemplate restTemplate;

    public String chat(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> request = Map.of(
            "model", "gpt-4o",
            "messages", List.of(Map.of("role", "user", "content", message))
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                entity,
                Map.class
            );

            return extractContent(response.getBody());
        } catch (Exception e) {
            // 手动错误处理
            throw new RuntimeException("AI调用失败", e);
        }
    }
}

// Spring AI方式
@Service
public class SpringAIService {
    private final ChatClient chatClient;

    public SpringAIService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String chat(String message) {
        return chatClient.prompt(message)
            .call()
            .content();
    }
}
```

### 1.4 在Spring生态系统中的定位

Spring AI在Spring生态系统中的定位：

- **Spring Boot**：提供自动配置和starter依赖
- **Spring Cloud**：支持微服务架构和服务发现
- **Spring Security**：集成安全认证和授权
- **Spring Data**：与数据访问层无缝集成
- **Spring WebFlux**：支持响应式编程模型

## 2. 核心技术实现原理

### 2.1 ChatClient API的工作机制和调用流程

ChatClient API是Spring AI的核心接口，采用流式API设计，提供类型安全的AI交互方式。

**API设计原理：**

```java
public interface ChatClient {
    // 流式API设计
    PromptSpec prompt();
    PromptSpec prompt(String text);

    interface PromptSpec {
        MessageSpec system(String text);
        MessageSpec user(String text);
        CallSpec call();
        StreamSpec stream();
    }

    interface CallSpec {
        ChatResponse chatResponse();
        String content();
        <T> T entity(Class<T> entityClass);
    }
}
```

**调用流程分析：**

1. **请求构建阶段**：
   ```java
   chatClient.prompt("用户输入")
       .system("系统提示")
       .user(u -> u.text("用户消息").param("key", "value"))
   ```

2. **Advisor链处理**：
   ```java
   // 请求经过Advisor链处理
   CallAdvisorChain -> LoggingAdvisor -> SecurityAdvisor -> RAGAdvisor -> ChatModel
   ```

3. **模型调用**：
   ```java
   // 最终调用底层模型API
   ChatModel.call(Prompt) -> OpenAI/Anthropic/Ollama API
   ```

4. **响应处理**：
   ```java
   // 响应经过Advisor链反向处理
   ChatModel -> RAGAdvisor -> SecurityAdvisor -> LoggingAdvisor -> Client
   ```

**完整示例：**

```java
@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultAdvisors(
                new LoggingAdvisor(),
                new SecurityAdvisor(),
                new RAGAdvisor()
            )
            .build();
    }

    // 同步调用
    public String simpleChat(String message) {
        return chatClient.prompt(message)
            .call()
            .content();
    }

    // 结构化输出
    public WeatherReport getWeatherReport(String city) {
        return chatClient.prompt()
            .system("你是一个天气分析专家")
            .user("分析{city}的天气情况", Map.of("city", city))
            .call()
            .entity(WeatherReport.class);
    }

    // 流式响应
    public Flux<String> streamChat(String message) {
        return chatClient.prompt(message)
            .stream()
            .content();
    }

    public record WeatherReport(
        String city,
        String temperature,
        String condition,
        List<String> recommendations
    ) {}
}
```

### 2.2 Vector Store向量存储的实现原理和数据流

Vector Store是Spring AI中用于存储和检索向量嵌入的核心组件。

**核心接口设计：**

```java
public interface VectorStore {
    // 添加文档
    void add(List<Document> documents);

    // 删除文档
    Optional<Boolean> delete(List<String> idList);

    // 相似性搜索
    List<Document> similaritySearch(SearchRequest request);

    // 相似性搜索（带分数）
    List<Document> similaritySearch(String query);
}
```

**数据流分析：**

1. **文档处理流程**：
   ```java
   原始文档 -> 文本分割 -> 向量化 -> 存储到向量数据库
   ```

2. **检索流程**：
   ```java
   查询文本 -> 向量化 -> 相似性搜索 -> 返回相关文档
   ```

**完整实现示例：**

```java
@Service
public class DocumentService {
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final DocumentReader documentReader;

    // 文档添加流程
    public void addDocuments(List<Resource> resources) {
        List<Document> documents = new ArrayList<>();

        for (Resource resource : resources) {
            // 1. 读取文档
            List<Document> docs = documentReader.get();

            // 2. 文档分割
            List<Document> splitDocs = textSplitter.apply(docs);

            // 3. 添加元数据
            splitDocs.forEach(doc -> {
                doc.getMetadata().put("source", resource.getFilename());
                doc.getMetadata().put("timestamp", Instant.now().toString());
            });

            documents.addAll(splitDocs);
        }

        // 4. 批量存储（自动向量化）
        vectorStore.add(documents);
    }

    // 相似性搜索
    public List<Document> searchSimilarDocuments(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(0.7)
            .filterExpression("source != 'temp'")
            .build();

        return vectorStore.similaritySearch(request);
    }

    // 高级搜索（带过滤）
    public List<Document> searchWithFilter(String query, Map<String, Object> filters) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();

        Expression filterExpression = builder.and(
            filters.entrySet().stream()
                .map(entry -> builder.eq(entry.getKey(), entry.getValue()))
                .toArray(Expression[]::new)
        ).build();

        SearchRequest request = SearchRequest.builder()
            .query(query)
            .topK(10)
            .filterExpression(filterExpression)
            .build();

        return vectorStore.similaritySearch(request);
    }
}
```

**向量存储选择指南：**

| 数据库 | 适用场景 | 优势 | 劣势 |
|--------|----------|------|------|
| Redis | 中小型应用 | 简单易用，性能好 | 内存限制 |
| Elasticsearch | 企业级应用 | 功能丰富，可扩展 | 复杂度高 |
| PGVector | 关系型数据 | 与现有数据库集成，支持PostgreSQL 17 | 性能一般 |
| Pinecone | 云端应用 | 专业向量数据库 | 成本较高 |
| Oracle | 企业级应用 | 企业级特性，高性能 | 成本高，复杂度高 |
| Neo4j | 图数据库应用 | 图关系查询，知识图谱 | 学习成本高 |

### 2.3 Embedding Models嵌入模型的集成方式

Embedding Models负责将文本转换为向量表示，是RAG应用的核心组件。

**核心接口：**

```java
public interface EmbeddingModel extends Model<EmbeddingRequest, EmbeddingResponse> {
    // 单个文本嵌入
    EmbeddingResponse embedForResponse(List<String> texts);

    // 批量嵌入
    List<Double> embed(String text);

    // 文档嵌入
    List<Double> embed(Document document);
}
```

**集成实现：**

```java
@Configuration
public class EmbeddingConfiguration {

    // OpenAI嵌入模型
    @Bean
    @ConditionalOnProperty("spring.ai.openai.embedding.enabled")
    public EmbeddingModel openAiEmbeddingModel() {
        return new OpenAiEmbeddingModel(openAiApi,
            OpenAiEmbeddingOptions.builder()
                .model("text-embedding-3-large")
                .build());
    }

    // Azure OpenAI嵌入模型
    @Bean
    @ConditionalOnProperty("spring.ai.azure.openai.embedding.enabled")
    public EmbeddingModel azureEmbeddingModel() {
        return new AzureOpenAiEmbeddingModel(azureOpenAiApi,
            AzureOpenAiEmbeddingOptions.builder()
                .deploymentName("text-embedding-ada-002")
                .build());
    }

    // Ollama本地嵌入模型
    @Bean
    @ConditionalOnProperty("spring.ai.ollama.embedding.enabled")
    public EmbeddingModel ollamaEmbeddingModel() {
        return new OllamaEmbeddingModel(ollamaApi,
            OllamaOptions.builder()
                .model("nomic-embed-text")
                .build());
    }
}
```

**使用示例：**

```java
@Service
public class EmbeddingService {
    private final EmbeddingModel embeddingModel;

    // 文本嵌入
    public List<Double> embedText(String text) {
        return embeddingModel.embed(text);
    }

    // 批量嵌入
    public List<List<Double>> embedTexts(List<String> texts) {
        EmbeddingResponse response = embeddingModel.embedForResponse(texts);
        return response.getResults().stream()
            .map(embedding -> embedding.getOutput())
            .collect(Collectors.toList());
    }

    // 文档相似度计算
    public double calculateSimilarity(String text1, String text2) {
        List<Double> embedding1 = embedText(text1);
        List<Double> embedding2 = embedText(text2);

        return cosineSimilarity(embedding1, embedding2);
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA += Math.pow(a.get(i), 2);
            normB += Math.pow(b.get(i), 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
```

### 2.4 Function Calling工具调用的底层实现

Function Calling允许AI模型调用外部工具和服务，是构建智能应用的关键技术。

**核心机制：**

1. **工具注册**：通过@Tool注解自动发现和注册工具
2. **参数绑定**：自动将AI模型输出绑定到方法参数
3. **结果处理**：将方法返回值转换为AI模型可理解的格式
4. **异常处理**：统一的错误处理和重试机制

**实现原理：**

```java
// 1. 工具定义
@Service
public class WeatherService {

    @Tool(description = "获取指定城市的天气信息")
    public WeatherInfo getWeather(
        @Parameter(description = "城市名称") String city,
        @Parameter(description = "温度单位", required = false) String unit
    ) {
        // 调用外部天气API
        return weatherApiClient.getWeather(city, unit);
    }

    @Tool(description = "获取天气预警信息")
    public List<WeatherAlert> getWeatherAlerts(String region) {
        return weatherApiClient.getAlerts(region);
    }

    public record WeatherInfo(
        String city,
        double temperature,
        String condition,
        int humidity
    ) {}

    public record WeatherAlert(
        String type,
        String severity,
        String description
    ) {}
}

// 2. 工具注册配置
@Configuration
public class ToolConfiguration {

    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(weatherService)
            .build();
    }

    // 自定义工具回调
    @Bean
    public ToolCallback customToolCallback() {
        return ToolCallback.builder()
            .name("database_query")
            .description("执行数据库查询")
            .inputTypeSchema(DatabaseQuery.class)
            .function(this::executeQuery)
            .build();
    }

    private String executeQuery(DatabaseQuery query) {
        // 执行数据库查询逻辑
        return jdbcTemplate.queryForObject(query.sql(), String.class, query.params());
    }

    public record DatabaseQuery(String sql, Object[] params) {}
}

// 3. 使用工具调用
@Service
public class WeatherChatService {
    private final ChatClient chatClient;

    public WeatherChatService(ChatClient.Builder builder,
                             List<ToolCallback> toolCallbacks) {
        this.chatClient = builder
            .defaultToolCallbacks(toolCallbacks)
            .build();
    }

    public String getWeatherAdvice(String userQuery) {
        return chatClient.prompt()
            .system("""
                你是一个天气助手。当用户询问天气时，使用getWeather工具获取实时天气信息。
                如果有恶劣天气，使用getWeatherAlerts工具获取预警信息。
                """)
            .user(userQuery)
            .call()
            .content();
    }
}
```

**工具调用流程：**

1. **解析阶段**：AI模型识别需要调用的工具
2. **参数提取**：从模型输出中提取工具参数
3. **方法调用**：Spring AI调用对应的Java方法
4. **结果返回**：将方法结果返回给AI模型
5. **响应生成**：AI模型基于工具结果生成最终响应

### 2.5 Advisors API的设计模式和扩展机制

Advisors API采用拦截器模式，允许开发者在AI调用的各个阶段插入自定义逻辑。

**核心接口设计：**

```java
// 同步调用拦截器
public interface CallAdvisor extends Advisor {
    ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain);
}

// 流式调用拦截器
public interface StreamAdvisor extends Advisor {
    Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain);
}

// 基础接口
public interface Advisor {
    String getName();
    int getOrder(); // 执行顺序
}
```

**自定义Advisor实现：**

```java
// 1. 日志记录Advisor
@Component
public class LoggingAdvisor implements CallAdvisor, StreamAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvisor.class);

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // 最高优先级
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("AI请求开始: {}", request.getPrompt().getInstructions());

        long startTime = System.currentTimeMillis();
        ChatClientResponse response = chain.nextCall(request);
        long duration = System.currentTimeMillis() - startTime;

        logger.info("AI请求完成，耗时: {}ms", duration);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        logger.info("AI流式请求开始: {}", request.getPrompt().getInstructions());

        return chain.nextStream(request)
            .doOnComplete(() -> logger.info("AI流式请求完成"));
    }
}

// 2. 安全检查Advisor
@Component
public class SecurityAdvisor implements CallAdvisor {
    private final List<String> sensitivePatterns = List.of(
        "\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b", // 信用卡号
        "\\b\\d{3}-\\d{2}-\\d{4}\\b", // SSN
        "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b" // 邮箱
    );

    @Override
    public String getName() {
        return "SecurityAdvisor";
    }

    @Override
    public int getOrder() {
        return 100; // 较高优先级
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // 检查请求中的敏感信息
        String userMessage = extractUserMessage(request);
        if (containsSensitiveInfo(userMessage)) {
            throw new SecurityException("请求包含敏感信息");
        }

        ChatClientResponse response = chain.nextCall(request);

        // 检查响应中的敏感信息
        String responseContent = response.getResult().getOutput().getContent();
        if (containsSensitiveInfo(responseContent)) {
            // 脱敏处理
            String maskedContent = maskSensitiveInfo(responseContent);
            return createMaskedResponse(response, maskedContent);
        }

        return response;
    }

    private boolean containsSensitiveInfo(String text) {
        return sensitivePatterns.stream()
            .anyMatch(pattern -> text.matches(".*" + pattern + ".*"));
    }

    private String maskSensitiveInfo(String text) {
        String masked = text;
        for (String pattern : sensitivePatterns) {
            masked = masked.replaceAll(pattern, "***");
        }
        return masked;
    }
}

// 3. 性能监控Advisor
@Component
public class MetricsAdvisor implements CallAdvisor {
    private final MeterRegistry meterRegistry;
    private final Timer.Sample sample;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            ChatClientResponse response = chain.nextCall(request);

            // 记录成功指标
            meterRegistry.counter("ai.requests.success").increment();
            sample.stop(Timer.builder("ai.request.duration")
                .tag("status", "success")
                .register(meterRegistry));

            return response;
        } catch (Exception e) {
            // 记录失败指标
            meterRegistry.counter("ai.requests.error")
                .tag("error", e.getClass().getSimpleName())
                .increment();
            sample.stop(Timer.builder("ai.request.duration")
                .tag("status", "error")
                .register(meterRegistry));
            throw e;
        }
    }
}
```

**Advisor链执行顺序：**

```java
@Configuration
public class AdvisorConfiguration {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultAdvisors(
                new SecurityAdvisor(),      // Order: 100
                new LoggingAdvisor(),       // Order: HIGHEST_PRECEDENCE
                new MetricsAdvisor(),       // Order: 200
                new RAGAdvisor()            // Order: 300
            )
            .build();
    }
}
```

## 3. 开发环境搭建与配置

### 3.1 项目依赖配置（Maven/Gradle）

**Maven配置：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>spring-ai-demo</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring AI BOM -->
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Spring AI Core -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-core</artifactId>
        </dependency>

        <!-- AI Model Providers (Spring AI 1.0) -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-openai</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-anthropic</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-ollama</artifactId>
        </dependency>

        <!-- Vector Stores -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-vector-store-redis</artifactId>
        </dependency>

        <!-- PostgreSQL Vector Store (pgvector) -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
        </dependency>

        <!-- RAG Advisors -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-advisors-vector-store</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-elasticsearch-store-spring-boot-starter</artifactId>
        </dependency>

        <!-- Document Readers -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-pdf-document-reader</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-tika-document-reader</artifactId>
        </dependency>

        <!-- Observability -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Gradle配置：**

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.5'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '1.0.0'
sourceCompatibility = '17'

repositories {
    mavenCentral()
    maven { url 'https://repo.spring.io/milestone' }
}

ext {
    set('springAiVersion', '1.0.0')
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'

    // Database
    runtimeOnly 'org.postgresql:postgresql'

    // Spring AI (1.0)
    implementation 'org.springframework.ai:spring-ai-core'
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
    implementation 'org.springframework.ai:spring-ai-starter-model-anthropic'
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-pgvector'
    implementation 'org.springframework.ai:spring-ai-pdf-document-reader'

    // Observability
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.ai:spring-ai-bom:${springAiVersion}"
    }
}
```

### 3.2 PostgreSQL 17 + pgvector 配置

**Docker 启动 PostgreSQL 17 with pgvector：**

```bash
# 使用官方 pgvector 镜像（基于 PostgreSQL 17）
docker run -d \
  --name postgres-vector \
  -p 5432:5432 \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=postgres \
  pgvector/pgvector:pg17

# 连接到数据库并创建扩展
docker exec -it postgres-vector psql -U postgres -c "CREATE EXTENSION IF NOT EXISTS vector;"
docker exec -it postgres-vector psql -U postgres -c "CREATE EXTENSION IF NOT EXISTS hstore;"
docker exec -it postgres-vector psql -U postgres -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"
```

**统一配置基础（application.yml）：**

```yaml
# 通用配置（所有环境共享）
spring:
  application:
    name: spring-ai-enterprise-kb

  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/postgres}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
        show_sql: false

  ai:
    openai:
      api-key: ${OPENAI_API_KEY:demo}
      chat:
        options:
          model: ${OPENAI_CHAT_MODEL:gpt-4o}
          temperature: ${OPENAI_TEMPERATURE:0.7}
      embedding:
        options:
          model: ${OPENAI_EMBEDDING_MODEL:text-embedding-3-large}

    vectorstore:
      pgvector:
        index-type: ${PGVECTOR_INDEX_TYPE:HNSW}
        distance-type: ${PGVECTOR_DISTANCE_TYPE:COSINE_DISTANCE}
        dimensions: ${PGVECTOR_DIMENSIONS:1536}
        initialize-schema: ${PGVECTOR_INIT_SCHEMA:true}
        schema-name: ${PGVECTOR_SCHEMA:public}
        table-name: ${PGVECTOR_TABLE:vector_store}
        max-document-batch-size: ${PGVECTOR_BATCH_SIZE:10000}
        schema-validation: true

# 应用配置
app:
  knowledge-base:
    document:
      storage-path: ${DOCUMENT_STORAGE_PATH:./uploads}
      max-size: ${DOCUMENT_MAX_SIZE:52428800}  # 50MB
      allowed-types: ${DOCUMENT_ALLOWED_TYPES:pdf,txt,docx,md}
    vectorization:
      chunk-size: ${CHUNK_SIZE:1000}
      chunk-overlap: ${CHUNK_OVERLAP:200}
      batch-size: ${VECTORIZATION_BATCH_SIZE:10}
```

**手动配置 PgVectorStore Bean：**

```java
@Configuration
public class VectorStoreConfiguration {

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            .dimensions(1536)                    // OpenAI embedding dimensions
            .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
            .indexType(PgVectorStore.PgIndexType.HNSW)
            .initializeSchema(true)              // 自动创建表和索引
            .schemaName("public")
            .vectorTableName("vector_store")
            .maxDocumentBatchSize(10000)
            .build();
    }
}
```

**环境特定配置：**

**开发环境（application-dev.yml）：**
```yaml
# 开发环境覆盖配置
spring:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        show_sql: true
        format_sql: true

logging:
  level:
    org.springframework.ai: DEBUG
    com.example.kb: DEBUG
    org.springframework.jdbc: DEBUG

# 环境变量示例
# DATABASE_URL=jdbc:postgresql://localhost:5432/postgres_dev
# PGVECTOR_TABLE=vector_store_dev
```

**测试环境（application-test.yml）：**
```yaml
# 测试环境覆盖配置
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # 每次测试重建表结构

logging:
  level:
    org.springframework.ai: WARN
    com.example.kb: DEBUG

# 环境变量示例
# DATABASE_URL=jdbc:postgresql://localhost:5432/postgres_test
# PGVECTOR_TABLE=vector_store_test
# PGVECTOR_BATCH_SIZE=100
```

**生产环境（application-prod.yml）：**
```yaml
# 生产环境覆盖配置
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 生产环境不自动修改表结构

logging:
  level:
    root: WARN
    com.example.kb: INFO

# 环境变量示例
# DATABASE_URL=jdbc:postgresql://prod-host:5432/postgres_prod
# PGVECTOR_TABLE=vector_store_prod
# OPENAI_TEMPERATURE=0.3
```

**统一 Docker Compose 配置（docker-compose.yml）：**

```yaml
version: '3.8'

services:
  # PostgreSQL 17 + pgvector（支持多环境）
  postgres:
    image: pgvector/pgvector:pg17
    container_name: postgres-vector
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
      - POSTGRES_DB=postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis（可选，用于缓存）
  redis:
    image: redis:7-alpine
    container_name: redis-cache
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:

# 使用方式：
# 开发环境: docker-compose up -d
# 测试环境: POSTGRES_DB=postgres_test docker-compose up -d
# 生产环境: 使用外部数据库或 K8s 部署
```

**环境一致性最佳实践：**

为确保开发、测试和生产环境的一致性，建议：

1. **统一数据库**：所有环境都使用 PostgreSQL 17 + pgvector
2. **版本锁定**：使用相同的 PostgreSQL 和 pgvector 版本
3. **配置标准化**：使用相同的向量存储配置参数
4. **数据隔离**：不同环境使用不同的数据库名称

```bash
# 环境数据库命名规范
开发环境: postgres_dev
测试环境: postgres_test
生产环境: postgres_prod
```

### 3.3 环境变量配置

**统一环境变量配置（.env 文件）：**

```bash
# === 数据库配置 ===
DATABASE_URL=jdbc:postgresql://localhost:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JPA_DDL_AUTO=update

# === AI 模型配置 ===
OPENAI_API_KEY=your-openai-api-key
OPENAI_CHAT_MODEL=gpt-4o
OPENAI_EMBEDDING_MODEL=text-embedding-3-large
OPENAI_TEMPERATURE=0.7

# === 向量存储配置 ===
PGVECTOR_INDEX_TYPE=HNSW
PGVECTOR_DISTANCE_TYPE=COSINE_DISTANCE
PGVECTOR_DIMENSIONS=1536
PGVECTOR_INIT_SCHEMA=true
PGVECTOR_SCHEMA=public
PGVECTOR_TABLE=vector_store
PGVECTOR_BATCH_SIZE=10000

# === 应用配置 ===
DOCUMENT_STORAGE_PATH=./uploads
DOCUMENT_MAX_SIZE=52428800
DOCUMENT_ALLOWED_TYPES=pdf,txt,docx,md
CHUNK_SIZE=1000
CHUNK_OVERLAP=200
VECTORIZATION_BATCH_SIZE=10
```

**环境特定变量覆盖：**

```bash
# 开发环境 (.env.dev)
DATABASE_URL=jdbc:postgresql://localhost:5432/postgres_dev
PGVECTOR_TABLE=vector_store_dev
JPA_DDL_AUTO=update

# 测试环境 (.env.test)
DATABASE_URL=jdbc:postgresql://localhost:5432/postgres_test
PGVECTOR_TABLE=vector_store_test
PGVECTOR_BATCH_SIZE=100
JPA_DDL_AUTO=create-drop

# 生产环境 (.env.prod)
DATABASE_URL=jdbc:postgresql://prod-host:5432/postgres_prod
PGVECTOR_TABLE=vector_store_prod
OPENAI_TEMPERATURE=0.3
JPA_DDL_AUTO=validate
```

**启动命令示例：**

```bash
# 开发环境
export $(cat .env.dev | xargs) && mvn spring-boot:run

# 测试环境
export $(cat .env.test | xargs) && mvn test

# 生产环境
export $(cat .env.prod | xargs) && java -jar target/app.jar
```

**多模型配置示例（可选）：**

```yaml
# 如需支持多个 AI 模型提供商，可添加以下配置
spring:
  ai:
    # Anthropic配置
    anthropic:
      api-key: ${ANTHROPIC_API_KEY:}
      chat:
        enabled: ${ANTHROPIC_ENABLED:false}
        options:
          model: claude-3-5-sonnet-20241022
          temperature: 0.7

    # Azure OpenAI配置
    azure:
      openai:
        api-key: ${AZURE_OPENAI_API_KEY:}
        endpoint: ${AZURE_OPENAI_ENDPOINT:}
        chat:
          enabled: ${AZURE_OPENAI_ENABLED:false}
          options:
            deployment-name: gpt-4o

    # Ollama配置（本地部署）
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      chat:
        enabled: ${OLLAMA_ENABLED:false}
        options:
          model: llama3.1
```

### 3.4 自动配置类的工作原理

Spring AI通过自动配置类简化了AI组件的配置和初始化过程。

**核心自动配置类：**

```java
// ChatClient自动配置
@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
@EnableConfigurationProperties(ChatClientProperties.class)
public class ChatClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ChatClient.Builder chatClientBuilder(
            ObjectProvider<ChatModel> chatModels,
            ObjectProvider<List<Advisor>> advisors) {

        ChatClient.Builder builder = ChatClient.builder();

        // 自动注入ChatModel
        chatModels.ifAvailable(builder::chatModel);

        // 自动注入Advisors
        advisors.ifAvailable(advisorList ->
            builder.defaultAdvisors(advisorList.toArray(new Advisor[0])));

        return builder;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ChatClient.Builder.class)
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}

// OpenAI自动配置
@AutoConfiguration
@ConditionalOnClass(OpenAiChatModel.class)
@ConditionalOnProperty(prefix = "spring.ai.openai", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAiApi openAiApi(OpenAiProperties properties) {
        return OpenAiApi.builder()
            .apiKey(properties.getApiKey())
            .baseUrl(properties.getBaseUrl())
            .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi, OpenAiProperties properties) {
        return new OpenAiChatModel(openAiApi, properties.getChat().getOptions());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.ai.openai.embedding", name = "enabled", havingValue = "true")
    public OpenAiEmbeddingModel openAiEmbeddingModel(OpenAiApi openAiApi, OpenAiProperties properties) {
        return new OpenAiEmbeddingModel(openAiApi, properties.getEmbedding().getOptions());
    }
}

// PgVectorStore自动配置（Spring AI 1.0）
@AutoConfiguration
@ConditionalOnClass(PgVectorStore.class)
@ConditionalOnProperty(prefix = "spring.ai.vectorstore.pgvector", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PgVectorStoreProperties.class)
public class PgVectorStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PgVectorStore pgVectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            PgVectorStoreProperties properties) {

        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            .dimensions(properties.getDimensions())
            .distanceType(properties.getDistanceType())
            .indexType(properties.getIndexType())
            .initializeSchema(properties.isInitializeSchema())
            .schemaName(properties.getSchemaName())
            .vectorTableName(properties.getTableName())
            .maxDocumentBatchSize(properties.getMaxDocumentBatchSize())
            .build();
    }
}
```

### 3.4 常用配置参数的详细说明

**ChatModel配置参数：**

| 参数 | 说明 | 默认值 | 示例 |
|------|------|--------|------|
| model | 模型名称 | gpt-4o | gpt-4o, claude-3-5-sonnet |
| temperature | 随机性控制 | 0.7 | 0.0-2.0 |
| max-tokens | 最大输出长度 | 2000 | 100-4000 |
| top-p | 核采样参数 | 1.0 | 0.1-1.0 |
| frequency-penalty | 频率惩罚 | 0.0 | -2.0-2.0 |
| presence-penalty | 存在惩罚 | 0.0 | -2.0-2.0 |

**VectorStore配置参数：**

| 参数 | 说明 | 默认值 | 示例 |
|------|------|--------|------|
| index | 索引名称 | spring-ai | documents, knowledge-base |
| prefix | 键前缀 | doc: | app:, kb: |
| similarity-threshold | 相似度阈值 | 0.7 | 0.5-0.9 |
| top-k | 返回结果数量 | 5 | 1-20 |

**完整配置示例：**

```yaml
spring:
  ai:
    # 全局配置
    retry:
      max-attempts: 3
      backoff:
        initial-interval: 1s
        max-interval: 10s
        multiplier: 2.0

    # 模型特定配置
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        enabled: true
        options:
          model: gpt-4o
          temperature: 0.7
          max-tokens: 2000
          top-p: 0.9
          frequency-penalty: 0.0
          presence-penalty: 0.0
          response-format:
            type: json_object  # 结构化输出
      embedding:
        enabled: true
        options:
          model: text-embedding-3-large
          dimensions: 1536

    # 向量存储配置
    vectorstore:
      redis:
        enabled: true
        uri: redis://localhost:6379
        index: spring-ai-docs
        prefix: doc:
        similarity-threshold: 0.75
        top-k: 10

    # 文档处理配置
    document:
      reader:
        pdf:
          enabled: true
          ocr-enabled: false
        tika:
          enabled: true
      transformer:
        splitter:
          text:
            chunk-size: 1000
            chunk-overlap: 200
```

## 4. 实际开发指南

### 4.1 从零开始创建第一个Spring AI应用

**步骤1：创建Spring Boot项目**

```bash
# 使用Spring Initializr创建项目
curl https://start.spring.io/starter.zip \
  -d dependencies=web,actuator \
  -d groupId=com.example \
  -d artifactId=spring-ai-demo \
  -d name=spring-ai-demo \
  -d description="Spring AI Demo Application" \
  -d packageName=com.example.springai \
  -d javaVersion=17 \
  -o spring-ai-demo.zip

unzip spring-ai-demo.zip
cd spring-ai-demo
```

**步骤2：添加Spring AI依赖**

在`pom.xml`中添加：

```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

**步骤3：配置API密钥**

在`application.yml`中添加：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
          temperature: 0.7
```

**步骤4：创建第一个AI服务**

```java
package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String chat(String message) {
        return chatClient.prompt(message)
            .call()
            .content();
    }
}
```

**步骤5：创建REST控制器**

```java
package com.example.springai.controller;

import com.example.springai.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public String chat(@RequestBody String message) {
        return chatService.chat(message);
    }
}
```

**步骤6：启动应用**

```java
package com.example.springai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringAiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiDemoApplication.class, args);
    }
}
```

**步骤7：测试应用**

```bash
# 设置环境变量
export OPENAI_API_KEY=your-api-key

# 启动应用
./mvnw spring-boot:run

# 测试API
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "你好，请介绍一下Spring AI框架"
```

### 4.2 核心API的使用方法和最佳实践

**ChatClient API最佳实践：**

```java
@Service
public class AdvancedChatService {

    private final ChatClient chatClient;

    public AdvancedChatService(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultAdvisors(
                new LoggingAdvisor(),
                new SecurityAdvisor()
            )
            .build();
    }

    // 1. 基础对话
    public String simpleChat(String message) {
        return chatClient.prompt(message)
            .call()
            .content();
    }

    // 2. 带系统提示的对话
    public String chatWithSystem(String userMessage, String systemPrompt) {
        return chatClient.prompt()
            .system(systemPrompt)
            .user(userMessage)
            .call()
            .content();
    }

    // 3. 参数化提示
    public String chatWithParams(String template, Map<String, Object> params) {
        return chatClient.prompt()
            .user(u -> u.text(template).params(params))
            .call()
            .content();
    }

    // 4. 结构化输出
    public WeatherReport getWeatherReport(String city) {
        return chatClient.prompt()
            .system("你是一个天气分析专家，返回JSON格式的天气报告")
            .user("分析{city}的天气情况", Map.of("city", city))
            .call()
            .entity(WeatherReport.class);
    }

    // 5. 流式响应
    public Flux<String> streamChat(String message) {
        return chatClient.prompt(message)
            .stream()
            .content();
    }

    // 6. 带工具调用的对话
    public String chatWithTools(String message, List<ToolCallback> tools) {
        return chatClient.prompt(message)
            .toolCallbacks(tools)
            .call()
            .content();
    }

    public record WeatherReport(
        String city,
        String temperature,
        String condition,
        List<String> recommendations
    ) {}
}
```

### 4.3 常见开发模式的具体实现

#### 4.3.1 RAG（检索增强生成）应用

**完整RAG实现：**

```java
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RAGService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final DocumentReader documentReader;
    private final TextSplitter textSplitter;

    public RAGService(VectorStore vectorStore,
                     ChatClient.Builder builder,
                     DocumentReader documentReader) {
        this.vectorStore = vectorStore;
        this.documentReader = documentReader;
        this.textSplitter = new TokenTextSplitter(1000, 200);

        // 配置RAG Advisor (Spring AI 1.0)
        this.chatClient = builder
            .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
            .build();
    }

    // 添加文档到知识库
    public void addDocuments(List<Resource> resources) {
        List<Document> documents = new ArrayList<>();

        for (Resource resource : resources) {
            // 读取文档
            List<Document> docs = documentReader.get();

            // 分割文档
            List<Document> splitDocs = textSplitter.apply(docs);

            // 添加元数据
            splitDocs.forEach(doc -> {
                doc.getMetadata().put("source", resource.getFilename());
                doc.getMetadata().put("timestamp", Instant.now().toString());
                doc.getMetadata().put("type", getDocumentType(resource));
            });

            documents.addAll(splitDocs);
        }

        // 存储到向量数据库
        vectorStore.add(documents);
    }

    // RAG查询
    public String queryKnowledgeBase(String question) {
        return chatClient.prompt()
            .system("""
                你是一个知识库助手。请基于提供的上下文信息回答用户问题。
                如果上下文中没有相关信息，请明确说明。
                """)
            .user(question)
            .call()
            .content();
    }

    // 带过滤的RAG查询
    public String queryWithFilter(String question, Map<String, Object> filters) {
        // 构建过滤表达式
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Expression filterExpression = builder.and(
            filters.entrySet().stream()
                .map(entry -> builder.eq(entry.getKey(), entry.getValue()))
                .toArray(Expression[]::new)
        ).build();

        // 检索相关文档
        List<Document> relevantDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(5)
                .filterExpression(filterExpression)
                .build()
        );

        // 构建上下文
        String context = relevantDocs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n"));

        return chatClient.prompt()
            .system("基于以下上下文回答问题：\n" + context)
            .user(question)
            .call()
            .content();
    }

    private String getDocumentType(Resource resource) {
        String filename = resource.getFilename();
        if (filename != null) {
            if (filename.endsWith(".pdf")) return "PDF";
            if (filename.endsWith(".docx")) return "Word";
            if (filename.endsWith(".txt")) return "Text";
        }
        return "Unknown";
    }
}
```

#### 4.3.2 聊天机器人应用

**智能聊天机器人实现：**

```java
@Service
public class ChatBotService {

    private final ChatClient chatClient;
    private final ConversationMemory conversationMemory;
    private final List<ToolCallback> toolCallbacks;

    public ChatBotService(ChatClient.Builder builder,
                         ConversationMemory conversationMemory,
                         List<ToolCallback> toolCallbacks) {
        this.conversationMemory = conversationMemory;
        this.toolCallbacks = toolCallbacks;

        this.chatClient = builder
            .defaultAdvisors(
                new ConversationMemoryAdvisor(conversationMemory),
                new LoggingAdvisor()
            )
            .defaultToolCallbacks(toolCallbacks)
            .build();
    }

    // 处理用户消息
    public ChatResponse chat(String sessionId, String userMessage) {
        return chatClient.prompt()
            .system("""
                你是一个友好的AI助手。你可以：
                1. 回答各种问题
                2. 查询天气信息
                3. 搜索相关资料
                4. 执行简单的计算

                请保持对话的连贯性，记住之前的对话内容。
                """)
            .user(userMessage)
            .advisors(a -> a.param("sessionId", sessionId))
            .call()
            .chatResponse();
    }

    // 流式聊天
    public Flux<String> streamChat(String sessionId, String userMessage) {
        return chatClient.prompt()
            .system("你是一个AI助手，请提供有帮助的回答。")
            .user(userMessage)
            .advisors(a -> a.param("sessionId", sessionId))
            .stream()
            .content();
    }

    // 获取对话历史
    public List<Message> getConversationHistory(String sessionId) {
        return conversationMemory.get(sessionId, 10);
    }

    // 清除对话历史
    public void clearConversation(String sessionId) {
        conversationMemory.clear(sessionId);
    }
}

// 对话记忆实现
@Component
public class ConversationMemory {
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();

    public void add(String sessionId, Message message) {
        conversations.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
    }

    public List<Message> get(String sessionId, int limit) {
        List<Message> messages = conversations.getOrDefault(sessionId, new ArrayList<>());
        return messages.stream()
            .skip(Math.max(0, messages.size() - limit))
            .collect(Collectors.toList());
    }

    public void clear(String sessionId) {
        conversations.remove(sessionId);
    }
}
```

#### 4.3.3 文档处理应用

**智能文档分析系统：**

```java
@Service
public class DocumentAnalysisService {

    private final ChatClient chatClient;
    private final DocumentReader pdfReader;
    private final DocumentReader tikaReader;
    private final VectorStore vectorStore;

    public DocumentAnalysisService(ChatClient.Builder builder,
                                  VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.pdfReader = new PagePdfDocumentReader();
        this.tikaReader = new TikaDocumentReader();
    }

    // 文档摘要生成
    public DocumentSummary generateSummary(Resource document) {
        List<Document> docs = readDocument(document);
        String content = docs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n"));

        return chatClient.prompt()
            .system("""
                请为以下文档生成结构化摘要，包括：
                1. 主要内容概述
                2. 关键要点列表
                3. 重要结论
                4. 建议的后续行动
                """)
            .user("文档内容：\n" + content)
            .call()
            .entity(DocumentSummary.class);
    }

    // 文档问答
    public String answerQuestion(Resource document, String question) {
        List<Document> docs = readDocument(document);
        String content = docs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n"));

        return chatClient.prompt()
            .system("基于提供的文档内容回答用户问题。如果文档中没有相关信息，请明确说明。")
            .user("文档内容：\n" + content + "\n\n问题：" + question)
            .call()
            .content();
    }

    // 批量文档处理
    public List<DocumentAnalysisResult> batchAnalyze(List<Resource> documents) {
        return documents.parallelStream()
            .map(this::analyzeDocument)
            .collect(Collectors.toList());
    }

    // 文档分类
    public DocumentClassification classifyDocument(Resource document) {
        List<Document> docs = readDocument(document);
        String content = docs.stream()
            .limit(3) // 只使用前3页进行分类
            .map(Document::getContent)
            .collect(Collectors.joining("\n"));

        return chatClient.prompt()
            .system("""
                请分析文档类型和主题，返回分类结果：
                - 文档类型：合同、报告、手册、政策等
                - 主题领域：技术、财务、法律、人力资源等
                - 重要程度：高、中、低
                - 处理优先级：紧急、正常、低优先级
                """)
            .user("文档内容：\n" + content)
            .call()
            .entity(DocumentClassification.class);
    }

    private List<Document> readDocument(Resource resource) {
        String filename = resource.getFilename();
        if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
            return pdfReader.get();
        } else {
            return tikaReader.get();
        }
    }

    private DocumentAnalysisResult analyzeDocument(Resource document) {
        try {
            DocumentSummary summary = generateSummary(document);
            DocumentClassification classification = classifyDocument(document);

            return new DocumentAnalysisResult(
                document.getFilename(),
                summary,
                classification,
                "SUCCESS",
                null
            );
        } catch (Exception e) {
            return new DocumentAnalysisResult(
                document.getFilename(),
                null,
                null,
                "ERROR",
                e.getMessage()
            );
        }
    }

    public record DocumentSummary(
        String overview,
        List<String> keyPoints,
        String conclusion,
        List<String> recommendations
    ) {}

    public record DocumentClassification(
        String documentType,
        String subject,
        String importance,
        String priority
    ) {}

    public record DocumentAnalysisResult(
        String filename,
        DocumentSummary summary,
        DocumentClassification classification,
        String status,
        String error
    ) {}
}
```

### 4.4 错误处理和调试技巧

**统一错误处理：**

```java
@ControllerAdvice
public class AIExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AIExceptionHandler.class);

    @ExceptionHandler(OpenAiApiException.class)
    public ResponseEntity<ErrorResponse> handleOpenAiException(OpenAiApiException e) {
        logger.error("OpenAI API错误: {}", e.getMessage(), e);

        ErrorResponse error = new ErrorResponse(
            "AI_API_ERROR",
            "AI服务暂时不可用，请稍后重试",
            e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(RateLimitException e) {
        logger.warn("API调用频率限制: {}", e.getMessage());

        ErrorResponse error = new ErrorResponse(
            "RATE_LIMIT_EXCEEDED",
            "请求过于频繁，请稍后重试",
            null
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(VectorStoreException.class)
    public ResponseEntity<ErrorResponse> handleVectorStoreException(VectorStoreException e) {
        logger.error("向量存储错误: {}", e.getMessage(), e);

        ErrorResponse error = new ErrorResponse(
            "VECTOR_STORE_ERROR",
            "知识库服务异常",
            e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    public record ErrorResponse(
        String code,
        String message,
        String details
    ) {}
}

// 重试配置
@Configuration
@EnableRetry
public class RetryConfiguration {

    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(1000, 2, 10000)
            .retryOn(OpenAiApiException.class)
            .build();
    }
}

// 带重试的服务
@Service
public class ResilientChatService {

    private final ChatClient chatClient;
    private final RetryTemplate retryTemplate;

    @Retryable(
        value = {OpenAiApiException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String chatWithRetry(String message) {
        return chatClient.prompt(message)
            .call()
            .content();
    }

    @Recover
    public String recover(OpenAiApiException e, String message) {
        logger.error("AI服务调用失败，使用默认回复: {}", e.getMessage());
        return "抱歉，AI服务暂时不可用，请稍后重试。";
    }
}
```

**调试技巧：**

```java
// 调试Advisor
@Component
public class DebugAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // 打印请求信息
        System.out.println("=== AI请求调试信息 ===");
        System.out.println("提示词: " + request.getPrompt().getInstructions());
        System.out.println("参数: " + request.getPrompt().getOptions());

        long startTime = System.currentTimeMillis();
        ChatClientResponse response = chain.nextCall(request);
        long duration = System.currentTimeMillis() - startTime;

        // 打印响应信息
        System.out.println("响应时间: " + duration + "ms");
        System.out.println("响应内容: " + response.getResult().getOutput().getContent());
        System.out.println("Token使用: " + response.getResult().getMetadata());
        System.out.println("========================");

        return response;
    }
}
```

## 5. 进阶开发技能

### 5.1 自定义Advisor的开发方法

**高级Advisor开发：**

```java
// 1. 缓存Advisor - 缓存AI响应
@Component
public class CacheAdvisor implements CallAdvisor {

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "CacheAdvisor";
    }

    @Override
    public int getOrder() {
        return 50; // 较高优先级
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String cacheKey = generateCacheKey(request);
        Cache cache = cacheManager.getCache("ai-responses");

        // 尝试从缓存获取
        Cache.ValueWrapper cached = cache.get(cacheKey);
        if (cached != null) {
            return (ChatClientResponse) cached.get();
        }

        // 调用AI服务
        ChatClientResponse response = chain.nextCall(request);

        // 缓存响应（只缓存成功的响应）
        if (response.getResult() != null) {
            cache.put(cacheKey, response);
        }

        return response;
    }

    private String generateCacheKey(ChatClientRequest request) {
        try {
            String promptText = request.getPrompt().getInstructions();
            return DigestUtils.md5DigestAsHex(promptText.getBytes());
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}

// 2. 内容过滤Advisor - 过滤不当内容
@Component
public class ContentFilterAdvisor implements CallAdvisor {

    private final List<String> bannedWords = List.of(
        "暴力", "仇恨", "歧视", "违法"
    );

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // 检查输入内容
        String userInput = extractUserMessage(request);
        if (containsBannedContent(userInput)) {
            throw new ContentViolationException("输入内容包含不当信息");
        }

        ChatClientResponse response = chain.nextCall(request);

        // 检查输出内容
        String aiResponse = response.getResult().getOutput().getContent();
        if (containsBannedContent(aiResponse)) {
            // 替换为安全回复
            return createSafeResponse(response);
        }

        return response;
    }

    private boolean containsBannedContent(String text) {
        return bannedWords.stream()
            .anyMatch(word -> text.toLowerCase().contains(word.toLowerCase()));
    }

    private ChatClientResponse createSafeResponse(ChatClientResponse original) {
        // 创建安全的替代响应
        return ChatClientResponse.builder()
            .from(original)
            .withContent("抱歉，我无法提供相关信息。请换个话题。")
            .build();
    }
}

// 3. 多语言Advisor - 自动翻译
@Component
public class MultiLanguageAdvisor implements CallAdvisor {

    private final ChatClient translationClient;
    private final LanguageDetector languageDetector;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String userMessage = extractUserMessage(request);
        String detectedLanguage = languageDetector.detect(userMessage);

        // 如果不是英文，先翻译为英文
        if (!"en".equals(detectedLanguage)) {
            String translatedMessage = translateToEnglish(userMessage);
            ChatClientRequest translatedRequest = modifyRequest(request, translatedMessage);

            ChatClientResponse response = chain.nextCall(translatedRequest);

            // 将响应翻译回原语言
            String translatedResponse = translateFromEnglish(
                response.getResult().getOutput().getContent(),
                detectedLanguage
            );

            return modifyResponse(response, translatedResponse);
        }

        return chain.nextCall(request);
    }

    private String translateToEnglish(String text) {
        return translationClient.prompt()
            .system("将以下文本翻译为英文")
            .user(text)
            .call()
            .content();
    }

    private String translateFromEnglish(String text, String targetLanguage) {
        return translationClient.prompt()
            .system("将以下英文翻译为" + getLanguageName(targetLanguage))
            .user(text)
            .call()
            .content();
    }
}
```

### 5.2 多模型集成和切换策略

**多模型配置：**

```java
@Configuration
public class MultiModelConfiguration {

    // OpenAI ChatClient
    @Bean
    @Qualifier("openai")
    public ChatClient openAiChatClient(@Qualifier("openai") ChatModel openAiModel) {
        return ChatClient.builder()
            .chatModel(openAiModel)
            .defaultAdvisors(new LoggingAdvisor("OpenAI"))
            .build();
    }

    // Anthropic ChatClient
    @Bean
    @Qualifier("anthropic")
    public ChatClient anthropicChatClient(@Qualifier("anthropic") ChatModel anthropicModel) {
        return ChatClient.builder()
            .chatModel(anthropicModel)
            .defaultAdvisors(new LoggingAdvisor("Anthropic"))
            .build();
    }

    // Ollama ChatClient
    @Bean
    @Qualifier("ollama")
    public ChatClient ollamaChatClient(@Qualifier("ollama") ChatModel ollamaModel) {
        return ChatClient.builder()
            .chatModel(ollamaModel)
            .defaultAdvisors(new LoggingAdvisor("Ollama"))
            .build();
    }
}

// 智能模型路由服务
@Service
public class ModelRoutingService {

    private final Map<String, ChatClient> chatClients;
    private final ModelSelectionStrategy selectionStrategy;

    public ModelRoutingService(@Qualifier("openai") ChatClient openAiClient,
                              @Qualifier("anthropic") ChatClient anthropicClient,
                              @Qualifier("ollama") ChatClient ollamaClient,
                              ModelSelectionStrategy selectionStrategy) {
        this.chatClients = Map.of(
            "openai", openAiClient,
            "anthropic", anthropicClient,
            "ollama", ollamaClient
        );
        this.selectionStrategy = selectionStrategy;
    }

    public String chat(String message, ChatContext context) {
        String selectedModel = selectionStrategy.selectModel(message, context);
        ChatClient client = chatClients.get(selectedModel);

        try {
            return client.prompt(message).call().content();
        } catch (Exception e) {
            // 故障转移到备用模型
            return fallbackChat(message, selectedModel);
        }
    }

    private String fallbackChat(String message, String failedModel) {
        List<String> availableModels = chatClients.keySet().stream()
            .filter(model -> !model.equals(failedModel))
            .collect(Collectors.toList());

        for (String model : availableModels) {
            try {
                return chatClients.get(model).prompt(message).call().content();
            } catch (Exception e) {
                // 继续尝试下一个模型
            }
        }

        throw new RuntimeException("所有AI模型都不可用");
    }
}

// 模型选择策略
@Component
public class ModelSelectionStrategy {

    public String selectModel(String message, ChatContext context) {
        // 基于消息类型选择模型
        if (isCodeRelated(message)) {
            return "anthropic"; // Claude擅长代码
        } else if (isCreativeTask(message)) {
            return "openai"; // GPT擅长创意
        } else if (context.isPrivacySensitive()) {
            return "ollama"; // 本地模型保护隐私
        } else {
            return "openai"; // 默认选择
        }
    }

    private boolean isCodeRelated(String message) {
        return message.toLowerCase().contains("代码") ||
               message.toLowerCase().contains("编程") ||
               message.contains("```");
    }

    private boolean isCreativeTask(String message) {
        return message.toLowerCase().contains("创作") ||
               message.toLowerCase().contains("故事") ||
               message.toLowerCase().contains("诗歌");
    }
}

public class ChatContext {
    private boolean privacySensitive;
    private String userRole;
    private String department;

    // getters and setters
}
```

### 5.3 性能优化和扩展性考虑

**连接池优化：**

```java
@Configuration
public class AIPerformanceConfiguration {

    @Bean
    public RestTemplate aiRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();

        // 连接池配置
        PoolingHttpClientConnectionManager connectionManager =
            new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);

        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
            .build();

        factory.setHttpClient(httpClient);
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);

        return new RestTemplate(factory);
    }

    @Bean
    public TaskExecutor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AI-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

**批处理优化：**

```java
@Service
public class BatchProcessingService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Async("aiTaskExecutor")
    public CompletableFuture<List<String>> batchProcess(List<String> messages) {
        // 分批处理，避免单次请求过大
        List<List<String>> batches = partition(messages, 10);

        List<CompletableFuture<List<String>>> futures = batches.stream()
            .map(this::processBatch)
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    private CompletableFuture<List<String>> processBatch(List<String> batch) {
        return CompletableFuture.supplyAsync(() ->
            batch.stream()
                .map(message -> chatClient.prompt(message).call().content())
                .collect(Collectors.toList())
        );
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        return IntStream.range(0, (list.size() + size - 1) / size)
            .mapToObj(i -> list.subList(i * size, Math.min((i + 1) * size, list.size())))
            .collect(Collectors.toList());
    }
}
```

**缓存策略：**

```java
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());

        return builder.build();
    }

    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}

@Service
public class CachedChatService {

    private final ChatClient chatClient;

    @Cacheable(value = "ai-responses", key = "#message.hashCode()")
    public String chat(String message) {
        return chatClient.prompt(message).call().content();
    }

    @CacheEvict(value = "ai-responses", allEntries = true)
    public void clearCache() {
        // 清除所有缓存
    }
}
```

### 5.4 生产环境部署的注意事项

**Docker部署配置：**

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制应用文件
COPY target/spring-ai-demo.jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

**数据库初始化脚本（init-db.sql）：**

```sql
-- 创建必要的扩展
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 创建向量存储表（可选，Spring AI 会自动创建）
CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1536)
);

-- 创建 HNSW 索引以提高查询性能
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
ON vector_store USING hnsw (embedding vector_cosine_ops);
```

**Docker Compose配置：**

```yaml
# docker-compose.yml
version: '3.8'

services:
  spring-ai-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - REDIS_URL=redis://redis:6379
      - DATABASE_URL=jdbc:postgresql://postgres:5432/springai
    depends_on:
      - redis
      - postgres
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    restart: unless-stopped

  postgres:
    image: pgvector/pgvector:pg17
    environment:
      - POSTGRES_DB=springai
      - POSTGRES_USER=springai
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
    restart: unless-stopped

  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    restart: unless-stopped

volumes:
  redis_data:
  postgres_data:
```

**Kubernetes部署配置：**

```yaml
# k8s-deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-ai-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-ai-app
  template:
    metadata:
      labels:
        app: spring-ai-app
    spec:
      containers:
      - name: spring-ai-app
        image: spring-ai-demo:latest
        ports:
        - containerPort: 8080
        env:
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: ai-secrets
              key: openai-api-key
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5

---
apiVersion: v1
kind: Service
metadata:
  name: spring-ai-service
spec:
  selector:
    app: spring-ai-app
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

## 6. 实战项目示例

### 6.1 企业知识库系统（RAG应用）

这是一个完整的企业知识库系统，支持文档上传、智能问答和知识管理。

**项目结构：**

```
enterprise-knowledge-base/
├── src/main/java/com/example/kb/
│   ├── KnowledgeBaseApplication.java
│   ├── config/
│   │   ├── AIConfiguration.java
│   │   └── SecurityConfiguration.java
│   ├── controller/
│   │   ├── DocumentController.java
│   │   └── QueryController.java
│   ├── service/
│   │   ├── DocumentService.java
│   │   ├── QueryService.java
│   │   └── UserService.java
│   ├── model/
│   │   ├── Document.java
│   │   ├── Query.java
│   │   └── User.java
│   └── repository/
│       ├── DocumentRepository.java
│       └── QueryRepository.java
├── src/main/resources/
│   ├── application.yml
│   └── static/
└── pom.xml
```

**核心实现代码：**

```java
// 1. 主应用类
@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
public class KnowledgeBaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeBaseApplication.class, args);
    }
}

// 2. AI配置
@Configuration
public class AIConfiguration {

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
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate,
                                  EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            .dimensions(1536)
            .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
            .indexType(PgVectorStore.PgIndexType.HNSW)
            .initializeSchema(true)
            .schemaName("public")
            .vectorTableName("enterprise_kb_vectors")
            .maxDocumentBatchSize(10000)
            .build();
    }

    @Bean
    public DocumentReader pdfDocumentReader() {
        return new PagePdfDocumentReader();
    }

    @Bean
    public TextSplitter textSplitter() {
        return new TokenTextSplitter(1000, 200);
    }
}

// 3. 文档服务
@Service
@Transactional
public class DocumentService {

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final DocumentReader documentReader;
    private final TextSplitter textSplitter;

    public DocumentUploadResult uploadDocument(MultipartFile file, String category, String userId) {
        try {
            // 保存文件信息到数据库
            DocumentEntity document = new DocumentEntity();
            document.setFilename(file.getOriginalFilename());
            document.setCategory(category);
            document.setUploadedBy(userId);
            document.setUploadTime(LocalDateTime.now());
            document.setStatus("PROCESSING");

            DocumentEntity savedDoc = documentRepository.save(document);

            // 异步处理文档
            processDocumentAsync(file, savedDoc);

            return new DocumentUploadResult(savedDoc.getId(), "SUCCESS", "文档上传成功，正在处理中");

        } catch (Exception e) {
            return new DocumentUploadResult(null, "ERROR", "文档上传失败：" + e.getMessage());
        }
    }

    @Async
    public void processDocumentAsync(MultipartFile file, DocumentEntity document) {
        try {
            // 读取文档内容
            Resource resource = new InputStreamResource(file.getInputStream());
            List<org.springframework.ai.document.Document> docs = documentReader.get();

            // 分割文档
            List<org.springframework.ai.document.Document> splitDocs = textSplitter.apply(docs);

            // 添加元数据
            splitDocs.forEach(doc -> {
                doc.getMetadata().put("document_id", document.getId().toString());
                doc.getMetadata().put("filename", document.getFilename());
                doc.getMetadata().put("category", document.getCategory());
                doc.getMetadata().put("upload_time", document.getUploadTime().toString());
            });

            // 存储到向量数据库
            vectorStore.add(splitDocs);

            // 更新文档状态
            document.setStatus("COMPLETED");
            document.setProcessedTime(LocalDateTime.now());
            documentRepository.save(document);

        } catch (Exception e) {
            document.setStatus("FAILED");
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
        }
    }

    public List<DocumentEntity> getDocuments(String userId, String category) {
        if (category != null) {
            return documentRepository.findByUploadedByAndCategory(userId, category);
        } else {
            return documentRepository.findByUploadedBy(userId);
        }
    }

    public void deleteDocument(Long documentId, String userId) {
        DocumentEntity document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("文档不存在"));

        if (!document.getUploadedBy().equals(userId)) {
            throw new RuntimeException("无权限删除此文档");
        }

        // 从向量数据库删除
        vectorStore.delete(List.of(documentId.toString()));

        // 从数据库删除
        documentRepository.delete(document);
    }
}

// 4. 查询服务
@Service
public class QueryService {

    private final ChatClient chatClient;
    private final QueryRepository queryRepository;

    public QueryResult query(String question, String userId, String category) {
        try {
            // 记录查询
            QueryEntity query = new QueryEntity();
            query.setQuestion(question);
            query.setUserId(userId);
            query.setCategory(category);
            query.setQueryTime(LocalDateTime.now());

            // 构建查询上下文
            String systemPrompt = buildSystemPrompt(category);

            // 执行查询
            String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .call()
                .content();

            // 保存结果
            query.setAnswer(answer);
            query.setStatus("SUCCESS");
            queryRepository.save(query);

            return new QueryResult(answer, "SUCCESS", null);

        } catch (Exception e) {
            return new QueryResult(null, "ERROR", e.getMessage());
        }
    }

    private String buildSystemPrompt(String category) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个企业知识库助手。请基于提供的文档内容回答用户问题。\n");
        prompt.append("回答要求：\n");
        prompt.append("1. 准确性：确保答案基于文档内容\n");
        prompt.append("2. 完整性：提供全面的信息\n");
        prompt.append("3. 可读性：使用清晰的语言和结构\n");
        prompt.append("4. 引用：在适当时候引用相关文档\n");

        if (category != null) {
            prompt.append("5. 专业性：重点关注").append(category).append("领域的专业知识\n");
        }

        prompt.append("\n如果文档中没有相关信息，请明确说明。");

        return prompt.toString();
    }

    public List<QueryEntity> getQueryHistory(String userId, int limit) {
        return queryRepository.findByUserIdOrderByQueryTimeDesc(userId, PageRequest.of(0, limit));
    }
}

// 5. REST控制器
@RestController
@RequestMapping("/api/documents")
@PreAuthorize("hasRole('USER')")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResult> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            Authentication authentication) {

        String userId = authentication.getName();
        DocumentUploadResult result = documentService.uploadDocument(file, category, userId);

        if ("SUCCESS".equals(result.status())) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentEntity>> getDocuments(
            @RequestParam(required = false) String category,
            Authentication authentication) {

        String userId = authentication.getName();
        List<DocumentEntity> documents = documentService.getDocuments(userId, category);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id,
                                             Authentication authentication) {
        String userId = authentication.getName();
        documentService.deleteDocument(id, userId);
        return ResponseEntity.ok().build();
    }
}

@RestController
@RequestMapping("/api/query")
@PreAuthorize("hasRole('USER')")
public class QueryController {

    private final QueryService queryService;

    @PostMapping
    public ResponseEntity<QueryResult> query(@RequestBody QueryRequest request,
                                           Authentication authentication) {
        String userId = authentication.getName();
        QueryResult result = queryService.query(request.question(), userId, request.category());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<QueryEntity>> getHistory(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {

        String userId = authentication.getName();
        List<QueryEntity> history = queryService.getQueryHistory(userId, limit);
        return ResponseEntity.ok(history);
    }
}

// 6. 数据模型
@Entity
@Table(name = "documents")
public class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String category;
    private String uploadedBy;
    private LocalDateTime uploadTime;
    private LocalDateTime processedTime;
    private String status; // PROCESSING, COMPLETED, FAILED
    private String errorMessage;

    // getters and setters
}

@Entity
@Table(name = "queries")
public class QueryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;
    private String answer;
    private String userId;
    private String category;
    private LocalDateTime queryTime;
    private String status;

    // getters and setters
}

// 7. 记录类型
public record DocumentUploadResult(Long documentId, String status, String message) {}
public record QueryResult(String answer, String status, String error) {}
public record QueryRequest(String question, String category) {}
```

### 6.2 智能客服机器人系统

这是一个完整的智能客服系统，支持多轮对话、工具调用和人工转接。

**核心功能：**
- 智能对话管理
- 订单查询和处理
- 常见问题自动回答
- 情感分析和升级处理
- 人工客服转接

**实现代码：**

```java
// 1. 客服机器人服务
@Service
public class CustomerServiceBot {

    private final ChatClient chatClient;
    private final ConversationMemory conversationMemory;
    private final OrderService orderService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final HumanAgentService humanAgentService;

    public CustomerServiceBot(ChatClient.Builder builder,
                             ConversationMemory conversationMemory,
                             List<ToolCallback> toolCallbacks) {
        this.conversationMemory = conversationMemory;

        this.chatClient = builder
            .defaultAdvisors(
                new ConversationMemoryAdvisor(conversationMemory),
                new SentimentAnalysisAdvisor(),
                new EscalationAdvisor(humanAgentService)
            )
            .defaultToolCallbacks(toolCallbacks)
            .build();
    }

    public ChatResponse handleMessage(String sessionId, String userMessage, String userId) {
        // 检查是否需要人工介入
        if (shouldEscalateToHuman(sessionId, userMessage)) {
            return escalateToHuman(sessionId, userMessage, userId);
        }

        // AI处理
        ChatResponse response = chatClient.prompt()
            .system(buildSystemPrompt())
            .user(userMessage)
            .advisors(a -> a.param("sessionId", sessionId).param("userId", userId))
            .call()
            .chatResponse();

        // 记录对话
        recordConversation(sessionId, userMessage, response.getResult().getOutput().getContent());

        return response;
    }

    private String buildSystemPrompt() {
        return """
            你是一个专业的客服助手，名字叫小智。你的职责是：

            1. 友好、耐心地回答客户问题
            2. 使用可用的工具查询订单、产品信息等
            3. 对于复杂问题，引导客户提供更多信息
            4. 如果无法解决问题，建议转接人工客服

            回答风格：
            - 使用礼貌、专业的语言
            - 保持简洁明了
            - 提供具体的解决方案
            - 适时表达同理心

            可用工具：
            - queryOrder: 查询订单信息
            - searchProducts: 搜索产品信息
            - createTicket: 创建客服工单
            - checkInventory: 检查库存状态
            """;
    }

    private boolean shouldEscalateToHuman(String sessionId, String userMessage) {
        // 检查升级条件
        ConversationContext context = conversationMemory.getContext(sessionId);

        // 1. 用户明确要求人工服务
        if (userMessage.toLowerCase().contains("人工") ||
            userMessage.toLowerCase().contains("转接")) {
            return true;
        }

        // 2. 连续多次无法解决问题
        if (context.getUnresolvedCount() >= 3) {
            return true;
        }

        // 3. 情感分析显示用户非常不满
        if (context.getSentimentScore() < -0.8) {
            return true;
        }

        return false;
    }

    private ChatResponse escalateToHuman(String sessionId, String userMessage, String userId) {
        // 创建人工客服工单
        Ticket ticket = humanAgentService.createTicket(sessionId, userMessage, userId);

        // 返回转接消息
        return ChatResponse.builder()
            .withContent(String.format(
                "我理解您的问题比较复杂，已为您转接人工客服。工单号：%s，预计等待时间：%d分钟。",
                ticket.getId(),
                humanAgentService.getEstimatedWaitTime()
            ))
            .build();
    }
}

// 2. 工具服务
@Service
public class CustomerServiceTools {

    private final OrderService orderService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final TicketService ticketService;

    @Tool(description = "查询订单信息")
    public OrderInfo queryOrder(@Parameter(description = "订单号") String orderNumber) {
        Order order = orderService.findByOrderNumber(orderNumber);
        if (order == null) {
            return new OrderInfo(null, "未找到订单", null, null, null);
        }

        return new OrderInfo(
            order.getOrderNumber(),
            order.getStatus(),
            order.getCreateTime(),
            order.getItems(),
            order.getTrackingNumber()
        );
    }

    @Tool(description = "搜索产品信息")
    public List<ProductInfo> searchProducts(@Parameter(description = "产品关键词") String keyword) {
        return productService.searchByKeyword(keyword).stream()
            .map(product -> new ProductInfo(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.isInStock()
            ))
            .collect(Collectors.toList());
    }

    @Tool(description = "检查库存状态")
    public InventoryInfo checkInventory(@Parameter(description = "产品ID") String productId) {
        Inventory inventory = inventoryService.getByProductId(productId);
        return new InventoryInfo(
            productId,
            inventory.getQuantity(),
            inventory.getReserved(),
            inventory.getAvailable(),
            inventory.getNextRestockDate()
        );
    }

    @Tool(description = "创建客服工单")
    public TicketInfo createTicket(
            @Parameter(description = "问题描述") String description,
            @Parameter(description = "优先级") String priority,
            @Parameter(description = "用户ID") String userId) {

        Ticket ticket = ticketService.createTicket(description, priority, userId);
        return new TicketInfo(
            ticket.getId(),
            ticket.getStatus(),
            ticket.getPriority(),
            ticket.getAssignedAgent(),
            ticket.getEstimatedResolutionTime()
        );
    }

    // 记录类型
    public record OrderInfo(String orderNumber, String status, LocalDateTime createTime,
                           List<OrderItem> items, String trackingNumber) {}
    public record ProductInfo(String id, String name, BigDecimal price,
                             String description, boolean inStock) {}
    public record InventoryInfo(String productId, int quantity, int reserved,
                               int available, LocalDateTime nextRestockDate) {}
    public record TicketInfo(String id, String status, String priority,
                            String assignedAgent, LocalDateTime estimatedResolutionTime) {}
}
```

## 7. 最佳实践和生产部署

### 7.1 开发最佳实践

**1. 提示词工程最佳实践：**

```java
@Component
public class PromptTemplates {

    // 使用模板化提示词
    public static final String ANALYSIS_TEMPLATE = """
        你是一个{role}专家。请分析以下{document_type}：

        分析要求：
        {requirements}

        输出格式：
        {output_format}

        文档内容：
        {content}
        """;

    // 提示词构建器
    public String buildPrompt(String role, String documentType,
                             List<String> requirements, String outputFormat, String content) {
        return ANALYSIS_TEMPLATE
            .replace("{role}", role)
            .replace("{document_type}", documentType)
            .replace("{requirements}", String.join("\n", requirements))
            .replace("{output_format}", outputFormat)
            .replace("{content}", content);
    }
}
```

**2. 错误处理和重试策略：**

```java
@Component
public class ResilientAIService {

    @Retryable(
        value = {OpenAiApiException.class, TimeoutException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
    )
    public String callAIWithRetry(String prompt) {
        return chatClient.prompt(prompt).call().content();
    }

    @CircuitBreaker(name = "ai-service", fallbackMethod = "fallbackResponse")
    public String callAIWithCircuitBreaker(String prompt) {
        return chatClient.prompt(prompt).call().content();
    }

    public String fallbackResponse(String prompt, Exception e) {
        return "AI服务暂时不可用，请稍后重试。";
    }
}
```

**3. 监控和观测：**

```java
@Component
public class AIMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final Timer aiRequestTimer;
    private final Counter successCounter;
    private final Counter errorCounter;

    public AIMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.aiRequestTimer = Timer.builder("ai.request.duration").register(meterRegistry);
        this.successCounter = Counter.builder("ai.request.success").register(meterRegistry);
        this.errorCounter = Counter.builder("ai.request.error").register(meterRegistry);
    }

    public String timedAICall(String prompt) {
        return aiRequestTimer.recordCallable(() -> {
            try {
                String result = chatClient.prompt(prompt).call().content();
                successCounter.increment();
                return result;
            } catch (Exception e) {
                errorCounter.increment(Tags.of("error", e.getClass().getSimpleName()));
                throw e;
            }
        });
    }
}
```

### 7.2 生产环境配置

**1. 安全配置：**

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI}

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    org.springframework.ai: INFO
    com.example: INFO
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

**2. 性能调优：**

```java
@Configuration
public class PerformanceConfiguration {

    @Bean
    public TaskExecutor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("AI-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("ai-responses", "embeddings", "documents");
    }
}
```

### 7.3 总结

Spring AI框架为企业级AI应用开发提供了完整的解决方案：

**核心优势：**
1. **开发效率**：统一API、自动配置、丰富的生态系统
2. **企业级特性**：安全、监控、扩展性、可观测性
3. **模型可移植性**：避免供应商锁定，支持多种AI提供商
4. **Spring生态集成**：与Spring Boot/Cloud无缝集成

**最佳实践：**
1. **从简单开始**：先实现基础功能，再逐步扩展
2. **重视安全**：API密钥管理、内容过滤、访问控制
3. **性能优化**：缓存、批处理、异步处理、连接池
4. **监控运维**：指标收集、健康检查、错误处理

**Spring AI 1.0 实施建议：**

1. **版本选择**：
   - 生产环境推荐使用 Spring AI 1.0.0 正式版
   - 配合 Spring Boot 3.3+ 和 Java 17+ 使用
   - 使用 PostgreSQL 17 + pgvector 作为向量存储

2. **技术选型**：
   - **向量数据库**：PostgreSQL 17 + pgvector（推荐）、Oracle、Neo4j
   - **嵌入模型**：OpenAI text-embedding-3-large、Azure OpenAI
   - **聊天模型**：OpenAI GPT-4、Anthropic Claude、本地 Ollama

3. **架构设计**：
   - 使用新的 starter 依赖：`spring-ai-starter-vector-store-pgvector`
   - 启用观察性：配置日志记录和监控
   - 考虑扩展性：使用批处理和异步处理
   - 安全性：API 密钥管理、内容过滤、访问控制

4. **迁移指南**：
   - 更新依赖到 Spring AI 1.0.0
   - 替换废弃的配置属性
   - 使用新的自动配置模块
   - 测试向量存储兼容性

5. **生产部署**：
   - 使用 Docker Compose 或 Kubernetes
   - 配置 PostgreSQL 17 集群
   - 启用监控和日志收集
   - 实施备份和恢复策略

**总结：**

Spring AI 1.0 标志着框架的成熟，提供了生产级的稳定性和性能。通过本指南的学习和实践，结合 PostgreSQL 17 + pgvector 的强大向量搜索能力，您可以构建出高质量、可扩展的企业级 AI 应用。

框架的 1.0 版本确保了 API 稳定性和向后兼容性，是企业采用 Spring AI 进行生产部署的理想选择。

---

*本技术指南基于Spring AI 1.0.0版本编写，建议关注官方文档获取最新更新。*
