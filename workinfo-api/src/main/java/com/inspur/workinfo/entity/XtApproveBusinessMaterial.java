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
 * 业务材料信息表
 *
 * @author yunho code generator
 * @date 2023-07-11 18:09:24
 */
@Data
@TableName("XT_APPROVE_BUSINESS_MATERIAL")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "业务材料信息表")
public class XtApproveBusinessMaterial extends Model<XtApproveBusinessMaterial> {
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
     * 事项中控平台提供的事项材料编号
     */
    @ApiModelProperty(value="事项中控平台提供的事项材料编号")
    private String stuffSeq;
    /**
     * 事项中控平台提供的事项材料名称
     */
    @ApiModelProperty(value="事项中控平台提供的事项材料名称")
    private String clmc;
    /**
     * 0：纸质，1：电子，2：电子证照
     */
    @ApiModelProperty(value="0：纸质，1：电子，2：电子证照")
    private String wjlx;
    /**
     * 0：原件，1：复印件，2：电子件
     */
    @ApiModelProperty(value="0：原件，1：复印件，2：电子件")
    private String cllx;
    /**
     * 材料数量
     */
    @ApiModelProperty(value="材料数量")
    private Integer clsl;
    /**
     * 材料附件全称（含后缀）
     */
    @ApiModelProperty(value="材料附件全称（含后缀）")
    private String attachName;
    /**
     * 材料附件唯一ID
     */
    @ApiModelProperty(value="材料附件唯一ID")
    private String attachId;
    /**
     * 备注
     */
    @ApiModelProperty(value="备注")
    private String remark;
    /**
     * 附件路径
     */
    @ApiModelProperty(value="附件路径")
    private String attachBody;
    /**
     * 附件类型
     */
    @ApiModelProperty(value="附件类型")
    private String attachType;
    /**
     * 网盘ID或证照唯一标识
     */
    @ApiModelProperty(value="网盘ID或证照唯一标识")
    private String attachPath;
//    /**
//     * 交换状态
//     */
//    @ApiModelProperty(value="交换状态")
//    private String exchange;
    }

