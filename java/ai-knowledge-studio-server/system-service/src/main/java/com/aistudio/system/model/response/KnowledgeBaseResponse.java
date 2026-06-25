package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库接口响应对象，供前端展示使用。
 */
@Data
@Builder
public class KnowledgeBaseResponse {

    private Long id;

    private String name;

    private String description;

    private Integer status;

    private String statusName;

    private Integer publishedStatus;

    private String publishedStatusName;

    private Integer documentCount;

    private Integer chunkCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
