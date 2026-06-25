package com.aistudio.system.enums;

/**
 * 文档处理任务状态。
 */
public enum ProcessTaskStatusEnum {

    PENDING("PENDING", "待处理"),
    QUEUED("QUEUED", "已入队"),
    RUNNING("RUNNING", "处理中"),
    SUCCESS("SUCCESS", "处理成功"),
    FAILED("FAILED", "处理失败");

    private final String code;

    private final String description;

    ProcessTaskStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }
}
