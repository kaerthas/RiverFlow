-- ============================================================
-- RiverFlow · 河狸流程编排平台
-- 数据库初始化脚本（MySQL 8.0）
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS riverflow
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE riverflow;

-- ============================================================
-- 1. 基础系统表
-- ============================================================

-- 行政区划
CREATE TABLE IF NOT EXISTS wf_region (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    region_code VARCHAR(20) NOT NULL COMMENT '区划代码',
    region_name VARCHAR(100) NOT NULL COMMENT '区划名称',
    parent_code VARCHAR(20) DEFAULT '0' COMMENT '父级代码',
    level TINYINT DEFAULT 1 COMMENT '层级：1-省 2-市 3-区县',
    sort_no INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    UNIQUE KEY uk_region_code (region_code),
    INDEX idx_parent (parent_code),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行政区划';

-- ============================================================
-- 2. 事项管理模块
-- ============================================================

-- 政务服务事项
CREATE TABLE IF NOT EXISTS wf_item (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    item_code VARCHAR(50) NOT NULL COMMENT '事项编码',
    item_name VARCHAR(200) NOT NULL COMMENT '事项名称',
    region_code VARCHAR(20) NOT NULL COMMENT '区划代码',
    region_name VARCHAR(100) DEFAULT NULL COMMENT '区划名称',
    catalog_code VARCHAR(50) DEFAULT NULL COMMENT '国家基本编码',
    task_code VARCHAR(50) DEFAULT NULL COMMENT '国家实施编码',
    task_handle_item VARCHAR(50) DEFAULT NULL COMMENT '国家业务办理项编码',
    service_obj TINYINT DEFAULT 0 COMMENT '办理对象：0-个人 1-法人',
    flow_id BIGINT DEFAULT NULL COMMENT '绑定流程定义ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    UNIQUE KEY uk_item_code (item_code),
    INDEX idx_region (region_code),
    INDEX idx_flow (flow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='政务服务事项';

-- ============================================================
-- 3. 动态数据源与动态表模块
-- ============================================================

-- 数据源配置
CREATE TABLE IF NOT EXISTS wf_datasource (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    ds_code VARCHAR(50) NOT NULL COMMENT '数据源编码',
    ds_name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    db_type VARCHAR(20) NOT NULL COMMENT '数据库类型：mysql/oracle/sqlserver/postgresql',
    url VARCHAR(500) NOT NULL COMMENT '连接URL',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码（Jasypt加密）',
    driver_class VARCHAR(200) DEFAULT NULL COMMENT '驱动类名',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    UNIQUE KEY uk_ds_code (ds_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态数据源配置';

-- 动态表定义
CREATE TABLE IF NOT EXISTS wf_dynamic_table (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    table_code VARCHAR(50) NOT NULL COMMENT '表编码（英文）',
    table_name VARCHAR(100) NOT NULL COMMENT '表名称（中文）',
    ds_id BIGINT DEFAULT 0 COMMENT '所属数据源ID，0表示主库',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    UNIQUE KEY uk_table_code (table_code),
    INDEX idx_ds (ds_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态表定义';

-- 动态表字段定义
CREATE TABLE IF NOT EXISTS wf_dynamic_table_column (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    table_id BIGINT NOT NULL COMMENT '所属表ID',
    column_code VARCHAR(50) NOT NULL COMMENT '字段编码',
    column_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    data_type VARCHAR(20) NOT NULL COMMENT '数据类型：varchar/int/bigint/datetime/text/decimal等',
    length INT DEFAULT NULL COMMENT '长度',
    decimal_scale INT DEFAULT NULL COMMENT '小数位',
    is_pk TINYINT DEFAULT 0 COMMENT '是否主键：0-否 1-是',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填：0-否 1-是',
    is_index TINYINT DEFAULT 0 COMMENT '是否索引：0-否 1-是',
    default_value VARCHAR(200) DEFAULT NULL COMMENT '默认值',
    sort_no INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    UNIQUE KEY uk_table_column (table_id, column_code),
    INDEX idx_table (table_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态表字段定义';

-- ============================================================
-- 4. 第三方接口管理模块
-- ============================================================

-- 接口目录
CREATE TABLE IF NOT EXISTS wf_api_catalog (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    api_code VARCHAR(50) NOT NULL COMMENT '接口编码',
    api_name VARCHAR(100) NOT NULL COMMENT '接口名称',
    api_type VARCHAR(20) NOT NULL COMMENT '接口类型：proxy-代理 sql-SQL服务 data-数据服务 script-脚本服务',
    method VARCHAR(10) DEFAULT 'POST' COMMENT '请求方式：GET/POST/PUT/DELETE',
    url VARCHAR(500) DEFAULT NULL COMMENT '请求地址',
    content_type VARCHAR(50) DEFAULT 'application/json' COMMENT '请求体类型',
    auth_type VARCHAR(20) DEFAULT 'none' COMMENT '认证方式：none/basic/token/oauth2',
    ds_id BIGINT DEFAULT NULL COMMENT 'SQL类型时绑定的数据源ID',
    script_id BIGINT DEFAULT NULL COMMENT '脚本类型时绑定的脚本ID',
    timeout INT DEFAULT 30000 COMMENT '超时毫秒',
    retry_times TINYINT DEFAULT 0 COMMENT '重试次数',
    proxy_enabled TINYINT DEFAULT 0 COMMENT '是否启用代理：0-否 1-是',
    proxy_host VARCHAR(200) DEFAULT NULL COMMENT '代理主机',
    proxy_port INT DEFAULT NULL COMMENT '代理端口',
    status TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-下线',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    UNIQUE KEY uk_api_code (api_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口目录';

-- 接口参数定义
CREATE TABLE IF NOT EXISTS wf_api_param (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    api_id BIGINT NOT NULL COMMENT '所属接口ID',
    param_type VARCHAR(20) NOT NULL COMMENT '参数类型：header/query/body/response',
    parent_id BIGINT DEFAULT 0 COMMENT '父参数ID，支持嵌套',
    param_key VARCHAR(100) NOT NULL COMMENT '参数键',
    param_name VARCHAR(100) DEFAULT NULL COMMENT '参数名称',
    data_type VARCHAR(20) DEFAULT 'string' COMMENT '数据类型：string/int/object/array',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填：0-否 1-是',
    default_value VARCHAR(500) DEFAULT NULL COMMENT '默认值',
    sort_no INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_api_type (api_id, param_type),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口参数定义';

-- 接口脚本库
CREATE TABLE IF NOT EXISTS wf_api_script (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    script_code VARCHAR(50) NOT NULL COMMENT '脚本编码',
    script_name VARCHAR(100) NOT NULL COMMENT '脚本名称',
    script_type VARCHAR(20) NOT NULL COMMENT '脚本类型：format-格式化 header-请求头 result-结果处理 condition-条件判断',
    script_content TEXT COMMENT 'Groovy脚本内容',
    params VARCHAR(500) DEFAULT NULL COMMENT '脚本入参定义JSON',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    UNIQUE KEY uk_script_code (script_code),
    INDEX idx_type (script_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口脚本库';

-- 接口调用日志
CREATE TABLE IF NOT EXISTS wf_api_call_log (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    api_id BIGINT NOT NULL COMMENT '接口ID',
    api_code VARCHAR(50) NOT NULL COMMENT '接口编码',
    request_url VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
    request_headers TEXT DEFAULT NULL COMMENT '请求头JSON',
    request_body TEXT DEFAULT NULL COMMENT '请求体',
    response_body TEXT DEFAULT NULL COMMENT '响应体',
    status_code INT DEFAULT NULL COMMENT 'HTTP状态码',
    cost_time INT DEFAULT NULL COMMENT '耗时毫秒',
    call_status TINYINT DEFAULT 0 COMMENT '调用状态：0-失败 1-成功',
    error_msg TEXT DEFAULT NULL COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_api (api_id),
    INDEX idx_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口调用日志';

-- ============================================================
-- 5. 工作流引擎核心表
-- ============================================================

-- 流程定义
CREATE TABLE IF NOT EXISTS wf_flow_definition (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    flow_code VARCHAR(50) NOT NULL COMMENT '流程编码',
    flow_name VARCHAR(100) NOT NULL COMMENT '流程名称',
    version INT DEFAULT 1 COMMENT '版本号',
    item_code VARCHAR(50) DEFAULT NULL COMMENT '绑定的事项编码',
    trigger_type VARCHAR(20) DEFAULT 'cron' COMMENT '触发方式：cron-定时 event-事件 manual-手动',
    trigger_config VARCHAR(200) DEFAULT NULL COMMENT '触发配置（cron表达式或事件类型）',
    status TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-下线',
    graph_json MEDIUMTEXT DEFAULT NULL COMMENT '流程图JSON（LogicFlow格式）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    UNIQUE KEY uk_flow_code_ver (flow_code, version),
    INDEX idx_item (item_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程定义';

-- 流程节点
CREATE TABLE IF NOT EXISTS wf_flow_node (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    flow_id BIGINT NOT NULL COMMENT '所属流程定义ID',
    node_id VARCHAR(50) NOT NULL COMMENT '画布节点ID',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_type VARCHAR(30) NOT NULL COMMENT '节点类型：start/api/db/script/condition/timer/end',
    config_json TEXT DEFAULT NULL COMMENT '节点配置JSON',
    input_mapping TEXT DEFAULT NULL COMMENT '输入映射JSON',
    output_mapping TEXT DEFAULT NULL COMMENT '输出映射JSON',
    cron_expression VARCHAR(100) DEFAULT NULL COMMENT 'Cron表达式',
    timeout INT DEFAULT 30000 COMMENT '超时毫秒',
    retry_times TINYINT DEFAULT 3 COMMENT '重试次数',
    fail_strategy VARCHAR(20) DEFAULT 'suspend' COMMENT '失败策略：suspend-挂起 skip-跳过 retry-重试',
    sort_no INT DEFAULT 0 COMMENT '排序号',
    x_coordinate DECIMAL(10,2) DEFAULT NULL COMMENT 'X坐标',
    y_coordinate DECIMAL(10,2) DEFAULT NULL COMMENT 'Y坐标',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    UNIQUE KEY uk_flow_node (flow_id, node_id),
    INDEX idx_flow (flow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程节点';

-- 流程边
CREATE TABLE IF NOT EXISTS wf_flow_edge (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    flow_id BIGINT NOT NULL COMMENT '所属流程定义ID',
    edge_id VARCHAR(50) NOT NULL COMMENT '画布边ID',
    source_node VARCHAR(50) NOT NULL COMMENT '源节点ID',
    target_node VARCHAR(50) NOT NULL COMMENT '目标节点ID',
    condition_type VARCHAR(20) DEFAULT 'default' COMMENT '条件类型：default/success/fail/custom',
    condition_expression VARCHAR(500) DEFAULT NULL COMMENT '自定义条件表达式（SpEL）',
    priority INT DEFAULT 0 COMMENT '优先级',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
    UNIQUE KEY uk_flow_edge (flow_id, edge_id),
    INDEX idx_flow (flow_id),
    INDEX idx_source (source_node)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程边';

-- 流程实例
CREATE TABLE IF NOT EXISTS wf_flow_instance (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    flow_id BIGINT NOT NULL COMMENT '流程定义ID',
    flow_code VARCHAR(50) NOT NULL COMMENT '流程编码',
    business_key VARCHAR(100) DEFAULT NULL COMMENT '业务主键',
    status VARCHAR(20) DEFAULT 'running' COMMENT '状态：running/completed/suspended/failed/terminated',
    current_node_id VARCHAR(50) DEFAULT NULL COMMENT '当前节点ID',
    context_json MEDIUMTEXT DEFAULT NULL COMMENT '流程上下文JSON',
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_flow (flow_id),
    INDEX idx_business (business_key),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程实例';

-- 流程任务实例
CREATE TABLE IF NOT EXISTS wf_flow_task (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    node_id VARCHAR(50) NOT NULL COMMENT '节点ID',
    node_name VARCHAR(100) DEFAULT NULL COMMENT '节点名称',
    node_type VARCHAR(30) DEFAULT NULL COMMENT '节点类型',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending/running/success/fail/waiting/skipped',
    input_context MEDIUMTEXT DEFAULT NULL COMMENT '执行前上下文快照',
    output_context MEDIUMTEXT DEFAULT NULL COMMENT '执行后上下文快照',
    result_json MEDIUMTEXT DEFAULT NULL COMMENT '执行结果',
    error_msg TEXT DEFAULT NULL COMMENT '错误信息',
    execute_count INT DEFAULT 0 COMMENT '执行次数',
    next_execute_time DATETIME DEFAULT NULL COMMENT '下次执行时间',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_instance (instance_id),
    INDEX idx_status (status),
    INDEX idx_next_time (next_execute_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程任务实例';

-- 流程执行日志
CREATE TABLE IF NOT EXISTS wf_flow_log (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    task_id BIGINT DEFAULT NULL COMMENT '任务ID',
    node_id VARCHAR(50) DEFAULT NULL COMMENT '节点ID',
    log_type VARCHAR(20) DEFAULT NULL COMMENT '日志类型：start/execute/condition/transition/error',
    log_content TEXT DEFAULT NULL COMMENT '日志内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_instance (instance_id),
    INDEX idx_type (log_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程执行日志';

-- ============================================================
-- 6. 系统权限表（简化版）
-- ============================================================

-- 系统用户
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    avatar VARCHAR(200) DEFAULT NULL COMMENT '头像URL',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

-- 系统操作日志
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
    module VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    operation VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
    method VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    request_method VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
    request_url VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    request_params TEXT DEFAULT NULL COMMENT '请求参数',
    response_code INT DEFAULT NULL COMMENT '响应状态码',
    response_msg VARCHAR(500) DEFAULT NULL COMMENT '响应消息',
    ip VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    username VARCHAR(50) DEFAULT NULL COMMENT '操作用户',
    execute_time BIGINT DEFAULT NULL COMMENT '执行时长(ms)',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败 1-成功',
    error_msg TEXT DEFAULT NULL COMMENT '异常信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志';

SET FOREIGN_KEY_CHECKS = 1;
