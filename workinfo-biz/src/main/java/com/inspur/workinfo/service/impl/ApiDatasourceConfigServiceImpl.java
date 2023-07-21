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
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.inspur.workinfo.entity.ApiDatasourceConfig;
import com.inspur.workinfo.mapper.ApiDatasourceConfigMapper;
import com.inspur.workinfo.service.ApiDatasourceConfigService;
import com.inspur.workinfo.util.ReflectUtils;
import com.inspur.workinfo.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 代理数据源管理表
 *
 * @author yunho code generator
 * @date 2023-07-13 16:28:50
 */
@Service
@Slf4j
public class ApiDatasourceConfigServiceImpl extends ServiceImpl<ApiDatasourceConfigMapper, ApiDatasourceConfig> implements ApiDatasourceConfigService {

    @Autowired
    private  DataSourceCreator dataSourceCreator;

    @Autowired
    private  StringEncryptor stringEncryptor;
    /**
     * 保存数据源并且加密
     * @param conf
     * @return
     */
    @Override
    public Boolean saveDsByEnc(ApiDatasourceConfig conf) {
        // 校验配置合法性
        if (!checkDataSource(conf)) {
            return Boolean.FALSE;
        }
//        try {
//        List<Class<?>> annotatedClasses = new ArrayList<>();
//        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
//        scanner.addIncludeFilter(new AnnotationTypeFilter(TableName.class));
//        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("com.inspur.workinfo.entity")) {
//
//                annotatedClasses.add(Class.forName(beanDefinition.getBeanClassName()));
//
//        }
        //循环之后获取类
//        for (Class<?> annotatedClass : annotatedClasses) {
//            TableName annotation = annotatedClass.getAnnotation(TableName.class);
//            if (annotation != null && annotation.value().equals("XT_APPROVE_BUSINESS_ACCEPT")) {
//
//                // 获取实体类的Class对象
//                String seqId ="seqId";
////                ReflectUtils<>
//
//// 创建MyBatis-Plus的Configuration对象，并创建SqlSessionFactory
//                Configuration configuration = new Configuration();
//                SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
//
//// 创建SqlSession
//                try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
//                    // 通过SqlSession获取数据库操作对象（例如Mapper）
////                    Object mapper = sqlSession.getMapper(annotatedClass.getClass());
////                    // 使用Mapper调用insert()方法将对象保存到数据库中
////                    mapper.insert(user);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//
//
//            }
//        }








//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
                // 添加动态数据源
        addDynamicDataSource(conf);

        // 更新数据库配置
        conf.setPassword(stringEncryptor.encrypt(conf.getPassword()));
        this.baseMapper.insert(conf);
        return Boolean.TRUE;
    }

    /**
     * 更新数据源
     * @param conf 数据源信息
     * @return
     */
    @Override
    public Boolean updateDsByEnc(ApiDatasourceConfig conf) {
        if (!checkDataSource(conf)) {
            return Boolean.FALSE;
        }
        // 先移除
        DynamicRoutingDataSource dynamicRoutingDataSource = SpringContextHolder.getBean(DynamicRoutingDataSource.class);
        dynamicRoutingDataSource.removeDataSource(baseMapper.selectById(conf.getSeqId()).getName());

        // 再添加
        addDynamicDataSource(conf);

        // 更新数据库配置
        if (StrUtil.isNotBlank(conf.getPassword())) {
            conf.setPassword(stringEncryptor.encrypt(conf.getPassword()));
        }
        this.baseMapper.updateById(conf);
        return Boolean.TRUE;
    }

    /**
     * 通过数据源名称删除
     * @param dsId 数据源ID
     * @return
     */
    @Override
    public Boolean removeByDsId(String  dsId) {
        DynamicRoutingDataSource dynamicRoutingDataSource = SpringContextHolder.getBean(DynamicRoutingDataSource.class);
        dynamicRoutingDataSource.removeDataSource(baseMapper.selectById(dsId).getName());
        this.baseMapper.deleteById(dsId);
        return Boolean.TRUE;
    }

    /**
     * 添加动态数据源
     * @param conf 数据源信息
     */
    @Override
    public void addDynamicDataSource(ApiDatasourceConfig conf) {
        DataSourceProperty dataSourceProperty = new DataSourceProperty();
        dataSourceProperty.setPoolName(conf.getName());
        dataSourceProperty.setUrl(conf.getUrl());
        dataSourceProperty.setUsername(conf.getUserName());
        dataSourceProperty.setPassword(conf.getPassword());
        DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);

        DynamicRoutingDataSource dynamicRoutingDataSource = SpringContextHolder.getBean(DynamicRoutingDataSource.class);
        dynamicRoutingDataSource.addDataSource(dataSourceProperty.getPoolName(), dataSource);
    }

    /**
     * 校验数据源配置是否有效
     * @param conf 数据源信息
     * @return 有效/无效
     */
    @Override
    public Boolean checkDataSource(ApiDatasourceConfig conf) {
        try (Connection connection = DriverManager.getConnection(conf.getUrl(), conf.getUserName(),
                conf.getPassword())) {
        }
        catch (SQLException e) {
            log.error("数据源配置 {} , 获取链接失败", conf.getName(), e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
