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

import com.inspur.workinfo.entity.XtApproveBusinessOutcome;
import com.inspur.workinfo.service.XtApproveBusinessOutcomeService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 业务信息结果物存量接口
 *
 * @author yunho code generator
 * @date 2023-07-17 17:50:43
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapprovebusinessoutcome" )
@Api(value = "xtapprovebusinessoutcome", tags = "业务信息结果物存量接口管理")
public class XtApproveBusinessOutcomeController {

    private final XtApproveBusinessOutcomeService xtApproveBusinessOutcomeService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveBusinessOutcome 业务信息结果物存量接口
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveBusinessOutcomePage(Page page, XtApproveBusinessOutcome xtApproveBusinessOutcome) {
        return R.ok(xtApproveBusinessOutcomeService.page(page, Wrappers.query(xtApproveBusinessOutcome)));
    }


    /**
     * 通过id查询业务信息结果物存量接口
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveBusinessOutcomeService.getById(seqId));
    }

    /**
     * 新增业务信息结果物存量接口
     * @param xtApproveBusinessOutcome 业务信息结果物存量接口
     * @return R
     */
    @ApiOperation(value = "新增业务信息结果物存量接口", notes = "新增业务信息结果物存量接口")
    @PostMapping
    public R save(@RequestBody XtApproveBusinessOutcome xtApproveBusinessOutcome) {
        return R.ok(xtApproveBusinessOutcomeService.save(xtApproveBusinessOutcome));
    }

    /**
     * 修改业务信息结果物存量接口
     * @param xtApproveBusinessOutcome 业务信息结果物存量接口
     * @return R
     */
    @ApiOperation(value = "修改业务信息结果物存量接口", notes = "修改业务信息结果物存量接口")
    @PutMapping
    public R updateById(@RequestBody XtApproveBusinessOutcome xtApproveBusinessOutcome) {
        return R.ok(xtApproveBusinessOutcomeService.updateById(xtApproveBusinessOutcome));
    }

    /**
     * 通过id删除业务信息结果物存量接口
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除业务信息结果物存量接口", notes = "通过id删除业务信息结果物存量接口")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveBusinessOutcomeService.removeById(seqId));
    }

}
