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

/**
 * ${comments}
 *
 * @author Jason
 * @date 2020-06-17 10:15:03
 */
@Data
@TableName("REGININFO")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "${comments}")
public class Regininfo extends Model<Regininfo> {
private static final long serialVersionUID = 1L;

    /**
     * $column.comments
     */
    @TableId
    @ApiModelProperty(value="区划代码")
    private String reginId;
    /**
     * 区划名称
     */
    @ApiModelProperty(value="区划名称")
    private String reginName;
    /**
     * 排序
     */
    @ApiModelProperty(value="排序")
    private Integer orderSt;
    }
