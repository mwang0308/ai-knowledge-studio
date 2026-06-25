package com.aistudio.system.controller;

import com.aistudio.foundation.domain.Result;
import com.aistudio.system.model.request.DocumentProcessCallbackRequest;
import com.aistudio.system.service.IKnowledgeDocumentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Python 文档处理服务内部回调接口。
 */
@Validated
@RestController
@RequestMapping("/api/internal/document-process")
public class InternalDocumentProcessController {

    @Resource
    private IKnowledgeDocumentService knowledgeDocumentService;

    @PostMapping("/callback")
    public Result<Boolean> handleProcessCallback(@RequestBody @Valid DocumentProcessCallbackRequest request) {
        knowledgeDocumentService.handleProcessCallback(request);
        return Result.success(Boolean.TRUE);
    }
}
