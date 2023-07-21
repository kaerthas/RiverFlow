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
 * 业务过程信息表
 *
 * @author yunho code generator
 * @date 2023-07-12 11:06:40
 */
@Data
@TableName("XT_APPROVE_BUSINESS_COURSE")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "业务过程信息表")
public class XtApproveBusinessCourse extends Model<XtApproveBusinessCourse> {
private static final long serialVersionUID = 1L;

    /**
     * 过程信息id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value="过程信息id")
    private String seqId;
    /**
     * 流程节点id
     */
    @ApiModelProperty(value="流程节点id")
    private String currentNodeId;
    /**
     * 流程节点代码
     */
    @ApiModelProperty(value="流程节点代码")
    private String currentNodeCode;
    /**
     * 活跃节点1代表活跃，0代表执行完成
     */
    @ApiModelProperty(value="活跃节点1代表活跃，0代表执行完成")
    private String active;
    /**
     * 申报流水号
     */
    @ApiModelProperty(value="申报流水号")
    private String sblshShort;
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
