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
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-07-13 09:23:20
 */
@Data
@TableName("API_OUTPUT_INFO")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "${comments}")
public class ApiOutputInfo extends Model<ApiOutputInfo> {
private static final long serialVersionUID = 1L;

    /**
     * $column.comments
     */
    @TableId
    @ApiModelProperty(value="$column.comments")
    private String id;
    /**
     * 键值
     */
    @ApiModelProperty(value="键值")
    private String key;
    /**
     * 键值含义
     */
    @ApiModelProperty(value="键值含义")
    private String name;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String parentId;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String remark;
    /****
     * 绑定API_ID
     * ****/
    @ApiModelProperty(value = "绑定接口id")
    private String apiId;

    }
