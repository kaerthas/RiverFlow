package com.riverflow.admin.modules.workflow.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.common.util.SpelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 条件判断节点执行器
 * 解析 configJson 中的 conditionExpression，使用 SpEL 求值
 */
@Slf4j
@Component
public class ConditionNodeExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "condition";
    }

//    public static void main(String[] args) {
//        Map<String, Object> testMap = new HashMap<>();
//        Map<String, Object> spelContext = new HashMap<>();
//
//        testMap.put("wsbz", "Y");
//        spelContext.put("context", testMap);
//        boolean testResult = SpelUtil.evaluateBoolean("#{context.wsbz == 'Y'}", spelContext);
//        log.info("测试结果: {}", testResult);
//    }
    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] 执行条件节点: {}", context.getInstanceId(), node.getNodeName());

        String configJson = node.getConfigJson();
        if (configJson == null || configJson.trim().isEmpty()) {
            log.warn("条件节点缺少配置: nodeId={}", node.getNodeId());
            return NodeExecuteResult.fail("条件节点缺少配置");
        }

        JSONObject config = JSON.parseObject(configJson);
        String expression = config.getString("conditionExpression");

        if (expression == null || expression.trim().isEmpty()) {
            log.warn("条件节点缺少表达式: nodeId={}", node.getNodeId());
            return NodeExecuteResult.fail("条件节点缺少表达式");
        }
        Map<String, Object> spelContext = new HashMap<>();
        spelContext.put("context", context.toMap());

        try {
            boolean result = SpelUtil.evaluateBoolean(expression, spelContext);
            log.info("[流程实例:{}] 条件求值: expression=[{}], result={}",
                    context.getInstanceId(), expression, result);

            // 将结果放入上下文，供边路由使用
            JSONObject resultData = new JSONObject();
            resultData.put("conditionResult", result);
            resultData.put("expression", expression);

            return NodeExecuteResult.success(resultData);
        } catch (Exception e) {
            log.error("[流程实例:{}] 条件表达式求值失败: {}", context.getInstanceId(), expression, e);
            return NodeExecuteResult.fail("条件表达式求值失败: " + e.getMessage());
        }
    }
}
