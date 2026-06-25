package com.aistudio.system.enums;

/**
 * 文档解析状态。
 */
public enum DocumentParseStatusEnum {

    UPLOADED("UPLOADED", "已上传"),
    PARSE_CHUNKING("PARSE_CHUNKING", "解析分片中"),
    PARSE_CHUNKED("PARSE_CHUNKED", "解析分片完成"),
    PROCESS_FAILED("PROCESS_FAILED", "处理失败");

    private final String code;

    private final String description;

    DocumentParseStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
