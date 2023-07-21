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
 * 协同调用中心基本数据表
 *
 * @author yunho code generator
 * @date 2023-07-11 14:16:19
 */
@Data
@TableName("XT_APPROVE_BUSINESS_BASE")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "协同调用中心基本数据表")
public class XtApproveBusinessBase extends Model<XtApproveBusinessBase> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 申办流水号
     */
    @ApiModelProperty(value="申办流水号")
    private String sblshShort;
    /**
     * 事项编码
     */
    @ApiModelProperty(value="事项编码")
    private String sxbm;
    /**
     * 事项名称
     */
    @ApiModelProperty(value="事项名称")
    private String sxmc;
    /**
     * 事项情形编码
     */
    @ApiModelProperty(value="事项情形编码")
    private String sxqxbm;
    /**
     * 行政区划代码
     */
    @ApiModelProperty(value="行政区划代码")
    private String xzqhdm;
    /**
     * 部门名称
     */
    @ApiModelProperty(value="部门名称")
    private String bmmc;
    /**
     * 部门统一社会信用代码
     */
    @ApiModelProperty(value="部门统一社会信用代码")
    private String bmzzjgdm;
    /**
     * 业务来源：01 网上申办
02 大厅受理
07 智能终端
08 手机APP
09 微信
     */
    @ApiModelProperty(value="业务来源：01 网上申办 02 大厅受理 07 智能终端 08 手机APP 09 微信")
    private String ywly;
    /**
     * 申报时间
     */
    @ApiModelProperty(value="申报时间")
    private Date sbsj;
    /**
     * 0个人事项，1法人事项
     */
    @ApiModelProperty(value="0个人事项，1法人事项")
    private String serviceObj;
    /**
     * 个人证件号码
     */
    @ApiModelProperty(value="个人证件号码")
    private String grIdcardno;
    /**
     * 个人证件类型默认为111
     */
    @ApiModelProperty(value="个人证件类型默认为111")
    private String grIdentitytype;
    /**
     * 个人联系电话
     */
    @ApiModelProperty(value="个人联系电话")
    private String grLinkphone;
    /**
     * 个人姓名
     */
    @ApiModelProperty(value="个人姓名")
    private String grName;
    /**
     * 企业名称
     */
    @ApiModelProperty(value="企业名称")
    private String qyOrgName;
    /**
     * 统一社会信用代码
     */
    @ApiModelProperty(value="统一社会信用代码")
    private String qyOrgCode;
    /**
     * 办理人姓名
     */
    @ApiModelProperty(value="办理人姓名")
    private String qyHandlerName;
    /**
     * 办理人联系电话
     */
    @ApiModelProperty(value="办理人联系电话")
    private String qyHandlerPhone;
    /**
     * 办理人证件类型
     */
    @ApiModelProperty(value="办理人证件类型")
    private String qyHandlerIdtype;
    /**
     * 办理人证件编号
     */
    @ApiModelProperty(value="办理人证件编号")
    private String qyHandlerId;
    /**
     * 备注
     */
    @ApiModelProperty(value="备注")
    private String remark;
    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;
    }
