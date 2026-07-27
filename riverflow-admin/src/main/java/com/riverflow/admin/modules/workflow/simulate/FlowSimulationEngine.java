package com.riverflow.admin.modules.workflow.simulate;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.api.modules.workflow.simulate.FlowSimulationResult;
import com.riverflow.admin.infra.groovy.GroovySandboxExecutor;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import com.riverflow.common.util.SpelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 流程沙箱模拟执行引擎
 *
 * <p>在不写数据库、不调用真实外部服务的前提下，模拟执行流程图，验证节点流转可行性。
 * 对 DB/API 节点返回 mock 数据，脚本节点在 Groovy 沙箱中执行，条件/开始/结束节点按真实逻辑计算。
 */
@Slf4j
@Component
public class FlowSimulationEngine {

    @Autowired
    private GroovySandboxExecutor groovySandboxExecutor;

    public void setGroovySandboxExecutor(GroovySandboxExecutor groovySandboxExecutor) {
        this.groovySandboxExecutor = groovySandboxExecutor;
    }

    private static final Pattern SPEL_PATTERN = Pattern.compile("#\\{([^}]+)}");

    /**
     * 模拟执行流程
     *
     * @param nodes   流程节点
     * @param edges   流程边
     * @param initial 初始上下文变量
     * @return 模拟执行结果
     */
    public FlowSimulationResult simulate(List<FlowNode> nodes, List<FlowEdge> edges, Map<String, Object> initial) {
        FlowSimulationResult result = new FlowSimulationResult();
        result.setSuccess(true);

        if (nodes == null || nodes.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMsg("流程节点为空");
            return result;
        }

        Map<String, FlowNode> nodeMap = nodes.stream()
                .filter(n -> n.getNodeId() != null)
                .collect(Collectors.toMap(FlowNode::getNodeId, n -> n, (a, b) -> a));

        FlowNode startNode = nodes.stream()
                .filter(n -> FlowNodeTypeEnum.START.getCode().equals(n.getNodeType()))
                .findFirst().orElse(null);
        if (startNode == null) {
            result.setSuccess(false);
            result.setErrorMsg("流程缺少开始节点");
            return result;
        }

        FlowContext context = new FlowContext();
        context.setGlobal("_instanceId", 0L);
        context.setGlobal("_businessKey", "simulate");
        context.setGlobal("_flowCode", "simulate");
        context.setGlobal("_currentTime", System.currentTimeMillis());
        if (initial != null) {
            initial.forEach(context::setGlobal);
        }

        FlowNode currentNode = startNode;
        int stepGuard = 0;
        final int maxSteps = 1000;

        while (currentNode != null && stepGuard++ < maxSteps) {
            FlowSimulationResult.SimulationStep step = executeNode(currentNode, context);
            result.addStep(step);

            if (!step.isSuccess()) {
                result.setSuccess(false);
                result.setErrorMsg("节点 [" + currentNode.getNodeName() + "] 模拟执行失败: " + step.getErrorMsg());
                break;
            }

            if (FlowNodeTypeEnum.END.getCode().equals(currentNode.getNodeType())) {
                result.setReachedEnd(true);
                break;
            }

            // 查找下一节点
            FlowNode nextNode = findNextNode(currentNode, nodes, nodeMap, edges, context, step);
            if (nextNode == null) {
                // 没有下一节点但不是结束节点，视为挂起
                if (!FlowNodeTypeEnum.END.getCode().equals(currentNode.getNodeType())) {
                    result.setSuccess(false);
                    result.setErrorMsg("节点 [" + currentNode.getNodeName() + "] 之后无有效流转");
                }
                break;
            }
            currentNode = nextNode;
        }

        if (stepGuard >= maxSteps && currentNode != null) {
            result.setSuccess(false);
            result.setErrorMsg("模拟执行超过最大步数 " + maxSteps + "，可能存在无限循环");
        }

        result.setFinalContext(context.getGlobals());
        return result;
    }

