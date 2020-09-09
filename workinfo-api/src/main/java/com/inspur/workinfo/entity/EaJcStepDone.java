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
 * 办结信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:07
 */
@Data
@TableName("EA_JC_STEP_DONE")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "办结信息")
public class EaJcStepDone extends Model<EaJcStepDone> {
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
     * 编码规则见附录2.4
     */
    @ApiModelProperty(value="编码规则见附录2.4")
    private String regionId;
    /**
     * 事项所属行政区划，编码规则见附录2.4
     */
    @ApiModelProperty(value="事项所属行政区划，编码规则见附录2.4")
    private String itemregionid;
    /**
     * 主键，默认为1，用于重报数据的情况
     */
    @ApiModelProperty(value="主键，默认为1，用于重报数据的情况")
    private Integer dataver;
    /**
     * 办结结果代码：0 – 出证办结（正常产生证照、批文的办结；准予许可的办结），1 – 不予许可，2 – 作废办结（指业务处理上无效的纪录），3 – 删除办结（指录入错误、操作错误等技术上的无效纪录），4 – 转报办结（指转报其他单位或上级单位的办结情况），5 – 退件（申请人长期不来补齐补正材料的办结；申请人主动放弃继续办理业务的办结），6 – 不予受理。
     */
    @ApiModelProperty(value="办结结果代码：0 – 出证办结（正常产生证照、批文的办结；准予许可的办结），1 – 不予许可，2 – 作废办结（指业务处理上无效的纪录），3 – 删除办结（指录入错误、操作错误等技术上的无效纪录），4 – 转报办结（指转报其他单位或上级单位的办结情况），5 – 退件（申请人长期不来补齐补正材料的办结；申请人主动放弃继续办理业务的办结），6 – 不予受理。")
    private Integer doneresult;
    /**
     * 在办结结果是上述的1、2、3、4、5时，本字段必须写明原因。
     */
    @ApiModelProperty(value="在办结结果是上述的1、2、3、4、5时，本字段必须写明原因。")
    private String exitres;
    /**
     * 行政许可决定书文号
     */
    @ApiModelProperty(value="行政许可决定书文号")
    private String approvalnumber;
    /**
     * 行政许可决定书有效期限
     */
    @ApiModelProperty(value="行政许可决定书有效期限")
    private Date approvallimit;
    /**
     * 提供的有效证件名称，详见附录2.5
     */
    @ApiModelProperty(value="提供的有效证件名称，详见附录2.5")
    private Integer cardtype;
    /**
     * 如果出证办结，必须有证件编号
     */
    @ApiModelProperty(value="如果出证办结，必须有证件编号")
    private String certificateno;
    /**
     * 如果出证办结，必须有
     */
    @ApiModelProperty(value="如果出证办结，必须有")
    private String certificatelimit;
    /**
     * GB/T 19488.2-2008中机构名称。
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中机构名称。")
    private String publisher;
    /**
     * 1-是，0-否。
     */
    @ApiModelProperty(value="1-是，0-否。")
    private Integer isfee;
    /**
     * GB/T 19488.2-2008中金额。
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中金额。")
    private Integer fee;
    /**
     * 如果收费 
     */
    @ApiModelProperty(value="如果收费 ")
    private String feestandard;
    /**
     * 如果收费
     */
    @ApiModelProperty(value="如果收费")
    private String feestandaccord;
    /**
     * GB/T 19488.2-2008中姓名。
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中姓名。")
    private String paypersonname;
    /**
     * GB/T 19488.2-2008中身份证件号码。
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中身份证件号码。")
    private String payperidcard;
    /**
     * GB/T 19488.2-2008中移动电话。
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中移动电话。")
    private String payermobile;
    /**
     * GB/T 19488.2-2008中联系电话。
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中联系电话。")
    private String payertel;
    /**
     * 环节发生时间
     */
    @ApiModelProperty(value="环节发生时间")
    private Date occurtime;
    /**
     * 环节办理人
     */
    @ApiModelProperty(value="环节办理人")
    private String transactor;
    /**
     * 进入数据库时间，默认为系统时间
     */
    @ApiModelProperty(value="进入数据库时间，默认为系统时间")
    private Date maketime;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="数据操作")
    private String cdOperation;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="结果证照名称")
    private String resultcetrname;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="是否快递递送结果")
    private String isdeliveryresults;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="满意度")
    private String satisfaction;
    }
