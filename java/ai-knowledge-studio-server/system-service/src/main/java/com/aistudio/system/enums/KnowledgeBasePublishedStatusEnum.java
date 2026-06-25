package com.aistudio.system.enums;

/**
 * 知识库发布状态。
 */
public enum KnowledgeBasePublishedStatusEnum {

    UNPUBLISHED(0, "未发布"),
    PUBLISHED(1, "已发布");

    private final Integer code;

    private final String description;

    KnowledgeBasePublishedStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static String descriptionOf(Integer code) {
        for (KnowledgeBasePublishedStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item.description;
            }
        }
        return "未知";
    }
}
