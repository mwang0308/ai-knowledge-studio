package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档处理任务进度响应对象。
 */
@Data
@Builder
public class KnowledgeTaskProgressResponse {

    private String taskId;

    private String taskNo;

    private String documentId;

    private String versionId;

    private String stageCode;

    private String taskStatus;

    private Integer progress;

    private String errorCode;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    private LocalDateTime updateTime;
}
