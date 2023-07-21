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
 * 代理数据源管理表
 *
 * @author yunho code generator
 * @date 2023-07-13 16:28:50
 */
@Data
@TableName("API_DATASOURCE_CONFIG")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "代理数据源管理表")
public class ApiDatasourceConfig extends Model<ApiDatasourceConfig> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 数据连接名称
     */
    @ApiModelProperty(value="数据连接名称")
    private String name;
    /**
     * 数据库链接路径
     */
    @ApiModelProperty(value="数据库链接路径")
    private String url;
    /**
     * 数据库登录名
     */
    @ApiModelProperty(value="数据库登录名")
    private String userName;
    /**
     * 数据库链接密码
     */
    @ApiModelProperty(value="数据库链接密码")
    private String password;
    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;
    /**
     * 修改时间
     */
    @ApiModelProperty(value="修改时间")
    private Date modifyTime;
    /**
     * 0正常 1删除
     */
    @ApiModelProperty(value="0正常 1删除")
    private String delFlag;

    /**
     * 驱动类型
     */
    @ApiModelProperty(value="驱动类型")
    private String driverType;
    }
