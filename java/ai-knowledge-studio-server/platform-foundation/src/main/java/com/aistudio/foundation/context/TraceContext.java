package com.aistudio.foundation.context;

import java.util.Optional;

/**
 * 当前线程 traceId 上下文，供日志、MQ 和远程调用透传使用。
 */
public final class TraceContext {

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    private TraceContext() {
    }

    public static void setTraceId(String traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }

    public static Optional<String> getTraceId() {
        return Optional.ofNullable(TRACE_ID_HOLDER.get());
    }

    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }
}
