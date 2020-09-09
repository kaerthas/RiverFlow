/*
 *    Copyright (c) 2019-2025, jason All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the yunho.top developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: jason (jj@163.com)
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
 * 服务评价信息
 *
 * @author Jason
 * @date 2020-06-17 11:38:08
 */
@Data
@TableName("EA_JC_SERVEVAL")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "服务评价信息")
public class EaJcServeval extends Model<EaJcServeval> {
private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId
    @ApiModelProperty(value="主键")
    private String orgbusno;
    /**
     * 主键，为办件的唯一标识，由业务系统按规则自动生成
     */
    @ApiModelProperty(value="主键，为办件的唯一标识，由业务系统按规则自动生成")
    private String projid;
    /**
     * 主键，默认为1，用于重报数据的情况。
     */
    @ApiModelProperty(value="主键，默认为1，用于重报数据的情况。")
    private Integer dataver;
    /**
     * 由于评价结果在业务系统中是自定义的，评价结果
     */
    @ApiModelProperty(value="由于评价结果在业务系统中是自定义的，评价结果")
    private String evalresult;
    /**
     * 评价时间
     */
    @ApiModelProperty(value="评价时间")
    private Date evaldate;
    /**
     * 区划代码
     */
    @ApiModelProperty(value="区划代码")
    private Integer regionId;
    /**
     * 进入数据库时间，默认为系统时间
     */
    @ApiModelProperty(value="进入数据库时间，默认为系统时间")
    private Date maketime;
    /**
     * 系统标记
     */
    @ApiModelProperty(value="系统标记")
    private String sysmark;
    /**
     * 数据操作
     */
    @ApiModelProperty(value="数据操作")
    private String cdOperation;
    }
