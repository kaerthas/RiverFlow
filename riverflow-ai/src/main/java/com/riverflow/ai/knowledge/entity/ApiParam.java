package com.riverflow.ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 接口参数定义（知识库来源，简化字段）
 */
@Data
@TableName("wf_api_param")
public class ApiParam {

    private Long id;

    @TableField("api_id")
    private Long apiId;

    /**
     * 参数类型：header/query/body/response
     */
    @TableField("param_type")
    private String paramType;

    @TableField("parent_id")
    private Long parentId;

    /**
     * 参数键
     */
    @TableField("param_key")
    private String paramKey;

    /**
     * 参数名称
     */
    @TableField("param_name")
    private String paramName;

    /**
     * 数据类型：string/int/long/double/boolean/object/array
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 是否必填：0-否 1-是
     */
    @TableField("is_required")
    private Integer isRequired;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    @TableField("sort_no")
    private Integer sortNo;
}