    private FlowSimulationResult.SimulationStep executeNode(FlowNode node, FlowContext context) {
        long start = System.currentTimeMillis();
        FlowSimulationResult.SimulationStep step = new FlowSimulationResult.SimulationStep();
        step.setNodeId(node.getNodeId());
        step.setNodeName(node.getNodeName());
        step.setNodeType(node.getNodeType());
        step.setInputContext(new HashMap<>(context.getGlobals()));

        try {
            NodeExecuteResult executeResult;
            String nodeType = node.getNodeType();

            switch (nodeType) {
                case "start":
                    executeResult = executeStartNode(node, context);
                    break;
                case "end":
                    executeResult = executeEndNode(node, context);
                    break;
                case "condition":
                    executeResult = executeConditionNode(node, context);
                    break;
                case "script":
                    executeResult = executeScriptNode(node, context);
                    break;
                case "db":
                    executeResult = executeDbNode(node, context);
                    break;
                case "api":
                    executeResult = executeApiNode(node, context);
                    break;
                case "timer":
                    executeResult = executeTimerNode(node, context);
                    break;
                case "foreach":
                case "while":
                    // 模拟执行中，循环节点做一次进入/退出标记，不展开循环体
                    executeResult = executeLoopNode(node, context);
                    break;
                default:
                    executeResult = NodeExecuteResult.fail("模拟执行暂不支持的节点类型: " + nodeType);
            }

            step.setSuccess(executeResult != null && executeResult.isSuccess());
            step.setResultData(executeResult != null ? executeResult.getData() : null);
            if (executeResult != null && !executeResult.isSuccess() && StringUtils.hasText(executeResult.getErrorMsg())) {
                step.setErrorMsg(executeResult.getErrorMsg());
            }
        } catch (Exception e) {
            log.warn("模拟执行节点 [{}] 异常", node.getNodeName(), e);
            step.setSuccess(false);
            step.setErrorMsg(e.getMessage());
        }

        step.setOutputContext(new HashMap<>(context.getGlobals()));
        step.setExecuteTimeMillis(System.currentTimeMillis() - start);
        return step;
    }

    private NodeExecuteResult executeStartNode(FlowNode node, FlowContext context) {
        log.debug("[模拟执行] 开始节点: {}", node.getNodeName());
        return NodeExecuteResult.success();
    }

    private NodeExecuteResult executeEndNode(FlowNode node, FlowContext context) {
        log.debug("[模拟执行] 结束节点: {}", node.getNodeName());
        String inputMapping = node.getInputMapping();
        if (StringUtils.hasText(inputMapping)) {
            try {
                JSONArray mappings = JSON.parseArray(inputMapping);
                for (int i = 0; i < mappings.size(); i++) {
                    JSONObject map = mappings.getJSONObject(i);
                    String source = map.getString("source");
                    String target = map.getString("target");
                    Object value = context.getByPath(source);
                    if (value != null) {
                        context.set(target, value);
                    }
                }
            } catch (Exception e) {
                log.warn("[模拟执行] 结束节点输入映射解析失败: {}", e.getMessage());
            }
        }
        return NodeExecuteResult.success();
    }

    private NodeExecuteResult executeConditionNode(FlowNode node, FlowContext context) {
        log.debug("[模拟执行] 条件节点: {}", node.getNodeName());
        String configJson = node.getConfigJson();
        if (!StringUtils.hasText(configJson)) {
            return NodeExecuteResult.fail("条件节点缺少配置");
        }
        JSONObject config = JSON.parseObject(configJson);
        String expression = config.getString("conditionExpression");
        if (!StringUtils.hasText(expression)) {
            return NodeExecuteResult.fail("条件节点缺少表达式");
        }
        try {
            Map<String, Object> spelContext = new HashMap<>();
            spelContext.put("context", context.toMap());
            boolean result = SpelUtil.evaluateBoolean(expression, spelContext);
            JSONObject data = new JSONObject();
            data.put("conditionResult", result);
            data.put("expression", expression);
            return NodeExecuteResult.success(data);
        } catch (Exception e) {
            return NodeExecuteResult.fail("条件表达式求值失败: " + e.getMessage());
        }
    }

