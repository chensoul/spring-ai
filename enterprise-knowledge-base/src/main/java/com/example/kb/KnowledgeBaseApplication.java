package com.example.kb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 企业知识库系统主应用类
 * 
 * 功能特性：
 * - RAG（检索增强生成）知识问答
 * - 文档上传和智能处理
 * - 向量化存储和相似性搜索
 * - 用户权限管理
 * - 异步文档处理
 * 
 * @author Spring AI Demo
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableAsync
@EnableScheduling
public class KnowledgeBaseApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeBaseApplication.class, args);
        
        System.out.println("""
            
            ========================================
            🚀 企业知识库系统启动成功！
            ========================================
            
            📚 功能特性：
            • RAG智能问答
            • 文档上传处理
            • 向量化搜索
            • 用户权限管理
            
            🔗 访问地址：
            • 应用首页: http://localhost:8080/api
            • 健康检查: http://localhost:8080/api/actuator/health
            • H2控制台: http://localhost:8080/api/h2-console
            • API文档: http://localhost:8080/api/swagger-ui.html
            
            🔑 默认账户：
            • 用户名: admin
            • 密码: admin123
            
            ========================================
            """);
    }
}