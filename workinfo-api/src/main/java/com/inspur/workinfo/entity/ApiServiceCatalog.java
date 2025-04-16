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
@TableName("API_SERVICE_CATALOG")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "${comments}")
public class ApiServiceCatalog extends Model<ApiServiceCatalog> {
private static final long serialVersionUID = 1L;

    /**
     * $column.comments
     */
    @TableId
    @ApiModelProperty(value="$column.comments")
    private String id;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String catalogName;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String serviceId;
    /**
     * POST/GET
     */
    @ApiModelProperty(value="POST/GET")
    private String method;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String url;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String version;
    /**
     * data（数据服务），sql(sql服务)，proxy（代理服务)，callback(回调服务),apply(申请服务)
     */
    @ApiModelProperty(value="data（数据服务），sql(sql服务)，proxy（代理服务)，callback(回调服务),apply(申请服务)")
    private String type;
    /**
     * 入参ID
     */
    @ApiModelProperty(value="入参ID")
    private String inputId;
    /**
     * 出参ID
     */
    @ApiModelProperty(value="出参ID")
    private String outputId;
    /**
     * JSON/XML
     */
    @ApiModelProperty(value="JSON/XML")
    private String resultType;
    /**
     * JSON/XML
     */
    @ApiModelProperty(value="JSON/XML")
    private String requestType;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String creator;
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
     * 发布时间
     */
    @ApiModelProperty(value="发布时间")
    private Date publishTime;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String remark;
    /**
     * 0已保存，1待审核，2已上线，3已下线，4已驳回，9已删除
     */
    @ApiModelProperty(value="0已保存，1待审核，2已上线，3已下线，4已驳回，9已删除")
    private String catalogStatus;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private Integer callTimes;
    /**
     * 返回方式，1：直接返回源接口数据，2：返回格式化的数据
     */
    @ApiModelProperty(value="返回方式，1：直接返回源接口数据，2：返回格式化的数据")
    private String returnMode;
    /**
     * 共享方式（0：不能被申请，不予共享。1：能被申请，授权码）
     */
    @ApiModelProperty(value="共享方式（0：不能被申请，不予共享。1：能被申请，授权码）")
    private String ableApplied;
    /**
     * 审核类型 9无 0待审核 1审核通过2审核驳回
     */
    @ApiModelProperty(value="审核类型 9无 0待审核 1审核通过2审核驳回")
    private String checkState;
    /**
     * 原始接口路径
     */
    @ApiModelProperty(value="原始接口路径")
    private String originalUrl;
    /**
     * 入参方式，1表示无，2表示格式化数据
     */
    @ApiModelProperty(value="入参方式，1表示无，2表示格式化数据")
    private String requestMode;

    /**
     * 是否已经生成出参物化表，1是，0否
     */
    @ApiModelProperty(value="是否已经生成出参物化表，1是，0否")
    private String  isWh;

    /**
     * 是否需要生成出参物化表，1是，0否
     */
    @ApiModelProperty(value="是否已经生成出参物化表，1是，0否")
    private String  isNeedWh;
    /**
     * 是否需要缓存，1是，0否
     */
    @ApiModelProperty(value = "是否需要缓存，1是，0否 ")
    private String  isNeedCache;
    /*****
     * 是否互联网区配置
     * *****/
    @ApiModelProperty(value = "是否互联网区0不是，1是")
    private String isInternet;


    }
