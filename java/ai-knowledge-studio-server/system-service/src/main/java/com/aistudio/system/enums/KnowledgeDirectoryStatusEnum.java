package com.aistudio.system.enums;

/**
 * 知识库目录启停状态。
 */
public enum KnowledgeDirectoryStatusEnum {

    DISABLED(0, "停用"),
    ENABLED(1, "启用");

    private final Integer code;

    private final String description;

    KnowledgeDirectoryStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public static boolean contains(Integer code) {
        if (code == null) {
            return false;
        }
        for (KnowledgeDirectoryStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static String descriptionOf(Integer code) {
        for (KnowledgeDirectoryStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item.description;
            }
        }
        return "未知";
    }
}
