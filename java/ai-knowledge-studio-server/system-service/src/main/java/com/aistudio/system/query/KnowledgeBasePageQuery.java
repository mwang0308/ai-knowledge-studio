package com.aistudio.system.query;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库分页查询对象，由 Request 转换而来，Mapper 只接收 Query。
 */
@Data
@Builder
public class KnowledgeBasePageQuery {

    private Long pageNo;

    private Long pageSize;

    private String name;

    private Integer status;

    private LocalDateTime createStartTime;

    private LocalDateTime createEndTime;
}
