package com.riverflow.admin.modules.workflow.validate.rules;

import com.riverflow.admin.modules.workflow.validate.FlowValidationResult;
import com.riverflow.admin.modules.workflow.validate.FlowValidationRule;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.enums.FlowNodeTypeEnum;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 图结构校验规则
 *
 * <p>校验节点与边的图结构合法性：节点唯一性、开始/结束节点、边指向、死节点、
 * 连通性、可达性、循环结构等。
 */
@Component
public class GraphStructureValidationRule implements FlowValidationRule {

    @Override
    public FlowValidationResult validate(List<FlowNode> nodes, List<FlowEdge> edges) {
        FlowValidationResult result = new FlowValidationResult();
        result.setValid(true);

        if (nodes == null || nodes.isEmpty()) {
            result.addError("流程节点为空");
            return result;
        }

        // 1. 节点 ID 唯一性
        Map<String, FlowNode> nodeMap = new HashMap<>();
        Set<String> duplicatedIds = new HashSet<>();
        for (FlowNode node : nodes) {
            String nodeId = node.getNodeId();
            if (nodeId == null || nodeId.trim().isEmpty()) {
                result.addError("存在节点未设置 nodeId");
                continue;
            }
            if (nodeMap.containsKey(nodeId)) {
                duplicatedIds.add(nodeId);
            } else {
                nodeMap.put(nodeId, node);
            }
        }
        if (!duplicatedIds.isEmpty()) {
            result.addError("节点 ID 重复: " + String.join(", ", duplicatedIds));
        }

        // 2. 节点类型合法
        for (FlowNode node : nodes) {
            String nodeType = node.getNodeType();
            if (nodeType == null || nodeType.trim().isEmpty()) {
                result.addError("节点 [" + node.getNodeId() + "] 未设置类型");
            } else if (FlowNodeTypeEnum.fromCode(nodeType) == null) {
                result.addError("节点 [" + node.getNodeName() + "] 类型 [" + nodeType + "] 不合法");
            }
        }

        // 3. 开始/结束节点
        List<FlowNode> startNodes = new ArrayList<>();
        List<FlowNode> endNodes = new ArrayList<>();
        for (FlowNode node : nodes) {
            if (FlowNodeTypeEnum.START.getCode().equals(node.getNodeType())) {
                startNodes.add(node);
            } else if (FlowNodeTypeEnum.END.getCode().equals(node.getNodeType())) {
                endNodes.add(node);
            }
        }
        if (startNodes.isEmpty()) {
            result.addError("流程缺少开始节点");
        }
        if (startNodes.size() > 1) {
            result.addError("流程只能有一个开始节点，当前有 " + startNodes.size() + " 个");
        }
        if (endNodes.isEmpty()) {
            result.addError("流程缺少结束节点");
        }

        // 4. 边指向的节点必须存在
        if (edges != null) {
            for (FlowEdge edge : edges) {
                String source = edge.getSourceNode();
                String target = edge.getTargetNode();
                if (source == null || source.trim().isEmpty()) {
                    result.addError("存在边未设置源节点");
                } else if (!nodeMap.containsKey(source)) {
                    result.addError("边引用了不存在的源节点: " + source);
                }
                if (target == null || target.trim().isEmpty()) {
                    result.addError("存在边未设置目标节点");
                } else if (!nodeMap.containsKey(target)) {
                    result.addError("边引用了不存在的目标节点: " + target);
                }
            }
        }

        // 5. 入边/出边基本检查
        Map<String, List<FlowEdge>> outEdges = new HashMap<>();
        Map<String, List<FlowEdge>> inEdges = new HashMap<>();
        if (edges != null) {
            for (FlowEdge edge : edges) {
                if (edge.getSourceNode() != null) {
                    outEdges.computeIfAbsent(edge.getSourceNode(), k -> new ArrayList<>()).add(edge);
                }
                if (edge.getTargetNode() != null) {
                    inEdges.computeIfAbsent(edge.getTargetNode(), k -> new ArrayList<>()).add(edge);
                }
            }
        }

        for (FlowNode node : nodes) {
            String nodeId = node.getNodeId();
            String nodeType = node.getNodeType();
            List<FlowEdge> outs = outEdges.getOrDefault(nodeId, Collections.emptyList());
            List<FlowEdge> ins = inEdges.getOrDefault(nodeId, Collections.emptyList());

            // 结束节点不应有出边
            if (FlowNodeTypeEnum.END.getCode().equals(nodeType) && !outs.isEmpty()) {
                result.addError("结束节点 [" + node.getNodeName() + "] 不应有出边");
            }
            // 开始节点不应有入边
            if (FlowNodeTypeEnum.START.getCode().equals(nodeType) && !ins.isEmpty()) {
                result.addError("开始节点 [" + node.getNodeName() + "] 不应有入边");
            }
            // 非结束节点、非循环结束节点必须有出边
            if (!FlowNodeTypeEnum.END.getCode().equals(nodeType)
                    && !FlowNodeTypeEnum.END_FOREACH.getCode().equals(nodeType)
                    && !FlowNodeTypeEnum.END_WHILE.getCode().equals(nodeType)
                    && outs.isEmpty()) {
                result.addError("节点 [" + node.getNodeName() + "] 没有出边，可能成为死节点");
            }
            // 非开始节点必须有入边
            if (!FlowNodeTypeEnum.START.getCode().equals(nodeType) && ins.isEmpty()) {
                result.addError("节点 [" + node.getNodeName() + "] 没有入边，无法被到达");
            }
        }

        // 6. 从 start 可达的节点集合
        if (!startNodes.isEmpty()) {
            Set<String> reachableFromStart = collectReachableNodes(startNodes.get(0).getNodeId(), edges);
            for (FlowNode node : nodes) {
                if (!reachableFromStart.contains(node.getNodeId())) {
                    result.addError("节点 [" + node.getNodeName() + "] 从开始节点无法到达");
                }
            }
        }

        // 7. 是否能到达某个结束节点
        if (edges != null && !endNodes.isEmpty()) {
            Set<String> canReachEnd = collectCanReachEnd(endNodes, edges);
            for (FlowNode node : nodes) {
                if (!canReachEnd.contains(node.getNodeId())) {
                    result.addError("节点 [" + node.getNodeName() + "] 无法到达任何结束节点");
                }
            }
        }

        // 8. 普通节点自环检查（循环节点允许自环回跳）
        if (edges != null) {
            for (FlowEdge edge : edges) {
                if (edge.getSourceNode() == null || edge.getTargetNode() == null) {
                    continue;
                }
                if (edge.getSourceNode().equals(edge.getTargetNode())) {
                    FlowNode node = nodeMap.get(edge.getSourceNode());
                    if (node != null && !isLoopNode(node)) {
                        result.addError("普通节点 [" + node.getNodeName() + "] 存在自环");
                    }
                }
            }
        }

        return result;
    }