    private NodeExecuteResult executeScriptNode(FlowNode node, FlowContext context) {
        log.debug("[模拟执行] 脚本节点: {}", node.getNodeName());
        String configJson = node.getConfigJson();
        if (!StringUtils.hasText(configJson)) {
            return NodeExecuteResult.fail("脚本节点缺少配置");
        }
        JSONObject config = JSON.parseObject(configJson);
        String script = config.getString("script");
        if (!StringUtils.hasText(script)) {
            script = config.getString("scriptContent");
        }
        if (!StringUtils.hasText(script)) {
            return NodeExecuteResult.fail("脚本节点缺少脚本内容");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("ctx", context.toMap());
        variables.put("context", context);
        variables.put("instanceId", 0L);
        applyInputMapping(node, context, variables);

        try {
            Object result = groovySandboxExecutor.execute(script, variables);
            JSONObject jsonResult;
            if (result instanceof JSONObject) {
                jsonResult = (JSONObject) result;
            } else if (result instanceof Map) {
                jsonResult = new JSONObject((Map<?, ?>) result);
            } else {
                jsonResult = new JSONObject();
                jsonResult.put("result", result);
            }
            applyOutputMapping(node, context, jsonResult);
            return NodeExecuteResult.success(jsonResult);
        } catch (Exception e) {
            return NodeExecuteResult.fail("脚本执行失败: " + e.getMessage());
        }
    }

    private NodeExecuteResult executeDbNode(FlowNode node, FlowContext context) {
        log.debug("[模拟执行] 数据库节点: {}", node.getNodeName());
        String configJson = node.getConfigJson();
        if (!StringUtils.hasText(configJson)) {
            return NodeExecuteResult.fail("数据库节点缺少配置");
        }
        JSONObject config = JSON.parseObject(configJson);
        String sql = config.getString("sql");
        String operation = config.getString("operation");
        String resultVarName = config.getString("resultVarName");

        if (!StringUtils.hasText(sql)) {
            return NodeExecuteResult.fail("数据库节点未配置 SQL");
        }

        // 解析占位符，从 inputMapping 构建参数
        Map<String, Object> paramMap = buildInputParamMap(node, context);
        Matcher matcher = SPEL_PATTERN.matcher(sql);
        StringBuffer sb = new StringBuffer();
        List<Object> args = new ArrayList<>();
        while (matcher.find()) {
            String expression = matcher.group(1).trim();
            if (!paramMap.containsKey(expression)) {
                return NodeExecuteResult.fail("SQL 占位符 [#{" + expression + "}] 未在输入映射中配置");
            }
            Object value = paramMap.get(expression);
            if (value instanceof Map || value instanceof List) {
                value = JSON.toJSONString(value);
            }
            args.add(value);
            matcher.appendReplacement(sb, "?");
        }
        matcher.appendTail(sb);

        // mock 返回数据
        JSONObject resultData = new JSONObject();
        resultData.put("operation", operation);
        resultData.put("sql", sb.toString());
        resultData.put("mock", true);
        if ("select".equalsIgnoreCase(operation)) {
            resultData.put("data", List.of(new HashMap<>()));
            resultData.put("count", 1);
        } else {
            resultData.put("affectedRows", 1);
        }

        if (StringUtils.hasText(resultVarName)) {
            context.set(resultVarName, resultData.get("data"));
        }
        applyOutputMapping(node, context, resultData);
        return NodeExecuteResult.success(resultData);
    }

    private NodeExecuteResult executeApiNode(FlowNode node, FlowContext context) {
        log.debug("[模拟执行] 接口节点: {}", node.getNodeName());
        String configJson = node.getConfigJson();
        if (!StringUtils.hasText(configJson)) {
            return NodeExecuteResult.fail("接口节点缺少配置");
        }
        JSONObject config = JSON.parseObject(configJson);
        String apiCode = config.getString("apiCode");
        if (!StringUtils.hasText(apiCode)) {
            return NodeExecuteResult.fail("接口节点未配置 API 编码");
        }

        // mock 返回数据
        JSONObject mockBody = new JSONObject();
        mockBody.put("mock", true);
        mockBody.put("apiCode", apiCode);

        JSONObject result = new JSONObject();
        result.put("statusCode", 200);
        result.put("body", JSON.toJSONString(mockBody));
        result.put("mock", true);

        applyOutputMapping(node, context, result);
        return NodeExecuteResult.success(result);
    }

    private NodeExecuteResult executeTimerNode(FlowNode node, FlowContext context) {
        log.debug("[模拟执行] 定时节点: {}", node.getNodeName());
        String configJson = node.getConfigJson();
        if (!StringUtils.hasText(configJson)) {
            return NodeExecuteResult.fail("定时节点缺少配置");
        }
        JSONObject config = JSON.parseObject(configJson);
        Long delaySeconds = config.getLong("delaySeconds");
        String cronExpression = config.getString("cronExpression");

        JSONObject data = new JSONObject();
        if (delaySeconds != null && delaySeconds > 0) {
            data.put("delaySeconds", delaySeconds);
            data.put("nextExecuteTime", System.currentTimeMillis() + delaySeconds * 1000);
        } else if (StringUtils.hasText(cronExpression)) {
            data.put("cronExpression", cronExpression);
        } else {
            return NodeExecuteResult.fail("定时节点未配置 delaySeconds 或 cronExpression");
        }
        data.put("skipped", true);
        return NodeExecuteResult.success(data);
    }

    private NodeExecuteResult executeLoopNode(FlowNode node, FlowContext context) {
        log.debug("[模拟执行] 循环节点: {}", node.getNodeName());
        // 模拟执行中循环节点只标记进入，并尝试沿默认边继续，不展开循环体
        JSONObject data = new JSONObject();
        data.put("simulated", true);
        data.put("note", "模拟执行不展开循环体，仅验证入口可到达性");
        return NodeExecuteResult.success(data);
    }

    private FlowNode findNextNode(FlowNode currentNode, List<FlowNode> nodes, Map<String, FlowNode> nodeMap,
                                    List<FlowEdge> edges, FlowContext context,
                                    FlowSimulationResult.SimulationStep step) {
        String currentNodeId = currentNode.getNodeId();
        List<FlowEdge> outEdges = edges == null ? Collections.emptyList() : edges.stream()
                .filter(e -> currentNodeId.equals(e.getSourceNode()))
                .sorted(Comparator.comparingInt(e -> e.getPriority() == null ? 0 : e.getPriority()))
                .collect(Collectors.toList());

        if (outEdges.isEmpty()) {
            return null;
        }

        for (FlowEdge edge : outEdges) {
            if (matchEdgeCondition(edge, context, step)) {
                return nodeMap.get(edge.getTargetNode());
            }
        }
        return null;
    }

    private boolean matchEdgeCondition(FlowEdge edge, FlowContext context, FlowSimulationResult.SimulationStep step) {
        String conditionType = edge.getConditionType();
        if (conditionType == null || "default".equals(conditionType)) {
            return true;
        }
        if ("success".equals(conditionType)) {
            return step.isSuccess();
        }
        if ("fail".equals(conditionType)) {
            return !step.isSuccess();
        }
        if ("custom".equals(conditionType)) {
            String expression = edge.getConditionExpression();
            if (!StringUtils.hasText(expression)) {
                return true;
            }
            try {
                Map<String, Object> spelContext = new HashMap<>();
                spelContext.put("context", context.toMap());
                spelContext.put("lastResult", step.getResultData());
                return SpelUtil.evaluateBoolean(expression, spelContext);
            } catch (Exception e) {
                log.warn("[模拟执行] 条件边求值失败: {}", expression, e);
                return false;
            }
        }
        return false;
    }

    private Map<String, Object> buildInputParamMap(FlowNode node, FlowContext context) {
        Map<String, Object> paramMap = new HashMap<>();
        String inputMapping = node.getInputMapping();
        if (!StringUtils.hasText(inputMapping)) {
            return paramMap;
        }
        try {
            JSONArray mappings = JSON.parseArray(inputMapping);
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject map = mappings.getJSONObject(i);
                String target = map.getString("target");
                String source = map.getString("source");
                String type = map.getString("type");
                if (!StringUtils.hasText(target)) {
                    continue;
                }
                Object value = "const".equals(type) ? source : context.getByPath(source);
                paramMap.put(target, value);
            }
        } catch (Exception e) {
            log.warn("[模拟执行] 输入映射解析失败: {}", e.getMessage());
        }
        return paramMap;
    }

