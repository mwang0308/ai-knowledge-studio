package com.aistudio.system.enums;

/**
 * 文档索引状态。
 */
public enum DocumentIndexStatusEnum {

    WAIT_INDEX("WAIT_INDEX", "待索引"),
    INDEXING("INDEXING", "索引中"),
    INDEXED("INDEXED", "索引完成"),
    INDEX_FAILED("INDEX_FAILED", "索引失败");

    private final String code;

    private final String description;

    DocumentIndexStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }
}
