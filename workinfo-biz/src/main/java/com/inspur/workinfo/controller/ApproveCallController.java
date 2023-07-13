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


import com.inspur.workinfo.annotation.SysLog;
import com.inspur.workinfo.entity.ApproveCall;
import com.inspur.workinfo.service.ApproveCallService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 接口调用信息
 *
 * @author yunho code generator
 * @date 2023-07-07 17:04:52
 */
@RestController
@AllArgsConstructor
@RequestMapping("/approvecall" )
@Api(value = "approvecall", tags = "接口调用信息管理")
public class ApproveCallController {

    private final ApproveCallService approveCallService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param approveCall 接口调用信息
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )

    public R getApproveCallPage(Page page, ApproveCall approveCall) {
        return R.ok(approveCallService.page(page, Wrappers.query(approveCall)));
    }


    /**
     * 通过id查询接口调用信息
     * @param callId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{callId}" )
    public R getById(@PathVariable("callId" ) String callId) {
        return R.ok(approveCallService.getById(callId));
    }

    /**
     * 新增接口调用信息
     * @param approveCall 接口调用信息
     * @return R
     */
    @ApiOperation(value = "新增接口调用信息", notes = "新增接口调用信息")
    @SysLog("新增接口调用信息" )
    @PostMapping
    public R save(@RequestBody ApproveCall approveCall) {
        return R.ok(approveCallService.save(approveCall));
    }

    /**
     * 修改接口调用信息
     * @param approveCall 接口调用信息
     * @return R
     */
    @ApiOperation(value = "修改接口调用信息", notes = "修改接口调用信息")
    @SysLog("修改接口调用信息" )
    @PutMapping
    public R updateById(@RequestBody ApproveCall approveCall) {
        return R.ok(approveCallService.updateById(approveCall));
    }

    /**
     * 通过id删除接口调用信息
     * @param callId id
     * @return R
     */
    @ApiOperation(value = "通过id删除接口调用信息", notes = "通过id删除接口调用信息")
    @SysLog("通过id删除接口调用信息" )
    @DeleteMapping("/{callId}" )
    public R removeById(@PathVariable String callId) {
        return R.ok(approveCallService.removeById(callId));
    }

}
