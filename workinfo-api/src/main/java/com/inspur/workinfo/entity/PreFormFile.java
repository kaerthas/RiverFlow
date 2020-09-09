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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 文档附件信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:04
 */
@Data
@TableName("PRE_FORM_FILE")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "文档附件信息")
public class PreFormFile extends Model<PreFormFile> {
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
     * 事项所属行政区划，编码规则见附录2.4
     */
    @ApiModelProperty(value="事项所属行政区划，编码规则见附录2.4")
    private String regionId;
    /**
     * 事项所属行政区划，编码规则见附录2.4
     */
    @ApiModelProperty(value="事项所属行政区划，编码规则见附录2.4")
    private String itemregionid;
    /**
     * 主键
     */
    @ApiModelProperty(value="主键")
    private String sn;
    /**
     * 附件的全称，包括文件后缀名
     */
    @ApiModelProperty(value="附件的全称，包括文件后缀名")
    private String filename;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String belongorgid;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String belongorgname;
    /**
     * 由各业务系统产生，时间格式：YYYY-MM-DDHH24:MI:SS
     */
    @ApiModelProperty(value="由各业务系统产生，时间格式：YYYY-MM-DDHH24:MI:SS")
    private Date createTime;
    /**
     * 进入数据库时间，默认为系统时间
     */
    @ApiModelProperty(value="进入数据库时间，默认为系统时间")
    private Date maketime;
    /**
     * 政务外网区地址
     */
    @ApiModelProperty(value="政务外网区地址")
    private String entityurlZ;
    /**
     * 互联网区地址
     */
    @ApiModelProperty(value="互联网区地址")
    private String entityurlH;
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
