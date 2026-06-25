package com.aistudio.system.controller;

import com.aistudio.foundation.domain.PageResult;
import com.aistudio.foundation.domain.Result;
import com.aistudio.system.model.request.DashboardTodoPageRequest;
import com.aistudio.system.model.response.DashboardSummaryResponse;
import com.aistudio.system.model.response.DashboardTodoResponse;
import com.aistudio.system.service.IDashboardService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识治理工作台接口。
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Resource
    private IDashboardService dashboardService;

    @GetMapping("/summary")
    public Result<DashboardSummaryResponse> getSummary() {
        return Result.success(dashboardService.getSummary());
    }

    @GetMapping("/todos/page")
    public Result<PageResult<DashboardTodoResponse>> pageTodos(@Valid DashboardTodoPageRequest request) {
        return Result.success(dashboardService.pageTodos(request));
    }
}
