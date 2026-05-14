-- ============================================
-- 修复缺少 BaseEntity 字段的表
-- 这些表继承了 BaseEntity，必须有 del_flag / create_by / update_by / update_time
-- ============================================

-- 1. wf_flow_log 缺少 update_time / create_by / update_by / del_flag
ALTER TABLE wf_flow_log
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
    ADD COLUMN create_by VARCHAR(64) DEFAULT '' COMMENT '创建人' AFTER update_time,
    ADD COLUMN update_by VARCHAR(64) DEFAULT '' COMMENT '更新人' AFTER create_by,
    ADD COLUMN del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除' AFTER update_by;

-- 2. wf_flow_instance 缺少 create_by / update_by / del_flag
ALTER TABLE wf_flow_instance
    ADD COLUMN create_by VARCHAR(64) DEFAULT '' COMMENT '创建人' AFTER update_time,
    ADD COLUMN update_by VARCHAR(64) DEFAULT '' COMMENT '更新人' AFTER create_by,
    ADD COLUMN del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除' AFTER update_by;

-- 3. wf_flow_task 缺少 create_by / update_by / del_flag
ALTER TABLE wf_flow_task
    ADD COLUMN create_by VARCHAR(64) DEFAULT '' COMMENT '创建人' AFTER update_time,
    ADD COLUMN update_by VARCHAR(64) DEFAULT '' COMMENT '更新人' AFTER create_by,
    ADD COLUMN del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除' AFTER update_by;

-- 4. wf_api_call_log 缺少 update_time / create_by / update_by / del_flag
ALTER TABLE wf_api_call_log
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
    ADD COLUMN create_by VARCHAR(64) DEFAULT '' COMMENT '创建人' AFTER update_time,
    ADD COLUMN update_by VARCHAR(64) DEFAULT '' COMMENT '更新人' AFTER create_by,
    ADD COLUMN del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除' AFTER update_by;

-- 5. wf_api_param 缺少 create_by / update_by / del_flag
ALTER TABLE wf_api_param
    ADD COLUMN create_by VARCHAR(64) DEFAULT '' COMMENT '创建人' AFTER update_time,
    ADD COLUMN update_by VARCHAR(64) DEFAULT '' COMMENT '更新人' AFTER create_by,
    ADD COLUMN del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除' AFTER update_by;
