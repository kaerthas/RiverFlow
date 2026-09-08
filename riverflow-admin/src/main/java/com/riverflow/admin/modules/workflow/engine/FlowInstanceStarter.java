package com.riverflow.admin.modules.workflow.engine;

import com.alibaba.fastjson2.JSON;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.admin.service.FlowNodeService;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.FlowDefinition;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import com.riverflow.api.enums.FlowTaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程实例启动器
 * 统一手动触发 / 定时触发 / 事件触发的实例启动链路：
 * 创建实例 -> 注入触发来源与默认入参 -> 创建开始节点的待执行任务（由 FlowScheduler 扫描驱动）
 */
@Slf4j
@Component
public class FlowInstanceStarter {

    @Autowired
    private FlowEngine flowEngine;
    @Autowired
    private FlowNodeService flowNodeService;
    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowTaskService flowTaskService;

    public FlowInstance start(FlowDefinition def, String businessKey, String itemCode, String triggerType) {
        FlowInstance instance = flowEngine.startInstance(
                def.getId(), def.getFlowCode(), def.getVersion(), businessKey, itemCode);
        enrichContext(def, instance, triggerType);
        createStartNodeTask(def.getId(), instance);
        return instance;
    }

    /**
     * 注入触发来源标记与流程默认入参
     */
    @SuppressWarnings("unchecked")
    private void enrichContext(FlowDefinition def, FlowInstance instance, String triggerType) {
        try {
            Map<String, Object> contextMap;
            String existingContext = instance.getContextJson();
            if (existingContext != null && !existingContext.isEmpty()) {
                contextMap = JSON.parseObject(existingContext, Map.class);
            } else {
                contextMap = new HashMap<>();
            }
            contextMap.put("_triggerType", triggerType);
            if (def.getInputParams() != null && !def.getInputParams().isEmpty()) {
                Map<String, Object> defaultVars = JSON.parseObject(def.getInputParams(), Map.class);
                if (defaultVars != null) {
                    contextMap.putAll(defaultVars);
                }
            }
            instance.setContextJson(JSON.toJSONString(contextMap));
            flowInstanceService.updateById(instance);
        } catch (Exception e) {
            log.warn("注入流程默认入参失败: flowId={}", def.getId(), e);
        }
    }

    /**
     * 找到开始节点，创建首个待执行任务
     */
    private void createStartNodeTask(Long flowId, FlowInstance instance) {
        List<FlowNode> nodes = flowNodeService.getNodesByFlowId(flowId);
        FlowNode startNode = nodes.stream()
                .filter(n -> FlowNodeTypeEnum.START.getCode().equals(n.getNodeType()))
                .findFirst().orElse(null);
        if (startNode == null) {
            log.warn("流程定义缺少开始节点，实例无首个任务: flowId={}, instanceId={}", flowId, instance.getId());
            return;
        }
        FlowTask task = new FlowTask();
        task.setInstanceId(instance.getId());
        task.setNodeId(startNode.getNodeId());
        task.setNodeName(startNode.getNodeName());
        task.setNodeType(startNode.getNodeType());
        task.setStatus(FlowTaskStatusEnum.PENDING.getCode());
        task.setCreateTime(LocalDateTime.now());
        flowTaskService.save(task);

        instance.setCurrentNodeId(startNode.getNodeId());
        flowInstanceService.updateById(instance);
    }
}
