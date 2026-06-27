-- ============================================================
-- 流程示例初始化脚本：while 计数器循环累加测试
-- 适用场景: 验证 while 循环基本执行流程（条件驱动、迭代计数、结果聚合）
--
-- 使用说明:
-- 1. 请先确认 wf_flow_definition 中你的流程ID，默认使用 3002
-- 2. 如需自定义，请全局替换 3002 为新的 flow_id
-- 3. 按顺序执行本脚本
-- ============================================================

SET NAMES utf8mb4;

-- --------------------------------------------------
-- 1. 流程定义（请根据实际 flow_id 修改）
-- --------------------------------------------------
INSERT INTO `wf_flow_definition` (`id`, `flow_code`, `flow_name`, `version`, `trigger_type`, `status`, `graph_json`, `create_time`, `update_time`, `create_by`, `del_flag`)
VALUES (3002, 'FLOW_WHILE_COUNTER_DEMO', 'while计数器循环累加示例', 1, 'manual', 1, '{"nodes":[],"edges":[]}', NOW(), NOW(), 'admin', 0)
ON DUPLICATE KEY UPDATE `flow_name` = 'while计数器循环累加示例', `status` = 1;

-- --------------------------------------------------
-- 2. 流程节点
-- --------------------------------------------------
DELETE FROM `wf_flow_node` WHERE `flow_id` = 3002;

INSERT INTO `wf_flow_node` (`id`, `flow_id`, `node_id`, `node_name`, `node_type`, `config_json`, `input_mapping`, `output_mapping`, `timeout`, `retry_times`, `fail_strategy`, `sort_no`, `del_flag`) VALUES
(300201, 3002, 'start_1', '开始', 'start', '{}', NULL, NULL, NULL, NULL, NULL, 0, 0),
(300202, 3002, 'script_init', '初始化计数器', 'script', '{"scriptContent":"def counter = 0\ndef results = []\nreturn [\\"counter\\": counter, \\"results\\": results]"}', NULL, '[{"source":"counter","target":"context.counter"},{"source":"results","target":"context.results"}]', 30000, 0, 'suspend', 1, 0),
(300203, 3002, 'while_loop', 'while循环', 'while', '{"conditionExpr":"#{context.counter < 5}","maxIterations":10,"timeout":30000,"resultVar":"loopResults"}', NULL, NULL, 30000, 0, 'suspend', 2, 0),
(300204, 3002, 'script_body', '计数器累加', 'script', '{"scriptContent":"def counter = context.get(\\"counter\\") ?: 0\ncounter = counter + 1\ndef results = context.get(\\"results\\") ?: []\nresults.add(counter)\ncontext.set(\\"counter\\", counter)\ncontext.set(\\"results\\", results)\nreturn [\\"counter\\": counter, \\"results\\": results]"}', NULL, '[{"source":"counter","target":"context.counter"},{"source":"results","target":"context.results"}]', 30000, 0, 'suspend', 3, 0),
(300205, 3002, 'end_while', '结束循环', 'end_while', '{"loopNodeId":"while_loop","aggregateExpr":"context.counter"}', NULL, NULL, NULL, NULL, NULL, 4, 0),
(300206, 3002, 'end_1', '结束', 'end', '{}', NULL, NULL, NULL, NULL, NULL, 5, 0);

-- --------------------------------------------------
-- 3. 流程边（连线）
-- --------------------------------------------------
DELETE FROM `wf_flow_edge` WHERE `flow_id` = 3002;

INSERT INTO `wf_flow_edge` (`id`, `flow_id`, `edge_id`, `source_node`, `target_node`, `condition_type`, `condition_expression`, `priority`, `del_flag`) VALUES
(300301, 3002, 'e1', 'start_1', 'script_init', 'default', NULL, 0, 0),
(300302, 3002, 'e2', 'script_init', 'while_loop', 'default', NULL, 0, 0),
(300303, 3002, 'e3', 'while_loop', 'script_body', 'default', NULL, 0, 0),
(300304, 3002, 'e4', 'script_body', 'end_while', 'default', NULL, 0, 0),
(300305, 3002, 'e5', 'end_while', 'end_1', 'default', NULL, 0, 0);

-- --------------------------------------------------
-- 4. 更新流程图 JSON（供前端设计器预览，配置项已简化）
-- --------------------------------------------------
UPDATE `wf_flow_definition`
SET `graph_json` = '{"nodes":[{"id":"start_1","type":"start","x":300,"y":120,"text":"开始","properties":{"name":"开始","code":"start_1"}},{"id":"script_init","type":"script","x":300,"y":240,"text":"初始化计数器","properties":{"name":"初始化计数器","code":"script_init","scriptContent":"def counter = 0\\ndef results = []\\nreturn [\\"counter\\": counter, \\"results\\": results]"}},{"id":"while_loop","type":"while","x":300,"y":360,"text":"while循环","properties":{"name":"while循环","code":"while_loop","conditionExpr":"#{context.counter < 5}","maxIterations":10,"timeout":30000,"resultVar":"loopResults"}},{"id":"script_body","type":"script","x":300,"y":480,"text":"计数器累加","properties":{"name":"计数器累加","code":"script_body","scriptContent":"def counter = context.get(\\"counter\\") ?: 0\\ncounter = counter + 1\\ndef results = context.get(\\"results\\") ?: []\\nresults.add(counter)\\ncontext.set(\\"counter\\", counter)\\ncontext.set(\\"results\\", results)\\nreturn [\\"counter\\": counter, \\"results\\": results]","inputMapping":"[]","outputMapping":"[{\\"source\\":\\"counter\\",\\"target\\":\\"context.counter\\"},{\\"source\\":\\"results\\",\\"target\\":\\"context.results\\"}]"}},{"id":"end_while","type":"end_while","x":300,"y":600,"text":"结束循环","properties":{"name":"结束循环","code":"end_while","loopNodeId":"while_loop","aggregateExpr":"context.counter"}},{"id":"end_1","type":"end","x":300,"y":720,"text":"结束","properties":{"name":"结束","code":"end_1"}}],"edges":[{"id":"e1","type":"bezier","sourceNodeId":"start_1","targetNodeId":"script_init","text":"","properties":{}},{"id":"e2","type":"bezier","sourceNodeId":"script_init","targetNodeId":"while_loop","text":"","properties":{}},{"id":"e3","type":"bezier","sourceNodeId":"while_loop","targetNodeId":"script_body","text":"","properties":{}},{"id":"e4","type":"bezier","sourceNodeId":"script_body","targetNodeId":"end_while","text":"","properties":{}},{"id":"e5","type":"bezier","sourceNodeId":"end_while","targetNodeId":"end_1","text":"","properties":{}}]}'
WHERE `id` = 3002;

-- --------------------------------------------------
-- 5. 验证查询
-- --------------------------------------------------
SELECT '流程节点数' AS `check_item`, COUNT(*) AS `cnt` FROM `wf_flow_node` WHERE `flow_id` = 3002
UNION ALL
SELECT '流程边数', COUNT(*) FROM `wf_flow_edge` WHERE `flow_id` = 3002;
