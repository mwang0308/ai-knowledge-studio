package com.aistudio.system.service;

import com.aistudio.foundation.domain.PageResult;
import com.aistudio.system.model.request.DashboardTodoPageRequest;
import com.aistudio.system.model.response.DashboardSummaryResponse;
import com.aistudio.system.model.response.DashboardTodoResponse;

/**
 * 工作台统计服务接口。
 */
public interface IDashboardService {

    DashboardSummaryResponse getSummary();

    PageResult<DashboardTodoResponse> pageTodos(DashboardTodoPageRequest request);
}
