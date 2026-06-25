package com.aistudio.system.controller;

import com.aistudio.foundation.domain.PageResult;
import com.aistudio.foundation.domain.Result;
import com.aistudio.system.model.request.KnowledgeChunkPageRequest;
import com.aistudio.system.model.response.KnowledgeChunkResponse;
import com.aistudio.system.service.IKnowledgeDocumentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识分片预览接口。
 */
@Validated
@RestController
@RequestMapping("/api/knowledge-chunks")
public class KnowledgeChunkController {

    @Resource
    private IKnowledgeDocumentService knowledgeDocumentService;

    @GetMapping("/page")
    public Result<PageResult<KnowledgeChunkResponse>> pageChunk(@Valid KnowledgeChunkPageRequest request) {
        return Result.success(knowledgeDocumentService.pageChunk(request));
    }
}
