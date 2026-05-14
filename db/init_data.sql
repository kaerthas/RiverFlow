-- ============================================
-- RiverFlow 初始数据脚本
-- 请先确保已执行 riverflow_init.sql 完成建表
-- ============================================

-- 1. 系统管理员账户（密码：admin123，BCrypt加密）
INSERT INTO sys_user (id, username, password, real_name, avatar, email, phone, status, create_time, update_time)
VALUES (1, 'admin', '$2a$10$t56jkpfYa2C.jIaTkjVy3uptUZn4vbkUIRSRSyryTYo116lqgMENi', '系统管理员', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', 'admin@riverflow.com', '13800138000', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE password = VALUES(password), status = 1;

-- 2. 行政区划数据（用于事项管理）
INSERT INTO wf_region (id, region_code, region_name, parent_code, level, sort_no, del_flag, create_time, update_time) VALUES
(1, '610000', '陕西省', '0', 1, 1, 0, NOW(), NOW()),
(2, '610100', '西安市', '610000', 2, 2, 0, NOW(), NOW()),
(3, '610102', '新城区', '610100', 3, 3, 0, NOW(), NOW()),
(4, '610103', '碑林区', '610100', 3, 4, 0, NOW(), NOW()),
(5, '610104', '莲湖区', '610100', 3, 5, 0, NOW(), NOW()),
(6, '610200', '铜川市', '610000', 2, 6, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE region_name = VALUES(region_name);

-- 3. 流程定义测试数据
INSERT INTO wf_flow_definition (id, flow_code, flow_name, version, item_code, trigger_type, trigger_config, status, graph_json, create_time, update_time, create_by, del_flag) VALUES
(1, 'FLOW_001', '残疾人证新办流程', 1, '610000-001', 'manual', '', 1, '{"nodes":[],"edges":[]}', NOW(), NOW(), 'admin', 0),
(2, 'FLOW_002', '火化信息推送流程', 1, '610000-002', 'event', '', 0, '{"nodes":[],"edges":[]}', NOW(), NOW(), 'admin', 0),
(3, 'FLOW_003', '低保申请协同流程', 1, '610000-003', 'cron', '0 0 2 * * ?', 1, '{"nodes":[],"edges":[]}', NOW(), NOW(), 'admin', 0)
ON DUPLICATE KEY UPDATE flow_name = VALUES(flow_name);

-- 4. 数据源测试数据（可选）
INSERT INTO wf_datasource (id, ds_code, ds_name, db_type, url, username, password, driver_class, status, create_time, update_time, del_flag) VALUES
(1, 'master', '主库(MySQL)', 'mysql', 'jdbc:mysql://127.0.0.1:3306/riverflow?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai', 'root', 'root', 'com.mysql.cj.jdbc.Driver', 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE ds_name = VALUES(ds_name);
