package com.riverflow.gateway.handler;

import com.alibaba.fastjson2.JSON;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 网关统一异常处理：输出与 riverflow-admin 兼容的 R 格式，避免网关内部错误格式直接外抛
 */
@Slf4j
@Order(-2)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        int status;
        String msg;
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = rse.getStatus().value();
            msg = status == 404 ? "请求的资源不存在"
                    : (rse.getReason() == null ? rse.getStatus().getReasonPhrase() : rse.getReason());
        } else if (ex instanceof NotFoundException || ex instanceof ConnectException
                || ex.getCause() instanceof ConnectException || ex instanceof TimeoutException) {
            // 目标服务不可达 / 无可用实例 / 转发超时
            status = 503;
            msg = "下游服务暂不可用，请稍后重试";
        } else {
            status = 500;
            msg = "网关内部错误";
        }

        log.error("[Gateway] {} {} 处理异常 ({}): {}",
                exchange.getRequest().getMethodValue(), exchange.getRequest().getURI().getRawPath(),
                status, ex.getMessage());

        response.setStatusCode(HttpStatus.valueOf(status));
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        byte[] bytes = JSON.toJSONString(R.fail(status, msg)).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
