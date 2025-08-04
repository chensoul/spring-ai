package com.example.kb.controller;

import com.example.kb.model.DocumentEntity;
import com.example.kb.model.Records.DocumentUploadResult;
import com.example.kb.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResult> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) {

        String userId = "admin";
        DocumentUploadResult result = documentService.uploadDocument(file, category, userId);

        if ("SUCCESS".equals(result.status())) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentEntity>> getDocuments(
            @RequestParam(required = false) String category) {

        String userId = "admin";
        List<DocumentEntity> documents = documentService.getDocuments(userId, category);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        String userId = "admin";
        documentService.deleteDocument(id, userId);
        return ResponseEntity.ok().build();
    }
}