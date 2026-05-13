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

        long nextTime;
        if (delaySeconds != null && delaySeconds > 0) {
            nextTime = System.currentTimeMillis() + delaySeconds * 1000;
            log.info("[流程实例:{}] 定时节点延迟 {} 秒执行", context.getInstanceId(), delaySeconds);
        } else if (fixedTime != null && !fixedTime.isEmpty()) {
            LocalDateTime dateTime = LocalDateTime.parse(fixedTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            nextTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            log.info("[流程实例:{}] 定时节点等待到 {}", context.getInstanceId(), fixedTime);
        } else {
            return NodeExecuteResult.fail("定时节点必须配置 delaySeconds 或 fixedTime");
        }

        return NodeExecuteResult.waiting(nextTime);
    }
}
