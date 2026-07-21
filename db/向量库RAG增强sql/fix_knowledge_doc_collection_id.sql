-- 关联知识库与向量集合配置
-- 给 wf_ai_knowledge_doc 增加 collection_id 字段，指向 wf_ai_vector_collection.id

ALTER TABLE `wf_ai_knowledge_doc`
    ADD COLUMN `collection_id` BIGINT NULL COMMENT '所属向量集合配置ID' AFTER `collection`,
    ADD KEY `idx_collection_id` (`collection_id`);

-- 向量集合配置增加 Embedding 独立配置字段（为空则回退到全局配置）
ALTER TABLE `wf_ai_vector_collection`
    ADD COLUMN `embedding_base_url` VARCHAR(255) NULL COMMENT 'Embedding 基础URL' AFTER `embedding_type`,
    ADD COLUMN `embedding_api_key` VARCHAR(255) NULL COMMENT 'Embedding API Key' AFTER `embedding_base_url`,
    ADD COLUMN `embedding_model` VARCHAR(128) NULL COMMENT 'Embedding 模型' AFTER `embedding_api_key`;

-- 将已有文档归到默认集合（riverflow_default，id=1）
UPDATE `wf_ai_knowledge_doc`
SET `collection_id` = 1
WHERE `collection_id` IS NULL
  AND `collection` = 'riverflow_default';

-- 向量集合配置增加默认集合标记
ALTER TABLE `wf_ai_vector_collection`
    ADD COLUMN `is_default` TINYINT DEFAULT 0 COMMENT '是否默认集合：0-否 1-是' AFTER `enabled`;

-- 初始化默认集合标记
UPDATE `wf_ai_vector_collection`
SET `is_default` = 1
WHERE `id` = 1;

-- 向量集合配置增加 Milvus 连接字段
ALTER TABLE `wf_ai_vector_collection`
    ADD COLUMN `milvus_host` VARCHAR(128) NULL COMMENT 'Milvus 主机地址' AFTER `embedding_model`,
    ADD COLUMN `milvus_port` INT NULL COMMENT 'Milvus 端口' AFTER `milvus_host`,
    ADD COLUMN `milvus_database` VARCHAR(64) NULL COMMENT 'Milvus 数据库名' AFTER `milvus_port`,
    ADD COLUMN `milvus_token` VARCHAR(255) NULL COMMENT 'Milvus Token' AFTER `milvus_database`,
    ADD COLUMN `milvus_secure` TINYINT NULL COMMENT '是否使用TLS/HTTPS：0-否 1-是' AFTER `milvus_token`;
