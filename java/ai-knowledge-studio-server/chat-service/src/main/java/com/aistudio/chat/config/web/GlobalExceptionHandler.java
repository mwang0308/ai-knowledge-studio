package com.aistudio.chat.config.web;

import com.aistudio.foundation.domain.ErrorCode;
import com.aistudio.foundation.domain.Result;
import com.aistudio.foundation.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 聊天服务统一异常处理器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidateException(Exception exception) {
        log.warn("请求参数校验失败", exception);
        return Result.fail(ErrorCode.PARAM_ERROR, ErrorCode.PARAM_ERROR.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        log.warn("业务处理失败，errorCode={}", exception.getErrorCode().getCode(), exception);
        return Result.fail(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        log.error("系统异常", exception);
        return Result.fail(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage());
    }
}
