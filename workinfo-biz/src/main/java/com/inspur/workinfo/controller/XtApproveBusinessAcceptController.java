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

import com.inspur.workinfo.entity.XtApproveBusinessAccept;
import com.inspur.workinfo.service.XtApproveBusinessAcceptService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 协同调度受理信息表
 *
 * @author yunho code generator
 * @date 2023-07-13 09:23:20
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapprovebusinessaccept" )
@Api(value = "xtapprovebusinessaccept", tags = "协同调度受理信息表管理")
public class XtApproveBusinessAcceptController {

    private final XtApproveBusinessAcceptService xtApproveBusinessAcceptService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveBusinessAccept 协同调度受理信息表
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveBusinessAcceptPage(Page page, XtApproveBusinessAccept xtApproveBusinessAccept) {
        return R.ok(xtApproveBusinessAcceptService.page(page, Wrappers.query(xtApproveBusinessAccept)));
    }


    /**
     * 通过id查询协同调度受理信息表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveBusinessAcceptService.getById(seqId));
    }

    /**
     * 新增协同调度受理信息表
     * @param xtApproveBusinessAccept 协同调度受理信息表
     * @return R
     */
    @ApiOperation(value = "新增协同调度受理信息表", notes = "新增协同调度受理信息表")
    @PostMapping
    public R save(@RequestBody XtApproveBusinessAccept xtApproveBusinessAccept) {
        return R.ok(xtApproveBusinessAcceptService.save(xtApproveBusinessAccept));
    }

    /**
     * 修改协同调度受理信息表
     * @param xtApproveBusinessAccept 协同调度受理信息表
     * @return R
     */
    @ApiOperation(value = "修改协同调度受理信息表", notes = "修改协同调度受理信息表")
    @PutMapping
    public R updateById(@RequestBody XtApproveBusinessAccept xtApproveBusinessAccept) {
        return R.ok(xtApproveBusinessAcceptService.updateById(xtApproveBusinessAccept));
    }

    /**
     * 通过id删除协同调度受理信息表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除协同调度受理信息表", notes = "通过id删除协同调度受理信息表")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveBusinessAcceptService.removeById(seqId));
    }

}
