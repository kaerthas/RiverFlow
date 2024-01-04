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
 * 事项办理流程配置
 *
 * @author yunho code generator
 * @date 2023-07-12 11:06:41
 */
@Data
@TableName("XT_APPROVE_ITEMFLOW_CONFIG")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "事项办理流程配置")
public class XtApproveItemflowConfig extends Model<XtApproveItemflowConfig> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 单事项编码
     */
    @ApiModelProperty(value="单事项编码")
    private String sxbm;
    /**
     * 节点名称
     */
    @ApiModelProperty(value="节点名称")
    private String nodeName;
    /**
     * 父级节点
     */
    @ApiModelProperty(value="父级节点")
    private String parentId;
    /**
     * 节点标识符号
     */
    @ApiModelProperty(value="节点标识符号")
    private String nodeCode;
    /**
     * 0表示接口调用，1表示库表交换
     */
    @ApiModelProperty(value="0表示接口调用，1表示库表交换")
    private String exchangeType;
    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;
    /**
     * 交换类型为接口调用时，需要绑定接口id
     */
    @ApiModelProperty(value="交换类型为接口调用时，需要绑定接口id")
    private String apiId;
    /**
     * 库表名称
     */
    @ApiModelProperty(value="库表名称")
    private String tableId;
    /**
     * 条件关键字
     */
    @ApiModelProperty(value="条件关键字")
    private String condition;
    /**
     * 下级条件值
     */
    @ApiModelProperty(value="下级条件值")
    private String childValue;

    /***
     * 排序
     * **/
    @ApiModelProperty(value = "排序")
    private String sorter;


    }
