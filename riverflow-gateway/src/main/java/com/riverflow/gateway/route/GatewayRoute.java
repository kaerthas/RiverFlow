package com.riverflow.gateway.route;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网关动态路由实体，对应 gateway_route 表
 *
 * <p>predicates / filters 为 JSON 数组，结构与 Spring Cloud Gateway 的
 * PredicateDefinition / FilterDefinition 一一对应，例如：
 * [{"name":"Path","args":{"_genkey_0":"/admin/**"}}]</p>
 */
@Data
@TableName("gateway_route")
public class GatewayRoute {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 路由 ID，对应 Spring Cloud Gateway route id
     */
    private String routeId;

    /**
     * 目标服务地址，支持 ${ENV:default} 占位符，如 ${RIVERFLOW_ADMIN_URL:http://localhost:8080}
     */
    private String uri;

    /**
     * 断言配置数组（JSON）
     */
    private String predicates;

    /**
     * 过滤器配置数组（JSON）
     */
    private String filters;

    /**
     * 优先级，数字越小越优先
     */
    private Integer routeOrder;

    /**
     * 是否启用：1 启用，0 禁用
     */
    private Integer enabled;

    /**
     * 路由说明
     */
    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
