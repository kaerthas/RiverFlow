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
import com.inspur.workinfo.entity.XtApproveBusinessinfo;

/**
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-07-07 17:04:51
 */
public interface XtApproveBusinessinfoService extends IService<XtApproveBusinessinfo> {

    JSONObject getBusinessApplyData(String receiveNumber , String sxbm);

    JSONObject analysisApplyData(String sxbm,String applyXmlStr) throws Exception;


    JSONObject getBusiApplyMaterial(String sblshShort, String sxbm, String ywlx);

    JSONObject analysisMaterial(String materialXmlStr, String sblshShort);

}
