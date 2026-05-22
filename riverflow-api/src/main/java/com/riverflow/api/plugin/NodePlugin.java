package com.riverflow.api.plugin;

import com.riverflow.api.entity.FlowNode;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 流程节点插件接口
 * 所有自定义节点插件必须实现此接口
 * 
 * 使用方式：
 * 1. 实现此接口
 * 2. 在META-INF/services/com.riverflow.api.plugin.NodePlugin文件中注册实现类
 * 3. 打包为JAR放入plugins目录
 */
public interface NodePlugin {

    /**
     * 获取节点类型标识（唯一）
     * 例如：minio、email、sms等
     */
    String getNodeType();

    /**
     * 获取节点显示名称
     */
    String getNodeName();

    /**
     * 获取节点图标（Element Plus图标名称）
     */
    String getIcon();

    /**
     * 获取节点分类
     * 例如：storage、communication、integration等
     */
    String getCategory();

    /**
     * 获取节点描述
     */
    String getDescription();

    /**
     * 获取默认配置模板（JSON格式）
     * 用于前端生成配置表单
     */
    String getConfigTemplate();

    /**
     * 获取配置表单Schema（JSON格式）
     * 定义表单字段、类型、验证规则等
     * 
     * Schema格式示例：
     * {
     *   "fields": [
     *     {
     *       "name": "endpoint",
     *       "label": "服务地址",
     *       "type": "text",
     *       "required": true,
     *       "placeholder": "http://localhost:9000",
     *       "defaultValue": "http://localhost:9000"
     *     },
     *     {
     *       "name": "operation",
     *       "label": "操作类型",
     *       "type": "select",
     *       "required": true,
     *       "options": [
     *         {"label": "上传", "value": "upload"},
     *         {"label": "下载", "value": "download"}
     *       ]
     *     }
     *   ]
     * }
     */
    default String getConfigSchema() {
        return "{\"fields\":[]}";
    }

    /**
     * 初始化插件，由主项目在加载完成后调用
     * 插件可通过 ApplicationContext 获取主项目的 Spring Bean（如 RedisTemplate、JdbcTemplate 等）
     *
     * @param applicationContext Spring 应用上下文
     */
    default void init(ApplicationContext applicationContext) {
        // 默认空实现，兼容旧插件
    }

    /**
     * 执行节点逻辑
     *
     * @param node 节点定义
     * @param context 流程上下文数据
     * @return 执行结果
     */
    NodePluginResult execute(FlowNode node, Map<String, Object> context);

    /**
     * 验证节点配置是否有效
     *
     * @param configJson 配置JSON
     * @return 验证结果
     */
    default ValidationResult validateConfig(String configJson) {
        return ValidationResult.success();
    }
}
