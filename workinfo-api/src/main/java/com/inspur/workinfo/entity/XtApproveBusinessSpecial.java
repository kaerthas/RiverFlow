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
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 特殊环节业务表
 *
 * @author yunho code generator
 * @date 2024-01-15 14:20:13
 */
@Data
@TableName("XT_APPROVE_BUSINESS_SPECIAL")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "特殊环节业务表")
public class XtApproveBusinessSpecial extends Model<XtApproveBusinessSpecial> {
private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value="主键id")
    private String seqId;
    /**
     * 申办流水号
     */
    @ApiModelProperty(value="申办流水号")
    private String sblshShort;
    /**
     * 身份证号或者统一社会信用代码
     */
    @ApiModelProperty(value="身份证号或者统一社会信用代码")
    private String idcard;
    /**
     * 申请人姓名
     */
    @ApiModelProperty(value="申请唯一id")
    private String onlineApplyId;


    /**
     * 申请人姓名
     */
    @ApiModelProperty(value="申请人姓名")
    private String apName;

    @ApiModelProperty(value="创建时间")
    private Date createTime;
    /**
     * 备注
     */
    @ApiModelProperty(value="备注")
    private String remark;
    }