    private void applyInputMapping(FlowNode node, FlowContext context, Map<String, Object> variables) {
        String inputMapping = node.getInputMapping();
        if (!StringUtils.hasText(inputMapping)) {
            return;
        }
        try {
            JSONArray mappings = JSON.parseArray(inputMapping);
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject map = mappings.getJSONObject(i);
                String source = map.getString("source");
                String target = map.getString("target");
                String type = map.getString("type");
                if (!StringUtils.hasText(target)) {
                    continue;
                }
                Object value = "const".equals(type) ? source : context.getByPath(source);
                if (value != null) {
                    variables.put(target, value);
                }
            }
        } catch (Exception e) {
            log.warn("[模拟执行] 输入映射解析失败: {}", e.getMessage());
        }
    }

    private void applyOutputMapping(FlowNode node, FlowContext context, JSONObject resultData) {
        String outputMapping = node.getOutputMapping();
        if (!StringUtils.hasText(outputMapping)) {
            return;
        }
        try {
            JSONArray mappings = JSON.parseArray(outputMapping);
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject map = mappings.getJSONObject(i);
                String source = map.getString("source");
                String target = map.getString("target");
                if (!StringUtils.hasText(source) || !StringUtils.hasText(target)) {
                    continue;
                }
                Object value = resolveResultPath(resultData, source);
                if (value != null) {
                    String key = target.startsWith("context.") ? target.substring(8) : target;
                    context.set(key, value);
                }
            }
        } catch (Exception e) {
            log.warn("[模拟执行] 输出映射解析失败: {}", e.getMessage());
        }
    }

    private Object resolveResultPath(JSONObject result, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.startsWith("result.")) {
            trimmed = trimmed.substring(7);
        }
        return result.getByPath(trimmed);
    }
}
