-- ============================================================
-- RiverFlow · 业务推送接口（comBusinessNotify）初始化脚本
-- 场景：A系统通过 application/x-www-form-urlencoded 推送办件数据
-- 使用方式：直接执行本脚本，或在【动态表设计】页面手动配置后生成接口
-- ============================================================

-- ------------------------------------------------------
-- 1. 创建物理表（手动执行DDL，也可通过接口 /dynamic-table/{id}/create-table 自动创建）
-- ------------------------------------------------------
CREATE TABLE IF NOT EXISTS com_business_notify (
  business_id       VARCHAR(64)  NOT NULL PRIMARY KEY COMMENT '业务编码（事项唯一编码）',
  item_id           VARCHAR(64)  NOT NULL COMMENT '事项ID',
  item_code         VARCHAR(64)  NOT NULL COMMENT '事项编码',
  item_name         VARCHAR(200) NOT NULL COMMENT '事项名称',
  org_name          VARCHAR(200) NOT NULL COMMENT '部门名称',
  org_code          VARCHAR(64)  NOT NULL COMMENT '部门编码',
  source            VARCHAR(10)  NOT NULL COMMENT '业务来源：2-外网, 1-窗口',
  region_code       VARCHAR(20)  NOT NULL COMMENT '区划编码',
  apply_subject     VARCHAR(500) NOT NULL COMMENT '业务主题',
  type              VARCHAR(20)  NOT NULL COMMENT '业务类型：Accept-新受理, Correct-补齐补正',
  business_type     VARCHAR(10)  NOT NULL COMMENT '申请对象类型：1-个人, 0-企业',
  base_info         JSON         COMMENT '基本信息（person/company嵌套对象）',
  form_info         JSON         COMMENT '表单信息',
  material_info     JSON         COMMENT '材料信息',
  ems_info          JSON         COMMENT '邮寄信息',
  create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_item_code (item_code),
  INDEX idx_org_code (org_code),
  INDEX idx_region_code (region_code),
  INDEX idx_source (source),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务推送通知表';


-- ------------------------------------------------------
-- 2. 动态表元数据（wf_dynamic_table）
-- 如通过UI操作，此步骤可省略，由页面自动插入
-- ------------------------------------------------------
INSERT INTO wf_dynamic_table (id, table_code, table_name, ds_id, remark, status, del_flag, create_time, update_time)
VALUES (1000000000000000001, 'com_business_notify', '业务推送通知表', 0,
        'A系统业务推送，接收各市级部门或政务服务部门的办件数据', 1, 0, NOW(), NOW());


-- ------------------------------------------------------
-- 3. 动态表字段元数据（wf_dynamic_table_column）
-- ------------------------------------------------------
INSERT INTO wf_dynamic_table_column (id, table_id, column_code, column_name, data_type, length, is_pk, is_required, is_index, sort_no, del_flag, create_time, update_time) VALUES
(1000000000000000001, 1000000000000000001, 'business_id', '业务编码', 'varchar', 64, 1, 1, 0, 1, 0, NOW(), NOW()),
(1000000000000000002, 1000000000000000001, 'item_id', '事项ID', 'varchar', 64, 0, 1, 0, 2, 0, NOW(), NOW()),
(1000000000000000003, 1000000000000000001, 'item_code', '事项编码', 'varchar', 64, 0, 1, 1, 3, 0, NOW(), NOW()),
(1000000000000000004, 1000000000000000001, 'item_name', '事项名称', 'varchar', 200, 0, 1, 0, 4, 0, NOW(), NOW()),
(1000000000000000005, 1000000000000000001, 'org_name', '部门名称', 'varchar', 200, 0, 1, 0, 5, 0, NOW(), NOW()),
(1000000000000000006, 1000000000000000001, 'org_code', '部门编码', 'varchar', 64, 0, 1, 1, 6, 0, NOW(), NOW()),
(1000000000000000007, 1000000000000000001, 'source', '业务来源', 'varchar', 10, 0, 1, 1, 7, 0, NOW(), NOW()),
(1000000000000000008, 1000000000000000001, 'region_code', '区划编码', 'varchar', 20, 0, 1, 1, 8, 0, NOW(), NOW()),
(1000000000000000009, 1000000000000000001, 'apply_subject', '业务主题', 'varchar', 500, 0, 1, 0, 9, 0, NOW(), NOW()),
(1000000000000000010, 1000000000000000001, 'type', '业务类型', 'varchar', 20, 0, 1, 0, 10, 0, NOW(), NOW()),
(1000000000000000011, 1000000000000000001, 'business_type', '申请对象类型', 'varchar', 10, 0, 1, 0, 11, 0, NOW(), NOW()),
(1000000000000000012, 1000000000000000001, 'base_info', '基本信息', 'json', NULL, 0, 0, 0, 12, 0, NOW(), NOW()),
(1000000000000000013, 1000000000000000001, 'form_info', '表单信息', 'json', NULL, 0, 0, 0, 13, 0, NOW(), NOW()),
(1000000000000000014, 1000000000000000001, 'material_info', '材料信息', 'json', NULL, 0, 0, 0, 14, 0, NOW(), NOW()),
(1000000000000000015, 1000000000000000001, 'ems_info', '邮寄信息', 'json', NULL, 0, 0, 0, 15, 0, NOW(), NOW());


-- ------------------------------------------------------
-- 4. 接口注册（wf_api_catalog）
-- 外部系统调用：POST /api/open/COM_BUSINESS_NOTIFY_INSERT
-- Content-Type: application/x-www-form-urlencoded
-- ------------------------------------------------------
INSERT INTO wf_api_catalog (id, api_code, api_name, api_type, method, url, content_type, auth_type, ds_id, timeout, retry_times, proxy_enabled, status, del_flag, create_time, update_time)
VALUES (1000000000000000001,
        'COM_BUSINESS_NOTIFY_INSERT',
        '业务推送-新增',
        'sql',
        'POST',
        'INSERT INTO com_business_notify (business_id, item_id, item_code, item_name, org_name, org_code, source, region_code, apply_subject, type, business_type, base_info, form_info, material_info, ems_info, create_time) VALUES (#{businessId}, #{itemId}, #{itemCode}, #{itemName}, #{orgName}, #{orgCode}, #{source}, #{regionCode}, #{applySubject}, #{type}, #{businessType}, #{baseInfo}, #{form}, #{material}, #{emsInfo}, NOW())',
        'application/x-www-form-urlencoded',
        'none',
        0,
        30000,
        0,
        0,
        1,
        0,
        NOW(),
        NOW());


-- ------------------------------------------------------
-- 5. （可选）查询接口，方便内部系统按业务编码查询单条记录
-- ------------------------------------------------------
INSERT INTO wf_api_catalog (id, api_code, api_name, api_type, method, url, content_type, auth_type, ds_id, timeout, retry_times, proxy_enabled, status, del_flag, create_time, update_time)
VALUES (1000000000000000002,
        'COM_BUSINESS_NOTIFY_SELECT',
        '业务推送-查询',
        'sql',
        'GET',
        'SELECT * FROM com_business_notify WHERE business_id = #{businessId}',
        'application/json',
        'none',
        0,
        30000,
        0,
        0,
        1,
        0,
        NOW(),
        NOW());
