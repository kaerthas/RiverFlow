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

import com.inspur.workinfo.entity.ApproveCallResult;
import com.inspur.workinfo.service.ApproveCallResultService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 接口调用结果信息
 *
 * @author yunho code generator
 * @date 2023-07-07 17:04:52
 */
@RestController
@AllArgsConstructor
@RequestMapping("/approvecallresult" )
@Api(value = "approvecallresult", tags = "接口调用结果信息管理")
public class ApproveCallResultController {

    private final ApproveCallResultService approveCallResultService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param approveCallResult 接口调用结果信息
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getApproveCallResultPage(Page page, ApproveCallResult approveCallResult) {
        return R.ok(approveCallResultService.page(page, Wrappers.query(approveCallResult)));
    }


    /**
     * 通过id查询接口调用结果信息
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(approveCallResultService.getById(seqId));
    }

    /**
     * 新增接口调用结果信息
     * @param approveCallResult 接口调用结果信息
     * @return R
     */
    @ApiOperation(value = "新增接口调用结果信息", notes = "新增接口调用结果信息")
    @PostMapping
    public R save(@RequestBody ApproveCallResult approveCallResult) {
        return R.ok(approveCallResultService.save(approveCallResult));
    }

    /**
     * 修改接口调用结果信息
     * @param approveCallResult 接口调用结果信息
     * @return R
     */
    @ApiOperation(value = "修改接口调用结果信息", notes = "修改接口调用结果信息")
    @PutMapping
    public R updateById(@RequestBody ApproveCallResult approveCallResult) {
        return R.ok(approveCallResultService.updateById(approveCallResult));
    }

    /**
     * 通过id删除接口调用结果信息
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除接口调用结果信息", notes = "通过id删除接口调用结果信息")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(approveCallResultService.removeById(seqId));
    }

}
