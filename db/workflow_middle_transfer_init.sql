-- ============================================================
-- 中台数据中转流程 - 完整初始数据脚本
-- 架构说明：
--   1. A接口（上游传数据）= /api/open/TRANSFER_INSERT  【SQL类型，动态暴露】
--   2. B接口（下游办理）   = DOWNSTREAM_B               【proxy类型，SQL注册】
--   3. C接口（下游回状态） = /api/open/TRANSFER_UPDATE  【SQL类型，动态暴露】
--   4. D接口（上游收结果） = UPSTREAM_D                 【proxy类型，SQL注册】
--   5. 异步等待：Timer + DB轮询（无需开发callback接口）
--
-- 前置依赖：
--   - OpenApiController 已部署（/api/open/{apiCode} 通用接口执行器）
--   - DynamicTableController.gen-api 已部署
-- ============================================================

SET NAMES utf8mb4;

-- --------------------------------------------------
-- 1. 创建业务中转表（wf_transfer_queue）
--    上游A接口写入 → 下游回调更新状态 → 工作流轮询读取
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS wf_transfer_queue (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    biz_key         VARCHAR(100) NOT NULL COMMENT '业务主键（办件流水号等）',
    source_data     JSON COMMENT '上游传入的原始业务数据',
    downstream_handle_id VARCHAR(100) COMMENT '下游系统返回的办理编号',
    status          VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待办理 / processing-办理中 / done-已完成',
    callback_data   JSON COMMENT '下游回调时传入的数据',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_biz_key (biz_key),
    INDEX idx_status (status),
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='中台业务中转队列表';

-- --------------------------------------------------
-- 2. 动态表配置（供 gen-api 使用，也便于前端管理）
-- --------------------------------------------------
DELETE FROM wf_dynamic_table_column WHERE table_id = 100;
DELETE FROM wf_dynamic_table WHERE id = 100;

INSERT INTO wf_dynamic_table (id, table_code, table_name, ds_id, remark, status, del_flag, create_time, update_time)
VALUES (100, 'wf_transfer_queue', '中台业务中转队列表', 0, '上游A接口写入，下游回调更新，工作流轮询', 1, 0, NOW(), NOW());

INSERT INTO wf_dynamic_table_column (id, table_id, column_code, column_name, data_type, length, is_pk, is_required, is_index, default_value, sort_no, del_flag, create_time, update_time) VALUES
(1001, 100, 'id', '主键ID', 'bigint', 20, 1, 1, 0, NULL, 1, 0, NOW(), NOW()),
(1002, 100, 'biz_key', '业务主键', 'varchar', 100, 0, 1, 1, NULL, 2, 0, NOW(), NOW()),
(1003, 100, 'source_data', '原始业务数据', 'text', NULL, 0, 0, 0, NULL, 3, 0, NOW(), NOW()),
(1004, 100, 'downstream_handle_id', '下游办理编号', 'varchar', 100, 0, 0, 0, NULL, 4, 0, NOW(), NOW()),
(1005, 100, 'status', '办理状态', 'varchar', 20, 0, 1, 1, 'pending', 5, 0, NOW(), NOW()),
(1006, 100, 'callback_data', '回调数据', 'text', NULL, 0, 0, 0, NULL, 6, 0, NOW(), NOW()),
(1007, 100, 'create_time', '创建时间', 'datetime', NULL, 0, 0, 0, 'CURRENT_TIMESTAMP', 7, 0, NOW(), NOW()),
(1008, 100, 'update_time', '更新时间', 'datetime', NULL, 0, 0, 0, 'CURRENT_TIMESTAMP', 8, 0, NOW(), NOW());

-- --------------------------------------------------
-- 3. 注册对外接口（A接口、C接口 通过 /api/open/{apiCode} 暴露）
--    B接口、D接口 为 outgoing proxy 调用
-- --------------------------------------------------

-- A接口：上游数据传入（INSERT）
INSERT INTO wf_api_catalog (id, api_code, api_name, api_type, method, url, content_type, auth_type, ds_id, timeout, retry_times, proxy_enabled, status, del_flag, create_time, update_time)
VALUES (10, 'TRANSFER_INSERT', 'A接口-上游数据传入', 'sql', 'POST',
  'INSERT INTO wf_transfer_queue (biz_key, source_data, status, create_time) VALUES (#{bizKey}, #{sourceData}, ''pending'', NOW())',
  'application/json', 'none', 0, 30000, 0, 0, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE api_name = VALUES(api_name), url = VALUES(url), status = 1;

-- C接口：下游状态回传（UPDATE）
INSERT INTO wf_api_catalog (id, api_code, api_name, api_type, method, url, content_type, auth_type, ds_id, timeout, retry_times, proxy_enabled, status, del_flag, create_time, update_time)
VALUES (11, 'TRANSFER_UPDATE', 'C接口-下游状态回传', 'sql', 'POST',
  'UPDATE wf_transfer_queue SET status = #{status}, callback_data = #{callbackData}, update_time = NOW() WHERE biz_key = #{bizKey}',
  'application/json', 'none', 0, 30000, 0, 0, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE api_name = VALUES(api_name), url = VALUES(url), status = 1;

-- C-查询接口：工作流内部查询单条（供轮询使用，也可供外部调用）
INSERT INTO wf_api_catalog (id, api_code, api_name, api_type, method, url, content_type, auth_type, ds_id, timeout, retry_times, proxy_enabled, status, del_flag, create_time, update_time)
VALUES (12, 'TRANSFER_SELECT', 'C接口-状态查询', 'sql', 'GET',
  'SELECT * FROM wf_transfer_queue WHERE biz_key = #{bizKey}',
  'application/json', 'none', 0, 30000, 0, 0, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE api_name = VALUES(api_name), url = VALUES(url), status = 1;

-- B接口：调用下游业务办理
INSERT INTO wf_api_catalog (id, api_code, api_name, api_type, method, url, content_type, auth_type, timeout, retry_times, proxy_enabled, status, del_flag, create_time, update_time)
VALUES (20, 'DOWNSTREAM_B', 'B接口-下游业务办理', 'proxy', 'POST', 'http://downstream-system/api/business/handle', 'application/json', 'none', 30000, 3, 0, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE api_name = VALUES(api_name), url = VALUES(url), status = 1;

-- D接口：调用上游结果回传
INSERT INTO wf_api_catalog (id, api_code, api_name, api_type, method, url, content_type, auth_type, timeout, retry_times, proxy_enabled, status, del_flag, create_time, update_time)
VALUES (21, 'UPSTREAM_D', 'D接口-上游结果回传', 'proxy', 'POST', 'http://upstream-system/api/transfer/result', 'application/json', 'none', 30000, 3, 0, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE api_name = VALUES(api_name), url = VALUES(url), status = 1;

-- --------------------------------------------------
-- 4. 流程定义
-- --------------------------------------------------
-- 清理已有数据（如果存在）
SET @old_flow_id = (SELECT id FROM wf_flow_definition WHERE flow_code = 'FLOW_MIDDLE_TRANSFER' LIMIT 1);
DELETE FROM wf_flow_edge WHERE flow_id = @old_flow_id;
DELETE FROM wf_flow_node WHERE flow_id = @old_flow_id;
DELETE FROM wf_flow_definition WHERE flow_code = 'FLOW_MIDDLE_TRANSFER';

-- 插入流程定义（id 取当前最大值+1000，避免冲突）
SET @new_flow_id = IFNULL((SELECT MAX(id) + 1000 FROM wf_flow_definition), 1000);
INSERT INTO wf_flow_definition (id, flow_code, flow_name, version, trigger_type, status, graph_json, del_flag, create_time, update_time)
VALUES (
  @new_flow_id,
  'FLOW_MIDDLE_TRANSFER',
  '中台数据中转流程',
  1,
  'event',
  1,
  '{
    "nodes": [
      {"id":"start_1","type":"start","x":300,"y":120,"text":"开始","properties":{"name":"开始","code":"start_1"}},
      {"id":"script_init","type":"script","x":300,"y":240,"text":"初始化上下文","properties":{"name":"初始化上下文","code":"script_init"}},
      {"id":"api_call_b","type":"api","x":300,"y":360,"text":"调用下游B接口","properties":{"name":"调用下游B接口","code":"api_call_b"}},
      {"id":"db_save","type":"db","x":300,"y":480,"text":"保存中转记录","properties":{"name":"保存中转记录","code":"db_save"}},
      {"id":"timer_wait","type":"timer","x":300,"y":600,"text":"等待30分钟","properties":{"name":"等待30分钟","code":"timer_wait"}},
      {"id":"db_poll","type":"db","x":300,"y":720,"text":"轮询状态","properties":{"name":"轮询状态","code":"db_poll"}},
      {"id":"cond_is_done","type":"condition","x":300,"y":840,"text":"是否完成","properties":{"name":"是否完成","code":"cond_is_done"}},
      {"id":"script_assemble","type":"script","x":500,"y":960,"text":"组装D接口数据","properties":{"name":"组装D接口数据","code":"script_assemble"}},
      {"id":"api_call_d","type":"api","x":500,"y":1080,"text":"调用上游D接口","properties":{"name":"调用上游D接口","code":"api_call_d"}},
      {"id":"db_mark","type":"db","x":500,"y":1200,"text":"标记已完成","properties":{"name":"标记已完成","code":"db_mark"}},
      {"id":"end_1","type":"end","x":500,"y":1320,"text":"结束","properties":{"name":"结束","code":"end_1"}}
    ],
    "edges": [
      {"id":"e1","type":"bezier","sourceNodeId":"start_1","targetNodeId":"script_init","text":"","properties":{}},
      {"id":"e2","type":"bezier","sourceNodeId":"script_init","targetNodeId":"api_call_b","text":"","properties":{}},
      {"id":"e3","type":"bezier","sourceNodeId":"api_call_b","targetNodeId":"db_save","text":"","properties":{}},
      {"id":"e4","type":"bezier","sourceNodeId":"db_save","targetNodeId":"timer_wait","text":"","properties":{}},
      {"id":"e5","type":"bezier","sourceNodeId":"timer_wait","targetNodeId":"db_poll","text":"","properties":{}},
      {"id":"e6","type":"bezier","sourceNodeId":"db_poll","targetNodeId":"cond_is_done","text":"","properties":{}},
      {"id":"e7","type":"bezier","sourceNodeId":"cond_is_done","targetNodeId":"script_assemble","text":"是","properties":{"conditionType":"custom","conditionExpression":"#{context.nodeResult_db_poll.data[0].status == \'done\'"}},
      {"id":"e8","type":"bezier","sourceNodeId":"cond_is_done","targetNodeId":"timer_wait","text":"否","properties":{"conditionType":"default"}},
      {"id":"e9","type":"bezier","sourceNodeId":"script_assemble","targetNodeId":"api_call_d","text":"","properties":{}},
      {"id":"e10","type":"bezier","sourceNodeId":"api_call_d","targetNodeId":"db_mark","text":"","properties":{}},
      {"id":"e11","type":"bezier","sourceNodeId":"db_mark","targetNodeId":"end_1","text":"","properties":{}}
    ]
  }',
  0,
  NOW(),
  NOW()
);
SET @flow_id = @new_flow_id;

-- --------------------------------------------------
-- 5. 流程节点
-- --------------------------------------------------
DELETE FROM wf_flow_node WHERE flow_id = @flow_id;

INSERT INTO wf_flow_node (flow_id, node_id, node_name, node_type, config_json, input_mapping, output_mapping, fail_strategy, timeout, retry_times, x_coordinate, y_coordinate, del_flag, create_time, update_time) VALUES
-- 开始
(@flow_id, 'start_1', '开始', 'start', '{}', NULL, NULL, 'suspend', 30000, 3, 300.00, 120.00, 0, NOW(), NOW()),

-- 脚本：初始化上下文
(@flow_id, 'script_init', '初始化上下文', 'script',
  '{"script":"def result = [:]\\nresult.bizKey = bizKey ?: context.bizKey\\nresult.applyData = applyData ?: context.applyData\\nresult.sourceSystem = sourceSystem ?: context.sourceSystem\\nresult.timestamp = new Date().format(\\"yyyy-MM-dd HH:mm:ss\\")\\nreturn result"}',
  '[{"source":"context.bizKey","target":"bizKey"},{"source":"context.applyData","target":"applyData"},{"source":"context.sourceSystem","target":"sourceSystem"}]',
  '[{"source":"result.bizKey","target":"context.bizKey"},{"source":"result.applyData","target":"context.applyData"},{"source":"result.sourceSystem","target":"context.sourceSystem"},{"source":"result.timestamp","target":"context.startTime"}]',
  'suspend', 30000, 3, 300.00, 240.00, 0, NOW(), NOW()),

-- API：调用下游B接口
(@flow_id, 'api_call_b', '调用下游B接口', 'api',
  '{"apiCode":"DOWNSTREAM_B","timeout":30000}',
  '[{"source":"context.applyData","target":"body.data"},{"source":"context.bizKey","target":"body.bizKey"},{"source":"context.sourceSystem","target":"body.sourceSystem"}]',
  '[{"source":"result.body.handleId","target":"context.downstreamHandleId"},{"source":"result.body.status","target":"context.bStatus"}]',
  'suspend', 30000, 3, 300.00, 360.00, 0, NOW(), NOW()),

-- DB：保存中转记录到 wf_transfer_queue
(@flow_id, 'db_save', '保存中转记录', 'db',
  '{"dsCode":"master","operation":"insert","sql":"INSERT INTO wf_transfer_queue (biz_key, source_data, downstream_handle_id, status, create_time) VALUES (\\'#{context.bizKey}\\', \\'#{context.applyData}\\', \\'#{context.downstreamHandleId}\\', \\'pending\\', NOW())"}',
  '[]', '[]',
  'suspend', 30000, 3, 300.00, 480.00, 0, NOW(), NOW()),

-- Timer：等待30分钟（1800秒）
(@flow_id, 'timer_wait', '等待30分钟', 'timer',
  '{"timerType":"delay","delaySeconds":1800}',
  '[]', '[]',
  'suspend', 30000, 3, 300.00, 600.00, 0, NOW(), NOW()),

-- DB：轮询状态
(@flow_id, 'db_poll', '轮询状态', 'db',
  '{"dsCode":"master","operation":"select","sql":"SELECT * FROM wf_transfer_queue WHERE biz_key = \\'#{context.bizKey}\\' ORDER BY update_time DESC LIMIT 1"}',
  '[]',
  '[{"source":"result.data","target":"context.nodeResult_db_poll"}]',
  'suspend', 30000, 3, 300.00, 720.00, 0, NOW(), NOW()),

-- Condition：是否完成
(@flow_id, 'cond_is_done', '是否完成', 'condition',
  '{"conditionExpression":"#{context.nodeResult_db_poll.data[0].status == \'done\'}"}',
  '[]', '[]',
  'suspend', 30000, 3, 300.00, 840.00, 0, NOW(), NOW()),

-- 脚本：组装D接口数据
(@flow_id, 'script_assemble', '组装D接口数据', 'script',
  '{"script":"def row = context.nodeResult_db_poll?.data?.getAt(0) ?: [:]\\ndef result = [:]\\ndef dBody = [:]\\ndBody.bizKey = context.bizKey\\ndBody.handleId = context.downstreamHandleId\\ndBody.callbackData = row.callback_data ?: [:]\\ndBody.sourceData = context.applyData\\ndBody.startTime = context.startTime\\ndBody.finishTime = new Date().format(\\"yyyy-MM-dd HH:mm:ss\\")\\ndBody.status = \\'completed\\'\\nresult.dRequestBody = dBody\\nreturn result"}',
  '[]',
  '[{"source":"result.dRequestBody","target":"context.dRequestBody"}]',
  'suspend', 30000, 3, 500.00, 960.00, 0, NOW(), NOW()),

-- API：调用上游D接口
(@flow_id, 'api_call_d', '调用上游D接口', 'api',
  '{"apiCode":"UPSTREAM_D","timeout":30000}',
  '[{"source":"context.dRequestBody","target":"body"}]',
  '[{"source":"result.body.success","target":"context.dResult"},{"source":"result.body.msg","target":"context.dMsg"}]',
  'suspend', 30000, 3, 500.00, 1080.00, 0, NOW(), NOW()),

-- DB：标记已完成
(@flow_id, 'db_mark', '标记已完成', 'db',
  '{"dsCode":"master","operation":"update","sql":"UPDATE wf_transfer_queue SET status = \\'finished\\' WHERE biz_key = \\'#{context.bizKey}\\'"}',
  '[]', '[]',
  'suspend', 30000, 3, 500.00, 1200.00, 0, NOW(), NOW()),

-- 结束
(@flow_id, 'end_1', '结束', 'end', '{}', NULL, NULL, 'suspend', 30000, 3, 500.00, 1320.00, 0, NOW(), NOW());

-- --------------------------------------------------
-- 6. 流程边
-- --------------------------------------------------
DELETE FROM wf_flow_edge WHERE flow_id = @flow_id;

INSERT INTO wf_flow_edge (flow_id, edge_id, source_node, target_node, condition_type, condition_expression, priority, del_flag, create_time, update_time) VALUES
(@flow_id, 'e1', 'start_1', 'script_init', 'default', NULL, 0, 0, NOW(), NOW()),
(@flow_id, 'e2', 'script_init', 'api_call_b', 'default', NULL, 0, 0, NOW(), NOW()),
(@flow_id, 'e3', 'api_call_b', 'db_save', 'default', NULL, 0, 0, NOW(), NOW()),
(@flow_id, 'e4', 'db_save', 'timer_wait', 'default', NULL, 0, 0, NOW(), NOW()),
(@flow_id, 'e5', 'timer_wait', 'db_poll', 'default', NULL, 0, 0, NOW(), NOW()),
(@flow_id, 'e6', 'db_poll', 'cond_is_done', 'default', NULL, 0, 0, NOW(), NOW()),
-- condition "是" 分支：status == 'done'
(@flow_id, 'e7', 'cond_is_done', 'script_assemble', 'custom', '#{context.nodeResult_db_poll.data[0].status == ''done''}', 0, 0, NOW(), NOW()),
-- condition "否" 分支：默认兜底，返回 timer 继续等待
(@flow_id, 'e8', 'cond_is_done', 'timer_wait', 'default', NULL, 1, 0, NOW(), NOW()),
(@flow_id, 'e9', 'script_assemble', 'api_call_d', 'default', NULL, 0, 0, NOW(), NOW()),
(@flow_id, 'e10', 'api_call_d', 'db_mark', 'default', NULL, 0, 0, NOW(), NOW()),
(@flow_id, 'e11', 'db_mark', 'end_1', 'default', NULL, 0, 0, NOW(), NOW());
