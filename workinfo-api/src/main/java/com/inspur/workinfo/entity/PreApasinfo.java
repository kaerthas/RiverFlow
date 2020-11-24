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
 * 登记（申报）信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:05
 */
@Data
@TableName("PRE_APASINFO")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "登记（申报）信息")
public class PreApasinfo extends Model<PreApasinfo> {
private static final long serialVersionUID = 1L;

    /**
     * 主键，详见附录2.3
     */
    @TableId(type = IdType.INPUT)
    @ApiModelProperty(value="主键，详见附录2.3")
    private String projid;
    /**
     * 由业务系统随机自动生成的数字，如：234765 
     */
    @ApiModelProperty(value="由业务系统随机自动生成的数字，如：234765 ")
    private String projpwd;
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
     * 审批事项的版本号
     */
    @ApiModelProperty(value="审批事项的版本号")
    private String itemversion;
    /**
     * 申报的事项名称，如：交通建设工程施工许可
     */
    @ApiModelProperty(value="申报的事项名称，如：交通建设工程施工许可")
    private String itemname;
    /**
     * 申请审批的项目的具体名称。如：关于XXX的交通建设工程施工许可
     */
    @ApiModelProperty(value="申请审批的项目的具体名称。如：关于XXX的交通建设工程施工许可")
    private String projectname;
    /**
     * 详见附录2.7
     */
    @ApiModelProperty(value="详见附录2.7")
    private Integer projectstate;
    /**
     * 1-即办件，2-承诺件，3-联办件，4-上报件。
     */
    @ApiModelProperty(value="1-即办件，2-承诺件，3-联办件，4-上报件。")
    private Integer infotype;
    /**
     * GB/T 19488.2-2008中姓名或
机构名称。

     */
    @ApiModelProperty(value="GB/T 19488.2-2008中姓名或机构名称")
    private String applyname;
    /**
     * GB/T 19488.2-2008中身份证件号码
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中身份证件号码")
    private String applyCardtypenumber;
    /**
     * GB/T 19488.2-2008中姓名。
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中姓名。")
    private String contactman;
    /**
     * GB/T 19488.2-2008中身份证件号码
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中身份证件号码")
    private String contactmanCardnumber;
    /**
     * GB/T 19488.2-2008中移动号码
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中移动号码")
    private String telphone;
    /**
     * GB/T 19488.2-2008中邮政编码
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中邮政编码")
    private Integer postcode;
    /**
     * GB/T 19488.2-2008中通信地址
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中通信地址")
    private String address;
    /**
     * GB/T 19488.2-2008中法定代表人
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中法定代表人")
    private String legalman;
    /**
     * GB 32100-2015中统一社会信用代码。
     */
    @ApiModelProperty(value="GB 32100-2015中统一社会信用代码。")
    private String deptid;
    /**
     * GB/T 19488.2-2008中机构名称。
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中机构名称。")
    private String deptname;
    /**
     * 创建用户唯一标识
     */
    @ApiModelProperty(value="创建用户唯一标识")
    private String receiveUseid;
    /**
     * 创建用户名称
     */
    @ApiModelProperty(value="创建用户名称")
    private String receiveName;
    /**
     * GB/T 19488.2-2008中时间。
     */
    @ApiModelProperty(value="GB/T 19488.2-2008中时间。")
    private Date receivetime;
    /**
     * 详见附录2.8
     */
    @ApiModelProperty(value="详见附录2.8")
    private Integer approveType;
    /**
     * 编码规则见附录2.4
     */
    @ApiModelProperty(value="编码规则见附录2.4")
    private String regionId;
    /**
     * 0-作废，1-有效。
     */
    @ApiModelProperty(value="0-作废，1-有效。")
    private Integer datastate;
    /**
     * 由各业务系统产生，时间格式：YYYY-MM-DDHH24:MI:SS。
     */
    @ApiModelProperty(value="由各业务系统产生，时间格式：YYYY-MM-DDHH24:MI:SS。")
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
     * 部门或其他第三方系统个人中心链接地址，用于政务服务网个人中心跳转使用
     */
    @ApiModelProperty(value="部门或其他第三方系统个人中心链接地址，用于政务服务网个人中心跳转使用")
    private String centerurl;
    /**
     * 格式为 ID:NAME,ID:NAME,···
     */
    @ApiModelProperty(value="格式为 ID:NAME,ID:NAME,···")
    private String acceptlist;
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
     * 申请人证件类型
     */
    @ApiModelProperty(value="申请人证件类型")
    private String applyCardtype;
    /**
     * 申请来源 办理形式（国家）
     */
    @ApiModelProperty(value="申请来源")
    private String applyfrom;
    /**
     * 联系人证件类对象
     */
    @ApiModelProperty(value="联系人证件类型")
    private String contactmanCardtype;
    /**
     * 事项ID
     */
    @ApiModelProperty(value="事项ID")
    private String sourceid;

    }
