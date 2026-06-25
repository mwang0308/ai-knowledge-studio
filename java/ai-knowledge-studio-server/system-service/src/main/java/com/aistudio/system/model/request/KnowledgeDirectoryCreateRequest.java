package com.aistudio.system.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建知识库目录请求对象，只用于接口入参。
 */
@Data
public class KnowledgeDirectoryCreateRequest {

    @NotNull(message = "知识库 ID 不能为空")
    private Long knowledgeBaseId;

    private Long parentId;

    @NotBlank(message = "目录名称不能为空")
    @Size(max = 128, message = "目录名称不能超过 128 个字符")
    private String name;

    @Size(max = 512, message = "目录描述不能超过 512 个字符")
    private String description;

    private Integer sortOrder = 0;
}
