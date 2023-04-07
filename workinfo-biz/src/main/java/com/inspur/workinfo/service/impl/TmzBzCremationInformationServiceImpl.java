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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inspur.workinfo.entity.TmzBzCremationInformation;
import com.inspur.workinfo.mapper.TmzBzCremationInformationMapper;
import com.inspur.workinfo.service.TmzBzCremationInformationService;
import com.inspur.workinfo.util.SM4Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 *
 * @author yunho code generator
 * @date 2023-01-09 12:26:25
 */
@Slf4j
@Service
public class TmzBzCremationInformationServiceImpl extends ServiceImpl<TmzBzCremationInformationMapper, TmzBzCremationInformation> implements TmzBzCremationInformationService {

    public JSONObject getInfoByIdAndName(String cardCode,String name){
        String key = "K0xwRysrbGNwMVIBMWYrVj==";
        SimpleDateFormat sf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject info = new JSONObject();
        JSONObject result = new JSONObject();
        Boolean flag = false;
        try {
            String enCardCode = SM4Util.encrypt(key,cardCode);
            QueryWrapper<TmzBzCremationInformation> queryWrapper=
                    new QueryWrapper<TmzBzCremationInformation>()
                            .eq(StringUtils.hasText(name),"NAME",name)
                            .eq("CARD_CODE",enCardCode);
            List<TmzBzCremationInformation> list = baseMapper.selectList(queryWrapper);
            if (list!= null&&list.size()>0) {
                    TmzBzCremationInformation a = list.get(0);
                    result.put("birthDate",sf.format(a.getBirthDate()));
                    result.put("home",a.getHome());
                    result.put("deathCertNo",a.getDeathCertNo());
                    result.put("gender",a.getGender());
                    result.put("address",a.getAddress());
                    result.put("deathDate",sf.format(a.getDeathDate()));
                    result.put("registerPlace",a.getRegisterPlace());
                    result.put("crematPlace",a.getCrematPlace());
                    result.put("cremateDate",sf.format(a.getCremateDate()));
                    result.put("areaNumber",a.getAreaNumber());
                    result.put("updateAt",sf.format(a.getUpdateAt()));
                    result.put("code",a.getCode());
                    result.put("name",a.getName());
                    result.put("checkinPlacle",a.getCheckinPlacle());
                    flag = true;
                    info.put("result",result);
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.fluentPut("message",e.getMessage())
                    .fluentPut("flag",false);
        }
        info.put("flag",flag);
        if(flag){
            info.put("message","");
        }else {
            info.put("message","未查询到对应数据");
        }
        return info;
    }
}
