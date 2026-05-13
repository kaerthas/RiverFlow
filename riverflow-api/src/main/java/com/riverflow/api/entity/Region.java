package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 行政区划
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_region")
public class Region extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 区划代码
     */
    private String regionCode;

    /**
     * 区划名称
     */
    private String regionName;

    /**
     * 父级代码
     */
    private String parentCode;

    /**
     * 层级：1-省，2-市，3-区县
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sortNo;
}
