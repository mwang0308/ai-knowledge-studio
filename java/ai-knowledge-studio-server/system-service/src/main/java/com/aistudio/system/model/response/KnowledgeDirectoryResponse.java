package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库目录接口响应对象，支持树形结构返回。
 */
@Data
@Builder
public class KnowledgeDirectoryResponse {

    private Long id;

    private Long knowledgeBaseId;

    private Long parentId;

    private String name;

    private String description;

    private String path;

    private Integer level;

    private Integer sortOrder;

    private Integer status;

    private String statusName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<KnowledgeDirectoryResponse> children;
}
