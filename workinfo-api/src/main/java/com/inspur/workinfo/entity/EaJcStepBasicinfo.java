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
 * 基本信息
 *
 * @author Jason
 * @date 2020-06-17 11:38:07
 */
@Data
@TableName("EA_JC_STEP_BASICINFO")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "基本信息")
public class EaJcStepBasicinfo extends Model<EaJcStepBasicinfo> {
private static final long serialVersionUID = 1L;

    /**
     * 主键，原系统中具体数据的唯一ID 
     */
    @TableId
    @ApiModelProperty(value="主键，原系统中具体数据的唯一ID ")
    private String orgbusno;
    /**
     * 主键，详见附录2.3
     */
    @ApiModelProperty(value="主键，详见附录2.3")
    private String projid;
    /**
     * 由省级政务服务平台随机自动生成的数字，如：234765 
     */
    @ApiModelProperty(value="由省级政务服务平台随机自动生成的数字，如：234765 ")
    private String projpwd;
    /**
     * 主键，默认为1，用于重报数据的情况。
     */
    @ApiModelProperty(value="主键，默认为1，用于重报数据的情况。")
    private Integer dataver;
    /**
     * 编码规则见附录2.4
     */
    @ApiModelProperty(value="编码规则见附录2.4")
    private String regionId;
    /**
     * 详见附录2.1
     */
    @ApiModelProperty(value="详见附录2.1")
    private String itemCode;
    /**
     * 详见附录2.2
     */
    @ApiModelProperty(value="详见附录2.2")
    private String implementCode;
    /**
     * 事项所属行政区划，编码规则见附录2.4
     */
    @ApiModelProperty(value="事项所属行政区划，编码规则见附录2.4")
    private String itemregionid;
    /**
     * 事项主项名称
     */
    @ApiModelProperty(value="事项主项名称")
    private String itemname;
    /**
     * 若审批事项，不按大小项分，则子项名称等于审批事项名称（可为空）
     */
    @ApiModelProperty(value="若审批事项，不按大小项分，则子项名称等于审批事项名称（可为空）")
    private String subitemname;
    /**
     * 申请审批的项目的具体名称。如：关于XXX的交通建设工程施工许可
     */
    @ApiModelProperty(value="申请审批的项目的具体名称。如：关于XXX的交通建设工程施工许可")
    private String projectname;
    /**
     * GB/T 19488.2-2008中姓名或机构名称
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中姓名或机构名称")
    private String applicant;
    /**
     * GB/T 19488.2-2008中移动电话
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中移动电话")
    private String applicantmobile;
    /**
     * GB/T 19488.2-2008中联系电话
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中联系电话")
    private String applicanttel;
    /**
     * GB/T 19488.2-2008中电子信箱
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中电子信箱")
    private String applicantemail;
    /**
     * GB/T 19488.2-2008中机构名称
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中机构名称")
    private String acceptdeptname;
    /**
     * GB 32100-2015中统一社会信用代码
     */
    @ApiModelProperty(value="GB 32100-2015中统一社会信用代码")
    private String acceptdeptid;
    /**
     * 若没有则填无
     */
    @ApiModelProperty(value="若没有则填无")
    private String acceptdeptcode1;
    /**
     * 若没有则填无
     */
    @ApiModelProperty(value="若没有则填无")
    private String acceptdeptcode2;
    /**
     * 1-即办件，2-承诺件，3-联办件，4-上报件。
     */
    @ApiModelProperty(value="1-即办件，2-承诺件，3-联办件，4-上报件。")
    private Integer approvaltype;
    /**
     * 承诺时限
     */
    @ApiModelProperty(value="承诺时限")
    private Integer promisetimelimit;
    /**
     * 1-工作日；2-自然日；3-小时；4-分钟；5-月
     */
    @ApiModelProperty(value="1-工作日；2-自然日；3-小时；4-分钟；5-月")
    private Integer promisetimeunit;
    /**
     * 按法律、法规规定的办理此项审批事项的时限。
     */
    @ApiModelProperty(value="按法律、法规规定的办理此项审批事项的时限。")
    private Integer timelimit;
    /**
     * 规定办理时限的单位（年、月的情况需要换算成天，1年等于365天，1个月等于30天）：G – 工作日（不包含法定节假日）Z – 自然日
     */
    @ApiModelProperty(value="规定办理时限的单位（年、月的情况需要换算成天，1年等于365天，1个月等于30天）：G – 工作日（不包含法定节假日）Z – 自然日")
    private String timeunit;
    /**
     * 0-窗口提交，1-网上提交，2-信函,3-电报，4-电传，5-传真，6-邮件,7-电子数据交换，8-其他
     */
    @ApiModelProperty(value="0-窗口提交，1-网上提交，2-信函,3-电报，4-电传，5-传真，6-邮件,7-电子数据交换，8-其他")
    private Integer submit;
    /**
     * GB/T 19488.2-2008中日期时间
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中日期时间")
    private Date occurtime;
    /**
     * GB/T 19488.2-2008中姓名
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中姓名")
    private String transactor;
    /**
     * 投资项目并联相关，非投资项目并联事项可空
     */
    @ApiModelProperty(value="投资项目并联相关，非投资项目并联事项可空")
    private String projectCode;
    /**
     * 进入数据库时间，默认为系统时间
     */
    @ApiModelProperty(value="进入数据库时间，默认为系统时间")
    private Date maketime;
    /**
     * 系统标识,非空
     */
    @ApiModelProperty(value="系统标识,非空")
    private String sysmark;
    /**
     * $column.comments
     */
    @ApiModelProperty(value="$column.comments")
    private String acceptlist;
    /**
     * 数据操作
     */
    @ApiModelProperty(value="数据操作")
    private String cdOperation;
    /**
     * 业务办理项编码
     */
    @ApiModelProperty(value="业务办理项编码")
    private String taskhandleitem;
    /**
     * 国家基本编码
     */
    @ApiModelProperty(value="国家基本编码")
    private String coucatalogcode;
    /**
     * 国家实施编码
     */
    @ApiModelProperty(value="国家实施编码")
    private String coutaskcode;
    /**
     * 申请人类型
     */
    @ApiModelProperty(value="申请人类型")
    private String applyertype;
    /**
     * 受理文书编号
     */
    @ApiModelProperty(value="受理文书编号")
    private String acceptdocno;
    /**
     * 承诺办结时间
     */
    @ApiModelProperty(value="承诺办结时间")
    private Date promisedate;
    /**
     * 事项ID
     */
    @ApiModelProperty(value="事项ID")
    private String sourceid;
    }
