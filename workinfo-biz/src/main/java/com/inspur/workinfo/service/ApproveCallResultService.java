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

package com.inspur.workinfo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.inspur.workinfo.entity.ApproveCall;
import com.inspur.workinfo.entity.ApproveCallResult;

/**
 * 接口调用结果信息
 *
 * @author yunho code generator
 * @date 2023-07-07 17:04:52
 */
public interface ApproveCallResultService extends IService<ApproveCallResult> {

    ApproveCallResult createCallResultBean(ApproveCall callBean, String 浪潮政务服务通用审批平台);
}
