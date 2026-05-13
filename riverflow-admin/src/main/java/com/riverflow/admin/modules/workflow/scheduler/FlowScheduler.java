package com.riverflow.admin.modules.workflow.scheduler;

import com.riverflow.admin.modules.workflow.engine.FlowEngine;
import com.riverflow.admin.service.FlowDefinitionService;
import com.riverflow.admin.service.FlowEdgeService;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.admin.service.FlowNodeService;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowInstanceStatusEnum;
import com.riverflow.api.enums.FlowTaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程调度器
 * 定时扫描待执行的流程任务并驱动执行
 */
@Slf4j
@Component
public class FlowScheduler {

    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowNodeService flowNodeService;
    @Autowired
    private FlowEdgeService flowEdgeService;
    @Autowired
    private FlowEngine flowEngine;

    /**
     * 每10秒扫描一次待执行任务
     */
    @Scheduled(fixedRate = 10000)
    public void scanPendingTasks() {
        try {
            List<FlowTask> pendingTasks = flowTaskService.getPendingTasks(LocalDateTime.now());
            if (pendingTasks == null || pendingTasks.isEmpty()) {
                return;
            }

            log.debug("扫描到 {} 个待执行任务", pendingTasks.size());

            for (FlowTask task : pendingTasks) {
                try {
                    FlowInstance instance = flowInstanceService.getById(task.getInstanceId());
                    if (instance == null) continue;
                    if (!FlowInstanceStatusEnum.RUNNING.getCode().equals(instance.getStatus())) {
                        continue;
                    }

                    List<FlowNode> nodes = flowNodeService.getNodesByFlowId(instance.getFlowId());
                    List<FlowEdge> edges = flowEdgeService.getEdgesByFlowId(instance.getFlowId());

                    FlowNode currentNode = nodes.stream()
                            .filter(n -> n.getNodeId().equals(instance.getCurrentNodeId()))
                            .findFirst().orElse(null);

                    if (currentNode == null) continue;

                    flowEngine.executeNode(instance, currentNode, edges, nodes);

                } catch (Exception e) {
                    log.error("调度执行任务失败: taskId={}", task.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("扫描待执行任务异常", e);
        }
    }
}
