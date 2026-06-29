package com.riverflow.admin.modules.workflow.loop;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 循环节点工具类
 */
@Slf4j
public class LoopUtils {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 解析循环节点配置
     */
    public static LoopConfig parseConfig(String configJson) {
        if (configJson == null || configJson.trim().isEmpty()) {
            return new LoopConfig();
        }
        try {
            return JSON.parseObject(configJson, LoopConfig.class);
        } catch (Exception e) {
            log.warn("解析循环节点配置失败: {}", configJson, e);
            return new LoopConfig();
        }
    }

    /**
     * 解析 end_loop 节点配置
     */
    public static EndLoopConfig parseEndConfig(String configJson) {
        if (configJson == null || configJson.trim().isEmpty()) {
            return new EndLoopConfig();
        }
        try {
            return JSON.parseObject(configJson, EndLoopConfig.class);
        } catch (Exception e) {
            log.warn("解析循环结束节点配置失败: {}", configJson, e);
            return new EndLoopConfig();
        }
    }

    /**
     * 求值 SpEL 表达式，返回对象
     */
    @SuppressWarnings("unchecked")
    public static Object evaluateExpression(String expression, FlowContext context) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }
        log.info("[LoopUtils] 开始求值表达式: {}", expression);

        String expr = expression.trim();
        if (expr.startsWith("#{")) {
            expr = expr.substring(2, expr.length() - 1);
        }
        try {
            StandardEvaluationContext evalContext = new StandardEvaluationContext();
            Map<String, Object> variables = context.toMap();
            evalContext.setRootObject(variables);
            variables.forEach(evalContext::setVariable);
            // 将 context 本身也注册为变量，供 #context['xxx'] 形式引用
            evalContext.setVariable("context", variables);

            Object result;
            if (expr.contains("context.")) {
                String processedExpr = expr.replaceAll("\\bcontext\\.(\\w+)", "#context['$1']");
                log.debug("表达式转换: {} -> {}", expr, processedExpr);
                result = PARSER.parseExpression(processedExpr).getValue(evalContext);
            } else {
                result = PARSER.parseExpression(expr).getValue(evalContext);
            }
            log.info("[LoopUtils] 表达式求值完成: {}, resultType={}", expression,
                    result != null ? result.getClass().getSimpleName() : "null");
            return result;
        } catch (Exception e) {
            log.error("SpEL 表达式求值失败: [{}]", expression, e);
            throw new RuntimeException("表达式求值失败: " + expression + ", " + e.getMessage());
        }
    }

    /**
     * 求值布尔表达式
     */
    public static boolean evaluateBoolean(String expression, FlowContext context) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        Object result = evaluateExpression(expression, context);
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return result != null;
    }

    /**
     * 求值集合并表达式（带上下文缓存）
     * <p>
     * 缓存转换后的大集合，避免 foreach 每次迭代都重复解析/转换。
     * 注意：该缓存假设 sourceExpr 对应的集合在循环生命周期内稳定；
     * 若业务需要在循环体中动态改变集合，应通过 clearEvaluationCache 清理。
     */
    @SuppressWarnings("unchecked")
    public static Collection<?> evaluateCollection(String expression, FlowContext context) {
        log.info("[LoopUtils] 开始求值集合表达式: {}", expression);
        // 缓存命中直接返回（避免重复转换和解析）
        Object cached = context.getEvaluationCache(expression);
        if (cached instanceof Collection) {
            log.info("[LoopUtils] 集合表达式命中缓存: {}, size={}", expression, ((Collection<?>) cached).size());
            return (Collection<?>) cached;
        }

        Object result = evaluateExpression(expression, context);
        Collection<?> collection;
        if (result == null) {
            collection = new ArrayList<>();
        } else if (result instanceof Collection) {
            collection = (Collection<Object>) result;
        } else if (result.getClass().isArray()) {
            List<Object> list = new ArrayList<>();
            for (Object item : (Object[]) result) {
                list.add(item);
            }
            collection = list;
        } else {
            // 单个对象包装为单元素集合
            List<Object> list = new ArrayList<>();
            list.add(result);
            collection = list;
        }
        context.putEvaluationCache(expression, collection);
        log.info("[LoopUtils] 集合表达式求值完成: {}, size={}", expression, collection.size());
        return collection;
    }

    /**
     * 解析循环体入口节点 ID
     */
    public static String resolveBodyEntryNodeId(String loopNodeId, List<FlowNode> nodes, List<FlowEdge> edges) {
        if (edges == null || edges.isEmpty()) {
            return null;
        }
        for (FlowEdge edge : edges) {
            if (loopNodeId.equals(edge.getSourceNode())) {
                String targetId = edge.getTargetNode();
                // 如果直接连到结束节点，说明是空循环体，返回结束节点（由执行器处理）
                FlowNode targetNode = findNode(nodes, targetId);
                if (targetNode != null && isEndLoopNode(targetNode)) {
                    return targetId;
                }
                return targetId;
            }
        }
        return null;
    }

    /**
     * 通过 BFS 查找循环体节点 ID 集合（兜底逻辑）
     */
    public static Set<String> findLoopBodyNodes(String loopNodeId, String endLoopNodeId,
                                                List<FlowEdge> edges, List<FlowNode> nodes) {
        Set<String> body = new LinkedHashSet<>();
        if (edges == null) {
            return body;
        }
        Queue<String> queue = new ArrayDeque<>();
        queue.add(loopNodeId);
        Set<String> visited = new HashSet<>();
        visited.add(loopNodeId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (FlowEdge edge : edges) {
                if (current.equals(edge.getSourceNode())) {
                    String target = edge.getTargetNode();
                    if (endLoopNodeId.equals(target)) {
                        continue;
                    }
                    if (visited.add(target)) {
                        body.add(target);
                        queue.add(target);
                    }
                }
            }
        }
        return body;
    }

    /**
     * 查找节点
     */
    public static FlowNode findNode(List<FlowNode> nodes, String nodeId) {
        if (nodes == null || nodeId == null) {
            return null;
        }
        return nodes.stream()
                .filter(n -> nodeId.equals(n.getNodeId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 查找边
     */
    public static List<FlowEdge> findOutEdges(List<FlowEdge> edges, String nodeId) {
        List<FlowEdge> result = new ArrayList<>();
        if (edges == null || nodeId == null) {
            return result;
        }
        for (FlowEdge edge : edges) {
            if (nodeId.equals(edge.getSourceNode())) {
                result.add(edge);
            }
        }
        return result;
    }

    /**
     * 判断节点是否是循环开始节点
     */
    public static boolean isStartLoopNode(FlowNode node) {
        if (node == null) {
            return false;
        }
        return FlowNodeTypeEnum.FOREACH.getCode().equals(node.getNodeType())
                || FlowNodeTypeEnum.WHILE.getCode().equals(node.getNodeType());
    }

    /**
     * 判断节点是否是循环结束节点
     */
    public static boolean isEndLoopNode(FlowNode node) {
        if (node == null) {
            return false;
        }
        return FlowNodeTypeEnum.END_FOREACH.getCode().equals(node.getNodeType())
                || FlowNodeTypeEnum.END_WHILE.getCode().equals(node.getNodeType());
    }

    /**
     * 获取循环开始节点类型对应的结束节点类型
     */
    public static String getEndLoopType(String startType) {
        if (FlowNodeTypeEnum.FOREACH.getCode().equals(startType)) {
            return FlowNodeTypeEnum.END_FOREACH.getCode();
        }
        if (FlowNodeTypeEnum.WHILE.getCode().equals(startType)) {
            return FlowNodeTypeEnum.END_WHILE.getCode();
        }
        return null;
    }

    /**
     * 获取所有循环开始节点的 ID 到结束节点 ID 的映射
     */
    public static Map<String, String> findLoopPairs(List<FlowNode> nodes, List<FlowEdge> edges) {
        Map<String, String> pairs = new LinkedHashMap<>();
        if (nodes == null || edges == null) {
            return pairs;
        }
        Map<String, FlowNode> nodeMap = new HashMap<>();
        for (FlowNode node : nodes) {
            nodeMap.put(node.getNodeId(), node);
        }

        for (FlowNode node : nodes) {
            if (isStartLoopNode(node)) {
                String endNodeId = resolveEndLoopNodeId(node.getNodeId(), node.getNodeType(), nodes, edges);
                if (endNodeId != null) {
                    pairs.put(node.getNodeId(), endNodeId);
                }
            }
        }
        return pairs;
    }

    /**
     * 解析循环开始节点对应的结束节点
     * 优先通过 configJson 中的配对关系，其次通过图可达性推导
     */
    public static String resolveEndLoopNodeId(String loopNodeId, String loopType,
                                              List<FlowNode> nodes, List<FlowEdge> edges) {
        String expectedEndType = getEndLoopType(loopType);
        if (expectedEndType == null) {
            return null;
        }

        // 1. 尝试从其他节点的 configJson 中找 loopNodeId 匹配
        for (FlowNode node : nodes) {
            if (expectedEndType.equals(node.getNodeType())) {
                EndLoopConfig config = parseEndConfig(node.getConfigJson());
                if (loopNodeId.equals(config.getLoopNodeId())) {
                    return node.getNodeId();
                }
            }
        }

        // 2. 兜底：BFS 找到第一个可达的结束节点
        Queue<String> queue = new ArrayDeque<>();
        queue.add(loopNodeId);
        Set<String> visited = new HashSet<>();
        visited.add(loopNodeId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (FlowEdge edge : findOutEdges(edges, current)) {
                String target = edge.getTargetNode();
                FlowNode targetNode = findNode(nodes, target);
                if (targetNode != null && expectedEndType.equals(targetNode.getNodeType())) {
                    return target;
                }
                if (visited.add(target)) {
                    queue.add(target);
                }
            }
        }
        return null;
    }

    /**
     * 构建节点 ID -> 节点 的映射
     */
    public static Map<String, FlowNode> buildNodeMap(List<FlowNode> nodes) {
        Map<String, FlowNode> map = new LinkedHashMap<>();
        if (nodes == null) {
            return map;
        }
        for (FlowNode node : nodes) {
            map.put(node.getNodeId(), node);
        }
        return map;
    }

    /**
     * 获取节点的出边目标节点
     */
    public static List<FlowNode> getOutNodes(FlowNode node, List<FlowEdge> edges, List<FlowNode> nodes) {
        List<FlowNode> result = new ArrayList<>();
        for (FlowEdge edge : findOutEdges(edges, node.getNodeId())) {
            FlowNode target = findNode(nodes, edge.getTargetNode());
            if (target != null) {
                result.add(target);
            }
        }
        return result;
    }

    /**
     * 安全地获取 JSONObject 中的字符串
     */
    public static String getString(JSONObject json, String key) {
        if (json == null) {
            return null;
        }
        return json.getString(key);
    }

    /**
     * 安全地获取 JSONObject 中的整数
     */
    public static Integer getInteger(JSONObject json, String key, Integer defaultValue) {
        if (json == null) {
            return defaultValue;
        }
        return json.getIntValue(key, defaultValue);
    }

    /**
     * 安全地获取 JSONObject 中的布尔值
     */
    public static Boolean getBoolean(JSONObject json, String key, Boolean defaultValue) {
        if (json == null) {
            return defaultValue;
        }
        return json.getBooleanValue(key, defaultValue);
    }
}
