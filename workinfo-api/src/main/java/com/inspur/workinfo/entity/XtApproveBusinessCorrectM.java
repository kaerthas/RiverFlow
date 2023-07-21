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
 * 补齐补正业务提交
 *
 * @author yunho code generator
 * @date 2023-07-18 17:04:11
 */
@Data
@TableName("XT_APPROVE_BUSINESS_CORRECT_M")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "补齐补正业务提交")
public class XtApproveBusinessCorrectM extends Model<XtApproveBusinessCorrectM> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 主表信息id 一对多关系
     */
    @ApiModelProperty(value="主表信息id 一对多关系")
    private String correctId;
    /**
     * 材料名称
     */
    @ApiModelProperty(value="材料名称")
    private String clmc;
    /**
     * 文件类型0：纸质，1：电子，2：电子证照
     */
    @ApiModelProperty(value="文件类型0：纸质，1：电子，2：电子证照")
    private String wjlx;
    /**
     * 材料类型0：原件，1：复印件，2：电子件
     */
    @ApiModelProperty(value="材料类型0：原件，1：复印件，2：电子件")
    private String cllx;
    /**
     * 材料数量
     */
    @ApiModelProperty(value="材料数量")
    private String clsl;
    /**
     * 附件名称
     */
    @ApiModelProperty(value="附件名称")
    private String attachName;
    /**
     * 附件UUID
     */
    @ApiModelProperty(value="附件UUID")
    private String attachId;
    /**
     * 附件路径
     */
    @ApiModelProperty(value="附件路径")
    private String attachBody;
    /**
     * 网盘ID或证照唯一标识
     */
    @ApiModelProperty(value="网盘ID或证照唯一标识")
    private String attachPath;
    /**
     * 材料编号
     */
    @ApiModelProperty(value="材料编号")
    private String stuffSeq;
    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;
    /**
     * 备注
     */
    @ApiModelProperty(value="备注")
    private String remark;
    }
