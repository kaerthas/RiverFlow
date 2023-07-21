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
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-07-11 14:16:18
 */
@Data
@TableName("XT_APPROVE_EXCHANGE")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "${comments}")
public class XtApproveExchange extends Model<XtApproveExchange> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 主表基本信息
     */
    @ApiModelProperty(value="主表基本信息")
    private String baseInfoId;
    /**
     * 是否交换 0未处理交换 1处理交换
     */
    @ApiModelProperty(value="是否交换 0未处理交换 1处理交换")
    private String isExchanged;
    /**
     * 是否交换成功 00为交换成功 
     */
    @ApiModelProperty(value="是否交换成功 00为交换成功 ")
    private String exchangeResult;
    /**
     * 交换次数，不能大于3
     */
    @ApiModelProperty(value="交换次数，不能大于3")
    private String exchanegTimes;
    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;
    /**
     * 交换完成时间
     */
    @ApiModelProperty(value="交换完成时间")
    private Date modifyTime;
    /**
     * 批次
     */
    @ApiModelProperty(value="批次")
    private String batch;
    }
