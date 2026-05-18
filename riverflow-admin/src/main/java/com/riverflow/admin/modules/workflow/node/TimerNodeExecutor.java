package com.riverflow.admin.modules.workflow.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.api.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 定时等待节点执行器
 * 支持延迟等待（delaySeconds）或指定时间（fixedTime）
 */
@Slf4j
@Component
public class TimerNodeExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "timer";
    }

    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] 执行定时节点: {}", context.getInstanceId(), node.getNodeName());

        String configJson = node.getConfigJson();
        if (configJson == null || configJson.trim().isEmpty()) {
            return NodeExecuteResult.fail("定时节点缺少配置");
        }

        JSONObject config = JSON.parseObject(configJson);
        Long delaySeconds = config.getLong("delaySeconds");
        String fixedTime = config.getString("fixedTime");

        // 从上下文中读取已记录的定时目标时间（防止每次重新计算导致无限延迟）
        Long targetTime = null;
        Object targetObj = context.get("_timerTargetTime_" + node.getNodeId());
        if (targetObj instanceof Number) {
            targetTime = ((Number) targetObj).longValue();
        } else if (targetObj != null) {
            try {
                targetTime = Long.valueOf(targetObj.toString());
            } catch (NumberFormatException ignored) {}
        }

        if (targetTime != null && targetTime > 0) {
            // 已经设置过目标时间，检查是否到达
            if (System.currentTimeMillis() >= targetTime) {
                log.info("[流程实例:{}] 定时节点等待完成，继续执行", context.getInstanceId());
                return NodeExecuteResult.success(new JSONObject());
            } else {
                log.info("[流程实例:{}] 定时节点继续等待，目标时间: {}", context.getInstanceId(), targetTime);
                return NodeExecuteResult.waiting(targetTime);
            }
        }

        long nextTime;
        if (delaySeconds != null && delaySeconds > 0) {
            nextTime = System.currentTimeMillis() + delaySeconds * 1000;
            log.info("[流程实例:{}] 定时节点延迟 {} 秒执行，目标时间: {}", context.getInstanceId(), delaySeconds, nextTime);
        } else if (fixedTime != null && !fixedTime.isEmpty()) {
            LocalDateTime dateTime = LocalDateTime.parse(fixedTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            nextTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            log.info("[流程实例:{}] 定时节点等待到 {}，目标时间: {}", context.getInstanceId(), fixedTime, nextTime);
        } else {
            return NodeExecuteResult.fail("定时节点必须配置 delaySeconds 或 fixedTime");
        }

        // 将目标时间记录到上下文中，确保下次执行不会重新计算
        context.set("_timerTargetTime_" + node.getNodeId(), nextTime);
        return NodeExecuteResult.waiting(nextTime);
    }
}
