package com.aistudio.system.query;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库目录树查询对象，由 Request 转换而来，Mapper 只接收 Query。
 */
@Data
@Builder
public class KnowledgeDirectoryTreeQuery {

    private Long knowledgeBaseId;

    private String description;

    private Integer status;

    private LocalDateTime createStartTime;

    private LocalDateTime createEndTime;
}
