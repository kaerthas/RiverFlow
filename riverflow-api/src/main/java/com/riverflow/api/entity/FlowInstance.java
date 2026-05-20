package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程实例
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_flow_instance")
public class FlowInstance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流程定义ID
     */
    private Long flowId;

    /**
     * 流程编码
     */
    private String flowCode;

    /**
     * 流程版本号（实例启动时的版本快照）
     */
    private Integer version;

    /**
     * 业务主键（如办件流水号）
     */
    private String businessKey;

    /**
     * 状态：running/completed/suspended/failed/terminated
     */
    private String status;

    /**
     * 当前节点ID
     */
    private String currentNodeId;

    /**
     * 流程上下文JSON
     */
    private String contextJson;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
