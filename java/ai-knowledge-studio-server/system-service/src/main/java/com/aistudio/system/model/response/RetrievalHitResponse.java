package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

/**
 * 召回测试命中结果。
 */
@Data
@Builder
public class RetrievalHitResponse {

    private Integer rankNo;
    private String chunkId;
    private Long documentId;
    private String titlePath;
    private String contentPreview;
    private Integer pageStart;
    private Integer pageEnd;
    private Double score;
    private String publishStatus;
    private Integer enabled;
}
