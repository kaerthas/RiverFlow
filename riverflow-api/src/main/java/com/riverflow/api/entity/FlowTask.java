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
     * 乐观锁版本号（分布式调度认领任务时使用）
     */
    private Integer version;

    /**
     * 实际执行该任务的节点标识（IP/主机名）
     */
    private String executeNode;

    /**
     * 任务被认领/开始执行的时间
     */
    private LocalDateTime executeTime;

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
     * 所属循环节点ID
     */
    private String loopNodeId;

    /**
     * 循环迭代下标
     */
    private Integer iterationIndex;

    /**
     * 是否循环体内部任务
     */
    private Integer isLoopInternal;

    /**
     * 任务类型：NODE / LOOP_ITERATION / LOOP_AGGREGATE
     */
    private String taskType;

    /**
     * 并行循环批次号
     */
    private String batchNo;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
