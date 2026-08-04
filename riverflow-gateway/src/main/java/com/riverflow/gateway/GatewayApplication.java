package com.riverflow.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RiverFlow 统一 API 网关
 *
 * <p>基于 Spring Cloud Gateway（WebFlux），路由全部从 MySQL gateway_route 表动态加载，
 * 不配置 spring.cloud.gateway.routes。变更路由后调用 /gateway/route/refresh 热生效。</p>
 */
@SpringBootApplication
@MapperScan("com.riverflow.gateway.route")
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
