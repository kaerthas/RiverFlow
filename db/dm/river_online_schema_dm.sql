-- 达梦 DM8 版（由 db/dm/river_online.sql 结构部分翻译而来）
-- 表结构：共 27 张表，对象名均带 RIVERFLOW. 模式前缀
-- 前提：已执行 db/dm/create_user_dm.sql 创建 RIVERFLOW 用户/模式
-- 提示：带模式前缀后对执行账号无要求（SYSDBA 或 RIVERFLOW 执行均可）

DROP TABLE IF EXISTS RIVERFLOW.com_business_notify;
CREATE TABLE RIVERFLOW.com_business_notify (
  business_id VARCHAR(64) NOT NULL,
  item_id VARCHAR(64) NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(200) NOT NULL,
  org_name VARCHAR(200) NOT NULL,
  org_code VARCHAR(64) NOT NULL,
  source VARCHAR(10) NOT NULL,
  region_code VARCHAR(20) NOT NULL,
  apply_subject VARCHAR(500) NOT NULL,
  type VARCHAR(20) NOT NULL,
  business_type VARCHAR(10) NOT NULL,
  base_info CLOB NULL,
  form_info CLOB NULL,
  material_info CLOB NULL,
  ems_info CLOB NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  receive_num VARCHAR(50) NULL,
  PRIMARY KEY (business_id)
);
COMMENT ON TABLE RIVERFLOW.com_business_notify IS '业务推送通知表';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.business_id IS '业务编码（事项唯一编码）';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.item_id IS '事项ID';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.item_code IS '事项编码';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.item_name IS '事项名称';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.org_name IS '部门名称';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.org_code IS '部门编码';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.source IS '业务来源：2-外网, 1-窗口';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.region_code IS '区划编码';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.apply_subject IS '业务主题';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.type IS '业务类型：Accept-新受理, Correct-补齐补正';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.business_type IS '申请对象类型：1-个人, 0-企业';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.base_info IS '基本信息（person/company嵌套对象）';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.form_info IS '表单信息';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.material_info IS '材料信息';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.ems_info IS '邮寄信息';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.com_business_notify.receive_num IS '申办流水号';
DROP INDEX IF EXISTS RIVERFLOW.idx_com_business_notify_item_code;
CREATE INDEX RIVERFLOW.idx_com_business_notify_item_code ON RIVERFLOW.com_business_notify(item_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_com_business_notify_org_code;
CREATE INDEX RIVERFLOW.idx_com_business_notify_org_code ON RIVERFLOW.com_business_notify(org_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_com_business_notify_region_code;
CREATE INDEX RIVERFLOW.idx_com_business_notify_region_code ON RIVERFLOW.com_business_notify(region_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_com_business_notify_source;
CREATE INDEX RIVERFLOW.idx_com_business_notify_source ON RIVERFLOW.com_business_notify(source);
DROP INDEX IF EXISTS RIVERFLOW.idx_com_business_notify_create_time;
CREATE INDEX RIVERFLOW.idx_com_business_notify_create_time ON RIVERFLOW.com_business_notify(create_time);

DROP TABLE IF EXISTS RIVERFLOW.gateway_route;
CREATE TABLE RIVERFLOW.gateway_route (
  id BIGINT NOT NULL,
  route_id VARCHAR(128) NOT NULL,
  uri VARCHAR(255) NOT NULL,
  predicates CLOB NOT NULL,
  filters CLOB NULL,
  route_order INT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  description VARCHAR(255) NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.gateway_route IS '网关动态路由表';
COMMENT ON COLUMN RIVERFLOW.gateway_route.id IS '主键';
COMMENT ON COLUMN RIVERFLOW.gateway_route.route_id IS '路由 ID，对应 Spring Cloud Gateway route id';
COMMENT ON COLUMN RIVERFLOW.gateway_route.uri IS '目标服务地址，支持 ${ENV:default} 占位符';
COMMENT ON COLUMN RIVERFLOW.gateway_route.predicates IS '断言配置数组，如 Path、Header 等';
COMMENT ON COLUMN RIVERFLOW.gateway_route.filters IS '过滤器配置数组，如 StripPrefix、RequestRateLimiter 等';
COMMENT ON COLUMN RIVERFLOW.gateway_route.route_order IS '优先级，数字越小越优先';
COMMENT ON COLUMN RIVERFLOW.gateway_route.enabled IS '是否启用：1 启用，0 禁用';
COMMENT ON COLUMN RIVERFLOW.gateway_route.description IS '路由说明';
DROP INDEX IF EXISTS RIVERFLOW.idx_gateway_route_uk_route_id;
CREATE UNIQUE INDEX RIVERFLOW.idx_gateway_route_uk_route_id ON RIVERFLOW.gateway_route(route_id);

DROP TABLE IF EXISTS RIVERFLOW.sys_operation_log;
CREATE TABLE RIVERFLOW.sys_operation_log (
  id BIGINT NOT NULL,
  module VARCHAR(50) NULL,
  operation VARCHAR(100) NULL,
  method VARCHAR(200) NULL,
  request_method VARCHAR(10) NULL,
  request_url VARCHAR(500) NULL,
  request_params CLOB NULL,
  response_code INT NULL,
  response_msg VARCHAR(500) NULL,
  ip VARCHAR(50) NULL,
  username VARCHAR(50) NULL,
  execute_time BIGINT NULL,
  status TINYINT DEFAULT 1,
  error_msg CLOB NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.sys_operation_log IS '系统操作日志';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.module IS '操作模块';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.operation IS '操作描述';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.method IS '请求方法';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.request_method IS 'HTTP方法';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.request_url IS '请求URL';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.request_params IS '请求参数';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.response_code IS '响应状态码';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.response_msg IS '响应消息';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.ip IS '操作IP';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.username IS '操作用户';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.execute_time IS '执行时长(ms)';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.status IS '状态：0-失败 1-成功';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.error_msg IS '异常信息';
COMMENT ON COLUMN RIVERFLOW.sys_operation_log.create_time IS '创建时间';

DROP TABLE IF EXISTS RIVERFLOW.sys_plugin;
CREATE TABLE RIVERFLOW.sys_plugin (
  id BIGINT NOT NULL,
  plugin_name VARCHAR(100) NOT NULL,
  plugin_type VARCHAR(50) NOT NULL,
  plugin_version VARCHAR(20) NULL,
  plugin_scope VARCHAR(20) DEFAULT 'node',
  category VARCHAR(50) NULL,
  description VARCHAR(500) NULL,
  jar_file VARCHAR(200) NOT NULL,
  jar_path VARCHAR(500) NOT NULL,
  file_size BIGINT NULL,
  icon VARCHAR(50) NULL,
  status VARCHAR(20) DEFAULT 'enabled',
  loaded TINYINT DEFAULT 0,
  config_template CLOB NULL,
  author VARCHAR(100) NULL,
  website VARCHAR(200) NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(50) NULL,
  update_by VARCHAR(50) NULL,
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.sys_plugin IS '插件管理表';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.plugin_name IS '插件名称';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.plugin_type IS '插件类型标识';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.plugin_version IS '插件版本';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.plugin_scope IS '插件作用域：node-流程节点 api-接口注册 both-两者皆可';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.category IS '插件分类';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.description IS '插件描述';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.jar_file IS 'JAR包文件名';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.jar_path IS 'JAR包存储路径';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.file_size IS '文件大小（字节）';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.icon IS '图标';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.status IS '状态：enabled/disabled';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.loaded IS '是否已加载';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.config_template IS '配置模板JSON';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.author IS '作者';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.website IS '官网/文档地址';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.sys_plugin.del_flag IS '删除标记';
DROP INDEX IF EXISTS RIVERFLOW.idx_sys_plugin_uk_plugin_type;
CREATE UNIQUE INDEX RIVERFLOW.idx_sys_plugin_uk_plugin_type ON RIVERFLOW.sys_plugin(plugin_type);
DROP INDEX IF EXISTS RIVERFLOW.idx_sys_plugin_status;
CREATE INDEX RIVERFLOW.idx_sys_plugin_status ON RIVERFLOW.sys_plugin(status);
DROP INDEX IF EXISTS RIVERFLOW.idx_sys_plugin_category;
CREATE INDEX RIVERFLOW.idx_sys_plugin_category ON RIVERFLOW.sys_plugin(category);
DROP INDEX IF EXISTS RIVERFLOW.idx_sys_plugin_scope;
CREATE INDEX RIVERFLOW.idx_sys_plugin_scope ON RIVERFLOW.sys_plugin(plugin_scope);

DROP TABLE IF EXISTS RIVERFLOW.sys_user;
CREATE TABLE RIVERFLOW.sys_user (
  id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(50) NULL,
  avatar VARCHAR(200) NULL,
  email VARCHAR(100) NULL,
  phone VARCHAR(20) NULL,
  status TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.sys_user IS '系统用户';
COMMENT ON COLUMN RIVERFLOW.sys_user.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.sys_user.username IS '用户名';
COMMENT ON COLUMN RIVERFLOW.sys_user.password IS '密码（BCrypt加密）';
COMMENT ON COLUMN RIVERFLOW.sys_user.real_name IS '真实姓名';
COMMENT ON COLUMN RIVERFLOW.sys_user.avatar IS '头像URL';
COMMENT ON COLUMN RIVERFLOW.sys_user.email IS '邮箱';
COMMENT ON COLUMN RIVERFLOW.sys_user.phone IS '手机号';
COMMENT ON COLUMN RIVERFLOW.sys_user.status IS '状态：0-停用 1-启用';
COMMENT ON COLUMN RIVERFLOW.sys_user.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.sys_user.update_time IS '更新时间';
DROP INDEX IF EXISTS RIVERFLOW.idx_sys_user_uk_username;
CREATE UNIQUE INDEX RIVERFLOW.idx_sys_user_uk_username ON RIVERFLOW.sys_user(username);

DROP TABLE IF EXISTS RIVERFLOW.t_business_info;
CREATE TABLE RIVERFLOW.t_business_info (
  business_id VARCHAR(50) NOT NULL,
  PRIMARY KEY (business_id)
);
COMMENT ON TABLE RIVERFLOW.t_business_info IS '业务申报表';
COMMENT ON COLUMN RIVERFLOW.t_business_info.business_id IS '业务主键';

DROP TABLE IF EXISTS RIVERFLOW.t_collect_social;
CREATE TABLE RIVERFLOW.t_collect_social (
  id BIGINT NOT NULL,
  batch_no VARCHAR(50) NOT NULL,
  enterprise_uscc VARCHAR(18) NOT NULL,
  enterprise_name VARCHAR(200) NULL,
  stat_month CHAR(6) NOT NULL,
  data_kind VARCHAR(20) NOT NULL,
  insure_count INT NULL,
  paid_until CHAR(6) NULL,
  collect_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.t_collect_social IS '社保/医保/公积金月度归集数据表';
COMMENT ON COLUMN RIVERFLOW.t_collect_social.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.t_collect_social.batch_no IS '归集批次号，如 202608-SOCIAL';
COMMENT ON COLUMN RIVERFLOW.t_collect_social.enterprise_uscc IS '统一社会信用代码';
COMMENT ON COLUMN RIVERFLOW.t_collect_social.enterprise_name IS '单位名称（冗余，便于核对）';
COMMENT ON COLUMN RIVERFLOW.t_collect_social.stat_month IS '数据所属月份 yyyyMM（归集当期）';
COMMENT ON COLUMN RIVERFLOW.t_collect_social.data_kind IS '数据类别：SOCIAL社保/MEDICAL医保/HOUSING公积金';
COMMENT ON COLUMN RIVERFLOW.t_collect_social.insure_count IS '最后缴存/参保人数';
COMMENT ON COLUMN RIVERFLOW.t_collect_social.paid_until IS '缴至年月 yyyyMM，如 202604';
COMMENT ON COLUMN RIVERFLOW.t_collect_social.collect_time IS '归集时间';
COMMENT ON COLUMN RIVERFLOW.t_collect_social.deleted IS '逻辑删除：0 正常，1 删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_t_collect_social_uk_data;
CREATE UNIQUE INDEX RIVERFLOW.idx_t_collect_social_uk_data ON RIVERFLOW.t_collect_social(data_kind, stat_month, enterprise_uscc);
DROP INDEX IF EXISTS RIVERFLOW.idx_t_collect_social_uscc;
CREATE INDEX RIVERFLOW.idx_t_collect_social_uscc ON RIVERFLOW.t_collect_social(enterprise_uscc);
DROP INDEX IF EXISTS RIVERFLOW.idx_t_collect_social_batch;
CREATE INDEX RIVERFLOW.idx_t_collect_social_batch ON RIVERFLOW.t_collect_social(batch_no);

DROP TABLE IF EXISTS RIVERFLOW.t_mid_housing_fund;
CREATE TABLE RIVERFLOW.t_mid_housing_fund (
  id BIGINT NOT NULL,
  统计月份 VARCHAR(6) NOT NULL,
  统一社会信用代码 VARCHAR(18) NOT NULL,
  单位名称 VARCHAR(200) NOT NULL,
  最后缴存人数 INT NULL,
  缴至年月 VARCHAR(6) NULL,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.t_mid_housing_fund IS '公积金中心中间库-单位月度缴存表';
COMMENT ON COLUMN RIVERFLOW.t_mid_housing_fund.id IS '主键';
COMMENT ON COLUMN RIVERFLOW.t_mid_housing_fund.统计月份 IS '数据所属月份yyyyMM';
COMMENT ON COLUMN RIVERFLOW.t_mid_housing_fund.统一社会信用代码 IS '统一社会信用代码';
COMMENT ON COLUMN RIVERFLOW.t_mid_housing_fund.单位名称 IS '单位名称';
COMMENT ON COLUMN RIVERFLOW.t_mid_housing_fund.最后缴存人数 IS '缴存人数';
COMMENT ON COLUMN RIVERFLOW.t_mid_housing_fund.缴至年月 IS '费用缴至年月yyyyMM，早于统计月份即欠缴';
DROP INDEX IF EXISTS RIVERFLOW.idx_t_mid_housing_fund_uk_uscc_month;
CREATE UNIQUE INDEX RIVERFLOW.idx_t_mid_housing_fund_uk_uscc_month ON RIVERFLOW.t_mid_housing_fund(统一社会信用代码, 统计月份);

DROP TABLE IF EXISTS RIVERFLOW.third_project_data;
CREATE TABLE RIVERFLOW.third_project_data (
  id VARCHAR(255) NOT NULL,
  project_no VARCHAR(64) NOT NULL,
  item_name VARCHAR(256) NULL,
  item_code VARCHAR(64) NULL,
  org_name VARCHAR(128) NULL,
  org_code VARCHAR(64) NULL,
  reg_name VARCHAR(128) NULL,
  reg_code VARCHAR(64) NULL,
  applyer_name VARCHAR(128) NULL,
  applyer_type VARCHAR(32) NULL,
  project_name VARCHAR(512) NULL,
  source VARCHAR(32) NULL,
  apply_time TIMESTAMP NULL,
  form_info CLOB NULL,
  form_schema_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  v_id_card VARCHAR(128),
  v_nian_fen VARCHAR(256),
  v_level VARCHAR(64),
  v_type VARCHAR(32),
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.third_project_data IS '审批推送申报信息表';
COMMENT ON COLUMN RIVERFLOW.third_project_data.id IS '主键';
COMMENT ON COLUMN RIVERFLOW.third_project_data.project_no IS '办件编号';
COMMENT ON COLUMN RIVERFLOW.third_project_data.item_name IS '事项名称';
COMMENT ON COLUMN RIVERFLOW.third_project_data.item_code IS '事项编码';
COMMENT ON COLUMN RIVERFLOW.third_project_data.org_name IS '组织机构名称';
COMMENT ON COLUMN RIVERFLOW.third_project_data.org_code IS '组织机构代码';
COMMENT ON COLUMN RIVERFLOW.third_project_data.reg_name IS '区划名称';
COMMENT ON COLUMN RIVERFLOW.third_project_data.reg_code IS '区划编码';
COMMENT ON COLUMN RIVERFLOW.third_project_data.applyer_name IS '申请人姓名';
COMMENT ON COLUMN RIVERFLOW.third_project_data.applyer_type IS '申请人类型';
COMMENT ON COLUMN RIVERFLOW.third_project_data.project_name IS '项目名称';
COMMENT ON COLUMN RIVERFLOW.third_project_data.source IS '来源';
COMMENT ON COLUMN RIVERFLOW.third_project_data.apply_time IS '申请时间';
COMMENT ON COLUMN RIVERFLOW.third_project_data.form_info IS '动态表单信息';
COMMENT ON COLUMN RIVERFLOW.third_project_data.form_schema_id IS '动态表单映射主键';
COMMENT ON COLUMN RIVERFLOW.third_project_data.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.third_project_data.update_time IS '修改时间';
COMMENT ON COLUMN RIVERFLOW.third_project_data.del_flag IS '是否作废0正常，1作废';
DROP INDEX IF EXISTS RIVERFLOW.idx_third_project_data_v_id_card;
CREATE INDEX RIVERFLOW.idx_third_project_data_v_id_card ON RIVERFLOW.third_project_data(v_id_card);
DROP INDEX IF EXISTS RIVERFLOW.idx_third_project_data_v_level;
CREATE INDEX RIVERFLOW.idx_third_project_data_v_level ON RIVERFLOW.third_project_data(v_level);
DROP INDEX IF EXISTS RIVERFLOW.idx_third_project_data_v_nian_fen;
CREATE INDEX RIVERFLOW.idx_third_project_data_v_nian_fen ON RIVERFLOW.third_project_data(v_nian_fen);
DROP INDEX IF EXISTS RIVERFLOW.idx_third_project_data_v_type;
CREATE INDEX RIVERFLOW.idx_third_project_data_v_type ON RIVERFLOW.third_project_data(v_type);
-- 注意：列 v_id_card, v_nian_fen, v_level, v_type 原为 MySQL JSON 生成列（自 form_info 提取），
-- 达梦不支持生成列，已降级为普通列；如需取值请在应用层解析 form_info

DROP TABLE IF EXISTS RIVERFLOW.wf_api_app;
CREATE TABLE RIVERFLOW.wf_api_app (
  id BIGINT NOT NULL,
  app_code VARCHAR(50) NOT NULL,
  app_name VARCHAR(100) NOT NULL,
  app_key VARCHAR(64) NULL,
  app_secret VARCHAR(128) NULL,
  description VARCHAR(500) NULL,
  icon VARCHAR(50) NULL,
  sort_no INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) NULL,
  update_by VARCHAR(64) NULL,
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_api_app IS '接口应用/目录';
COMMENT ON COLUMN RIVERFLOW.wf_api_app.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_app.app_code IS '应用编码';
COMMENT ON COLUMN RIVERFLOW.wf_api_app.app_name IS '应用名称';
COMMENT ON COLUMN RIVERFLOW.wf_api_app.app_key IS '应用标识（AK），开放接口调用方标识';
COMMENT ON COLUMN RIVERFLOW.wf_api_app.app_secret IS '应用密钥（SK），仅服务端与调用方持有';
COMMENT ON COLUMN RIVERFLOW.wf_api_app.description IS '应用描述';
COMMENT ON COLUMN RIVERFLOW.wf_api_app.icon IS '应用图标';
COMMENT ON COLUMN RIVERFLOW.wf_api_app.sort_no IS '排序号';
COMMENT ON COLUMN RIVERFLOW.wf_api_app.status IS '0-禁用 1-启用';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_app_uk_app_code;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_api_app_uk_app_code ON RIVERFLOW.wf_api_app(app_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_app_uk_app_key;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_api_app_uk_app_key ON RIVERFLOW.wf_api_app(app_key);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_app_status;
CREATE INDEX RIVERFLOW.idx_wf_api_app_status ON RIVERFLOW.wf_api_app(status);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_app_sort_no;
CREATE INDEX RIVERFLOW.idx_wf_api_app_sort_no ON RIVERFLOW.wf_api_app(sort_no);

DROP TABLE IF EXISTS RIVERFLOW.wf_api_call_log;
CREATE TABLE RIVERFLOW.wf_api_call_log (
  id BIGINT NOT NULL,
  api_id BIGINT NOT NULL,
  api_code VARCHAR(50) NOT NULL,
  source VARCHAR(20) DEFAULT 'openapi',
  request_url VARCHAR(500) NULL,
  request_method VARCHAR(10) NULL,
  request_headers CLOB NULL,
  request_body CLOB NULL,
  response_body CLOB NULL,
  status_code INT NULL,
  cost_time INT NULL,
  call_status TINYINT DEFAULT 0,
  error_msg CLOB NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_api_call_log IS '接口调用日志';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.api_id IS '接口ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.api_code IS '接口编码';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.source IS '日志来源：openapi-开放接口调用，flow-流程API节点调用';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.request_url IS '请求URL';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.request_method IS '请求方式';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.request_headers IS '请求头JSON';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.request_body IS '请求体';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.response_body IS '响应体';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.status_code IS 'HTTP状态码';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.cost_time IS '耗时毫秒';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.call_status IS '调用状态：0-失败 1-成功';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.error_msg IS '错误信息';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_api_call_log.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_call_log_api;
CREATE INDEX RIVERFLOW.idx_wf_api_call_log_api ON RIVERFLOW.wf_api_call_log(api_id);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_call_log_time;
CREATE INDEX RIVERFLOW.idx_wf_api_call_log_time ON RIVERFLOW.wf_api_call_log(create_time);

DROP TABLE IF EXISTS RIVERFLOW.wf_api_catalog;
CREATE TABLE RIVERFLOW.wf_api_catalog (
  id BIGINT NOT NULL,
  app_id BIGINT NULL,
  api_code VARCHAR(50) NOT NULL,
  api_name VARCHAR(100) NOT NULL,
  api_type VARCHAR(20) NOT NULL,
  method VARCHAR(10) DEFAULT 'POST',
  open_method VARCHAR(10) DEFAULT 'POST',
  url VARCHAR(1000) NULL,
  open_path VARCHAR(200) NULL,
  content_type VARCHAR(50) DEFAULT 'application/json',
  auth_type VARCHAR(20) DEFAULT 'none',
  allowed_ips VARCHAR(500) NULL,
  ds_id BIGINT NULL,
  script_id BIGINT NULL,
  plugin_type VARCHAR(50) NULL,
  timeout INT DEFAULT 30000,
  retry_times TINYINT DEFAULT 0,
  proxy_enabled TINYINT DEFAULT 0,
  proxy_host VARCHAR(200) NULL,
  proxy_port INT NULL,
  status TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  trigger_enabled TINYINT DEFAULT 0,
  trigger_flow_id BIGINT NULL,
  trigger_flow_code VARCHAR(50) NULL,
  trigger_biz_key_field VARCHAR(64) NULL,
  success_code VARCHAR(100) DEFAULT '200',
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_api_catalog IS '接口目录';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.app_id IS '所属应用ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.api_code IS '接口编码';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.api_name IS '接口名称';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.api_type IS '接口类型：proxy-代理 sql-SQL服务 data-数据服务 script-脚本服务';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.method IS '请求方式：GET/POST/PUT/DELETE';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.open_method IS '代理后请求方式：GET/POST/PUT/DELETE';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.url IS '请求地址';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.open_path IS '代理后暴露路径，如 /user/list';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.content_type IS '请求体类型';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.auth_type IS '认证方式：none/basic/token/oauth2';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.allowed_ips IS '调用方 IP 白名单，多个用逗号分隔，支持 CIDR，如 10.0.0.0/24,192.168.1.10';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.ds_id IS 'SQL类型时绑定的数据源ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.script_id IS '脚本类型时绑定的脚本ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.plugin_type IS '插件类型标识，api_type=plugin 时生效';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.timeout IS '超时毫秒';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.retry_times IS '重试次数';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.proxy_enabled IS '是否启用代理：0-否 1-是';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.proxy_host IS '代理主机';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.proxy_port IS '代理端口';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.status IS '状态：0-草稿 1-已发布 2-下线';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.del_flag IS '删除标志：0-正常 1-已删除';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.trigger_enabled IS '是否启用流程触发：0-否 1-是';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.trigger_flow_id IS '执行成功后触发的流程定义ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.trigger_flow_code IS '触发流程编码（绑定编码，自动取最新发布版本）';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.trigger_biz_key_field IS '从请求参数中提取业务主键的字段名';
COMMENT ON COLUMN RIVERFLOW.wf_api_catalog.success_code IS '业务成功状态码，多个用逗号分隔，如 200,0,1';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_catalog_uk_api_code;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_api_catalog_uk_api_code ON RIVERFLOW.wf_api_catalog(api_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_catalog_uk_open_path_method;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_api_catalog_uk_open_path_method ON RIVERFLOW.wf_api_catalog(open_path, open_method);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_catalog_status;
CREATE INDEX RIVERFLOW.idx_wf_api_catalog_status ON RIVERFLOW.wf_api_catalog(status);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_catalog_plugin_type;
CREATE INDEX RIVERFLOW.idx_wf_api_catalog_plugin_type ON RIVERFLOW.wf_api_catalog(plugin_type);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_catalog_app_id;
CREATE INDEX RIVERFLOW.idx_wf_api_catalog_app_id ON RIVERFLOW.wf_api_catalog(app_id);

DROP TABLE IF EXISTS RIVERFLOW.wf_api_param;
CREATE TABLE RIVERFLOW.wf_api_param (
  id BIGINT NOT NULL,
  api_id BIGINT NOT NULL,
  param_type VARCHAR(20) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  param_key VARCHAR(100) NOT NULL,
  param_name VARCHAR(100) NULL,
  data_type VARCHAR(20) DEFAULT 'string',
  is_required TINYINT DEFAULT 0,
  default_value VARCHAR(500) NULL,
  sort_no INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_api_param IS '接口参数定义';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.api_id IS '所属接口ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.param_type IS '参数类型：header/query/body/response';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.parent_id IS '父参数ID，支持嵌套';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.param_key IS '参数键';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.param_name IS '参数名称';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.data_type IS '数据类型：string/int/object/array';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.is_required IS '是否必填：0-否 1-是';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.default_value IS '默认值';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.sort_no IS '排序号';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_api_param.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_param_api_type;
CREATE INDEX RIVERFLOW.idx_wf_api_param_api_type ON RIVERFLOW.wf_api_param(api_id, param_type);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_param_parent;
CREATE INDEX RIVERFLOW.idx_wf_api_param_parent ON RIVERFLOW.wf_api_param(parent_id);

DROP TABLE IF EXISTS RIVERFLOW.wf_api_script;
CREATE TABLE RIVERFLOW.wf_api_script (
  id BIGINT NOT NULL,
  script_code VARCHAR(50) NOT NULL,
  script_name VARCHAR(100) NOT NULL,
  script_type VARCHAR(20) NOT NULL,
  script_content CLOB NULL,
  params VARCHAR(500) NULL,
  status TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_api_script IS '接口脚本库';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.script_code IS '脚本编码';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.script_name IS '脚本名称';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.script_type IS '脚本类型：format-格式化 header-请求头 result-结果处理 condition-条件判断';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.script_content IS 'Groovy脚本内容';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.params IS '脚本入参定义JSON';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.status IS '状态：0-停用 1-启用';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_api_script.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_script_uk_script_code;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_api_script_uk_script_code ON RIVERFLOW.wf_api_script(script_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_api_script_type;
CREATE INDEX RIVERFLOW.idx_wf_api_script_type ON RIVERFLOW.wf_api_script(script_type);

DROP TABLE IF EXISTS RIVERFLOW.wf_datasource;
CREATE TABLE RIVERFLOW.wf_datasource (
  id BIGINT NOT NULL,
  ds_code VARCHAR(50) NOT NULL,
  ds_name VARCHAR(100) NOT NULL,
  db_type VARCHAR(20) NOT NULL,
  url VARCHAR(500) NOT NULL,
  username VARCHAR(100) NOT NULL,
  password VARCHAR(200) NOT NULL,
  driver_class VARCHAR(200) NULL,
  driver_jar_path VARCHAR(500) NULL,
  status TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_datasource IS '动态数据源配置';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.ds_code IS '数据源编码';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.ds_name IS '数据源名称';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.db_type IS '数据库类型：mysql/oracle/sqlserver/postgresql';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.url IS '连接URL';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.username IS '用户名';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.password IS '密码（Jasypt加密）';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.driver_class IS '驱动类名';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.driver_jar_path IS '驱动JAR包路径（自定义驱动时使用）';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.status IS '状态：0-停用 1-启用';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_datasource.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_datasource_uk_ds_code;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_datasource_uk_ds_code ON RIVERFLOW.wf_datasource(ds_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_datasource_status;
CREATE INDEX RIVERFLOW.idx_wf_datasource_status ON RIVERFLOW.wf_datasource(status);

DROP TABLE IF EXISTS RIVERFLOW.wf_dynamic_table;
CREATE TABLE RIVERFLOW.wf_dynamic_table (
  id BIGINT NOT NULL,
  table_code VARCHAR(50) NOT NULL,
  table_name VARCHAR(100) NOT NULL,
  ds_id BIGINT DEFAULT 0,
  remark VARCHAR(500) NULL,
  status TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_dynamic_table IS '动态表定义';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.table_code IS '表编码（英文）';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.table_name IS '表名称（中文）';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.ds_id IS '所属数据源ID，0表示主库';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.remark IS '备注';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.status IS '状态：0-停用 1-启用';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_dynamic_table_uk_table_code;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_dynamic_table_uk_table_code ON RIVERFLOW.wf_dynamic_table(table_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_dynamic_table_ds;
CREATE INDEX RIVERFLOW.idx_wf_dynamic_table_ds ON RIVERFLOW.wf_dynamic_table(ds_id);

DROP TABLE IF EXISTS RIVERFLOW.wf_dynamic_table_column;
CREATE TABLE RIVERFLOW.wf_dynamic_table_column (
  id BIGINT NOT NULL,
  table_id BIGINT NOT NULL,
  column_code VARCHAR(50) NOT NULL,
  column_name VARCHAR(100) NOT NULL,
  data_type VARCHAR(20) NOT NULL,
  length INT NULL,
  decimal_scale INT NULL,
  is_pk TINYINT DEFAULT 0,
  is_required TINYINT DEFAULT 0,
  is_index TINYINT DEFAULT 0,
  default_value VARCHAR(200) NULL,
  sort_no INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_dynamic_table_column IS '动态表字段定义';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.table_id IS '所属表ID';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.column_code IS '字段编码';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.column_name IS '字段名称';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.data_type IS '数据类型：varchar/int/bigint/datetime/text/decimal等';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.length IS '长度';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.decimal_scale IS '小数位';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.is_pk IS '是否主键：0-否 1-是';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.is_required IS '是否必填：0-否 1-是';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.is_index IS '是否索引：0-否 1-是';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.default_value IS '默认值';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.sort_no IS '排序号';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_dynamic_table_column.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_dynamic_table_column_uk_table_column;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_dynamic_table_column_uk_table_column ON RIVERFLOW.wf_dynamic_table_column(table_id, column_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_dynamic_table_column_table;
CREATE INDEX RIVERFLOW.idx_wf_dynamic_table_column_table ON RIVERFLOW.wf_dynamic_table_column(table_id);

DROP TABLE IF EXISTS RIVERFLOW.wf_flow_definition;
CREATE TABLE RIVERFLOW.wf_flow_definition (
  id BIGINT NOT NULL,
  flow_code VARCHAR(50) NOT NULL,
  flow_name VARCHAR(100) NOT NULL,
  version INT DEFAULT 1,
  item_code VARCHAR(50) NULL,
  trigger_type VARCHAR(20) DEFAULT 'cron',
  trigger_config VARCHAR(200) NULL,
  cron_enabled TINYINT NOT NULL DEFAULT 0,
  status TINYINT DEFAULT 0,
  graph_json CLOB NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  execution_mode VARCHAR(20) NOT NULL DEFAULT 'ASYNC',
  input_params CLOB NULL,
  output_params CLOB NULL,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_flow_definition IS '流程定义';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.flow_code IS '流程编码';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.flow_name IS '流程名称';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.version IS '版本号';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.item_code IS '绑定的事项编码';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.trigger_type IS '触发方式：cron-定时 event-事件 manual-手动';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.trigger_config IS '触发配置（cron表达式或事件类型）';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.cron_enabled IS '定时触发开关：0-停用(默认) 1-启用';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.status IS '状态：0-草稿 1-已发布 2-下线';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.graph_json IS '流程图JSON（LogicFlow格式）';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.del_flag IS '删除标志：0-正常 1-已删除';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.execution_mode IS '执行模式：ASYNC-异步(默认) SYNC-同步';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.input_params IS '同步流程默认入参(JSON)，启动时自动注入上下文';
COMMENT ON COLUMN RIVERFLOW.wf_flow_definition.output_params IS '同步流程输出参数(JSON)，用于声明流程返回结果结构';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_definition_uk_flow_code_ver;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_flow_definition_uk_flow_code_ver ON RIVERFLOW.wf_flow_definition(flow_code, version);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_definition_item;
CREATE INDEX RIVERFLOW.idx_wf_flow_definition_item ON RIVERFLOW.wf_flow_definition(item_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_definition_status;
CREATE INDEX RIVERFLOW.idx_wf_flow_definition_status ON RIVERFLOW.wf_flow_definition(status);

DROP TABLE IF EXISTS RIVERFLOW.wf_flow_edge;
CREATE TABLE RIVERFLOW.wf_flow_edge (
  id BIGINT NOT NULL,
  flow_id BIGINT NOT NULL,
  edge_id VARCHAR(50) NOT NULL,
  source_node VARCHAR(50) NOT NULL,
  target_node VARCHAR(50) NOT NULL,
  condition_type VARCHAR(20) DEFAULT 'default',
  condition_expression VARCHAR(500) NULL,
  priority INT DEFAULT 0,
  is_hidden TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_flow_edge IS '流程边';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.flow_id IS '所属流程定义ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.edge_id IS '画布边ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.source_node IS '源节点ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.target_node IS '目标节点ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.condition_type IS '条件类型：default/success/fail/custom';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.condition_expression IS '自定义条件表达式（SpEL）';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.priority IS '优先级';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.is_hidden IS '画布上是否隐藏（如循环回跳边）：0-显示 1-隐藏';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_edge.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_edge_uk_flow_edge;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_flow_edge_uk_flow_edge ON RIVERFLOW.wf_flow_edge(flow_id, edge_id);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_edge_flow;
CREATE INDEX RIVERFLOW.idx_wf_flow_edge_flow ON RIVERFLOW.wf_flow_edge(flow_id);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_edge_source;
CREATE INDEX RIVERFLOW.idx_wf_flow_edge_source ON RIVERFLOW.wf_flow_edge(source_node);

DROP TABLE IF EXISTS RIVERFLOW.wf_flow_instance;
CREATE TABLE RIVERFLOW.wf_flow_instance (
  id BIGINT NOT NULL,
  flow_id BIGINT NOT NULL,
  flow_code VARCHAR(50) NOT NULL,
  version INT DEFAULT 1,
  business_key VARCHAR(100) NULL,
  status VARCHAR(20) DEFAULT 'running',
  current_node_id VARCHAR(50) NULL,
  context_json CLOB NULL,
  start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  end_time TIMESTAMP NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_flow_instance IS '流程实例';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.flow_id IS '流程定义ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.flow_code IS '流程编码';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.version IS '流程版本号';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.business_key IS '业务主键';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.status IS '状态：running/completed/suspended/failed/terminated';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.current_node_id IS '当前节点ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.context_json IS '流程上下文JSON';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.start_time IS '开始时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.end_time IS '结束时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_instance.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_instance_flow;
CREATE INDEX RIVERFLOW.idx_wf_flow_instance_flow ON RIVERFLOW.wf_flow_instance(flow_id);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_instance_business;
CREATE INDEX RIVERFLOW.idx_wf_flow_instance_business ON RIVERFLOW.wf_flow_instance(business_key);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_instance_status;
CREATE INDEX RIVERFLOW.idx_wf_flow_instance_status ON RIVERFLOW.wf_flow_instance(status);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_instance_flow_ver;
CREATE INDEX RIVERFLOW.idx_wf_flow_instance_flow_ver ON RIVERFLOW.wf_flow_instance(flow_code, version);

DROP TABLE IF EXISTS RIVERFLOW.wf_flow_log;
CREATE TABLE RIVERFLOW.wf_flow_log (
  id BIGINT NOT NULL,
  instance_id BIGINT NOT NULL,
  task_id BIGINT NULL,
  node_id VARCHAR(50) NULL,
  log_type VARCHAR(20) NULL,
  log_content CLOB NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_flow_log IS '流程执行日志';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.instance_id IS '流程实例ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.task_id IS '任务ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.node_id IS '节点ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.log_type IS '日志类型：start/execute/condition/transition/error';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.log_content IS '日志内容';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_log.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_log_instance;
CREATE INDEX RIVERFLOW.idx_wf_flow_log_instance ON RIVERFLOW.wf_flow_log(instance_id);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_log_type;
CREATE INDEX RIVERFLOW.idx_wf_flow_log_type ON RIVERFLOW.wf_flow_log(log_type);

DROP TABLE IF EXISTS RIVERFLOW.wf_flow_node;
CREATE TABLE RIVERFLOW.wf_flow_node (
  id BIGINT NOT NULL,
  flow_id BIGINT NOT NULL,
  node_id VARCHAR(50) NOT NULL,
  node_name VARCHAR(100) NOT NULL,
  node_type VARCHAR(30) NOT NULL,
  config_json CLOB NULL,
  input_mapping CLOB NULL,
  output_mapping CLOB NULL,
  cron_expression VARCHAR(100) NULL,
  timeout INT DEFAULT 30000,
  retry_times TINYINT DEFAULT 3,
  fail_strategy VARCHAR(20) DEFAULT 'suspend',
  sort_no INT DEFAULT 0,
  x_coordinate DECIMAL(10, 2) NULL,
  y_coordinate DECIMAL(10, 2) NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_flow_node IS '流程节点';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.flow_id IS '所属流程定义ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.node_id IS '画布节点ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.node_name IS '节点名称';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.node_type IS '节点类型：start/api/db/script/condition/timer/end';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.config_json IS '节点配置JSON';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.input_mapping IS '输入映射JSON';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.output_mapping IS '输出映射JSON';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.cron_expression IS 'Cron表达式';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.timeout IS '超时毫秒';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.retry_times IS '重试次数';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.fail_strategy IS '失败策略：suspend-挂起 skip-跳过 retry-重试';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.sort_no IS '排序号';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.x_coordinate IS 'X坐标';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.y_coordinate IS 'Y坐标';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_node.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_node_uk_flow_node;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_flow_node_uk_flow_node ON RIVERFLOW.wf_flow_node(flow_id, node_id);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_node_flow;
CREATE INDEX RIVERFLOW.idx_wf_flow_node_flow ON RIVERFLOW.wf_flow_node(flow_id);

DROP TABLE IF EXISTS RIVERFLOW.wf_flow_task;
CREATE TABLE RIVERFLOW.wf_flow_task (
  id BIGINT NOT NULL,
  instance_id BIGINT NOT NULL,
  node_id VARCHAR(50) NOT NULL,
  node_name VARCHAR(100) NULL,
  loop_node_id VARCHAR(50) NULL,
  iteration_index INT NULL,
  is_loop_internal TINYINT DEFAULT 0,
  task_type VARCHAR(30) DEFAULT 'NODE',
  batch_no VARCHAR(64) NULL,
  node_type VARCHAR(30) NULL,
  status VARCHAR(20) DEFAULT 'pending',
  input_context CLOB NULL,
  output_context CLOB NULL,
  result_json CLOB NULL,
  error_msg CLOB NULL,
  execute_count INT DEFAULT 0,
  next_execute_time TIMESTAMP NULL,
  start_time TIMESTAMP NULL,
  end_time TIMESTAMP NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_flow_task IS '流程任务实例';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.instance_id IS '流程实例ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.node_id IS '节点ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.node_name IS '节点名称';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.loop_node_id IS '所属循环节点ID';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.iteration_index IS '循环迭代下标';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.is_loop_internal IS '是否循环体内部任务：0-否 1-是';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.task_type IS '任务类型：NODE/LOOP_ITERATION/LOOP_AGGREGATE';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.batch_no IS '并行循环批次号';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.node_type IS '节点类型';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.status IS '状态：pending/running/success/fail/waiting/skipped';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.input_context IS '执行前上下文快照';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.output_context IS '执行后上下文快照';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.result_json IS '执行结果';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.error_msg IS '错误信息';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.execute_count IS '执行次数';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.next_execute_time IS '下次执行时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.start_time IS '开始时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.end_time IS '结束时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_flow_task.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_task_instance;
CREATE INDEX RIVERFLOW.idx_wf_flow_task_instance ON RIVERFLOW.wf_flow_task(instance_id);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_task_status;
CREATE INDEX RIVERFLOW.idx_wf_flow_task_status ON RIVERFLOW.wf_flow_task(status);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_flow_task_next_time;
CREATE INDEX RIVERFLOW.idx_wf_flow_task_next_time ON RIVERFLOW.wf_flow_task(next_execute_time, status);

DROP TABLE IF EXISTS RIVERFLOW.wf_item;
CREATE TABLE RIVERFLOW.wf_item (
  id BIGINT NOT NULL,
  item_code VARCHAR(50) NOT NULL,
  item_name VARCHAR(200) NOT NULL,
  region_code VARCHAR(20) NOT NULL,
  region_name VARCHAR(100) NULL,
  catalog_code VARCHAR(50) NULL,
  task_code VARCHAR(50) NULL,
  task_handle_item VARCHAR(50) NULL,
  service_obj TINYINT DEFAULT 0,
  flow_id BIGINT NULL,
  flow_code VARCHAR(50) NULL,
  status TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_item IS '政务服务事项';
COMMENT ON COLUMN RIVERFLOW.wf_item.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_item.item_code IS '事项编码';
COMMENT ON COLUMN RIVERFLOW.wf_item.item_name IS '事项名称';
COMMENT ON COLUMN RIVERFLOW.wf_item.region_code IS '区划代码';
COMMENT ON COLUMN RIVERFLOW.wf_item.region_name IS '区划名称';
COMMENT ON COLUMN RIVERFLOW.wf_item.catalog_code IS '国家基本编码';
COMMENT ON COLUMN RIVERFLOW.wf_item.task_code IS '国家实施编码';
COMMENT ON COLUMN RIVERFLOW.wf_item.task_handle_item IS '国家业务办理项编码';
COMMENT ON COLUMN RIVERFLOW.wf_item.service_obj IS '办理对象：0-个人 1-法人';
COMMENT ON COLUMN RIVERFLOW.wf_item.flow_id IS '绑定流程定义ID';
COMMENT ON COLUMN RIVERFLOW.wf_item.flow_code IS '流程编码';
COMMENT ON COLUMN RIVERFLOW.wf_item.status IS '状态：0-停用 1-启用';
COMMENT ON COLUMN RIVERFLOW.wf_item.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_item.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_item.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_item.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_item.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_item_uk_item_code;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_item_uk_item_code ON RIVERFLOW.wf_item(item_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_item_region;
CREATE INDEX RIVERFLOW.idx_wf_item_region ON RIVERFLOW.wf_item(region_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_item_flow;
CREATE INDEX RIVERFLOW.idx_wf_item_flow ON RIVERFLOW.wf_item(flow_id);

DROP TABLE IF EXISTS RIVERFLOW.wf_qr_auth_record;
CREATE TABLE RIVERFLOW.wf_qr_auth_record (
  id BIGINT NOT NULL,
  namespace VARCHAR(50) DEFAULT '',
  qr_id VARCHAR(64) NULL,
  biz_id VARCHAR(100) NULL,
  operation VARCHAR(20) NOT NULL,
  content CLOB NULL,
  status INT NULL,
  status_name VARCHAR(20) NULL,
  auth_status VARCHAR(20) NULL,
  user_name VARCHAR(50) NULL,
  id_card VARCHAR(100) NULL,
  phone VARCHAR(100) NULL,
  expire_time TIMESTAMP NULL,
  request_params CLOB NULL,
  response_summary CLOB NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_qr_auth_record IS '二维码认证全量记录';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_qr_auth_record_qr_id;
CREATE INDEX RIVERFLOW.idx_wf_qr_auth_record_qr_id ON RIVERFLOW.wf_qr_auth_record(qr_id);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_qr_auth_record_biz_id;
CREATE INDEX RIVERFLOW.idx_wf_qr_auth_record_biz_id ON RIVERFLOW.wf_qr_auth_record(biz_id);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_qr_auth_record_create_time;
CREATE INDEX RIVERFLOW.idx_wf_qr_auth_record_create_time ON RIVERFLOW.wf_qr_auth_record(create_time);

DROP TABLE IF EXISTS RIVERFLOW.wf_region;
CREATE TABLE RIVERFLOW.wf_region (
  id BIGINT NOT NULL,
  region_code VARCHAR(20) NOT NULL,
  region_name VARCHAR(100) NOT NULL,
  parent_code VARCHAR(20) DEFAULT '0',
  level TINYINT DEFAULT 1,
  sort_no INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_by VARCHAR(64) DEFAULT '',
  update_by VARCHAR(64) DEFAULT '',
  del_flag TINYINT DEFAULT 0,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_region IS '行政区划';
COMMENT ON COLUMN RIVERFLOW.wf_region.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_region.region_code IS '区划代码';
COMMENT ON COLUMN RIVERFLOW.wf_region.region_name IS '区划名称';
COMMENT ON COLUMN RIVERFLOW.wf_region.parent_code IS '父级代码';
COMMENT ON COLUMN RIVERFLOW.wf_region.level IS '层级：1-省 2-市 3-区县';
COMMENT ON COLUMN RIVERFLOW.wf_region.sort_no IS '排序号';
COMMENT ON COLUMN RIVERFLOW.wf_region.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_region.update_time IS '更新时间';
COMMENT ON COLUMN RIVERFLOW.wf_region.create_by IS '创建人';
COMMENT ON COLUMN RIVERFLOW.wf_region.update_by IS '更新人';
COMMENT ON COLUMN RIVERFLOW.wf_region.del_flag IS '删除标志：0-正常 1-已删除';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_region_uk_region_code;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_region_uk_region_code ON RIVERFLOW.wf_region(region_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_region_parent;
CREATE INDEX RIVERFLOW.idx_wf_region_parent ON RIVERFLOW.wf_region(parent_code);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_region_level;
CREATE INDEX RIVERFLOW.idx_wf_region_level ON RIVERFLOW.wf_region(level);

DROP TABLE IF EXISTS RIVERFLOW.wf_transfer_queue;
CREATE TABLE RIVERFLOW.wf_transfer_queue (
  id BIGINT NOT NULL,
  biz_key VARCHAR(100) NOT NULL,
  source_data CLOB NULL,
  downstream_handle_id VARCHAR(100) NULL,
  status VARCHAR(20) DEFAULT 'pending',
  callback_data CLOB NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
COMMENT ON TABLE RIVERFLOW.wf_transfer_queue IS '中台业务中转队列表';
COMMENT ON COLUMN RIVERFLOW.wf_transfer_queue.id IS '主键ID';
COMMENT ON COLUMN RIVERFLOW.wf_transfer_queue.biz_key IS '业务主键（办件流水号等）';
COMMENT ON COLUMN RIVERFLOW.wf_transfer_queue.source_data IS '上游传入的原始业务数据';
COMMENT ON COLUMN RIVERFLOW.wf_transfer_queue.downstream_handle_id IS '下游系统返回的办理编号';
COMMENT ON COLUMN RIVERFLOW.wf_transfer_queue.status IS '状态：pending-待办理 / processing-办理中 / done-已完成';
COMMENT ON COLUMN RIVERFLOW.wf_transfer_queue.callback_data IS '下游回调时传入的数据';
COMMENT ON COLUMN RIVERFLOW.wf_transfer_queue.create_time IS '创建时间';
COMMENT ON COLUMN RIVERFLOW.wf_transfer_queue.update_time IS '更新时间';
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_transfer_queue_uk_biz_key;
CREATE UNIQUE INDEX RIVERFLOW.idx_wf_transfer_queue_uk_biz_key ON RIVERFLOW.wf_transfer_queue(biz_key);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_transfer_queue_status;
CREATE INDEX RIVERFLOW.idx_wf_transfer_queue_status ON RIVERFLOW.wf_transfer_queue(status);
DROP INDEX IF EXISTS RIVERFLOW.idx_wf_transfer_queue_update_time;
CREATE INDEX RIVERFLOW.idx_wf_transfer_queue_update_time ON RIVERFLOW.wf_transfer_queue(update_time);

