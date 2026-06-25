package com.aistudio.foundation.domain;

/**
 * 平台统一错误码定义，业务模块扩展错误码时必须保持编码唯一。
 */
public enum ErrorCode {

    SUCCESS("000000", "处理成功"),
    PARAM_ERROR("A00001", "请求参数错误"),
    BUSINESS_ERROR("B00001", "业务处理失败"),
    SYSTEM_ERROR("C00001", "系统异常");

    private final String code;

    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
