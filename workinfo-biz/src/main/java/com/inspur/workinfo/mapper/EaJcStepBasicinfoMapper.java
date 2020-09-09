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

package com.inspur.workinfo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inspur.workinfo.entity.EaJcStepBasicinfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 基本信息
 *
 * @author Jason
 * @date 2020-06-17 11:38:07
 */
@Mapper
public interface EaJcStepBasicinfoMapper extends BaseMapper<EaJcStepBasicinfo> {

    Integer itemCount(@Param("otime") Date otime) ;
}
