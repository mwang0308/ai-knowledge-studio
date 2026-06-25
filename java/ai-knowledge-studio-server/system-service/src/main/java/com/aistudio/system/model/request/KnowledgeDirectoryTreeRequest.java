package com.aistudio.system.model.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 知识库目录树查询请求对象，只用于接口入参。
 */
@Data
public class KnowledgeDirectoryTreeRequest {

    private Long knowledgeBaseId;

    private String description;

    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createStartTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createEndTime;
}
