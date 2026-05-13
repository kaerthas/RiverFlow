package com.riverflow.api.enums;

import lombok.Getter;

/**
 * 流程任务状态枚举
 */
@Getter
public enum FlowTaskStatusEnum {

    PENDING("pending", "待执行"),
    RUNNING("running", "执行中"),
    SUCCESS("success", "成功"),
    FAIL("fail", "失败"),
    WAITING("waiting", "等待中"),
    SKIPPED("skipped", "已跳过");

    private final String code;
    private final String desc;

    FlowTaskStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
