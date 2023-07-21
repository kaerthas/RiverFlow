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

package com.inspur.workinfo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.inspur.workinfo.entity.ApiDatasourceConfig;

/**
 * 代理数据源管理表
 *
 * @author yunho code generator
 * @date 2023-07-13 16:28:50
 */
public interface ApiDatasourceConfigService extends IService<ApiDatasourceConfig> {

    /**
     * 保存数据源并且加密
     * @param genDatasourceConf
     * @return
     */
    Boolean saveDsByEnc(ApiDatasourceConfig genDatasourceConf);

    /**
     * 更新数据源
     * @param genDatasourceConf
     * @return
     */
    Boolean updateDsByEnc(ApiDatasourceConfig genDatasourceConf);

    /**
     * 更新动态数据的数据源列表
     * @param datasourceConf
     * @return
     */
    void addDynamicDataSource(ApiDatasourceConfig datasourceConf);

    /**
     * 校验数据源配置是否有效
     * @param datasourceConf 数据源信息
     * @return 有效/无效
     */
    Boolean checkDataSource(ApiDatasourceConfig datasourceConf);

    /**
     * 通过数据源名称删除
     * @param dsId 数据源ID
     * @return
     */
    Boolean removeByDsId(String  dsId);
}
