package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程任务实例（节点执行实例）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_flow_task")
public class FlowTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流程实例ID
     */
    private Long instanceId;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型
     */
    private String nodeType;

    /**
     * 状态：pending/running/success/fail/waiting/skipped
     */
    private String status;

    /**
     * 执行前上下文快照
     */
    private String inputContext;

    /**
     * 执行后上下文快照
     */
    private String outputContext;

    /**
     * 执行结果
     */
    private String resultJson;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 执行次数（含重试）
     */
    private Integer executeCount;

    /**
     * 下次执行时间（定时场景）
     */
    private LocalDateTime nextExecuteTime;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
