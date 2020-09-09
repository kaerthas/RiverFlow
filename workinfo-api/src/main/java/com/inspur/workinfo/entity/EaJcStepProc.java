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
 * 审批过程信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:07
 */
@Data
@TableName("EA_JC_STEP_PROC")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "审批过程信息")
public class EaJcStepProc extends Model<EaJcStepProc> {
private static final long serialVersionUID = 1L;

    /**
     * 主键，具体数据的唯一ID，中间库表之间数据的关联
     */
    @TableId
    @ApiModelProperty(value="主键，具体数据的唯一ID，中间库表之间数据的关联")
    private String orgbusno;
    /**
     * 主键，详见附录2.3
     */
    @ApiModelProperty(value="主键，详见附录2.3")
    private String projid;
    /**
     * 投资项目并联相关，非投资项目并联事项可空
     */
    @ApiModelProperty(value="投资项目并联相关，非投资项目并联事项可空")
    private String projectCode;
    /**
     * 事项所属行政区划，编码规则见附录2.4
     */
    @ApiModelProperty(value="事项所属行政区划，编码规则见附录2.4")
    private String itemregionid;
    /**
     * 主键，默认为1，用于重报数据的情况。
     */
    @ApiModelProperty(value="主键，默认为1，用于重报数据的情况。")
    private Integer dataver;
    /**
     * 主键，审批过程序号
     */
    @ApiModelProperty(value="主键，审批过程序号")
    private Integer sn;
    /**
     * 环节名称
     */
    @ApiModelProperty(value="环节名称")
    private String nodename;
    /**
     * 记录环节的批次信息
     */
    @ApiModelProperty(value="记录环节的批次信息")
    private String nodecode;
    /**
     * 1-开始环节；2-中间环节；3-结束环节；4-只有一条审批数据（开始既是结束
     */
    @ApiModelProperty(value="1-开始环节；2-中间环节；3-结束环节；4-只有一条审批数据（开始既是结束")
    private Integer nodetype;
    /**
     * 环节处理人编号
     */
    @ApiModelProperty(value="环节处理人编号")
    private String nodeprocer;
    /**
     * 环节处理人姓名
     */
    @ApiModelProperty(value="环节处理人姓名")
    private String nodeprocername;
    /**
     * 编码规则见附录2.4
     */
    @ApiModelProperty(value="编码规则见附录2.4")
    private String nodeprocerarea;
    /**
     * 编码规则见附录2.4
     */
    @ApiModelProperty(value="编码规则见附录2.4")
    private String regionId;
    /**
     * GB 32100-2015中统一社会信用代码
     */
    @ApiModelProperty(value="处理单位组织机构代码 GB 32100-2015中统一社会信用代码")
    private String procunit;
    /**
     * GB/T 19488.2-2008中机构名称。
     */
    @ApiModelProperty(value="处理单位名称 GB/T 19488.2-2008中机构名称。")
    private String procunitname;
    /**
     * 1-待办，2-已办
     */
    @ApiModelProperty(value="1-待办，2-已办")
    private Integer nodestate;
    /**
     * 环节开始时间
     */
    @ApiModelProperty(value="环节开始时间")
    private Date nodestarttime;
    /**
     * 如果没有结束时间，则填写开始时间
     */
    @ApiModelProperty(value="如果没有结束时间，则填写开始时间")
    private Date nodeendtime;
    /**
     * 环节处理意见
     */
    @ApiModelProperty(value="环节处理意见")
    private String nodeadv;
    /**
     * 环节承诺时限
     */
    @ApiModelProperty(value="环节承诺时限")
    private Integer timelimit;
    /**
     * 环节承诺时限单位 1-工作日；2-自然日；3-小时；4-分钟；5-月（可空）
     */
    @ApiModelProperty(value="环节承诺时限单位 1-工作日；2-自然日；3-小时；4-分钟；5-月（可空）")
    private Integer promisetimeunit;
    /**
     * 0 - 不同意，1 - 同意 ，2-不受理，3-不予受理，4-受理，5-补齐补正，6-特别程序（挂起操作），7-退回 
     */
    @ApiModelProperty(value="0 - 不同意，1 - 同意 ，2-不受理，3-不予受理，4-受理，5-补齐补正，6-特别程序（挂起操作），7-退回 ")
    private Integer noderesult;
    /**
     * 这里取环节开始时间
     */
    @ApiModelProperty(value="这里取环节开始时间")
    private Date occurtime;
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
     * 业务动作
     */
    @ApiModelProperty(value="业务动作")
    private String eventname;
    }