    private boolean isLoopNode(FlowNode node) {
        String type = node.getNodeType();
        return FlowNodeTypeEnum.FOREACH.getCode().equals(type)
                || FlowNodeTypeEnum.WHILE.getCode().equals(type)
                || FlowNodeTypeEnum.END_FOREACH.getCode().equals(type)
                || FlowNodeTypeEnum.END_WHILE.getCode().equals(type);
    }

    /**
     * 从起始节点出发，广度搜索可到达的所有节点
     */
    private Set<String> collectReachableNodes(String startNodeId, List<FlowEdge> edges) {
        Set<String> reachable = new HashSet<>();
        if (startNodeId == null || edges == null) {
            return reachable;
        }
        Queue<String> queue = new LinkedList<>();
        queue.add(startNodeId);
        reachable.add(startNodeId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (FlowEdge edge : edges) {
                if (current.equals(edge.getSourceNode())) {
                    String target = edge.getTargetNode();
                    if (target != null && reachable.add(target)) {
                        queue.add(target);
                    }
                }
            }
        }
        return reachable;
    }

    /**
     * 收集能到达任意结束节点的节点集合
     */
    private Set<String> collectCanReachEnd(List<FlowNode> endNodes, List<FlowEdge> edges) {
        Set<String> canReachEnd = new HashSet<>();
        if (edges == null) {
            return canReachEnd;
        }
        // 反向图 BFS
        Map<String, List<String>> reverseGraph = new HashMap<>();
        for (FlowEdge edge : edges) {
            if (edge.getTargetNode() != null && edge.getSourceNode() != null) {
                reverseGraph.computeIfAbsent(edge.getTargetNode(), k -> new ArrayList<>()).add(edge.getSourceNode());
            }
        }
        Queue<String> queue = new LinkedList<>();
        for (FlowNode endNode : endNodes) {
            canReachEnd.add(endNode.getNodeId());
            queue.add(endNode.getNodeId());
        }
        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<String> prevs = reverseGraph.getOrDefault(current, Collections.emptyList());
            for (String prev : prevs) {
                if (canReachEnd.add(prev)) {
                    queue.add(prev);
                }
            }
        }
        return canReachEnd;
    }
}
