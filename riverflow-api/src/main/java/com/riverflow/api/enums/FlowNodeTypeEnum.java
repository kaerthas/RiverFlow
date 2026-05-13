package com.riverflow.api.enums;

import lombok.Getter;

/**
 * 流程节点类型枚举
 */
@Getter
public enum FlowNodeTypeEnum {

    START("start", "开始节点", "circle"),
    END("end", "结束节点", "circle"),
    API("api", "接口调用", "cloud"),
    DB("db", "数据库操作", "database"),
    SCRIPT("script", "脚本处理", "code"),
    CONDITION("condition", "条件判断", "diamond"),
    TIMER("timer", "定时等待", "clock");

    private final String code;
    private final String desc;
    private final String icon;

    FlowNodeTypeEnum(String code, String desc, String icon) {
        this.code = code;
        this.desc = desc;
        this.icon = icon;
    }

    public static FlowNodeTypeEnum fromCode(String code) {
        for (FlowNodeTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
