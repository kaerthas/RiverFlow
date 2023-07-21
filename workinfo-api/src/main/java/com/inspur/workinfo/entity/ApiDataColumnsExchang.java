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
 * 库表交换字段信息表
 *
 * @author yunho code generator
 * @date 2023-07-14 15:31:49
 */
@Data
@TableName("API_DATA_COLUMNS_EXCHANG")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "库表交换字段信息表")
public class ApiDataColumnsExchang extends Model<ApiDataColumnsExchang> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 绑定表主键
     */
    @ApiModelProperty(value="绑定表主键")
    private String tableId;
    /**
     * 本地表字段名称
     */
    @ApiModelProperty(value="本地表字段名称")
    private String localColumns;
    /**
     * 业务库表字段名称
     */
    @ApiModelProperty(value="业务库表字段名称")
    private String businessColumns;
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
     * 描述
     */
    @ApiModelProperty(value="描述")
    private String comments;
    /**
     * 备注
     */
    @ApiModelProperty(value="备注")
    private String remark;
    /**
     * 脚本格式化id
     */
    @ApiModelProperty(value="脚本格式化id")
    private String scrtptId;
    @ApiModelProperty(value = "colum and condition")
    private String columnsType;
    }
