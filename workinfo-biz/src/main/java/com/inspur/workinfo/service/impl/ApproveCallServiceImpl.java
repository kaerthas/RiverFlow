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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.ApproveCall;
import com.inspur.workinfo.mapper.ApproveCallMapper;
import com.inspur.workinfo.service.ApproveCallService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * 接口调用信息
 *
 * @author yunho code generator
 * @date 2023-07-07 17:04:52
 */
@Service
public class ApproveCallServiceImpl extends ServiceImpl<ApproveCallMapper, ApproveCall> implements ApproveCallService {

    @Override
    public ApproveCall createCallBean(String bsnum, String url, String param, String systemName, String method, String interfaceName) {
        ApproveCall callBean=new ApproveCall();
        try {
            callBean.setCallState(CommonConstants.API_SUCCESS);
            callBean.setBsnum(bsnum);
            callBean.setCalledSystemAddr(url);
            callBean.setCalledSystemName(systemName);
            callBean.setCallId(UUID.randomUUID().toString());
            callBean.setCallParameter(method);
            callBean.setCallTime(new Date());
            callBean.setCallTimes(1);
            callBean.setInterfaceName(interfaceName);
            callBean.setParameterValue(param);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return callBean;
    }
}
