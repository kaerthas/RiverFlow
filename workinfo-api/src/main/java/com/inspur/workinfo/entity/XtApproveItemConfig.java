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
 * 事项中控配置信息表
 *
 * @author yunho code generator
 * @date 2023-07-10 16:25:34
 */
@Data
@TableName("XT_APPROVE_ITEM_CONFIG")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "事项中控配置信息表")
public class XtApproveItemConfig extends Model<XtApproveItemConfig> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 事项编码
     */
    @ApiModelProperty(value="事项编码")
    private String sxbm;
    /**
     * 行政区划
     */
    @ApiModelProperty(value="行政区划")
    private String regionCode;
    /**
     * 事项名称
     */
    @ApiModelProperty(value="事项名称")
    private String itemName;
    /**
     * 区划名称
     */
    @ApiModelProperty(value="区划名称")
    private String regionName;
    /**
     * 事项垂管id
     */
    @ApiModelProperty(value="事项垂管id")
    private String itemId;
    /*****
     * 事项办理类型
     * ***/
    @ApiModelProperty(value ="事项办理类型 0 个人 1 法人")
    private String serviceObj;


    }
