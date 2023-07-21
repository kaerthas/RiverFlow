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

package com.inspur.workinfo.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.inspur.workinfo.entity.ApiDatasourceConfig;
import com.inspur.workinfo.service.ApiDatasourceConfigService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 代理数据源管理表
 *
 * @author yunho code generator
 * @date 2023-07-13 16:28:50
 */
@RestController
@AllArgsConstructor
@RequestMapping("/apidatasourceconfig" )
@Api(value = "apidatasourceconfig", tags = "代理数据源管理表管理")
public class ApiDatasourceConfigController {

    private final ApiDatasourceConfigService apiDatasourceConfigService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param apiDatasourceConfig 代理数据源管理表
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getApiDatasourceConfigPage(Page page, ApiDatasourceConfig apiDatasourceConfig) {
        return R.ok(apiDatasourceConfigService.page(page, Wrappers.query(apiDatasourceConfig)));
    }


    /**
     * 通过id查询代理数据源管理表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(apiDatasourceConfigService.getById(seqId));
    }

    /**
     * 新增代理数据源管理表
     * @param apiDatasourceConfig 代理数据源管理表
     * @return R
     */
    @ApiOperation(value = "新增代理数据源管理表", notes = "新增代理数据源管理表")
    @PostMapping
    public R save(@RequestBody ApiDatasourceConfig apiDatasourceConfig) {
        return R.ok(apiDatasourceConfigService.saveDsByEnc(apiDatasourceConfig));
    }

    /**
     * 修改代理数据源管理表
     * @param apiDatasourceConfig 代理数据源管理表
     * @return R
     */
    @ApiOperation(value = "修改代理数据源管理表", notes = "修改代理数据源管理表")
    @PutMapping
    public R updateById(@RequestBody ApiDatasourceConfig apiDatasourceConfig) {
        return R.ok(apiDatasourceConfigService.updateDsByEnc(apiDatasourceConfig));
    }

    /**
     * 通过id删除代理数据源管理表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除代理数据源管理表", notes = "通过id删除代理数据源管理表")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(apiDatasourceConfigService.removeByDsId(seqId));
    }

}
