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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.mapper.XtApproveBusinessinfoMapper;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.DateUtils;
import com.inspur.workinfo.util.HttpClientUtils;
import com.inspur.workinfo.util.XmlHandleUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-07-07 17:04:51
 */
@Service
@Slf4j
public class XtApproveBusinessinfoServiceImpl extends ServiceImpl<XtApproveBusinessinfoMapper, XtApproveBusinessinfo> implements XtApproveBusinessinfoService {

    private Logger logger = LoggerFactory.getLogger(XtApproveBusinessinfoServiceImpl.class);

    @Autowired
    private PropertyConfig propertyConfig;

    @Autowired
    private XtApproveItemConfigService xtApproveItemConfigService;

    @Autowired
    private XtApproveBusinessBaseService xtApproveBusinessBaseService;

    @Autowired
    private XtApproveBusinessEmailService xtApproveBusinessEmailService;

    @Autowired
    private XtApproveBusinessMaterialService materialService;

    @Autowired
    private XtApproveExchangeService exchangeService;

    @Autowired
    private XtApproveBusinessXmlConfigService xmlConfigService;

    @Autowired
    private XtApproveBusinessSpecialService businessSpecialService;


    /**
     * 根据业务受理编号获取业务申办，业务预受理，业务受理数据
     * @return
     */
    @Override
    public JSONObject getBusinessApplyData(String receiveNumber , String sxbm)
    {
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "请求成功！");
        JSONObject paramJson = new JSONObject();
        JSONObject txnCommCom = new JSONObject();
        paramJson.put("txnCommCom" , txnCommCom);
        JSONObject txnBodyCom = new JSONObject();
        txnBodyCom.put("sblshShort" , receiveNumber);
        txnBodyCom.put("sxbm" , sxbm);
        paramJson.put("txnBodyCom" , txnBodyCom);
        String dispatchUrl =propertyConfig.getDispatchUrl();
        if(!dispatchUrl.endsWith("/")){
            dispatchUrl = dispatchUrl.concat("/");
        }
        String method = "___ddpt/ddpt/ddpt61002";
        HttpClientUtils httpClientUtils=new HttpClientUtils();
        String callRest= httpClientUtils.sendJsonHttpPost(dispatchUrl+method,paramJson.toString());
        if(logger.isInfoEnabled()) {
            logger.info("61002返回："+callRest);
        }
        if(StrUtil.isBlank(callRest)){
            result.put("code",  CommonConstants.API_SUCCESS);
            result.put("error","未调用到接口,请检查接口地址是否正确！");
            return result;
        }else{
            result.put("xmlStr", callRest);
        }
        return result;
    }
    /****
     * 数据分析，并保存相关库表，采用sql语句的方式动态插入
     * ***/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JSONObject analysisApplyData(String sxbm,String applyXmlStr) throws Exception {
        //初始化返回值
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "请求成功！");
        try {


            //第一步根据是事项编码查询，事项垂管id
            XtApproveItemConfig xtApproveItemConfig = xtApproveItemConfigService.getOne(new QueryWrapper<XtApproveItemConfig>()
                    .eq("sxbm",sxbm));

            String  itemId  = xtApproveItemConfig.getItemId();
            String  serviceObj = xtApproveItemConfig.getServiceObj();
            if (StrUtil.isNotBlank(itemId)){
                //开始分析
                JSONObject  applyJsonData  =this.analysisApplyData(applyXmlStr);

                JSONObject applyAcceptData = applyJsonData.getJSONObject("shenqin");
                JSONObject acceptData = applyJsonData.getJSONObject("shouli");
                JSONObject mailData   = applyJsonData.getJSONObject("mail");
                JSONObject personData = applyJsonData.getJSONObject("person");
                JSONObject companyData = applyJsonData.getJSONObject("company");

                //第二步 插入基本信息表，批次表，调用定时调用批次表 分发配置表redis 组装模型
                //创建基本信息表  applyAcceptData
                // TODO 后续应该按照接口对返回参数进行可配置，目前对接协同写死
                /*******************************************保存基本表*********************************************/
                XtApproveBusinessBase businessBase = new XtApproveBusinessBase();
                String  baseInfoId  =   UUID.randomUUID().toString();

                businessBase.setSeqId(baseInfoId);//绑定其他业务信息
                businessBase.setSblshShort(applyAcceptData.getString("sblshShort"));
                businessBase.setSxbm(sxbm);
                businessBase.setSxmc(applyAcceptData.getString("sxmc"));
                businessBase.setSxqxbm(applyAcceptData.getString("sxqxbm"));
                businessBase.setXzqhdm(applyAcceptData.getString("xzqhdm"));
                businessBase.setBmmc(applyAcceptData.getString("bmmc"));
                businessBase.setBmzzjgdm(applyAcceptData.getString("bmzzjgdm"));
                businessBase.setYwly(applyJsonData.getString("ywly"));//业务来源
                businessBase.setSbsj(applyAcceptData.getDate("sbsj"));


                //个人存入人员信息
                if("0".equals(serviceObj)){
                    businessBase.setServiceObj(serviceObj);
                    businessBase.setGrName(personData.getString("name"));
                    businessBase.setGrIdcardno(personData.getString("idcardno"));
                    businessBase.setGrIdentitytype(personData.getString("identitytype"));
                    businessBase.setGrLinkphone(personData.getString("linkphone"));
                }
                //法人存入法人信息
                if("1".equals(serviceObj)){
                    businessBase.setServiceObj(serviceObj);
                    businessBase.setQyOrgName(companyData.getString("orgname"));
                    businessBase.setQyOrgCode(companyData.getString("orgcode"));
                    businessBase.setQyHandlerName(companyData.getString("handlername"));
                    businessBase.setQyHandlerPhone(companyData.getString("handlerphone"));
                    businessBase.setQyHandlerId(companyData.getString("handlerid"));
                    businessBase.setQyHandlerIdtype(companyData.getString("handleridtype"));
                }
                xtApproveBusinessBaseService.saveOrUpdate(businessBase);
                /*******************************************保存邮寄信息表*********************************************/
                if (mailData!=null&&StrUtil.isNotBlank(mailData.getString("mailType"))){
                    XtApproveBusinessEmail businessEmail  = new XtApproveBusinessEmail();

                    businessEmail.setSeqId(UUID.randomUUID().toString());
                    businessEmail.setBaseInfoId(baseInfoId);
                    businessEmail.setMailType(mailData.getString("mailType"));
                    businessEmail.setSendMailAddress(mailData.getString("sendMailAddress"));
                    businessEmail.setSendMailPostCode(mailData.getString("sendMailPostCode"));
                    businessEmail.setSendMailName(mailData.getString("sendMailName"));
                    businessEmail.setSendMailProvince(mailData.getString("sendMailProvince"));
                    businessEmail.setSendMailCity(mailData.getString("sendMailCity"));
                    businessEmail.setSendMailCounty(mailData.getString("sendMailCounty"));
                    businessEmail.setSendMailPhone(mailData.getString("sendMailPhone"));
                    xtApproveBusinessEmailService.saveOrUpdate(businessEmail);
                }



                //处理表单信息并动态保存
                if(applyAcceptData.containsKey("xmlData")){

                    String xmlData  = applyAcceptData.getString("xmlData");
                    logger.error("!!!!!!!!!!!!!!!!!!!1"+xmlData);
                    xmlData=xmlData.replace("\\n","");
                    xmlData=xmlData.replace("\\","");
                    Document document  = XmlHandleUtil.fromXML(xmlData, "utf-8");
                    Element root = document.getRootElement();
                    Element spItemList = root.element("SPItemListDef");
//                    Element formTypeE = root.element("formType");
                    Element TableCols = spItemList.element("TableCols");
                    Element rows = TableCols.element("rows");
                    String formData=rows.getText();
                    JSONObject formJson = JSONObject.parseObject(formData);
                    if(formJson != null && formJson.size() > 0){
                        //不对接产品表单按数据存库
                        //查询关于xml_data配置信息通过sql将信息存入相关库表
                        //通过事项垂管id获取表名，字段名等等 TODO 后续抽出为单独的方法
                        List<XtApproveBusinessXmlConfig>  xmlConfigs  = xmlConfigService.getBaseMapper().selectList(new QueryWrapper<XtApproveBusinessXmlConfig>()
                                .eq("ITEM_ID",itemId));
                        Map<String, Object> params  =  new HashMap<>();
                        String[] colums  = new String[xmlConfigs.size()];
                        for (int i = 0; i < xmlConfigs.size(); i++) {
                            if ("table".equals(xmlConfigs.get(i).getType())){
                                //将表名插入map
                                params.put("tableName",xmlConfigs.get(i).getXmlCode());
                            }else if("column".equals(xmlConfigs.get(i).getType())){
                                //将字段插入数组
                                colums[i] = xmlConfigs.get(i).getXmlCode();
                                //将值遍历插入
                                if(StrUtil.isNotBlank(formJson.getString(colums[i]))){
                                    params.put(colums[i].toString(),formJson.getString(colums[i]));
                                }else{
                                    if (!"onlineApplyId".equals(xmlConfigs.get(i).getXmlCode())) {
                                        params.put(colums[i].toString(), "");
                                    }
                                }
                                if ("idcard".equals(colums[i])){
                                    List<XtApproveBusinessSpecial> specials  = businessSpecialService.getBaseMapper().selectList(
                                            new QueryWrapper<XtApproveBusinessSpecial>().eq("IDCARD",formJson.get(colums[i]))
                                    );
                                    if (specials!=null&&specials.size()>0){
                                        params.put("onlineApplyId",specials.get(0).getOnlineApplyId());
                                    }
                                }


                            }else if("keyword".equals(xmlConfigs.get(i).getType())){
                                //TODO 后续修改为可配置的关联关系
                                params.put("keyword",xmlConfigs.get(i).getXmlCode());
                                params.put("keywordvalue",applyAcceptData.getString("sblshShort"));
                            }
//                            else if("custom".equals(xmlConfigs.get(i).getXmlType())){
//                                params.put("custom",xmlConfigs.get(i).getXmlCode());
//                                //将值遍历插入
//                                if(StrUtil.isNotBlank(formJson.getString(colums[i]))){
//                                    params.put("customvalue",formJson.getString(colums[i]));
//                                }else{
//                                    params.put("customvalue","");
//                                }
//                            }

                        }
                        //循环结束将字段名数组插入map
                        params.put("columns",colums);
                        //拼装完成后插入相关
//                        xmlConfigService.selectXmlByCustomProvider(params);
                        xmlConfigService.insertXmlDataProvider(params);
                    }

                }
                /**********************************保存过程信息信息表**************************************/
               //是否引入流程概念引入接口



            }else{
                throw new Exception("请联系管理员，获取事项垂管id为空！");
            }

            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
//            result.put("code", CommonConstants.API_FAIL);
//            result.put("error", e.getMessage());
//            return result;
        }


    }

    @Override
    /**
     * 根据业务受理编号获取材料数据信息
     * @return
     */
    public JSONObject getBusiApplyMaterial(String receiveNumber ,String sxbm,String ywlx){
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "请求成功！");
        JSONObject paramJson = new JSONObject();
        JSONObject txnCommCom = new JSONObject();
        paramJson.put("txnCommCom" , txnCommCom);
        JSONObject txnBodyCom = new JSONObject();
        txnBodyCom.put("sblshShort" , receiveNumber);
        txnBodyCom.put("sxbm" , sxbm);
        txnBodyCom.put("ywlx" , ywlx);
        paramJson.put("txnBodyCom" , txnBodyCom);
        String dispatchUrl =propertyConfig.getDispatchUrl();
        if(!dispatchUrl.endsWith("/")){
            dispatchUrl = dispatchUrl.concat("/");
        }
        String method = "___ddpt/ddpt/ddpt61003";
        HttpClientUtils httpClientUtils=new HttpClientUtils();
        String callRestXml= httpClientUtils.sendJsonHttpPost(dispatchUrl+method,paramJson.toString());
        if(logger.isInfoEnabled()) {
            logger.info("61003返回："+callRestXml);
        }
        if(StrUtil.isBlank(callRestXml)){
            result.put("code", CommonConstants.API_SUCCESS);
            result.put("error","未调用到接口,请检查接口地址是否正确！");
            return result;
        }else{
            result.put("xmlStr", callRestXml);
        }
        return result;
    }

    @Override
    /**
     * 解析材料数据
     * @return
     */
    @SuppressWarnings("unchecked")
    public JSONObject analysisMaterial(String xmlStr,String sblshShort){
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "	请求成功！");
        try {
            JSONObject txnBodyComJson = JSONObject.parseObject(xmlStr);
            if(!"success".equals(txnBodyComJson.getString("C-Response-Desc"))){
                result.put("code", CommonConstants.API_FAIL);
                result.put("error", "接口返回失败");
                return result;
            }
            String  bodyStr = txnBodyComJson.getString("C-Response-Body");
            JSONObject bodyJson = JSONObject.parseObject(bodyStr);
            if(!"00".equals(bodyJson.getString("code"))){
                result.put("code", CommonConstants.API_FAIL);
                result.put("error",bodyJson.getString("msg"));
                return result;
            }
            JSONArray dataArray=bodyJson.getJSONArray("records");
            if(!dataArray.isEmpty()){
                //保存材料信息
                //创建一个材料的list
                List<XtApproveBusinessMaterial> materials  = new ArrayList<>();

                for (int i = 0; i <dataArray.size() ; i++) {
                    XtApproveBusinessMaterial material = new XtApproveBusinessMaterial();
                    material.setSeqId(UUID.randomUUID().toString());
                    material.setSblshShort(sblshShort);//业务办理编号

                    material.setStuffSeq(StrUtil.isNotBlank(dataArray.getJSONObject(i).getString("stuffSeq"))?dataArray.getJSONObject(i).getString("stuffSeq"):"");

                    material.setClmc(StrUtil.isNotBlank(dataArray.getJSONObject(i).getString("clmc"))?dataArray.getJSONObject(i).getString("clmc"):"");

                    material.setWjlx(StrUtil.isNotBlank(dataArray.getJSONObject(i).getString("wjlx"))?dataArray.getJSONObject(i).getString("wjlx"):"");

                    material.setCllx(StrUtil.isNotBlank(dataArray.getJSONObject(i).getString("cllx"))?dataArray.getJSONObject(i).getString("cllx"):"");

                    material.setClsl(Integer.valueOf(dataArray.getJSONObject(i).getString("clsl")));
                    material.setAttachName(dataArray.getJSONObject(i).getString("attachName"));
                    material.setAttachId(dataArray.getJSONObject(i).getString("attachId"));
                    material.setRemark(dataArray.getJSONObject(i).getString("remark"));
                    material.setAttachBody(dataArray.getJSONObject(i).getString("attachBody"));
                    material.setAttachType(dataArray.getJSONObject(i).getString("attachType"));
                    material.setAttachPath(dataArray.getJSONObject(i).getString("attachPath"));
                    materials.add(material);
                }
                materialService.saveBatch(materials,materials.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", CommonConstants.API_FAIL);
            result.put("error", e.getMessage());
            return result;
        }
        return result;
    }

    /**
     * 解析申办，预受理，受理数据
     * @param xmlStr
     * @return
     */
    public JSONObject analysisApplyData(String xmlStr){
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "请求成功！");
        try {
            JSONObject shenqinJson = new JSONObject();
            JSONObject shouliJson = new JSONObject();
            JSONObject txnBodyComJson = JSONObject.parseObject(xmlStr);
            if(!"success".equals(txnBodyComJson.getString("C-Response-Desc"))){
                result.put("code", CommonConstants.API_FAIL);
                result.put("error", "接口返回失败");
                return result;
            }
            String  bodyStr = txnBodyComJson.getString("C-Response-Body");
            JSONObject bodyJson = JSONObject.parseObject(bodyStr);
            if(!"00".equals(bodyJson.getString("code"))){
                result.put("code", CommonConstants.API_FAIL);
                result.put("error",bodyJson.getString("msg"));
                return result;
            }
            JSONObject dataJson=bodyJson.getJSONObject("data");
            String  itemId = dataJson.getString("sxbbbm");
            String  formId = dataJson.getString("sxbdbm");
            String  ywly = dataJson.getString("ywly");
            if(dataJson.containsKey("spshenqin")){
                shenqinJson  = dataJson.getJSONObject("spshenqin");
            }
            if(dataJson.containsKey("spshouli")){
                shouliJson  = dataJson.getJSONObject("spshouli");
            }
            if(dataJson.containsKey("sprenyuan")){
                JSONObject personJson = dataJson.getJSONObject("sprenyuan");
                result.put("person", personJson);
            }
            if(dataJson.containsKey("spxiangmu")){
                JSONObject projectJson = dataJson.getJSONObject("spxiangmu");
                result.put("project", projectJson);
            }
            if(dataJson.containsKey("spqiye")){
                JSONObject companyJson = dataJson.getJSONObject("spqiye");
                result.put("company", companyJson);
            }
            if(dataJson.containsKey("spcailiaoshenhe")){
                JSONObject spcailiaoshenheJson  = dataJson.getJSONObject("spcailiaoshenhe");
                result.put("yushen", spcailiaoshenheJson);
            }
            if(dataJson.containsKey("spbanjie")){
                JSONObject spbanjieJson  = dataJson.getJSONObject("spbanjie");
                result.put("banjie", spbanjieJson);
            }
            if(dataJson.containsKey("spmail")){
                JSONObject spmailJson  = dataJson.getJSONObject("spmail");
                result.put("mail", spmailJson);
            }
            result.put("shenqin", shenqinJson);
            result.put("shouli", shouliJson);

            result.put("itemId", itemId);
            result.put("formId", formId);
            result.put("applyFrom", ywly);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", CommonConstants.API_FAIL);
            result.put("error", e.getMessage());
        }
        return result ;

    }

}
