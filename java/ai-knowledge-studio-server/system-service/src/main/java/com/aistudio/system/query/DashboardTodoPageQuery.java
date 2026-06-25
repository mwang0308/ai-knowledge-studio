package com.aistudio.system.query;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作台待办分页查询条件，供 Mapper 使用。
 */
@Data
@Builder
public class DashboardTodoPageQuery {

    private Long pageNo;

    private Long pageSize;

    private String documentName;

    private String parseStatus;

    private String reviewStatus;

    private String publishStatus;

    private LocalDateTime createStartTime;

    private LocalDateTime createEndTime;
}
