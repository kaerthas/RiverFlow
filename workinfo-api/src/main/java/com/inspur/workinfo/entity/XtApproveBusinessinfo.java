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
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-07-07 17:04:51
 */
@Data
@TableName("XT_APPROVE_BUSINESSINFO")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "${comments}")
public class XtApproveBusinessinfo extends Model<XtApproveBusinessinfo> {
private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value="主键")
    private String seqId;
    /**
     * 行政区划编码
     */
    @ApiModelProperty(value="行政区划编码")
    private String xzqhdm;
    /**
     * 事项编码
     */
    @ApiModelProperty(value="事项编码")
    private String sxbm;
    /**
     * 描述
     */
    @ApiModelProperty(value="描述")
    private String expressType;
    /**
     * 办件流水号
     */
    @ApiModelProperty(value="办件流水号")
    private String sblshShort;
    /**
     * 渠道
     */
    @ApiModelProperty(value="渠道")
    private String channelCode;
    /**
     * 是否调用办件查询接口
     */
    @ApiModelProperty(value="是否调用办件查询接口")
    private String isUsed;
    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;
    /**
     * 修改时间
     */
    @ApiModelProperty(value="修改时间")
    private Date modifyTime;
    }
