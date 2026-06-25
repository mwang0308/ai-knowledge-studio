package com.aistudio.system.enums;

/**
 * 文档处理任务阶段。
 */
public enum ProcessTaskStageEnum {

    DOCUMENT_PARSE_CHUNK("DOCUMENT_PARSE_CHUNK", "文档解析与分片"),
    DOCUMENT_EMBEDDING_INDEX("DOCUMENT_EMBEDDING_INDEX", "向量化与索引");

    private final String code;

    private final String description;

    ProcessTaskStageEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }
}
