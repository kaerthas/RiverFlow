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
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.mapper.XtApproveBusinessCourseMapper;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;

/**
 * 业务过程信息表
 *
 * @author yunho code generator
 * @date 2023-07-12 11:06:40
 */
@Slf4j
@Service
public class XtApproveBusinessCourseServiceImpl extends ServiceImpl<XtApproveBusinessCourseMapper, XtApproveBusinessCourse> implements XtApproveBusinessCourseService {

    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;
    @Autowired
    private XtApproveBusinessAcceptService businessAcceptService;
    @Autowired
    private XtApproveBusinessDoneService businessDoneService;
    @Autowired
    private XtApproveBusinessXmlConfigService xmlConfigService;
    @Autowired
    private XtApproveItemConfigService itemConfigService;
    @Autowired
    private SqlSessionFactory sqlSessionFactory; // 注入SqlSessionFactory
    @Autowired
    private  XtApproveBusinessSpecialService businessSpecialService;
    @Autowired
    private XtApproveBusinessBaseService businessBaseService;


    @Override
    @Transactional
    public JSONObject analysisCourse(String sblshShort)throws Exception {

        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "	请求成功！");
        try {

            XtApproveBusinessCourse businessCourseOld = this.baseMapper.selectOne(new QueryWrapper<XtApproveBusinessCourse>()
                    .eq("ACTIVE","1")
                    .eq("SBLSH_SHORT",sblshShort));
            if (businessCourseOld!=null){
                //查询当前环节流程配置
                XtApproveItemflowConfig itemflowConfigOld = this.itemflowConfigService.getBaseMapper()
                        .selectById(businessCourseOld.getCurrentNodeId());
                if (StrUtil.isBlank(itemflowConfigOld.getCondition())){
                    //如果流程没有设置条件直接进入下一个流程
                    List<XtApproveItemflowConfig> itemflowConfigs = this.itemflowConfigService.getBaseMapper()
                            .selectList(new QueryWrapper<XtApproveItemflowConfig>()
                                    .eq("PARENT_ID",businessCourseOld.getCurrentNodeId())
                                    .ne("CONDITION_TYPE",CommonConstants.XT_ITEM_CONDITION_ERROR));
                    //判断流程是否唯一
                    if(itemflowConfigs!=null&&itemflowConfigs.size()==1){
                        //新增环节信息
                        XtApproveBusinessCourse businessCourseNew  = new XtApproveBusinessCourse();

                        businessCourseNew.setSeqId(UUID.randomUUID().toString());
                        businessCourseNew.setSblshShort(sblshShort);
                        businessCourseNew.setCurrentNodeId(itemflowConfigs.get(0).getSeqId());
                        businessCourseNew.setCurrentNodeCode(itemflowConfigs.get(0).getNodeCode());
                        businessCourseNew.setActive("1");

                        this.baseMapper.insert(businessCourseNew);
                        businessCourseOld.setModifyTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss",new Date()));
                        businessCourseOld.setActive("0");
                        this.baseMapper.updateById(businessCourseOld);
                    }else{
                        throw new Exception("流程配置错误，事项itemid为"+itemflowConfigOld.getSxbm());
                    }
                }else {
                    this.analysisCoursePlus(sblshShort,businessCourseOld,itemflowConfigOld);
                }
            }else{
                throw  new Exception("环节配置失败，初始化信息失败！");
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }


    }

