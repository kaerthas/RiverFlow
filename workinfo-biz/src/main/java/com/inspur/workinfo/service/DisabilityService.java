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

package com.inspur.workinfo.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inspur.workinfo.entity.PreApasinfo;

/**
 * 登记（申报）信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:05
 */
public interface DisabilityService extends IService<PreApasinfo> {

    JSONObject getCities(String provinceid);

    JSONObject getCounty(String cityid);

    JSONObject getTown(String countyid);

    JSONObject applyCheck(String name,String idcard,String mobile,String provinceid,String cityid, String countyid,String townid);

    JSONObject upLoadImg(String docId,String fileName,String idCard);

    JSONObject submitApply(String name,String idcard,String mobile,String provinceid,String cityid, String countyid,String townid,String clientType);

    JSONObject cancelSubmit(String idcard,String onlineApplyId);

    JSONObject modifyApply(String mobile,String onlineApplyId,String cityid,String countyid,String townid);

    JSONObject getApplyProcess(String userName,String idCard,String provinceid);

}
