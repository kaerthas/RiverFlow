-- AI Prompt 模板表
-- 支持按场景(scene) + 模型(model) + 版本(version) 管理 Prompt 模板
-- 数据库中的 Prompt 优先级高于 classpath:ai-prompts/ 下的文件
CREATE TABLE IF NOT EXISTS `wf_ai_prompt` (
    `id`              BIGINT       NOT NULL COMMENT '主键ID',
    `scene`           VARCHAR(64)  NOT NULL COMMENT '场景：flow-generation/condition-generation/mapping-recommendation/script-generation/api-doc-parse',
    `model`           VARCHAR(64)  NOT NULL COMMENT '模型：qwen2.5:14b/gpt-4o-mini/default',
    `version`         VARCHAR(16)  NOT NULL COMMENT '版本：v1/v2',
    `template`        TEXT         NOT NULL COMMENT 'Prompt 模板',
    `system_prompt`   TEXT         NULL     COMMENT '系统 Prompt（可选，为空时使用默认系统 Prompt）',
    `examples`        TEXT         NULL     COMMENT 'Few-shot 示例 JSON 数组',
    `output_schema`   TEXT         NULL     COMMENT '输出 JSON Schema',
    `description`     VARCHAR(500) NULL     COMMENT '描述',
    `enabled`         TINYINT      DEFAULT 1 COMMENT '是否启用：0-停用 1-启用',
    `sort_no`         INT          DEFAULT 0 COMMENT '排序号',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       VARCHAR(64)  DEFAULT '' COMMENT '创建人',
    `update_by`       VARCHAR(64)  DEFAULT '' COMMENT '更新人',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scene_model_version` (`scene`, `model`, `version`),
    KEY `idx_scene` (`scene`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Prompt 模板表';

-- 初始化默认 Prompt：使用 classpath 文件兜底，数据库留空即可
-- 如需覆盖文件中的 Prompt，可在此插入记录
