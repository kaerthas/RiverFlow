-- AI 向量知识库表结构（第二阶段）
-- 支持多向量库：Milvus / PGVector / InMemory

-- 知识文档元数据表
CREATE TABLE IF NOT EXISTS `wf_ai_knowledge_doc` (
    `id`              BIGINT       NOT NULL COMMENT '主键ID',
    `title`           VARCHAR(200) NOT NULL COMMENT '文档标题',
    `source_type`     VARCHAR(32)  NOT NULL COMMENT '来源类型：flow/api/datasource/dynamic_table/file/upload',
    `source_id`       VARCHAR(64)  NULL     COMMENT '来源业务ID',
    `content`         LONGTEXT     NULL     COMMENT '原始内容',
    `chunk_count`     INT          DEFAULT 0 COMMENT '分块数量',
    `vector_status`   TINYINT      DEFAULT 0 COMMENT '向量状态：0-未索引 1-索引中 2-已索引 3-失败',
    `collection`      VARCHAR(64)  NULL     COMMENT '所属向量集合',
    `enabled`         TINYINT      DEFAULT 1 COMMENT '是否启用：0-停用 1-启用',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       VARCHAR(64)  DEFAULT '' COMMENT '创建人',
    `update_by`       VARCHAR(64)  DEFAULT '' COMMENT '更新人',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_source` (`source_type`, `source_id`),
    KEY `idx_collection` (`collection`),
    KEY `idx_status` (`vector_status`, `enabled`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 知识文档元数据表';

-- 知识文档分块表
CREATE TABLE IF NOT EXISTS `wf_ai_knowledge_chunk` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `doc_id`          BIGINT       NOT NULL COMMENT '文档ID',
    `chunk_index`     INT          NOT NULL COMMENT '分块序号',
    `content`         TEXT         NOT NULL COMMENT '分块内容',
    `token_length`    INT          DEFAULT NULL COMMENT '预估token长度',
    `metadata`        JSON         NULL     COMMENT '分块元数据',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_doc_chunk` (`doc_id`, `chunk_index`),
    KEY `idx_doc_id` (`doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 知识文档分块表';

-- 向量集合配置表
CREATE TABLE IF NOT EXISTS `wf_ai_vector_collection` (
    `id`              BIGINT       NOT NULL COMMENT '主键ID',
    `collection`      VARCHAR(64)  NOT NULL COMMENT '集合/表名称',
    `store_type`      VARCHAR(32)  NOT NULL COMMENT '向量库类型：milvus/pgvector/memory',
    `dimension`       INT          NOT NULL COMMENT '向量维度',
    `distance_metric` VARCHAR(32)  DEFAULT 'COSINE' COMMENT '距离度量：COSINE/IP/L2',
    `embedding_type`  VARCHAR(32)  NOT NULL COMMENT 'Embedding类型/客户端',
    `description`     VARCHAR(500) NULL,
    `enabled`         TINYINT      DEFAULT 1 COMMENT '是否启用',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_collection` (`collection`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 向量集合配置表';

-- 初始化默认向量集合（Milvus + OpenAI 兼容 Embedding，1536 维）
INSERT INTO `wf_ai_vector_collection` (`id`, `collection`, `store_type`, `dimension`, `distance_metric`, `embedding_type`, `description`, `enabled`)
VALUES (1, 'riverflow_default', 'milvus', 1536, 'COSINE', 'openai', 'RiverFlow 默认知识库向量集合', 1)
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;
