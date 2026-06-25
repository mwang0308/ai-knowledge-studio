package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 召回测试响应。
 */
@Data
@Builder
public class RetrievalTestResponse {

    private Long testId;
    private Long knowledgeBaseId;
    private Long directoryId;
    private Long documentId;
    private String queryText;
    private String testScope;
    private Integer topK;
    private Double topScore;
    private Long latencyMs;
    private List<RetrievalHitResponse> hits;
}
