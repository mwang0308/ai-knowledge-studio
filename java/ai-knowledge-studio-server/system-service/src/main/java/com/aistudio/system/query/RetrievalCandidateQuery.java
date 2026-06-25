package com.aistudio.system.query;

import lombok.Builder;
import lombok.Data;

/**
 * 召回测试候选分片查询条件。
 */
@Data
@Builder
public class RetrievalCandidateQuery {

    private Long knowledgeBaseId;
    private Long directoryId;
    private Long documentId;
    private String testScope;
    private Integer limitSize;
}
