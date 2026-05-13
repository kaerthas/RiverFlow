package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口脚本库
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_api_script")
public class ApiScript extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 脚本编码
     */
    private String scriptCode;

    /**
     * 脚本名称
     */
    private String scriptName;

    /**
     * 脚本类型：format-格式化 header-请求头 result-结果处理 condition-条件判断
     */
    private String scriptType;

    /**
     * Groovy脚本内容
     */
    private String scriptContent;

    /**
     * 脚本入参定义JSON
     */
    private String params;

    /**
     * 状态：0-停用，1-启用
     */
    private Integer status;
}
