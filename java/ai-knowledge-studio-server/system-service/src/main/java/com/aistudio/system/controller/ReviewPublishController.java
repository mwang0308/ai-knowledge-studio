package com.aistudio.system.controller;

import com.aistudio.foundation.domain.Result;
import com.aistudio.system.model.request.PublishSubmitRequest;
import com.aistudio.system.model.request.ReviewSubmitRequest;
import com.aistudio.system.service.IKnowledgeDocumentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审核发布接口，控制知识是否进入正式检索范围。
 */
@Validated
@RestController
@RequestMapping("/api/knowledge-governance")
public class ReviewPublishController {

    @Resource
    private IKnowledgeDocumentService knowledgeDocumentService;

    @PostMapping("/review/pass")
    public Result<Boolean> passReview(@RequestBody @Valid ReviewSubmitRequest request) {
        knowledgeDocumentService.passReview(request);
        return Result.success(Boolean.TRUE);
    }

    @PostMapping("/review/reject")
    public Result<Boolean> rejectReview(@RequestBody @Valid ReviewSubmitRequest request) {
        knowledgeDocumentService.rejectReview(request);
        return Result.success(Boolean.TRUE);
    }

    @PostMapping("/publish")
    public Result<Boolean> publish(@RequestBody @Valid PublishSubmitRequest request) {
        knowledgeDocumentService.publishDocument(request);
        return Result.success(Boolean.TRUE);
    }

    @PostMapping("/offline")
    public Result<Boolean> offline(@RequestBody @Valid PublishSubmitRequest request) {
        knowledgeDocumentService.offlineDocument(request);
        return Result.success(Boolean.TRUE);
    }
}
