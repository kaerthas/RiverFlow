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
 * 登记（申报）信息业务表单信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:04
 */
@Data
@TableName("PRE_COMM_FORM")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "登记（申报）信息业务表单信息")
public class PreCommForm extends Model<PreCommForm> {
private static final long serialVersionUID = 1L;

    /**
     * 主键，由业务系统自动产生
     */
    @TableId
    @ApiModelProperty(value="主键，由业务系统自动产生")
    private String unid;
    /**
     * 主键，详见附录2.3
     */
    @ApiModelProperty(value="主键，详见附录2.3")
    private String projid;
    /**
     * GB 32100-2015中统一社会信用代码。
     */
    @ApiModelProperty(value="GB 32100-2015中统一社会信用代码。")
    private String deptid;
    /**
     * 业务表单的具体中文名称
     */
    @ApiModelProperty(value="业务表单的具体中文名称")
    private String formName;
    /**
     * 如果业务系统有该字段，则填写
     */
    @ApiModelProperty(value="如果业务系统有该字段，则填写")
    private String formUnid;
    /**
     * 业务表单展示的顺序号，如果只有一个表单则指=1 
     */
    @ApiModelProperty(value="业务表单展示的顺序号，如果只有一个表单则指=1 ")
    private String formSort;
    /**
     * 如材料的表单、办件申报号等， 
     */
    @ApiModelProperty(value="如材料的表单、办件申报号等， ")
    private String useUnid;
    /**
     * 业务环节发生行政区划，编码规则见附录2.4
     */
    @ApiModelProperty(value="业务环节发生行政区划，编码规则见附录2.4")
    private String regionId;
    /**
     * 事项所属行政区划，编码规则见附录2.4
     */
    @ApiModelProperty(value="事项所属行政区划，编码规则见附录2.4")
    private String itemregionid;
    /**
     * 详见附录2.5证件类型
     */
    @ApiModelProperty(value="详见附录2.5证件类型")
    private Integer useType;
    /**
     * 业务表单信息项的值
     */
    @ApiModelProperty(value="业务表单信息项的值")
    private String itemValues;
    /**
     * 补充说明
     */
    @ApiModelProperty(value="补充说明")
    private String remark;
    /**
     * 由各业务系统产生，时间格式：YYYY-MM-DDHH24:MI:SS 
     */
    @ApiModelProperty(value="由各业务系统产生，时间格式：YYYY-MM-DDHH24:MI:SS ")
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
