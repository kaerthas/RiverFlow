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
 * 申报邮寄信息
 *
 * @author yunho code generator
 * @date 2023-07-11 14:16:19
 */
@Data
@TableName("XT_APPROVE_BUSINESS_EMAIL")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "申报邮寄信息")
public class XtApproveBusinessEmail extends Model<XtApproveBusinessEmail> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 主表对应主键id
     */
    @ApiModelProperty(value="主表对应主键id")
    private String baseInfoId;
    /**
     * 0：不邮寄 ，1：邮寄
     */
    @ApiModelProperty(value="0：不邮寄 ，1：邮寄")
    private String mailType;
    /**
     * 收件人姓名
     */
    @ApiModelProperty(value="收件人姓名")
    private String sendMailName;
    /**
     * 收件人电话号码
     */
    @ApiModelProperty(value="收件人电话号码")
    private String sendMailPhone;
    /**
     * 收件人详细地址
     */
    @ApiModelProperty(value="收件人详细地址")
    private String sendMailAddress;
    /**
     * 收件人省份名称
     */
    @ApiModelProperty(value="收件人省份名称")
    private String sendMailProvince;
    /**
     * 收件人地市名称
     */
    @ApiModelProperty(value="收件人地市名称")
    private String sendMailCity;
    /**
     * 收件人区县名称
     */
    @ApiModelProperty(value="收件人区县名称")
    private String sendMailCounty;
    /**
     * 收件人邮编
     */
    @ApiModelProperty(value="收件人邮编")
    private String sendMailPostCode;
    }
