package com.aistudio.system.controller;

import com.aistudio.foundation.domain.PageResult;
import com.aistudio.foundation.domain.Result;
import com.aistudio.system.model.request.KnowledgeBaseCreateRequest;
import com.aistudio.system.model.request.KnowledgeBasePageRequest;
import com.aistudio.system.model.request.KnowledgeBaseUpdateRequest;
import com.aistudio.system.model.response.KnowledgeBaseResponse;
import com.aistudio.system.service.IKnowledgeBaseService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库管理接口，Controller 只负责参数接收和调用 Service。
 */
@Validated
@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    @Resource
    private IKnowledgeBaseService knowledgeBaseService;

    @PostMapping
    public Result<Long> createKnowledgeBase(@Valid @RequestBody KnowledgeBaseCreateRequest request) {
        return Result.success(knowledgeBaseService.createKnowledgeBase(request));
    }

    @PutMapping("/{id}")
    public Result<Void> updateKnowledgeBase(@PathVariable("id") @NotNull Long id,
                                            @Valid @RequestBody KnowledgeBaseUpdateRequest request) {
        request.setId(id);
        knowledgeBaseService.updateKnowledgeBase(request);
        return Result.success(null);
    }

    @GetMapping("/{id}")
    public Result<KnowledgeBaseResponse> getKnowledgeBase(@PathVariable("id") @NotNull Long id) {
        return Result.success(knowledgeBaseService.getKnowledgeBase(id));
    }

    @GetMapping("/page")
    public Result<PageResult<KnowledgeBaseResponse>> pageKnowledgeBase(@Valid KnowledgeBasePageRequest request) {
        return Result.success(knowledgeBaseService.pageKnowledgeBase(request));
    }

    @PutMapping("/{id}/enable")
    public Result<Void> enableKnowledgeBase(@PathVariable("id") @NotNull Long id) {
        knowledgeBaseService.enableKnowledgeBase(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/disable")
    public Result<Void> disableKnowledgeBase(@PathVariable("id") @NotNull Long id) {
        knowledgeBaseService.disableKnowledgeBase(id);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteKnowledgeBase(@PathVariable("id") @NotNull Long id) {
        knowledgeBaseService.deleteKnowledgeBase(id);
        return Result.success(null);
    }
}
