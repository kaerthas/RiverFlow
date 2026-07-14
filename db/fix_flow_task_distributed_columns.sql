-- RiverFlow 分布式调度与并发控制改造：wf_flow_task 表结构变更
-- 适用版本：v1.0.0-SNAPSHOT
-- 执行说明：在 riverflow 数据库中执行本脚本

-- 1. 增加乐观锁版本号
ALTER TABLE `wf_flow_task`
    ADD COLUMN `version` INT NOT NULL DEFAULT '0' COMMENT '乐观锁版本号' AFTER `status`;

-- 2. 增加任务执行节点标识
ALTER TABLE `wf_flow_task`
    ADD COLUMN `execute_node` VARCHAR(64) DEFAULT NULL COMMENT '实际执行该任务的节点标识（IP/主机名）' AFTER `version`;

-- 3. 增加任务执行/认领时间
ALTER TABLE `wf_flow_task`
    ADD COLUMN `execute_time` DATETIME DEFAULT NULL COMMENT '任务被认领/开始执行的时间' AFTER `execute_node`;

-- 4. 初始化历史数据版本号
UPDATE `wf_flow_task` SET `version` = 0 WHERE `version` IS NULL;

-- 5. 优化索引：状态 + 下次执行时间，用于调度器快速查询待执行任务
ALTER TABLE `wf_flow_task`
    ADD INDEX `idx_status_next_time` (`status`, `next_execute_time`);

-- 6. 优化索引：实例 + 节点，用于引擎二次校验与日志查询
ALTER TABLE `wf_flow_task`
    ADD INDEX `idx_instance_node` (`instance_id`, `node_id`);

-- 7. 若原表无 status 字段，请使用以下语句（视实际情况选择）
-- ALTER TABLE `wf_flow_task`
--     ADD COLUMN `status` VARCHAR(16) DEFAULT NULL COMMENT '任务状态：pending/running/waiting/completed/failed' AFTER `node_type`;
