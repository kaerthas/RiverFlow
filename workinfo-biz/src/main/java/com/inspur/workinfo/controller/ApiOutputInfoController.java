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

import com.inspur.workinfo.entity.ApiOutputInfo;
import com.inspur.workinfo.service.ApiOutputInfoService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-07-13 09:23:20
 */
@RestController
@AllArgsConstructor
@RequestMapping("/apioutputinfo" )
@Api(value = "apioutputinfo", tags = "${comments}管理")
public class ApiOutputInfoController {

    private final ApiOutputInfoService apiOutputInfoService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param apiOutputInfo ${comments}
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getApiOutputInfoPage(Page page, ApiOutputInfo apiOutputInfo) {
        return R.ok(apiOutputInfoService.page(page, Wrappers.query(apiOutputInfo)));
    }


    /**
     * 通过id查询${comments}
     * @param id id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{id}" )
    public R getById(@PathVariable("id" ) String id) {
        return R.ok(apiOutputInfoService.getById(id));
    }

    /**
     * 新增${comments}
     * @param apiOutputInfo ${comments}
     * @return R
     */
    @ApiOperation(value = "新增${comments}", notes = "新增${comments}")
    @PostMapping
    public R save(@RequestBody ApiOutputInfo apiOutputInfo) {
        return R.ok(apiOutputInfoService.save(apiOutputInfo));
    }

    /**
     * 修改${comments}
     * @param apiOutputInfo ${comments}
     * @return R
     */
    @ApiOperation(value = "修改${comments}", notes = "修改${comments}")
    @PutMapping
    public R updateById(@RequestBody ApiOutputInfo apiOutputInfo) {
        return R.ok(apiOutputInfoService.updateById(apiOutputInfo));
    }

    /**
     * 通过id删除${comments}
     * @param id id
     * @return R
     */
    @ApiOperation(value = "通过id删除${comments}", notes = "通过id删除${comments}")
    @DeleteMapping("/{id}" )
    public R removeById(@PathVariable String id) {
        return R.ok(apiOutputInfoService.removeById(id));
    }

}
