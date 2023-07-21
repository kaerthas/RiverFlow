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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inspur.workinfo.entity.ApiDataTableExchange;
import com.inspur.workinfo.entity.XtApproveItemflowConfig;

/**
 * 库表交换绑定关系表
 *
 * @author yunho code generator
 * @date 2023-07-14 15:31:50
 */
public interface ApiDataTableExchangeService extends IService<ApiDataTableExchange> {

    JSONObject analysisDataExchange(XtApproveItemflowConfig itemflowConfig,String sblshShort);
}
