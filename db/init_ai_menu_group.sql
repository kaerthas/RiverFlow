-- 将 AI 相关菜单统一聚合到 AI 根目录下
-- 执行后左侧菜单显示为：
-- AI
--   ├── AI 助手
--   ├── AI 模型管理
--   ├── AI Prompt 管理
--   └── AI 知识库管理
-- 注意：sys_menu.id 不是自增列，需要显式指定主键

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 判断 AI 根目录是否已存在
SET @ai_id = (SELECT `id` FROM `sys_menu` WHERE `path` = '/ai' AND `parent_id` = 0 LIMIT 1);

-- 2. 如果不存在，取一个未使用的菜单 ID 并插入 AI 根目录
SET @new_id = (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_menu`);

INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `icon`, `path`, `component`, `perms`, `sort_no`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`)
VALUES (COALESCE(@ai_id, @new_id), 0, 'AI', 0, 'MagicStick', '/ai', NULL, 'ai:list', 600, 1, NOW(), NOW(), 'system', 'system', 0);

-- 3. 重新获取 AI 根目录 ID（兼容已存在 / 刚插入两种情况）
SET @ai_id = (SELECT `id` FROM `sys_menu` WHERE `path` = '/ai' AND `parent_id` = 0 LIMIT 1);

-- 4. 修正 AI 助手路径
UPDATE `sys_menu` SET `path` = '/ai/assistant', `component` = 'ai/index' WHERE `menu_name` = 'AI 助手';

-- 5. 修正 AI 模型管理路径
UPDATE `sys_menu` SET `path` = '/ai/model', `component` = 'ai/model/index' WHERE `menu_name` = 'AI模型管理';

-- 6. 新增 AI Prompt 管理菜单（如已存在则忽略）
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `icon`, `path`, `component`, `perms`, `sort_no`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`)
VALUES (37, 0, 'AI Prompt 管理', 1, 'ChatDotSquare', '/ai/prompt', 'ai/prompt/index', 'ai:prompt:list', 625, 1, NOW(), NOW(), 'system', 'system', 0);

-- 6.1 新增 AI 知识库管理菜单（如已存在则忽略）
SET @knowledge_id = (SELECT `id` FROM `sys_menu` WHERE `path` = '/ai/knowledge' LIMIT 1);
SET @knowledge_new_id = (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_menu`);

INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `icon`, `path`, `component`, `perms`, `sort_no`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`)
VALUES (COALESCE(@knowledge_id, @knowledge_new_id), @ai_id, 'AI 知识库管理', 1, 'Collection', '/ai/knowledge', 'ai/knowledge/index', 'ai:knowledge:list', 630, 1, NOW(), NOW(), 'system', 'system', 0);

-- 6.2 新增向量库管理菜单（如已存在则忽略）
SET @vector_id = (SELECT `id` FROM `sys_menu` WHERE `path` = '/ai/vector' LIMIT 1);
SET @vector_new_id = (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `sys_menu`);

INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `icon`, `path`, `component`, `perms`, `sort_no`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`)
VALUES (COALESCE(@vector_id, @vector_new_id), @ai_id, '向量库管理', 1, 'Coin', '/ai/vector', 'ai/vector/index', 'ai:vector:list', 640, 1, NOW(), NOW(), 'system', 'system', 0);

-- 7. 把五个 AI 菜单挂到 AI 根目录下
UPDATE `sys_menu` SET `parent_id` = @ai_id WHERE `menu_name` IN ('AI 助手', 'AI模型管理', 'AI Prompt 管理', 'AI 知识库管理', '向量库管理');

-- 8. 将 AI 根目录及所有 AI 子菜单关联到超级管理员角色
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT 1, `id`, NOW() FROM `sys_menu` WHERE `menu_name` IN ('AI', 'AI 助手', 'AI模型管理', 'AI Prompt 管理', 'AI 知识库管理', '向量库管理') AND `del_flag` = 0;

SET FOREIGN_KEY_CHECKS = 1;
