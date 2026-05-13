package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 动态表字段定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_dynamic_table_column")
public class DynamicTableColumn extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属表ID
     */
    private Long tableId;

    /**
     * 字段编码
     */
    private String columnCode;

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 数据类型：varchar/int/bigint/datetime/text/decimal等
     */
    private String dataType;

    /**
     * 长度
     */
    private Integer length;

    /**
     * 小数位
     */
    private Integer decimalScale;

    /**
     * 是否主键：0-否，1-是
     */
    private Integer isPk;

    /**
     * 是否必填：0-否，1-是
     */
    private Integer isRequired;

    /**
     * 是否索引：0-否，1-是
     */
    private Integer isIndex;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 排序号
     */
    private Integer sortNo;
}
