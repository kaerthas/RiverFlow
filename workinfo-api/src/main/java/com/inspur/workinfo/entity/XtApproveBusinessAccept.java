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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/**
 * 协同调度受理信息表
 *
 * @author yunho code generator
 * @date 2023-07-13 09:23:20
 */
@Data
@TableName("XT_APPROVE_BUSINESS_ACCEPT")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "协同调度受理信息表")
public class XtApproveBusinessAccept extends Model<XtApproveBusinessAccept> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 申报流水号
     */
    @ApiModelProperty(value="申报流水号")
    private String sblshShort;
    /**
     * 事项编码
     */
    @ApiModelProperty(value="事项编码")
    private String sxbm;
    /**
     * 业务受理时间
     */
    @ApiModelProperty(value="业务受理时间")
    private Date ywslsj;
    /**
     * 业务受理名称
     */
    @ApiModelProperty(value="业务受理名称")
    private String ywlsmc;
    /**
     * 0：不予受理，1：受理
     */
    @ApiModelProperty(value="0：不予受理，1：受理")
    private String ywlszt;
    /**
     * 业务受理意见
     */
    @ApiModelProperty(value="业务受理意见")
    private String ywslyj;
    /**
     * 业务受理区划名称
     */
    @ApiModelProperty(value="业务受理区划名称")
    private String ywslqhmc;
    /**
     * 业务受理区划编码
     */
    @ApiModelProperty(value="业务受理区划编码")
    private String ywslqhbm;
    /**
     * 业务受理部门名称
     */
    @ApiModelProperty(value="业务受理部门名称")
    private String ywslbmmc;
    /**
     * 业务受理部门编码
     */
    @ApiModelProperty(value="业务受理部门编码")
    private String ywslbmbm;
    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;
    }
