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
 * 接口调用信息
 *
 * @author yunho code generator
 * @date 2023-07-07 17:04:52
 */
@Data
@TableName("APPROVE_CALL")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "接口调用信息")
public class ApproveCall extends Model<ApproveCall> {
private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value="主键")
    private String callId;
    /**
     * 业务实例号
     */
    @ApiModelProperty(value="业务实例号")
    private String bsnum;
    /**
     * 调用最终状态,不能为空，0为失败，1为成功
     */
    @ApiModelProperty(value="调用最终状态,不能为空，0为失败，1为成功")
    private String callState;
    /**
     * 调用参数
     */
    @ApiModelProperty(value="调用参数")
    private String callParameter;
    /**
     * 方法名称
     */
    @ApiModelProperty(value="方法名称")
    private String interfaceName;
    /**
     * 调用系统代码
     */
    @ApiModelProperty(value="调用系统代码")
    private String calledSystemCode;
    /**
     * 调用系统名称
     */
    @ApiModelProperty(value="调用系统名称")
    private String calledSystemName;
    /**
     * 被调用系统地址
     */
    @ApiModelProperty(value="被调用系统地址")
    private String calledSystemAddr;
    /**
     * 调用次数
     */
    @ApiModelProperty(value="调用次数")
    private Integer callTimes;
    /**
     * 调用时间
     */
    @ApiModelProperty(value="调用时间")
    private Date callTime;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String parameterValue;
    }
