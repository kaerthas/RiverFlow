-- ============================================================
-- 流程示例初始化脚本：批量同步 list 数据到 B 系统
-- 适用场景: 从源表取 list → foreach 循环 → 调用 B 系统接口 → 写入目标表
--
-- 使用说明:
-- 1. 请先确认 wf_flow_definition 中你的流程ID，替换下方 3001
-- 2. 按顺序执行本脚本
-- ============================================================

SET NAMES utf8mb4;

-- --------------------------------------------------
-- 1. 创建测试表
-- --------------------------------------------------
DROP TABLE IF EXISTS `example_order_source`;
CREATE TABLE `example_order_source` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
    `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名称',
    `amount` decimal(12, 2) NOT NULL COMMENT '金额',
    `status` tinyint(4) DEFAULT 0 COMMENT '状态：0-待同步 1-已同步',
    `create_time` datetime(0) DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '批量同步示例-源表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `example_order_target`;
CREATE TABLE `example_order_target` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
    `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名称',
    `amount` decimal(12, 2) NOT NULL COMMENT '金额',
    `source_id` bigint(20) NOT NULL COMMENT '来源表主键',
    `sync_time` datetime(0) DEFAULT CURRENT_TIMESTAMP(0) COMMENT '同步时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_order_no` (`order_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '批量同步示例-目标表（模拟B库）' ROW_FORMAT = Dynamic;

-- --------------------------------------------------
-- 2. 插入 100 条测试数据
-- --------------------------------------------------
INSERT INTO `example_order_source` (`order_no`, `user_name`, `amount`, `status`)
SELECT CONCAT('ORD', LPAD(`seq`, 6, '0')) AS `order_no`,
       CONCAT('用户', `seq`) AS `user_name`,
       ROUND(100 + RAND() * 900, 2) AS `amount`,
       0 AS `status`
FROM (
    SELECT `a`.`N` + `b`.`N` * 10 + `c`.`N` * 100 + 1 AS `seq`
    FROM (
        SELECT 0 AS N UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) `a`
    CROSS JOIN (
        SELECT 0 AS N UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) `b`
    CROSS JOIN (
        SELECT 0 AS N UNION ALL SELECT 1
    ) `c`
) `numbers`
WHERE `seq` <= 100;

-- --------------------------------------------------
-- 3. 注册 B 系统订单同步接口到 wf_api_catalog
-- --------------------------------------------------
INSERT INTO `wf_api_catalog` (`id`, `api_code`, `api_name`, `api_type`, `method`, `url`, `content_type`, `auth_type`, `timeout`, `retry_times`, `proxy_enabled`, `status`, `del_flag`)
VALUES (1000010, 'b-sys-order-sync', 'B系统-订单同步单条写入', 'proxy', 'POST', 'http://localhost:8080/example/b-sys/insertOrder', 'application/json', 'none', 30000, 0, 0, 1, 0)
ON DUPLICATE KEY UPDATE `api_name` = 'B系统-订单同步单条写入', `url` = 'http://localhost:8080/example/b-sys/insertOrder', `status` = 1;

-- --------------------------------------------------
-- 4. 接口参数定义
-- --------------------------------------------------
INSERT INTO `wf_api_param` (`id`, `api_id`, `param_type`, `parent_id`, `param_key`, `param_name`, `data_type`, `is_required`, `default_value`, `sort_no`, `del_flag`) VALUES
(100001000, 1000010, 'body', 0, 'orderNo', '订单编号', 'string', 1, NULL, 1, 0),
(100001001, 1000010, 'body', 0, 'userName', '用户名称', 'string', 1, NULL, 2, 0),
(100001002, 1000010, 'body', 0, 'amount', '金额', 'string', 1, NULL, 3, 0),
(100001003, 1000010, 'body', 0, 'sourceId', '来源主键', 'long', 1, NULL, 4, 0),
(100001004, 1000010, 'response', 0, 'id', '目标表主键', 'long', 0, NULL, 1, 0)
ON DUPLICATE KEY UPDATE `param_name` = VALUES(`param_name`), `data_type` = VALUES(`data_type`), `is_required` = VALUES(`is_required`);

-- --------------------------------------------------
-- 5. 流程定义（请根据实际 flow_id 修改）
-- --------------------------------------------------
INSERT INTO `wf_flow_definition` (`id`, `flow_code`, `flow_name`, `version`, `trigger_type`, `status`, `graph_json`, `create_time`, `update_time`, `create_by`, `del_flag`)
VALUES (3001, 'FLOW_BATCH_SYNC_DEMO', '批量同步list数据到B系统示例', 1, 'manual', 1, '{"nodes":[],"edges":[]}', NOW(), NOW(), 'admin', 0)
ON DUPLICATE KEY UPDATE `flow_name` = '批量同步list数据到B系统示例', `status` = 1;

-- --------------------------------------------------
-- 6. 流程节点
-- --------------------------------------------------
DELETE FROM `wf_flow_node` WHERE `flow_id` = 3001;

