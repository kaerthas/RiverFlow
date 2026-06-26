package com.riverflow.admin.modules.workflow.loop;

import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.admin.modules.workflow.node.NodeExecutor;
import com.riverflow.admin.modules.workflow.node.NodeExecutorFactory;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 循环体执行引擎
 * 在单次迭代内串行执行循环体子图。
 */
@Slf4j
@Component
public class LoopBodyEngine {

    private final ObjectProvider<NodeExecutorFactory> nodeExecutorFactoryProvider;

    public LoopBodyEngine(ObjectProvider<NodeExecutorFactory> nodeExecutorFactoryProvider) {
        this.nodeExecutorFactoryProvider = nodeExecutorFactoryProvider;
    }

    private NodeExecutorFactory getNodeExecutorFactory() {
        return nodeExecutorFactoryProvider.getIfAvailable();
    }

    /**
     * 同步执行循环体子图
     *
     * @param context       迭代上下文
     * @param entryNode     循环体入口节点
     * @param endLoopNodeId 循环结束节点 ID
     * @param nodes         所有节点
     * @param edges         所有边
     * @return 循环体执行结束时的上下文
     */
    public FlowContext executeBodySync(FlowContext context, FlowNode entryNode,
                                       String endLoopNodeId, List<FlowNode> nodes,
                                       List<FlowEdge> edges) {
        FlowNode currentNode = entryNode;
        int guard = 0;
        final int maxGuard = 10000;

        while (currentNode != null && guard++ < maxGuard) {
            if (endLoopNodeId.equals(currentNode.getNodeId())) {
                break;
            }

            NodeExecutor executor = getNodeExecutorFactory().getExecutor(currentNode.getNodeType());
            NodeExecuteResult result = executor.execute(currentNode, context);

            if (!result.isSuccess()) {
                throw new RuntimeException("循环体节点执行失败 [" + currentNode.getNodeName() + "]: " + result.getErrorMsg());
            }
            if (result.isWaiting()) {
                throw new RuntimeException("循环体不支持等待状态节点: " + currentNode.getNodeName());
            }

            if (result.getData() != null) {
                context.set("nodeResult_" + currentNode.getNodeId(), result.getData());
            }

            // 循环体内部的循环回跳（理论上不应出现，但做兜底）
            if (result.getNextEntryNodeId() != null && !result.getNextEntryNodeId().isEmpty()) {
                FlowNode nextNode = LoopUtils.findNode(nodes, result.getNextEntryNodeId());
                if (nextNode == null) {
                    throw new RuntimeException("循环体跳转目标节点不存在: " + result.getNextEntryNodeId());
                }
                currentNode = nextNode;
                continue;
            }

            currentNode = findNextNode(currentNode, edges, nodes, context, result);
        }

        return context;
    }

    private FlowNode findNextNode(FlowNode currentNode, List<FlowEdge> edges,
                                  List<FlowNode> nodes, FlowContext context,
                                  NodeExecuteResult result) {
        String currentNodeId = currentNode.getNodeId();

        List<FlowEdge> outEdges = edges.stream()
                .filter(e -> e.getSourceNode().equals(currentNodeId))
                .sorted(Comparator.comparingInt(FlowEdge::getPriority))
                .collect(Collectors.toList());

        if (outEdges.isEmpty()) {
            return null;
        }

        FlowEdge matchedEdge = null;
        for (FlowEdge edge : outEdges) {
            if (matchEdgeCondition(edge, context, result)) {
                matchedEdge = edge;
                break;
            }
        }

        if (matchedEdge == null) {
            return null;
        }

        String targetNodeId = matchedEdge.getTargetNode();
        return nodes.stream()
                .filter(n -> n.getNodeId().equals(targetNodeId))
                .findFirst()
                .orElse(null);
    }

    private boolean matchEdgeCondition(FlowEdge edge, FlowContext context, NodeExecuteResult result) {
        String conditionType = edge.getConditionType();

        if ("default".equals(conditionType)) {
            return true;
        }
        if ("success".equals(conditionType)) {
            return result.isSuccess() && !result.isWaiting();
        }
        if ("fail".equals(conditionType)) {
            return !result.isSuccess();
        }
        if ("custom".equals(conditionType)) {
            String expression = edge.getConditionExpression();
            if (expression == null || expression.isEmpty()) {
                return true;
            }
            context.set("_lastResult", result.getData());
            Map<String, Object> spelContext = new HashMap<>();
            spelContext.put("context", context.toMap());
            return com.riverflow.common.util.SpelUtil.evaluateBoolean(expression, spelContext);
        }
        return false;
    }

    /**
     * 判断节点是否是结束节点（用于循环体执行边界）
     */
    public static boolean isLoopBoundaryEnd(FlowNode node) {
        if (node == null) {
            return false;
        }
        return FlowNodeTypeEnum.END_FOREACH.getCode().equals(node.getNodeType())
                || FlowNodeTypeEnum.END_WHILE.getCode().equals(node.getNodeType())
                || FlowNodeTypeEnum.END.getCode().equals(node.getNodeType());
    }
}
