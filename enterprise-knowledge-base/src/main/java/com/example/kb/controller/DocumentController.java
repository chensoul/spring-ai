package com.example.kb.controller;

import com.example.kb.model.DocumentEntity;
import com.example.kb.model.Records.DocumentUploadResult;
import com.example.kb.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResult> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(value = "userId", defaultValue = "admin") String userId) {

        logger.info("收到文档上传请求: filename={}, category={}, userId={}", 
                file.getOriginalFilename(), category, userId);

        try {
            DocumentUploadResult result = documentService.uploadDocument(file, category, userId);

            if ("SUCCESS".equals(result.status())) {
                logger.info("文档上传成功: documentId={}", result.documentId());
                return ResponseEntity.ok(result);
            } else {
                logger.warn("文档上传失败: status={}, message={}", result.status(), result.message());
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error("文档上传异常: filename={}, error={}", file.getOriginalFilename(), e.getMessage(), e);
            DocumentUploadResult errorResult = new DocumentUploadResult(null, "ERROR", "文档上传失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    /**
     * 获取文档列表
     */
    @GetMapping
    public ResponseEntity<List<DocumentEntity>> getDocuments(
            @RequestParam(required = false) String category,
            @RequestParam(value = "userId", defaultValue = "admin") String userId) {

        logger.debug("查询文档列表: userId={}, category={}", userId, category);

        try {
            List<DocumentEntity> documents = documentService.getDocuments(userId, category);
            logger.debug("查询到文档数量: userId={}, count={}", userId, documents.size());
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            logger.warn("查询文档参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("查询文档异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentEntity> getDocumentById(
            @PathVariable Long id,
            @RequestParam(value = "userId", defaultValue = "admin") String userId) {

        logger.debug("查询文档详情: documentId={}, userId={}", id, userId);

        try {
            Optional<DocumentEntity> document = documentService.getDocumentById(id, userId);
            
            if (document.isPresent()) {
                logger.debug("文档详情查询成功: documentId={}", id);
                return ResponseEntity.ok(document.get());
            } else {
                logger.warn("文档不存在或无权限访问: documentId={}, userId={}", id, userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("查询文档详情异常: documentId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(
            @PathVariable Long id,
            @RequestParam(value = "userId", defaultValue = "admin") String userId) {

        logger.info("删除文档请求: documentId={}, userId={}", id, userId);

        try {
            documentService.deleteDocument(id, userId);
            logger.info("文档删除成功: documentId={}", id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "文档删除成功");
            response.put("documentId", id.toString());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("删除文档参数错误: documentId={}, error={}", id, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (SecurityException e) {
            logger.warn("删除文档权限不足: documentId={}, userId={}, error={}", id, userId, e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "无权限删除此文档");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            
        } catch (Exception e) {
            logger.error("删除文档异常: documentId={}, error={}", id, e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "文档删除失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 重新处理失败的文档
     */
    @PostMapping("/{id}/reprocess")
    public ResponseEntity<DocumentUploadResult> reprocessDocument(
            @PathVariable Long id,
            @RequestParam(value = "userId", defaultValue = "admin") String userId) {

        logger.info("重新处理文档请求: documentId={}, userId={}", id, userId);

        try {
            DocumentUploadResult result = documentService.reprocessDocument(id, userId);
            logger.info("文档重新处理启动成功: documentId={}", id);
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("重新处理文档参数错误: documentId={}, error={}", id, e.getMessage());
            DocumentUploadResult errorResult = new DocumentUploadResult(id, "VALIDATION_ERROR", e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
            
        } catch (SecurityException e) {
            logger.warn("重新处理文档权限不足: documentId={}, userId={}, error={}", id, userId, e.getMessage());
            DocumentUploadResult errorResult = new DocumentUploadResult(id, "PERMISSION_ERROR", "无权限重新处理此文档");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResult);
            
        } catch (Exception e) {
            logger.error("重新处理文档异常: documentId={}, error={}", id, e.getMessage(), e);
            DocumentUploadResult errorResult = new DocumentUploadResult(id, "ERROR", "重新处理失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    /**
     * 获取用户文档分类
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getUserCategories(
            @RequestParam(value = "userId", defaultValue = "admin") String userId) {

        logger.debug("查询用户文档分类: userId={}", userId);

        try {
            List<String> categories = documentService.getUserCategories(userId);
            logger.debug("查询到分类数量: userId={}, count={}", userId, categories.size());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("查询用户分类异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取失败的文档
     */
    @GetMapping("/failed")
    public ResponseEntity<List<DocumentEntity>> getFailedDocuments(
            @RequestParam(value = "userId", defaultValue = "admin") String userId) {

        logger.debug("查询失败文档: userId={}", userId);

        try {
            List<DocumentEntity> failedDocuments = documentService.getFailedDocuments(userId);
            logger.debug("查询到失败文档数量: userId={}, count={}", userId, failedDocuments.size());
            return ResponseEntity.ok(failedDocuments);
        } catch (Exception e) {
            logger.error("查询失败文档异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取文档统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDocumentStats(
            @RequestParam(value = "userId", defaultValue = "admin") String userId) {

        logger.debug("查询文档统计: userId={}", userId);

        try {
            List<DocumentEntity> allDocuments = documentService.getDocuments(userId, null);
            List<DocumentEntity> failedDocuments = documentService.getFailedDocuments(userId);
            List<String> categories = documentService.getUserCategories(userId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDocuments", allDocuments.size());
            stats.put("failedDocuments", failedDocuments.size());
            stats.put("completedDocuments", allDocuments.stream()
                    .filter(doc -> "COMPLETED".equals(doc.getStatus())).count());
            stats.put("processingDocuments", allDocuments.stream()
                    .filter(doc -> "PROCESSING".equals(doc.getStatus())).count());
            stats.put("categories", categories);
            stats.put("categoriesCount", categories.size());

            logger.debug("文档统计查询成功: userId={}, total={}, failed={}", 
                    userId, allDocuments.size(), failedDocuments.size());
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("查询文档统计异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批量删除文档
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteDocuments(
            @RequestBody List<Long> documentIds,
            @RequestParam(value = "userId", defaultValue = "admin") String userId) {

        logger.info("批量删除文档请求: documentIds={}, userId={}", documentIds, userId);

        Map<String, Object> response = new HashMap<>();
        List<Long> successIds = new java.util.ArrayList<>();
        List<Long> failedIds = new java.util.ArrayList<>();
        Map<Long, String> errorMessages = new HashMap<>();

        for (Long documentId : documentIds) {
            try {
                documentService.deleteDocument(documentId, userId);
                successIds.add(documentId);
                logger.debug("文档删除成功: documentId={}", documentId);
            } catch (Exception e) {
                failedIds.add(documentId);
                errorMessages.put(documentId, e.getMessage());
                logger.warn("文档删除失败: documentId={}, error={}", documentId, e.getMessage());
            }
        }

        response.put("successCount", successIds.size());
        response.put("failedCount", failedIds.size());
        response.put("successIds", successIds);
        response.put("failedIds", failedIds);
        response.put("errorMessages", errorMessages);

        logger.info("批量删除完成: 成功={}, 失败={}", successIds.size(), failedIds.size());
        
        if (failedIds.isEmpty()) {
            return ResponseEntity.ok(response);
        } else if (successIds.isEmpty()) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        }
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "DocumentController");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}