INSERT INTO `wf_flow_node` (`id`, `flow_id`, `node_id`, `node_name`, `node_type`, `config_json`, `input_mapping`, `output_mapping`, `timeout`, `retry_times`, `fail_strategy`, `sort_no`, `del_flag`) VALUES
(300101, 3001, 'start_1', '开始', 'start', '{}', NULL, NULL, NULL, NULL, NULL, 0, 0),
(300102, 3001, 'db_query_source', '查询待同步订单', 'db', '{"dsCode":"master","operation":"select","sql":"SELECT id, order_no, user_name, amount FROM example_order_source WHERE status = 0 ORDER BY id ASC LIMIT 100","resultVarName":"orderList"}', NULL, NULL, 30000, 0, 'suspend', 1, 0),
(300103, 3001, 'foreach_sync', '循环同步订单', 'foreach', '{"sourceExpr":"context.orderList","itemVar":"order","indexVar":"index","resultVar":"syncResults","maxIterations":100,"parallel":true,"parallelLimit":3}', NULL, NULL, 30000, 0, 'suspend', 2, 0),
(300104, 3001, 'api_call_b', '调用B系统写入', 'api', '{"apiCode":"b-sys-order-sync","timeout":30000,"failStrategy":"suspend"}', '[{"source":"context.order.order_no","target":"body.orderNo"},{"source":"context.order.user_name","target":"body.userName"},{"source":"context.order.amount","target":"body.amount"},{"source":"context.order.id","target":"body.sourceId"}]', NULL, 30000, 0, 'suspend', 3, 0),
(300105, 3001, 'db_mark_synced', '标记已同步', 'db', '{"dsCode":"master","operation":"update","sql":"UPDATE example_order_source SET status = 1 WHERE id = #{order.id}"}', NULL, NULL, 30000, 0, 'suspend', 4, 0),
(300106, 3001, 'end_foreach', '结束循环', 'end_foreach', '{"loopNodeId":"foreach_sync"}', NULL, NULL, NULL, NULL, NULL, 5, 0),
(300107, 3001, 'end_1', '结束', 'end', '{}', NULL, NULL, NULL, NULL, NULL, 6, 0);

-- --------------------------------------------------
-- 7. 流程边（连线）
-- --------------------------------------------------
DELETE FROM `wf_flow_edge` WHERE `flow_id` = 3001;

INSERT INTO `wf_flow_edge` (`id`, `flow_id`, `edge_id`, `source_node`, `target_node`, `condition_type`, `condition_expression`, `priority`, `del_flag`) VALUES
(300201, 3001, 'e1', 'start_1', 'db_query_source', 'default', NULL, 0, 0),
(300202, 3001, 'e2', 'db_query_source', 'foreach_sync', 'default', NULL, 0, 0),
(300203, 3001, 'e3', 'foreach_sync', 'api_call_b', 'default', NULL, 0, 0),
(300204, 3001, 'e4', 'api_call_b', 'db_mark_synced', 'default', NULL, 0, 0),
(300205, 3001, 'e5', 'db_mark_synced', 'end_foreach', 'default', NULL, 0, 0),
(300206, 3001, 'e6', 'end_foreach', 'end_1', 'default', NULL, 0, 0);

-- --------------------------------------------------
-- 8. 更新流程图 JSON（让前端设计器能正确加载）
-- --------------------------------------------------
UPDATE `wf_flow_definition`
SET `graph_json` = '{"nodes":[{"id":"start_1","type":"start","x":300,"y":120,"text":"开始","properties":{"name":"开始","code":"start_1"}},{"id":"db_query_source","type":"db","x":300,"y":240,"text":"查询待同步订单","properties":{"name":"查询待同步订单","code":"db_query_source","dsCode":"master","operation":"select","sql":"SELECT id, order_no, user_name, amount FROM example_order_source WHERE status = 0 ORDER BY id ASC LIMIT 100","resultVarName":"orderList"}},{"id":"foreach_sync","type":"foreach","x":300,"y":360,"text":"循环同步订单","properties":{"name":"循环同步订单","code":"foreach_sync","sourceExpr":"context.orderList","itemVar":"order","indexVar":"index","resultVar":"syncResults","maxIterations":100,"parallel":true,"parallelLimit":3}},{"id":"api_call_b","type":"api","x":180,"y":480,"text":"调用B系统写入","properties":{"name":"调用B系统写入","code":"api_call_b","apiCode":"b-sys-order-sync","timeout":30000,"failStrategy":"suspend"}},{"id":"db_mark_synced","type":"db","x":420,"y":480,"text":"标记已同步","properties":{"name":"标记已同步","code":"db_mark_synced","dsCode":"master","operation":"update","sql":"UPDATE example_order_source SET status = 1 WHERE id = #{order.id}"}},{"id":"end_foreach","type":"end_foreach","x":300,"y":600,"text":"结束循环","properties":{"name":"结束循环","code":"end_foreach","loopNodeId":"foreach_sync"}},{"id":"end_1","type":"end","x":300,"y":720,"text":"结束","properties":{"name":"结束","code":"end_1"}}],"edges":[{"id":"e1","type":"bezier","sourceNodeId":"start_1","targetNodeId":"db_query_source","text":"","properties":{}},{"id":"e2","type":"bezier","sourceNodeId":"db_query_source","targetNodeId":"foreach_sync","text":"","properties":{}},{"id":"e3","type":"bezier","sourceNodeId":"foreach_sync","targetNodeId":"api_call_b","text":"","properties":{}},{"id":"e4","type":"bezier","sourceNodeId":"api_call_b","targetNodeId":"db_mark_synced","text":"","properties":{}},{"id":"e5","type":"bezier","sourceNodeId":"db_mark_synced","targetNodeId":"end_foreach","text":"","properties":{}},{"id":"e6","type":"bezier","sourceNodeId":"end_foreach","targetNodeId":"end_1","text":"","properties":{}}]}'
WHERE `id` = 3001;
