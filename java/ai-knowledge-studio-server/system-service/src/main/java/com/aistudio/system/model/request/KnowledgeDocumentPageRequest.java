package com.aistudio.system.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 知识文档分页查询请求，只用于接口层入参。
 */
@Data
public class KnowledgeDocumentPageRequest {

    private Long knowledgeBaseId;

    private Long directoryId;

    private String name;

    private String parseStatus;

    private String publishStatus;

    @NotNull(message = "页码不能为空")
    private Long pageNo = 1L;

    @NotNull(message = "每页条数不能为空")
    private Long pageSize = 10L;
}
