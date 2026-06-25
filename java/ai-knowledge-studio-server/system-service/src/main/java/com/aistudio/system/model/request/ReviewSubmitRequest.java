package com.aistudio.system.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核提交请求。
 */
@Data
public class ReviewSubmitRequest {

    @NotNull(message = "文档 ID 不能为空")
    private Long documentId;

    private String reviewComment;
}
