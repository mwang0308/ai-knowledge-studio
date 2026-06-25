package com.aistudio.system.enums;

/**
 * 文档发布状态。
 */
public enum DocumentPublishStatusEnum {

    UNPUBLISHED("UNPUBLISHED", "未发布"),
    PUBLISHING("PUBLISHING", "发布中"),
    PUBLISHED("PUBLISHED", "已发布"),
    OFFLINE("OFFLINE", "已下线");

    private final String code;

    private final String description;

    DocumentPublishStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }
}
