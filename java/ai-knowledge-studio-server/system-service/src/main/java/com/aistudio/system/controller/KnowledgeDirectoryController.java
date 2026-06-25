package com.aistudio.system.controller;

import com.aistudio.foundation.domain.Result;
import com.aistudio.system.model.request.KnowledgeDirectoryCreateRequest;
import com.aistudio.system.model.request.KnowledgeDirectoryTreeRequest;
import com.aistudio.system.model.request.KnowledgeDirectoryUpdateRequest;
import com.aistudio.system.model.response.KnowledgeDirectoryResponse;
import com.aistudio.system.service.IKnowledgeDirectoryService;
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

import java.util.List;

/**
 * 知识库目录管理接口，Controller 只负责参数接收和调用 Service。
 */
@Validated
@RestController
@RequestMapping("/api/knowledge-directories")
public class KnowledgeDirectoryController {

    @Resource
    private IKnowledgeDirectoryService knowledgeDirectoryService;

    @PostMapping
    public Result<Long> createKnowledgeDirectory(@Valid @RequestBody KnowledgeDirectoryCreateRequest request) {
        return Result.success(knowledgeDirectoryService.createKnowledgeDirectory(request));
    }

    @PutMapping("/{id}")
    public Result<Void> updateKnowledgeDirectory(@PathVariable("id") @NotNull Long id,
                                                 @Valid @RequestBody KnowledgeDirectoryUpdateRequest request) {
        request.setId(id);
        knowledgeDirectoryService.updateKnowledgeDirectory(request);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteKnowledgeDirectory(@PathVariable("id") @NotNull Long id) {
        knowledgeDirectoryService.deleteKnowledgeDirectory(id);
        return Result.success(null);
    }

    @GetMapping("/tree")
    public Result<List<KnowledgeDirectoryResponse>> treeKnowledgeDirectory(@Valid KnowledgeDirectoryTreeRequest request) {
        return Result.success(knowledgeDirectoryService.treeKnowledgeDirectory(request));
    }

    @PutMapping("/{id}/enable")
    public Result<Void> enableKnowledgeDirectory(@PathVariable("id") @NotNull Long id) {
        knowledgeDirectoryService.enableKnowledgeDirectory(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/disable")
    public Result<Void> disableKnowledgeDirectory(@PathVariable("id") @NotNull Long id) {
        knowledgeDirectoryService.disableKnowledgeDirectory(id);
        return Result.success(null);
    }
}
