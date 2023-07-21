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
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.entity.XtApproveBusinessOutcome;

import java.util.List;
import java.util.Map;

/**
 * 业务信息结果物存量接口
 *
 * @author yunho code generator
 * @date 2023-07-17 17:50:43
 */
public interface XtApproveBusinessOutcomeService extends IService<XtApproveBusinessOutcome> {

    void sendBusinessOutCome(List<XtApproveBusinessOutcome> businessOutcomeList);

    void saveFromTable(List<Map<String, Object>> list, XtApproveBusinessCourse detail, String tableId);
}
