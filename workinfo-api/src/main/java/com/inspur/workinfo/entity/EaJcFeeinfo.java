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
 * 收费明细信息
 *
 * @author Jason
 * @date 2020-06-17 11:38:08
 */
@Data
@TableName("EA_JC_FEEINFO")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "收费明细信息")
public class EaJcFeeinfo extends Model<EaJcFeeinfo> {
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
    private String busRegionId;
    /**
     * 事项所属行政区划，编码规则见附录2.4
     */
    @ApiModelProperty(value="事项所属行政区划，编码规则见附录2.4")
    private String regionId;
    /**
     * 主键，详见附录2.3
     */
    @ApiModelProperty(value="主键，详见附录2.3")
    private String projid;
    /**
     * 主键，默认为1，用于重报数据的情况
     */
    @ApiModelProperty(value="主键，默认为1，用于重报数据的情况")
    private Integer dataver;
    /**
     * 序号
     */
    @ApiModelProperty(value="序号")
    private String sn;
    /**
     *收费单位编码  GB 32100-2015中统一社会信用代码
     */
    @ApiModelProperty(value="收费单位编码 GB 32100-2015中统一社会信用代码")
    private String feeorgid;
    /**
     * 收费单位名称 GB/T 19488.2-2008中机构名称
     */
    @ApiModelProperty(value="收费单位名称 GB/T 19488.2-2008中机构名称")
    private String feeorgname;
    /**
     * 征缴单编码
     */
    @ApiModelProperty(value="征缴单编码")
    private String billnum;
    /**
     * GB/T 19488.2-2008中姓名
     */
    @ApiModelProperty(value="缴款人 GB/T 19488.2-2008中姓名")
    private String payperson;
    /**
     * 费用名称
     */
    @ApiModelProperty(value="费用名称")
    private String feetypename;
    /**
     * 费用标准
     */
    @ApiModelProperty(value="费用标准")
    private String feestand;
    /**
     * 实收标准
     */
    @ApiModelProperty(value="实收标准")
    private String feeamount;
    /**
     * 数量
     */
    @ApiModelProperty(value="数量")
    private String feenum;
    /**
     * 应收总额
     */
    @ApiModelProperty(value="应收总额")
    private Integer feestandamount;
    /**
     * 减免总额
     */
    @ApiModelProperty(value="减免总额")
    private Integer feederate;
    /**
     * 实收总额
     */
    @ApiModelProperty(value="实收总额")
    private Integer feetotal;
    /**
     * 减免原因
     */
    @ApiModelProperty(value="减免原因")
    private String reducereason;
    /**
     * 审核人 GB/T 19488.2-2008中姓名
     */
    @ApiModelProperty(value="审核人 GB/T 19488.2-2008中姓名")
    private String audiperson;
    /**
     * 支付时间
     */
    @ApiModelProperty(value="支付时间")
    private Date paytime;
    /**
     * 1-已缴 2-未缴
     */
    @ApiModelProperty(value="1-已缴 2-未缴")
    private Integer state;
    /**
     * 数据标记时间
     */
    @ApiModelProperty(value="数据标记时间")
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
