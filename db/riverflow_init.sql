/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80013
 Source Host           : localhost:3306
 Source Schema         : riverflow

 Target Server Type    : MySQL
 Target Server Version : 80013
 File Encoding         : 65001

 Date: 22/05/2026 16:30:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for com_business_notify
-- ----------------------------
DROP TABLE IF EXISTS `com_business_notify`;
CREATE TABLE `com_business_notify`  (
  `business_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务编码（事项唯一编码）',
  `item_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事项ID',
  `item_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事项编码',
  `item_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事项名称',
  `org_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门编码',
  `source` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务来源：2-外网, 1-窗口',
  `region_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区划编码',
  `apply_subject` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务主题',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型：Accept-新受理, Correct-补齐补正',
  `business_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请对象类型：1-个人, 0-企业',
  `base_info` json NULL COMMENT '基本信息（person/company嵌套对象）',
  `form_info` json NULL COMMENT '表单信息',
  `material_info` json NULL COMMENT '材料信息',
  `ems_info` json NULL COMMENT '邮寄信息',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `receive_num` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申办流水号',
  PRIMARY KEY (`business_id`) USING BTREE,
  INDEX `idx_item_code`(`item_code`) USING BTREE,
  INDEX `idx_org_code`(`org_code`) USING BTREE,
  INDEX `idx_region_code`(`region_code`) USING BTREE,
  INDEX `idx_source`(`source`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '业务推送通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of com_business_notify
-- ----------------------------

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作模块',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作描述',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'HTTP方法',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `response_code` int(11) NULL DEFAULT NULL COMMENT '响应状态码',
  `response_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '响应消息',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作IP',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作用户',
  `execute_time` bigint(20) NULL DEFAULT NULL COMMENT '执行时长(ms)',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-失败 1-成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '异常信息',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统操作日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_plugin
-- ----------------------------
DROP TABLE IF EXISTS `sys_plugin`;
CREATE TABLE `sys_plugin`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plugin_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '插件名称',
  `plugin_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '插件类型标识',
  `plugin_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '插件版本',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '插件分类',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '插件描述',
  `jar_file` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'JAR包文件名',
  `jar_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'JAR包存储路径',
  `file_size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  `loaded` tinyint(1) NULL DEFAULT 0 COMMENT '是否已加载',
  `config_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '配置模板JSON',
  `author` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作者',
  `website` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '官网/文档地址',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_plugin_type`(`plugin_type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2057711403983212547 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '插件管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_plugin
-- ----------------------------
INSERT INTO `sys_plugin` VALUES (2057111124514504706, 'MinIO文件推送', 'minio', '1.0.0', 'storage', 'MinIO对象存储操作，支持文件上传、下载、删除、查询等', 'river-minio-1.0.0-SNAPSHOT.jar', 'C:\\Users\\kaerthas\\riverflow\\plugins\\1779288485833_river-minio-1.0.0-SNAPSHOT.jar', 20133676, 'CloudUpload', 'enabled', 1, '{\"endpoint\":\"http://localhost:9000\",\"accessKey\":\"minioadmin\",\"secretKey\":\"minioadmin\",\"bucket\":\"materials\",\"operation\":\"upload\",\"filePath\":\"${context.filePath}\",\"objectName\":\"${context.fileName}\",\"contentType\":\"application/octet-stream\"}', NULL, NULL, '2026-05-20 22:48:06', '2026-05-20 22:48:06', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$t56jkpfYa2C.jIaTkjVy3uptUZn4vbkUIRSRSyryTYo116lqgMENi', '系统管理员', 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', 'admin@riverflow.com', '13800138000', 1, '2026-05-13 15:15:56', '2026-05-13 15:15:56');

-- ----------------------------
-- Table structure for t_business_info
-- ----------------------------
DROP TABLE IF EXISTS `t_business_info`;
CREATE TABLE `t_business_info`  (
  `business_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务主键',
  PRIMARY KEY (`business_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '业务申报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_business_info
-- ----------------------------

-- ----------------------------
-- Table structure for wf_api_call_log
-- ----------------------------
DROP TABLE IF EXISTS `wf_api_call_log`;
CREATE TABLE `wf_api_call_log`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `api_id` bigint(20) NOT NULL COMMENT '接口ID',
  `api_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口编码',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方式',
  `request_headers` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求头JSON',
  `request_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求体',
  `response_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应体',
  `status_code` int(11) NULL DEFAULT NULL COMMENT 'HTTP状态码',
  `cost_time` int(11) NULL DEFAULT NULL COMMENT '耗时毫秒',
  `call_status` tinyint(4) NULL DEFAULT 0 COMMENT '调用状态：0-失败 1-成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api`(`api_id`) USING BTREE,
  INDEX `idx_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '接口调用日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_api_call_log
-- ----------------------------

-- ----------------------------
-- Table structure for wf_api_catalog
-- ----------------------------
DROP TABLE IF EXISTS `wf_api_catalog`;
CREATE TABLE `wf_api_catalog`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `api_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口编码',
  `api_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口名称',
  `api_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口类型：proxy-代理 sql-SQL服务 data-数据服务 script-脚本服务',
  `method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'POST' COMMENT '请求方式：GET/POST/PUT/DELETE',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求地址',
  `content_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'application/json' COMMENT '请求体类型',
  `auth_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'none' COMMENT '认证方式：none/basic/token/oauth2',
  `ds_id` bigint(20) NULL DEFAULT NULL COMMENT 'SQL类型时绑定的数据源ID',
  `script_id` bigint(20) NULL DEFAULT NULL COMMENT '脚本类型时绑定的脚本ID',
  `timeout` int(11) NULL DEFAULT 30000 COMMENT '超时毫秒',
  `retry_times` tinyint(4) NULL DEFAULT 0 COMMENT '重试次数',
  `proxy_enabled` tinyint(4) NULL DEFAULT 0 COMMENT '是否启用代理：0-否 1-是',
  `proxy_host` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '代理主机',
  `proxy_port` int(11) NULL DEFAULT NULL COMMENT '代理端口',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-下线',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  `trigger_enabled` tinyint(4) NULL DEFAULT 0 COMMENT '是否启用流程触发：0-否 1-是',
  `trigger_flow_id` bigint(20) NULL DEFAULT NULL COMMENT '执行成功后触发的流程定义ID',
  `trigger_flow_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发流程编码（绑定编码，自动取最新发布版本）',
  `trigger_biz_key_field` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '从请求参数中提取业务主键的字段名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_api_code`(`api_code`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '接口目录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_api_catalog
-- ----------------------------
INSERT INTO `wf_api_catalog` VALUES (10, 'TRANSFER_INSERT', 'A接口-上游数据传入', 'sql', 'POST', 'INSERT INTO wf_transfer_queue (biz_key, source_data, status, create_time) VALUES (#{bizKey}, #{sourceData}, \'pending\', NOW())', 'application/json', 'none', 0, NULL, 30000, 0, 0, NULL, NULL, 1, '2026-05-13 17:54:16', '2026-05-14 17:24:36', '', NULL, 1, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (11, 'TRANSFER_UPDATE', 'C接口-下游状态回传', 'sql', 'POST', 'UPDATE wf_transfer_queue SET status = #{status}, callback_data = #{callbackData}, update_time = NOW() WHERE biz_key = #{bizKey}', 'application/json', 'none', 0, NULL, 30000, 0, 0, NULL, NULL, 1, '2026-05-13 17:54:16', '2026-05-14 17:24:38', '', NULL, 1, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (12, 'TRANSFER_SELECT', 'C接口-状态查询', 'sql', 'GET', 'SELECT * FROM wf_transfer_queue WHERE biz_key = #{bizKey}', 'application/json', 'none', 0, NULL, 30000, 0, 0, NULL, NULL, 1, '2026-05-13 17:54:17', '2026-05-14 17:24:26', '', NULL, 1, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (20, 'DOWNSTREAM_B', 'B接口-下游业务办理', 'proxy', 'POST', 'http://downstream-system/api/business/handle', 'application/json', 'none', NULL, NULL, 30000, 3, 0, NULL, NULL, 1, '2026-05-13 17:54:17', '2026-05-14 17:24:30', '', NULL, 1, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (21, 'UPSTREAM_D', 'D接口-上游结果回传', 'proxy', 'POST', 'http://upstream-system/api/transfer/result', 'application/json', 'none', NULL, NULL, 30000, 3, 0, NULL, NULL, 1, '2026-05-13 17:54:17', '2026-05-14 17:24:33', '', NULL, 1, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (1000001, 'b-sys-example-submit', 'B系统-示例提交接口', 'proxy', 'POST', 'http://localhost:8080/example/b-sys/submit', 'application/json', 'none', NULL, NULL, 30000, 0, 0, NULL, NULL, 1, '2026-05-15 15:14:50', '2026-05-15 15:14:50', '', '', 0, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (1000002, 'b-sys-xayjs-getclfcjhsxx', 'B系统-存量房交易核税信息获取', 'proxy', 'POST', 'http://localhost:8080/example/b-sys/getClfcjhsxx', 'application/json', 'none', NULL, NULL, 30000, 0, 0, NULL, NULL, 1, '2026-05-16 10:21:08', '2026-05-16 22:08:03', '', '', 0, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (1000003, 'b-sys-query-wsqkxx', 'B系统-完税状态信息查询', 'proxy', 'POST', 'http://localhost:8080/example/b-sys/getWsqkxx', 'application/json', 'none', NULL, NULL, 30000, 0, 0, NULL, NULL, 1, '2026-05-16 22:08:14', '2026-05-16 22:08:14', '', '', 0, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (1000000000000000001, 'COM_BUSINESS_NOTIFY_INSERT', '业务推送-新增', 'sql', 'POST', 'INSERT INTO com_business_notify (business_id,receive_num,item_id, item_code, item_name, org_name, org_code, source, region_code, apply_subject, type, business_type, base_info, form_info, material_info, ems_info, create_time) VALUES (#{businessId}, #{receiveNum},#{itemId}, #{itemCode}, #{itemName}, #{orgName}, #{orgCode}, #{source}, #{regionCode}, #{applySubject}, #{type}, #{businessType}, #{baseInfo}, #{form}, #{material}, #{emsInfo}, NOW())', 'application/x-www-form-urlencoded', 'none', 2054828849055723522, NULL, 30000, 0, 0, NULL, NULL, 1, '2026-05-14 14:10:06', '2026-05-18 10:26:33', '', '', 0, 1, 1003, 'FLOW_1779068031191', 'receiveNum');
INSERT INTO `wf_api_catalog` VALUES (1000000000000000002, 'COM_BUSINESS_NOTIFY_SELECT', '业务推送-查询', 'sql', 'GET', 'SELECT * FROM com_business_notify WHERE business_id = #{businessId}', 'application/json', 'none', 0, NULL, 30000, 0, 0, NULL, NULL, 1, '2026-05-14 14:10:06', '2026-05-14 14:10:06', '', '', 0, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (2055135972549554177, 't_business_info_INSERT', '业务申报表-新增', 'sql', 'POST', 'INSERT INTO t_business_info () VALUES ()', 'application/json', 'none', 2054828849055723500, NULL, 30000, 0, 0, NULL, NULL, 0, '2026-05-15 11:59:33', '2026-05-15 12:01:17', NULL, NULL, 1, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (2055135977469472770, 't_business_info_SELECT', '业务申报表-查询', 'sql', 'GET', 'SELECT * FROM t_business_info WHERE business_id = #{business_id}', 'application/json', 'none', 2054828849055723500, NULL, 30000, 0, 0, NULL, NULL, 0, '2026-05-15 11:59:34', '2026-05-15 12:01:15', NULL, NULL, 1, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (2055135978576769026, 't_business_info_DELETE', '业务申报表-删除', 'sql', 'POST', 'DELETE FROM t_business_info WHERE business_id = #{business_id}', 'application/json', 'none', 2054828849055723500, NULL, 30000, 0, 0, NULL, NULL, 0, '2026-05-15 11:59:35', '2026-05-15 12:01:10', NULL, NULL, 1, 0, NULL, NULL, NULL);
INSERT INTO `wf_api_catalog` VALUES (2055135978899730434, 't_business_info_LIST', '业务申报表-列表', 'sql', 'GET', 'SELECT * FROM t_business_info ORDER BY business_id DESC LIMIT #{limit} OFFSET #{offset}', 'application/json', 'none', 2054828849055723500, NULL, 30000, 0, 0, NULL, NULL, 0, '2026-05-15 11:59:35', '2026-05-15 12:01:13', NULL, NULL, 1, 0, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for wf_api_param
-- ----------------------------
DROP TABLE IF EXISTS `wf_api_param`;
CREATE TABLE `wf_api_param`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `api_id` bigint(20) NOT NULL COMMENT '所属接口ID',
  `param_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数类型：header/query/body/response',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父参数ID，支持嵌套',
  `param_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数键',
  `param_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '参数名称',
  `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'string' COMMENT '数据类型：string/int/object/array',
  `is_required` tinyint(4) NULL DEFAULT 0 COMMENT '是否必填：0-否 1-是',
  `default_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认值',
  `sort_no` int(11) NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_type`(`api_id`, `param_type`) USING BTREE,
  INDEX `idx_parent`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '接口参数定义' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_api_param
-- ----------------------------

INSERT INTO `wf_api_param` VALUES (1010100, 1000003, 'body', 0, 'appid', '应用标识', 'string', 1, 'XT000', 1, '2026-05-16 22:11:13', '2026-05-16 22:11:13', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010101, 1000003, 'body', 0, 'password', '接口密码', 'string', 1, 'SZRZYT', 2, '2026-05-16 22:11:13', '2026-05-16 22:11:13', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010102, 1000003, 'body', 0, 'intrfaceid', '接口编号', 'string', 1, 'SNSW.FCSB.GETWSQKXX', 3, '2026-05-16 22:11:13', '2026-05-16 22:11:13', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010103, 1000003, 'body', 0, 'HTBH', '合同编号', 'string', 1, NULL, 4, '2026-05-16 22:11:13', '2026-05-16 22:11:13', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010104, 1000003, 'body', 0, 'QXDM', '区县代码', 'string', 1, NULL, 5, '2026-05-16 22:11:13', '2026-05-16 22:11:13', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010200, 1000003, 'response', 0, 'RetCode', '返回是否成功标签', 'string', 1, NULL, 1, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010201, 1000003, 'response', 0, 'RetMsg', '返回结果说明', 'string', 0, NULL, 2, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010202, 1000003, 'response', 0, 'Wsbz', '完税标志', 'string', 1, NULL, 3, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010203, 1000003, 'response', 0, 'Nsrmc', '纳税人名称', 'string', 0, NULL, 4, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010204, 1000003, 'response', 0, 'Nsrsbh', '纳税人识别号', 'string', 0, NULL, 5, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010205, 1000003, 'response', 0, 'Tdfwdz', '土地房屋地址', 'string', 0, NULL, 6, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010206, 1000003, 'response', 0, 'Ybtse', '税额', 'string', 0, NULL, 7, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010207, 1000003, 'response', 0, 'Pgjyjg', '评估金额', 'string', 0, NULL, 8, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010208, 1000003, 'response', 0, 'Htje', '合同金额', 'string', 0, NULL, 9, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010209, 1000003, 'response', 0, 'Mj', '面积', 'string', 0, NULL, 10, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010210, 1000003, 'response', 0, 'Dzsphm', '电子税票号码', 'string', 0, NULL, 11, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010211, 1000003, 'response', 0, 'Htqdsj', '合同签订时间', 'string', 0, NULL, 12, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010212, 1000003, 'response', 0, 'Htbh', '合同编号', 'string', 0, NULL, 13, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (1010213, 1000003, 'response', 0, 'WspzPdf', '完税凭证', 'string', 0, NULL, 14, '2026-05-16 22:11:14', '2026-05-16 22:11:14', '', '', 0);
INSERT INTO `wf_api_param` VALUES (2055320492380839938, 1000001, 'body', 0, 'projectName', '项目名称', 'string', 1, '', 1, '2026-05-16 00:12:46', '2026-05-16 00:12:46', NULL, NULL, 0);
INSERT INTO `wf_api_param` VALUES (2055320492414394369, 1000001, 'body', 0, 'projectNo', '项目编号', 'string', 1, '', 2, '2026-05-16 00:12:46', '2026-05-16 00:12:46', NULL, NULL, 0);


-- ----------------------------
-- Table structure for wf_api_script
-- ----------------------------
DROP TABLE IF EXISTS `wf_api_script`;
CREATE TABLE `wf_api_script`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `script_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '脚本编码',
  `script_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '脚本名称',
  `script_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '脚本类型：format-格式化 header-请求头 result-结果处理 condition-条件判断',
  `script_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'Groovy脚本内容',
  `params` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '脚本入参定义JSON',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_script_code`(`script_code`) USING BTREE,
  INDEX `idx_type`(`script_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '接口脚本库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_api_script
-- ----------------------------

-- ----------------------------
-- Table structure for wf_datasource
-- ----------------------------
DROP TABLE IF EXISTS `wf_datasource`;
CREATE TABLE `wf_datasource`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `ds_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据源编码',
  `ds_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据源名称',
  `db_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据库类型：mysql/oracle/sqlserver/postgresql',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '连接URL',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（Jasypt加密）',
  `driver_class` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '驱动类名',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ds_code`(`ds_code`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态数据源配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_datasource
-- ----------------------------
INSERT INTO `wf_datasource` VALUES (2054828849055723522, 'biz_db', '收件主库', 'mysql', 'jdbc:mysql://127.0.0.1:3306/riverflow?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true', 'root', 'java', 'com.mysql.cj.jdbc.Driver', 1, '2026-05-14 15:39:09', '2026-05-14 15:39:09', NULL, NULL, 0);

-- ----------------------------
-- Table structure for wf_dynamic_table
-- ----------------------------
DROP TABLE IF EXISTS `wf_dynamic_table`;
CREATE TABLE `wf_dynamic_table`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `table_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '表编码（英文）',
  `table_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '表名称（中文）',
  `ds_id` bigint(20) NULL DEFAULT 0 COMMENT '所属数据源ID，0表示主库',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_table_code`(`table_code`) USING BTREE,
  INDEX `idx_ds`(`ds_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态表定义' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_dynamic_table
-- ----------------------------
INSERT INTO `wf_dynamic_table` VALUES (100, 'wf_transfer_queue', '中台业务中转队列表', 0, '上游A接口写入，下游回调更新，工作流轮询', 1, '2026-05-13 18:01:41', '2026-05-13 18:01:41', '', '', 0);
INSERT INTO `wf_dynamic_table` VALUES (1000000000000000001, 'com_business_notify', '业务推送通知表', 2054828849055723522, 'A系统业务推送，接收各市级部门或政务服务部门的办件数据', 1, '2026-05-14 14:10:05', '2026-05-14 14:10:05', '', '', 0);
INSERT INTO `wf_dynamic_table` VALUES (2055128279663304705, 't_business_info', '业务申报表', 2054828849055723522, '测试', 0, '2026-05-14 14:10:05', '2026-05-14 14:10:05', '', '', 0);

-- ----------------------------
-- Table structure for wf_dynamic_table_column
-- ----------------------------
DROP TABLE IF EXISTS `wf_dynamic_table_column`;
CREATE TABLE `wf_dynamic_table_column`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `table_id` bigint(20) NOT NULL COMMENT '所属表ID',
  `column_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段编码',
  `column_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段名称',
  `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据类型：varchar/int/bigint/datetime/text/decimal等',
  `length` int(11) NULL DEFAULT NULL COMMENT '长度',
  `decimal_scale` int(11) NULL DEFAULT NULL COMMENT '小数位',
  `is_pk` tinyint(4) NULL DEFAULT 0 COMMENT '是否主键：0-否 1-是',
  `is_required` tinyint(4) NULL DEFAULT 0 COMMENT '是否必填：0-否 1-是',
  `is_index` tinyint(4) NULL DEFAULT 0 COMMENT '是否索引：0-否 1-是',
  `default_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认值',
  `sort_no` int(11) NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_table_column`(`table_id`, `column_code`) USING BTREE,
  INDEX `idx_table`(`table_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态表字段定义' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_dynamic_table_column
-- ----------------------------
INSERT INTO `wf_dynamic_table_column` VALUES (1001, 100, 'id', '主键ID', 'bigint', 20, NULL, 1, 1, 0, NULL, 1, '2026-05-13 18:01:41', '2026-05-13 18:01:41', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (1002, 100, 'biz_key', '业务主键', 'varchar', 100, NULL, 0, 1, 1, NULL, 2, '2026-05-13 18:01:41', '2026-05-13 18:01:41', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (1003, 100, 'source_data', '原始业务数据', 'text', NULL, NULL, 0, 0, 0, NULL, 3, '2026-05-13 18:01:41', '2026-05-13 18:01:41', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (1004, 100, 'downstream_handle_id', '下游办理编号', 'varchar', 100, NULL, 0, 0, 0, NULL, 4, '2026-05-13 18:01:41', '2026-05-13 18:01:41', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (1005, 100, 'status', '办理状态', 'varchar', 20, NULL, 0, 1, 1, 'pending', 5, '2026-05-13 18:01:41', '2026-05-13 18:01:41', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (1006, 100, 'callback_data', '回调数据', 'text', NULL, NULL, 0, 0, 0, NULL, 6, '2026-05-13 18:01:41', '2026-05-13 18:01:41', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (1007, 100, 'create_time', '创建时间', 'datetime', NULL, NULL, 0, 0, 0, 'CURRENT_TIMESTAMP', 7, '2026-05-13 18:01:41', '2026-05-13 18:01:41', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (1008, 100, 'update_time', '更新时间', 'datetime', NULL, NULL, 0, 0, 0, 'CURRENT_TIMESTAMP', 8, '2026-05-13 18:01:41', '2026-05-13 18:01:41', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055132096018571266, 2055128279663304705, 'business_id', '业务主键', 'varchar', 50, 0, 1, 1, 0, '', 1, '2026-05-15 11:44:09', '2026-05-15 11:44:09', NULL, NULL, 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937619390466, 1000000000000000001, 'business_id', '业务编码', 'varchar', 64, NULL, 1, 1, 0, NULL, 1, '2026-05-14 14:10:06', '2026-05-14 17:15:17', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556290, 1000000000000000001, 'item_id', '事项ID', 'varchar', 64, NULL, 0, 1, 0, NULL, 2, '2026-05-14 14:10:06', '2026-05-14 17:15:17', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556291, 1000000000000000001, 'item_code', '事项编码', 'varchar', 64, NULL, 0, 1, 1, NULL, 3, '2026-05-14 14:10:06', '2026-05-14 17:15:18', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556292, 1000000000000000001, 'item_name', '事项名称', 'varchar', 200, NULL, 0, 1, 0, NULL, 4, '2026-05-14 14:10:06', '2026-05-14 17:15:18', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556293, 1000000000000000001, 'org_name', '部门名称', 'varchar', 200, NULL, 0, 1, 0, NULL, 5, '2026-05-14 14:10:06', '2026-05-14 17:15:18', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556294, 1000000000000000001, 'org_code', '部门编码', 'varchar', 64, NULL, 0, 1, 1, NULL, 6, '2026-05-14 14:10:06', '2026-05-14 17:15:18', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556295, 1000000000000000001, 'source', '业务来源', 'varchar', 10, NULL, 0, 1, 1, NULL, 7, '2026-05-14 14:10:06', '2026-05-14 17:15:18', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556296, 1000000000000000001, 'region_code', '区划编码', 'varchar', 20, NULL, 0, 1, 1, NULL, 8, '2026-05-14 14:10:06', '2026-05-14 17:15:18', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556297, 1000000000000000001, 'apply_subject', '业务主题', 'varchar', 500, NULL, 0, 1, 0, NULL, 9, '2026-05-14 14:10:06', '2026-05-14 17:15:18', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556298, 1000000000000000001, 'type', '业务类型', 'varchar', 20, NULL, 0, 1, 0, NULL, 10, '2026-05-14 14:10:06', '2026-05-14 17:15:18', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556299, 1000000000000000001, 'business_type', '申请对象类型', 'varchar', 10, NULL, 0, 1, 0, NULL, 11, '2026-05-14 14:10:06', '2026-05-14 17:15:19', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937644556300, 1000000000000000001, 'base_info', '基本信息', 'json', NULL, NULL, 0, 0, 0, NULL, 12, '2026-05-14 14:10:06', '2026-05-14 17:15:19', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937690693634, 1000000000000000001, 'form_info', '表单信息', 'json', NULL, NULL, 0, 0, 0, NULL, 13, '2026-05-14 14:10:06', '2026-05-14 17:15:19', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937690693635, 1000000000000000001, 'material_info', '材料信息', 'json', NULL, NULL, 0, 0, 0, NULL, 14, '2026-05-14 14:10:06', '2026-05-14 17:15:19', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937690693636, 1000000000000000001, 'ems_info', '邮寄信息', 'json', NULL, NULL, 0, 0, 0, NULL, 15, '2026-05-14 14:10:06', '2026-05-14 17:15:19', '', '', 0);
INSERT INTO `wf_dynamic_table_column` VALUES (2055135937690693637, 1000000000000000001, 'receive_num', '申报流水号', 'varchar', 50, 0, 0, 0, 0, '', 16, '2026-05-15 11:26:41', '2026-05-15 11:26:41', NULL, NULL, 0);

-- ----------------------------
-- Table structure for wf_flow_definition
-- ----------------------------
DROP TABLE IF EXISTS `wf_flow_definition`;
CREATE TABLE `wf_flow_definition`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `flow_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程编码',
  `flow_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程名称',
  `version` int(11) NULL DEFAULT 1 COMMENT '版本号',
  `item_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '绑定的事项编码',
  `trigger_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'cron' COMMENT '触发方式：cron-定时 event-事件 manual-手动',
  `trigger_config` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发配置（cron表达式或事件类型）',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-下线',
  `graph_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '流程图JSON（LogicFlow格式）',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flow_code_ver`(`flow_code`, `version`) USING BTREE,
  INDEX `idx_item`(`item_code`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程定义' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_flow_definition
-- ----------------------------
INSERT INTO `wf_flow_definition` VALUES (1, 'FLOW_001', '残疾人证新办流程', 1, '610000-001', 'manual', '', 1, '{\"nodes\":[],\"edges\":[]}', '2026-05-13 15:06:38', '2026-05-13 15:06:38', 'admin', '', 0);
INSERT INTO `wf_flow_definition` VALUES (2, 'FLOW_002', '火化信息推送流程', 1, '610000-002', 'event', '', 0, '{\"nodes\":[],\"edges\":[]}', '2026-05-13 15:06:38', '2026-05-13 15:06:38', 'admin', '', 0);
INSERT INTO `wf_flow_definition` VALUES (3, 'FLOW_003', '低保申请协同流程', 1, '610000-003', 'cron', '0 0 2 * * ?', 1, '{\"nodes\":[],\"edges\":[]}', '2026-05-13 15:06:38', '2026-05-13 15:06:38', 'admin', '', 0);
INSERT INTO `wf_flow_definition` VALUES (1003, 'FLOW_1778863694671', '中台数据中转流程', 1, NULL, 'event', NULL, 1, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":128,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":128,\"value\":\"开始\"}},{\"id\":\"db_query\",\"type\":\"db\",\"x\":304,\"y\":240,\"properties\":{\"name\":\"查询收件数据\",\"code\":\"db_query\",\"dsCode\":\"biz_db\",\"operation\":\"select\",\"sql\":\"SELECT * FROM com_business_notify WHERE receive_num = #{_businessKey} limit 1\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\",\"resultVarName\":\"aData\"},\"text\":{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}},{\"id\":\"script_assemble\",\"type\":\"script\",\"x\":304,\"y\":368,\"properties\":{\"name\":\"组装B系统报文\",\"code\":\"script_assemble\",\"scriptContent\":\"def a = context.get(\'aData\'); def b = [:]; b.projectName = a.apply_subject; b.projectNo = a.receive_num; context.set(\'bRequest\', b); return b;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.projectName\\\",\\\"target\\\":\\\"context.projectName\\\"},{\\\"source\\\":\\\"result.projectNo\\\",\\\"target\\\":\\\"context.projectNo\\\"}]\"},\"text\":{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}},{\"id\":\"api_call_b\",\"type\":\"api\",\"x\":304,\"y\":480,\"properties\":{\"name\":\"调用B系统提交\",\"code\":\"api_call_b\",\"apiCode\":\"b-sys-example-submit\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectName\\\",\\\"target\\\":\\\"body.projectName\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectNo\\\",\\\"target\\\":\\\"body.projectNo\\\"}]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":352,\"y\":624,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":352,\"y\":624,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"db_query\",\"startPoint\":{\"x\":304,\"y\":154},\"endPoint\":{\"x\":304,\"y\":212},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":154},{\"x\":304,\"y\":254},{\"x\":304,\"y\":112},{\"x\":304,\"y\":212}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"db_query\",\"targetNodeId\":\"script_assemble\",\"startPoint\":{\"x\":304,\"y\":268},\"endPoint\":{\"x\":304,\"y\":340},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":268},{\"x\":304,\"y\":368},{\"x\":304,\"y\":240},{\"x\":304,\"y\":340}]},{\"id\":\"e3\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble\",\"targetNodeId\":\"api_call_b\",\"startPoint\":{\"x\":304,\"y\":396},\"endPoint\":{\"x\":304,\"y\":452},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":396},{\"x\":304,\"y\":496},{\"x\":304,\"y\":352},{\"x\":304,\"y\":452}]},{\"id\":\"60d11cef-8b75-42cf-a609-f6a8e5df8b33\",\"type\":\"bezier\",\"sourceNodeId\":\"api_call_b\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":304,\"y\":508},\"endPoint\":{\"x\":352,\"y\":598},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":508},{\"x\":304,\"y\":608},{\"x\":352,\"y\":498},{\"x\":352,\"y\":598}]}]}', '2026-05-13 18:01:41', '2026-05-16 00:48:15', '', NULL, 0);
INSERT INTO `wf_flow_definition` VALUES (1004, 'FLOW_1779068031191', 'B系统提交-完税状态轮询流程', 1, NULL, 'event', NULL, 2, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":128,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":128,\"value\":\"开始\"}},{\"id\":\"db_query\",\"type\":\"db\",\"x\":304,\"y\":240,\"properties\":{\"name\":\"查询收件数据\",\"code\":\"db_query\",\"dsCode\":\"biz_db\",\"operation\":\"select\",\"sql\":\"SELECT * FROM com_business_notify WHERE receive_num = #{_businessKey} limit 1\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\",\"resultVarName\":\"aData\"},\"text\":{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}},{\"id\":\"script_assemble\",\"type\":\"script\",\"x\":304,\"y\":368,\"properties\":{\"name\":\"组装B系统报文\",\"code\":\"script_assemble\",\"scriptContent\":\"def a = context.get(\'aData\'); def b = [:]; b.projectName = a.apply_subject; b.projectNo = a.receive_num; context.set(\'bRequest\', b); return b;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.projectName\\\",\\\"target\\\":\\\"context.projectName\\\"},{\\\"source\\\":\\\"result.projectNo\\\",\\\"target\\\":\\\"context.projectNo\\\"}]\"},\"text\":{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}},{\"id\":\"api_call_b\",\"type\":\"api\",\"x\":304,\"y\":480,\"properties\":{\"name\":\"调用B系统提交\",\"code\":\"api_call_b\",\"apiCode\":\"b-sys-example-submit\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectName\\\",\\\"target\\\":\\\"body.projectName\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectNo\\\",\\\"target\\\":\\\"body.projectNo\\\"}]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}},{\"id\":\"script_assemble_c\",\"type\":\"script\",\"x\":304,\"y\":608,\"properties\":{\"name\":\"组装C接口请求\",\"code\":\"script_assemble_c\",\"scriptContent\":\"def req = [:]; req.appid = \'XT000\'; req.password = \'SZRZYT\'; req.intrfaceid = \'SNSW.FCSB.GETWSQKXX\'; req.HTBH = context.get(\'projectNo\'); def aData = context.get(\'aData\'); req.QXDM = (aData instanceof List && aData.size() > 0) ? aData[0].region_code : (aData?.region_code ?: \'\'); context.set(\'cRequest\', req); return req;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result\\\",\\\"target\\\":\\\"context.cRequest\\\"}]\"},\"text\":{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}},{\"id\":\"timer_wait\",\"type\":\"timer\",\"x\":304,\"y\":720,\"properties\":{\"name\":\"等待60秒\",\"code\":\"timer_wait\",\"timerType\":\"delay\",\"delaySeconds\":60},\"text\":{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}},{\"id\":\"api_poll_c\",\"type\":\"api\",\"x\":128,\"y\":848,\"properties\":{\"name\":\"轮询C接口状态\",\"code\":\"api_poll_c\",\"apiCode\":\"b-sys-query-wsqkxx\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.cRequest\\\",\\\"target\\\":\\\"body\\\"}]\",\"outputMapping\":\"[{\\\"source\\\":\\\"body\\\",\\\"target\\\":\\\"context.cResponseBody\\\"}]\"},\"text\":{\"x\":128,\"y\":848,\"value\":\"轮询C接口状态\"}},{\"id\":\"script_parse_c\",\"type\":\"script\",\"x\":304,\"y\":960,\"properties\":{\"name\":\"解析C接口响应\",\"code\":\"script_parse_c\",\"scriptContent\":\"def bodyStr = context.get(\'cResponseBody\'); if (bodyStr == null) { context.set(\'wsbz\', \'N\'); return [:]; } def resp = com.alibaba.fastjson2.JSON.parseObject(bodyStr); def data = resp.getJSONObject(\'data\'); context.set(\'wsbz\', data?.getString(\'Wsbz\') ?: \'N\'); context.set(\'retCode\', data?.getString(\'RetCode\') ?: \'\'); context.set(\'cResult\', data); return data;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.Wsbz\\\",\\\"target\\\":\\\"context.wsbz\\\"},{\\\"source\\\":\\\"result.RetCode\\\",\\\"target\\\":\\\"context.retCode\\\"}]\"},\"text\":{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}},{\"id\":\"cond_is_done\",\"type\":\"condition\",\"x\":304,\"y\":1088,\"properties\":{\"name\":\"是否已办结\",\"code\":\"cond_is_done\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":496,\"y\":1200,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":496,\"y\":1200,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"db_query\",\"startPoint\":{\"x\":304,\"y\":154},\"endPoint\":{\"x\":304,\"y\":212},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":154},{\"x\":304,\"y\":254},{\"x\":304,\"y\":112},{\"x\":304,\"y\":212}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"db_query\",\"targetNodeId\":\"script_assemble\",\"startPoint\":{\"x\":304,\"y\":268},\"endPoint\":{\"x\":304,\"y\":340},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":268},{\"x\":304,\"y\":368},{\"x\":304,\"y\":240},{\"x\":304,\"y\":340}]},{\"id\":\"e3\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble\",\"targetNodeId\":\"api_call_b\",\"startPoint\":{\"x\":304,\"y\":396},\"endPoint\":{\"x\":304,\"y\":452},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":396},{\"x\":304,\"y\":496},{\"x\":304,\"y\":352},{\"x\":304,\"y\":452}]},{\"id\":\"e4\",\"type\":\"bezier\",\"sourceNodeId\":\"api_call_b\",\"targetNodeId\":\"script_assemble_c\",\"startPoint\":{\"x\":304,\"y\":508},\"endPoint\":{\"x\":304,\"y\":580},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":508},{\"x\":304,\"y\":608},{\"x\":304,\"y\":480},{\"x\":304,\"y\":580}]},{\"id\":\"e5\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble_c\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":636},\"endPoint\":{\"x\":304,\"y\":692},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":636},{\"x\":304,\"y\":736},{\"x\":304,\"y\":592},{\"x\":304,\"y\":692}]},{\"id\":\"e6\",\"type\":\"bezier\",\"sourceNodeId\":\"timer_wait\",\"targetNodeId\":\"api_poll_c\",\"startPoint\":{\"x\":304,\"y\":748},\"endPoint\":{\"x\":128,\"y\":820},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":748},{\"x\":304,\"y\":848},{\"x\":128,\"y\":720},{\"x\":128,\"y\":820}]},{\"id\":\"e7\",\"type\":\"bezier\",\"sourceNodeId\":\"api_poll_c\",\"targetNodeId\":\"script_parse_c\",\"startPoint\":{\"x\":128,\"y\":876},\"endPoint\":{\"x\":304,\"y\":932},\"properties\":{},\"pointsList\":[{\"x\":128,\"y\":876},{\"x\":128,\"y\":976},{\"x\":304,\"y\":832},{\"x\":304,\"y\":932}]},{\"id\":\"e8\",\"type\":\"bezier\",\"sourceNodeId\":\"script_parse_c\",\"targetNodeId\":\"cond_is_done\",\"startPoint\":{\"x\":304,\"y\":988},\"endPoint\":{\"x\":304,\"y\":1060},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":988},{\"x\":304,\"y\":1088},{\"x\":304,\"y\":960},{\"x\":304,\"y\":1060}]},{\"id\":\"e9\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":378,\"y\":1088},\"endPoint\":{\"x\":426,\"y\":1200},\"properties\":{\"conditionType\":\"custom\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":402,\"y\":1144,\"value\":\"是\"},\"pointsList\":[{\"x\":378,\"y\":1088},{\"x\":478,\"y\":1088},{\"x\":326,\"y\":1200},{\"x\":426,\"y\":1200}]},{\"id\":\"e10\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":1060},\"endPoint\":{\"x\":304,\"y\":748},\"properties\":{\"conditionType\":\"default\"},\"text\":{\"x\":304,\"y\":904,\"value\":\"否\"},\"pointsList\":[{\"x\":304,\"y\":1060},{\"x\":304,\"y\":960},{\"x\":304,\"y\":848},{\"x\":304,\"y\":748}]}]}', '2026-05-16 22:45:58', '2026-05-18 12:16:10', '', NULL, 0);
INSERT INTO `wf_flow_definition` VALUES (2001, 'minio-upload-demo', 'MinIO文件上传示例', 1, NULL, 'manual', NULL, 2, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":96,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":96,\"value\":\"开始\"}},{\"id\":\"minio_upload\",\"type\":\"minio\",\"x\":304,\"y\":224,\"properties\":{\"name\":\"上传文件到MinIO\",\"code\":\"minio_upload\",\"endpoint\":\"http://localhost:9000\",\"accessKey\":\"admin\",\"secretKey\":\"12345678\",\"bucket\":\"riverflow\",\"operation\":\"upload\",\"filePath\":\"${context.filePath}\",\"objectName\":\"${context.fileName}\",\"contentType\":\"application/octet-stream\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":304,\"y\":336,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":304,\"y\":336,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"minio_upload\",\"startPoint\":{\"x\":304,\"y\":122},\"endPoint\":{\"x\":304,\"y\":196},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":122},{\"x\":304,\"y\":222},{\"x\":304,\"y\":96},{\"x\":304,\"y\":196}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"minio_upload\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":304,\"y\":252},\"endPoint\":{\"x\":304,\"y\":310},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":252},{\"x\":304,\"y\":352},{\"x\":304,\"y\":210},{\"x\":304,\"y\":310}]}]}', '2026-05-19 14:33:12', '2026-05-20 22:55:21', '', NULL, 0);
INSERT INTO `wf_flow_definition` VALUES (2056195003653402625, 'FLOW_1779068031191', 'B系统提交-完税状态轮询流程', 2, NULL, 'event', NULL, 2, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":128,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":128,\"value\":\"开始\"}},{\"id\":\"db_query\",\"type\":\"db\",\"x\":304,\"y\":240,\"properties\":{\"name\":\"查询收件数据\",\"code\":\"db_query\",\"dsCode\":\"biz_db\",\"operation\":\"select\",\"sql\":\"SELECT * FROM com_business_notify WHERE receive_num = #{_businessKey} limit 1\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\",\"resultVarName\":\"aData\"},\"text\":{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}},{\"id\":\"script_assemble\",\"type\":\"script\",\"x\":304,\"y\":368,\"properties\":{\"name\":\"组装B系统报文\",\"code\":\"script_assemble\",\"scriptContent\":\"def a = context.get(\'aData\'); def b = [:]; b.projectName = a.apply_subject; b.projectNo = a.receive_num; context.set(\'bRequest\', b); return b;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.projectName\\\",\\\"target\\\":\\\"context.projectName\\\"},{\\\"source\\\":\\\"result.projectNo\\\",\\\"target\\\":\\\"context.projectNo\\\"}]\"},\"text\":{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}},{\"id\":\"api_call_b\",\"type\":\"api\",\"x\":304,\"y\":480,\"properties\":{\"name\":\"调用B系统提交\",\"code\":\"api_call_b\",\"apiCode\":\"b-sys-example-submit\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectName\\\",\\\"target\\\":\\\"body.projectName\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectNo\\\",\\\"target\\\":\\\"body.projectNo\\\"}]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}},{\"id\":\"script_assemble_c\",\"type\":\"script\",\"x\":304,\"y\":608,\"properties\":{\"name\":\"组装C接口请求\",\"code\":\"script_assemble_c\",\"scriptContent\":\"def req = [:]; req.appid = \'XT000\'; req.password = \'SZRZYT\'; req.intrfaceid = \'SNSW.FCSB.GETWSQKXX\'; req.HTBH = context.get(\'projectNo\'); def aData = context.get(\'aData\'); req.QXDM = (aData instanceof List && aData.size() > 0) ? aData[0].region_code : (aData?.region_code ?: \'\'); context.set(\'cRequest\', req); return req;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result\\\",\\\"target\\\":\\\"context.cRequest\\\"}]\"},\"text\":{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}},{\"id\":\"timer_wait\",\"type\":\"timer\",\"x\":304,\"y\":720,\"properties\":{\"name\":\"等待60秒\",\"code\":\"timer_wait\",\"timerType\":\"delay\",\"delaySeconds\":60},\"text\":{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}},{\"id\":\"api_poll_c\",\"type\":\"api\",\"x\":128,\"y\":848,\"properties\":{\"name\":\"轮询C接口状态\",\"code\":\"api_poll_c\",\"apiCode\":\"b-sys-query-wsqkxx\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.cRequest\\\",\\\"target\\\":\\\"body\\\"}]\",\"outputMapping\":\"[{\\\"source\\\":\\\"body\\\",\\\"target\\\":\\\"context.cResponseBody\\\"}]\"},\"text\":{\"x\":128,\"y\":848,\"value\":\"轮询C接口状态\"}},{\"id\":\"script_parse_c\",\"type\":\"script\",\"x\":304,\"y\":960,\"properties\":{\"name\":\"解析C接口响应\",\"code\":\"script_parse_c\",\"scriptContent\":\"def bodyStr = context.get(\'cResponseBody\'); if (bodyStr == null) { context.set(\'wsbz\', \'N\'); return [:]; } def resp = com.alibaba.fastjson2.JSON.parseObject(bodyStr); def data = resp.getJSONObject(\'data\'); context.set(\'wsbz\', data?.getString(\'Wsbz\') ?: \'N\'); context.set(\'retCode\', data?.getString(\'RetCode\') ?: \'\'); context.set(\'cResult\', data); return data;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.Wsbz\\\",\\\"target\\\":\\\"context.wsbz\\\"},{\\\"source\\\":\\\"result.RetCode\\\",\\\"target\\\":\\\"context.retCode\\\"}]\"},\"text\":{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}},{\"id\":\"cond_is_done\",\"type\":\"condition\",\"x\":304,\"y\":1088,\"properties\":{\"name\":\"是否已办结\",\"code\":\"cond_is_done\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":496,\"y\":1200,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":496,\"y\":1200,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"db_query\",\"startPoint\":{\"x\":304,\"y\":154},\"endPoint\":{\"x\":304,\"y\":212},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":154},{\"x\":304,\"y\":254},{\"x\":304,\"y\":112},{\"x\":304,\"y\":212}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"db_query\",\"targetNodeId\":\"script_assemble\",\"startPoint\":{\"x\":304,\"y\":268},\"endPoint\":{\"x\":304,\"y\":340},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":268},{\"x\":304,\"y\":368},{\"x\":304,\"y\":240},{\"x\":304,\"y\":340}]},{\"id\":\"e3\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble\",\"targetNodeId\":\"api_call_b\",\"startPoint\":{\"x\":304,\"y\":396},\"endPoint\":{\"x\":304,\"y\":452},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":396},{\"x\":304,\"y\":496},{\"x\":304,\"y\":352},{\"x\":304,\"y\":452}]},{\"id\":\"e4\",\"type\":\"bezier\",\"sourceNodeId\":\"api_call_b\",\"targetNodeId\":\"script_assemble_c\",\"startPoint\":{\"x\":304,\"y\":508},\"endPoint\":{\"x\":304,\"y\":580},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":508},{\"x\":304,\"y\":608},{\"x\":304,\"y\":480},{\"x\":304,\"y\":580}]},{\"id\":\"e5\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble_c\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":636},\"endPoint\":{\"x\":304,\"y\":692},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":636},{\"x\":304,\"y\":736},{\"x\":304,\"y\":592},{\"x\":304,\"y\":692}]},{\"id\":\"e6\",\"type\":\"bezier\",\"sourceNodeId\":\"timer_wait\",\"targetNodeId\":\"api_poll_c\",\"startPoint\":{\"x\":304,\"y\":748},\"endPoint\":{\"x\":128,\"y\":820},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":748},{\"x\":304,\"y\":848},{\"x\":128,\"y\":720},{\"x\":128,\"y\":820}]},{\"id\":\"e7\",\"type\":\"bezier\",\"sourceNodeId\":\"api_poll_c\",\"targetNodeId\":\"script_parse_c\",\"startPoint\":{\"x\":128,\"y\":876},\"endPoint\":{\"x\":304,\"y\":932},\"properties\":{},\"pointsList\":[{\"x\":128,\"y\":876},{\"x\":128,\"y\":976},{\"x\":304,\"y\":832},{\"x\":304,\"y\":932}]},{\"id\":\"e8\",\"type\":\"bezier\",\"sourceNodeId\":\"script_parse_c\",\"targetNodeId\":\"cond_is_done\",\"startPoint\":{\"x\":304,\"y\":988},\"endPoint\":{\"x\":304,\"y\":1060},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":988},{\"x\":304,\"y\":1088},{\"x\":304,\"y\":960},{\"x\":304,\"y\":1060}]},{\"id\":\"e9\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":378,\"y\":1088},\"endPoint\":{\"x\":426,\"y\":1200},\"properties\":{\"conditionType\":\"custom\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":402,\"y\":1144,\"value\":\"是\"},\"pointsList\":[{\"x\":378,\"y\":1088},{\"x\":478,\"y\":1088},{\"x\":326,\"y\":1200},{\"x\":426,\"y\":1200}]},{\"id\":\"e10\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":1060},\"endPoint\":{\"x\":304,\"y\":748},\"properties\":{\"conditionType\":\"default\"},\"text\":{\"x\":304,\"y\":904,\"value\":\"否\"},\"pointsList\":[{\"x\":304,\"y\":1060},{\"x\":304,\"y\":960},{\"x\":304,\"y\":848},{\"x\":304,\"y\":748}]}]}', '2026-05-18 10:07:46', '2026-05-18 10:28:24', NULL, NULL, 0);
INSERT INTO `wf_flow_definition` VALUES (2056225968668884994, 'FLOW_1779068031191', 'B系统提交-完税状态轮询流程', 3, NULL, 'event', NULL, 2, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":128,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":128,\"value\":\"开始\"}},{\"id\":\"db_query\",\"type\":\"db\",\"x\":304,\"y\":240,\"properties\":{\"name\":\"查询收件数据\",\"code\":\"db_query\",\"dsCode\":\"biz_db\",\"operation\":\"select\",\"sql\":\"SELECT * FROM com_business_notify WHERE receive_num = #{_businessKey} limit 1\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\",\"resultVarName\":\"aData\"},\"text\":{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}},{\"id\":\"script_assemble\",\"type\":\"script\",\"x\":304,\"y\":368,\"properties\":{\"name\":\"组装B系统报文\",\"code\":\"script_assemble\",\"scriptContent\":\"def a = context.get(\'aData\'); \\r\\ndef b = [:]; \\r\\nb.projectName = a.apply_subject; \\r\\nb.projectNo = a.receive_num; \\r\\ncontext.set(\'bRequest\', b); \\r\\nreturn b;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.projectName\\\",\\\"target\\\":\\\"context.projectName\\\"},{\\\"source\\\":\\\"result.projectNo\\\",\\\"target\\\":\\\"context.projectNo\\\"}]\"},\"text\":{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}},{\"id\":\"api_call_b\",\"type\":\"api\",\"x\":304,\"y\":480,\"properties\":{\"name\":\"调用B系统提交\",\"code\":\"api_call_b\",\"apiCode\":\"b-sys-example-submit\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectName\\\",\\\"target\\\":\\\"body.projectName\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectNo\\\",\\\"target\\\":\\\"body.projectNo\\\"}]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}},{\"id\":\"script_assemble_c\",\"type\":\"script\",\"x\":304,\"y\":608,\"properties\":{\"name\":\"组装C接口请求\",\"code\":\"script_assemble_c\",\"scriptContent\":\"def req = [:]; \\r\\nreq.appid = \'XT000\'; \\r\\nreq.password = \'SZRZYT\'; \\r\\nreq.intrfaceid = \'SNSW.FCSB.GETWSQKXX\'; \\r\\nreq.HTBH = context.get(\'projectNo\'); \\r\\ndef aData = context.get(\'aData\'); \\r\\nreq.QXDM = (aData instanceof List && aData.size() > 0) ? aData[0].region_code : (aData?.region_code ?: \'\'); \\r\\ncontext.set(\'cRequest\', req); \\r\\nreturn req;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result\\\",\\\"target\\\":\\\"context.cRequest\\\"}]\"},\"text\":{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}},{\"id\":\"timer_wait\",\"type\":\"timer\",\"x\":304,\"y\":720,\"properties\":{\"name\":\"等待60秒\",\"code\":\"timer_wait\",\"timerType\":\"delay\",\"delaySeconds\":60},\"text\":{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}},{\"id\":\"api_poll_c\",\"type\":\"api\",\"x\":304,\"y\":848,\"properties\":{\"name\":\"轮询C接口状态\",\"code\":\"api_poll_c\",\"apiCode\":\"b-sys-query-wsqkxx\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.cRequest\\\",\\\"target\\\":\\\"body\\\"}]\",\"outputMapping\":\"[{\\\"source\\\":\\\"body\\\",\\\"target\\\":\\\"context.cResponseBody\\\"}]\"},\"text\":{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}},{\"id\":\"script_parse_c\",\"type\":\"script\",\"x\":304,\"y\":960,\"properties\":{\"name\":\"解析C接口响应\",\"code\":\"script_parse_c\",\"scriptContent\":\"def bodyStr = context.get(\'cResponseBody\'); \\r\\nif (bodyStr == null) { context.set(\'wsbz\', \'N\'); \\r\\nreturn [:]; } \\r\\ndef resp = com.alibaba.fastjson2.JSON.parseObject(bodyStr); \\r\\ndef data = resp.getJSONObject(\'data\'); \\r\\ncontext.set(\'wsbz\', data?.getString(\'Wsbz\') ?: \'N\'); \\r\\ncontext.set(\'retCode\', data?.getString(\'RetCode\') ?: \'\'); \\r\\ncontext.set(\'cResult\', data); \\r\\nreturn data;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.Wsbz\\\",\\\"target\\\":\\\"context.wsbz\\\"},{\\\"source\\\":\\\"result.RetCode\\\",\\\"target\\\":\\\"context.retCode\\\"}]\"},\"text\":{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}},{\"id\":\"cond_is_done\",\"type\":\"condition\",\"x\":304,\"y\":1088,\"properties\":{\"name\":\"是否已办结\",\"code\":\"cond_is_done\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":496,\"y\":1200,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":496,\"y\":1200,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"db_query\",\"startPoint\":{\"x\":304,\"y\":154},\"endPoint\":{\"x\":304,\"y\":212},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":154},{\"x\":304,\"y\":254},{\"x\":304,\"y\":112},{\"x\":304,\"y\":212}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"db_query\",\"targetNodeId\":\"script_assemble\",\"startPoint\":{\"x\":304,\"y\":268},\"endPoint\":{\"x\":304,\"y\":340},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":268},{\"x\":304,\"y\":368},{\"x\":304,\"y\":240},{\"x\":304,\"y\":340}]},{\"id\":\"e3\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble\",\"targetNodeId\":\"api_call_b\",\"startPoint\":{\"x\":304,\"y\":396},\"endPoint\":{\"x\":304,\"y\":452},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":396},{\"x\":304,\"y\":496},{\"x\":304,\"y\":352},{\"x\":304,\"y\":452}]},{\"id\":\"e4\",\"type\":\"bezier\",\"sourceNodeId\":\"api_call_b\",\"targetNodeId\":\"script_assemble_c\",\"startPoint\":{\"x\":304,\"y\":508},\"endPoint\":{\"x\":304,\"y\":580},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":508},{\"x\":304,\"y\":608},{\"x\":304,\"y\":480},{\"x\":304,\"y\":580}]},{\"id\":\"e5\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble_c\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":636},\"endPoint\":{\"x\":304,\"y\":692},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":636},{\"x\":304,\"y\":736},{\"x\":304,\"y\":592},{\"x\":304,\"y\":692}]},{\"id\":\"e6\",\"type\":\"bezier\",\"sourceNodeId\":\"timer_wait\",\"targetNodeId\":\"api_poll_c\",\"startPoint\":{\"x\":304,\"y\":748},\"endPoint\":{\"x\":304,\"y\":820},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":748},{\"x\":304,\"y\":848},{\"x\":304,\"y\":720},{\"x\":304,\"y\":820}]},{\"id\":\"e7\",\"type\":\"bezier\",\"sourceNodeId\":\"api_poll_c\",\"targetNodeId\":\"script_parse_c\",\"startPoint\":{\"x\":304,\"y\":876},\"endPoint\":{\"x\":304,\"y\":932},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":876},{\"x\":304,\"y\":976},{\"x\":304,\"y\":832},{\"x\":304,\"y\":932}]},{\"id\":\"e8\",\"type\":\"bezier\",\"sourceNodeId\":\"script_parse_c\",\"targetNodeId\":\"cond_is_done\",\"startPoint\":{\"x\":304,\"y\":988},\"endPoint\":{\"x\":304,\"y\":1060},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":988},{\"x\":304,\"y\":1088},{\"x\":304,\"y\":960},{\"x\":304,\"y\":1060}]},{\"id\":\"e9\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":378,\"y\":1088},\"endPoint\":{\"x\":426,\"y\":1200},\"properties\":{\"conditionType\":\"custom\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":402,\"y\":1144,\"value\":\"是\"},\"pointsList\":[{\"x\":378,\"y\":1088},{\"x\":478,\"y\":1088},{\"x\":326,\"y\":1200},{\"x\":426,\"y\":1200}]},{\"id\":\"e10\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":1060},\"endPoint\":{\"x\":304,\"y\":748},\"properties\":{\"conditionType\":\"default\"},\"text\":{\"x\":122.87849473551256,\"y\":898.1112624294639,\"value\":\"否\"},\"pointsList\":[{\"x\":304,\"y\":1060},{\"x\":-62.848273686869625,\"y\":1006.6962090694541},{\"x\":-53.63774737108015,\"y\":777.7488406484014},{\"x\":304,\"y\":748}]}]}', '2026-05-18 12:10:48', '2026-05-18 14:39:29', NULL, NULL, 0);
INSERT INTO `wf_flow_definition` VALUES (2056255943241449474, 'FLOW_1779068031191', 'B系统提交-完税状态轮询流程', 4, NULL, 'event', NULL, 2, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":128,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":128,\"value\":\"开始\"}},{\"id\":\"db_query\",\"type\":\"db\",\"x\":304,\"y\":240,\"properties\":{\"name\":\"查询收件数据\",\"code\":\"db_query\",\"dsCode\":\"biz_db\",\"operation\":\"select\",\"sql\":\"SELECT * FROM com_business_notify WHERE receive_num = #{_businessKey} limit 1\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\",\"resultVarName\":\"aData\"},\"text\":{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}},{\"id\":\"script_assemble\",\"type\":\"script\",\"x\":304,\"y\":368,\"properties\":{\"name\":\"组装B系统报文\",\"code\":\"script_assemble\",\"scriptContent\":\"def a = context.get(\'aData\'); \\r\\ndef b = [:]; \\r\\nb.projectName = a.apply_subject; \\r\\nb.projectNo = a.receive_num; \\r\\ncontext.set(\'bRequest\', b); \\r\\nreturn b;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.projectName\\\",\\\"target\\\":\\\"context.projectName\\\"},{\\\"source\\\":\\\"result.projectNo\\\",\\\"target\\\":\\\"context.projectNo\\\"}]\"},\"text\":{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}},{\"id\":\"api_call_b\",\"type\":\"api\",\"x\":304,\"y\":480,\"properties\":{\"name\":\"调用B系统提交\",\"code\":\"api_call_b\",\"apiCode\":\"b-sys-example-submit\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectName\\\",\\\"target\\\":\\\"body.projectName\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectNo\\\",\\\"target\\\":\\\"body.projectNo\\\"}]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}},{\"id\":\"script_assemble_c\",\"type\":\"script\",\"x\":304,\"y\":608,\"properties\":{\"name\":\"组装C接口请求\",\"code\":\"script_assemble_c\",\"scriptContent\":\"def req = [:]; \\r\\nreq.appid = \'XT000\'; \\r\\nreq.password = \'SZRZYT\'; \\r\\nreq.intrfaceid = \'SNSW.FCSB.GETWSQKXX\'; \\r\\nreq.HTBH = context.get(\'projectNo\'); \\r\\ndef aData = context.get(\'aData\'); \\r\\nreq.QXDM = (aData instanceof List && aData.size() > 0) ? aData[0].region_code : (aData?.region_code ?: \'\'); \\r\\ncontext.set(\'cRequest\', req); \\r\\nreturn req;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result\\\",\\\"target\\\":\\\"context.cRequest\\\"}]\"},\"text\":{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}},{\"id\":\"timer_wait\",\"type\":\"timer\",\"x\":304,\"y\":720,\"properties\":{\"name\":\"等待60秒\",\"code\":\"timer_wait\",\"timerType\":\"delay\",\"delaySeconds\":60},\"text\":{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}},{\"id\":\"api_poll_c\",\"type\":\"api\",\"x\":304,\"y\":848,\"properties\":{\"name\":\"轮询C接口状态\",\"code\":\"api_poll_c\",\"apiCode\":\"b-sys-query-wsqkxx\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.cRequest\\\",\\\"target\\\":\\\"body\\\"}]\",\"outputMapping\":\"[{\\\"source\\\":\\\"body\\\",\\\"target\\\":\\\"context.cResponseBody\\\"}]\"},\"text\":{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}},{\"id\":\"script_parse_c\",\"type\":\"script\",\"x\":304,\"y\":960,\"properties\":{\"name\":\"解析C接口响应\",\"code\":\"script_parse_c\",\"scriptContent\":\"def bodyStr = context.get(\'cResponseBody\'); \\r\\nif (bodyStr == null) { context.set(\'wsbz\', \'N\'); \\r\\nreturn [:]; } \\r\\ndef resp = com.alibaba.fastjson2.JSON.parseObject(bodyStr); \\r\\ndef data = resp.getJSONObject(\'data\'); \\r\\ncontext.set(\'wsbz\', data?.getString(\'Wsbz\') ?: \'N\'); \\r\\ncontext.set(\'retCode\', data?.getString(\'RetCode\') ?: \'\'); \\r\\ncontext.set(\'cResult\', data); \\r\\nreturn data;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.Wsbz\\\",\\\"target\\\":\\\"context.wsbz\\\"},{\\\"source\\\":\\\"result.RetCode\\\",\\\"target\\\":\\\"context.retCode\\\"}]\"},\"text\":{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}},{\"id\":\"cond_is_done\",\"type\":\"condition\",\"x\":304,\"y\":1088,\"properties\":{\"name\":\"是否已办结\",\"code\":\"cond_is_done\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":496,\"y\":1200,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":496,\"y\":1200,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"db_query\",\"startPoint\":{\"x\":304,\"y\":154},\"endPoint\":{\"x\":304,\"y\":212},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":154},{\"x\":304,\"y\":254},{\"x\":304,\"y\":112},{\"x\":304,\"y\":212}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"db_query\",\"targetNodeId\":\"script_assemble\",\"startPoint\":{\"x\":304,\"y\":268},\"endPoint\":{\"x\":304,\"y\":340},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":268},{\"x\":304,\"y\":368},{\"x\":304,\"y\":240},{\"x\":304,\"y\":340}]},{\"id\":\"e3\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble\",\"targetNodeId\":\"api_call_b\",\"startPoint\":{\"x\":304,\"y\":396},\"endPoint\":{\"x\":304,\"y\":452},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":396},{\"x\":304,\"y\":496},{\"x\":304,\"y\":352},{\"x\":304,\"y\":452}]},{\"id\":\"e4\",\"type\":\"bezier\",\"sourceNodeId\":\"api_call_b\",\"targetNodeId\":\"script_assemble_c\",\"startPoint\":{\"x\":304,\"y\":508},\"endPoint\":{\"x\":304,\"y\":580},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":508},{\"x\":304,\"y\":608},{\"x\":304,\"y\":480},{\"x\":304,\"y\":580}]},{\"id\":\"e5\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble_c\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":636},\"endPoint\":{\"x\":304,\"y\":692},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":636},{\"x\":304,\"y\":736},{\"x\":304,\"y\":592},{\"x\":304,\"y\":692}]},{\"id\":\"e6\",\"type\":\"bezier\",\"sourceNodeId\":\"timer_wait\",\"targetNodeId\":\"api_poll_c\",\"startPoint\":{\"x\":304,\"y\":748},\"endPoint\":{\"x\":304,\"y\":820},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":748},{\"x\":304,\"y\":848},{\"x\":304,\"y\":720},{\"x\":304,\"y\":820}]},{\"id\":\"e7\",\"type\":\"bezier\",\"sourceNodeId\":\"api_poll_c\",\"targetNodeId\":\"script_parse_c\",\"startPoint\":{\"x\":304,\"y\":876},\"endPoint\":{\"x\":304,\"y\":932},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":876},{\"x\":304,\"y\":976},{\"x\":304,\"y\":832},{\"x\":304,\"y\":932}]},{\"id\":\"e8\",\"type\":\"bezier\",\"sourceNodeId\":\"script_parse_c\",\"targetNodeId\":\"cond_is_done\",\"startPoint\":{\"x\":304,\"y\":988},\"endPoint\":{\"x\":304,\"y\":1060},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":988},{\"x\":304,\"y\":1088},{\"x\":304,\"y\":960},{\"x\":304,\"y\":1060}]},{\"id\":\"e9\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":378,\"y\":1088},\"endPoint\":{\"x\":426,\"y\":1200},\"properties\":{\"conditionType\":\"custom\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":402,\"y\":1144,\"value\":\"是\"},\"pointsList\":[{\"x\":378,\"y\":1088},{\"x\":478,\"y\":1088},{\"x\":326,\"y\":1200},{\"x\":426,\"y\":1200}]},{\"id\":\"e10\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":1060},\"endPoint\":{\"x\":304,\"y\":748},\"properties\":{\"conditionType\":\"default\"},\"text\":{\"x\":122.87849473551256,\"y\":898.1112624294639,\"value\":\"否\"},\"pointsList\":[{\"x\":304,\"y\":1060},{\"x\":-62.848273686869625,\"y\":1006.6962090694541},{\"x\":-53.63774737108015,\"y\":777.7488406484014},{\"x\":304,\"y\":748}]}]}', '2026-05-18 14:09:55', '2026-05-18 14:39:32', NULL, NULL, 0);
INSERT INTO `wf_flow_definition` VALUES (2056256708752261121, 'FLOW_1779068031191', 'B系统提交-完税状态轮询流程', 5, NULL, 'event', NULL, 1, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":128,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":128,\"value\":\"开始\"}},{\"id\":\"db_query\",\"type\":\"db\",\"x\":304,\"y\":240,\"properties\":{\"name\":\"查询收件数据\",\"code\":\"db_query\",\"dsCode\":\"biz_db\",\"operation\":\"select\",\"sql\":\"SELECT * FROM com_business_notify WHERE receive_num = #{_businessKey} limit 1\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\",\"resultVarName\":\"aData\"},\"text\":{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}},{\"id\":\"script_assemble\",\"type\":\"script\",\"x\":304,\"y\":368,\"properties\":{\"name\":\"组装B系统报文\",\"code\":\"script_assemble\",\"scriptContent\":\"def a = context.get(\'aData\'); \\r\\ndef b = [:]; \\r\\nb.projectName = a.apply_subject; \\r\\nb.projectNo = a.receive_num; \\r\\ncontext.set(\'bRequest\', b); \\r\\nreturn b;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.projectName\\\",\\\"target\\\":\\\"context.projectName\\\"},{\\\"source\\\":\\\"result.projectNo\\\",\\\"target\\\":\\\"context.projectNo\\\"}]\"},\"text\":{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}},{\"id\":\"api_call_b\",\"type\":\"api\",\"x\":304,\"y\":480,\"properties\":{\"name\":\"调用B系统提交\",\"code\":\"api_call_b\",\"apiCode\":\"b-sys-example-submit\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectName\\\",\\\"target\\\":\\\"body.projectName\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectNo\\\",\\\"target\\\":\\\"body.projectNo\\\"}]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}},{\"id\":\"script_assemble_c\",\"type\":\"script\",\"x\":304,\"y\":608,\"properties\":{\"name\":\"组装C接口请求\",\"code\":\"script_assemble_c\",\"scriptContent\":\"def req = [:]; \\r\\nreq.appid = \'XT000\'; \\r\\nreq.password = \'SZRZYT\'; \\r\\nreq.intrfaceid = \'SNSW.FCSB.GETWSQKXX\'; \\r\\nreq.HTBH = context.get(\'projectNo\'); \\r\\ndef aData = context.get(\'aData\'); \\r\\nreq.QXDM = (aData instanceof List && aData.size() > 0) ? aData[0].region_code : (aData?.region_code ?: \'\'); \\r\\ncontext.set(\'cRequest\', req); \\r\\nreturn req;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.appid\\\",\\\"target\\\":\\\"context.appid\\\"},{\\\"source\\\":\\\"result.password\\\",\\\"target\\\":\\\"context.password\\\"},{\\\"source\\\":\\\"result.intrfaceid\\\",\\\"target\\\":\\\"context.intrfaceid\\\"},{\\\"source\\\":\\\"result.HTBH\\\",\\\"target\\\":\\\"context.HTBH\\\"},{\\\"source\\\":\\\"result.QXDM\\\",\\\"target\\\":\\\"context.QXDM\\\"}]\"},\"text\":{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}},{\"id\":\"timer_wait\",\"type\":\"timer\",\"x\":304,\"y\":720,\"properties\":{\"name\":\"等待60秒\",\"code\":\"timer_wait\",\"timerType\":\"delay\",\"delaySeconds\":60},\"text\":{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}},{\"id\":\"api_poll_c\",\"type\":\"api\",\"x\":304,\"y\":848,\"properties\":{\"name\":\"轮询C接口状态\",\"code\":\"api_poll_c\",\"apiCode\":\"b-sys-query-wsqkxx\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.nodeResult_script_assemble_c.appid\\\",\\\"target\\\":\\\"body.appid\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble_c.password\\\",\\\"target\\\":\\\"body.password\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble_c.intrfaceid\\\",\\\"target\\\":\\\"body.intrfaceid\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble_c.HTBH\\\",\\\"target\\\":\\\"body.HTBH\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble_c.QXDM\\\",\\\"target\\\":\\\"body.QXDM\\\"}]\",\"outputMapping\":\"[{\\\"source\\\":\\\"body\\\",\\\"target\\\":\\\"context.cResponseBody\\\"}]\"},\"text\":{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}},{\"id\":\"script_parse_c\",\"type\":\"script\",\"x\":304,\"y\":960,\"properties\":{\"name\":\"解析C接口响应\",\"code\":\"script_parse_c\",\"scriptContent\":\"def bodyStr = context.get(\'cResponseBody\'); \\r\\nif (bodyStr == null) { context.set(\'wsbz\', \'N\'); \\r\\nreturn [:]; } \\r\\ndef resp = com.alibaba.fastjson2.JSON.parseObject(bodyStr); \\r\\ndef data = resp.getJSONObject(\'data\'); \\r\\ncontext.set(\'wsbz\', data?.getString(\'Wsbz\') ?: \'N\'); \\r\\ncontext.set(\'retCode\', data?.getString(\'RetCode\') ?: \'\'); \\r\\ncontext.set(\'cResult\', data); \\r\\nreturn data;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.Wsbz\\\",\\\"target\\\":\\\"context.wsbz\\\"},{\\\"source\\\":\\\"result.RetCode\\\",\\\"target\\\":\\\"context.retCode\\\"}]\"},\"text\":{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}},{\"id\":\"cond_is_done\",\"type\":\"condition\",\"x\":304,\"y\":1088,\"properties\":{\"name\":\"是否已办结\",\"code\":\"cond_is_done\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":496,\"y\":1200,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":496,\"y\":1200,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"db_query\",\"startPoint\":{\"x\":304,\"y\":154},\"endPoint\":{\"x\":304,\"y\":212},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":154},{\"x\":304,\"y\":254},{\"x\":304,\"y\":112},{\"x\":304,\"y\":212}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"db_query\",\"targetNodeId\":\"script_assemble\",\"startPoint\":{\"x\":304,\"y\":268},\"endPoint\":{\"x\":304,\"y\":340},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":268},{\"x\":304,\"y\":368},{\"x\":304,\"y\":240},{\"x\":304,\"y\":340}]},{\"id\":\"e3\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble\",\"targetNodeId\":\"api_call_b\",\"startPoint\":{\"x\":304,\"y\":396},\"endPoint\":{\"x\":304,\"y\":452},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":396},{\"x\":304,\"y\":496},{\"x\":304,\"y\":352},{\"x\":304,\"y\":452}]},{\"id\":\"e4\",\"type\":\"bezier\",\"sourceNodeId\":\"api_call_b\",\"targetNodeId\":\"script_assemble_c\",\"startPoint\":{\"x\":304,\"y\":508},\"endPoint\":{\"x\":304,\"y\":580},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":508},{\"x\":304,\"y\":608},{\"x\":304,\"y\":480},{\"x\":304,\"y\":580}]},{\"id\":\"e5\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble_c\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":636},\"endPoint\":{\"x\":304,\"y\":692},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":636},{\"x\":304,\"y\":736},{\"x\":304,\"y\":592},{\"x\":304,\"y\":692}]},{\"id\":\"e6\",\"type\":\"bezier\",\"sourceNodeId\":\"timer_wait\",\"targetNodeId\":\"api_poll_c\",\"startPoint\":{\"x\":304,\"y\":748},\"endPoint\":{\"x\":304,\"y\":820},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":748},{\"x\":304,\"y\":848},{\"x\":304,\"y\":720},{\"x\":304,\"y\":820}]},{\"id\":\"e7\",\"type\":\"bezier\",\"sourceNodeId\":\"api_poll_c\",\"targetNodeId\":\"script_parse_c\",\"startPoint\":{\"x\":304,\"y\":876},\"endPoint\":{\"x\":304,\"y\":932},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":876},{\"x\":304,\"y\":976},{\"x\":304,\"y\":832},{\"x\":304,\"y\":932}]},{\"id\":\"e8\",\"type\":\"bezier\",\"sourceNodeId\":\"script_parse_c\",\"targetNodeId\":\"cond_is_done\",\"startPoint\":{\"x\":304,\"y\":988},\"endPoint\":{\"x\":304,\"y\":1060},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":988},{\"x\":304,\"y\":1088},{\"x\":304,\"y\":960},{\"x\":304,\"y\":1060}]},{\"id\":\"e9\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":378,\"y\":1088},\"endPoint\":{\"x\":426,\"y\":1200},\"properties\":{\"conditionType\":\"custom\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":402,\"y\":1144,\"value\":\"是\"},\"pointsList\":[{\"x\":378,\"y\":1088},{\"x\":478,\"y\":1088},{\"x\":326,\"y\":1200},{\"x\":426,\"y\":1200}]},{\"id\":\"e10\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":1060},\"endPoint\":{\"x\":304,\"y\":748},\"properties\":{\"conditionType\":\"default\"},\"text\":{\"x\":122.87849473551256,\"y\":898.1112624294639,\"value\":\"否\"},\"pointsList\":[{\"x\":304,\"y\":1060},{\"x\":-62.848273686869625,\"y\":1006.6962090694541},{\"x\":-53.63774737108015,\"y\":777.7488406484014},{\"x\":304,\"y\":748}]}]}', '2026-05-18 14:12:57', '2026-05-18 14:40:22', NULL, NULL, 0);
INSERT INTO `wf_flow_definition` VALUES (2056307752567889922, 'FLOW_1779068031191', 'B系统提交-完税状态轮询流程', 6, NULL, 'event', NULL, 1, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":128,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":128,\"value\":\"开始\"}},{\"id\":\"db_query\",\"type\":\"db\",\"x\":304,\"y\":240,\"properties\":{\"name\":\"查询收件数据\",\"code\":\"db_query\",\"dsCode\":\"biz_db\",\"operation\":\"select\",\"sql\":\"SELECT * FROM com_business_notify WHERE receive_num = #{_businessKey} limit 1\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\",\"resultVarName\":\"aData\"},\"text\":{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}},{\"id\":\"script_assemble\",\"type\":\"script\",\"x\":304,\"y\":368,\"properties\":{\"name\":\"组装B系统报文\",\"code\":\"script_assemble\",\"scriptContent\":\"def a = context.get(\'aData\'); \\r\\ndef b = [:]; \\r\\nb.projectName = a.apply_subject; \\r\\nb.projectNo = a.receive_num; \\r\\ncontext.set(\'bRequest\', b); \\r\\nreturn b;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.projectName\\\",\\\"target\\\":\\\"context.projectName\\\"},{\\\"source\\\":\\\"result.projectNo\\\",\\\"target\\\":\\\"context.projectNo\\\"}]\"},\"text\":{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}},{\"id\":\"api_call_b\",\"type\":\"api\",\"x\":304,\"y\":480,\"properties\":{\"name\":\"调用B系统提交\",\"code\":\"api_call_b\",\"apiCode\":\"b-sys-example-submit\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectName\\\",\\\"target\\\":\\\"body.projectName\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectNo\\\",\\\"target\\\":\\\"body.projectNo\\\"}]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}},{\"id\":\"script_assemble_c\",\"type\":\"script\",\"x\":304,\"y\":608,\"properties\":{\"name\":\"组装C接口请求\",\"code\":\"script_assemble_c\",\"scriptContent\":\"def req = [:]; \\r\\nreq.appid = \'XT000\'; \\r\\nreq.password = \'SZRZYT\'; \\r\\nreq.intrfaceid = \'SNSW.FCSB.GETWSQKXX\'; \\r\\nreq.HTBH = context.get(\'projectNo\'); \\r\\ndef aData = context.get(\'aData\'); \\r\\nreq.QXDM = (aData instanceof List && aData.size() > 0) ? aData[0].region_code : (aData?.region_code ?: \'\'); \\r\\ncontext.set(\'cRequest\', req); \\r\\nreturn req;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.appid\\\",\\\"target\\\":\\\"context.appid\\\"},{\\\"source\\\":\\\"result.password\\\",\\\"target\\\":\\\"context.password\\\"},{\\\"source\\\":\\\"result.intrfaceid\\\",\\\"target\\\":\\\"context.intrfaceid\\\"},{\\\"source\\\":\\\"result.HTBH\\\",\\\"target\\\":\\\"context.HTBH\\\"},{\\\"source\\\":\\\"result.QXDM\\\",\\\"target\\\":\\\"context.QXDM\\\"}]\"},\"text\":{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}},{\"id\":\"timer_wait\",\"type\":\"timer\",\"x\":304,\"y\":720,\"properties\":{\"name\":\"等待60秒\",\"code\":\"timer_wait\",\"timerType\":\"delay\",\"delaySeconds\":60},\"text\":{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}},{\"id\":\"api_poll_c\",\"type\":\"api\",\"x\":304,\"y\":848,\"properties\":{\"name\":\"轮询C接口状态\",\"code\":\"api_poll_c\",\"apiCode\":\"b-sys-query-wsqkxx\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.nodeResult_script_assemble_c.appid\\\",\\\"target\\\":\\\"body.appid\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble_c.password\\\",\\\"target\\\":\\\"body.password\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble_c.intrfaceid\\\",\\\"target\\\":\\\"body.intrfaceid\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble_c.HTBH\\\",\\\"target\\\":\\\"body.HTBH\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble_c.QXDM\\\",\\\"target\\\":\\\"body.QXDM\\\"}]\",\"outputMapping\":\"[{\\\"source\\\":\\\"body\\\",\\\"target\\\":\\\"context.cResponseBody\\\"}]\"},\"text\":{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}},{\"id\":\"script_parse_c\",\"type\":\"script\",\"x\":304,\"y\":960,\"properties\":{\"name\":\"解析C接口响应\",\"code\":\"script_parse_c\",\"scriptContent\":\"def bodyStr = context.get(\'cResponseBody\'); \\r\\nif (bodyStr == null) { context.set(\'wsbz\', \'N\'); \\r\\nreturn [:]; } \\r\\ndef resp = com.alibaba.fastjson2.JSON.parseObject(bodyStr); \\r\\ndef data = resp.getJSONObject(\'data\'); \\r\\ncontext.set(\'wsbz\', data?.getString(\'Wsbz\') ?: \'N\'); \\r\\ncontext.set(\'retCode\', data?.getString(\'RetCode\') ?: \'\'); \\r\\ncontext.set(\'cResult\', data); \\r\\nreturn data;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.Wsbz\\\",\\\"target\\\":\\\"context.wsbz\\\"},{\\\"source\\\":\\\"result.RetCode\\\",\\\"target\\\":\\\"context.retCode\\\"}]\"},\"text\":{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}},{\"id\":\"cond_is_done\",\"type\":\"condition\",\"x\":304,\"y\":1088,\"properties\":{\"name\":\"是否已办结\",\"code\":\"cond_is_done\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\"},\"text\":{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":496,\"y\":1200,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":496,\"y\":1200,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"db_query\",\"startPoint\":{\"x\":304,\"y\":154},\"endPoint\":{\"x\":304,\"y\":212},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":154},{\"x\":304,\"y\":254},{\"x\":304,\"y\":112},{\"x\":304,\"y\":212}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"db_query\",\"targetNodeId\":\"script_assemble\",\"startPoint\":{\"x\":304,\"y\":268},\"endPoint\":{\"x\":304,\"y\":340},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":268},{\"x\":304,\"y\":368},{\"x\":304,\"y\":240},{\"x\":304,\"y\":340}]},{\"id\":\"e3\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble\",\"targetNodeId\":\"api_call_b\",\"startPoint\":{\"x\":304,\"y\":396},\"endPoint\":{\"x\":304,\"y\":452},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":396},{\"x\":304,\"y\":496},{\"x\":304,\"y\":352},{\"x\":304,\"y\":452}]},{\"id\":\"e4\",\"type\":\"bezier\",\"sourceNodeId\":\"api_call_b\",\"targetNodeId\":\"script_assemble_c\",\"startPoint\":{\"x\":304,\"y\":508},\"endPoint\":{\"x\":304,\"y\":580},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":508},{\"x\":304,\"y\":608},{\"x\":304,\"y\":480},{\"x\":304,\"y\":580}]},{\"id\":\"e5\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble_c\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":636},\"endPoint\":{\"x\":304,\"y\":692},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":636},{\"x\":304,\"y\":736},{\"x\":304,\"y\":592},{\"x\":304,\"y\":692}]},{\"id\":\"e6\",\"type\":\"bezier\",\"sourceNodeId\":\"timer_wait\",\"targetNodeId\":\"api_poll_c\",\"startPoint\":{\"x\":304,\"y\":748},\"endPoint\":{\"x\":304,\"y\":820},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":748},{\"x\":304,\"y\":848},{\"x\":304,\"y\":720},{\"x\":304,\"y\":820}]},{\"id\":\"e7\",\"type\":\"bezier\",\"sourceNodeId\":\"api_poll_c\",\"targetNodeId\":\"script_parse_c\",\"startPoint\":{\"x\":304,\"y\":876},\"endPoint\":{\"x\":304,\"y\":932},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":876},{\"x\":304,\"y\":976},{\"x\":304,\"y\":832},{\"x\":304,\"y\":932}]},{\"id\":\"e8\",\"type\":\"bezier\",\"sourceNodeId\":\"script_parse_c\",\"targetNodeId\":\"cond_is_done\",\"startPoint\":{\"x\":304,\"y\":988},\"endPoint\":{\"x\":304,\"y\":1060},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":988},{\"x\":304,\"y\":1088},{\"x\":304,\"y\":960},{\"x\":304,\"y\":1060}]},{\"id\":\"e9\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":378,\"y\":1088},\"endPoint\":{\"x\":426,\"y\":1200},\"properties\":{\"conditionType\":\"custom\",\"conditionExpression\":\"#{context.wsbz == \'Y\'}\",\"priority\":1},\"text\":{\"x\":402,\"y\":1144,\"value\":\"是\"},\"pointsList\":[{\"x\":378,\"y\":1088},{\"x\":478,\"y\":1088},{\"x\":326,\"y\":1200},{\"x\":426,\"y\":1200}]},{\"id\":\"e10\",\"type\":\"bezier\",\"sourceNodeId\":\"cond_is_done\",\"targetNodeId\":\"timer_wait\",\"startPoint\":{\"x\":304,\"y\":1060},\"endPoint\":{\"x\":304,\"y\":748},\"properties\":{\"conditionType\":\"default\",\"priority\":2},\"text\":{\"x\":122.87849473551256,\"y\":898.1112624294639,\"value\":\"否\"},\"pointsList\":[{\"x\":304,\"y\":1060},{\"x\":-62.848273686869625,\"y\":1006.6962090694541},{\"x\":-53.63774737108015,\"y\":777.7488406484014},{\"x\":304,\"y\":748}]}]}', '2026-05-18 17:35:47', '2026-05-18 18:09:05', NULL, NULL, 0);
INSERT INTO `wf_flow_definition` VALUES (2056313274914377730, 'FLOW_1778863694671', '中台数据中转流程', 2, NULL, 'event', NULL, 0, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":128,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":128,\"value\":\"开始\"}},{\"id\":\"db_query\",\"type\":\"db\",\"x\":304,\"y\":240,\"properties\":{\"name\":\"查询收件数据\",\"code\":\"db_query\",\"dsCode\":\"biz_db\",\"operation\":\"select\",\"sql\":\"SELECT * FROM com_business_notify WHERE receive_num = #{_businessKey} limit 1\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\",\"resultVarName\":\"aData\"},\"text\":{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}},{\"id\":\"script_assemble\",\"type\":\"script\",\"x\":304,\"y\":368,\"properties\":{\"name\":\"组装B系统报文\",\"code\":\"script_assemble\",\"scriptContent\":\"def a = context.get(\'aData\'); def b = [:]; b.projectName = a.apply_subject; b.projectNo = a.receive_num; context.set(\'bRequest\', b); return b;\",\"inputMapping\":\"[]\",\"outputMapping\":\"[{\\\"source\\\":\\\"result.projectName\\\",\\\"target\\\":\\\"context.projectName\\\"},{\\\"source\\\":\\\"result.projectNo\\\",\\\"target\\\":\\\"context.projectNo\\\"}]\"},\"text\":{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}},{\"id\":\"api_call_b\",\"type\":\"api\",\"x\":304,\"y\":480,\"properties\":{\"name\":\"调用B系统提交\",\"code\":\"api_call_b\",\"apiCode\":\"b-sys-example-submit\",\"timeout\":30000,\"failStrategy\":\"suspend\",\"inputMapping\":\"[{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectName\\\",\\\"target\\\":\\\"body.projectName\\\"},{\\\"source\\\":\\\"context.nodeResult_script_assemble.projectNo\\\",\\\"target\\\":\\\"body.projectNo\\\"}]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":352,\"y\":624,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":352,\"y\":624,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"db_query\",\"startPoint\":{\"x\":304,\"y\":154},\"endPoint\":{\"x\":304,\"y\":212},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":154},{\"x\":304,\"y\":254},{\"x\":304,\"y\":112},{\"x\":304,\"y\":212}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"db_query\",\"targetNodeId\":\"script_assemble\",\"startPoint\":{\"x\":304,\"y\":268},\"endPoint\":{\"x\":304,\"y\":340},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":268},{\"x\":304,\"y\":368},{\"x\":304,\"y\":240},{\"x\":304,\"y\":340}]},{\"id\":\"e3\",\"type\":\"bezier\",\"sourceNodeId\":\"script_assemble\",\"targetNodeId\":\"api_call_b\",\"startPoint\":{\"x\":304,\"y\":396},\"endPoint\":{\"x\":304,\"y\":452},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":396},{\"x\":304,\"y\":496},{\"x\":304,\"y\":352},{\"x\":304,\"y\":452}]},{\"id\":\"60d11cef-8b75-42cf-a609-f6a8e5df8b33\",\"type\":\"bezier\",\"sourceNodeId\":\"api_call_b\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":304,\"y\":508},\"endPoint\":{\"x\":352,\"y\":598},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":508},{\"x\":304,\"y\":608},{\"x\":352,\"y\":498},{\"x\":352,\"y\":598}]}]}', '2026-05-18 17:57:44', '2026-05-18 17:57:44', NULL, NULL, 0);
INSERT INTO `wf_flow_definition` VALUES (2057112300765118465, 'minio-upload-demo', 'MinIO文件上传示例', 2, NULL, 'manual', NULL, 2, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":96,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":96,\"value\":\"开始\"}},{\"id\":\"minio_upload\",\"type\":\"minio\",\"x\":304,\"y\":224,\"properties\":{\"name\":\"上传文件到MinIO\",\"code\":\"minio_upload\",\"endpoint\":\"http://localhost:9000\",\"accessKey\":\"admin\",\"secretKey\":\"12345678\",\"bucket\":\"riverflow\",\"operation\":\"upload\",\"filePath\":\"${context.filePath}\",\"objectName\":\"${context.fileName}\",\"contentType\":\"application/octet-stream\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":304,\"y\":336,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":304,\"y\":336,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"minio_upload\",\"startPoint\":{\"x\":304,\"y\":122},\"endPoint\":{\"x\":304,\"y\":196},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":122},{\"x\":304,\"y\":222},{\"x\":304,\"y\":96},{\"x\":304,\"y\":196}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"minio_upload\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":304,\"y\":252},\"endPoint\":{\"x\":304,\"y\":310},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":252},{\"x\":304,\"y\":352},{\"x\":304,\"y\":210},{\"x\":304,\"y\":310}]}]}', '2026-05-20 22:52:46', '2026-05-20 22:57:18', NULL, NULL, 0);
INSERT INTO `wf_flow_definition` VALUES (2057113253346082818, 'minio-upload-demo', 'MinIO文件上传示例', 3, NULL, 'manual', NULL, 1, '{\"nodes\":[{\"id\":\"start_1\",\"type\":\"start\",\"x\":304,\"y\":96,\"properties\":{\"name\":\"开始\",\"code\":\"start_1\"},\"text\":{\"x\":304,\"y\":96,\"value\":\"开始\"}},{\"id\":\"minio_upload\",\"type\":\"minio\",\"x\":304,\"y\":224,\"properties\":{\"name\":\"上传文件到MinIO\",\"code\":\"minio_upload\",\"endpoint\":\"http://localhost:9000\",\"accessKey\":\"admin\",\"secretKey\":\"12345678\",\"bucket\":\"riverflow\",\"operation\":\"upload\",\"filePath\":\"${context.filePath}\",\"objectName\":\"${context.fileName}\",\"contentType\":\"application/octet-stream\",\"inputMapping\":\"[]\",\"outputMapping\":\"[]\"},\"text\":{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}},{\"id\":\"end_1\",\"type\":\"end\",\"x\":304,\"y\":336,\"properties\":{\"name\":\"结束\",\"code\":\"end_1\"},\"text\":{\"x\":304,\"y\":336,\"value\":\"结束\"}}],\"edges\":[{\"id\":\"e1\",\"type\":\"bezier\",\"sourceNodeId\":\"start_1\",\"targetNodeId\":\"minio_upload\",\"startPoint\":{\"x\":304,\"y\":122},\"endPoint\":{\"x\":304,\"y\":196},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":122},{\"x\":304,\"y\":222},{\"x\":304,\"y\":96},{\"x\":304,\"y\":196}]},{\"id\":\"e2\",\"type\":\"bezier\",\"sourceNodeId\":\"minio_upload\",\"targetNodeId\":\"end_1\",\"startPoint\":{\"x\":304,\"y\":252},\"endPoint\":{\"x\":304,\"y\":310},\"properties\":{},\"pointsList\":[{\"x\":304,\"y\":252},{\"x\":304,\"y\":352},{\"x\":304,\"y\":210},{\"x\":304,\"y\":310}]}]}', '2026-05-20 22:56:34', '2026-05-20 22:57:00', NULL, NULL, 0);

-- ----------------------------
-- Table structure for wf_flow_edge
-- ----------------------------
DROP TABLE IF EXISTS `wf_flow_edge`;
CREATE TABLE `wf_flow_edge`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `flow_id` bigint(20) NOT NULL COMMENT '所属流程定义ID',
  `edge_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '画布边ID',
  `source_node` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '源节点ID',
  `target_node` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标节点ID',
  `condition_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'default' COMMENT '条件类型：default/success/fail/custom',
  `condition_expression` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自定义条件表达式（SpEL）',
  `priority` int(11) NULL DEFAULT 0 COMMENT '优先级',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flow_edge`(`flow_id`, `edge_id`) USING BTREE,
  INDEX `idx_flow`(`flow_id`) USING BTREE,
  INDEX `idx_source`(`source_node`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程边' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_flow_edge
-- ----------------------------
INSERT INTO `wf_flow_edge` VALUES (2055329425241726977, 1003, 'e1', 'start_1', 'db_query', 'default', NULL, 0, '2026-05-16 00:48:16', '2026-05-16 00:48:16', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2055329425241726978, 1003, 'e2', 'db_query', 'script_assemble', 'default', NULL, 1, '2026-05-16 00:48:16', '2026-05-16 00:48:16', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2055329425241726979, 1003, 'e3', 'script_assemble', 'api_call_b', 'default', NULL, 2, '2026-05-16 00:48:16', '2026-05-16 00:48:16', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2055329425241726980, 1003, '60d11cef-8b75-42cf-a609-f6a8e5df8b33', 'api_call_b', 'end_1', 'default', NULL, 3, '2026-05-16 00:48:16', '2026-05-16 00:48:16', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056186481427402754, 1004, 'e1', 'start_1', 'db_query', 'default', NULL, 0, '2026-05-18 09:33:54', '2026-05-18 09:33:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056186481427402755, 1004, 'e2', 'db_query', 'script_assemble', 'default', NULL, 1, '2026-05-18 09:33:54', '2026-05-18 09:33:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056186481490317313, 1004, 'e3', 'script_assemble', 'api_call_b', 'default', NULL, 2, '2026-05-18 09:33:54', '2026-05-18 09:33:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056186481490317314, 1004, 'e4', 'api_call_b', 'script_assemble_c', 'default', NULL, 3, '2026-05-18 09:33:54', '2026-05-18 09:33:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056186481490317315, 1004, 'e5', 'script_assemble_c', 'timer_wait', 'default', NULL, 4, '2026-05-18 09:33:54', '2026-05-18 09:33:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056186481490317316, 1004, 'e6', 'timer_wait', 'api_poll_c', 'default', NULL, 5, '2026-05-18 09:33:54', '2026-05-18 09:33:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056186481490317317, 1004, 'e7', 'api_poll_c', 'script_parse_c', 'default', NULL, 6, '2026-05-18 09:33:54', '2026-05-18 09:33:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056186481490317318, 1004, 'e8', 'script_parse_c', 'cond_is_done', 'default', NULL, 7, '2026-05-18 09:33:54', '2026-05-18 09:33:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056186481490317319, 1004, 'e9', 'cond_is_done', 'end_1', 'custom', '#{context.wsbz == \'Y\'}', 8, '2026-05-18 09:33:54', '2026-05-18 09:33:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056186481490317320, 1004, 'e10', 'cond_is_done', 'timer_wait', 'default', NULL, 9, '2026-05-18 09:33:54', '2026-05-18 09:33:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056195005284986881, 2056195003653402625, 'e1', 'start_1', 'db_query', 'default', NULL, 0, '2026-05-18 10:07:46', '2026-05-18 10:07:46', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056195005284986882, 2056195003653402625, 'e2', 'db_query', 'script_assemble', 'default', NULL, 1, '2026-05-18 10:07:46', '2026-05-18 10:07:46', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056195005284986883, 2056195003653402625, 'e3', 'script_assemble', 'api_call_b', 'default', NULL, 2, '2026-05-18 10:07:46', '2026-05-18 10:07:46', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056195005284986884, 2056195003653402625, 'e4', 'api_call_b', 'script_assemble_c', 'default', NULL, 3, '2026-05-18 10:07:46', '2026-05-18 10:07:46', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056195005284986885, 2056195003653402625, 'e5', 'script_assemble_c', 'timer_wait', 'default', NULL, 4, '2026-05-18 10:07:46', '2026-05-18 10:07:46', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056195005284986886, 2056195003653402625, 'e6', 'timer_wait', 'api_poll_c', 'default', NULL, 5, '2026-05-18 10:07:46', '2026-05-18 10:07:46', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056195005284986887, 2056195003653402625, 'e7', 'api_poll_c', 'script_parse_c', 'default', NULL, 6, '2026-05-18 10:07:46', '2026-05-18 10:07:46', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056195005284986888, 2056195003653402625, 'e8', 'script_parse_c', 'cond_is_done', 'default', NULL, 7, '2026-05-18 10:07:46', '2026-05-18 10:07:46', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056195005284986889, 2056195003653402625, 'e9', 'cond_is_done', 'end_1', 'custom', '#{context.wsbz == \'Y\'}', 8, '2026-05-18 10:07:46', '2026-05-18 10:07:46', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056195005352095746, 2056195003653402625, 'e10', 'cond_is_done', 'timer_wait', 'default', NULL, 9, '2026-05-18 10:07:46', '2026-05-18 10:07:46', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056226508526141441, 2056225968668884994, 'e1', 'start_1', 'db_query', 'default', NULL, 0, '2026-05-18 12:12:57', '2026-05-18 12:12:57', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056226508526141442, 2056225968668884994, 'e2', 'db_query', 'script_assemble', 'default', NULL, 1, '2026-05-18 12:12:57', '2026-05-18 12:12:57', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056226508526141443, 2056225968668884994, 'e3', 'script_assemble', 'api_call_b', 'default', NULL, 2, '2026-05-18 12:12:57', '2026-05-18 12:12:57', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056226508526141444, 2056225968668884994, 'e4', 'api_call_b', 'script_assemble_c', 'default', NULL, 3, '2026-05-18 12:12:57', '2026-05-18 12:12:57', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056226508526141445, 2056225968668884994, 'e5', 'script_assemble_c', 'timer_wait', 'default', NULL, 4, '2026-05-18 12:12:57', '2026-05-18 12:12:57', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056226508526141446, 2056225968668884994, 'e6', 'timer_wait', 'api_poll_c', 'default', NULL, 5, '2026-05-18 12:12:57', '2026-05-18 12:12:57', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056226508526141447, 2056225968668884994, 'e7', 'api_poll_c', 'script_parse_c', 'default', NULL, 6, '2026-05-18 12:12:57', '2026-05-18 12:12:57', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056226508526141448, 2056225968668884994, 'e8', 'script_parse_c', 'cond_is_done', 'default', NULL, 7, '2026-05-18 12:12:57', '2026-05-18 12:12:57', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056226508526141449, 2056225968668884994, 'e9', 'cond_is_done', 'end_1', 'custom', '#{context.wsbz == \'Y\'}', 8, '2026-05-18 12:12:57', '2026-05-18 12:12:57', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056226508526141450, 2056225968668884994, 'e10', 'cond_is_done', 'timer_wait', 'default', NULL, 9, '2026-05-18 12:12:57', '2026-05-18 12:12:57', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056255943841234946, 2056255943241449474, 'e1', 'start_1', 'db_query', 'default', NULL, 0, '2026-05-18 14:09:55', '2026-05-18 14:09:55', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056255943841234947, 2056255943241449474, 'e2', 'db_query', 'script_assemble', 'default', NULL, 1, '2026-05-18 14:09:55', '2026-05-18 14:09:55', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056255943841234948, 2056255943241449474, 'e3', 'script_assemble', 'api_call_b', 'default', NULL, 2, '2026-05-18 14:09:55', '2026-05-18 14:09:55', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056255943841234949, 2056255943241449474, 'e4', 'api_call_b', 'script_assemble_c', 'default', NULL, 3, '2026-05-18 14:09:55', '2026-05-18 14:09:55', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056255943841234950, 2056255943241449474, 'e5', 'script_assemble_c', 'timer_wait', 'default', NULL, 4, '2026-05-18 14:09:55', '2026-05-18 14:09:55', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056255943841234951, 2056255943241449474, 'e6', 'timer_wait', 'api_poll_c', 'default', NULL, 5, '2026-05-18 14:09:55', '2026-05-18 14:09:55', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056255943841234952, 2056255943241449474, 'e7', 'api_poll_c', 'script_parse_c', 'default', NULL, 6, '2026-05-18 14:09:55', '2026-05-18 14:09:55', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056255943841234953, 2056255943241449474, 'e8', 'script_parse_c', 'cond_is_done', 'default', NULL, 7, '2026-05-18 14:09:55', '2026-05-18 14:09:55', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056255943841234954, 2056255943241449474, 'e9', 'cond_is_done', 'end_1', 'custom', '#{context.wsbz == \'Y\'}', 8, '2026-05-18 14:09:55', '2026-05-18 14:09:55', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056255943841234955, 2056255943241449474, 'e10', 'cond_is_done', 'timer_wait', 'default', NULL, 9, '2026-05-18 14:09:55', '2026-05-18 14:09:55', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056257199678767105, 2056256708752261121, 'e1', 'start_1', 'db_query', 'default', NULL, 0, '2026-05-18 14:14:54', '2026-05-18 14:14:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056257199678767106, 2056256708752261121, 'e2', 'db_query', 'script_assemble', 'default', NULL, 1, '2026-05-18 14:14:54', '2026-05-18 14:14:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056257199678767107, 2056256708752261121, 'e3', 'script_assemble', 'api_call_b', 'default', NULL, 2, '2026-05-18 14:14:54', '2026-05-18 14:14:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056257199678767108, 2056256708752261121, 'e4', 'api_call_b', 'script_assemble_c', 'default', NULL, 3, '2026-05-18 14:14:54', '2026-05-18 14:14:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056257199678767109, 2056256708752261121, 'e5', 'script_assemble_c', 'timer_wait', 'default', NULL, 4, '2026-05-18 14:14:54', '2026-05-18 14:14:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056257199678767110, 2056256708752261121, 'e6', 'timer_wait', 'api_poll_c', 'default', NULL, 5, '2026-05-18 14:14:54', '2026-05-18 14:14:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056257199678767111, 2056256708752261121, 'e7', 'api_poll_c', 'script_parse_c', 'default', NULL, 6, '2026-05-18 14:14:54', '2026-05-18 14:14:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056257199678767112, 2056256708752261121, 'e8', 'script_parse_c', 'cond_is_done', 'default', NULL, 7, '2026-05-18 14:14:54', '2026-05-18 14:14:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056257199678767113, 2056256708752261121, 'e9', 'cond_is_done', 'end_1', 'custom', '#{context.wsbz == \'Y\'}', 8, '2026-05-18 14:14:54', '2026-05-18 14:14:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056257199678767114, 2056256708752261121, 'e10', 'cond_is_done', 'timer_wait', 'default', NULL, 9, '2026-05-18 14:14:54', '2026-05-18 14:14:54', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056313278185934850, 2056313274914377730, 'e1', 'start_1', 'db_query', 'default', NULL, 0, '2026-05-18 17:57:45', '2026-05-18 17:57:45', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056313278185934851, 2056313274914377730, 'e2', 'db_query', 'script_assemble', 'default', NULL, 1, '2026-05-18 17:57:45', '2026-05-18 17:57:45', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056313278185934852, 2056313274914377730, 'e3', 'script_assemble', 'api_call_b', 'default', NULL, 2, '2026-05-18 17:57:45', '2026-05-18 17:57:45', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056313278185934853, 2056313274914377730, '60d11cef-8b75-42cf-a609-f6a8e5df8b33', 'api_call_b', 'end_1', 'default', NULL, 3, '2026-05-18 17:57:45', '2026-05-18 17:57:45', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056316109211078658, 2056307752567889922, 'e1', 'start_1', 'db_query', 'default', NULL, 0, '2026-05-18 18:09:00', '2026-05-18 18:09:00', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056316109211078659, 2056307752567889922, 'e2', 'db_query', 'script_assemble', 'default', NULL, 1, '2026-05-18 18:09:00', '2026-05-18 18:09:00', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056316109211078660, 2056307752567889922, 'e3', 'script_assemble', 'api_call_b', 'default', NULL, 2, '2026-05-18 18:09:00', '2026-05-18 18:09:00', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056316109211078661, 2056307752567889922, 'e4', 'api_call_b', 'script_assemble_c', 'default', NULL, 3, '2026-05-18 18:09:00', '2026-05-18 18:09:00', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056316109211078662, 2056307752567889922, 'e5', 'script_assemble_c', 'timer_wait', 'default', NULL, 4, '2026-05-18 18:09:00', '2026-05-18 18:09:00', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056316109211078663, 2056307752567889922, 'e6', 'timer_wait', 'api_poll_c', 'default', NULL, 5, '2026-05-18 18:09:00', '2026-05-18 18:09:00', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056316109211078664, 2056307752567889922, 'e7', 'api_poll_c', 'script_parse_c', 'default', NULL, 6, '2026-05-18 18:09:00', '2026-05-18 18:09:00', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056316109211078665, 2056307752567889922, 'e8', 'script_parse_c', 'cond_is_done', 'default', NULL, 7, '2026-05-18 18:09:00', '2026-05-18 18:09:00', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056316109211078666, 2056307752567889922, 'e9', 'cond_is_done', 'end_1', 'custom', '#{context.wsbz == \'Y\'}', 8, '2026-05-18 18:09:00', '2026-05-18 18:09:00', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2056316109211078667, 2056307752567889922, 'e10', 'cond_is_done', 'timer_wait', 'default', NULL, 9, '2026-05-18 18:09:00', '2026-05-18 18:09:00', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2057107692781105153, 2001, 'e1', 'start_1', 'minio_upload', 'default', NULL, 0, '2026-05-20 22:34:28', '2026-05-20 22:34:28', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2057107692781105154, 2001, 'e2', 'minio_upload', 'end_1', 'default', NULL, 1, '2026-05-20 22:34:28', '2026-05-20 22:34:28', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2057112318041456641, 2057112300765118465, 'e1', 'start_1', 'minio_upload', 'default', NULL, 0, '2026-05-20 22:52:51', '2026-05-20 22:52:51', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2057112318041456642, 2057112300765118465, 'e2', 'minio_upload', 'end_1', 'default', NULL, 1, '2026-05-20 22:52:51', '2026-05-20 22:52:51', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2057113355364139009, 2057113253346082818, 'e1', 'start_1', 'minio_upload', 'default', NULL, 0, '2026-05-20 22:56:58', '2026-05-20 22:56:58', NULL, NULL, 0);
INSERT INTO `wf_flow_edge` VALUES (2057113355364139010, 2057113253346082818, 'e2', 'minio_upload', 'end_1', 'default', NULL, 1, '2026-05-20 22:56:58', '2026-05-20 22:56:58', NULL, NULL, 0);

-- ----------------------------
-- Table structure for wf_flow_instance
-- ----------------------------
DROP TABLE IF EXISTS `wf_flow_instance`;
CREATE TABLE `wf_flow_instance`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `flow_id` bigint(20) NOT NULL COMMENT '流程定义ID',
  `flow_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程编码',
  `version` int(11) NULL DEFAULT 1 COMMENT '流程版本号',
  `business_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务主键',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'running' COMMENT '状态：running/completed/suspended/failed/terminated',
  `current_node_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前节点ID',
  `context_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '流程上下文JSON',
  `start_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_flow`(`flow_id`) USING BTREE,
  INDEX `idx_business`(`business_key`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_flow_ver`(`flow_code`, `version`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程实例' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_flow_instance
-- ----------------------------

INSERT INTO `wf_flow_instance` VALUES (2057108722059132929, 2001, 'minio-upload-demo', 1, 'test-001', 'terminated', 'minio_upload', '{\"fileName\":\"random-image.jpg\",\"_businessKey\":\"test-001\",\"_instanceId\":2057108722059132929,\"filePath\":\"https://picsum.photos/800/600.jpg\",\"_flowCode\":\"minio-upload-demo\"}', '2026-05-20 22:38:33', '2026-05-20 22:44:50', '2026-05-20 22:38:33', '2026-05-20 22:38:55', NULL, NULL, 0);
INSERT INTO `wf_flow_instance` VALUES (2057111300486529025, 2001, 'minio-upload-demo', 1, 'test-001', 'suspended', 'minio_upload', '{\"fileName\":\"random-image.jpg\",\"_businessKey\":\"test-001\",\"filePath\":\"https://picsum.photos/800/600.jpg\",\"_instanceId\":2057111300486529025,\"_flowCode\":\"minio-upload-demo\"}', '2026-05-20 22:48:48', NULL, '2026-05-20 22:48:48', '2026-05-20 22:49:04', NULL, NULL, 0);
INSERT INTO `wf_flow_instance` VALUES (2057111956442120193, 2001, 'minio-upload-demo', 1, 'test-http-001', 'suspended', 'minio_upload', '{\"fileName\":\"placeholder.jpg\",\"_businessKey\":\"test-http-001\",\"filePath\":\"http://via.placeholder.com/800x600.jpg\",\"_instanceId\":2057111956442120193,\"_flowCode\":\"minio-upload-demo\"}', '2026-05-20 22:51:24', NULL, '2026-05-20 22:51:24', '2026-05-20 22:51:45', NULL, NULL, 0);
INSERT INTO `wf_flow_instance` VALUES (2057113029261197313, 2057112300765118465, 'minio-upload-demo', 2, 'test-http-001', 'suspended', 'minio_upload', '{\"fileName\":\"placeholder.jpg\",\"_businessKey\":\"test-http-001\",\"filePath\":\"http://via.placeholder.com/800x600.jpg\",\"_instanceId\":2057113029261197313,\"_flowCode\":\"minio-upload-demo\"}', '2026-05-20 22:55:40', NULL, '2026-05-20 22:55:40', '2026-05-20 22:56:05', NULL, NULL, 0);
INSERT INTO `wf_flow_instance` VALUES (2057113593554468865, 2057113253346082818, 'minio-upload-demo', 3, 'test-http-001', 'completed', 'end_1', '{\"fileName\":\"placeholder.jpg\",\"_businessKey\":\"test-http-001\",\"_currentTime\":1779289090132,\"filePath\":\"http://via.placeholder.com/800x600.jpg\",\"_instanceId\":2057113593554468865,\"nodeResult_minio_upload\":{\"bucket\":\"riverflow\",\"sourceType\":\"url\",\"success\":true,\"objectName\":\"placeholder.jpg\",\"operation\":\"upload\",\"url\":\"http://43.167.213.62:9000/riverflow/placeholder.jpg\"},\"_flowCode\":\"minio-upload-demo\"}', '2026-05-20 22:57:55', '2026-05-20 22:58:13', '2026-05-20 22:57:55', '2026-05-20 22:58:13', NULL, NULL, 0);
INSERT INTO `wf_flow_instance` VALUES (2057120924065398785, 2057113253346082818, 'minio-upload-demo', 3, 'test-http-001', 'completed', 'end_1', '{\"fileName\":\"placeholder.jpg\",\"_businessKey\":\"test-http-001\",\"_currentTime\":1779290840190,\"filePath\":\"http://via.placeholder.com/800x600.jpg\",\"_instanceId\":2057120924065398785,\"nodeResult_minio_upload\":{\"bucket\":\"riverflow\",\"sourceType\":\"url\",\"success\":true,\"objectName\":\"placeholder.jpg\",\"operation\":\"upload\",\"url\":\"http://43.167.213.62:9000/riverflow/placeholder.jpg\"},\"_flowCode\":\"minio-upload-demo\"}', '2026-05-20 23:27:02', '2026-05-20 23:27:21', '2026-05-20 23:27:02', '2026-05-20 23:27:21', NULL, NULL, 0);

-- ----------------------------
-- Table structure for wf_flow_log
-- ----------------------------
DROP TABLE IF EXISTS `wf_flow_log`;
CREATE TABLE `wf_flow_log`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `instance_id` bigint(20) NOT NULL COMMENT '流程实例ID',
  `task_id` bigint(20) NULL DEFAULT NULL COMMENT '任务ID',
  `node_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '节点ID',
  `log_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '日志类型：start/execute/condition/transition/error',
  `log_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '日志内容',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_instance`(`instance_id`) USING BTREE,
  INDEX `idx_type`(`log_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程执行日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_flow_log
-- ----------------------------
INSERT INTO `wf_flow_log` VALUES (2055463207877804034, 2055463206460129281, NULL, NULL, 'start', '流程实例启动成功', '2026-05-16 09:39:52', '2026-05-16 09:39:52', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2055463236281630722, 2055463206460129281, 2055463208586641410, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":128,\"value\":\"开始\"}', '2026-05-16 09:39:59', '2026-05-16 09:39:59', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2055463240593375234, 2055463206460129281, 2055463240262025217, 'db_query', 'transition', '从 [{\"x\":304,\"y\":128,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}]', '2026-05-16 09:40:00', '2026-05-16 09:40:00', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2055463263343280129, 2055463206460129281, 2055463240262025217, 'db_query', 'execute', '节点执行成功: {\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}', '2026-05-16 09:40:05', '2026-05-16 09:40:05', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2055463263544606722, 2055463206460129281, 2055463263544606721, 'script_assemble', 'transition', '从 [{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}] 流转到 [{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}]', '2026-05-16 09:40:05', '2026-05-16 09:40:05', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2055463314375376898, 2055463206460129281, 2055463263544606721, 'script_assemble', 'execute', '节点执行成功: {\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}', '2026-05-16 09:40:17', '2026-05-16 09:40:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2055463314501206019, 2055463206460129281, 2055463314501206018, 'api_call_b', 'transition', '从 [{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}] 流转到 [{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}]', '2026-05-16 09:40:17', '2026-05-16 09:40:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2055463390757847042, 2055463206460129281, 2055463314501206018, 'api_call_b', 'execute', '节点执行成功: {\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}', '2026-05-16 09:40:36', '2026-05-16 09:40:36', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2055463391022088194, 2055463206460129281, NULL, 'end', 'transition', '流程执行完成', '2026-05-16 09:40:36', '2026-05-16 09:40:36', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2055463391022088195, 2055463206460129281, NULL, 'end_1', 'transition', '从 [{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}] 流转到 [{\"x\":352,\"y\":624,\"value\":\"结束\"}]（流程结束）', '2026-05-16 09:40:36', '2026-05-16 09:40:36', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056204718277947394, 2056204716461813762, NULL, NULL, 'start', '流程实例启动成功, version=2', '2026-05-18 10:46:22', '2026-05-18 10:46:22', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056204992564457474, 2056204716461813762, 2056204719137779714, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":128,\"value\":\"开始\"}', '2026-05-18 10:47:27', '2026-05-18 10:47:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056204992895807491, 2056204716461813762, 2056204992895807490, 'db_query', 'transition', '从 [{\"x\":304,\"y\":128,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}]', '2026-05-18 10:47:27', '2026-05-18 10:47:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056205032846553090, 2056204716461813762, 2056204992895807490, 'db_query', 'execute', '节点执行成功: {\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}', '2026-05-18 10:47:37', '2026-05-18 10:47:37', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056205033102405634, 2056204716461813762, 2056205033102405633, 'script_assemble', 'transition', '从 [{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}] 流转到 [{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}]', '2026-05-18 10:47:37', '2026-05-18 10:47:37', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056205089071198209, 2056204716461813762, 2056205033102405633, 'script_assemble', 'execute', '节点执行成功: {\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}', '2026-05-18 10:47:50', '2026-05-18 10:47:50', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056205089389965314, 2056204716461813762, 2056205089389965313, 'api_call_b', 'transition', '从 [{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}] 流转到 [{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}]', '2026-05-18 10:47:50', '2026-05-18 10:47:50', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056205162140168194, 2056204716461813762, 2056205089389965313, 'api_call_b', 'execute', '节点执行成功: {\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}', '2026-05-18 10:48:08', '2026-05-18 10:48:08', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056205162479906818, 2056204716461813762, 2056205162400215042, 'script_assemble_c', 'transition', '从 [{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}] 流转到 [{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}]', '2026-05-18 10:48:08', '2026-05-18 10:48:08', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056205201063309314, 2056204716461813762, 2056205162400215042, 'script_assemble_c', 'execute', '节点执行成功: {\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}', '2026-05-18 10:48:17', '2026-05-18 10:48:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056205201193332739, 2056204716461813762, 2056205201193332738, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 10:48:17', '2026-05-18 10:48:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056212862827765761, 2056204716461813762, 2056205201193332738, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 11:18:44', '2026-05-18 11:18:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056212862987149314, 2056204716461813762, 2056212862987149313, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":128,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 11:18:44', '2026-05-18 11:18:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056212892246614017, 2056204716461813762, 2056212862987149313, 'api_poll_c', 'error', '节点执行失败，流程挂起: 接口调用失败, 业务码: 503, 错误: 系统繁忙，请稍后重试', '2026-05-18 11:18:51', '2026-05-18 11:18:51', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265085209362433, 2056265083410006018, NULL, NULL, 'start', '流程实例启动成功, version=5', '2026-05-18 14:46:15', '2026-05-18 14:46:15', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265282454896642, 2056265083410006018, 2056265087893716993, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":128,\"value\":\"开始\"}', '2026-05-18 14:47:02', '2026-05-18 14:47:02', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265282517811203, 2056265083410006018, 2056265282517811202, 'db_query', 'transition', '从 [{\"x\":304,\"y\":128,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}]', '2026-05-18 14:47:02', '2026-05-18 14:47:02', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265324255330305, 2056265083410006018, 2056265282517811202, 'db_query', 'execute', '节点执行成功: {\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}', '2026-05-18 14:47:12', '2026-05-18 14:47:12', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265324519571457, 2056265083410006018, 2056265324452462593, 'script_assemble', 'transition', '从 [{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}] 流转到 [{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}]', '2026-05-18 14:47:12', '2026-05-18 14:47:12', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265375757189121, 2056265083410006018, 2056265324452462593, 'script_assemble', 'execute', '节点执行成功: {\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}', '2026-05-18 14:47:24', '2026-05-18 14:47:24', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265375954321410, 2056265083410006018, 2056265375954321409, 'api_call_b', 'transition', '从 [{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}] 流转到 [{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}]', '2026-05-18 14:47:24', '2026-05-18 14:47:24', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265445688819713, 2056265083410006018, 2056265375954321409, 'api_call_b', 'execute', '节点执行成功: {\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}', '2026-05-18 14:47:40', '2026-05-18 14:47:40', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265445885952003, 2056265083410006018, 2056265445885952002, 'script_assemble_c', 'transition', '从 [{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}] 流转到 [{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}]', '2026-05-18 14:47:41', '2026-05-18 14:47:41', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265449870540801, 2056265083410006018, 2056265445885952002, 'script_assemble_c', 'execute', '节点执行成功: {\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}', '2026-05-18 14:47:41', '2026-05-18 14:47:41', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265450134781954, 2056265083410006018, 2056265450134781953, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 14:47:42', '2026-05-18 14:47:42', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265742356135938, 2056265083410006018, 2056265450134781953, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 14:48:51', '2026-05-18 14:48:51', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056265742423244802, 2056265083410006018, 2056265742423244801, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 14:48:51', '2026-05-18 14:48:51', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056266394276806657, 2056265083410006018, 2056265742423244801, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 14:51:27', '2026-05-18 14:51:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056266394796900353, 2056265083410006018, 2056266394343915521, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 14:51:27', '2026-05-18 14:51:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056266417488084994, 2056265083410006018, 2056266394343915521, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 14:51:32', '2026-05-18 14:51:32', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056266417613914114, 2056265083410006018, 2056266417613914113, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 14:51:32', '2026-05-18 14:51:32', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056266458193805313, 2056265083410006018, 2056266417613914113, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 14:51:42', '2026-05-18 14:51:42', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056266458453852163, 2056265083410006018, 2056266458453852162, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 14:51:42', '2026-05-18 14:51:42', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056266748921987073, 2056265083410006018, 2056266458453852162, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 14:52:51', '2026-05-18 14:52:51', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056266749433692162, 2056265083410006018, 2056266749433692161, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 14:52:51', '2026-05-18 14:52:51', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056267825650155521, 2056265083410006018, 2056266749433692161, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 14:57:08', '2026-05-18 14:57:08', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056267825918590978, 2056265083410006018, 2056267825918590977, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 14:57:08', '2026-05-18 14:57:08', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056267845162057730, 2056265083410006018, 2056267825918590977, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 14:57:13', '2026-05-18 14:57:13', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056267845162057732, 2056265083410006018, 2056267845162057731, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 14:57:13', '2026-05-18 14:57:13', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056268340790378498, 2056265083410006018, 2056267845162057731, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 14:59:11', '2026-05-18 14:59:11', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056268340920401922, 2056265083410006018, 2056268340920401921, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 14:59:11', '2026-05-18 14:59:11', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056268363997462529, 2056265083410006018, 2056267845162057731, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 14:59:16', '2026-05-18 14:59:16', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056268364584665090, 2056265083410006018, 2056268364584665089, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 14:59:16', '2026-05-18 14:59:16', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056268636493004801, 2056265083410006018, 2056268364584665089, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 15:00:21', '2026-05-18 15:00:21', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056268636685942786, 2056265083410006018, 2056268636685942785, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 15:00:21', '2026-05-18 15:00:21', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272209339535361, 2056265083410006018, 2056268636685942785, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 15:14:33', '2026-05-18 15:14:33', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272209922543619, 2056265083410006018, 2056272209922543618, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 15:14:33', '2026-05-18 15:14:33', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272256366071809, 2056265083410006018, 2056272209922543618, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 15:14:44', '2026-05-18 15:14:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272256550621187, 2056265083410006018, 2056272256550621186, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 15:14:44', '2026-05-18 15:14:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272299823255553, 2056265083410006018, 2056272256550621186, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:14:55', '2026-05-18 15:14:55', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272299881975811, 2056265083410006018, 2056272299881975810, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:14:55', '2026-05-18 15:14:55', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272622491062273, 2056265083410006018, 2056272299881975810, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 15:16:12', '2026-05-18 15:16:12', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272622616891394, 2056265083410006018, 2056272622616891393, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 15:16:12', '2026-05-18 15:16:12', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272687938981889, 2056265083410006018, 2056272622616891393, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 15:16:27', '2026-05-18 15:16:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272688073199618, 2056265083410006018, 2056272688073199617, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 15:16:27', '2026-05-18 15:16:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272707090173953, 2056265083410006018, 2056272688073199617, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 15:16:32', '2026-05-18 15:16:32', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272707153088515, 2056265083410006018, 2056272707153088514, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 15:16:32', '2026-05-18 15:16:32', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272748458594305, 2056265083410006018, 2056272707153088514, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:16:42', '2026-05-18 15:16:42', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056272748458594307, 2056265083410006018, 2056272748458594306, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:16:42', '2026-05-18 15:16:42', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273084225212417, 2056265083410006018, 2056272748458594306, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 15:18:02', '2026-05-18 15:18:02', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273084225212419, 2056265083410006018, 2056273084225212418, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 15:18:02', '2026-05-18 15:18:02', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273143557836801, 2056265083410006018, 2056273084225212418, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 15:18:16', '2026-05-18 15:18:16', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273143616557058, 2056265083410006018, 2056273143599779842, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 15:18:16', '2026-05-18 15:18:16', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273167826079746, 2056265083410006018, 2056273143599779842, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 15:18:22', '2026-05-18 15:18:22', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273167888994307, 2056265083410006018, 2056273167888994306, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 15:18:22', '2026-05-18 15:18:22', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273209945280514, 2056265083410006018, 2056273167888994306, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:18:32', '2026-05-18 15:18:32', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273210012389378, 2056265083410006018, 2056273210012389377, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:18:32', '2026-05-18 15:18:32', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273545346994177, 2056265083410006018, 2056273210012389377, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 15:19:52', '2026-05-18 15:19:52', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273545405714434, 2056265083410006018, 2056273545405714433, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 15:19:52', '2026-05-18 15:19:52', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273608282525697, 2056265083410006018, 2056273545405714433, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 15:20:07', '2026-05-18 15:20:07', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273608370606082, 2056265083410006018, 2056273608370606081, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 15:20:07', '2026-05-18 15:20:07', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273629073690625, 2056265083410006018, 2056273608370606081, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 15:20:12', '2026-05-18 15:20:12', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273629136605186, 2056265083410006018, 2056273629136605185, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 15:20:12', '2026-05-18 15:20:12', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273671226445826, 2056265083410006018, 2056273629136605185, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:20:22', '2026-05-18 15:20:22', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056273671385829379, 2056265083410006018, 2056273671385829378, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:20:22', '2026-05-18 15:20:22', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056274006527496193, 2056265083410006018, 2056273671385829378, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 15:21:42', '2026-05-18 15:21:42', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056274006590410755, 2056265083410006018, 2056274006590410754, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 15:21:42', '2026-05-18 15:21:42', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056274048621531138, 2056265083410006018, 2056274006590410754, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 15:21:52', '2026-05-18 15:21:52', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056274048684445697, 2056265083410006018, 2056274048621531139, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 15:21:52', '2026-05-18 15:21:52', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056274090803646465, 2056265083410006018, 2056274048621531139, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 15:22:02', '2026-05-18 15:22:02', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056274091130802178, 2056265083410006018, 2056274091130802177, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 15:22:02', '2026-05-18 15:22:02', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056276427261661186, 2056265083410006018, 2056274091130802177, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:31:19', '2026-05-18 15:31:19', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056276427332964354, 2056265083410006018, 2056276427328770049, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:31:19', '2026-05-18 15:31:19', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056276456793755649, 2056265083410006018, 2056274091130802177, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:31:26', '2026-05-18 15:31:26', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056276456911196163, 2056265083410006018, 2056276456911196162, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:31:26', '2026-05-18 15:31:26', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056276485285662721, 2056265083410006018, 2056274091130802177, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:31:33', '2026-05-18 15:31:33', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056276485625401347, 2056265083410006018, 2056276485625401346, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:31:33', '2026-05-18 15:31:33', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056276729608122369, 2056265083410006018, 2056276485625401346, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 15:32:31', '2026-05-18 15:32:31', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056276732049207298, 2056265083410006018, 2056276731977904130, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 15:32:31', '2026-05-18 15:32:31', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056276991303331842, 2056265083410006018, 2056276731977904130, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 15:33:33', '2026-05-18 15:33:33', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056276992486125569, 2056265083410006018, 2056276992419016705, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 15:33:33', '2026-05-18 15:33:33', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277031417655297, 2056265083410006018, 2056276992419016705, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 15:33:43', '2026-05-18 15:33:43', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277031556067330, 2056265083410006018, 2056277031484764162, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 15:33:43', '2026-05-18 15:33:43', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277219498635265, 2056265083410006018, 2056277031484764162, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:34:28', '2026-05-18 15:34:28', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277219599298562, 2056265083410006018, 2056277219599298561, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:34:28', '2026-05-18 15:34:28', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277482401804289, 2056265083410006018, 2056277219599298561, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 15:35:30', '2026-05-18 15:35:30', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277482468913154, 2056265083410006018, 2056277482468913153, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 15:35:30', '2026-05-18 15:35:30', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277524487450625, 2056265083410006018, 2056277482468913153, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 15:35:40', '2026-05-18 15:35:40', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277525347282947, 2056265083410006018, 2056277525347282946, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 15:35:40', '2026-05-18 15:35:40', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277566342410242, 2056265083410006018, 2056277525347282946, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 15:35:50', '2026-05-18 15:35:50', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277566409519107, 2056265083410006018, 2056277566409519106, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 15:35:50', '2026-05-18 15:35:50', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277759032930306, 2056265083410006018, 2056277566409519106, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:36:36', '2026-05-18 15:36:36', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056277759272005634, 2056265083410006018, 2056277759272005633, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:36:36', '2026-05-18 15:36:36', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056278027741016066, 2056265083410006018, 2056277759272005633, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 15:37:40', '2026-05-18 15:37:40', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056278028005257218, 2056265083410006018, 2056278027933954049, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 15:37:40', '2026-05-18 15:37:40', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056278069667278850, 2056265083410006018, 2056278027933954049, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 15:37:50', '2026-05-18 15:37:50', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056278069738582019, 2056265083410006018, 2056278069738582018, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 15:37:50', '2026-05-18 15:37:50', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056278111920697346, 2056265083410006018, 2056278069738582018, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 15:38:00', '2026-05-18 15:38:00', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056278111975223299, 2056265083410006018, 2056278111975223298, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 15:38:00', '2026-05-18 15:38:00', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056278268624089090, 2056265083410006018, 2056278111975223298, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:38:38', '2026-05-18 15:38:38', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056278268691197955, 2056265083410006018, 2056278268691197954, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:38:38', '2026-05-18 15:38:38', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056279569600626690, 2056265083410006018, 2056278268691197954, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 15:43:48', '2026-05-18 15:43:48', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056279569730650114, 2056265083410006018, 2056279569730650113, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 15:43:48', '2026-05-18 15:43:48', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056279601389256706, 2056265083410006018, 2056279569730650113, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 15:43:55', '2026-05-18 15:43:55', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056279601523474435, 2056265083410006018, 2056279601523474434, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 15:43:56', '2026-05-18 15:43:56', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056279645790158850, 2056265083410006018, 2056279601523474434, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 15:44:06', '2026-05-18 15:44:06', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056279645924376579, 2056265083410006018, 2056279645924376578, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 15:44:06', '2026-05-18 15:44:06', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056281511324303361, 2056265083410006018, 2056279645924376578, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:51:31', '2026-05-18 15:51:31', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056281511848591363, 2056265083410006018, 2056281511848591362, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:51:31', '2026-05-18 15:51:31', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056281513400483842, 2056265083410006018, 2056279645924376578, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 15:51:31', '2026-05-18 15:51:31', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056281514142875649, 2056265083410006018, 2056281514134487041, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 15:51:31', '2026-05-18 15:51:31', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285041183461377, 2056265083410006018, 2056281511848591362, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:05:32', '2026-05-18 16:05:32', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285041258958850, 2056265083410006018, 2056285041258958849, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:05:32', '2026-05-18 16:05:32', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285085043298306, 2056265083410006018, 2056285041258958849, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:05:43', '2026-05-18 16:05:43', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285085173321731, 2056265083410006018, 2056285085173321730, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:05:43', '2026-05-18 16:05:43', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285140613632001, 2056265083410006018, 2056285085173321730, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:05:56', '2026-05-18 16:05:56', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285140676546563, 2056265083410006018, 2056285140676546562, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:05:56', '2026-05-18 16:05:56', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285227456696322, 2056265083410006018, 2056285140676546562, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:06:17', '2026-05-18 16:06:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285227515416578, 2056265083410006018, 2056285227515416577, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:06:17', '2026-05-18 16:06:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285610446979073, 2056265083410006018, 2056285227515416577, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:07:48', '2026-05-18 16:07:48', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285610581196802, 2056265083410006018, 2056285610581196801, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:07:48', '2026-05-18 16:07:48', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285636900454402, 2056265083410006018, 2056285610581196801, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:07:54', '2026-05-18 16:07:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285639454785538, 2056265083410006018, 2056285639454785537, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:07:55', '2026-05-18 16:07:55', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285685940256769, 2056265083410006018, 2056285639454785537, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:08:06', '2026-05-18 16:08:06', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285686653288451, 2056265083410006018, 2056285686653288450, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:08:06', '2026-05-18 16:08:06', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285732018880513, 2056265083410006018, 2056285686653288450, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:08:17', '2026-05-18 16:08:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056285732618665987, 2056265083410006018, 2056285732618665986, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:08:17', '2026-05-18 16:08:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056286053390647297, 2056265083410006018, 2056285732618665986, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:09:34', '2026-05-18 16:09:34', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056286053659082754, 2056265083410006018, 2056286053659082753, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:09:34', '2026-05-18 16:09:34', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056286096516481026, 2056265083410006018, 2056286053659082753, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:09:44', '2026-05-18 16:09:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056286096642310147, 2056265083410006018, 2056286096642310146, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:09:44', '2026-05-18 16:09:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056286137754877953, 2056265083410006018, 2056286096642310146, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:09:54', '2026-05-18 16:09:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056286137805209602, 2056265083410006018, 2056286137805209601, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:09:54', '2026-05-18 16:09:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056286200690409474, 2056265083410006018, 2056286137805209601, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:10:09', '2026-05-18 16:10:09', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056286201084674050, 2056265083410006018, 2056286201084674049, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:10:09', '2026-05-18 16:10:09', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056288999532687361, 2056265083410006018, 2056286201084674049, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:21:16', '2026-05-18 16:21:16', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056288999767568386, 2056265083410006018, 2056288999767568385, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:21:16', '2026-05-18 16:21:16', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289030532788225, 2056265083410006018, 2056288999767568385, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:21:24', '2026-05-18 16:21:24', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289030654423043, 2056265083410006018, 2056289030654423042, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:21:24', '2026-05-18 16:21:24', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289076456222721, 2056265083410006018, 2056289030654423042, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:21:35', '2026-05-18 16:21:35', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289079878774785, 2056265083410006018, 2056289079807471617, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:21:35', '2026-05-18 16:21:35', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289194752372738, 2056265083410006018, 2056289079807471617, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:22:03', '2026-05-18 16:22:03', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289195356352514, 2056265083410006018, 2056289195356352513, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:22:03', '2026-05-18 16:22:03', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289523262844930, 2056265083410006018, 2056289195356352513, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:23:21', '2026-05-18 16:23:21', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289523397062658, 2056265083410006018, 2056289523397062657, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:23:21', '2026-05-18 16:23:21', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289565084250114, 2056265083410006018, 2056289523397062657, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:23:31', '2026-05-18 16:23:31', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289565591760899, 2056265083410006018, 2056289565591760898, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:23:31', '2026-05-18 16:23:31', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289607996174338, 2056265083410006018, 2056289565591760898, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:23:41', '2026-05-18 16:23:41', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289608122003459, 2056265083410006018, 2056289608122003458, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:23:41', '2026-05-18 16:23:41', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289676585627649, 2056265083410006018, 2056289608122003458, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:23:58', '2026-05-18 16:23:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056289678779248642, 2056265083410006018, 2056289678779248641, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:23:58', '2026-05-18 16:23:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056291598138982402, 2056265083410006018, 2056289678779248641, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:31:36', '2026-05-18 16:31:36', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056291598340308994, 2056265083410006018, 2056291598340308993, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:31:36', '2026-05-18 16:31:36', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056291627763351554, 2056265083410006018, 2056291598340308993, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:31:43', '2026-05-18 16:31:43', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056291629080363011, 2056265083410006018, 2056291629080363010, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:31:43', '2026-05-18 16:31:43', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056291674802470914, 2056265083410006018, 2056291629080363010, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:31:54', '2026-05-18 16:31:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056291675716829187, 2056265083410006018, 2056291675716829186, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:31:54', '2026-05-18 16:31:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056292249560530946, 2056265083410006018, 2056291675716829186, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:34:11', '2026-05-18 16:34:11', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056292249824772097, 2056265083410006018, 2056292249816383490, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:34:11', '2026-05-18 16:34:11', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056292690876813313, 2056265083410006018, 2056292249816383490, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:35:56', '2026-05-18 16:35:56', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056292691086528515, 2056265083410006018, 2056292691086528514, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:35:56', '2026-05-18 16:35:56', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056292745230798850, 2056265083410006018, 2056292691086528514, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:36:09', '2026-05-18 16:36:09', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056292745700560899, 2056265083410006018, 2056292745700560898, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:36:09', '2026-05-18 16:36:09', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056292788008505346, 2056265083410006018, 2056292745700560898, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:36:19', '2026-05-18 16:36:19', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056292789384237058, 2056265083410006018, 2056292789384237057, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:36:20', '2026-05-18 16:36:20', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056292895806312449, 2056265083410006018, 2056292789384237057, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:36:45', '2026-05-18 16:36:45', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056292900143222786, 2056265083410006018, 2056292900071919618, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:36:46', '2026-05-18 16:36:46', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293235591073793, 2056265083410006018, 2056292900071919618, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:38:06', '2026-05-18 16:38:06', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293235658182658, 2056265083410006018, 2056293235658182657, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:38:06', '2026-05-18 16:38:06', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293277240512514, 2056265083410006018, 2056293235658182657, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:38:16', '2026-05-18 16:38:16', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293277366341635, 2056265083410006018, 2056293277366341634, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:38:16', '2026-05-18 16:38:16', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293319187746817, 2056265083410006018, 2056293277366341634, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:38:26', '2026-05-18 16:38:26', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293319254855682, 2056265083410006018, 2056293319254855681, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:38:26', '2026-05-18 16:38:26', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293637065658370, 2056265083410006018, 2056293319254855681, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:39:42', '2026-05-18 16:39:42', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293637602529283, 2056265083410006018, 2056293637602529282, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:39:42', '2026-05-18 16:39:42', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293691063128065, 2056265083410006018, 2056293319254855681, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:39:55', '2026-05-18 16:39:55', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293691239288835, 2056265083410006018, 2056293691239288834, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:39:55', '2026-05-18 16:39:55', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293948287209474, 2056265083410006018, 2056293691239288834, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:40:56', '2026-05-18 16:40:56', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056293948740194307, 2056265083410006018, 2056293948740194306, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:40:56', '2026-05-18 16:40:56', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056294159067762690, 2056265083410006018, 2056293948740194306, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:41:46', '2026-05-18 16:41:46', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056294159134871555, 2056265083410006018, 2056294159134871554, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:41:46', '2026-05-18 16:41:46', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056294203628048386, 2056265083410006018, 2056294159134871554, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:41:57', '2026-05-18 16:41:57', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056294204609515523, 2056265083410006018, 2056294204609515522, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:41:57', '2026-05-18 16:41:57', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297229893771266, 2056265083410006018, 2056294204609515522, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:53:58', '2026-05-18 16:53:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297231055593474, 2056265083410006018, 2056297231055593473, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:53:59', '2026-05-18 16:53:59', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297507737051138, 2056265083410006018, 2056297231055593473, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:55:05', '2026-05-18 16:55:05', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297507862880259, 2056265083410006018, 2056297507862880258, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:55:05', '2026-05-18 16:55:05', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297550648975361, 2056265083410006018, 2056297507862880258, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:55:15', '2026-05-18 16:55:15', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297550850301954, 2056265083410006018, 2056297550850301953, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:55:15', '2026-05-18 16:55:15', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297605028126722, 2056265083410006018, 2056297550850301953, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:55:28', '2026-05-18 16:55:28', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297605472722946, 2056265083410006018, 2056297605472722945, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:55:28', '2026-05-18 16:55:28', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297630890205186, 2056265083410006018, 2056297605472722945, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:55:34', '2026-05-18 16:55:34', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297631020228610, 2056265083410006018, 2056297631020228609, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:55:34', '2026-05-18 16:55:34', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297924550205442, 2056265083410006018, 2056297631020228609, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:56:44', '2026-05-18 16:56:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297925137408003, 2056265083410006018, 2056297925137408002, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:56:44', '2026-05-18 16:56:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297967185305602, 2056265083410006018, 2056297925137408002, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:56:54', '2026-05-18 16:56:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056297968359710723, 2056265083410006018, 2056297968359710722, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:56:54', '2026-05-18 16:56:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298008662777858, 2056265083410006018, 2056297968359710722, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:57:04', '2026-05-18 16:57:04', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298009312894979, 2056265083410006018, 2056298009312894978, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:57:04', '2026-05-18 16:57:04', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298053084651522, 2056265083410006018, 2056298009312894978, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:57:15', '2026-05-18 16:57:15', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298053147566083, 2056265083410006018, 2056298053147566082, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:57:15', '2026-05-18 16:57:15', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298385885896705, 2056265083410006018, 2056298053147566082, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 16:58:34', '2026-05-18 16:58:34', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298386020114435, 2056265083410006018, 2056298386020114434, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 16:58:34', '2026-05-18 16:58:34', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298428537774081, 2056265083410006018, 2056298386020114434, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 16:58:44', '2026-05-18 16:58:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298428600688642, 2056265083410006018, 2056298428600688641, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 16:58:44', '2026-05-18 16:58:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298470120103937, 2056265083410006018, 2056298428600688641, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 16:58:54', '2026-05-18 16:58:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298470178824194, 2056265083410006018, 2056298470178824193, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 16:58:54', '2026-05-18 16:58:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298512398688257, 2056265083410006018, 2056298470178824193, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 16:59:04', '2026-05-18 16:59:04', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298512532905987, 2056265083410006018, 2056298512532905986, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 16:59:04', '2026-05-18 16:59:04', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298847318056961, 2056265083410006018, 2056298512532905986, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 17:00:24', '2026-05-18 17:00:24', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298847452274690, 2056265083410006018, 2056298847452274689, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 17:00:24', '2026-05-18 17:00:24', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298889315622913, 2056265083410006018, 2056298847452274689, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 17:00:34', '2026-05-18 17:00:34', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298889374343170, 2056265083410006018, 2056298889374343169, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 17:00:34', '2026-05-18 17:00:34', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298931212525570, 2056265083410006018, 2056298889374343169, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 17:00:44', '2026-05-18 17:00:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298931619373059, 2056265083410006018, 2056298931619373058, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 17:00:44', '2026-05-18 17:00:44', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298973231063041, 2056265083410006018, 2056298931619373058, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 17:00:54', '2026-05-18 17:00:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056298973830848515, 2056265083410006018, 2056298973830848514, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 17:00:54', '2026-05-18 17:00:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316770623459330, 2056316768786354177, NULL, NULL, 'start', '流程实例启动成功, version=6', '2026-05-18 18:11:37', '2026-05-18 18:11:37', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316829331132418, 2056316768786354177, 2056316771785281537, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":128,\"value\":\"开始\"}', '2026-05-18 18:11:51', '2026-05-18 18:11:51', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316830371319810, 2056316768786354177, 2056316830111272961, 'db_query', 'transition', '从 [{\"x\":304,\"y\":128,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}]', '2026-05-18 18:11:52', '2026-05-18 18:11:52', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316854299824130, 2056316768786354177, 2056316830111272961, 'db_query', 'execute', '节点执行成功: {\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}', '2026-05-18 18:11:57', '2026-05-18 18:11:57', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316854622785539, 2056316768786354177, 2056316854622785538, 'script_assemble', 'transition', '从 [{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}] 流转到 [{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}]', '2026-05-18 18:11:57', '2026-05-18 18:11:57', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316907018031105, 2056316768786354177, 2056316854622785538, 'script_assemble', 'execute', '节点执行成功: {\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}', '2026-05-18 18:12:10', '2026-05-18 18:12:10', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316907148054529, 2056316768786354177, 2056316907085139970, 'api_call_b', 'transition', '从 [{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}] 流转到 [{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}]', '2026-05-18 18:12:10', '2026-05-18 18:12:10', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316938324316161, 2056316768786354177, 2056316907085139970, 'api_call_b', 'execute', '节点执行成功: {\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}', '2026-05-18 18:12:17', '2026-05-18 18:12:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316938458533890, 2056316768786354177, 2056316938458533889, 'script_assemble_c', 'transition', '从 [{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}] 流转到 [{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}]', '2026-05-18 18:12:17', '2026-05-18 18:12:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316979034230785, 2056316768786354177, 2056316938458533889, 'script_assemble_c', 'execute', '节点执行成功: {\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}', '2026-05-18 18:12:27', '2026-05-18 18:12:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056316979231363075, 2056316768786354177, 2056316979231363074, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 18:12:27', '2026-05-18 18:12:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317313714524161, 2056316768786354177, 2056316979231363074, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 18:13:47', '2026-05-18 18:13:47', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317313915850755, 2056316768786354177, 2056317313915850754, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 18:13:47', '2026-05-18 18:13:47', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317358622937090, 2056316768786354177, 2056317313915850754, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 18:13:57', '2026-05-18 18:13:57', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317358748766211, 2056316768786354177, 2056317358748766210, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 18:13:58', '2026-05-18 18:13:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317400071049217, 2056316768786354177, 2056317358748766210, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 18:14:07', '2026-05-18 18:14:07', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317400322707459, 2056316768786354177, 2056317400322707458, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 18:14:07', '2026-05-18 18:14:07', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317440688689153, 2056316768786354177, 2056317400322707458, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 18:14:17', '2026-05-18 18:14:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317441414303746, 2056316768786354177, 2056317441414303745, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 18:14:17', '2026-05-18 18:14:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317775079575554, 2056316768786354177, 2056317441414303745, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 18:15:37', '2026-05-18 18:15:37', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317775276707843, 2056316768786354177, 2056317775276707842, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 18:15:37', '2026-05-18 18:15:37', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317817051975682, 2056316768786354177, 2056317775276707842, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 18:15:47', '2026-05-18 18:15:47', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317817576263683, 2056316768786354177, 2056317817576263682, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 18:15:47', '2026-05-18 18:15:47', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317861343825921, 2056316768786354177, 2056317817576263682, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 18:15:57', '2026-05-18 18:15:57', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317861406740481, 2056316768786354177, 2056317861343825922, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 18:15:57', '2026-05-18 18:15:57', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317901869191169, 2056316768786354177, 2056317861343825922, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 18:16:07', '2026-05-18 18:16:07', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056317901944688643, 2056316768786354177, 2056317901944688642, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 18:16:07', '2026-05-18 18:16:07', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318237342208002, 2056316768786354177, 2056317901944688642, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 18:17:27', '2026-05-18 18:17:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318242572505089, 2056316768786354177, 2056318242245349377, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 18:17:28', '2026-05-18 18:17:28', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318277875961857, 2056316768786354177, 2056318242245349377, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 18:17:37', '2026-05-18 18:17:37', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318278798708739, 2056316768786354177, 2056318278798708738, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 18:17:37', '2026-05-18 18:17:37', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318320137768962, 2056316768786354177, 2056318278798708738, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 18:17:47', '2026-05-18 18:17:47', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318320137768964, 2056316768786354177, 2056318320137768963, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 18:17:47', '2026-05-18 18:17:47', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318361967562753, 2056316768786354177, 2056318320137768963, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 18:17:57', '2026-05-18 18:17:57', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318362668011522, 2056316768786354177, 2056318362668011521, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 18:17:57', '2026-05-18 18:17:57', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318697365082114, 2056316768786354177, 2056318362668011521, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 18:19:17', '2026-05-18 18:19:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318697427996674, 2056316768786354177, 2056318697365082115, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 18:19:17', '2026-05-18 18:19:17', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318739224236033, 2056316768786354177, 2056318697365082115, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 18:19:27', '2026-05-18 18:19:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318739354259459, 2056316768786354177, 2056318739354259458, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 18:19:27', '2026-05-18 18:19:27', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318781179858946, 2056316768786354177, 2056318739354259458, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 18:19:37', '2026-05-18 18:19:37', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318781360214019, 2056316768786354177, 2056318781360214018, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 18:19:37', '2026-05-18 18:19:37', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318824095977473, 2056316768786354177, 2056318781360214018, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 18:19:47', '2026-05-18 18:19:47', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056318824095977475, 2056316768786354177, 2056318824095977474, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 18:19:47', '2026-05-18 18:19:47', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322440483610626, 2056322438893969409, NULL, NULL, 'start', '流程实例启动成功, version=6', '2026-05-18 18:34:09', '2026-05-18 18:34:09', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322495840034817, 2056322438893969409, 2056322441519603714, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":128,\"value\":\"开始\"}', '2026-05-18 18:34:22', '2026-05-18 18:34:22', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322496678895617, 2056322438893969409, 2056322496230105089, 'db_query', 'transition', '从 [{\"x\":304,\"y\":128,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}]', '2026-05-18 18:34:22', '2026-05-18 18:34:22', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322521840525314, 2056322438893969409, 2056322496230105089, 'db_query', 'execute', '节点执行成功: {\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}', '2026-05-18 18:34:28', '2026-05-18 18:34:29', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322522104766467, 2056322438893969409, 2056322522104766466, 'script_assemble', 'transition', '从 [{\"x\":304,\"y\":240,\"value\":\"查询收件数据\"}] 流转到 [{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}]', '2026-05-18 18:34:29', '2026-05-18 18:34:29', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322570184073217, 2056322438893969409, 2056322522104766466, 'script_assemble', 'execute', '节点执行成功: {\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}', '2026-05-18 18:34:40', '2026-05-18 18:34:40', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322570645446659, 2056322438893969409, 2056322570645446658, 'api_call_b', 'transition', '从 [{\"x\":304,\"y\":368,\"value\":\"组装B系统报文\"}] 流转到 [{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}]', '2026-05-18 18:34:40', '2026-05-18 18:34:40', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322606523523074, 2056322438893969409, 2056322570645446658, 'api_call_b', 'execute', '节点执行成功: {\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}', '2026-05-18 18:34:49', '2026-05-18 18:34:49', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322606653546499, 2056322438893969409, 2056322606653546498, 'script_assemble_c', 'transition', '从 [{\"x\":304,\"y\":480,\"value\":\"调用B系统提交\"}] 流转到 [{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}]', '2026-05-18 18:34:49', '2026-05-18 18:34:49', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322646675595266, 2056322438893969409, 2056322606653546498, 'script_assemble_c', 'execute', '节点执行成功: {\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}', '2026-05-18 18:34:58', '2026-05-18 18:34:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322646935642115, 2056322438893969409, 2056322646935642114, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":608,\"value\":\"组装C接口请求\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 18:34:58', '2026-05-18 18:34:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322939555454978, 2056322438893969409, 2056322646935642114, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 18:36:08', '2026-05-18 18:36:08', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322939622563842, 2056322438893969409, 2056322939622563841, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 18:36:08', '2026-05-18 18:36:08', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322981632712705, 2056322438893969409, 2056322939622563841, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 18:36:18', '2026-05-18 18:36:18', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056322981901148163, 2056322438893969409, 2056322981901148162, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 18:36:18', '2026-05-18 18:36:18', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323023827410946, 2056322438893969409, 2056322981901148162, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 18:36:28', '2026-05-18 18:36:28', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323024020348931, 2056322438893969409, 2056323024020348930, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 18:36:28', '2026-05-18 18:36:28', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323066164715522, 2056322438893969409, 2056323024020348930, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 18:36:38', '2026-05-18 18:36:38', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323066277961730, 2056322438893969409, 2056323066277961729, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 18:36:38', '2026-05-18 18:36:38', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323359052963842, 2056322438893969409, 2056323066277961729, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 18:37:48', '2026-05-18 18:37:48', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323359250096130, 2056322438893969409, 2056323359250096129, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 18:37:48', '2026-05-18 18:37:48', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323401021169666, 2056322438893969409, 2056323359250096129, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 18:37:58', '2026-05-18 18:37:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323401088278530, 2056322438893969409, 2056323401088278529, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 18:37:58', '2026-05-18 18:37:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323442985181185, 2056322438893969409, 2056323401088278529, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 18:38:08', '2026-05-18 18:38:08', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323443115204611, 2056322438893969409, 2056323443115204610, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 18:38:08', '2026-05-18 18:38:08', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323484869500930, 2056322438893969409, 2056323443115204610, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 18:38:18', '2026-05-18 18:38:18', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323484928221186, 2056322438893969409, 2056323484928221185, 'timer_wait', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}]', '2026-05-18 18:38:18', '2026-05-18 18:38:18', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323778420449282, 2056322438893969409, 2056323484928221185, 'timer_wait', 'execute', '定时节点完成（旧实例兼容）', '2026-05-18 18:39:28', '2026-05-18 18:39:28', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323778487558146, 2056322438893969409, 2056323778487558145, 'api_poll_c', 'transition', '从 [{\"x\":304,\"y\":720,\"value\":\"等待60秒\"}] 流转到 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}]', '2026-05-18 18:39:28', '2026-05-18 18:39:28', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323820795502594, 2056322438893969409, 2056323778487558145, 'api_poll_c', 'execute', '节点执行成功: {\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}', '2026-05-18 18:39:38', '2026-05-18 18:39:38', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323820925526019, 2056322438893969409, 2056323820925526018, 'script_parse_c', 'transition', '从 [{\"x\":304,\"y\":848,\"value\":\"轮询C接口状态\"}] 流转到 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}]', '2026-05-18 18:39:38', '2026-05-18 18:39:38', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323863153778690, 2056322438893969409, 2056323820925526018, 'script_parse_c', 'execute', '节点执行成功: {\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}', '2026-05-18 18:39:48', '2026-05-18 18:39:48', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323863740981251, 2056322438893969409, 2056323863740981250, 'cond_is_done', 'transition', '从 [{\"x\":304,\"y\":960,\"value\":\"解析C接口响应\"}] 流转到 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}]', '2026-05-18 18:39:48', '2026-05-18 18:39:48', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323904262152194, 2056322438893969409, 2056323863740981250, 'cond_is_done', 'execute', '节点执行成功: {\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}', '2026-05-18 18:39:58', '2026-05-18 18:39:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323904396369922, 2056322438893969409, NULL, 'end_1', 'transition', '流程执行完成', '2026-05-18 18:39:58', '2026-05-18 18:39:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2056323904396369923, 2056322438893969409, NULL, 'end_1', 'transition', '从 [{\"x\":304,\"y\":1088,\"value\":\"是否已办结\"}] 流转到 [{\"x\":496,\"y\":1200,\"value\":\"结束\"}]（流程结束）', '2026-05-18 18:39:58', '2026-05-18 18:39:58', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057108729734709249, 2057108722059132929, NULL, NULL, 'start', '流程实例启动成功, version=1', '2026-05-20 22:38:35', '2026-05-20 22:38:35', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057108786596888577, 2057108722059132929, 2057108732695887874, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":96,\"value\":\"开始\"}', '2026-05-20 22:38:49', '2026-05-20 22:38:49', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057108787838402562, 2057108722059132929, 2057108787708379137, 'minio_upload', 'transition', '从 [{\"x\":304,\"y\":96,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}]', '2026-05-20 22:38:49', '2026-05-20 22:38:49', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057108813255884802, 2057108722059132929, 2057108787708379137, 'minio_upload', 'error', '节点执行失败，流程挂起: MinIO操作失败: Illegal char <:> at index 5: https://picsum.photos/800/600.jpg', '2026-05-20 22:38:55', '2026-05-20 22:38:55', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057111302369771521, 2057111300486529025, NULL, NULL, 'start', '流程实例启动成功, version=1', '2026-05-20 22:48:48', '2026-05-20 22:48:48', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057111324452782081, 2057111300486529025, 2057111303611285505, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":96,\"value\":\"开始\"}', '2026-05-20 22:48:54', '2026-05-20 22:48:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057111324784132099, 2057111300486529025, 2057111324784132098, 'minio_upload', 'transition', '从 [{\"x\":304,\"y\":96,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}]', '2026-05-20 22:48:54', '2026-05-20 22:48:54', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057111369713516546, 2057111300486529025, 2057111324784132098, 'minio_upload', 'error', '节点执行失败，流程挂起: MinIO操作失败: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target', '2026-05-20 22:49:04', '2026-05-20 22:49:04', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057111957809463298, 2057111956442120193, NULL, NULL, 'start', '流程实例启动成功, version=1', '2026-05-20 22:51:25', '2026-05-20 22:51:25', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057111980542590978, 2057111956442120193, 2057111958790930433, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":96,\"value\":\"开始\"}', '2026-05-20 22:51:30', '2026-05-20 22:51:30', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057111980542590980, 2057111956442120193, 2057111980542590979, 'minio_upload', 'transition', '从 [{\"x\":304,\"y\":96,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}]', '2026-05-20 22:51:30', '2026-05-20 22:51:30', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057112042026893314, 2057111956442120193, 2057111980542590979, 'minio_upload', 'error', '节点执行失败，流程挂起: MinIO操作失败: Failed to connect to localhost/0:0:0:0:0:0:0:1:9000', '2026-05-20 22:51:45', '2026-05-20 22:51:45', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057113030821478402, 2057113029261197313, NULL, NULL, 'start', '流程实例启动成功, version=2', '2026-05-20 22:55:41', '2026-05-20 22:55:41', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057113071262957570, 2057113029261197313, 2057113032570503170, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":96,\"value\":\"开始\"}', '2026-05-20 22:55:50', '2026-05-20 22:55:50', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057113071342649346, 2057113029261197313, 2057113071262957571, 'minio_upload', 'transition', '从 [{\"x\":304,\"y\":96,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}]', '2026-05-20 22:55:50', '2026-05-20 22:55:50', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057113131967119362, 2057113029261197313, 2057113071262957571, 'minio_upload', 'error', '节点执行失败，流程挂起: MinIO操作失败: Failed to connect to localhost/0:0:0:0:0:0:0:1:9000', '2026-05-20 22:56:05', '2026-05-20 22:56:05', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057113594728873985, 2057113593554468865, NULL, NULL, 'start', '流程实例启动成功, version=3', '2026-05-20 22:57:55', '2026-05-20 22:57:55', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057113617243897858, 2057113593554468865, 2057113595701952514, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":96,\"value\":\"开始\"}', '2026-05-20 22:58:00', '2026-05-20 22:58:00', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057113617243897860, 2057113593554468865, 2057113617243897859, 'minio_upload', 'transition', '从 [{\"x\":304,\"y\":96,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}]', '2026-05-20 22:58:00', '2026-05-20 22:58:00', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057113669827887105, 2057113593554468865, 2057113617243897859, 'minio_upload', 'execute', '节点执行成功: {\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}', '2026-05-20 22:58:13', '2026-05-20 22:58:13', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057113670092128257, 2057113593554468865, NULL, 'end_1', 'transition', '流程执行完成', '2026-05-20 22:58:13', '2026-05-20 22:58:13', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057113670092128258, 2057113593554468865, NULL, 'end_1', 'transition', '从 [{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}] 流转到 [{\"x\":304,\"y\":336,\"value\":\"结束\"}]（流程结束）', '2026-05-20 22:58:13', '2026-05-20 22:58:13', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057120925642457089, 2057120924065398785, NULL, NULL, 'start', '流程实例启动成功, version=3', '2026-05-20 23:27:03', '2026-05-20 23:27:03', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057120956361539586, 2057120924065398785, 2057120926565203969, 'start_1', 'execute', '节点执行成功: {\"x\":304,\"y\":96,\"value\":\"开始\"}', '2026-05-20 23:27:10', '2026-05-20 23:27:10', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057120956361539588, 2057120924065398785, 2057120956361539587, 'minio_upload', 'transition', '从 [{\"x\":304,\"y\":96,\"value\":\"开始\"}] 流转到 [{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}]', '2026-05-20 23:27:10', '2026-05-20 23:27:10', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057121003522293761, 2057120924065398785, 2057120956361539587, 'minio_upload', 'execute', '节点执行成功: {\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}', '2026-05-20 23:27:21', '2026-05-20 23:27:21', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057121003581014018, 2057120924065398785, NULL, 'end_1', 'transition', '流程执行完成', '2026-05-20 23:27:21', '2026-05-20 23:27:21', NULL, NULL, 0);
INSERT INTO `wf_flow_log` VALUES (2057121003660705794, 2057120924065398785, NULL, 'end_1', 'transition', '从 [{\"x\":304,\"y\":224,\"value\":\"上传文件到MinIO\"}] 流转到 [{\"x\":304,\"y\":336,\"value\":\"结束\"}]（流程结束）', '2026-05-20 23:27:21', '2026-05-20 23:27:21', NULL, NULL, 0);

-- ----------------------------
-- Table structure for wf_flow_node
-- ----------------------------
DROP TABLE IF EXISTS `wf_flow_node`;
CREATE TABLE `wf_flow_node`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `flow_id` bigint(20) NOT NULL COMMENT '所属流程定义ID',
  `node_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '画布节点ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点名称',
  `node_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点类型：start/api/db/script/condition/timer/end',
  `config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '节点配置JSON',
  `input_mapping` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '输入映射JSON',
  `output_mapping` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '输出映射JSON',
  `cron_expression` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Cron表达式',
  `timeout` int(11) NULL DEFAULT 30000 COMMENT '超时毫秒',
  `retry_times` tinyint(4) NULL DEFAULT 3 COMMENT '重试次数',
  `fail_strategy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'suspend' COMMENT '失败策略：suspend-挂起 skip-跳过 retry-重试',
  `sort_no` int(11) NULL DEFAULT 0 COMMENT '排序号',
  `x_coordinate` decimal(10, 2) NULL DEFAULT NULL COMMENT 'X坐标',
  `y_coordinate` decimal(10, 2) NULL DEFAULT NULL COMMENT 'Y坐标',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flow_node`(`flow_id`, `node_id`) USING BTREE,
  INDEX `idx_flow`(`flow_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程节点' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_flow_node
-- ----------------------------

-- ----------------------------
-- Table structure for wf_flow_task
-- ----------------------------
DROP TABLE IF EXISTS `wf_flow_task`;
CREATE TABLE `wf_flow_task`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `instance_id` bigint(20) NOT NULL COMMENT '流程实例ID',
  `node_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '节点名称',
  `node_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '节点类型',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态：pending/running/success/fail/waiting/skipped',
  `input_context` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '执行前上下文快照',
  `output_context` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '执行后上下文快照',
  `result_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '执行结果',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `execute_count` int(11) NULL DEFAULT 0 COMMENT '执行次数',
  `next_execute_time` datetime(0) NULL DEFAULT NULL COMMENT '下次执行时间',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_instance`(`instance_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_next_time`(`next_execute_time`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程任务实例' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_flow_task
-- ----------------------------

-- ----------------------------
-- Table structure for wf_item
-- ----------------------------
DROP TABLE IF EXISTS `wf_item`;
CREATE TABLE `wf_item`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `item_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事项编码',
  `item_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事项名称',
  `region_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区划代码',
  `region_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区划名称',
  `catalog_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '国家基本编码',
  `task_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '国家实施编码',
  `task_handle_item` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '国家业务办理项编码',
  `service_obj` tinyint(4) NULL DEFAULT 0 COMMENT '办理对象：0-个人 1-法人',
  `flow_id` bigint(20) NULL DEFAULT NULL COMMENT '绑定流程定义ID',
  `flow_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '流程编码',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_item_code`(`item_code`) USING BTREE,
  INDEX `idx_region`(`region_code`) USING BTREE,
  INDEX `idx_flow`(`flow_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '政务服务事项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_item
-- ----------------------------

-- ----------------------------
-- Table structure for wf_region
-- ----------------------------
DROP TABLE IF EXISTS `wf_region`;
CREATE TABLE `wf_region`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `region_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区划代码',
  `region_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区划名称',
  `parent_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '父级代码',
  `level` tinyint(4) NULL DEFAULT 1 COMMENT '层级：1-省 2-市 3-区县',
  `sort_no` int(11) NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_region_code`(`region_code`) USING BTREE,
  INDEX `idx_parent`(`parent_code`) USING BTREE,
  INDEX `idx_level`(`level`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '行政区划' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_region
-- ----------------------------
INSERT INTO `wf_region` VALUES (1, '610000', '陕西省', '0', 1, 1, '2026-05-13 15:06:37', '2026-05-13 15:06:37', '', '', 0);
INSERT INTO `wf_region` VALUES (2, '610100', '西安市', '610000', 2, 2, '2026-05-13 15:06:37', '2026-05-13 15:06:37', '', '', 0);
INSERT INTO `wf_region` VALUES (3, '610102', '新城区', '610100', 3, 3, '2026-05-13 15:06:37', '2026-05-13 15:06:37', '', '', 0);
INSERT INTO `wf_region` VALUES (4, '610103', '碑林区', '610100', 3, 4, '2026-05-13 15:06:37', '2026-05-13 15:06:37', '', '', 0);
INSERT INTO `wf_region` VALUES (5, '610104', '莲湖区', '610100', 3, 5, '2026-05-13 15:06:37', '2026-05-13 15:06:37', '', '', 0);
INSERT INTO `wf_region` VALUES (6, '610200', '铜川市', '610000', 2, 6, '2026-05-13 15:06:37', '2026-05-13 15:06:37', '', '', 0);

-- ----------------------------
-- Table structure for wf_transfer_queue
-- ----------------------------
DROP TABLE IF EXISTS `wf_transfer_queue`;
CREATE TABLE `wf_transfer_queue`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `biz_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务主键（办件流水号等）',
  `source_data` json NULL COMMENT '上游传入的原始业务数据',
  `downstream_handle_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '下游系统返回的办理编号',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态：pending-待办理 / processing-办理中 / done-已完成',
  `callback_data` json NULL COMMENT '下游回调时传入的数据',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_biz_key`(`biz_key`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_update_time`(`update_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '中台业务中转队列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_transfer_queue
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
-- 为 wf_api_catalog 表增加代理后路径和请求方式字段
-- 支持用户自定义对外暴露的路径和请求方式

-- 1. 新增 open_path 字段（代理后暴露路径）
ALTER TABLE wf_api_catalog
    ADD COLUMN open_path VARCHAR(200) DEFAULT NULL COMMENT '代理后暴露路径，如 /user/list' AFTER url;

-- 2. 新增 open_method 字段（代理后请求方式）
ALTER TABLE wf_api_catalog
    ADD COLUMN open_method VARCHAR(10) DEFAULT 'POST' COMMENT '代理后请求方式：GET/POST/PUT/DELETE' AFTER method;

-- 3. 迁移现有数据：open_path 默认使用 /{apiCode}，open_method 默认使用原 method
UPDATE wf_api_catalog
SET open_path = CONCAT('/', api_code),
    open_method = method
WHERE open_path IS NULL;

-- 4. 增加唯一约束，防止路径和方式冲突
ALTER TABLE wf_api_catalog
    ADD UNIQUE KEY uk_open_path_method (open_path, open_method);


-- ----------------------------
-- 接口注册模块插件化支持
-- 日期: 2026-06-04
-- ----------------------------

-- 1. sys_plugin 增加 plugin_scope 字段，区分插件作用域
ALTER TABLE `sys_plugin`
    ADD COLUMN `plugin_scope` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
    NULL DEFAULT 'node' COMMENT '插件作用域：node-流程节点 api-接口注册 both-两者皆可' AFTER `plugin_version`,
    ADD INDEX `idx_scope` (`plugin_scope`);

-- 2. wf_api_catalog 增加 plugin_type 字段
ALTER TABLE `wf_api_catalog`
    ADD COLUMN `plugin_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
    NULL DEFAULT NULL COMMENT '插件类型标识，api_type=plugin 时生效' AFTER `script_id`,
    ADD INDEX `idx_plugin_type` (`plugin_type`);

-- 3. 更新 sys_plugin 现有数据的作用域为 node（兼容历史数据）
UPDATE `sys_plugin` SET `plugin_scope` = 'node' WHERE `plugin_scope` IS NULL;
