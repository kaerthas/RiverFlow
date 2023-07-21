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

import com.inspur.workinfo.entity.ApiDataTableExchange;
import com.inspur.workinfo.service.ApiDataTableExchangeService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 库表交换绑定关系表
 *
 * @author yunho code generator
 * @date 2023-07-14 15:31:50
 */
@RestController
@AllArgsConstructor
@RequestMapping("/apidatatableexchange" )
@Api(value = "apidatatableexchange", tags = "库表交换绑定关系表管理")
public class ApiDataTableExchangeController {

    private final ApiDataTableExchangeService apiDataTableExchangeService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param apiDataTableExchange 库表交换绑定关系表
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getApiDataTableExchangePage(Page page, ApiDataTableExchange apiDataTableExchange) {
        return R.ok(apiDataTableExchangeService.page(page, Wrappers.query(apiDataTableExchange)));
    }


    /**
     * 通过id查询库表交换绑定关系表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(apiDataTableExchangeService.getById(seqId));
    }

    /**
     * 新增库表交换绑定关系表
     * @param apiDataTableExchange 库表交换绑定关系表
     * @return R
     */
    @ApiOperation(value = "新增库表交换绑定关系表", notes = "新增库表交换绑定关系表")
    @PostMapping
    public R save(@RequestBody ApiDataTableExchange apiDataTableExchange) {
        return R.ok(apiDataTableExchangeService.save(apiDataTableExchange));
    }

    /**
     * 修改库表交换绑定关系表
     * @param apiDataTableExchange 库表交换绑定关系表
     * @return R
     */
    @ApiOperation(value = "修改库表交换绑定关系表", notes = "修改库表交换绑定关系表")
    @PutMapping
    public R updateById(@RequestBody ApiDataTableExchange apiDataTableExchange) {
        return R.ok(apiDataTableExchangeService.updateById(apiDataTableExchange));
    }

    /**
     * 通过id删除库表交换绑定关系表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除库表交换绑定关系表", notes = "通过id删除库表交换绑定关系表")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(apiDataTableExchangeService.removeById(seqId));
    }

}
