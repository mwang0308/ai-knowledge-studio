package com.aistudio.system.mq;

import lombok.Builder;
import lombok.Data;

/**
 * Java 投递给 Python document-process-service 的文档处理任务消息。
 */
@Data
@Builder
public class DocumentProcessMessage {

    private String messageId;

    private String taskId;

    private String taskNo;

    private String stageCode;

    private Long knowledgeBaseId;

    private Long directoryId;

    private String documentId;

    private String versionId;

    private Long fileResourceId;

    private String fileName;

    private String fileType;

    private String bucketName;

    private String objectKey;

    private String chunkConfigSnapshot;

    private String callbackUrl;

    private Integer retryCount;

    private String traceId;

    private String createdTime;
}
