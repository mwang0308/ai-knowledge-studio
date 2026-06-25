package com.aistudio.system.enums;

/**
 * 文档审核状态。
 */
public enum DocumentReviewStatusEnum {

    NOT_SUBMITTED("NOT_SUBMITTED", "未提交"),
    WAIT_AUDIT("WAIT_AUDIT", "待审核"),
    AUDIT_PASSED("AUDIT_PASSED", "审核通过"),
    AUDIT_REJECTED("AUDIT_REJECTED", "审核驳回");

    private final String code;

    private final String description;

    DocumentReviewStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }
}
