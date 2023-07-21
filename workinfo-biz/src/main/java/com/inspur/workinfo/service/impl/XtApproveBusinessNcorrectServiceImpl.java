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
import com.inspur.workinfo.entity.ApproveCall;
import com.inspur.workinfo.entity.ApproveCallResult;
import com.inspur.workinfo.entity.XtApproveBusinessBase;
import com.inspur.workinfo.entity.XtApproveBusinessNcorrect;
import com.inspur.workinfo.mapper.XtApproveBusinessNcorrectMapper;
import com.inspur.workinfo.service.ApproveCallResultService;
import com.inspur.workinfo.service.ApproveCallService;
import com.inspur.workinfo.service.XtApproveBusinessBaseService;
import com.inspur.workinfo.service.XtApproveBusinessNcorrectService;
import com.inspur.workinfo.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * 业务系统补齐补正告知
 *
 * @author yunho code generator
 * @date 2023-07-18 14:14:53
 */
@Service
@Slf4j
public class XtApproveBusinessNcorrectServiceImpl extends ServiceImpl<XtApproveBusinessNcorrectMapper, XtApproveBusinessNcorrect> implements XtApproveBusinessNcorrectService {

    private Logger logger  = LoggerFactory.getLogger(XtApproveBusinessNcorrectServiceImpl.class);
    @Autowired
    private ApproveCallResultService callResultService;
    @Autowired
    private ApproveCallService callService;
    @Autowired
    private XtApproveBusinessBaseService businessBaseService;
    @Autowired
    private PropertyConfig propertyConfig;
    /**
     * 推送补齐补正数据到调度系统
     * @return
     */
    @Override
    public void sendNeedCorrectionData(List<XtApproveBusinessNcorrect> businessNcorrectList,String sblsh){
        //接口调用记录表
        ApproveCall callBean=new ApproveCall();
        ApproveCallResult callResultBean=new ApproveCallResult();
        String callRestXml = "";

        //获取办件基本信息
        XtApproveBusinessBase businessBase =  businessBaseService
                .getOne(new QueryWrapper<XtApproveBusinessBase>().eq("SBLSH_SHORT",sblsh));
        try {
            JSONObject paramJson = new JSONObject();
            JSONObject txnCommCom = new JSONObject();
            paramJson.put("txnCommCom" , txnCommCom);
            JSONObject txnBodyCom = new JSONObject();
            txnBodyCom.put("sxbm" , businessBase.getSxbm());
            txnBodyCom.put("sblshShort" , businessBase.getSblshShort());
            JSONObject spbuzhenggaozhi = new JSONObject();
//            spbuzhenggaozhi.put("sxbm" , itemCode);
//            spbuzhenggaozhi.put("sblshShort" , businessBean.getBsNum());
            String dispatchUrl = propertyConfig.getDispatchUrl();
            if(!dispatchUrl.endsWith("/")){
                dispatchUrl = dispatchUrl.concat("/");
            }
            String method = "___ddpt/ddpt/ddpt61005";
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
            callBean.setInterfaceName("ddpt61005");
            callBean.setParameterValue(paramJson.toJSONString());
            //组装调用结果信息
            callResultBean.setSeqId(UUID.randomUUID().toString());
            callResultBean.setCallTime(new Date());
            callResultBean.setCalledSystemName(callBean.getCalledSystemName());
            callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
            callResultBean.setCallId(callBean.getCallId());

            //拼接参数
            if (businessNcorrectList.size()>0){
                StringBuilder materialNames   =  new StringBuilder();
                StringBuilder materialNumbers  =  new StringBuilder();
                for (int i = 0; i <businessNcorrectList.size() ; i++) {
                    materialNames.append(businessNcorrectList.get(i).getBzclqd()).append(";");
                    materialNumbers.append(businessNcorrectList.get(i).getBqbzclbm()).append(";");

                }
                //去掉末尾多余拼接
                materialNames.deleteCharAt(materialNames.lastIndexOf(";"));
                materialNumbers.deleteCharAt(materialNumbers.lastIndexOf(";"));
                //材料清单
                spbuzhenggaozhi.put("bzclqd",materialNames.toString());

                spbuzhenggaozhi.put("bqbzclbm",materialNumbers.toString());
                //办结时间
                spbuzhenggaozhi.put("bzgzsj",businessNcorrectList.get(0).getBzgzsj());
                //补正发起人姓名
                spbuzhenggaozhi.put("bzgzfcrxm",businessNcorrectList.get(0).getBzgzfcrxm());
                //补正告知原因
                spbuzhenggaozhi.put("bzgzyy",businessNcorrectList.get(0).getBzgzyy());
                //补正限制时间
                spbuzhenggaozhi.put("bzgzsx",businessNcorrectList.get(0).getBzgzsx());
                //补正告知时限单位 G 工作日 Z 自然日
                spbuzhenggaozhi.put("bzgzsxdw",businessNcorrectList.get(0).getBzgzsxdw());
                //补正区划代码
                spbuzhenggaozhi.put("xzqhdm",businessNcorrectList.get(0).getXzqhdm());


                txnBodyCom.put("spbuzhenggaozhi" , spbuzhenggaozhi);
                paramJson.put("txnBodyCom" , txnBodyCom);
                callBean.setParameterValue(paramJson.toJSONString());
                callRestXml= httpClientUtils.sendJsonHttpPost(dispatchUrl+method,paramJson.toString());

                JSONObject txnBodyComJson = JSONObject.parseObject(callRestXml);
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
                callResultBean.setResultValue("返回结果：数据库不存在该办件的补正告知信息");
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
