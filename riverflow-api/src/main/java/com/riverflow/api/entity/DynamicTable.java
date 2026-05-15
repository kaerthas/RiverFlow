package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 动态表定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_dynamic_table")
public class DynamicTable extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 表编码（英文，用于生成真实表名）
     */
    private String tableCode;

    /**
     * 表名称（中文）
     */
    private String tableName;

    /**
     * 所属数据源ID，0表示主库
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dsId;

    /**
     * 数据源名称（非持久化，列表展示用）
     */
    @TableField(exist = false)
    private String dsName;

    /**
     * 字段数量（非持久化，列表展示用）
     */
    @TableField(exist = false)
    private Integer columnCount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态：0-停用，1-启用
     */
    private Integer status;
}
