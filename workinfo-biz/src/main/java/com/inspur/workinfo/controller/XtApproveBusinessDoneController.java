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

import com.inspur.workinfo.entity.XtApproveBusinessDone;
import com.inspur.workinfo.service.XtApproveBusinessDoneService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 业务办结信息表
 *
 * @author yunho code generator
 * @date 2023-07-13 15:25:11
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapprovebusinessdone" )
@Api(value = "xtapprovebusinessdone", tags = "业务办结信息表管理")
public class XtApproveBusinessDoneController {

    private final XtApproveBusinessDoneService xtApproveBusinessDoneService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveBusinessDone 业务办结信息表
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveBusinessDonePage(Page page, XtApproveBusinessDone xtApproveBusinessDone) {
        return R.ok(xtApproveBusinessDoneService.page(page, Wrappers.query(xtApproveBusinessDone)));
    }


    /**
     * 通过id查询业务办结信息表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveBusinessDoneService.getById(seqId));
    }

    /**
     * 新增业务办结信息表
     * @param xtApproveBusinessDone 业务办结信息表
     * @return R
     */
    @ApiOperation(value = "新增业务办结信息表", notes = "新增业务办结信息表")
    @PostMapping
    public R save(@RequestBody XtApproveBusinessDone xtApproveBusinessDone) {
        return R.ok(xtApproveBusinessDoneService.save(xtApproveBusinessDone));
    }

    /**
     * 修改业务办结信息表
     * @param xtApproveBusinessDone 业务办结信息表
     * @return R
     */
    @ApiOperation(value = "修改业务办结信息表", notes = "修改业务办结信息表")
    @PutMapping
    public R updateById(@RequestBody XtApproveBusinessDone xtApproveBusinessDone) {
        return R.ok(xtApproveBusinessDoneService.updateById(xtApproveBusinessDone));
    }

    /**
     * 通过id删除业务办结信息表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除业务办结信息表", notes = "通过id删除业务办结信息表")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveBusinessDoneService.removeById(seqId));
    }

}
