-- =====================================================================
-- RiverFlow 网关动态路由表（Spring Cloud Gateway / riverflow-gateway）
--
-- 说明：
--   1. 路由不写死在网关 application.yml，全部从本表加载；
--   2. 变更数据后调用 POST /gateway/route/refresh 即可热生效，无需重启网关；
--   3. uri 支持 ${ENV:default} 占位符，由网关启动/刷新时按环境变量解析。
-- =====================================================================

CREATE TABLE IF NOT EXISTS `gateway_route` (
    `id`            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `route_id`      VARCHAR(128) NOT NULL COMMENT '路由 ID，对应 Spring Cloud Gateway route id',
    `uri`           VARCHAR(255) NOT NULL COMMENT '目标服务地址，支持 ${ENV:default} 占位符',
    `predicates`    JSON         NOT NULL COMMENT '断言配置数组，如 Path、Header 等',
    `filters`       JSON                  COMMENT '过滤器配置数组，如 StripPrefix、RequestRateLimiter 等',
    `route_order`   INT          DEFAULT 0 COMMENT '优先级，数字越小越优先',
    `enabled`       TINYINT(1)   DEFAULT 1 COMMENT '是否启用：1 启用，0 禁用',
    `description`   VARCHAR(255)          COMMENT '路由说明',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_route_id` (`route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网关动态路由表';

-- 管理后台 API（规范前缀 /admin，剥一层前缀后转发到 admin 服务）
INSERT IGNORE INTO `gateway_route` (`route_id`, `uri`, `predicates`, `filters`, `route_order`, `enabled`, `description`)
VALUES (
    'admin-service',
    '${RIVERFLOW_ADMIN_URL:http://localhost:8080}',
    '[{"name":"Path","args":{"_genkey_0":"/admin/**"}}]',
    '[{"name":"StripPrefix","args":{"_genkey_0":"1"}}]',
    1, 1, '管理后台 API 路由（规范前缀 /admin）'
);

-- 管理后台 API（兼容现有前端 /api 前缀，行为与原 Nginx 直连一致）
INSERT IGNORE INTO `gateway_route` (`route_id`, `uri`, `predicates`, `filters`, `route_order`, `enabled`, `description`)
VALUES (
    'admin-api-compat',
    '${RIVERFLOW_ADMIN_URL:http://localhost:8080}',
    '[{"name":"Path","args":{"_genkey_0":"/api/**"}}]',
    '[{"name":"StripPrefix","args":{"_genkey_0":"1"}}]',
    2, 1, '管理后台 API 兼容路由（现有前端 /api 前缀，待前端切换到 /admin 后可下线）'
);

-- 对外开放 API（admin 侧控制器本身位于 /open 下，不剥前缀；由 admin OpenApiAuthFilter 做 AppKey/签名认证）
INSERT IGNORE INTO `gateway_route` (`route_id`, `uri`, `predicates`, `filters`, `route_order`, `enabled`, `description`)
VALUES (
    'open-service',
    '${RIVERFLOW_ADMIN_URL:http://localhost:8080}',
    '[{"name":"Path","args":{"_genkey_0":"/open/**"}}]',
    NULL,
    3, 1, '对外开放 API 路由'
);

-- Knife4j 在线文档（不剥前缀；生产环境建议将 enabled 置 0 或仅内网开放）
INSERT IGNORE INTO `gateway_route` (`route_id`, `uri`, `predicates`, `filters`, `route_order`, `enabled`, `description`)
VALUES (
    'admin-docs',
    '${RIVERFLOW_ADMIN_URL:http://localhost:8080}',
    '[{"name":"Path","args":{"_genkey_0":"/doc.html","_genkey_1":"/webjars/**","_genkey_2":"/swagger-resources/**","_genkey_3":"/v3/api-docs/**"}}]',
    NULL,
    4, 1, 'Knife4j API 文档路由（建议仅内网/开发环境启用）'
);
