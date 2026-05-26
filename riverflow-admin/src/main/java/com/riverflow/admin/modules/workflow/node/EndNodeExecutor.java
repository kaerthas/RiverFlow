package com.riverflow.admin.modules.workflow.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.api.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 结束节点执行器
 * 支持输入映射，将上下文变量提取到返回结果中（用于同步流程输出）
 */
@Slf4j
@Component
public class EndNodeExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "end";
    }

    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] 执行结束节点: {}", context.getInstanceId(), node.getNodeName());

        // 解析输入映射，将上下文变量提取到返回结果中
        String inputMapping = node.getInputMapping();
        if (inputMapping != null && !inputMapping.isEmpty()) {
            JSONArray mappings = JSON.parseArray(inputMapping);
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject map = mappings.getJSONObject(i);
                String source = map.getString("source"); // context.xxx
                String target = map.getString("target"); // 返回结果字段名
                Object value = context.getByPath(source);
                if (value != null) {
                    // 写入上下文顶层，使同步流程返回结果中可直接访问
                    context.set(target, value);
                }
            }
        }

        return NodeExecuteResult.success();
    }
}
