package com.aistudio.system.model.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档上传请求对象，只用于接口入参和 Service 编排，不进入 Mapper。
 */
@Data
@Builder
public class KnowledgeDocumentUploadRequest {

    private Long knowledgeBaseId;

    private Long directoryId;

    private MultipartFile file;
}
