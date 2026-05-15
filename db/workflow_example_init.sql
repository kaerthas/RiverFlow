-- ============================================================
-- 流程示例初始化脚本
-- 适用场景: A系统收件 → 查动态表 → 脚本组装 → 调用B系统 → 更新状态
-- 
-- 使用说明:
-- 1. 请先确认 wf_flow_definition 中你的流程ID，替换下方 @flowId
-- 2. 确认动态表 com_business_notify 的列名是否匹配（receive_num, apply_subject, status, b_order_id）
-- 3. 按顺序执行本脚本
-- ============================================================

-- --------------------------------------------------
-- 1. 注册B系统示例接口到 wf_api_catalog
-- --------------------------------------------------
INSERT INTO wf_api_catalog (id, api_code, api_name, api_type, method, url, content_type, auth_type, timeout, retry_times, proxy_enabled, status, del_flag)
VALUES (1000001, 'b-sys-example-submit', 'B系统-示例提交接口', 'proxy', 'POST', 'http://localhost:8080/example/b-sys/submit', 'application/json', 'none', 30000, 0, 0, 1, 0)
ON DUPLICATE KEY UPDATE api_name='B系统-示例提交接口', url='http://localhost:8080/example/b-sys/submit', status=1;


-- --------------------------------------------------
-- 2. 插入流程节点（请根据实际 flow_id 修改）
-- --------------------------------------------------
-- 注意: 将下方所有 1003 替换为你实际的流程定义ID
--       可通过 SELECT id, flow_name FROM wf_flow_definition; 查询

DELETE FROM wf_flow_node WHERE flow_id = 1003;

INSERT INTO wf_flow_node (id, flow_id, node_id, node_name, node_type, config_json, input_mapping, output_mapping, timeout, retry_times, fail_strategy, sort_no, del_flag) VALUES
(100001, 1003, 'start_1',     '开始',           'start',  '{}', NULL, NULL, NULL, NULL, NULL, 0, 0),
(100002, 1003, 'db_query',    '查询收件数据',    'db',     '{"dsCode":"master","operation":"select","sql":"SELECT * FROM com_business_notify WHERE receive_num = #{_businessKey}"}', NULL, '[{"source":"result.data[0]","target":"context.aData"}]', 30000, 0, 'suspend', 1, 0),
(100003, 1003, 'script_assemble', '组装B系统报文', 'script', '{"scriptContent":"def a = context.get(''aData''); def b = [:]; b.projectName = a.apply_subject; b.projectNo = a.receive_num; context.set(''bRequest'', b); return b;"}', NULL, '[{"source":"result","target":"context.bRequest"}]', 30000, 0, 'suspend', 2, 0),
(100004, 1003, 'api_call_b',  '调用B系统提交',   'api',    '{"apiCode":"b-sys-example-submit","timeout":30000,"failStrategy":"suspend"}', '[{"source":"context.bRequest","target":"body"}]', '[{"source":"body.orderId","target":"context.bOrderId"}]', 30000, 0, 'suspend', 3, 0),
(100005, 1003, 'db_update',   '更新推送状态',    'db',     '{"dsCode":"master","operation":"update","sql":"UPDATE com_business_notify SET status = ''SENT'', b_order_id = #{bOrderId} WHERE receive_num = #{_businessKey}"}', NULL, NULL, 30000, 0, 'suspend', 4, 0),
(100006, 1003, 'end_1',       '结束',           'end',    '{}', NULL, NULL, NULL, NULL, NULL, 5, 0);


-- --------------------------------------------------
-- 3. 插入流程边（连线）
-- --------------------------------------------------
DELETE FROM wf_flow_edge WHERE flow_id = 1003;

INSERT INTO wf_flow_edge (id, flow_id, edge_id, source_node, target_node, condition_type, condition_expression, priority, del_flag) VALUES
(100001, 1003, 'e1', 'start_1',     'db_query',      'default', NULL, 0, 0),
(100002, 1003, 'e2', 'db_query',    'script_assemble','default', NULL, 0, 0),
(100003, 1003, 'e3', 'script_assemble','api_call_b',  'default', NULL, 0, 0),
(100004, 1003, 'e4', 'api_call_b',  'db_update',     'default', NULL, 0, 0),
(100005, 1003, 'e5', 'db_update',   'end_1',         'default', NULL, 0, 0);


-- --------------------------------------------------
-- 4. 更新流程定义的 graph_json（让前端设计器能正确加载）
-- --------------------------------------------------
UPDATE wf_flow_definition SET graph_json = '{"nodes":[{"id":"start_1","type":"start","x":300,"y":120,"text":"开始","properties":{"name":"开始","code":"start_1"}},{"id":"db_query","type":"db","x":300,"y":240,"text":"查询收件数据","properties":{"name":"查询收件数据","code":"db_query","dsCode":"master","operation":"select","sql":"SELECT * FROM com_business_notify WHERE receive_num = #{_businessKey}"}},{"id":"script_assemble","type":"script","x":300,"y":360,"text":"组装B系统报文","properties":{"name":"组装B系统报文","code":"script_assemble","scriptContent":"def a = context.get(''aData''); def b = [:]; b.projectName = a.apply_subject; b.projectNo = a.receive_num; context.set(''bRequest'', b); return b;"}},{"id":"api_call_b","type":"api","x":300,"y":480,"text":"调用B系统提交","properties":{"name":"调用B系统提交","code":"api_call_b","apiCode":"b-sys-example-submit","timeout":30000,"failStrategy":"suspend"}},{"id":"db_update","type":"db","x":300,"y":600,"text":"更新推送状态","properties":{"name":"更新推送状态","code":"db_update","dsCode":"master","operation":"update","sql":"UPDATE com_business_notify SET status = ''SENT'', b_order_id = #{bOrderId} WHERE receive_num = #{_businessKey}"}},{"id":"end_1","type":"end","x":300,"y":720,"text":"结束","properties":{"name":"结束","code":"end_1"}}],"edges":[{"id":"e1","type":"bezier","sourceNodeId":"start_1","targetNodeId":"db_query","text":"","properties":{}},{"id":"e2","type":"bezier","sourceNodeId":"db_query","targetNodeId":"script_assemble","text":"","properties":{}},{"id":"e3","type":"bezier","sourceNodeId":"script_assemble","targetNodeId":"api_call_b","text":"","properties":{}},{"id":"e4","type":"bezier","sourceNodeId":"api_call_b","targetNodeId":"db_update","text":"","properties":{}},{"id":"e5","type":"bezier","sourceNodeId":"db_update","targetNodeId":"end_1","text":"","properties":{}}]}' WHERE id = 1003;
