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
import com.inspur.workinfo.entity.TmzBzCremationInformation;

/**
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-01-09 12:26:25
 */
public interface TmzBzCremationInformationService extends IService<TmzBzCremationInformation> {

    JSONObject getInfoByIdAndName(String cardCode,String name);

}
