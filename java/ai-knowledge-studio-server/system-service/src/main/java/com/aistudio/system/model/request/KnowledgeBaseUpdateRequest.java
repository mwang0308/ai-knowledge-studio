package com.aistudio.system.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新知识库请求对象，只用于接口入参。
 */
@Data
public class KnowledgeBaseUpdateRequest {

    @NotNull(message = "知识库 ID 不能为空")
    private Long id;

    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 128, message = "知识库名称不能超过 128 个字符")
    private String name;

    @Size(max = 512, message = "知识库描述不能超过 512 个字符")
    private String description;
}
