package com.riverflow.api.plugin;

import com.riverflow.api.entity.ApiCatalog;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 接口插件 SPI
 * 所有自定义接口插件必须实现此接口
 *
 * 使用方式：
 * 1. 实现此接口
 * 2. 在 META-INF/services/com.riverflow.api.plugin.ApiPlugin 文件中注册实现类
 * 3. 打包为 JAR 通过插件管理上传
 * 4. 在接口注册页面选择 api_type = plugin，并绑定对应的 plugin_type
 */
public interface ApiPlugin {

    /**
     * 获取插件类型标识（唯一）
     * 例如：minio、oss、custom-sdk 等
     */
    String getPluginType();

    /**
     * 获取插件显示名称
     */
    String getPluginName();

    /**
     * 获取插件图标（Element Plus 图标名称）
     */
    default String getIcon() {
        return "Connection";
    }

    /**
     * 获取插件分类
     * 例如：storage、communication、integration 等
     */
    default String getCategory() {
        return "integration";
    }

    /**
     * 获取插件描述
     */
    default String getDescription() {
        return "";
    }

    /**
     * 获取配置表单 Schema（JSON 格式）
     * 定义表单字段、类型、验证规则等
     *
     * Schema 格式示例：
     * {
     *   "fields": [
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
     *
     * @param applicationContext Spring 应用上下文
     */
    default void init(ApplicationContext applicationContext) {
        // 默认空实现
    }

    /**
     * 执行接口调用
     *
     * @param api    接口目录定义（包含 plugin_type、url 配置等）
     * @param params 请求参数（header/query/body 已合并的 Map）
     * @return 执行结果，建议返回 Map 或 JSONObject，由框架统一包装为 R.ok()
     */
    Object execute(ApiCatalog api, Map<String, Object> params);

    /**
     * 验证接口配置是否有效
     *
     * @param configJson 配置 JSON（通常来自 api.getUrl()）
     * @return 验证结果
     */
    default ValidationResult validateConfig(String configJson) {
        return ValidationResult.success();
    }
}
