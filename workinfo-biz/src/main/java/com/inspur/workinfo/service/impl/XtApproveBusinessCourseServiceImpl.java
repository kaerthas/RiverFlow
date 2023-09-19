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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.XtApproveBusinessAccept;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.entity.XtApproveItemflowConfig;
import com.inspur.workinfo.mapper.XtApproveBusinessCourseMapper;
import com.inspur.workinfo.service.XtApproveBusinessAcceptService;
import com.inspur.workinfo.service.XtApproveBusinessCourseService;
import com.inspur.workinfo.service.XtApproveItemflowConfigService;
import com.inspur.workinfo.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 业务过程信息表
 *
 * @author yunho code generator
 * @date 2023-07-12 11:06:40
 */
@Service
public class XtApproveBusinessCourseServiceImpl extends ServiceImpl<XtApproveBusinessCourseMapper, XtApproveBusinessCourse> implements XtApproveBusinessCourseService {

    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;
    @Autowired
    private XtApproveBusinessAcceptService businessAcceptService;

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
                                    .eq("PARENT_ID",businessCourseOld.getCurrentNodeId()));
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
                    //判断环节名称
                    //处理受理发送环节流程
                    if (CommonConstants.XT_BUSINESS_SEND_ACCEPT.equals(businessCourseOld.getCurrentNodeCode())){
                        //TODO 暂时写死 后期通过反射获取
//                        Class clazz = XtApproveBusinessAccept.class;
//                        clazz.getField(itemflowConfigOld.getCondition());

                        XtApproveBusinessAccept businessAccept = businessAcceptService.getBaseMapper().selectOne(new QueryWrapper<XtApproveBusinessAccept>()
                                .eq("SBLSH_SHORT",sblshShort));

                        //获取下一级流程
                        List<XtApproveItemflowConfig> itemflowConfigs = this.itemflowConfigService.getBaseMapper()
                                .selectList(new QueryWrapper<XtApproveItemflowConfig>()
                                        .eq("PARENT_ID",businessCourseOld.getCurrentNodeId()));

                        if (itemflowConfigs!=null&&itemflowConfigs.size()>1){

                            for (int i = 0; i < itemflowConfigs.size(); i++) {
                                //判断流程中的值走那一步

                                if(itemflowConfigs.get(i).getChildValue().equals(businessAccept.getYwlszt())){

                                    XtApproveBusinessCourse businessCourseNew  = new XtApproveBusinessCourse();

                                    businessCourseNew.setSeqId(UUID.randomUUID().toString());
                                    businessCourseNew.setSblshShort(sblshShort);
                                    businessCourseNew.setCurrentNodeId(itemflowConfigs.get(i).getSeqId());
                                    businessCourseNew.setCurrentNodeCode(itemflowConfigs.get(i).getNodeCode());
                                    businessCourseNew.setActive("1");

                                    this.baseMapper.insert(businessCourseNew);
                                    businessCourseOld.setModifyTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss",new Date()));
                                    businessCourseOld.setActive("0");
                                    this.baseMapper.updateById(businessCourseOld);

                                    break;
                                }


                            }
                        }
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
}
