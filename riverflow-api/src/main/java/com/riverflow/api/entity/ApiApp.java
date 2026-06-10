package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口应用/目录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_api_app")
public class ApiApp extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 应用编码
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 应用图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
