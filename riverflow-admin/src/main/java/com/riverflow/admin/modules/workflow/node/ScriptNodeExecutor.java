package com.riverflow.admin.modules.workflow.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.infra.groovy.GroovySandboxExecutor;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.api.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 脚本节点执行器
 * 执行 Groovy 脚本，支持输入变量绑定和输出映射
 */
@Slf4j
@Component
public class ScriptNodeExecutor implements NodeExecutor {

    @Autowired
    private GroovySandboxExecutor groovyExecutor;

    @Override
    public String getNodeType() {
        return "script";
    }

    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] 执行脚本节点: {}", context.getInstanceId(), node.getNodeName());

        String configJson = node.getConfigJson();
        if (configJson == null || configJson.trim().isEmpty()) {
            return NodeExecuteResult.fail("脚本节点缺少配置");
        }

        JSONObject config = JSON.parseObject(configJson);
        // 兼容前端字段名 scriptContent 和 script
        String script = config.getString("script");
        if (script == null || script.isEmpty()) {
            script = config.getString("scriptContent");
        }
        if (script == null || script.isEmpty()) {
            return NodeExecuteResult.fail("脚本节点未配置脚本内容");
        }

        // 组装脚本变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("ctx", context.toMap());
        variables.put("context", context);
        variables.put("instanceId", context.getInstanceId());

        // 输入映射
        String inputMapping = node.getInputMapping();
        if (inputMapping != null && !inputMapping.isEmpty()) {
            try {
                JSONArray mappings = JSON.parseArray(inputMapping);
                for (int i = 0; i < mappings.size(); i++) {
                    JSONObject map = mappings.getJSONObject(i);
                    String source = map.getString("source");
                    String target = map.getString("target");
                    Object value = context.getByPath(source);
                    if (value != null) {
                        variables.put(target, value);
                    }
                }
            } catch (Exception e) {
                log.warn("输入映射解析失败: {}", e.getMessage());
            }
        }

        try {
            Object result = groovyExecutor.execute(script, variables);
            log.info("[流程实例:{}] 脚本执行完成: result={}", context.getInstanceId(), result);

            // 构建结果对象
            JSONObject resultData = new JSONObject();
            resultData.put("result", result);

            // 输出映射
            applyOutputMapping(node, context, resultData);

            return NodeExecuteResult.success(resultData);
        } catch (Exception e) {
            log.error("[流程实例:{}] 脚本执行失败", context.getInstanceId(), e);
            return NodeExecuteResult.fail("脚本执行失败: " + e.getMessage());
        }
    }

    /**
     * 应用输出映射
     */
    private void applyOutputMapping(FlowNode node, FlowContext context, JSONObject resultData) {
        String outputMapping = node.getOutputMapping();
        if (outputMapping == null || outputMapping.isEmpty()) return;
        try {
            JSONArray mappings = JSON.parseArray(outputMapping);
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject map = mappings.getJSONObject(i);
                String source = map.getString("source");
                String target = map.getString("target");
                Object value = resolveResultPath(resultData, source);
                if (value != null) {
                    context.set(target.replace("context.", ""), value);
                }
            }
        } catch (Exception e) {
            log.warn("输出映射解析失败: {}", e.getMessage());
        }
    }

    private Object resolveResultPath(JSONObject result, String path) {
        if (path == null || path.isEmpty()) return null;
        String trimmed = path.trim();
        if (trimmed.startsWith("result.")) trimmed = trimmed.substring(7);
        if (trimmed.startsWith("script.")) trimmed = trimmed.substring(7);
        return result.getByPath(trimmed);
    }
}
