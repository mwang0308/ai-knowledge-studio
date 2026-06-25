package com.aistudio.system.service;

import com.aistudio.system.model.request.KnowledgeDirectoryCreateRequest;
import com.aistudio.system.model.request.KnowledgeDirectoryTreeRequest;
import com.aistudio.system.model.request.KnowledgeDirectoryUpdateRequest;
import com.aistudio.system.model.response.KnowledgeDirectoryResponse;

import java.util.List;

/**
 * 知识库目录服务接口，定义目录创建、编辑、删除和树查询能力。
 */
public interface IKnowledgeDirectoryService {

    Long createKnowledgeDirectory(KnowledgeDirectoryCreateRequest request);

    void updateKnowledgeDirectory(KnowledgeDirectoryUpdateRequest request);

    void deleteKnowledgeDirectory(Long id);

    List<KnowledgeDirectoryResponse> treeKnowledgeDirectory(KnowledgeDirectoryTreeRequest request);

    void enableKnowledgeDirectory(Long id);

    void disableKnowledgeDirectory(Long id);
}
