package com.riverflow.admin.modules.workflow.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.infra.plugin.NodePluginLoader;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.plugin.NodePlugin;
import com.riverflow.api.plugin.NodePluginResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 节点执行器工厂
 * 管理所有节点执行器，根据节点类型路由到对应实现
 * 
 * 支持两种节点类型：
 * 1. 内置节点：通过NodeExecutor接口实现，Spring自动注入
 * 2. 插件节点：通过NodePlugin接口实现，动态加载JAR包
 */
@Slf4j
@Component
public class NodeExecutorFactory {

    @Autowired
    private List<NodeExecutor> executors;

    @Autowired
    private NodePluginLoader pluginLoader;

    private final Map<String, NodeExecutor> executorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (NodeExecutor executor : executors) {
            executorMap.put(executor.getNodeType(), executor);
            log.info("注册内置节点执行器: type={}", executor.getNodeType());
        }

        log.info("内置节点执行器加载完成，共 {} 个", executorMap.size());
        log.info("插件节点加载完成，共 {} 个", pluginLoader.getPluginCount());
    }

    /**
     * 获取执行器
     * 优先查找内置执行器，如果没有则查找插件
     */
    public NodeExecutor getExecutor(String nodeType) {
        NodeExecutor executor = executorMap.get(nodeType);
        if (executor != null) {
            return executor;
        }

        if (pluginLoader.hasPlugin(nodeType)) {
            return new PluginExecutorAdapter(pluginLoader.getPlugin(nodeType));
        }

        throw new IllegalArgumentException("不支持的节点类型: " + nodeType);
    }

    /**
     * 检查节点类型是否支持
     */
    public boolean isSupported(String nodeType) {
        return executorMap.containsKey(nodeType) || pluginLoader.hasPlugin(nodeType);
    }

    /**
     * 获取所有支持的节点类型
     */
    public List<String> getAllSupportedTypes() {
        List<String> types = new ArrayList<>();
        types.addAll(executorMap.keySet());
        types.addAll(pluginLoader.getAllPlugins().keySet());
        return types;
    }

    /**
     * 插件执行器适配器
     * 将NodePlugin适配为NodeExecutor
     */
    private static class PluginExecutorAdapter implements NodeExecutor {

        private final NodePlugin plugin;

        public PluginExecutorAdapter(NodePlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getNodeType() {
            return plugin.getNodeType();
        }

        @Override
        public NodeExecuteResult execute(FlowNode node, FlowContext context) {
            Map<String, Object> contextMap = context.toMap();
            NodePluginResult pluginResult = plugin.execute(node, contextMap);
            
            // 自动将插件输出字段写入上下文
            if (pluginResult.isSuccess() && pluginResult.getData() != null) {
                autoMapPluginOutput(pluginResult.getData(), context);
            }
            
            return convertResult(pluginResult);
        }
        
        private void autoMapPluginOutput(Object data, FlowContext context) {
            String outputSchema = plugin.getOutputSchema();
            if (outputSchema == null || outputSchema.isEmpty()) return;
            try {
                JSONObject schema = JSON.parseObject(outputSchema);
                if (schema == null) return;
                JSONArray fields = schema.getJSONArray("fields");
                if (fields == null || fields.isEmpty()) return;
                
                // data 可能是 Map 或 JSONObject，统一转为 JSONObject
                JSONObject dataJson;
                if (data instanceof JSONObject) {
                    dataJson = (JSONObject) data;
                } else {
                    dataJson = JSON.parseObject(JSON.toJSONString(data));
                }
                
                for (int i = 0; i < fields.size(); i++) {
                    JSONObject field = fields.getJSONObject(i);
                    if (field == null) continue;
                    String fieldName = field.getString("name");
                    if (fieldName == null || fieldName.isEmpty()) continue;
                    Object value = dataJson.get(fieldName);
                    if (value != null) {
                        context.set(fieldName, value);
                    }
                }
            } catch (Exception e) {
                log.warn("插件输出字段自动映射失败: nodeType={}", plugin.getNodeType(), e);
            }
        }

        private NodeExecuteResult convertResult(NodePluginResult pluginResult) {
            if (pluginResult.isSuccess()) {
                if (pluginResult.isWaiting()) {
                    return NodeExecuteResult.waiting(pluginResult.getNextExecuteTime());
                }
                return NodeExecuteResult.success(pluginResult.getData());
            } else {
                return NodeExecuteResult.fail(pluginResult.getErrorMsg());
            }
        }
    }
}
