package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程执行日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_flow_log")
public class FlowLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流程实例ID
     */
    private Long instanceId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 日志类型：start/execute/condition/transition/error
     */
    private String logType;

    /**
     * 日志内容
     */
    private String logContent;
}
