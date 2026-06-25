package com.aistudio.foundation.exception;

import com.aistudio.foundation.domain.ErrorCode;

/**
 * 业务异常。业务校验失败时抛出该异常，由全局异常处理器统一转换为接口返回。
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(String message) {
        this(ErrorCode.BUSINESS_ERROR, message);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
