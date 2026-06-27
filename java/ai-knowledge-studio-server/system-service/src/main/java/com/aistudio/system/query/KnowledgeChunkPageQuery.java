package com.aistudio.system.query;

import lombok.Builder;
import lombok.Data;

/**
 * 知识分片分页查询条件。
 */
@Data
@Builder
public class KnowledgeChunkPageQuery {

    private Long knowledgeBaseId;
    private Long directoryId;
    private String documentId;
    private String versionId;
    private String publishStatus;
    private Integer enabled;
    private Long pageNo;
    private Long pageSize;
}
