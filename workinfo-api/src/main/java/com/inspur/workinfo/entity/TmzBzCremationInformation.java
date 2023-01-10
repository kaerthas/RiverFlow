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
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-01-09 12:26:25
 */
@Data
@TableName("TMZ_BZ_CREMATION_INFORMATION")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "民政殡葬数据表")
public class TmzBzCremationInformation extends Model<TmzBzCremationInformation> {
private static final long serialVersionUID = 1L;

    /**
     * $column.comments
     */
    @TableId
    @ApiModelProperty(value="主键")
    private Long id;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="逝者姓名")
    private String name;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="身份证号")
    private String cardCode;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="出生日期")
    private Date birthDate;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="籍贯")
    private String home;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="死亡原因")
    private String deathCause;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="死亡签证机关")
    private String deathCertNo;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="性别")
    private String gender;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="民族")
    private String folk;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="家庭住址")
    private String address;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="死亡日期")
    private Date deathDate;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="户口所在地")
    private String registerPlace;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="火化单位名称")
    private String crematPlace;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="火化时间")
    private Date cremateDate;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="火化场区划代码")
    private String areaNumber;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="更新时间")
    private Date updateAt;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="行政区划代码")
    private String code;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="登记地点")
    private String checkinPlacle;
    }
