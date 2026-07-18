-- AI Prompt 管理菜单初始化（已部署环境增量执行）
-- 同时把 AI 相关页面统一归到 /ai 根目录下

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 修正 AI 助手菜单路径
UPDATE `sys_menu` SET `path` = '/ai/assistant', `component` = 'ai/index' WHERE `menu_name` = 'AI 助手';

-- 2. 修正 AI 模型管理菜单路径
UPDATE `sys_menu` SET `path` = '/ai/model', `component` = 'ai/model/index' WHERE `menu_name` = 'AI模型管理';

-- 3. 新增 AI Prompt 管理菜单
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `icon`, `path`, `component`, `perms`, `sort_no`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`) VALUES
(37, 0, 'AI Prompt 管理', 1, 'ChatDotSquare', '/ai/prompt', 'ai/prompt/index', 'ai:prompt:list', 625, 1, NOW(), NOW(), 'system', 'system', 0);

-- 4. 将 AI Prompt 管理权限关联到超级管理员角色
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT 1, id, NOW() FROM `sys_menu` WHERE id = 37;

SET FOREIGN_KEY_CHECKS = 1;
