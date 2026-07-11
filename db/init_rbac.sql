-- ----------------------------
-- RBAC 权限体系初始化脚本
-- 适用场景：在已有 RiverFlow 数据库基础上扩展角色-菜单-按钮权限体系
-- 执行前请确保 sys_user 表已存在
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 扩展 sys_user 表：增加部门字段（为后续数据权限预留）
-- ----------------------------
ALTER TABLE `sys_user`
    ADD COLUMN IF NOT EXISTS `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID' AFTER `phone`,
    ADD COLUMN IF NOT EXISTS `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '部门名称' AFTER `dept_id`;

-- ----------------------------
-- 2. 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `sort_no` int(11) NULL DEFAULT 0 COMMENT '显示排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3. 菜单/权限表
-- menu_type: 0-目录 1-菜单 2-按钮/API权限
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID，0为顶层',
  `menu_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint(4) NULL DEFAULT 1 COMMENT '菜单类型：0-目录 1-菜单 2-按钮/API权限',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识，如 system:user:list',
  `sort_no` int(11) NULL DEFAULT 0 COMMENT '显示排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_menu_type`(`menu_type`) USING BTREE,
  UNIQUE INDEX `uk_perms`(`perms`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统菜单/权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 4. 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 5. 角色菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单/权限ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id`, `menu_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单/权限关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 7. 部门表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父部门ID，0为顶层',
  `dept_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门编码',
  `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `leader` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `sort_no` int(11) NULL DEFAULT 0 COMMENT '显示排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '删除标志：0-正常 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dept_code`(`dept_code`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统部门' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 6. 初始化数据
-- ----------------------------

-- 6.1 初始化超级管理员角色
INSERT INTO `sys_role` VALUES (1, 'admin', '超级管理员', '拥有系统所有权限', 1, 1, NOW(), NOW(), 'system', 'system', 0);

-- 6.2 初始化菜单与按钮权限
-- 注意：以下菜单结构对应前端现有路由，perms 字段作为后端权限校验标识

-- 系统管理目录
INSERT INTO `sys_menu` VALUES (1, 0, '系统管理', 0, 'Setting', '/system', NULL, NULL, 100, 1, NOW(), NOW(), 'system', 'system', 0);

-- 用户管理
INSERT INTO `sys_menu` VALUES (11, 1, '用户管理', 1, 'User', '/system/user', 'system/user/index', 'system:user:list', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (111, 11, '用户新增', 2, NULL, NULL, NULL, 'system:user:add', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (112, 11, '用户修改', 2, NULL, NULL, NULL, 'system:user:edit', 2, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (113, 11, '用户删除', 2, NULL, NULL, NULL, 'system:user:delete', 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- 角色管理
INSERT INTO `sys_menu` VALUES (12, 1, '角色管理', 1, 'UserFilled', '/system/role', 'system/role/index', 'system:role:list', 2, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (121, 12, '角色新增', 2, NULL, NULL, NULL, 'system:role:add', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (122, 12, '角色修改', 2, NULL, NULL, NULL, 'system:role:edit', 2, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (123, 12, '角色删除', 2, NULL, NULL, NULL, 'system:role:delete', 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- 菜单管理
INSERT INTO `sys_menu` VALUES (13, 1, '菜单管理', 1, 'Menu', '/system/menu', 'system/menu/index', 'system:menu:list', 3, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (131, 13, '菜单新增', 2, NULL, NULL, NULL, 'system:menu:add', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (132, 13, '菜单修改', 2, NULL, NULL, NULL, 'system:menu:edit', 2, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (133, 13, '菜单删除', 2, NULL, NULL, NULL, 'system:menu:delete', 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- 部门管理
INSERT INTO `sys_menu` VALUES (14, 1, '部门管理', 1, 'OfficeBuilding', '/system/dept', 'system/dept/index', 'system:dept:list', 4, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (141, 14, '部门新增', 2, NULL, NULL, NULL, 'system:dept:add', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (142, 14, '部门修改', 2, NULL, NULL, NULL, 'system:dept:edit', 2, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (143, 14, '部门删除', 2, NULL, NULL, NULL, 'system:dept:delete', 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- 数据源管理
INSERT INTO `sys_menu` VALUES (21, 0, '数据源管理', 1, 'Coin', '/datasource', 'datasource/index', 'datasource:list', 200, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (211, 21, '数据源新增', 2, NULL, NULL, NULL, 'datasource:add', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (212, 21, '数据源修改', 2, NULL, NULL, NULL, 'datasource:edit', 2, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (213, 21, '数据源删除', 2, NULL, NULL, NULL, 'datasource:delete', 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- 事项管理
INSERT INTO `sys_menu` VALUES (22, 0, '事项管理', 1, 'Document', '/item', 'item/index', 'item:list', 300, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (221, 22, '事项新增', 2, NULL, NULL, NULL, 'item:add', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (222, 22, '事项修改', 2, NULL, NULL, NULL, 'item:edit', 2, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (223, 22, '事项删除', 2, NULL, NULL, NULL, 'item:delete', 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- 工作流管理（目录）
INSERT INTO `sys_menu` VALUES (23, 0, '流程管理', 0, 'Connection', '/workflow', NULL, 'workflow:list', 400, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (231, 23, '流程新增', 2, NULL, NULL, NULL, 'workflow:add', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (232, 23, '流程修改', 2, NULL, NULL, NULL, 'workflow:edit', 2, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (233, 23, '流程删除', 2, NULL, NULL, NULL, 'workflow:delete', 3, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (234, 23, '流程发布', 2, NULL, NULL, NULL, 'workflow:publish', 4, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (235, 23, '流程定义', 1, NULL, '/workflow/definition', 'workflow/definition/index', 'workflow:definition:list', 5, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (236, 23, '实例监控', 1, NULL, '/workflow/instance', 'workflow/instance/index', 'workflow:instance:list', 6, 1, NOW(), NOW(), 'system', 'system', 0);

-- 补充静态路由中已有的其他菜单
INSERT INTO `sys_menu` VALUES (31, 0, '数据大盘', 1, 'Odometer', '/dashboard', 'dashboard/index', 'dashboard:list', 0, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (32, 0, '动态表设计', 1, 'Grid', '/dynamic-table', 'dynamicTable/index', 'dynamic-table:list', 150, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (33, 0, '接口注册', 1, 'Link', '/api-mgr', 'apiMgr/index', 'api-mgr:list', 250, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (34, 0, 'AI模型管理', 1, 'Cpu', '/ai-model', 'ai/model/index', 'ai-model:list', 650, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (35, 0, '运行监控', 1, 'Monitor', '/monitor', 'monitor/index', 'monitor:list', 750, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (36, 0, '脚本管理', 1, 'DocumentCopy', '/script-mgr', 'scriptMgr/index', 'script-mgr:list', 850, 1, NOW(), NOW(), 'system', 'system', 0);

-- 接口管理
INSERT INTO `sys_menu` VALUES (24, 0, '接口管理', 1, 'Link', '/api-catalog', 'api/index', 'api:catalog:list', 500, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (241, 24, '接口新增', 2, NULL, NULL, NULL, 'api:catalog:add', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (242, 24, '接口修改', 2, NULL, NULL, NULL, 'api:catalog:edit', 2, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (243, 24, '接口删除', 2, NULL, NULL, NULL, 'api:catalog:delete', 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- AI 助手
INSERT INTO `sys_menu` VALUES (25, 0, 'AI 助手', 1, 'MagicStick', '/ai-assistant', 'ai/index', 'ai:assistant', 600, 1, NOW(), NOW(), 'system', 'system', 0);

-- 插件管理
INSERT INTO `sys_menu` VALUES (26, 0, '插件管理', 1, 'Box', '/plugin', 'plugin/index', 'plugin:list', 700, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (261, 26, '插件上传', 2, NULL, NULL, NULL, 'plugin:upload', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_menu` VALUES (262, 26, '插件删除', 2, NULL, NULL, NULL, 'plugin:delete', 2, 1, NOW(), NOW(), 'system', 'system', 0);

-- 操作日志
INSERT INTO `sys_menu` VALUES (27, 0, '操作日志', 1, 'DocumentCopy', '/operation-log', 'log/index', 'system:log:list', 800, 1, NOW(), NOW(), 'system', 'system', 0);

-- 6.3 初始化默认部门
INSERT INTO `sys_dept` VALUES (1, 0, 'root', '研发中心', 'admin', '13800138000', 'admin@riverflow.com', 1, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_dept` VALUES (2, 0, 'product', '产品中心', NULL, NULL, NULL, 2, 1, NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_dept` VALUES (3, 0, 'ops', '运维中心', NULL, NULL, NULL, 3, 1, NOW(), NOW(), 'system', 'system', 0);

-- 6.4 将 admin 用户与超级管理员角色关联，并设置默认部门
UPDATE `sys_user` SET `dept_id` = 1, `dept_name` = '研发中心' WHERE `id` = 1;
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `create_time`) VALUES (1, 1, NOW());

-- 6.5 将超级管理员角色与所有菜单/权限关联
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT 1, id, NOW() FROM `sys_menu`;
