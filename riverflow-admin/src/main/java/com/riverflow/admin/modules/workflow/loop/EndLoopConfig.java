package com.riverflow.admin.modules.workflow.loop;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 循环结束节点配置
 */
@Data
public class EndLoopConfig {

    private String loopNodeId;

    private String aggregateExpr;

    private List<Map<String, String>> outputMapping;
}
