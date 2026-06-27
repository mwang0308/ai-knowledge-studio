package com.aistudio.system.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 知识分片分页请求。
 */
@Data
public class KnowledgeChunkPageRequest {

    private Long knowledgeBaseId;
    private Long directoryId;
    private String documentId;
    private String versionId;
    private String publishStatus;
    private Integer enabled;

    @NotNull(message = "页码不能为空")
    private Long pageNo = 1L;

    @NotNull(message = "每页条数不能为空")
    private Long pageSize = 10L;
}
