package com.aistudio.system.controller;

import com.aistudio.foundation.domain.PageResult;
import com.aistudio.foundation.domain.Result;
import com.aistudio.system.model.request.KnowledgeDocumentPageRequest;
import com.aistudio.system.model.request.KnowledgeDocumentUploadRequest;
import com.aistudio.system.model.response.KnowledgeDocumentResponse;
import com.aistudio.system.model.response.KnowledgeDocumentUploadResponse;
import com.aistudio.system.model.response.KnowledgeTaskProgressResponse;
import com.aistudio.system.service.IKnowledgeDocumentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识文档管理接口，Controller 只负责参数接收和调用 Service。
 */
@Validated
@RestController
@RequestMapping("/api/knowledge-documents")
public class KnowledgeDocumentController {

    @Resource
    private IKnowledgeDocumentService knowledgeDocumentService;

    @PostMapping("/upload")
    public Result<KnowledgeDocumentUploadResponse> uploadDocument(@RequestParam("knowledgeBaseId") @NotNull Long knowledgeBaseId,
                                                                  @RequestParam("directoryId") @NotNull Long directoryId,
                                                                  @RequestParam("file") MultipartFile file) {
        KnowledgeDocumentUploadRequest request = KnowledgeDocumentUploadRequest.builder()
                .knowledgeBaseId(knowledgeBaseId)
                .directoryId(directoryId)
                .file(file)
                .build();
        return Result.success(knowledgeDocumentService.uploadDocument(request));
    }

    @GetMapping({"/page", ""})
    public Result<PageResult<KnowledgeDocumentResponse>> pageDocument(@Valid KnowledgeDocumentPageRequest request) {
        return Result.success(knowledgeDocumentService.pageDocument(request));
    }

    @GetMapping("/tasks/{taskId}/progress")
    public Result<KnowledgeTaskProgressResponse> getTaskProgress(@PathVariable("taskId") Long taskId) {
        return Result.success(knowledgeDocumentService.getTaskProgress(taskId));
    }
}
