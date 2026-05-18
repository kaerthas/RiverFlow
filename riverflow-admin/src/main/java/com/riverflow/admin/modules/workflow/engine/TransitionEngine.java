package com.riverflow.admin.modules.workflow.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.admin.service.FlowLogService;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowLog;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowInstanceStatusEnum;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import com.riverflow.api.enums.FlowTaskStatusEnum;
import com.riverflow.common.util.SpelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程流转引擎
 */
@Slf4j
@Component
public class TransitionEngine {

    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowLogService flowLogService;

    public void transition(FlowInstance instance, FlowNode currentNode,
                           List<FlowEdge> edges, List<FlowNode> nodes,
                           FlowContext context, NodeExecuteResult result) {

        String currentNodeId = currentNode.getNodeId();

        List<FlowEdge> outEdges = edges.stream()
                .filter(e -> e.getSourceNode().equals(currentNodeId))
                .sorted(Comparator.comparingInt(FlowEdge::getPriority))
                .collect(Collectors.toList());

        if (outEdges.isEmpty()) {
            if (FlowNodeTypeEnum.END.getCode().equals(currentNode.getNodeType())) {
                completeInstance(instance);
            } else {
                log.warn("[流程实例:{}] 节点 {} 没有出边，流程挂起", instance.getId(), currentNode.getNodeName());
                instance.setStatus(FlowInstanceStatusEnum.SUSPENDED.getCode());
                flowInstanceService.updateById(instance);
            }
            return;
        }

        FlowEdge matchedEdge = null;
        for (FlowEdge edge : outEdges) {
            if (matchEdgeCondition(edge, context, result)) {
                matchedEdge = edge;
                break;
            }
        }

        if (matchedEdge == null) {
            log.error("[流程实例:{}] 没有匹配到任何边，流程挂起", instance.getId());
            instance.setStatus(FlowInstanceStatusEnum.SUSPENDED.getCode());
            flowInstanceService.updateById(instance);
            return;
        }

        String targetNodeId = matchedEdge.getTargetNode();
        FlowNode targetNode = nodes.stream()
                .filter(n -> n.getNodeId().equals(targetNodeId))
                .findFirst().orElse(null);

        if (targetNode == null) {
            log.error("[流程实例:{}] 目标节点 {} 不存在，流程挂起", instance.getId(), targetNodeId);
            instance.setStatus(FlowInstanceStatusEnum.SUSPENDED.getCode());
            flowInstanceService.updateById(instance);
            return;
        }

        log.info("[流程实例:{}] 从 [{}] 流转到 [{}] (conditionType={})",
                instance.getId(), currentNode.getNodeName(), targetNode.getNodeName(), matchedEdge.getConditionType());

        // 更新实例当前节点
        instance.setCurrentNodeId(targetNodeId);
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);

        // 如果是结束节点，直接完成，不创建 pending 任务
        if (FlowNodeTypeEnum.END.getCode().equals(targetNode.getNodeType())) {
            completeInstance(instance);
            saveLog(instance.getId(), null, targetNodeId, "transition",
                    String.format("从 [%s] 流转到 [%s]（流程结束）", currentNode.getNodeName(), targetNode.getNodeName()));
            return;
        }

        // 创建新任务
        FlowTask newTask = new FlowTask();
        newTask.setInstanceId(instance.getId());
        newTask.setNodeId(targetNode.getNodeId());
        newTask.setNodeName(targetNode.getNodeName());
        newTask.setNodeType(targetNode.getNodeType());
        newTask.setStatus(FlowTaskStatusEnum.PENDING.getCode());
        newTask.setCreateTime(LocalDateTime.now());
        flowTaskService.save(newTask);

        // 记录流转日志
        saveLog(instance.getId(), newTask.getId(), targetNodeId, "transition",
                String.format("从 [%s] 流转到 [%s]", currentNode.getNodeName(), targetNode.getNodeName()));
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
            
            return SpelUtil.evaluateBoolean(expression, spelContext);
        }

        return false;
    }

    private void completeInstance(FlowInstance instance) {
        log.info("[流程实例:{}] 流程执行完成", instance.getId());
        instance.setStatus(FlowInstanceStatusEnum.COMPLETED.getCode());
        instance.setEndTime(LocalDateTime.now());
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);
        saveLog(instance.getId(), null, instance.getCurrentNodeId(), "transition", "流程执行完成");
    }

    private void saveLog(Long instanceId, Long taskId, String nodeId, String logType, String content) {
        try {
            FlowLog log = new FlowLog();
            log.setInstanceId(instanceId);
            log.setTaskId(taskId);
            log.setNodeId(nodeId);
            log.setLogType(logType);
            log.setLogContent(content);
            log.setCreateTime(LocalDateTime.now());
            flowLogService.save(log);
        } catch (Exception e) {
            log.error("保存流转日志失败", e);
        }
    }
}
