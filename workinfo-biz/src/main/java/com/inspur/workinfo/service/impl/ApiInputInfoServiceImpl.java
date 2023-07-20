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

import cn.hutool.json.XML;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.inspur.workinfo.entity.ApiInputInfo;
import com.inspur.workinfo.entity.ApiServiceCatalog;
import com.inspur.workinfo.mapper.ApiInputInfoMapper;
import com.inspur.workinfo.service.ApiInputInfoService;
import com.inspur.workinfo.service.ApiServiceCatalogService;
import com.inspur.workinfo.util.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-07-13 09:23:20
 */
@Service
public class ApiInputInfoServiceImpl extends ServiceImpl<ApiInputInfoMapper, ApiInputInfo> implements ApiInputInfoService {

    @Autowired
    ApiServiceCatalogService apiServiceCatalogService;
    @Autowired
    ApiInputInfoService apiInputInfoService;

    public JSONObject serviceHandle(String appId, String param,Map<String,Object> header){
        JSONObject result = new JSONObject();
        ApiServiceCatalog apiServiceCatalog = apiServiceCatalogService.getById(appId);
        String url = apiServiceCatalog.getUrl();
        if("proxy".equals(apiServiceCatalog.getType())){
            String requestType = apiServiceCatalog.getRequestType();//GET/POST/FORM
            String method = apiServiceCatalog.getMethod();
            String info = "";
            if("JSON".equals(requestType)) {
                if ("POST".equals(method)) {
                    info = HttpClientUtils.sendPostWithHeader(url,param,header);
                } else if ("GET".equals(method)) {
                    info = HttpClientUtils.sendGetWithHeader(url,JSON.parseObject(param),header);
                } else if ("FORM".equals(method)) {
                    info = HttpClientUtils.sendFormPostWithHeader(url,JSON.parseObject(param),header);
                } else {
                    result.put("state", "300");
                    result.put("msg", "未识别的接口类型");
                }
            }else if("XML".equals(requestType)){
                if ("POST".equals(method)) {
                    info = HttpClientUtils.postXmlRequest(url,param,header);
                }
            }else {
                result.put("state","300");
                result.put("msg","未找到对应请求类型");
            }
            if(!StringUtils.isEmpty(info)){
                result.put("state", "200");
                result.put("info", info);
            }else {
                result.put("state", "300");
                result.put("msg","请求出错");
            }
        }else{
            result.put("state","300");
            result.put("msg","未找到对应代理服务");
        }
        return result;
    }





    public JSONObject getServiceByString(String appId, String body){
        ApiServiceCatalog apiServiceCatalog = apiServiceCatalogService.getById(appId);
        String requestType = apiServiceCatalog.getRequestType();
        if("JSON".equals(requestType)||"XML".equals(requestType)) {
            JSONObject jsonObject = JSONObject.parseObject(body);
            JSONObject param = jsonObject.getJSONObject("param");
            JSONObject header = jsonObject.getJSONObject("header");
            return serviceHandle(appId,param.toString(),header);
        }else {
            return new JSONObject().fluentPut("state","300").fluentPut("msg","不支持的请求类型");
        }
    }

    public JSONObject getServiceByMap(String appId, Map<String,Object> params,Map<String,Object> headers){
        ApiServiceCatalog apiServiceCatalog = apiServiceCatalogService.getById(appId);
        String requestType = apiServiceCatalog.getRequestType();

        //全部输入
        QueryWrapper<ApiInputInfo> queryWrapperAll = new QueryWrapper<ApiInputInfo>()
                .eq("API_ID", appId);
        List<ApiInputInfo> apiInputInfoListAll = apiInputInfoService.list(queryWrapperAll);

        //body顶层参数
        QueryWrapper<ApiInputInfo> queryWrapper = new QueryWrapper<ApiInputInfo>()
                .eq("API_ID", appId)
                .eq("PARENT_ID","#")
                .eq("TYPE","NORMAL");
        List<ApiInputInfo> apiInputInfoList = apiInputInfoService.list(queryWrapper);
        JSONObject param = new JSONObject();
        for(int i = 0;i<apiInputInfoList.size();i++){
            String id = apiInputInfoList.get(i).getId();
            String key = apiInputInfoList.get(i).getKey();
            param.fluentPutAll(getChildrenNode(id,key,apiInputInfoListAll,params));
        }

        //header顶层参数
        QueryWrapper<ApiInputInfo> queryWrapperHeader = new QueryWrapper<ApiInputInfo>()
                .eq("API_ID", appId)
                .eq("PARENT_ID","#")
                .eq("TYPE","HEADER");
        List<ApiInputInfo> apiInputInfoListHeader = apiInputInfoService.list(queryWrapperHeader);
        Map<String,Object> header = new HashMap<>();
        for(int i = 0;i<apiInputInfoListHeader.size();i++){
            String id = apiInputInfoListHeader.get(i).getId();
            String key = apiInputInfoListHeader.get(i).getKey();
            header.putAll(getChildrenNode(id,key,apiInputInfoListAll,headers));
        }
        if("JSON".equals(requestType)) {
            return serviceHandle(appId,param.toString(),header);
        }else if("XML".equals(requestType)){
            String xmlParam = XML.toXml(param);
            return serviceHandle(appId,xmlParam,header);
        }else {
            return new JSONObject().fluentPut("state","300").fluentPut("msg","不支持的请求类型");
        }
    }




    /**
     * 递归获取子节点下的子节点
     * @return
     */
    private JSONObject getChildrenNode(String parentId,String key, List<ApiInputInfo> paramList,Map<String,Object> value) {
        JSONObject baseParams = new JSONObject();//针对叶节点
        JSONObject layerParams = new JSONObject();//针对叶节点
        Boolean flag = true;//判断是否是叶节点
        for (ApiInputInfo apiInputInfo: paramList) {
            if ("#".equals(apiInputInfo.getParentId())) {
                //跳过顶节点
                continue;
            }
            if (parentId.equals(apiInputInfo.getParentId())) {
                // 递归获取子节点下的子节点，即设置树控件中的children
                flag = false;
                JSONObject child = getChildrenNode(apiInputInfo.getId(),apiInputInfo.getKey(), paramList,value);
                baseParams.fluentPutAll(child);
            }
        }
        if(flag){
            //如果是叶节点
            baseParams.put(key,value.get(key));
            return baseParams;
        }else {
            layerParams.put(key,baseParams);
            return layerParams;
        }

    }





}
