/*
 *    Copyright (c) 2018-2025, yunho All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the yunho.io developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: yunho
 */

package com.inspur.workinfo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 事项与流程关联关系表
 *
 * @author yunho code generator
 * @date 2024-01-19 10:30:14
 */
@Data
@TableName("XT_APPROVE_ITEM_ITEMFLOW")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "事项与流程关联关系表")
public class XtApproveItemItemflow extends Model<XtApproveItemItemflow> {
private static final long serialVersionUID = 1L;

    /**
     * 事项编码
     */
    @TableId
    @ApiModelProperty(value="事项编码")
    private String itemSxbm;
    /**
     * 事项流程模型id
     */
    @ApiModelProperty(value="事项流程模型id")
    private String itemflowSxbm;
    /**
     * 行政区划代码
     */
    @ApiModelProperty(value="行政区划代码")
    private String regionCode;
    /**
     * 行政区划名称
     */
    @ApiModelProperty(value="行政区划名称")
    private String regionName;
    }
