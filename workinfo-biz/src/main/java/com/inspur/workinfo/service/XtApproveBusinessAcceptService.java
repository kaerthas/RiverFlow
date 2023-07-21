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
import com.inspur.workinfo.entity.XtApproveBusinessAccept;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;

import java.util.List;
import java.util.Map;

/**
 * 协同调度受理信息表
 *
 * @author yunho code generator
 * @date 2023-07-13 09:23:20
 */
public interface XtApproveBusinessAcceptService extends IService<XtApproveBusinessAccept> {

    JSONObject saveFromTable(List<Map<String, Object>> list, XtApproveBusinessCourse detail,String tableId);

    //推送受理
    JSONObject sendBusinessAccept(XtApproveBusinessAccept businessAccept);
}
