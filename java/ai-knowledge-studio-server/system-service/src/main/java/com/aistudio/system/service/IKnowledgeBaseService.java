package com.aistudio.system.service;

import com.aistudio.foundation.domain.PageResult;
import com.aistudio.system.model.request.KnowledgeBaseCreateRequest;
import com.aistudio.system.model.request.KnowledgeBasePageRequest;
import com.aistudio.system.model.request.KnowledgeBaseUpdateRequest;
import com.aistudio.system.model.response.KnowledgeBaseResponse;

/**
 * 知识库服务接口，定义知识库基础维护能力。
 */
public interface IKnowledgeBaseService {

    Long createKnowledgeBase(KnowledgeBaseCreateRequest request);

    void updateKnowledgeBase(KnowledgeBaseUpdateRequest request);

    KnowledgeBaseResponse getKnowledgeBase(Long id);

    PageResult<KnowledgeBaseResponse> pageKnowledgeBase(KnowledgeBasePageRequest request);

    void enableKnowledgeBase(Long id);

    void disableKnowledgeBase(Long id);

    void deleteKnowledgeBase(Long id);
}
