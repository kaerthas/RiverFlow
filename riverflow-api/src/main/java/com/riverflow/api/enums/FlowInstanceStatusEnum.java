package com.riverflow.api.enums;

import lombok.Getter;

/**
 * 流程实例状态枚举
 */
@Getter
public enum FlowInstanceStatusEnum {

    RUNNING("running", "运行中"),
    COMPLETED("completed", "已完成"),
    SUSPENDED("suspended", "已挂起"),
    FAILED("failed", "失败"),
    TERMINATED("terminated", "已终止");

    private final String code;
    private final String desc;

    FlowInstanceStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
