/*
 *  *    Copyright (c) 2019-2025, Jason All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * Redistributions of source code must retain the above copyright notice,
 *  * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *  * notice, this list of conditions and the following disclaimer in the
 *  * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the yunho.top developer nor the names of its
 *  * contributors may be used to endorse or promote products derived from
 *  * this software without specific prior written permission.
 *  * Author: Jason (yunho@mail.yunho.io)
 */

package com.inspur.workinfo.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.dto.DruidDTO;
import com.inspur.workinfo.enums.DriverTypeEnum;
import com.mysql.cj.jdbc.Driver;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Jason
 * @date : 2020/4/30 14:25
 * @description :
 */
@Slf4j
@Component
public class DruidUtil {
//    private final StringEncryptor stringEncryptor;
//    private static StringEncryptor stringEncryptorstatic;
//
//    @PostConstruct
//    public void init(){
//        stringEncryptorstatic = stringEncryptor;
//    }

    protected static Map<String,DruidDataSource> druidDataSourceMap = new ConcurrentHashMap<>();

    public static List<Map<String,Object>> druidSql(String driverType, String url, String userName,
                                                    String password, String sqlCon, StringEncryptor stringEncryptor, String druidID) {
        DruidDTO druidDTO = new DruidDTO();
        druidDTO.setUrl(url);
        druidDTO.setDriverType(driverType);
        druidDTO.setUsername(userName);
        String decPwd = stringEncryptor.decrypt(password);
        druidDTO.setPassword(decPwd);
        DruidDataSource dds = setDruidDataSourceMap(druidID,druidDTO);
        List<Map<String, Object>> dbList = new JdbcTemplate(dds).queryForList(sqlCon);
        return dbList;
    }

    public static void insertSql(String driverType, String url, String userName,
                                                    String password, String sqlCon, StringEncryptor stringEncryptor, String druidID) {
        DruidDTO druidDTO = new DruidDTO();
        druidDTO.setUrl(url);
        druidDTO.setDriverType(driverType);
        druidDTO.setUsername(userName);
        String decPwd = stringEncryptor.decrypt(password);
        druidDTO.setPassword(decPwd);
        DruidDataSource dds = setDruidDataSourceMap(druidID,druidDTO);
        new JdbcTemplate(dds).execute(sqlCon);
    }


    public static DruidDataSource setDruidDataSourceMap(String drudId, DruidDTO druidDataSource){
        if(druidDataSourceMap.containsKey(drudId)){
            return druidDataSourceMap.get(drudId);
        }
        DruidDataSource dds = new DruidDataSource();
        dds.setUrl(druidDataSource.getUrl());
        if(druidDataSource.getDriverType().equals(DriverTypeEnum.oracle.getValue())){
            dds.setDriverClassName(CommonConstants.DRIVER_ORACLE);
        }else {
            dds.setDriverClassName(Driver.class.getName());
        }
        dds.setUsername(druidDataSource.getUsername());
        dds.setPassword(druidDataSource.getPassword());
        druidDataSourceMap.put(drudId,dds);
        return dds;
    }
}
