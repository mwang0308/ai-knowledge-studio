package com.aistudio.system.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 召回测试请求。
 */
@Data
public class RetrievalTestRequest {

    @NotNull(message = "知识库 ID 不能为空")
    private Long knowledgeBaseId;

    private Long directoryId;

    private Long documentId;

    @NotBlank(message = "测试问题不能为空")
    private String queryText;

    private String testScope = "KNOWLEDGE_BASE";

    private Integer topK = 5;
}
