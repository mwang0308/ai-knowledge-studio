package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

/**
 * 文档上传响应对象，返回文档、版本、任务和 MQ 投递结果。
 */
@Data
@Builder
public class KnowledgeDocumentUploadResponse {

    private Long fileResourceId;

    private Long documentId;

    private Long versionId;

    private Long taskId;

    private String taskNo;

    private String mqMessageId;

    private String fileHash;

    private String parseStatus;

    private String taskStatus;
}
