package com.aistudio.system.convert;

import com.aistudio.system.model.request.DashboardTodoPageRequest;
import com.aistudio.system.query.DashboardTodoPageQuery;
import org.springframework.stereotype.Component;

/**
 * 工作台对象转换器，集中处理 Request 到 Query 的转换。
 */
@Component
public class DashboardConvert {

    public DashboardTodoPageQuery toTodoPageQuery(DashboardTodoPageRequest request) {
        return DashboardTodoPageQuery.builder()
                .pageNo(request.getPageNo())
                .pageSize(request.getPageSize())
                .documentName(request.getDocumentName())
                .parseStatus(request.getParseStatus())
                .reviewStatus(request.getReviewStatus())
                .publishStatus(request.getPublishStatus())
                .createStartTime(request.getCreateStartTime())
                .createEndTime(request.getCreateEndTime())
                .build();
    }
}
