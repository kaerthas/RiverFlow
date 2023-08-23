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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.mapper.XtApproveItemflowConfigMapper;
import com.inspur.workinfo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事项办理流程配置
 *
 * @author yunho code generator
 * @date 2023-07-12 11:06:41
 */
@Service
public class XtApproveItemflowConfigServiceImpl extends ServiceImpl<XtApproveItemflowConfigMapper, XtApproveItemflowConfig> implements XtApproveItemflowConfigService {

    @Autowired
    private XtApproveBusinessBaseService businessBaseService;
    @Autowired
    private XtApproveItemConfigService itemConfigService;
    @Autowired
    private XtApproveBusinessXmlConfigService businessXmlConfigService;
    @Autowired
    private XtApproveBusinessMaterialService businessMaterialService;

    @Override
    public Map<String, Object> getImportantXtMessage(XtApproveItemflowConfig itemflowConfig,String sblshshort) throws Exception {
        //创建一个map
        Map<String ,Object> map  = new HashMap<>();
        try {

            XtApproveBusinessBase businessBase = businessBaseService
                    .getOne(new QueryWrapper<XtApproveBusinessBase>().eq("SBLSH_SHORT",sblshshort));
            JSONObject businessBaseJson  = JSONObject.parseObject(JSON.toJSONString(businessBase));
            //将数据放到map中
            map.put(CommonConstants.XT_BUSINESS_BASE,businessBaseJson);

            //查询xml数据
            XtApproveItemConfig itemConfig  = itemConfigService.getOne(new QueryWrapper<XtApproveItemConfig>()
                    .eq("SXBM",itemflowConfig.getSxbm()));
            map.put(CommonConstants.XT_BUSINESS_ITEM,itemConfig);
            //根据模型id获取xmlData模板
            List<XtApproveBusinessXmlConfig> xmlConfigs  = businessXmlConfigService.getBaseMapper()
                    .selectList(new QueryWrapper<XtApproveBusinessXmlConfig>().eq("ITEM_ID",itemConfig.getItemId()));
            if (xmlConfigs!=null&&xmlConfigs.size()>0) {
                //拼接查询需要用到的参数放到map中
                Map<String, Object> params = new HashMap<>();
                String[] colums = new String[xmlConfigs.size()];
                for (int j = 0; j < xmlConfigs.size(); j++) {
                    if ("table".equals(xmlConfigs.get(j).getType())) {
                        //将表名插入map
                        params.put("tableName", xmlConfigs.get(j).getXmlCode());
                    } else if ("column".equals(xmlConfigs.get(j).getType())) {
                        //将字段插入数组
                        colums[j] = xmlConfigs.get(j).getXmlCode();
                    } else if ("keyword".equals(xmlConfigs.get(j).getType())) {
                        //将条件插入
                        params.put("keyword", xmlConfigs.get(j).getXmlCode());
                        params.put("keywordValue", sblshshort);
                    }
                }
                //循环结束将字段名数组插入map
                params.put("columns", colums);
                Map<String, Object> xmlMap = businessXmlConfigService.selectXmlDataByKeyWord(params);
                //传入xml 的JSONOBJECT
                JSONObject XMLObject = JSONObject.parseObject(JSON.toJSONString(xmlMap));
                map.put(CommonConstants.XT_BUSINESS_XML, XMLObject);
                //查询材料信息，传入map中
                List<XtApproveBusinessMaterial> businessMaterials   = businessMaterialService
                        .list(new QueryWrapper<XtApproveBusinessMaterial>().eq("SBLSH_SHORT",sblshshort));
                if (businessMaterials!=null && businessMaterials.size()>0){

                    JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(businessMaterials));
                    map.put(CommonConstants.XT_BUSINESS_FILE,jsonArray);

                }else{
                    map.put(CommonConstants.XT_BUSINESS_FILE,null);
                }

            }
        }catch (Exception e){
            throw e;
        }
        return map;
    }
}
