package com.aistudio.system.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发布或下架提交请求。
 */
@Data
public class PublishSubmitRequest {

    @NotNull(message = "文档 ID 不能为空")
    private Long documentId;
}
