-- AI 知识库检索增强：为现有表添加全文索引
-- 如果数据库不支持全文索引或已存在，可忽略报错继续执行

-- 接口目录全文索引
ALTER TABLE `wf_api_catalog`
    ADD FULLTEXT INDEX `ft_api_catalog` (`api_code`, `api_name`, `url`);

-- 流程定义全文索引
ALTER TABLE `wf_flow_definition`
    ADD FULLTEXT INDEX `ft_flow_definition` (`flow_code`, `flow_name`);

-- 动态数据源全文索引
ALTER TABLE `wf_datasource`
    ADD FULLTEXT INDEX `ft_datasource` (`ds_code`, `ds_name`);

-- 动态表定义全文索引（可选，用于表名检索）
ALTER TABLE `wf_dynamic_table`
    ADD FULLTEXT INDEX `ft_dynamic_table` (`table_code`, `table_name`, `remark`);
