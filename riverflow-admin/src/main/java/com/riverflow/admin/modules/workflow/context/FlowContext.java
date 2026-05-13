package com.riverflow.admin.modules.workflow.context;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程数据上下文
 * 负责节点间的数据流转与共享
 */
@Slf4j
public class FlowContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 上下文变量存储
     */
    private final Map<String, Object> variables = new ConcurrentHashMap<>();

    /**
     * 内置系统变量
     */
    private static final String SYS_INSTANCE_ID = "_instanceId";
    private static final String SYS_BUSINESS_KEY = "_businessKey";
    private static final String SYS_FLOW_CODE = "_flowCode";
    private static final String SYS_CURRENT_TIME = "_currentTime";

    public FlowContext() {
    }

    public FlowContext(Long instanceId, String businessKey, String flowCode) {
        this.variables.put(SYS_INSTANCE_ID, instanceId);
        this.variables.put(SYS_BUSINESS_KEY, businessKey);
        this.variables.put(SYS_FLOW_CODE, flowCode);
    }

    /**
     * 设置变量
     */
    public void set(String key, Object value) {
        this.variables.put(key, value);
    }

    /**
     * 获取变量（支持简单key）
     */
    public Object get(String key) {
        return this.variables.get(key);
    }

    /**
     * 获取变量（支持JSONPath）
     * 例如：context.apiResult.data[0].name
     */
    public Object getByPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String trimmed = path.trim();

        // 如果是简单的顶层key，直接返回
        if (!trimmed.contains(".")) {
            return this.variables.get(trimmed);
        }

        // 支持 context.xxx.yyy 格式
        if (trimmed.startsWith("context.")) {
            trimmed = trimmed.substring(8);
        }

        // 使用JSONPath解析嵌套路径
        try {
            JSONObject jsonObject = new JSONObject(new HashMap<>(this.variables));
            return JSONPath.eval(jsonObject, "$" + (trimmed.startsWith(".") ? trimmed : "." + trimmed));
        } catch (Exception e) {
            log.warn("JSONPath解析失败: path={}, variables={}", path, this.variables);
            return null;
        }
    }

    /**
     * 获取字符串值
     */
    public String getString(String key) {
        Object value = getByPath(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 转为Map（用于SpEL求值）
     */
    public Map<String, Object> toMap() {
        // 更新系统变量
        this.variables.put(SYS_CURRENT_TIME, System.currentTimeMillis());
        return new HashMap<>(this.variables);
    }

    /**
     * 转为JSON字符串
     */
    public String toJsonString() {
        return JSON.toJSONString(this.variables);
    }

    /**
     * 从JSON字符串恢复
     */
    public static FlowContext fromJson(String json) {
        FlowContext context = new FlowContext();
        if (json != null && !json.isEmpty()) {
            Map<String, Object> map = JSON.parseObject(json, Map.class);
            context.variables.putAll(map);
        }
        return context;
    }

    public Long getInstanceId() {
        Object val = get(SYS_INSTANCE_ID);
        return val != null ? Long.valueOf(val.toString()) : null;
    }

    public String getBusinessKey() {
        return getString(SYS_BUSINESS_KEY);
    }
}
