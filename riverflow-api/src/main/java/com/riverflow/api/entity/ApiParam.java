package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口参数定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_api_param")
public class ApiParam extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属接口ID
     */
    @TableField("api_id")
    private Long apiId;

    /**
     * 参数类型：header/query/body/response
     */
    @TableField("param_type")
    private String paramType;

    /**
     * 父参数ID，支持嵌套
     */
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

    /**
     * 排序号
     */
    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 前端客户端ID，用于保存时建立嵌套关系（不存数据库）
     */
    @TableField(exist = false)
    private String clientId;

    /**
     * 父参数的客户端ID（不存数据库）
     */
    @TableField(exist = false)
    private String parentClientId;
}
