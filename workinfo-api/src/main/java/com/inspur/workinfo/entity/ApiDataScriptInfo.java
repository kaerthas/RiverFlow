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
 * @date 2023-07-13 09:23:20
 */
@Data
@TableName("API_DATA_SCRIPT_INFO")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "${comments}")
public class ApiDataScriptInfo extends Model<ApiDataScriptInfo> {
private static final long serialVersionUID = 1L;

    /**
     * $column.comments
     */
    @TableId
    @ApiModelProperty(value="$column.comments")
    private String id;
    /**
     * 脚本名称
     */
    @ApiModelProperty(value="脚本名称")
    private String ruleName;
    /**
     * 脚本编码
     */
    @ApiModelProperty(value="脚本编码")
    private String ruleCode;
    /**
     * 脚本内容
     */
    @ApiModelProperty(value="脚本内容")
    private String ruleScript;
    /**
     * 脚本输出参数
     */
    @ApiModelProperty(value="脚本输出参数")
    private String ruleParam;
    /**
     * 1公共脚本，0私有脚本
     */
    @ApiModelProperty(value="1公共脚本，0私有脚本")
    private String ruleType;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String regionCode;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String regionName;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String orgCode;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String orgName;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private Date createTime;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private Date modifyTime;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String creator;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String remark;
    /**
     * 状态 0删除 1正常
     */
    @ApiModelProperty(value="状态 0删除 1正常")
    private String scriptStatus;
    }
