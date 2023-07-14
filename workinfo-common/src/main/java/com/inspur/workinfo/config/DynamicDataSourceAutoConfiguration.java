package com.inspur.workinfo.config;


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


import com.baomidou.dynamic.datasource.processor.DsProcessor;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.inspur.workinfo.config.db.DruidDataSourceProperties;
import com.inspur.workinfo.config.db.JdbcDynamicDataSourceProvider;
import com.inspur.workinfo.config.db.LastParamDsProcessor;
import lombok.AllArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yunho
 * @date 2020-02-06
 * <p>
 * 动态数据源切换配置
 */
@Configuration
@AllArgsConstructor
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DruidDataSourceProperties.class)
public class DynamicDataSourceAutoConfiguration {

private final StringEncryptor stringEncryptor;

private final DruidDataSourceProperties properties;

//	@Bean
//	public DynamicDataSourceProvider dynamicDataSourceProvider() {
//		DruidDataSourceProperties druidDataSourceProperties = new DruidDataSourceProperties();
//		druidDataSourceProperties.setDriverClassName("oracle.jdbc.driver.OracleDriver");
//		druidDataSourceProperties.setPassword("zRAAFrfjjHZl656sGCyEcw==");
//		druidDataSourceProperties.setUsername("dsptest");
//		druidDataSourceProperties.setUrl("jdbc:oracle:thin:@172.23.7.200:1521/orcl");
//		return new JdbcDynamicDataSourceProvider(stringEncryptor, druidDataSourceProperties);
//	}

@Bean
public DynamicDataSourceProvider dynamicDataSourceProvider() {
    return new JdbcDynamicDataSourceProvider(stringEncryptor, properties);
}

@Bean
public DsProcessor dsProcessor() {
    return new LastParamDsProcessor();
}

}

