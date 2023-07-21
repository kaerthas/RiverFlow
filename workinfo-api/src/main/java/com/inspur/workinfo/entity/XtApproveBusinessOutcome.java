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
 * 业务信息结果物存量接口
 *
 * @author yunho code generator
 * @date 2023-07-17 17:50:43
 */
@Data
@TableName("XT_APPROVE_BUSINESS_OUTCOME")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "业务信息结果物存量接口")
public class XtApproveBusinessOutcome extends Model<XtApproveBusinessOutcome> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 文件类型0：纸质，1：电子，2：电子证照
     */
    @ApiModelProperty(value="文件类型0：纸质，1：电子，2：电子证照")
    private String wjlx;
    /**
     * 附件名称
     */
    @ApiModelProperty(value="附件名称")
    private String attachName;
    /**
     * 附件路径可下载路径
     */
    @ApiModelProperty(value="附件路径可下载路径")
    private String attachBody;
    /**
     * 网盘ID或证照唯一标识
     */
    @ApiModelProperty(value="网盘ID或证照唯一标识")
    private String attachPath;

    @ApiModelProperty(value = "申报流水号")
    private String sblshShort;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    }
