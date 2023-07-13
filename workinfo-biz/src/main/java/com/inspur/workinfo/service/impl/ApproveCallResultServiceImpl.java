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
package com.inspur.workinfo.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.ApproveCall;
import com.inspur.workinfo.entity.ApproveCallResult;
import com.inspur.workinfo.mapper.ApproveCallResultMapper;
import com.inspur.workinfo.service.ApproveCallResultService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 接口调用结果信息
 *
 * @author yunho code generator
 * @date 2023-07-07 17:04:52
 */
@Service
public class ApproveCallResultServiceImpl extends ServiceImpl<ApproveCallResultMapper, ApproveCallResult> implements ApproveCallResultService {

    @Override
    public ApproveCallResult createCallResultBean(ApproveCall callBean,String calledSystemName) {
        ApproveCallResult callResultBean=new ApproveCallResult();
        callResultBean.setCallState(CommonConstants.API_SUCCESS);
        callResultBean.setSeqId(UUID.randomUUID().toString());
        callResultBean.setCallTime(new Date());
        callResultBean.setCalledSystemName(calledSystemName);
        callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
        callResultBean.setCallId(callBean.getCallId());
        return callResultBean;
    }
}
