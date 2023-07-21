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

import com.inspur.workinfo.entity.XtApproveBusinessCorrection;
import com.inspur.workinfo.service.XtApproveBusinessCorrectionService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 补齐补正提交信息主表
 *
 * @author yunho code generator
 * @date 2023-07-18 17:04:11
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapprovebusinesscorrection" )
@Api(value = "xtapprovebusinesscorrection", tags = "补齐补正提交信息主表管理")
public class XtApproveBusinessCorrectionController {

    private final XtApproveBusinessCorrectionService xtApproveBusinessCorrectionService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveBusinessCorrection 补齐补正提交信息主表
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveBusinessCorrectionPage(Page page, XtApproveBusinessCorrection xtApproveBusinessCorrection) {
        return R.ok(xtApproveBusinessCorrectionService.page(page, Wrappers.query(xtApproveBusinessCorrection)));
    }


    /**
     * 通过id查询补齐补正提交信息主表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveBusinessCorrectionService.getById(seqId));
    }

    /**
     * 新增补齐补正提交信息主表
     * @param xtApproveBusinessCorrection 补齐补正提交信息主表
     * @return R
     */
    @ApiOperation(value = "新增补齐补正提交信息主表", notes = "新增补齐补正提交信息主表")
    @PostMapping
    public R save(@RequestBody XtApproveBusinessCorrection xtApproveBusinessCorrection) {
        return R.ok(xtApproveBusinessCorrectionService.save(xtApproveBusinessCorrection));
    }

    /**
     * 修改补齐补正提交信息主表
     * @param xtApproveBusinessCorrection 补齐补正提交信息主表
     * @return R
     */
    @ApiOperation(value = "修改补齐补正提交信息主表", notes = "修改补齐补正提交信息主表")
    @PutMapping
    public R updateById(@RequestBody XtApproveBusinessCorrection xtApproveBusinessCorrection) {
        return R.ok(xtApproveBusinessCorrectionService.updateById(xtApproveBusinessCorrection));
    }

    /**
     * 通过id删除补齐补正提交信息主表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除补齐补正提交信息主表", notes = "通过id删除补齐补正提交信息主表")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveBusinessCorrectionService.removeById(seqId));
    }

}
