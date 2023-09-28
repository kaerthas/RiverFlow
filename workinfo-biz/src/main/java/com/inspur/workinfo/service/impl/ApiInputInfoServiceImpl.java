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

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.XML;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.google.gson.Gson;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.mapper.ApiInputInfoMapper;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.DateUtils;
import com.inspur.workinfo.util.HttpClientUtils;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.util.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.*;

import static com.inspur.workinfo.constant.CommonConstants.*;

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
    @Autowired
    ApiScriptInfoService apiScriptInfoService;
    @Autowired
    ApiOutputInfoService apiOutputInfoService;
    @Autowired
    GroovyService groovyService;
    @Autowired
    RedisCache redisCache;

    public R serviceHandle(String apiId, String param, Map<String,Object> header){
        JSONObject result = new JSONObject();
        ApiServiceCatalog apiServiceCatalog = apiServiceCatalogService.getById(apiId);
        String url = apiServiceCatalog.getUrl();
        String requestType = apiServiceCatalog.getRequestType();//GET/POST/FORM
        String method = apiServiceCatalog.getMethod();
        //增加互联网区正向代理模式
        boolean isInternet ="0".equals(apiServiceCatalog.getIsInternet())?false:true;



        String info = "";
        if(API_PROXY.equals(apiServiceCatalog.getType())){

            if("JSON".equals(requestType)) {
                if ("POST".equals(method)) {
                    info = HttpClientUtils.sendPostByHttpURLConnection(url,param,header,isInternet);
                } else if ("GET".equals(method)) {
                    info = HttpClientUtils.sendGetWithHeader(url,JSONObject.parseObject(param),header,isInternet);
                } else if ("FORM".equals(method)) {
                    info = HttpClientUtils.sendFormPostWithHeader(url,JSONObject.parseObject(param),header,isInternet);
                } else{
                    return R.failed("未识别的接口类型");
                }
            }else if("XML".equals(requestType)){
                if ("POST".equals(method)) {
                    info = HttpClientUtils.postXmlRequest(url,param,header,isInternet);
                }
            }else {
                return R.failed("未找到对应请求类型");
            }

        }else if(API_TOKEN.equals(apiServiceCatalog.getType())){
            if("JSON".equals(requestType)) {
                if ("FORM".equals(method)) {
                    info = HttpClientUtils.sendFormPostWithHeader(url,JSON.parseObject(param),header,isInternet);

                }else if ("POST".equals(method)) {
                    info = HttpClientUtils.sendPostWithHeader(url,param,header,isInternet);
                }
                 else {
                    return R.failed("未识别的接口类型");
                }
            }


        } else{
            return R.failed("未找到对应代理服务");
        }

        if(!StringUtils.isEmpty(info)){
            //查询结果处理脚本,如果有则对返回值进行处理
            QueryWrapper<ApiScriptInfo> queryWrapperAll = new QueryWrapper<ApiScriptInfo>()
                    .eq("API_ID", apiId)
                    .eq("RULE_CLASSIFY","RESULT");
            List<ApiScriptInfo> apiScriptInfoList = apiScriptInfoService.list(queryWrapperAll);
            if(apiScriptInfoList.size()>0){
                result = groovyService.invokeScript(apiScriptInfoList.get(0).getRuleScript(),info);
            }else{
                //根据反参定义直接处理
                result = JSONObject.parseObject(info);
            }
            return R.ok(result);
        }else {
            return R.failed("请求出错");
        }
    }





    public R getServiceByString(String apiId, String body){
        ApiServiceCatalog apiServiceCatalog = apiServiceCatalogService.getById(apiId);

        if("JSON".equals(apiServiceCatalog.getRequestType())
                ||"XML".equals(apiServiceCatalog.getRequestType())) {
            JSONObject jsonObject = JSONObject.parseObject(body);
            JSONObject param = jsonObject.getJSONObject("param");
            JSONObject header = jsonObject.getJSONObject("header");

            //FORMAT/HEADER/RESULT三类
            List<ApiScriptInfo> apiScriptInfoList = apiScriptInfoService
                    .list(new QueryWrapper<ApiScriptInfo>().eq("API_ID",apiId));


            //处理param参数
            QueryWrapper<ApiInputInfo> queryWrapperNormal = new QueryWrapper<ApiInputInfo>()
                    .eq("API_ID", apiId)
                    .eq("PARENT_ID","#")
                    .eq("TYPE",API_INPUT_NORMAL);
            List<ApiInputInfo> apiInputInfoListNormal = apiInputInfoService.list(queryWrapperNormal);
            JSONObject paramIn = new JSONObject();
            for(ApiInputInfo apiInputInfo:apiInputInfoListNormal){
                //首先判断是否为常数
                if ("1".equals(apiInputInfo.getIsConstant())){
                    paramIn.put(apiInputInfo.getKey(),apiInputInfo.getValue());
                }else{
                    if (param!=null) {
                        paramIn.put(apiInputInfo.getKey(), param.getString(apiInputInfo.getKey()));
                    }
                }
            }

            //处理header参数
            QueryWrapper<ApiInputInfo> queryWrapperHeader = new QueryWrapper<ApiInputInfo>()
                    .eq("API_ID", apiId)
                    .eq("PARENT_ID","#")
                    .eq("TYPE",API_INPUT_HEADER);
            List<ApiInputInfo> apiInputInfoListHeader = apiInputInfoService.list(queryWrapperHeader);
            JSONObject headerIn = new JSONObject();
            for(ApiInputInfo apiInputInfo:apiInputInfoListHeader){
                //首先判断是否为常数
                if ("1".equals(apiInputInfo.getIsConstant())){
                    //在判断是否绑定缓存中的数据
                    if (StrUtil.isBlank(apiInputInfo.getValue())&&StrUtil.isNotBlank(apiInputInfo.getTokenApiId())){

                        headerIn.put(apiInputInfo.getKey(),redisCache.getCacheObject(BACK_END_PROJECT+"_"+apiInputInfo.getTokenApiId()+"_"+apiInputInfo.getKey()));
                    }else{

                        headerIn.put(apiInputInfo.getKey(),apiInputInfo.getValue());
                    }

                }else{
                    if (header!=null) {
                        headerIn.put(apiInputInfo.getKey(), header.getString(apiInputInfo.getKey()));
                    }
                }

            }
            //TODO 暂时未对参数进行处理后期处理
            //查询所有脚本,后期构建有点重复代码
            for(int i = 0;i<apiScriptInfoList.size();i++){
                String scriptType = apiScriptInfoList.get(i).getRuleClassify();
                String scriptContent = apiScriptInfoList.get(i).getRuleScript();
                if("FORMAT".equals(scriptType)){
                    param = groovyService.invokeScript(scriptContent,paramIn.toJSONString());
                }else if("HEADER".equals(scriptType)){
                    header = groovyService.invokeScript(scriptContent,headerIn.toJSONString());
                }
            }
            return serviceHandle(apiId,param.toString(),header);
        }else {
            return R.failed("不支持的请求类型");
        }
    }

    public R getServiceByMap(String apiId, Map<String,Object> params,Map<String,Object> headers){
        ApiServiceCatalog apiServiceCatalog = apiServiceCatalogService.getById(apiId);
        String requestType = apiServiceCatalog.getRequestType();

        //全部输入
        QueryWrapper<ApiInputInfo> queryWrapperAll = new QueryWrapper<ApiInputInfo>()
                .eq("API_ID", apiId);
        List<ApiInputInfo> apiInputInfoListAll = apiInputInfoService.list(queryWrapperAll);

        //body顶层参数
        QueryWrapper<ApiInputInfo> queryWrapper = new QueryWrapper<ApiInputInfo>()
                .eq("API_ID", apiId)
                .eq("PARENT_ID","#")
                .eq("TYPE",API_INPUT_NORMAL);
        List<ApiInputInfo> apiInputInfoList = apiInputInfoService.list(queryWrapper);
        JSONObject param = new JSONObject();
        for(int i = 0;i<apiInputInfoList.size();i++){
            String id = apiInputInfoList.get(i).getId();
            String key = apiInputInfoList.get(i).getKey();
            param.fluentPutAll(getChildrenNode(id,key,apiInputInfoListAll,params));
        }

        //header顶层参数
        QueryWrapper<ApiInputInfo> queryWrapperHeader = new QueryWrapper<ApiInputInfo>()
                .eq("API_ID", apiId)
                .eq("PARENT_ID","#")
                .eq("TYPE",API_INPUT_HEADER);
        List<ApiInputInfo> apiInputInfoListHeader = apiInputInfoService.list(queryWrapperHeader);
        Map<String,Object> header = new HashMap<>();
        for(int i = 0;i<apiInputInfoListHeader.size();i++){
            String id = apiInputInfoListHeader.get(i).getId();
            String key = apiInputInfoListHeader.get(i).getKey();
            header.putAll(getChildrenNode(id,key,apiInputInfoListAll,headers));
        }




        if("JSON".equals(requestType)) {
            return serviceHandle(apiId,param.toString(),header);
        }else if("XML".equals(requestType)){
            String xmlParam = XML.toXml(param);
            return serviceHandle(apiId,xmlParam,header);
        }else {
            return R.failed("不支持的请求类型");
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


    public R getServiceByMap(String apiId, Map<String,Object> params){
        ApiServiceCatalog apiServiceCatalog = apiServiceCatalogService.getById(apiId);
        String requestType = apiServiceCatalog.getRequestType();

        //查询所有脚本
        QueryWrapper<ApiScriptInfo> queryWrapperAll = new QueryWrapper<ApiScriptInfo>()
                .eq("API_ID", apiId);
        //FORMAT/HEADER/RESULT三类
        List<ApiScriptInfo> apiScriptInfoList = apiScriptInfoService.list(queryWrapperAll);

        //header顶层参数
        QueryWrapper<ApiInputInfo> queryWrapperHeader = new QueryWrapper<ApiInputInfo>()
                .eq("API_ID", apiId)
                .eq("TYPE","HEADER");
        List<ApiInputInfo> apiInputInfoListHeader = apiInputInfoService.list(queryWrapperHeader);
        Map<String,Object> headerParam = new HashMap<>();
        for(int i = 0;i<apiInputInfoListHeader.size();i++){
            String value = apiInputInfoListHeader.get(i).getValue();
            String key = apiInputInfoListHeader.get(i).getKey();
            String tokenApiId = apiInputInfoListHeader.get(i).getTokenApiId();
            headerParam.put(key,value);
            if(!StringUtils.isEmpty(tokenApiId)){
                String cacheValue = redisCache.getCacheObject(BACK_END_PROJECT+"_"+tokenApiId+"_"+key);
                if(!StringUtils.isEmpty(cacheValue)) {
                    headerParam.put(key, cacheValue);
                }
            }
        }
        //参数转换
        JSONObject param = new JSONObject(params);
        JSONObject header = new JSONObject(headerParam);
        //查询所有入参 TODO 目前没遇到嵌套的入参形式，遇到了在做处理
        QueryWrapper<ApiInputInfo> queryWrapperParam = new QueryWrapper<ApiInputInfo>()
                .eq("API_ID", apiId)
                .eq("PARENT_ID","#")
                .eq("TYPE",API_INPUT_NORMAL);
        List<ApiInputInfo> apiInputInfoListParams = apiInputInfoService.list(queryWrapperParam);
        if (apiInputInfoListParams!=null&&apiInputInfoListParams.size()>0){
            for (int k = 0; k <apiInputInfoListParams.size() ; k++) {
                //判断是否为常量,常量则放入
                if ("1".equals(apiInputInfoListParams.get(k).getIsConstant()))
                    param.put(apiInputInfoListParams.get(k).getKey(),apiInputInfoListParams.get(k).getValue());
            }
        }


        for(int i = 0;i<apiScriptInfoList.size();i++){
            String scriptType = apiScriptInfoList.get(i).getRuleClassify();
            String scriptContent = apiScriptInfoList.get(i).getRuleScript();
            if("FORMAT".equals(scriptType)){
                param = groovyService.invokeScript(scriptContent,param.toString());
            }else if("HEADER".equals(scriptType)){
                header = groovyService.invokeScript(scriptContent,header.toString());
            }
        }

        if("JSON".equals(requestType)) {
            //将SCRIPT 动态填充到
            return serviceHandle(apiId,param.toString(),header);
        }else if("XML".equals(requestType)){
            //对xml类型参数进行处理
            QueryWrapper<ApiInputInfo> queryWrapper = new QueryWrapper<ApiInputInfo>()
                    .eq("API_ID", apiId)
                    .eq("PARENT_ID","#")
                    .eq("TYPE",API_INPUT_SCRIPT);
            //xml参数来说只有一个body
            List<ApiInputInfo> inputInfolist =  apiInputInfoService.list(queryWrapper);
            if(inputInfolist!=null&&inputInfolist.size()==1){

                String xml  = param.getString(inputInfolist.get(0).getKey());

                return serviceHandle(apiId,xml,header);
            }else{
                return R.failed("找到多条，请配置唯一值作为xml的传参!");
            }

        }else {
            return R.failed("不支持的请求类型");
        }
    }





}
