package com.example.kb.controller;

import com.example.kb.dto.DocumentInfo;
import com.example.kb.dto.DocumentUploadResult;
import com.example.kb.service.DocumentService;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 文档管理控制器
 * 
 * 提供文档上传、查询、删除等功能的REST API
 */
@RestController
@RequestMapping("/documents")
@PreAuthorize("hasRole('USER')")
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
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        try {
            String userId = authentication.getName();
            logger.info("用户 {} 上传文档: {}", userId, file.getOriginalFilename());
            
            DocumentUploadResult result = documentService.uploadDocument(file, category, userId);
            
            if ("SUCCESS".equals(result.status())) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            logger.error("文档上传失败", e);
            return ResponseEntity.badRequest().body(
                DocumentUploadResult.error(file.getOriginalFilename(), e.getMessage())
            );
        }
    }
    
    /**
     * 获取文档列表
     */
    @GetMapping
    public ResponseEntity<List<DocumentInfo>> getDocuments(
            @RequestParam(value = "category", required = false) String category,
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<DocumentInfo> documents = documentService.getDocuments(userId, category);
        return ResponseEntity.ok(documents);
    }
    
    /**
     * 分页获取文档列表
     */
    @GetMapping("/page")
    public ResponseEntity<Page<DocumentInfo>> getDocumentsPage(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "uploadTime") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            Authentication authentication) {
        
        String userId = authentication.getName();
        
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<DocumentInfo> documents = documentService.getDocuments(userId, category, pageable);
        
        return ResponseEntity.ok(documents);
    }
    
    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentInfo> getDocument(
            @PathVariable Long id,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Optional<DocumentInfo> document = documentService.getDocument(id, userId);
        
        return document.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            String userId = authentication.getName();
            documentService.deleteDocument(id, userId);
            logger.info("用户 {} 删除文档: {}", userId, id);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("文档删除失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 搜索文档
     */
    @GetMapping("/search")
    public ResponseEntity<List<DocumentInfo>> searchDocuments(
            @RequestParam @NotBlank String keyword,
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<DocumentInfo> documents = documentService.searchDocuments(keyword, userId);
        return ResponseEntity.ok(documents);
    }
    
    /**
     * 获取文档统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<DocumentService.DocumentStatistics> getStatistics(
            Authentication authentication) {
        
        String userId = authentication.getName();
        DocumentService.DocumentStatistics statistics = documentService.getStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 批量删除文档
     */
    @DeleteMapping("/batch")
    public ResponseEntity<BatchOperationResult> batchDeleteDocuments(
            @RequestBody List<Long> documentIds,
            Authentication authentication) {
        
        String userId = authentication.getName();
        int successCount = 0;
        int failureCount = 0;
        
        for (Long id : documentIds) {
            try {
                documentService.deleteDocument(id, userId);
                successCount++;
            } catch (Exception e) {
                logger.error("批量删除文档失败: {}", id, e);
                failureCount++;
            }
        }
        
        BatchOperationResult result = new BatchOperationResult(
            successCount, 
            failureCount, 
            "批量删除完成"
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 批量操作结果记录类
     */
    public record BatchOperationResult(
        int successCount,
        int failureCount,
        String message
    ) {}
}