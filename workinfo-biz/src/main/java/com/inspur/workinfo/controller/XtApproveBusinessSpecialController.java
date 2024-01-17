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
import com.inspur.workinfo.entity.XtApproveBusinessSpecial;
import com.inspur.workinfo.service.XtApproveBusinessSpecialService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 特殊环节业务表
 *
 * @author yunho code generator
 * @date 2024-01-15 14:20:13
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapprovebusinessspecial" )
@Api(value = "xtapprovebusinessspecial", tags = "特殊环节业务表管理")
public class XtApproveBusinessSpecialController {

    private final XtApproveBusinessSpecialService xtApproveBusinessSpecialService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveBusinessSpecial 特殊环节业务表
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveBusinessSpecialPage(Page page, XtApproveBusinessSpecial xtApproveBusinessSpecial) {
        return R.ok(xtApproveBusinessSpecialService.page(page, Wrappers.query(xtApproveBusinessSpecial)));
    }


    /**
     * 通过id查询特殊环节业务表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveBusinessSpecialService.getById(seqId));
    }

    /**
     * 新增特殊环节业务表
     * @param xtApproveBusinessSpecial 特殊环节业务表
     * @return R
     */
    @ApiOperation(value = "新增特殊环节业务表", notes = "新增特殊环节业务表")
    @SysLog("新增特殊环节业务表" )
    @PostMapping
    public R save(@RequestBody XtApproveBusinessSpecial xtApproveBusinessSpecial) {
        return R.ok(xtApproveBusinessSpecialService.save(xtApproveBusinessSpecial));
    }

    /**
     * 修改特殊环节业务表
     * @param xtApproveBusinessSpecial 特殊环节业务表
     * @return R
     */
    @ApiOperation(value = "修改特殊环节业务表", notes = "修改特殊环节业务表")
    @SysLog("修改特殊环节业务表" )
    @PutMapping
    public R updateById(@RequestBody XtApproveBusinessSpecial xtApproveBusinessSpecial) {
        return R.ok(xtApproveBusinessSpecialService.updateById(xtApproveBusinessSpecial));
    }

    /**
     * 通过id删除特殊环节业务表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除特殊环节业务表", notes = "通过id删除特殊环节业务表")
    @SysLog("通过id删除特殊环节业务表" )
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveBusinessSpecialService.removeById(seqId));
    }

}
