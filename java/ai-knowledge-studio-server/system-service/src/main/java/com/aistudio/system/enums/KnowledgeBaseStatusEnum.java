package com.aistudio.system.enums;

/**
 * 知识库启停状态。
 */
public enum KnowledgeBaseStatusEnum {

    DISABLED(0, "停用"),
    ENABLED(1, "启用");

    private final Integer code;

    private final String description;

    KnowledgeBaseStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static boolean contains(Integer code) {
        if (code == null) {
            return false;
        }
        for (KnowledgeBaseStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static String descriptionOf(Integer code) {
        for (KnowledgeBaseStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item.description;
            }
        }
        return "未知";
    }
}
