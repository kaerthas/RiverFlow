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
 * 特殊环节信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:06
 */
@Data
@TableName("EA_JC_STEP_SPECIALNODE")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "特殊环节信息")
public class EaJcStepSpecialnode extends Model<EaJcStepSpecialnode> {
private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId
    @ApiModelProperty(value="主键")
    private String orgbusno;
    /**
     * 编码规则见附录2.4
     */
    @ApiModelProperty(value="编码规则见附录2.4")
    private Integer busRegionId;
    /**
     * 主键，详见附录2.3
     */
    @ApiModelProperty(value="主键，详见附录2.3")
    private String projid;
    /**
     * 编码规则见附录2.4
     */
    @ApiModelProperty(value="编码规则见附录2.4")
    private String regionId;
    /**
     * 主键，默认为1，用于重报数据的情况。
     */
    @ApiModelProperty(value="主键，默认为1，用于重报数据的情况。")
    private Integer dataver;
    /**
     * 主键，特殊环节序号，标识第几次特殊环节
     */
    @ApiModelProperty(value="主键，特殊环节序号，标识第几次特殊环节")
    private Integer sn;
    /**
     * 环节名称
     */
    @ApiModelProperty(value="环节名称")
    private String nodename;
    /**
     * GB/T 19488.2-2008中机构名称。
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中机构名称。")
    private String procunitname;
    /**
     * GB/T 19488.2-2008中组织机构代码
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中组织机构代码")
    private String procunitid;
    /**
     * 处理人标识
     */
    @ApiModelProperty(value="处理人标识")
    private String procerid;
    /**
     * GB/T 19488.2-2008中姓名
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中姓名")
    private String procername;
    /**
     * 处理人相关备注
     */
    @ApiModelProperty(value="处理人相关备注")
    private String procerremark;
    /**
     * 环节开始时间
     */
    @ApiModelProperty(value="环节开始时间")
    private Date nodestarttime;
    /**
     * （如果没有结束时间，填写开始时间)
     */
    @ApiModelProperty(value="（如果没有结束时间，填写开始时间)")
    private Date nodeendtime;
    /**
     * 通知申请人时间
     */
    @ApiModelProperty(value="通知申请人时间")
    private Date notetime;
    /**
     * 环节处理意见
     */
    @ApiModelProperty(value="环节处理意见")
    private String nodeprocadv;
    /**
     * 环节处理地点
     */
    @ApiModelProperty(value="环节处理地点")
    private String nodeprocaddr;
    /**
     * 环节处理依据
     */
    @ApiModelProperty(value="环节处理依据")
    private String nodeprocaccord;
    /**
     * 1 - 补齐补正。2 - 听证。3 - 公示。4 - 核实。5 - 挂起。6 – 预审补齐补正。7-预审驳回。
     */
    @ApiModelProperty(value="1 - 补齐补正。2 - 听证。3 - 公示。4 - 核实。5 - 挂起。6 – 预审补齐补正。7-预审驳回。")
    private Integer noderesult;
    /**
     * 这里取环节开始时间
     */
    @ApiModelProperty(value="这里取环节开始时间")
    private Date nodetime;
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
     * $column.comments
     */
    @ApiModelProperty(value="数据操作")
    private String cdOperation;
    /**
     * 材料清单
     */
    @ApiModelProperty(value="材料清单")
    private String lists;
    /**
     * 特别程序种类
     */
    @ApiModelProperty(value="特别程序种类")
    private String specialtype;
    /**
     * 特别程序名称
     */
    @ApiModelProperty(value="特别程序名称")
    private String specialname;
    /**
     * 申请人
     */
    @ApiModelProperty(value="申请人")
    private String applyusername;
    /**
     * 特别程序处理结果
     */
    @ApiModelProperty(value="特别程序处理结果")
    private String result;
    }
