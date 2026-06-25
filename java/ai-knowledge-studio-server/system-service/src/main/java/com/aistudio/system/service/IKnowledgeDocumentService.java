package com.aistudio.system.service;

import com.aistudio.foundation.domain.PageResult;
import com.aistudio.system.model.request.DocumentProcessCallbackRequest;
import com.aistudio.system.model.request.KnowledgeChunkPageRequest;
import com.aistudio.system.model.request.KnowledgeDocumentPageRequest;
import com.aistudio.system.model.request.KnowledgeDocumentUploadRequest;
import com.aistudio.system.model.request.PublishSubmitRequest;
import com.aistudio.system.model.request.RetrievalTestRequest;
import com.aistudio.system.model.request.ReviewSubmitRequest;
import com.aistudio.system.model.response.KnowledgeChunkResponse;
import com.aistudio.system.model.response.KnowledgeDocumentResponse;
import com.aistudio.system.model.response.KnowledgeDocumentUploadResponse;
import com.aistudio.system.model.response.KnowledgeTaskProgressResponse;
import com.aistudio.system.model.response.RetrievalTestResponse;

/**
 * 知识文档服务接口，定义文档上传和任务创建能力。
 */
public interface IKnowledgeDocumentService {

    KnowledgeDocumentUploadResponse uploadDocument(KnowledgeDocumentUploadRequest request);

    PageResult<KnowledgeDocumentResponse> pageDocument(KnowledgeDocumentPageRequest request);

    KnowledgeTaskProgressResponse getTaskProgress(Long taskId);

    void handleProcessCallback(DocumentProcessCallbackRequest request);

    PageResult<KnowledgeChunkResponse> pageChunk(KnowledgeChunkPageRequest request);

    RetrievalTestResponse testRetrieval(RetrievalTestRequest request);

    void passReview(ReviewSubmitRequest request);

    void rejectReview(ReviewSubmitRequest request);

    void publishDocument(PublishSubmitRequest request);

    void offlineDocument(PublishSubmitRequest request);
}
