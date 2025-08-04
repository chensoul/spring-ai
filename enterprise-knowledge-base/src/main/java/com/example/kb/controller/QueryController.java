package com.example.kb.controller;

import com.example.kb.dto.QueryRequest;
import com.example.kb.dto.QueryResult;
import com.example.kb.model.QueryEntity;
import com.example.kb.service.QueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 查询控制器
 * 
 * 提供知识库查询、对话历史等功能的REST API
 */
@RestController
@RequestMapping("/query")
@PreAuthorize("hasRole('USER')")
public class QueryController {
    
    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);
    
    private final QueryService queryService;
    
    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }
    
    /**
     * 执行查询
     */
    @PostMapping
    public ResponseEntity<QueryResult> query(
            @Valid @RequestBody QueryRequest request,
            Authentication authentication) {
        
        try {
            String userId = authentication.getName();
            logger.info("用户 {} 执行查询: {}", userId, request.question());
            
            QueryResult result = queryService.query(request, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("查询执行失败", e);
            return ResponseEntity.ok(QueryResult.error("查询失败：" + e.getMessage()));
        }
    }
    
    /**
     * 快速查询（仅支持简单问题）
     */
    @PostMapping("/quick")
    public ResponseEntity<QueryResult> quickQuery(
            @RequestParam @NotBlank String question,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "true") boolean useRag,
            Authentication authentication) {
        
        try {
            String userId = authentication.getName();
            QueryRequest request = new QueryRequest(question, category, null, useRag);
            
            QueryResult result = queryService.query(request, userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("快速查询失败", e);
            return ResponseEntity.ok(QueryResult.error("查询失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取查询历史
     */
    @GetMapping("/history")
    public ResponseEntity<List<QueryEntity>> getHistory(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit,
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<QueryEntity> history = queryService.getQueryHistory(userId, limit);
        return ResponseEntity.ok(history);
    }
    
    /**
     * 获取会话历史
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<QueryEntity>> getSessionHistory(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        // 这里可以添加权限检查，确保用户只能访问自己的会话
        List<QueryEntity> history = queryService.getSessionHistory(sessionId);
        
        // 过滤出当前用户的查询
        String userId = authentication.getName();
        List<QueryEntity> userHistory = history.stream()
            .filter(query -> query.getUserId().equals(userId))
            .toList();
        
        return ResponseEntity.ok(userHistory);
    }
    
    /**
     * 搜索查询历史
     */
    @GetMapping("/search")
    public ResponseEntity<List<QueryEntity>> searchHistory(
            @RequestParam @NotBlank String keyword,
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<QueryEntity> results = queryService.searchQueryHistory(userId, keyword);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 获取查询统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<QueryService.QueryStatistics> getStatistics(
            Authentication authentication) {
        
        String userId = authentication.getName();
        QueryService.QueryStatistics statistics = queryService.getStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取热门查询
     */
    @GetMapping("/popular")
    public ResponseEntity<List<String>> getPopularQueries(
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit) {
        
        List<String> popularQueries = queryService.getPopularQueries(limit);
        return ResponseEntity.ok(popularQueries);
    }
    
    /**
     * 清理历史查询（管理员功能）
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cleanupHistory(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int daysToKeep) {
        
        try {
            queryService.cleanupOldQueries(daysToKeep);
            return ResponseEntity.ok("清理完成，保留了最近 " + daysToKeep + " 天的查询记录");
            
        } catch (Exception e) {
            logger.error("清理查询历史失败", e);
            return ResponseEntity.badRequest().body("清理失败：" + e.getMessage());
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<HealthStatus> health() {
        try {
            // 执行一个简单的测试查询
            QueryRequest testRequest = new QueryRequest("测试", null, null, false);
            QueryResult result = queryService.query(testRequest, "system");
            
            return ResponseEntity.ok(new HealthStatus("UP", "查询服务正常"));
            
        } catch (Exception e) {
            logger.error("查询服务健康检查失败", e);
            return ResponseEntity.ok(new HealthStatus("DOWN", "查询服务异常：" + e.getMessage()));
        }
    }
    
    /**
     * 健康状态记录类
     */
    public record HealthStatus(String status, String message) {}
}