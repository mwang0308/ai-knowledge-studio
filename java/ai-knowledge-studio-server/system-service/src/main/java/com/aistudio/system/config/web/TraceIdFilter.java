package com.aistudio.system.config.web;

import com.aistudio.foundation.constant.TraceConstant;
import com.aistudio.foundation.context.TraceContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * 系统服务 traceId 过滤器。
 */
@Component
public class TraceIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String traceId = httpRequest.getHeader(TraceConstant.TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        try {
            TraceContext.setTraceId(traceId);
            MDC.put(TraceConstant.TRACE_ID_MDC_KEY, traceId);
            httpResponse.setHeader(TraceConstant.TRACE_ID_HEADER, traceId);
            chain.doFilter(request, response);
        } finally {
            TraceContext.clear();
            MDC.remove(TraceConstant.TRACE_ID_MDC_KEY);
        }
    }
}
