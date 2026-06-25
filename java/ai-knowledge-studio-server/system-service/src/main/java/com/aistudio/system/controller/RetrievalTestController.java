package com.aistudio.system.controller;

import com.aistudio.foundation.domain.Result;
import com.aistudio.system.model.request.RetrievalTestRequest;
import com.aistudio.system.model.response.RetrievalTestResponse;
import com.aistudio.system.service.IKnowledgeDocumentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 召回测试接口，入口在 system-service，后续可替换为调用 ai-agent-service。
 */
@Validated
@RestController
@RequestMapping("/api/retrieval-tests")
public class RetrievalTestController {

    @Resource
    private IKnowledgeDocumentService knowledgeDocumentService;

    @PostMapping
    public Result<RetrievalTestResponse> testRetrieval(@RequestBody @Valid RetrievalTestRequest request) {
        return Result.success(knowledgeDocumentService.testRetrieval(request));
    }
}
