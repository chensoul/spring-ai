package com.example.kb.service;

import com.example.kb.dto.DocumentInfo;
import com.example.kb.dto.DocumentUploadResult;
import com.example.kb.model.DocumentEntity;
import com.example.kb.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 文档服务类
 * <p>
 * 负责文档的上传、处理、存储和管理
 */
@Service
@Transactional
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final DocumentReader pdfDocumentReader;
    private final DocumentReader tikaDocumentReader;
    private final TextSplitter textSplitter;

    @Value("${app.knowledge-base.document.storage-path:./uploads}")
    private String storagePath;

    @Value("${app.knowledge-base.document.max-size:52428800}") // 50MB
    private long maxFileSize;

    @Value("${app.knowledge-base.document.allowed-types:pdf,txt,docx,md}")
    private String allowedTypes;

    public DocumentService(VectorStore vectorStore,
                           DocumentRepository documentRepository,
                           @Qualifier("pdfDocumentReader") DocumentReader pdfDocumentReader,
                           @Qualifier("tikaDocumentReader") DocumentReader tikaDocumentReader,
                           TextSplitter textSplitter) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
        this.pdfDocumentReader = pdfDocumentReader;
        this.tikaDocumentReader = tikaDocumentReader;
        this.textSplitter = textSplitter;
    }

    /**
     * 上传文档
     */
    public DocumentUploadResult uploadDocument(MultipartFile file, String category, String userId) {
        try {
            // 验证文件
            validateFile(file);

            // 保存文件到磁盘
            String filePath = saveFile(file);

            // 保存文档信息到数据库
            DocumentEntity document = new DocumentEntity();
            document.setFilename(file.getOriginalFilename());
            document.setFilePath(filePath);
            document.setCategory(category);
            document.setUploadedBy(userId);
            document.setFileSize(file.getSize());
            document.setContentType(file.getContentType());
            document.setStatus("PROCESSING");

            DocumentEntity savedDoc = documentRepository.save(document);

            // 异步处理文档
            processDocumentAsync(savedDoc);

            return DocumentUploadResult.success(
                    savedDoc.getId(),
                    savedDoc.getFilename(),
                    savedDoc.getFileSize()
            );

        } catch (Exception e) {
            logger.error("文档上传失败: {}", e.getMessage(), e);
            return DocumentUploadResult.error(
                    file.getOriginalFilename(),
                    "文档上传失败：" + e.getMessage()
            );
        }
    }

    /**
     * 异步处理文档
     */
    @Async("documentTaskExecutor")
    public CompletableFuture<Void> processDocumentAsync(DocumentEntity document) {
        try {
            logger.info("开始处理文档: {}", document.getFilename());

            // 读取文档内容
            List<Document> docs = readDocument(document.getFilePath(), document.getContentType());

            // 分割文档
            List<Document> splitDocs = textSplitter.apply(docs);

            // 添加元数据
            splitDocs.forEach(doc -> {
                doc.getMetadata().put("document_id", document.getId().toString());
                doc.getMetadata().put("filename", document.getFilename());
                doc.getMetadata().put("category", document.getCategory());
                doc.getMetadata().put("upload_time", document.getUploadTime().toString());
                doc.getMetadata().put("uploaded_by", document.getUploadedBy());
            });

            // 存储到向量数据库
            vectorStore.add(splitDocs);

            // 更新文档状态
            document.setStatus("COMPLETED");
            document.setProcessedTime(LocalDateTime.now());
            document.setChunkCount(splitDocs.size());
            documentRepository.save(document);

            logger.info("文档处理完成: {}, 分块数量: {}", document.getFilename(), splitDocs.size());

        } catch (Exception e) {
            logger.error("文档处理失败: {}", document.getFilename(), e);

            // 更新失败状态
            document.setStatus("FAILED");
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 获取用户文档列表
     */
    public List<DocumentInfo> getDocuments(String userId, String category) {
        List<DocumentEntity> documents;

        if (category != null && !category.trim().isEmpty()) {
            documents = documentRepository.findByUploadedByAndCategory(userId, category);
        } else {
            documents = documentRepository.findByUploadedByOrderByUploadTimeDesc(userId);
        }

        return documents.stream()
                .map(DocumentInfo::from)
                .collect(Collectors.toList());
    }

    /**
     * 分页获取用户文档
     */
    public Page<DocumentInfo> getDocuments(String userId, String category, Pageable pageable) {
        Page<DocumentEntity> documents;

        if (category != null && !category.trim().isEmpty()) {
            documents = documentRepository.findByUploadedByAndCategory(userId, category, pageable);
        } else {
            documents = documentRepository.findByUploadedBy(userId, pageable);
        }

        return documents.map(DocumentInfo::from);
    }

    /**
     * 获取文档详情
     */
    public Optional<DocumentInfo> getDocument(Long documentId, String userId) {
        return documentRepository.findById(documentId)
                .filter(doc -> doc.getUploadedBy().equals(userId))
                .map(DocumentInfo::from);
    }

    /**
     * 删除文档
     */
    public void deleteDocument(Long documentId, String userId) {
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        if (!document.getUploadedBy().equals(userId)) {
            throw new RuntimeException("无权限删除此文档");
        }

        try {
            // 从向量数据库删除
            // 注意：这里需要根据实际的VectorStore实现来删除相关向量
            // vectorStore.delete(List.of(documentId.toString()));

            // 删除物理文件
            if (document.getFilePath() != null) {
                Path filePath = Paths.get(document.getFilePath());
                Files.deleteIfExists(filePath);
            }

            // 从数据库删除
            documentRepository.delete(document);

            logger.info("文档删除成功: {}", document.getFilename());

        } catch (Exception e) {
            logger.error("文档删除失败: {}", document.getFilename(), e);
            throw new RuntimeException("文档删除失败：" + e.getMessage());
        }
    }

    /**
     * 搜索文档
     */
    public List<DocumentInfo> searchDocuments(String keyword, String userId) {
        List<DocumentEntity> documents = documentRepository.findByKeyword(keyword);

        return documents.stream()
                .filter(doc -> doc.getUploadedBy().equals(userId))
                .map(DocumentInfo::from)
                .collect(Collectors.toList());
    }

    /**
     * 获取文档统计信息
     */
    public DocumentStatistics getStatistics(String userId) {
        long totalCount = documentRepository.countByUploadedBy(userId);
        long processingCount = documentRepository.countByStatus("PROCESSING");
        long completedCount = documentRepository.countByStatus("COMPLETED");
        long failedCount = documentRepository.countByStatus("FAILED");

        List<Object[]> categoryStats = documentRepository.countByCategoryAndUser(userId);

        return new DocumentStatistics(
                totalCount,
                processingCount,
                completedCount,
                failedCount,
                categoryStats
        );
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制：" + (maxFileSize / 1024 / 1024) + "MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(filename);
        if (!isAllowedType(extension)) {
            throw new IllegalArgumentException("不支持的文件类型：" + extension);
        }
    }

    /**
     * 保存文件到磁盘
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 创建存储目录
        Path uploadDir = Paths.get(storagePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        Path filePath = uploadDir.resolve(uniqueFilename);

        // 保存文件
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    /**
     * 读取文档内容
     */
    private List<Document> readDocument(String filePath, String contentType) throws IOException {
        Path path = Paths.get(filePath);
        Resource resource = new InputStreamResource(Files.newInputStream(path));

        DocumentReader reader;
        if (contentType != null && contentType.equals("application/pdf")) {
            reader = pdfDocumentReader;
        } else {
            reader = tikaDocumentReader;
        }

        return reader.get();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 检查是否为允许的文件类型
     */
    private boolean isAllowedType(String extension) {
        String[] types = allowedTypes.split(",");
        for (String type : types) {
            if (type.trim().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 文档统计信息记录类
     */
    public record DocumentStatistics(
            long totalCount,
            long processingCount,
            long completedCount,
            long failedCount,
            List<Object[]> categoryStats
    ) {
    }
}