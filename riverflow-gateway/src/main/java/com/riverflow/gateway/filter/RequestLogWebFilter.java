package com.riverflow.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 请求日志过滤器（WebFilter）：记录所有进入网关的请求——方法、路径、客户端 IP、
 * 状态码与耗时，覆盖路由转发请求与本地管理端点，用于审计与排障
 */
@Slf4j
@Component
public class RequestLogWebFilter implements WebFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethodValue();
        String path = request.getURI().getRawPath();
        String query = request.getURI().getRawQuery();
        String clientIp = request.getRemoteAddress() == null
                ? "-" : request.getRemoteAddress().getAddress().getHostAddress();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long cost = System.currentTimeMillis() - start;
            HttpStatus status = exchange.getResponse().getStatusCode();
            log.info("[Gateway] {} {}{} from {} -> {} ({}ms)",
                    method, path, query == null ? "" : "?" + query,
                    clientIp, status == null ? 200 : status.value(), cost);
        }));
    }

    @Override
    public int getOrder() {
        // 最先进入、最后记录，保证耗时统计覆盖完整链路
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
