package com.aistudio.system.query;

import lombok.Builder;
import lombok.Data;

/**
 * 知识文档分页查询条件，供 Mapper 使用。
 */
@Data
@Builder
public class KnowledgeDocumentPageQuery {

    private Long knowledgeBaseId;

    private Long directoryId;

    private String name;

    private String parseStatus;

    private String publishStatus;

    private Long pageNo;

    private Long pageSize;
}
