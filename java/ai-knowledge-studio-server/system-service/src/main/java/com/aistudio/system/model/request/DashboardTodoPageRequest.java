package com.aistudio.system.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 工作台待办分页查询请求对象，只用于接口入参。
 */
@Data
public class DashboardTodoPageRequest {

    @Min(value = 1, message = "页码不能小于 1")
    private Long pageNo = 1L;

    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Long pageSize = 10L;

    private String documentName;

    private String parseStatus;

    private String reviewStatus;

    private String publishStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createStartTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createEndTime;
}
