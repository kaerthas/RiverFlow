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

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 登记（申报）材料信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:04
 */
@Data
@TableName("PRE_FILE")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "登记（申报）材料信息")
public class PreFile extends Model<PreFile> {
private static final long serialVersionUID = 1L;

    /**
     * 主键，有业务系统自动产生
     */
    @TableId
    @ApiModelProperty(value="主键，有业务系统自动产生")
    private String unid;
    /**
     * 详见附录2.3
     */
    @ApiModelProperty(value="详见附录2.3")
    private String projid;
    /**
     * 审批事项所对应的提交材料
     */
    @ApiModelProperty(value="审批事项所对应的提交材料")
    private String attrname;
    /**
     * GB 32100-2015中统一社会信用代码。
     */
    @ApiModelProperty(value="GB 32100-2015中统一社会信用代码。")
    private String deptid;
    /**
     * 对应材料编码
     */
    @ApiModelProperty(value="对应材料编码")
    private String attrid;
    /**
     * 根据材料顺序依次编号
     */
    @ApiModelProperty(value="根据材料顺序依次编号")
    private String sortid;
    /**
     * 纸质收取、附件上传、电子证照库
     */
    @ApiModelProperty(value="纸质收取、附件上传、电子证照库")
    private String taketype;
    /**
     * 标识材料收取的情况，1-是，0-否
     */
    @ApiModelProperty(value="标识材料收取的情况，1-是，0-否")
    private Integer istake;
    /**
     * 记录所收取材料的数量
     */
    @ApiModelProperty(value="记录所收取材料的数量")
    private Integer amount;
    /**
     * GB/T 19488.2-2008中日期时间
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中日期时间")
    private Date taketime;
    /**
     * 如果有上传附件必填，上传附件的文件全称包含后缀名，如身份证.JPG
     */
    @ApiModelProperty(value="如果有上传附件必填，上传附件的文件全称包含后缀名，如身份证.JPG")
    private String filename;
    /**
     * 作为材料收取情况的补充说明
     */
    @ApiModelProperty(value="作为材料收取情况的补充说明")
    private String memo;
    /**
     * 由各业务系统产生，时间格式：YYYY-MM-DDHH24:MI:SS
     */
    @ApiModelProperty(value="由各业务系统产生，时间格式：YYYY-MM-DDHH24:MI:SS")
    private Date createTime;
    /**
     * 主键，默认值=1，如果有信息变更，则版本号递增
     */
    @ApiModelProperty(value="主键，默认值=1，如果有信息变更，则版本号递增")
    private Integer dataver;
    /**
     * 进入数据库时间，默认为系统时间
     */
    @ApiModelProperty(value="进入数据库时间，默认为系统时间")
    private Date maketime;
    /**
     * 审批过程序号
     */
    @ApiModelProperty(value="审批过程序号")
    private String sn;
    /**
     * 业务环节发生行政区划，编码规则见附录2.4
     */
    @ApiModelProperty(value="业务环节发生行政区划，编码规则见附录2.4")
    private String regionId;
    /**
     * 编码规则见附录2.4
     */
    @ApiModelProperty(value="编码规则见附录2.4")
    private String itemregionid;
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
