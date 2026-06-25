package com.aistudio.system.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 工作台统计响应对象。
 */
@Data
@Builder
public class DashboardSummaryResponse {

    private Long knowledgeBaseCount;

    private Long documentCount;

    private Long waitAuditCount;

    private Long processFailedCount;

    private Long publishedCount;

    private Long chunkCount;

    private List<DashboardTodoResponse> todos;
}
