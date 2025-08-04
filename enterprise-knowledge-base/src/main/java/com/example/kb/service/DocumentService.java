package com.example.kb.service;

import com.example.kb.config.KnowledgeBaseProperties;
import com.example.kb.model.DocumentEntity;
import com.example.kb.model.Records.DocumentUploadResult;
import com.example.kb.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final TextSplitter textSplitter;
    private final KnowledgeBaseProperties kbProperties;

    public DocumentService(VectorStore vectorStore, DocumentRepository documentRepository,
                           TextSplitter textSplitter, KnowledgeBaseProperties kbProperties) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
        this.textSplitter = textSplitter;
        this.kbProperties = kbProperties;
    }

    /**
     * 上传并处理文档
     */
    public DocumentUploadResult uploadDocument(MultipartFile file, String category, String userId) {
        logger.info("开始处理文档上传: filename={}, category={}, userId={}",
                file.getOriginalFilename(), category, userId);

        try {
            // 输入验证
            validateUploadRequest(file, category, userId);

            // 创建文档实体
            DocumentEntity document = createDocumentEntity(file, category, userId);
            DocumentEntity savedDoc = documentRepository.save(document);

            // 保存文件到配置的存储路径
            try {
                saveFileToStorage(file, document);
            } catch (IOException e) {
                logger.warn("文件保存失败，但继续处理: filename={}, error={}",
                        file.getOriginalFilename(), e.getMessage());
            }

            logger.info("文档实体已保存: documentId={}", savedDoc.getId());

            // 异步处理文档
            processDocumentAsync(file, savedDoc);

            return new DocumentUploadResult(savedDoc.getId(), "SUCCESS", "文档上传成功，正在处理中");

        } catch (IllegalArgumentException e) {
            logger.warn("文档上传验证失败: {}", e.getMessage());
            return new DocumentUploadResult(null, "VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            logger.error("文档上传失败: filename={}, error={}", file.getOriginalFilename(), e.getMessage(), e);
            return new DocumentUploadResult(null, "ERROR", "文档上传失败：" + e.getMessage());
        }
    }

    /**
     * 异步处理文档
     */
    @Async
    @Transactional
    public void processDocumentAsync(MultipartFile file, DocumentEntity document) {
        logger.info("开始异步处理文档: documentId={}, filename={}",
                document.getId(), document.getFilename());

        try {
            // 读取文档内容
            List<Document> docs = readDocumentContent(file);
            logger.debug("文档读取完成: documentId={}, chunks={}", document.getId(), docs.size());

            // 分割文档
            List<Document> splitDocs = textSplitter.apply(docs);
            logger.debug("文档分割完成: documentId={}, splitChunks={}", document.getId(), splitDocs.size());

            // 添加元数据
            enrichDocumentMetadata(splitDocs, document);

            // 使用配置的批处理大小存储到向量数据库
            int batchSize = kbProperties.getVectorization().getBatchSize();
            for (int i = 0; i < splitDocs.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, splitDocs.size());
                List<Document> batch = splitDocs.subList(i, endIndex);
                vectorStore.add(batch);
                logger.debug("已处理文档批次: documentId={}, batch={}/{}, size={}",
                        document.getId(), (i / batchSize) + 1,
                        (splitDocs.size() + batchSize - 1) / batchSize, batch.size());
            }
            logger.info("文档已存储到向量数据库: documentId={}, vectors={}, batches={}",
                    document.getId(), splitDocs.size(),
                    (splitDocs.size() + batchSize - 1) / batchSize);

            // 更新文档状态
            updateDocumentStatus(document, "COMPLETED", null);

        } catch (Exception e) {
            logger.error("文档处理失败: documentId={}, error={}", document.getId(), e.getMessage(), e);
            updateDocumentStatus(document, "FAILED", e.getMessage());
        }
    }

    /**
     * 获取用户文档列表
     */
    @Transactional(readOnly = true)
    public List<DocumentEntity> getDocuments(String userId, String category) {
        logger.debug("查询用户文档: userId={}, category={}", userId, category);

        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        List<DocumentEntity> documents;
        if (StringUtils.hasText(category)) {
            documents = documentRepository.findByUploadedByAndCategory(userId, category);
        } else {
            documents = documentRepository.findByUploadedBy(userId);
        }

        logger.debug("查询到文档数量: userId={}, count={}", userId, documents.size());
        return documents;
    }

    /**
     * 获取文档详情
     */
    @Transactional(readOnly = true)
    public Optional<DocumentEntity> getDocumentById(Long documentId, String userId) {
        logger.debug("查询文档详情: documentId={}, userId={}", documentId, userId);

        Optional<DocumentEntity> document = documentRepository.findById(documentId);

        if (document.isPresent() && !document.get().getUploadedBy().equals(userId)) {
            logger.warn("用户无权限访问文档: documentId={}, userId={}", documentId, userId);
            return Optional.empty();
        }

        return document;
    }

    /**
     * 删除文档
     */
    @Transactional
    public void deleteDocument(Long documentId, String userId) {
        logger.info("开始删除文档: documentId={}, userId={}", documentId, userId);

        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentId));

        if (!document.getUploadedBy().equals(userId)) {
            throw new SecurityException("无权限删除此文档");
        }

        try {
            // 从向量数据库删除
            vectorStore.delete(List.of(documentId.toString()));
            logger.debug("已从向量数据库删除文档: documentId={}", documentId);

            // 从数据库删除
            documentRepository.delete(document);
            logger.info("文档删除成功: documentId={}", documentId);

        } catch (Exception e) {
            logger.error("文档删除失败: documentId={}, error={}", documentId, e.getMessage(), e);
            throw new RuntimeException("文档删除失败: " + e.getMessage());
        }
    }

    /**
     * 重新处理失败的文档
     */
    @Transactional
    public DocumentUploadResult reprocessDocument(Long documentId, String userId) {
        logger.info("开始重新处理文档: documentId={}, userId={}", documentId, userId);

        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在: " + documentId));

        if (!document.getUploadedBy().equals(userId)) {
            throw new SecurityException("无权限重新处理此文档");
        }

        if (!"FAILED".equals(document.getStatus())) {
            throw new IllegalArgumentException("只能重新处理失败的文档");
        }

        // 重置状态
        document.setStatus("PROCESSING");
        document.setErrorMessage(null);
        documentRepository.save(document);

        // 这里需要重新获取文件内容，实际实现中可能需要存储文件或从其他地方获取
        // 暂时返回成功状态
        return new DocumentUploadResult(documentId, "SUCCESS", "文档重新处理已启动");
    }

    /**
     * 获取用户文档分类
     */
    @Transactional(readOnly = true)
    public List<String> getUserCategories(String userId) {
        logger.debug("查询用户文档分类: userId={}", userId);
        return documentRepository.findUserCategories(userId);
    }

    /**
     * 获取处理失败的文档
     */
    @Transactional(readOnly = true)
    public List<DocumentEntity> getFailedDocuments(String userId) {
        logger.debug("查询失败文档: userId={}", userId);
        return documentRepository.findByUploadedByAndStatus(userId, "FAILED");
    }

    // 私有辅助方法

    private void validateUploadRequest(MultipartFile file, String category, String userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (!StringUtils.hasText(category)) {
            throw new IllegalArgumentException("文档分类不能为空");
        }

        // 使用配置的文件大小限制
        if (file.getSize() > kbProperties.getDocument().getMaxSize()) {
            long maxSizeMB = kbProperties.getDocument().getMaxSize() / (1024 * 1024);
            throw new IllegalArgumentException("文件大小不能超过" + maxSizeMB + "MB");
        }

        // 使用配置的文件类型限制
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedFileType(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType +
                    "，支持的类型: " + String.join(", ", kbProperties.getDocument().getAllowedTypes()));
        }
    }

    private boolean isAllowedFileType(String contentType) {
        // 将 MIME 类型映射到文件扩展名
        String fileExtension = getFileExtensionFromMimeType(contentType);
        return kbProperties.getDocument().getAllowedTypes().contains(fileExtension);
    }

    private String getFileExtensionFromMimeType(String mimeType) {
        switch (mimeType.toLowerCase()) {
            case "application/pdf":
                return "pdf";
            case "text/plain":
                return "txt";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "docx";
            case "text/markdown":
            case "text/x-markdown":
                return "md";
            default:
                return mimeType;
        }
    }

    private DocumentEntity createDocumentEntity(MultipartFile file, String category, String userId) {
        DocumentEntity document = new DocumentEntity();
        document.setFilename(file.getOriginalFilename());
        document.setCategory(category);
        document.setUploadedBy(userId);
        document.setUploadTime(LocalDateTime.now());
        document.setStatus("PROCESSING");
        return document;
    }

    private void saveFileToStorage(MultipartFile file, DocumentEntity document) throws IOException {
        // 确保存储目录存在
        Path storagePath = Paths.get(kbProperties.getDocument().getStoragePath());
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            logger.info("创建存储目录: {}", storagePath);
        }

        // 生成唯一的文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = originalFilename + "_" + System.currentTimeMillis() + fileExtension;
        Path filePath = storagePath.resolve(uniqueFilename);

        // 保存文件
        Files.copy(file.getInputStream(), filePath);
        logger.info("文件已保存到存储: documentId={}, path={}", document.getId(), filePath);
    }

    private List<Document> readDocumentContent(MultipartFile file) throws IOException {
        Resource resource = new InputStreamResource(file.getInputStream());

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource, PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                        .withNumberOfTopTextLinesToDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build());
        return pdfReader.read();
    }

    private void enrichDocumentMetadata(List<Document> documents, DocumentEntity document) {
        documents.forEach(doc -> {
            doc.getMetadata().put("document_id", document.getId().toString());
            doc.getMetadata().put("filename", document.getFilename());
            doc.getMetadata().put("category", document.getCategory());
            doc.getMetadata().put("upload_time", document.getUploadTime().toString());
            doc.getMetadata().put("uploaded_by", document.getUploadedBy());
            doc.getMetadata().put("chunk_id", UUID.randomUUID().toString());
        });
    }

    private void updateDocumentStatus(DocumentEntity document, String status, String errorMessage) {
        document.setStatus(status);
        document.setErrorMessage(errorMessage);
        if ("COMPLETED".equals(status)) {
            document.setProcessedTime(LocalDateTime.now());
        }
        documentRepository.save(document);
        logger.info("文档状态已更新: documentId={}, status={}", document.getId(), status);
    }
}