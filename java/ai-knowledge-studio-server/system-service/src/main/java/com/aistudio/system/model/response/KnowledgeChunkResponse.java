package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识分片响应对象。
 */
@Data
@Builder
public class KnowledgeChunkResponse {

    private Long id;
    private String chunkId;
    private Long knowledgeBaseId;
    private Long directoryId;
    private Long documentId;
    private Long documentVersionId;
    private Integer chunkNo;
    private String titlePath;
    private String contentPreview;
    private Integer tokenCount;
    private Integer charCount;
    private Integer pageStart;
    private Integer pageEnd;
    private List<String> blockIds;
    private String publishStatus;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
