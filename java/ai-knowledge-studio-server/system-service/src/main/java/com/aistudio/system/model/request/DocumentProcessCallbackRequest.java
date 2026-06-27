package com.aistudio.system.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Python 文档处理服务回调请求，只用于内部接口层入参。
 */
@Data
public class DocumentProcessCallbackRequest {

    @NotNull(message = "任务 ID 不能为空")
    private String taskId;

    private String documentId;

    @NotBlank(message = "阶段编码不能为空")
    private String stageCode;

    @NotBlank(message = "处理状态不能为空")
    private String status;

    private Integer progress;

    private DocumentProcessCallbackResult result;

    private String message;

    private String errorCode;

    private String errorMessage;

    private String traceId;

    private LocalDateTime finishedTime;
}
