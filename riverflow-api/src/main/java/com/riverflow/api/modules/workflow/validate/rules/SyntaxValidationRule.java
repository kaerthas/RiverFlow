package com.riverflow.api.modules.workflow.validate.rules;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import com.riverflow.api.modules.workflow.validate.FlowValidationResult;
import com.riverflow.api.modules.workflow.validate.FlowValidationRule;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语法校验规则
 *
 * <p>校验节点配置、条件表达式、脚本、Cron 等语法合法性。
 */
@Component
public class SyntaxValidationRule implements FlowValidationRule {

    private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();
    private static final GroovyShell GROOVY_SHELL = new GroovyShell();
    private static final Pattern SPEL_PATTERN = Pattern.compile("#\\{([^}]+)}");

    @Override
    public FlowValidationResult validate(List<FlowNode> nodes, List<FlowEdge> edges) {
        FlowValidationResult result = new FlowValidationResult();
        result.setValid(true);

        if (nodes == null || nodes.isEmpty()) {
            return result;
        }

        for (FlowNode node : nodes) {
            String nodeType = node.getNodeType();
            String configJson = node.getConfigJson();

            // 1. 配置 JSON 必须可解析
            if (configJson != null && !configJson.trim().isEmpty()) {
                try {
                    JSON.parseObject(configJson);
                } catch (Exception e) {
                    result.addError("节点 [" + getNodeName(node) + "] 配置 JSON 解析失败: " + e.getMessage());
                }
            }

            // 2. 映射 JSON 格式校验
            validateMappingJson(node, "inputMapping", result);
            validateMappingJson(node, "outputMapping", result);

            // 3. 节点类型级语法校验
            if (FlowNodeTypeEnum.SCRIPT.getCode().equals(nodeType)) {
                validateScriptNode(node, result);
            } else if (FlowNodeTypeEnum.CONDITION.getCode().equals(nodeType)) {
                validateConditionNode(node, result);
            } else if (FlowNodeTypeEnum.TIMER.getCode().equals(nodeType)) {
                validateTimerNode(node, result);
            } else if (FlowNodeTypeEnum.WHILE.getCode().equals(nodeType)) {
                validateWhileNode(node, result);
            }
        }

        // 4. 条件边语法校验
        if (edges != null) {
            for (FlowEdge edge : edges) {
                if ("custom".equals(edge.getConditionType())) {
                    String expr = edge.getConditionExpression();
                    if (expr == null || expr.trim().isEmpty()) {
                        result.addError("条件边 [" + edge.getSourceNode() + " -> " + edge.getTargetNode()
                                + "] 缺少条件表达式");
                    } else {
                        validateSpelExpression(expr, "条件边 [" + edge.getSourceNode() + " -> "
                                + edge.getTargetNode() + "]", result);
                    }
                }
            }
        }

        return result;
    }

    private void validateMappingJson(FlowNode node, String field, FlowValidationResult result) {
        String mapping = null;
        if ("inputMapping".equals(field)) {
            mapping = node.getInputMapping();
        } else if ("outputMapping".equals(field)) {
            mapping = node.getOutputMapping();
        }
        if (mapping == null || mapping.trim().isEmpty()) {
            return;
        }
        try {
            JSON.parseArray(mapping);
        } catch (Exception e) {
            result.addError("节点 [" + getNodeName(node) + "] " + field + " 格式非法: " + e.getMessage());
        }
    }

