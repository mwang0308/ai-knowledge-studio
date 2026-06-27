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
import com.aistudio.system.model.response.KnowledgeDocumentStructureResponse;
import com.aistudio.system.model.response.KnowledgeDocumentUploadResponse;
import com.aistudio.system.model.response.KnowledgeTaskProgressResponse;
import com.aistudio.system.model.response.RetrievalTestResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识文档服务接口，定义文档上传和任务创建能力。
 */
public interface IKnowledgeDocumentService {

    KnowledgeDocumentUploadResponse uploadDocument(KnowledgeDocumentUploadRequest request);

    KnowledgeDocumentUploadResponse reuploadDocument(String documentId, MultipartFile file);

    KnowledgeDocumentUploadResponse reprocessDocument(String documentId);

    void deleteDocument(String documentId);

    PageResult<KnowledgeDocumentResponse> pageDocument(KnowledgeDocumentPageRequest request);

    KnowledgeTaskProgressResponse getTaskProgress(String taskId);

    KnowledgeDocumentStructureResponse getDocumentStructure(String documentId);

    void handleProcessCallback(DocumentProcessCallbackRequest request);

    PageResult<KnowledgeChunkResponse> pageChunk(KnowledgeChunkPageRequest request);

    RetrievalTestResponse testRetrieval(RetrievalTestRequest request);

    void passReview(ReviewSubmitRequest request);

    void rejectReview(ReviewSubmitRequest request);

    void publishDocument(PublishSubmitRequest request);

    void offlineDocument(PublishSubmitRequest request);
}
