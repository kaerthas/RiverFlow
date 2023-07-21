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
import java.util.Date;

/**
 * 补齐补正提交信息主表
 *
 * @author yunho code generator
 * @date 2023-07-18 17:04:11
 */
@Data
@TableName("XT_APPROVE_BUSINESS_CORRECTION")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "补齐补正提交信息主表")
public class XtApproveBusinessCorrection extends Model<XtApproveBusinessCorrection> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 审核人员部门名称
     */
    @ApiModelProperty(value="审核人员部门名称")
    private String orgName;
    /**
     * 审核人员部门编码
     */
    @ApiModelProperty(value="审核人员部门编码")
    private String orgCode;
    /**
     * 事项编码
     */
    @ApiModelProperty(value="事项编码")
    private String sxbm;
    /**
     * 确认补齐审核人工号
     */
    @ApiModelProperty(value="确认补齐审核人工号")
    private String userCode;
    /**
     * 确认补齐审核人名称
     */
    @ApiModelProperty(value="确认补齐审核人名称")
    private String userName;
    /**
     * 处理意见
     */
    @ApiModelProperty(value="处理意见")
    private String opinion;
    /**
     * 申办流水号
     */
    @ApiModelProperty(value="申办流水号")
    private String sblshShort;
    /**
     * 确认补齐时间
     */
    @ApiModelProperty(value="确认补齐时间")
    private Date endTime;
    /**
     * 备注字段
     */
    @ApiModelProperty(value="备注字段")
    private String remark;
    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;
    }
