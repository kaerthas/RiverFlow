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
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.ApiDataColumnsExchang;
import com.inspur.workinfo.entity.ApiDataTableExchange;
import com.inspur.workinfo.entity.ApiDatasourceConfig;
import com.inspur.workinfo.entity.XtApproveItemflowConfig;
import com.inspur.workinfo.mapper.ApiDataTableExchangeMapper;
import com.inspur.workinfo.service.ApiDataColumnsExchangService;
import com.inspur.workinfo.service.ApiDataTableExchangeService;
import com.inspur.workinfo.service.ApiDatasourceConfigService;
import com.inspur.workinfo.util.DateUtils;
import com.inspur.workinfo.util.DruidUtil;
import com.inspur.workinfo.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 库表交换绑定关系表
 *
 * @author yunho code generator
 * @date 2023-07-14 15:31:50
 */
@Service
@Slf4j
public class ApiDataTableExchangeServiceImpl extends ServiceImpl<ApiDataTableExchangeMapper, ApiDataTableExchange> implements ApiDataTableExchangeService {

    private final Logger logger  = LoggerFactory.getLogger(ApiDataTableExchangeServiceImpl.class);
    @Autowired
    private ApiDataColumnsExchangService columnsExchangService;
    @Autowired
    private ApiDatasourceConfigService datasourceConfigService;
    @Autowired
    private StringEncryptor stringEncryptor;

    @Override
    public JSONObject analysisDataExchange(XtApproveItemflowConfig itemflowConfig,String sblshShort) {

        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "	请求成功！");
        try {

            ApiDataTableExchange tableExchange = this.baseMapper.selectById(itemflowConfig.getTableId());
            if (tableExchange!=null){
                //拼接查询sql语句
                ApiDatasourceConfig datasourceConf = datasourceConfigService.getBaseMapper()
                        .selectOne(new QueryWrapper<ApiDatasourceConfig>()
                        .eq("NAME",tableExchange.getAimDatasourceName()));
                if(datasourceConf==null){
                    log.info("数据源不存在{}"+tableExchange.getSeqId());
                    throw  new Exception("数据源不存在！");
                }
                //存在则查询相关数据库表
                List<ApiDataColumnsExchang> columnsList   = columnsExchangService
                        .getBaseMapper().selectList(new QueryWrapper<ApiDataColumnsExchang>()
                                .eq("TABLE_ID",tableExchange.getSeqId())
                                .eq("COLUMNS_TYPE",CommonConstants.SELECT_COLUMN));
                if (columnsList!=null&&columnsList.size()>0){
                    //按照目标表查询字段 拼接sql语句查询到相关信息
                    StringBuilder sb  = new StringBuilder();
                    sb.append("select ");
                    for (int i = 0; i <columnsList.size() ; i++) {
                        sb.append(columnsList.get(i).getBusinessColumns()).append(",");
                    }
                    sb.deleteCharAt(sb.lastIndexOf(","));
                    sb.append(" from ").append(tableExchange.getBusinessTable());
                    sb.append(" where 1=1 ");
                    List<ApiDataColumnsExchang> conditionList   = columnsExchangService
                            .getBaseMapper().selectList(new QueryWrapper<ApiDataColumnsExchang>()
                                    .eq("TABLE_ID",tableExchange.getSeqId())
                                    .eq("COLUMNS_TYPE",CommonConstants.SELECT_CONDITION));
                    if (conditionList!=null && conditionList.size()>0) {
                        for (int j = 0; j <conditionList.size(); j++) {
                            sb.append(" and ").append(conditionList.get(j).getBusinessColumns()).append(" = '").append(sblshShort).append("'");
                        }
                    }

                    List<Map<String, Object>> mapList = DruidUtil.druidSql(datasourceConf.getDriverType(),
                            datasourceConf.getUrl(), datasourceConf.getUserName(), datasourceConf.getPassword(),sb.toString()
                            ,stringEncryptor,tableExchange.getSeqId());

                    //拿到查询到的信息与映射字段进行保存
                    result.put("data",mapList);
                    result.put("tableId" ,tableExchange.getSeqId());
                    return result;

                }
            }else{
                throw  new Exception("环节配置失败，初始化信息失败！");
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage(),e);
            result.put("code", CommonConstants.API_FAIL);
            result.put("error",e.getMessage());
            return result;
        }

    }
    private  static Class<?> getEntityClassFromTableName(String tableName) {
        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
        try {
            return classLoader.loadClass(tableName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法加载实体类 " + tableName, e);
        }
    }
}
