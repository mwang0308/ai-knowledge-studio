package com.aistudio.gateway.filter;

import com.aistudio.foundation.constant.TraceConstant;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 网关 traceId 过滤器，统一生成并向下游服务透传链路标识。
 */
@Slf4j
@Component
public class GatewayTraceIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TraceConstant.TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(TraceConstant.TRACE_ID_HEADER, traceId)
                .build();
        exchange.getResponse().getHeaders().set(TraceConstant.TRACE_ID_HEADER, traceId);

        MDC.put(TraceConstant.TRACE_ID_MDC_KEY, traceId);
        log.debug("网关请求开始，path={}", request.getPath());
        return chain.filter(exchange.mutate().request(request).build())
                .doFinally(signalType -> MDC.remove(TraceConstant.TRACE_ID_MDC_KEY));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
