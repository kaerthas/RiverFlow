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

import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.mapper.XtApproveBusinessAcceptMapper;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.DateUtils;
import com.inspur.workinfo.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 协同调度受理信息表
 *
 * @author yunho code generator
 * @date 2023-07-13 09:23:20
 */
@Service
@Slf4j
public class XtApproveBusinessAcceptServiceImpl extends ServiceImpl<XtApproveBusinessAcceptMapper, XtApproveBusinessAccept> implements XtApproveBusinessAcceptService {

    private final Logger logger = LoggerFactory.getLogger(XtApproveBusinessAcceptServiceImpl.class);

    @Autowired
    private XtApproveBusinessCourseService businessCourseService;
    @Autowired
    private ApiDataColumnsExchangService columnsExchangService;
    @Autowired
    private XtApproveBusinessBaseService businessBaseService;
    @Autowired
    private PropertyConfig propertyConfig;
    @Autowired
    private ApproveCallService callService;
    @Autowired
    private ApproveCallResultService callResultService;

    @Override
    @Transactional
    public JSONObject saveFromTable(List<Map<String, Object>> list, XtApproveBusinessCourse detail,String tableId) {
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "	请求成功！");
        try {
            if (list!=null && list.size()==1){

                List<ApiDataColumnsExchang> columnsExchang = this.columnsExchangService.getBaseMapper().selectList(new QueryWrapper<ApiDataColumnsExchang>()
                        .eq("TABLE_ID",tableId)
                        .eq("COLUMNS_TYPE",CommonConstants.SELECT_COLUMN));
                //通过反射获取fieds
                XtApproveBusinessAccept businessAccept = new XtApproveBusinessAccept();//实体类

                //获取实体类 返回的是一个数组 数组的数据就是实体类中的字段
                Field[] fields = XtApproveBusinessAccept.class.getDeclaredFields();


                for (int i = 0; i < columnsExchang.size(); i++) {
                    //这个是，有的字段是用private修饰的 将他设置为可读
                    //TODO 缺少对返回结果进行格式化操作，后续完善通过script_id进行配置
                    //TODO 暂时分别写在每个实现中，最终可以抽出
                    for (int j = 0; j < fields.length; j++) {
                        fields[j].setAccessible(true);
//                        System.err.println(fields[j].getName()+":"+j);
                        //判断如果字段名称相等了
                        if (columnsExchang.get(i).getLocalColumns().equals(fields[j].getName())){
                            //取出map中参数
//                            System.out.println("!!!"+fields[j].getGenericType());
                            if (fields[j].getGenericType().toString().equals("class java.util.Date")){
                                fields[j].set(businessAccept, DateUtils.formatDate("yyyy-MM-dd HH:mm:ss",String.valueOf(list.get(0).get(columnsExchang.get(i).getBusinessColumns()))));
                            }else{
                                fields[j].set(businessAccept,list.get(0).get(columnsExchang.get(i).getBusinessColumns()));

                            }
                            break;
                        }
                    }
                }
                businessAccept.setSeqId(UUID.randomUUID().toString());
                //查询事项编码
                XtApproveBusinessBase businessBase =  businessBaseService.getOne(new QueryWrapper<XtApproveBusinessBase>().eq("SBLSH_SHORT",businessAccept.getSblshShort()));
                businessAccept.setSxbm(businessBase.getSxbm());
                //保存数据库并启动下一个流程
                this.baseMapper.insert(businessAccept);
                this.businessCourseService.analysisCourse(businessAccept.getSblshShort());
            }


            return result;
        }catch (Exception e){
            result.put("code", CommonConstants.API_FAIL);
            result.put("error",e.getMessage());
            return result;
        }
    }


    /**
     * 推送受理数据到调度系统
     * @return
     */
    @Override
    @Transactional
    public JSONObject sendBusinessAccept(XtApproveBusinessAccept businessAccept){
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("code", CommonConstants.API_SUCCESS);
        jsonResult.put("error", "");
        ApproveCall callBean=new ApproveCall();
        ApproveCallResult callResultBean=new ApproveCallResult();
        String callRestXml = "";

        try {
            JSONObject paramJson = new JSONObject();
            JSONObject txnCommCom = new JSONObject();
            paramJson.put("txnCommCom" , txnCommCom);
            JSONObject txnBodyCom = new JSONObject();
            txnBodyCom.put("sxbm" , businessAccept.getSxbm());
            txnBodyCom.put("sblshShort" , businessAccept.getSblshShort());
            JSONObject shouli = new JSONObject();
            shouli.put("sxbm" , businessAccept.getSxbm());
            shouli.put("sblshShort" , businessAccept.getSblshShort());
            String dispatchUrl =propertyConfig.getDispatchUrl();
            if(!dispatchUrl.endsWith("/")){
                dispatchUrl = dispatchUrl.concat("/");
            }
            String method = "___ddpt/ddpt/ddpt61004";
            HttpClientUtils httpClientUtils=new HttpClientUtils();
            String httpMethod="POST";

            //组装调用记录信息
            callBean.setBsnum(businessAccept.getSblshShort());
            callBean.setCalledSystemAddr(dispatchUrl + method);
            callBean.setCalledSystemCode("");
            callBean.setCalledSystemName("陕西协同调度系统");
            callBean.setCallId(UUID.randomUUID().toString());
            callBean.setCallParameter(httpMethod);
            callBean.setCallTime(new Date());
            callBean.setCallTimes(1);
            callBean.setInterfaceName("ddpt61004");
            callBean.setParameterValue(paramJson.toJSONString());
            //组装调用结果信息
            callResultBean.setSeqId(UUID.randomUUID().toString());
            callResultBean.setCallTime(new Date());
            callResultBean.setCalledSystemName(callBean.getCalledSystemName());
            callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
            callResultBean.setCallId(callBean.getCallId());
            SimpleDateFormat simpleFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            shouli.put("ywslsj"   , simpleFormat.format(businessAccept.getYwslsj()));
            shouli.put("ywslqhmc" , businessAccept.getYwslqhbm());
            shouli.put("ywslyj"   ,   businessAccept.getYwslyj());
            shouli.put("ywslbmmc" , businessAccept.getYwslbmmc());
            shouli.put("ywslrmc"  , businessAccept.getYwlsmc());
            shouli.put("ywslzt"   , businessAccept.getYwlszt());
            shouli.put("ywslbmbm" , businessAccept.getYwslbmbm());
            shouli.put("ywslqhbm" , businessAccept.getYwslqhbm());

            txnBodyCom.put("shouli" , shouli);
            paramJson.put("txnBodyCom" , txnBodyCom);
            callBean.setParameterValue(paramJson.toJSONString());
            callRestXml= httpClientUtils.sendJsonHttpPost(dispatchUrl+method,paramJson.toString());

            JSONObject txnBodyComJson = JSONObject.parseObject(callRestXml);
            //TODO 后续用注解抽出多余代码
            if(!"success".equals(txnBodyComJson.getString("C-Response-Desc"))){
                jsonResult.put("code", CommonConstants.API_FAIL);
                jsonResult.put("error", "接口返回失败");
                callBean.setCallState("0");
                callResultBean.setResultValue(callRestXml);
            }else {
                String bodyStr = txnBodyComJson.getString("C-Response-Body");
                JSONObject bodyJson = JSONObject.parseObject(bodyStr);
                if (!"00".equals(bodyJson.getString("code"))) {
                    jsonResult.put("code", CommonConstants.API_FAIL);
                    jsonResult.put("error", bodyJson.getString("msg"));
                    callBean.setCallState("0");
                    callResultBean.setResultValue(callRestXml);
                }else{
                    //3.判断业务是否推送成功,如果成功变更流程
                    businessCourseService.analysisCourse(businessAccept.getSblshShort()) ;
                    callBean.setCallState("1");
                    callResultBean.setResultValue(callRestXml);
                }
            }
            callResultBean.setCallState(callBean.getCallState());
            callService.saveOrUpdate(callBean);
            callResultService.saveOrUpdate(callResultBean);
        } catch (Exception e) {
            jsonResult.put("code", CommonConstants.API_FAIL);
            jsonResult.put("error", "调用失败" + e.getMessage());
            e.printStackTrace();
            try{
                callBean.setCallState("0");
                callResultBean.setCallState(callBean.getCallState());
                callResultBean.setResultValue("返回结果：" + callRestXml + "----------异常原因：" + e.getMessage());
                callService.saveOrUpdate(callBean);
                callResultService.saveOrUpdate(callResultBean);
            }catch (Exception e1){
                jsonResult.put("code", "300");
                jsonResult.put("error", "保存接口调用记录失败！" + e.getMessage());
                logger.error("保存接口调用记录失败！", e);
                e.printStackTrace();
            }
        }
        return jsonResult;
    }

    @Override
    @Transactional
    public JSONObject saveFormApi(JSONObject resObj,String sblshShort) {

        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "	请求成功！");
        try {
           if (resObj.get("code").equals(CommonConstants.API_SUCCESS)) {

               XtApproveBusinessAccept businessAccept = new XtApproveBusinessAccept();//实体类

               //获取实体类 返回的是一个数组 数组的数据就是实体类中的字段
               Field[] fields = XtApproveBusinessAccept.class.getDeclaredFields();
               for (int j = 0; j < fields.length; j++) {
                   fields[j].setAccessible(true);
//
                   if (fields[j].getGenericType().toString().equals("class java.util.Date")) {
                       fields[j].set(businessAccept, DateUtils.formatDate("yyyy-MM-dd HH:mm:ss", resObj.getString(fields[j].getName())));
                   } else {
                       fields[j].set(businessAccept, resObj.getString(fields[j].getName()));

                   }
               }
               businessAccept.setSeqId(UUID.randomUUID().toString());
               businessAccept.setSblshShort(sblshShort);
               //查询事项编码
               XtApproveBusinessBase businessBase = businessBaseService.getOne(new QueryWrapper<XtApproveBusinessBase>().eq("SBLSH_SHORT", businessAccept.getSblshShort()));
               businessAccept.setSxbm(businessBase.getSxbm());
               //保存数据库并启动下一个流程
               this.baseMapper.insert(businessAccept);
               this.businessCourseService.analysisCourse(sblshShort);
           }else{

               throw new Exception("未查询到受理信息，请等待");

           }


        }catch (Exception e){
            result.put("code", CommonConstants.API_FAIL);
            result.put("error",e.getMessage());
            return result;
        }
        return result;
    }
}