    private void validateScriptNode(FlowNode node, FlowValidationResult result) {
        if (node.getConfigJson() == null || node.getConfigJson().trim().isEmpty()) {
            result.addError("脚本节点 [" + getNodeName(node) + "] 缺少配置");
            return;
        }
        JSONObject config = JSON.parseObject(node.getConfigJson());
        String script = config.getString("script");
        if (script == null || script.isEmpty()) {
            script = config.getString("scriptContent");
        }
        if (script == null || script.trim().isEmpty()) {
            result.addError("脚本节点 [" + getNodeName(node) + "] 缺少脚本内容");
            return;
        }
        try {
            // 仅做语法解析，不执行
            Script parsed = GROOVY_SHELL.parse(script);
            if (parsed == null) {
                result.addError("脚本节点 [" + getNodeName(node) + "] Groovy 脚本解析失败");
            }
        } catch (Exception e) {
            result.addError("脚本节点 [" + getNodeName(node) + "] Groovy 语法错误: " + e.getMessage());
        }
    }

    private void validateConditionNode(FlowNode node, FlowValidationResult result) {
        if (node.getConfigJson() == null || node.getConfigJson().trim().isEmpty()) {
            result.addError("条件节点 [" + getNodeName(node) + "] 缺少配置");
            return;
        }
        JSONObject config = JSON.parseObject(node.getConfigJson());
        String expression = config.getString("conditionExpression");
        if (expression == null || expression.trim().isEmpty()) {
            result.addError("条件节点 [" + getNodeName(node) + "] 缺少条件表达式");
            return;
        }
        validateSpelExpression(expression, "条件节点 [" + getNodeName(node) + "]", result);
    }

    private void validateTimerNode(FlowNode node, FlowValidationResult result) {
        if (node.getConfigJson() == null || node.getConfigJson().trim().isEmpty()) {
            result.addError("定时节点 [" + getNodeName(node) + "] 缺少配置");
            return;
        }
        JSONObject config = JSON.parseObject(node.getConfigJson());
        String cron = config.getString("cronExpression");
        if (cron == null || cron.trim().isEmpty()) {
            cron = node.getCronExpression();
        }
        if (cron == null || cron.trim().isEmpty()) {
            result.addError("定时节点 [" + getNodeName(node) + "] 缺少 Cron 表达式");
            return;
        }
        try {
            CronExpression.parse(cron);
        } catch (Exception e) {
            result.addError("定时节点 [" + getNodeName(node) + "] Cron 表达式非法: " + e.getMessage());
        }
    }

    private void validateWhileNode(FlowNode node, FlowValidationResult result) {
        if (node.getConfigJson() == null || node.getConfigJson().trim().isEmpty()) {
            result.addError("while 节点 [" + getNodeName(node) + "] 缺少配置");
            return;
        }
        JSONObject config = JSON.parseObject(node.getConfigJson());
        String conditionExpr = config.getString("conditionExpr");
        if (conditionExpr == null || conditionExpr.trim().isEmpty()) {
            result.addError("while 节点 [" + getNodeName(node) + "] 缺少条件表达式");
            return;
        }
        validateSpelExpression(conditionExpr, "while 节点 [" + getNodeName(node) + "]", result);
    }

    private void validateSpelExpression(String expression, String context, FlowValidationResult result) {
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }
        String expr = expression.trim();
        // 支持 #{...} 包裹
        if (expr.startsWith("#{")) {
            if (expr.endsWith("}")) {
                expr = expr.substring(2, expr.length() - 1);
            } else {
                result.addError(context + " SpEL 表达式格式错误，缺少右括号: " + expression);
                return;
            }
        }
        // 提取内部所有 #{...} 占位符（针对混合表达式）
        Matcher matcher = SPEL_PATTERN.matcher(expression);
        while (matcher.find()) {
            String inner = matcher.group(1);
            validateSpelExpression(inner, context + " 中的占位符", result);
        }
        if (expr.contains("#{")) {
            // 已经由占位符逻辑处理，跳过整体解析
            return;
        }
        try {
            SPEL_PARSER.parseExpression(expr);
        } catch (Exception e) {
            result.addError(context + " SpEL 表达式语法错误: " + e.getMessage());
        }
    }

    private String getNodeName(FlowNode node) {
        if (node.getNodeName() != null && !node.getNodeName().isEmpty()) {
            return node.getNodeName();
        }
        return node.getNodeId();
    }
}
