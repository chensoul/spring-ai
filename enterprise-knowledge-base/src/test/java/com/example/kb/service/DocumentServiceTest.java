package com.example.kb.service;

import com.example.kb.dto.DocumentUploadResult;
import com.example.kb.model.DocumentEntity;
import com.example.kb.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 文档服务测试类
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private VectorStore vectorStore;
    
    @Mock
    private DocumentRepository documentRepository;
    
    @Mock
    private DocumentReader pdfDocumentReader;
    
    @Mock
    private DocumentReader tikaDocumentReader;
    
    @Mock
    private TextSplitter textSplitter;
    
    private DocumentService documentService;
    
    @BeforeEach
    void setUp() {
        documentService = new DocumentService(
            vectorStore,
            documentRepository,
            pdfDocumentReader,
            tikaDocumentReader,
            textSplitter
        );
        
        // 设置配置属性
        ReflectionTestUtils.setField(documentService, "storagePath", "./test-uploads");
        ReflectionTestUtils.setField(documentService, "maxFileSize", 52428800L);
        ReflectionTestUtils.setField(documentService, "allowedTypes", "pdf,txt,docx,md");
    }
    
    @Test
    void testUploadDocument_Success() {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "这是一个测试文档内容".getBytes()
        );
        
        DocumentEntity savedDocument = new DocumentEntity();
        savedDocument.setId(1L);
        savedDocument.setFilename("test.txt");
        savedDocument.setFileSize(file.getSize());
        
        when(documentRepository.save(any(DocumentEntity.class))).thenReturn(savedDocument);
        
        // 执行测试
        DocumentUploadResult result = documentService.uploadDocument(file, "测试", "testuser");
        
        // 验证结果
        assertNotNull(result);
        assertEquals("SUCCESS", result.status());
        assertEquals("test.txt", result.filename());
        assertEquals(1L, result.documentId());
        
        // 验证方法调用
        verify(documentRepository).save(any(DocumentEntity.class));
    }
    
    @Test
    void testUploadDocument_EmptyFile() {
        // 准备测试数据
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.txt",
            "text/plain",
            new byte[0]
        );
        
        // 执行测试
        DocumentUploadResult result = documentService.uploadDocument(emptyFile, "测试", "testuser");
        
        // 验证结果
        assertNotNull(result);
        assertEquals("ERROR", result.status());
        assertTrue(result.message().contains("文件不能为空"));
        
        // 验证没有调用保存方法
        verify(documentRepository, never()).save(any(DocumentEntity.class));
    }
    
    @Test
    void testUploadDocument_UnsupportedFileType() {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.exe",
            "application/octet-stream",
            "binary content".getBytes()
        );
        
        // 执行测试
        DocumentUploadResult result = documentService.uploadDocument(file, "测试", "testuser");
        
        // 验证结果
        assertNotNull(result);
        assertEquals("ERROR", result.status());
        assertTrue(result.message().contains("不支持的文件类型"));
        
        // 验证没有调用保存方法
        verify(documentRepository, never()).save(any(DocumentEntity.class));
    }
}