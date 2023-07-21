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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.mapper.XtApproveBusinessOutcomeMapper;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.DateUtils;
import com.inspur.workinfo.util.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.lang.reflect.Field;
import java.util.*;

/**
 * 业务信息结果物存量接口
 *
 * @author yunho code generator
 * @date 2023-07-17 17:50:43
 */
@Service
public class XtApproveBusinessOutcomeServiceImpl extends ServiceImpl<XtApproveBusinessOutcomeMapper, XtApproveBusinessOutcome> implements XtApproveBusinessOutcomeService {

    private final Logger logger = LoggerFactory.getLogger(XtApproveBusinessOutcomeServiceImpl.class);

    @Autowired
    private XtApproveBusinessBaseService businessBaseService;
    @Autowired
    private XtApproveBusinessCourseService businessCourseService;
    @Autowired
    private ApproveCallService callService;
    @Autowired
    private ApproveCallResultService callResultService;
    @Autowired
    private ApiDataColumnsExchangService columnsExchangService;

    @Autowired
    private PropertyConfig propertyConfig;

    @Override
    @Transactional
    public void saveFromTable(List<Map<String, Object>> list, XtApproveBusinessCourse detail, String tableId) {
        try {

            List<XtApproveBusinessOutcome> businessOutcomes  = new ArrayList<>();
            if (list!=null && list.size()>0){
                for (Map<String, Object> map:
                                    list) {
                List<ApiDataColumnsExchang> columnsExchang = this.columnsExchangService.getBaseMapper().selectList(new QueryWrapper<ApiDataColumnsExchang>()
                        .eq("TABLE_ID",tableId)
                        .in("COLUMNS_TYPE",new String[]{CommonConstants.SELECT_COLUMN,CommonConstants.SELECT_CONSTANTS}));
                //通过反射获取fieds
                XtApproveBusinessOutcome businessOutcome = new XtApproveBusinessOutcome();//实体类

                //获取实体类 返回的是一个数组 数组的数据就是实体类中的字段
                Field[] fields = XtApproveBusinessOutcome.class.getDeclaredFields();


                for (int i = 0; i < columnsExchang.size(); i++) {
                    //增加常数项插入
                    if (CommonConstants.SELECT_COLUMN.equals(columnsExchang.get(i).getColumnsType())) {

                        //这个是，有的字段是用private修饰的 将他设置为可读
                        //TODO 缺少对返回结果进行格式化操作，后续完善通过script_id进行配置
                        //TODO 暂时分别写在每个实现中，最终可以抽出
                        for (int j = 0; j < fields.length; j++) {
                            fields[j].setAccessible(true);
                            //                        System.err.println(fields[j].getName()+":"+j);
                            //判断如果字段名称相等了
                            if (columnsExchang.get(i).getLocalColumns().equals(fields[j].getName())) {
                                //取出map中参数
                                //                            System.out.println("!!!"+fields[j].getGenericType());
                                if (fields[j].getGenericType().toString().equals("class java.util.Date")) {
                                    fields[j].set(businessOutcome, DateUtils.formatDate("yyyy-MM-dd HH:mm:ss"
                                            ,String.valueOf(map.get(columnsExchang.get(i).getBusinessColumns()))));
                                } else {
                                    fields[j].set(businessOutcome,columnsExchang.get(i).getRemark());

                                }
                                break;
                            }
                        }
                    }else{
                        //常数项插入CommonConstants.SELECT_CONSTANTS Remark作为字段的值插入 important!!!
                        for (int j = 0; j < fields.length; j++) {
                            fields[j].setAccessible(true);
                            //System.err.println(fields[j].getName()+":"+j);
                            //判断如果字段名称相等了
                            if (columnsExchang.get(i).getLocalColumns().equals(fields[j].getName())) {
                                //取出map中参数
                                if (fields[j].getGenericType().toString().equals("class java.util.Date")) {
                                    fields[j].set(businessOutcome, DateUtils.formatDate("yyyy-MM-dd HH:mm:ss"
                                            ,columnsExchang.get(i).getRemark()));
                                } else {
                                    fields[j].set(businessOutcome, map.get(columnsExchang.get(i).getBusinessColumns()));

                                }
                                break;
                            }
                        }
                    }
                }
                businessOutcome.setSeqId(UUID.randomUUID().toString());
                //查询事项编码
                XtApproveBusinessBase businessBase =  businessBaseService
                        .getOne(new QueryWrapper<XtApproveBusinessBase>()
                                .eq("SBLSH_SHORT",detail.getSblshShort()));
                businessOutcome.setSblshShort(businessBase.getSblshShort());
                //保存数据库并启动下一个流程
                businessOutcomes.add(businessOutcome);


                }
                this.saveBatch(businessOutcomes,businessOutcomes.size());
                this.businessCourseService.analysisCourse(detail.getSblshShort());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void sendBusinessOutCome(List<XtApproveBusinessOutcome> businessOutcomeList) {
        /**
         * 推送结果物数据到调度系统
         * @return
         */
            ApproveCall callBean=new ApproveCall();
            ApproveCallResult callResultBean=new ApproveCallResult();
            String callRestXml = "";

            try {
                //查询基本信息
                XtApproveBusinessBase businessBase = businessBaseService.getOne(new QueryWrapper<XtApproveBusinessBase>()
                        .eq("SBLSH_SHORT",businessOutcomeList.get(0).getSblshShort()));
                JSONObject paramJson = new JSONObject();
                JSONObject txnCommCom = new JSONObject();
                paramJson.put("txnCommCom" , txnCommCom);
                JSONObject txnBodyCom = new JSONObject();
                txnBodyCom.put("sxbm" , businessBase.getSxbm());
                txnBodyCom.put("sblshShort" , businessOutcomeList.get(0).getSblshShort());
                JSONArray spshenpijieguo = new JSONArray();


                String dispatchUrl =propertyConfig.getDispatchUrl();
                if(!dispatchUrl.endsWith("/")){
                    dispatchUrl = dispatchUrl.concat("/");
                }
                String method = "___ddpt/ddpt/ddpt61009";
                HttpClientUtils httpClientUtils=new HttpClientUtils();
                String httpMethod="POST";

                //组装调用记录信息
                callBean.setBsnum(businessBase.getSblshShort());
                callBean.setCalledSystemAddr(dispatchUrl + method);
                callBean.setCalledSystemCode("");
                callBean.setCalledSystemName("陕西协同调度系统");
                callBean.setCallId(UUID.randomUUID().toString());
                callBean.setCallParameter(httpMethod);
                callBean.setCallTime(new Date());
                callBean.setCallTimes(1);
                callBean.setInterfaceName("ddpt61009");
                callBean.setParameterValue(paramJson.toJSONString());
                //组装调用结果信息
                callResultBean.setSeqId(UUID.randomUUID().toString());
                callResultBean.setCallTime(new Date());
                callResultBean.setCalledSystemName(callBean.getCalledSystemName());
                callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
                callResultBean.setCallId(callBean.getCallId());

                txnBodyCom.put("spshenpijieguo" , spshenpijieguo);
                paramJson.put("txnBodyCom" , txnBodyCom);
                callBean.setParameterValue(paramJson.toJSONString());
                //判断
                if (businessOutcomeList!=null &&businessOutcomeList.size()>0){
                    //循环储存数据到JsonArrary中
                    for (int i = 0; i <businessOutcomeList.size() ; i++) {
                        JSONObject jsonObject  = new JSONObject();
                        jsonObject.put("seqId",businessOutcomeList.get(i).getSeqId());
                        jsonObject.put("wjlx",businessOutcomeList.get(i).getWjlx());
                        jsonObject.put("attachName",businessOutcomeList.get(i).getAttachName());
                        jsonObject.put("attachBody",businessOutcomeList.get(i).getAttachBody());
                        jsonObject.put("attachPath",businessOutcomeList.get(i).getAttachPath());
                        //保存在材料数组中
                        spshenpijieguo.add(jsonObject);

                    }

                    callRestXml= httpClientUtils.sendJsonHttpPost(dispatchUrl+method,paramJson.toString());

                    JSONObject txnBodyComJson = JSONObject.parseObject(callRestXml);
                    //TODO 后续用注解抽出多余代码
                    if(!"success".equals(txnBodyComJson.getString("C-Response-Desc"))){
                        callBean.setCallState("0");
                        callResultBean.setResultValue(callRestXml);
                    }else {
                        String bodyStr = txnBodyComJson.getString("C-Response-Body");
                        JSONObject bodyJson = JSONObject.parseObject(bodyStr);
                        if (!"00".equals(bodyJson.getString("code"))) {
                            callBean.setCallState("0");
                            callResultBean.setResultValue(callRestXml);
                        }else{
                            //3.判断业务是否推送成功,如果成功变更流程
                            businessCourseService.analysisCourse(businessBase.getSblshShort()) ;
                            callBean.setCallState("1");
                            callResultBean.setResultValue(callRestXml);
                        }
                    }
                    callResultBean.setCallState(callBean.getCallState());
                    callService.saveOrUpdate(callBean);
                    callResultService.saveOrUpdate(callResultBean);


                }else{

                    callBean.setCallState("0");
                    callResultBean.setCallState(callBean.getCallState());
                    callResultBean.setResultValue("结果物材料为空，请联系运维人员处理");
                    callService.saveOrUpdate(callBean);
                    callResultService.saveOrUpdate(callResultBean);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try{
                    callBean.setCallState("0");
                    callResultBean.setCallState(callBean.getCallState());
                    callResultBean.setResultValue("返回结果：" + callRestXml + "----------异常原因：" + e.getMessage());
                    callService.saveOrUpdate(callBean);
                    callResultService.saveOrUpdate(callResultBean);
                }catch (Exception e1){
                    logger.error("保存接口调用记录失败！", e);
                    e.printStackTrace();
                }
            }
    }
}
