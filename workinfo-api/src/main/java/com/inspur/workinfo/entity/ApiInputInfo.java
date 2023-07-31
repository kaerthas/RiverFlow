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
@TableName("API_INPUT_INFO")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "${comments}")
public class ApiInputInfo extends Model<ApiInputInfo> {
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
     * 上层参数ID
     */
    @ApiModelProperty(value="上层参数ID")
    private String parentId;
    /**
     * 所属接口ID
     */
    @ApiModelProperty(value="所属接口ID")
    private String apiId;
    /**
     * NORMAL/TOKEN/HEADER
     */
    @ApiModelProperty(value="NORMAL/TOKEN/HEADER")
    private String type;
    /**
     * 关联的TOKENID
     */
    @ApiModelProperty(value="关联的TOKENID")
    private String tokenApiId;
    /**
     * 是否缓存TOKEN，可根据接口设置某规则key
     */
    @ApiModelProperty(value="是否常量，1表示常量 0 表示变量")
    private String isConstant;
    /**
     * 注释
     */
    @ApiModelProperty(value="注释")
    private String remark;
    /**
     * 键值含义
     */
    @ApiModelProperty(value="键值含义")
    private String name;
    /**
     * 是否必填1是0否
     */
    @ApiModelProperty(value="是否必填1是0否")
    private String isMust;


    @ApiModelProperty(value="默认值")
    private String value;
    }

