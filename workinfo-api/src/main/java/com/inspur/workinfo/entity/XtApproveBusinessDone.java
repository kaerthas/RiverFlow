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
 * 业务办结信息表
 *
 * @author yunho code generator
 * @date 2023-07-13 15:25:11
 */
@Data
@TableName("XT_APPROVE_BUSINESS_DONE")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "业务办结信息表")
public class XtApproveBusinessDone extends Model<XtApproveBusinessDone> {
private static final long serialVersionUID = 1L;

    /**
     * 主键信息
     */
    @TableId
    @ApiModelProperty(value="主键信息")
    private String seqId;
    /**
     * 申报信息流水号
     */
    @ApiModelProperty(value="申报信息流水号")
    private String sblshShort;
    /**
     * 事项编码
     */
    @ApiModelProperty(value="事项编码")
    private String sxbm;
    /**
     * 办结部门名称
     */
    @ApiModelProperty(value="办结部门名称")
    private String bjbmmc;
    /**
     * 办结部门编码
     */
    @ApiModelProperty(value="办结部门编码")
    private String bjbmbm;
    /**
     * 审批人姓名
     */
    @ApiModelProperty(value="审批人姓名")
    private String sprxm;
    /**
     * 审批人代码
     */
    @ApiModelProperty(value="审批人代码")
    private String sprdm;
    /**
     * 办结结果代码0：出证办结
1：退回办结
2：作废办结
3：删除办结
5：补正不来办结
6：准予许可
7：不予许可
     */
    @ApiModelProperty(value="办结结果代码0：出证办结 1：退回办结 2：作废办结 3：删除办结 5：补正不来办结 6：准予许可 7：不予许可")
    private String bjjgdm;
    /**
     * 办结结果描述
     */
    @ApiModelProperty(value="办结结果描述")
    private String bjjgms;
    /**
     * 作废退回原因 1，2，3，5
     */
    @ApiModelProperty(value="作废退回原因 1，2，3，5")
    private String zfhthyy;
    /**
     * 办结时间
     */
    @ApiModelProperty(value="办结时间")
    private Date bjsj;
    /**
     * 回传表单信息 json字符串
     */
    @ApiModelProperty(value="回传表单信息 json字符串")
    private String bz;
    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;
    }
