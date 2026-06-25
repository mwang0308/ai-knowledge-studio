package com.aistudio.foundation.domain;

import com.aistudio.foundation.context.TraceContext;

/**
 * 统一接口返回结构，所有 Controller 对外响应都应使用该对象包裹。
 *
 * @param code 错误码
 * @param message 返回消息
 * @param data 业务数据
 * @param traceId 链路追踪 ID
 * @param <T> 业务数据类型
 */
public record Result<T>(String code, String message, T data, String traceId) {

    public static <T> Result<T> success(T data) {
        return new Result<>(
                ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(),
                data,
                TraceContext.getTraceId().orElse(null)
        );
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(
                errorCode.getCode(),
                message,
                null,
                TraceContext.getTraceId().orElse(null)
        );
    }
}
