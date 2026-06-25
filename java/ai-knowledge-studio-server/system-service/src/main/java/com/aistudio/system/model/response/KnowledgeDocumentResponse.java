package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文档列表响应对象。
 */
@Data
@Builder
public class KnowledgeDocumentResponse {

    private Long id;

    private Long knowledgeBaseId;

    private Long directoryId;

    private String name;

    private Long currentVersionId;

    private String fileName;

    private String fileExt;

    private Long fileSize;

    private String parseStatus;

    private String indexStatus;

    private String reviewStatus;

    private String publishStatus;

    private Integer chunkCount;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
