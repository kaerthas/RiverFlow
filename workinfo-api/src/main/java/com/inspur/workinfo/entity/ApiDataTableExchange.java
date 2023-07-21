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
 * 库表交换绑定关系表
 *
 * @author yunho code generator
 * @date 2023-07-14 15:31:50
 */
@Data
@TableName("API_DATA_TABLE_EXCHANGE")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "库表交换绑定关系表")
public class ApiDataTableExchange extends Model<ApiDataTableExchange> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 本地数据库表名称
     */
    @ApiModelProperty(value="本地数据库表名称")
    private String localTable;
    /**
     * 业务库表名称
     */
    @ApiModelProperty(value="业务库表名称")
    private String businessTable;
    /**
     * 数据库配置名称 与配置表关联
     */
    @ApiModelProperty(value="数据库配置名称 与配置表关联")
    private String datasourceName;

    /********
     * 目标表数据源配置名称
     * **********/
    @ApiModelProperty(value = "目标表数据源配置名称")
    private String aimDatasourceName;

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
    }
