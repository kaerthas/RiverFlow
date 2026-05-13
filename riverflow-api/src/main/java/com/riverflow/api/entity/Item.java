package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 政务服务事项
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_item")
public class Item extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 事项编码
     */
    private String itemCode;

    /**
     * 事项名称
     */
    private String itemName;

    /**
     * 区划代码
     */
    private String regionCode;

    /**
     * 区划名称
     */
    private String regionName;

    /**
     * 国家基本编码
     */
    private String catalogCode;

    /**
     * 国家实施编码
     */
    private String taskCode;

    /**
     * 国家业务办理项编码
     */
    private String taskHandleItem;

    /**
     * 办理对象：0-个人，1-法人
     */
    private Integer serviceObj;

    /**
     * 绑定流程定义ID
     */
    private Long flowId;

    /**
     * 状态：0-停用，1-启用
     */
    private Integer status;
}
