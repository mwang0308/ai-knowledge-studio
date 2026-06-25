package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作台待办响应对象。
 */
@Data
@Builder
public class DashboardTodoResponse {

    private Long documentId;

    private String documentName;

    private Long knowledgeBaseId;

    private Long directoryId;

    private String currentStatus;

    private String parseStatus;

    private String reviewStatus;

    private String publishStatus;

    private String nextAction;

    private String actionPath;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