    @Override
    @Transactional
    public JSONObject analysisCourseSuccess(String sblshShort, Map<String,Object> objectMap) throws Exception {
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "	请求成功！");
        try {

            XtApproveBusinessCourse businessCourseOld = this.baseMapper.selectOne(new QueryWrapper<XtApproveBusinessCourse>()
                    .eq("ACTIVE","1")
                    .eq("SBLSH_SHORT",sblshShort));
            if (businessCourseOld!=null){
                //查询当前环节流程配置
                XtApproveItemflowConfig itemflowConfigOld = this.itemflowConfigService.getBaseMapper()
                        .selectById(businessCourseOld.getCurrentNodeId());
                if (StrUtil.isBlank(itemflowConfigOld.getCondition())){
                    //如果流程没有设置条件直接进入下一个流程
                    List<XtApproveItemflowConfig> itemflowConfigs = this.itemflowConfigService.getBaseMapper()
                            .selectList(new QueryWrapper<XtApproveItemflowConfig>()
                                    .eq("PARENT_ID",businessCourseOld.getCurrentNodeId())
                            .ne("CONDITION_TYPE",CommonConstants.XT_ITEM_CONDITION_ERROR));
                    //判断流程是否唯一
                    if(itemflowConfigs!=null&&itemflowConfigs.size()==1){
                        //新增环节信息
                        XtApproveBusinessCourse businessCourseNew  = new XtApproveBusinessCourse();

                        businessCourseNew.setSeqId(UUID.randomUUID().toString());
                        businessCourseNew.setSblshShort(sblshShort);
                        businessCourseNew.setCurrentNodeId(itemflowConfigs.get(0).getSeqId());
                        businessCourseNew.setCurrentNodeCode(itemflowConfigs.get(0).getNodeCode());
                        businessCourseNew.setActive("1");

                        this.baseMapper.insert(businessCourseNew);
                        businessCourseOld.setModifyTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss",new Date()));
                        businessCourseOld.setActive("0");
                        this.baseMapper.updateById(businessCourseOld);
                        //回填相关物化表信息
                        //首先根据流程表获取xml配置
                        //获取事项编码
                        XtApproveBusinessBase businessBase = businessBaseService
                                .getOne(new QueryWrapper<XtApproveBusinessBase>().eq("SBLSH_SHORT",sblshShort));
                        if (businessBase!=null) {
                            XtApproveItemConfig itemConfig = itemConfigService.getOne(new QueryWrapper<XtApproveItemConfig>()
                                    .eq("SXBM", businessBase.getSxbm()));
                            //根据模型id获取xmlData模板
                            List<XtApproveBusinessXmlConfig> xmlConfigs = xmlConfigService.getBaseMapper()
                                    .selectList(new QueryWrapper<XtApproveBusinessXmlConfig>().eq("ITEM_ID", itemConfig.getItemId()));
                            Map<String, Object> params = new HashMap<>();
                            for (int j = 0; j < xmlConfigs.size(); j++) {
                                if ("table".equals(xmlConfigs.get(j).getType())) {
                                    //将表名插入map
                                    params.put("tableName", xmlConfigs.get(j).getXmlCode());
                                } else if ("keyword".equals(xmlConfigs.get(j).getType())) {
                                    //将条件插入
                                    params.put("keyword", xmlConfigs.get(j).getXmlCode());
                                    params.put("keywordValue", sblshShort);
                                }
                            }
                            params.put("columns", objectMap);

                            xmlConfigService.updateXmlDataProvider(params);
                        }else {
                            throw new Exception("基本信息表中数据不存在，办件编号为"+sblshShort);
                        }
                    }else{
                        throw new Exception("流程配置错误，流程id为"+itemflowConfigOld.getSeqId());
                    }
                }else {
                    this.analysisCoursePlus(sblshShort,businessCourseOld,itemflowConfigOld);

                }
            }else{
                throw  new Exception("环节配置失败，初始化信息失败！");
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public JSONObject analysisCourseError(String sblshShort,JSONObject res) throws Exception {
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "	请求成功！");
        try {

            XtApproveBusinessCourse businessCourseOld = this.baseMapper.selectOne(new QueryWrapper<XtApproveBusinessCourse>()
                    .eq("ACTIVE","1")
                    .eq("SBLSH_SHORT",sblshShort));
            if (businessCourseOld!=null){
                //查询当前环节流程配置
                XtApproveItemflowConfig itemflowConfigOld = this.itemflowConfigService.getBaseMapper()
                        .selectById(businessCourseOld.getCurrentNodeId());
                if (StrUtil.isBlank(itemflowConfigOld.getCondition())){
                    //如果流程没有设置条件直接进入下一个流程
                    List<XtApproveItemflowConfig> itemflowConfigs = this.itemflowConfigService.getBaseMapper()
                            .selectList(new QueryWrapper<XtApproveItemflowConfig>()
                                    .eq("PARENT_ID",businessCourseOld.getCurrentNodeId())
                            .eq("CONDITION_TYPE",CommonConstants.XT_ITEM_CONDITION_ERROR));
                    //判断流程是否唯一
                    if(itemflowConfigs!=null&&itemflowConfigs.size()==1) {
                        XtApproveBusinessBase businessBase = businessBaseService.getBaseMapper()
                                .selectOne(new QueryWrapper<XtApproveBusinessBase>().eq("SBLSH_SHORT",sblshShort));
                        if (businessBase!=null) {
                            XtApproveBusinessAccept businessAccept = new XtApproveBusinessAccept();

                            businessAccept.setSeqId(UUID.randomUUID().toString().replace("-", ""));//主键
                            businessAccept.setSxbm(businessBase.getSxbm());
                            businessAccept.setSblshShort(sblshShort);
                            businessAccept.setYwlsmc("系统平台管理员");
                            businessAccept.setYwlszt("0");//0位为不予受理，1为受理
                            if (StrUtil.isNotBlank(res.getString("message"))) {
                                log.error("#############################"+res.getString("message"));
                                businessAccept.setYwslyj( res.getString("message").length()>200? res.getString("message").substring(0, 200) : res.getString("message"));//受理意见
                            }else {
                                businessAccept.setYwslyj("");
                            }
                            businessAccept.setYwslbmbm(businessBase.getBmzzjgdm());
                            businessAccept.setYwslbmmc(businessBase.getBmmc());
                            businessAccept.setYwslqhbm(businessBase.getXzqhdm());
//                                                        accept.setYwslqhmc(businessBase.get);
                            businessAccept.setYwslsj(new Date());
                            businessAcceptService.getBaseMapper().insert(businessAccept);
                        }else{
                            throw new Exception("办件信息不完整!申报流水号为"+sblshShort);

                        }

                        //新增环节信息
                        this.saveCourse(sblshShort, itemflowConfigs.get(0), businessCourseOld);
                        //保存受理信息
                    }else{
                            throw new Exception("流程配置错误，流程id为"+itemflowConfigOld.getSeqId());
                    }
                }
            }else{
                throw  new Exception("环节配置失败，初始化信息失败！");
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private void analysisCoursePlus(String sblshShort , XtApproveBusinessCourse businessCourseOld,XtApproveItemflowConfig itemflowConfigOld) throws Exception{
        //如果下一个环节拿字段进行判断
        if (CommonConstants.XT_ITEM_CONDITION_COLUMNS.equals(itemflowConfigOld.getConditionType())) {
            //判断环节名称
            //处理受理发送环节流程
            //受理信息不推受理，直接推办结接口完成办结
            if (CommonConstants.XT_BUSINESS_SEND_ACCEPT.equals(businessCourseOld.getCurrentNodeCode())
                    || CommonConstants.XT_BUSINESS_GET_ACCEPT.equals(businessCourseOld.getCurrentNodeCode())) {
                //TODO 暂时写死 后期通过反射获取
//                        Class clazz = XtApproveBusinessAccept.class;
//                        clazz.getField(itemflowConfigOld.getCondition());

                XtApproveBusinessAccept businessAccept = businessAcceptService.getBaseMapper().selectOne(new QueryWrapper<XtApproveBusinessAccept>()
                        .eq("SBLSH_SHORT", sblshShort));

                //获取下一级流程
                List<XtApproveItemflowConfig> itemflowConfigs = this.itemflowConfigService.getBaseMapper()
                        .selectList(new QueryWrapper<XtApproveItemflowConfig>()
                                .eq("PARENT_ID", businessCourseOld.getCurrentNodeId()));

                if (itemflowConfigs != null && itemflowConfigs.size() > 1) {

                    for (int i = 0; i < itemflowConfigs.size(); i++) {
                        try {
                            //判断流程中的值走那一步
                            if (itemflowConfigs.get(i).getChildValue().equals(businessAccept.getYwlszt())) {
                                this.saveCourse(sblshShort, itemflowConfigs.get(i), businessCourseOld);
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw e;
                        }

                    }
                }
            }
            //两补增加办结驳回，撤回申请流程
            else if (CommonConstants.XT_BUSINESS_SEND_DONE.equals(businessCourseOld.getCurrentNodeCode())) {
                XtApproveBusinessDone businessDone = businessDoneService.getBaseMapper().selectOne(new QueryWrapper<XtApproveBusinessDone>()
                        .eq("SBLSH_SHORT", sblshShort));

                //获取下一级流程
                List<XtApproveItemflowConfig> itemflowConfigs = this.itemflowConfigService.getBaseMapper()
                        .selectList(new QueryWrapper<XtApproveItemflowConfig>()
                                .eq("PARENT_ID", businessCourseOld.getCurrentNodeId()));

                if (itemflowConfigs != null && itemflowConfigs.size() > 0) {
                    for (int i = 0; i < itemflowConfigs.size(); i++) {
                        //0：出证办结 1：退回办结 2：作废办结 3：删除办结 5：补正不来办结 6：准予许可 7：不予许可
                        try {
                            if (itemflowConfigs.get(i).getChildValue().contains(businessDone.getBjjgdm())) {
                                this.saveCourse(sblshShort, itemflowConfigs.get(i), businessCourseOld);
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw e;
                        }
                    }
                }
            }

        }else if (CommonConstants.XT_ITEM_CONDITION_SQL.equals(itemflowConfigOld.getConditionType())){

            //获取物化表中的信息
            Map<String , Object> item = itemflowConfigService.getImportantXtMessage(itemflowConfigOld,sblshShort);
            //拿出sql语句
            JSONObject itemJson = (JSONObject) item.get(CommonConstants.XT_BUSINESS_XML);
            String tableName  =  itemflowConfigOld.getCondition();

            //条件
            String value  =   itemJson.get(tableName)!=null?itemJson.get(tableName).toString():"";
            //构建一个挂起表
            QueryWrapper<XtApproveBusinessSpecial> wrapper  = new QueryWrapper<>();

            wrapper.eq(tableName,value);
            List<XtApproveBusinessSpecial> specials =  businessSpecialService.getBaseMapper().selectList(wrapper);
            int childValue = 0;//首次申报
            if (specials.size()>0){
                //再次申报
                childValue = 1;
            }
//            QueryWrapper<>
            //调用查询代码块返回参数值
//            Class<?> mapperClass = null;
//
//            try (org.apache.ibatis.session.SqlSession session = sqlSessionFactory.openSession()) {
//                String namespace = "com.inspur.workinfo.mapper."+tableName+"Mapper"; // Mapper命名空间为"com.example.tableName"
//
//                // 动态加载Mapper接口
//                mapperClass = Class.forName(namespace);
//
//                Object mapperInstance = session.getMapper(mapperClass);
//
//                if (mapperInstance instanceof BaseMapper) {
//                    // 这里可以直接使用BaseMapper提供的CRUD等方法进行数据库操作
//
//                    QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
//                    List<Object> resultList = ((BaseMapper<Object>) mapperInstance).selectList(queryWrapper);
//
//                } else {
//                    throw new RuntimeException("无效的Mapper接口！");
//                }
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException("未找到指定的Mapper接口！", e);
//            } finally {
//                if (mapperClass != null && !mapperClass.equals(null)) {
//                    // 释放资源
//                    mapperClass = null;
//                }
//            }



            //查询下一级流程
            List<XtApproveItemflowConfig> itemflowConfigs = this.itemflowConfigService.getBaseMapper()
                    .selectList(new QueryWrapper<XtApproveItemflowConfig>()
                            .eq("PARENT_ID", businessCourseOld.getCurrentNodeId()));
            //如果流程存在且大于0
            if (itemflowConfigs!=null && itemflowConfigs.size()>0){
                for (int i = 0; i <itemflowConfigs.size() ; i++) {
                    try {
                        //判断特殊环节表中如果有数据

                        if (Integer.parseInt(itemflowConfigs.get(i).getChildValue())==childValue){
                            this.saveCourse(sblshShort,itemflowConfigs.get(i),businessCourseOld);
                            break;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        throw  e;
                    }
                }
            }
        }

    }

    @Transactional
    public void saveCourse(String sblshShort,XtApproveItemflowConfig itemflowConfig, XtApproveBusinessCourse businessCourseOld) throws ParseException {
        XtApproveBusinessCourse businessCourseNew = new XtApproveBusinessCourse();

        businessCourseNew.setSeqId(UUID.randomUUID().toString());
        businessCourseNew.setSblshShort(sblshShort);
        businessCourseNew.setCurrentNodeId(itemflowConfig.getSeqId());
        businessCourseNew.setCurrentNodeCode(itemflowConfig.getNodeCode());
        businessCourseNew.setActive("1");

        this.baseMapper.insert(businessCourseNew);
        businessCourseOld.setModifyTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss", new Date()));
        businessCourseOld.setActive("0");
        this.baseMapper.updateById(businessCourseOld);

    }


}
