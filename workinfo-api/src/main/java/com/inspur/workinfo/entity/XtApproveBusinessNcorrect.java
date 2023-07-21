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
 * 业务系统补齐补正告知
 *
 * @author yunho code generator
 * @date 2023-07-18 14:14:53
 */
@Data
@TableName("XT_APPROVE_BUSINESS_NCORRECT")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "业务系统补齐补正告知")
public class XtApproveBusinessNcorrect extends Model<XtApproveBusinessNcorrect> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 申办流水号，业务唯一值
     */
    @ApiModelProperty(value="申办流水号，业务唯一值")
    private String sblshShort;
    /**
     * 补正告知发出人姓名
     */
    @ApiModelProperty(value="补正告知发出人姓名")
    private String bzgzfcrxm;
    /**
     * 补正告知原因
     */
    @ApiModelProperty(value="补正告知原因")
    private String bzgzyy;
    /**
     * 补正材料清单名
     */
    @ApiModelProperty(value="补正材料清单名")
    private String bzclqd;
    /**
     * 补正告知时限默认0
     */
    @ApiModelProperty(value="补正告知时限默认0")
    private String bzgzsx;
    /**
     * G：工作日（不包含法定节假日Z：自然日
     */
    @ApiModelProperty(value="G：工作日（不包含法定节假日Z：自然日")
    private String bzgzsxdw;
    /**
     * 补正告知材料编码
     */
    @ApiModelProperty(value="补正告知材料编码")
    private String bqbzclbm;
    /**
     * 补正时间
     */
    @ApiModelProperty(value="补正时间")
    private Date bzgzsj;
    /**
     * 区划代码
     */
    @ApiModelProperty(value="区划代码")
    private String xzqhdm;
    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;
    }
