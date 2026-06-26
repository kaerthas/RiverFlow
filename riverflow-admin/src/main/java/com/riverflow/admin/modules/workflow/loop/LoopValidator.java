package com.riverflow.admin.modules.workflow.loop;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 循环结构校验器
 */
@Slf4j
@Component
public class LoopValidator {

    public void validate(List<FlowNode> nodes, List<FlowEdge> edges) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        Map<String, FlowNode> nodeMap = new HashMap<>();
        for (FlowNode node : nodes) {
            nodeMap.put(node.getNodeId(), node);
        }

        // 1. 收集所有循环节点
        List<FlowNode> startLoopNodes = new ArrayList<>();
        List<FlowNode> endLoopNodes = new ArrayList<>();
        for (FlowNode node : nodes) {
            if (LoopUtils.isStartLoopNode(node)) {
                startLoopNodes.add(node);
            } else if (LoopUtils.isEndLoopNode(node)) {
                endLoopNodes.add(node);
            }
        }

        // 2. 检查结束节点配置
        for (FlowNode endNode : endLoopNodes) {
            validateEndLoopNode(endNode, startLoopNodes, nodeMap);
        }

        // 3. 检查每个循环开始节点都有对应的结束节点
        for (FlowNode startNode : startLoopNodes) {
            String endNodeId = LoopUtils.resolveEndLoopNodeId(
                    startNode.getNodeId(), startNode.getNodeType(), nodes, edges);
            if (endNodeId == null) {
                throw new com.riverflow.common.exception.BusinessException(
                        "循环节点 [" + startNode.getNodeName() + "] 缺少对应的结束节点");
            }
        }

        // 4. 检查循环体边界不交叉
        validateNoCrossing(startLoopNodes, endLoopNodes, nodes, edges);

        // 5. 检查 foreach / while 特有配置
        for (FlowNode startNode : startLoopNodes) {
            validateStartLoopNode(startNode);
        }
    }

    private void validateEndLoopNode(FlowNode endNode, List<FlowNode> startLoopNodes,
                                     Map<String, FlowNode> nodeMap) {
        EndLoopConfig config = LoopUtils.parseEndConfig(endNode.getConfigJson());
        String loopNodeId = config.getLoopNodeId();
        if (loopNodeId == null || loopNodeId.trim().isEmpty()) {
            throw new com.riverflow.common.exception.BusinessException(
                    "循环结束节点 [" + endNode.getNodeName() + "] 未配置关联的循环节点");
        }
        FlowNode startNode = nodeMap.get(loopNodeId);
        if (startNode == null) {
            throw new com.riverflow.common.exception.BusinessException(
                    "循环结束节点 [" + endNode.getNodeName() + "] 关联的循环节点不存在: " + loopNodeId);
        }
        String expectedEndType = LoopUtils.getEndLoopType(startNode.getNodeType());
        if (!endNode.getNodeType().equals(expectedEndType)) {
            throw new com.riverflow.common.exception.BusinessException(
                    "循环结束节点 [" + endNode.getNodeName() + "] 与循环开始节点类型不匹配");
        }
    }

    private void validateStartLoopNode(FlowNode startNode) {
        String configJson = startNode.getConfigJson();
        if (configJson == null || configJson.trim().isEmpty()) {
            throw new com.riverflow.common.exception.BusinessException(
                    "循环节点 [" + startNode.getNodeName() + "] 缺少配置");
        }
        JSONObject config = JSON.parseObject(configJson);

        Integer maxIterations = config.getInteger("maxIterations");
        if (maxIterations == null) {
            maxIterations = 100;
        }
        if (maxIterations < 1 || maxIterations > 10000) {
            throw new com.riverflow.common.exception.BusinessException(
                    "循环节点 [" + startNode.getNodeName() + "] 最大迭代数必须在 1~10000 之间");
        }

        if (FlowNodeTypeEnum.FOREACH.getCode().equals(startNode.getNodeType())) {
            String sourceExpr = config.getString("sourceExpr");
            if (sourceExpr == null || sourceExpr.trim().isEmpty()) {
                throw new com.riverflow.common.exception.BusinessException(
                        "foreach 节点 [" + startNode.getNodeName() + "] 缺少循环源表达式");
            }
        }

        if (FlowNodeTypeEnum.WHILE.getCode().equals(startNode.getNodeType())) {
            String conditionExpr = config.getString("conditionExpr");
            if (conditionExpr == null || conditionExpr.trim().isEmpty()) {
                throw new com.riverflow.common.exception.BusinessException(
                        "while 节点 [" + startNode.getNodeName() + "] 缺少条件表达式");
            }
        }
    }

    /**
     * 检查循环体边界不交叉：不允许循环 A 的循环体包含循环 B 的开始节点但不包含其结束节点。
     */
    private void validateNoCrossing(List<FlowNode> startLoopNodes, List<FlowNode> endLoopNodes,
                                    List<FlowNode> nodes, List<FlowEdge> edges) {
        Map<String, String> loopPairs = new HashMap<>();
        for (FlowNode startNode : startLoopNodes) {
            String endNodeId = LoopUtils.resolveEndLoopNodeId(
                    startNode.getNodeId(), startNode.getNodeType(), nodes, edges);
            if (endNodeId != null) {
                loopPairs.put(startNode.getNodeId(), endNodeId);
            }
        }

        for (FlowNode startNode : startLoopNodes) {
            String endNodeId = loopPairs.get(startNode.getNodeId());
            if (endNodeId == null) {
                continue;
            }
            Set<String> bodyNodeIds = LoopUtils.findLoopBodyNodes(
                    startNode.getNodeId(), endNodeId, edges, nodes);

            for (FlowNode otherStart : startLoopNodes) {
                if (otherStart.getNodeId().equals(startNode.getNodeId())) {
                    continue;
                }
                String otherEndId = loopPairs.get(otherStart.getNodeId());
                if (otherEndId == null) {
                    continue;
                }
                boolean containsOtherStart = bodyNodeIds.contains(otherStart.getNodeId());
                boolean containsOtherEnd = bodyNodeIds.contains(otherEndId);
                if (containsOtherStart != containsOtherEnd) {
                    throw new com.riverflow.common.exception.BusinessException(
                            "循环边界交叉：[" + startNode.getNodeName() + "] 的循环体包含 ["
                                    + otherStart.getNodeName() + "] 但不包含其结束节点");
                }
            }
        }
    }

    /**
     * 检查节点是否可以从 fromNodeId 到达 toNodeId
     */
    public static boolean isReachable(String fromNodeId, String toNodeId, List<FlowEdge> edges) {
        if (fromNodeId == null || toNodeId == null || edges == null) {
            return false;
        }
        if (fromNodeId.equals(toNodeId)) {
            return true;
        }
        Queue<String> queue = new ArrayDeque<>();
        queue.add(fromNodeId);
        Set<String> visited = new HashSet<>();
        visited.add(fromNodeId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (FlowEdge edge : edges) {
                if (current.equals(edge.getSourceNode())) {
                    String target = edge.getTargetNode();
                    if (toNodeId.equals(target)) {
                        return true;
                    }
                    if (visited.add(target)) {
                        queue.add(target);
                    }
                }
            }
        }
        return false;
    }
}
