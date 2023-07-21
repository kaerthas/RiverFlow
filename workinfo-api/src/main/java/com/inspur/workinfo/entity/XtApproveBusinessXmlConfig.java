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
 * 业务表单字段配置进入
 *
 * @author yunho code generator
 * @date 2023-07-10 16:25:35
 */
@Data
@TableName("XT_APPROVE_BUSINESS_XML_CONFIG")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "业务表单字段配置进入")
public class XtApproveBusinessXmlConfig extends Model<XtApproveBusinessXmlConfig> {
private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId
    @ApiModelProperty(value="主键id")
    private String seqId;
    /**
     * 事项垂管id
     */
    @ApiModelProperty(value="事项垂管id")
    private String itemId;
    /**
     * xml中字段编码
     */
    @ApiModelProperty(value="xml中字段编码")
    private String xmlCode;
    /**
     * xml中字段含义
     */
    @ApiModelProperty(value="xml中字段含义")
    private String xmlName;
    /**
     * 备注
     */
    @ApiModelProperty(value="备注")
    private String remark;
    /**
     * xml中字段类型
     */
    @ApiModelProperty(value="xml中字段类型")
    private String xmlType;
    /**
     * xml中字段大小
     */
    @ApiModelProperty(value="xml中字段大小")
    private Integer xmlSize;
    /**
     * 表属性
     */
    @ApiModelProperty(value="表属性")
    private String type;
    }
