FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制应用文件
COPY target/enterprise-knowledge-base-1.0.0.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]