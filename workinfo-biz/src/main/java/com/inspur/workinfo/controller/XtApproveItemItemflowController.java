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
import com.inspur.workinfo.entity.XtApproveItemItemflow;
import com.inspur.workinfo.service.XtApproveItemItemflowService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 事项与流程关联关系表
 *
 * @author yunho code generator
 * @date 2024-01-19 10:30:14
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapproveitemitemflow" )
@Api(value = "xtapproveitemitemflow", tags = "事项与流程关联关系表管理")
public class XtApproveItemItemflowController {

    private final XtApproveItemItemflowService xtApproveItemItemflowService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveItemItemflow 事项与流程关联关系表
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveItemItemflowPage(Page page, XtApproveItemItemflow xtApproveItemItemflow) {
        return R.ok(xtApproveItemItemflowService.page(page, Wrappers.query(xtApproveItemItemflow)));
    }


    /**
     * 通过id查询事项与流程关联关系表
     * @param itemSxbm id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{itemSxbm}" )
    public R getById(@PathVariable("itemSxbm" ) String itemSxbm) {
        return R.ok(xtApproveItemItemflowService.getById(itemSxbm));
    }

    /**
     * 新增事项与流程关联关系表
     * @param xtApproveItemItemflow 事项与流程关联关系表
     * @return R
     */
    @ApiOperation(value = "新增事项与流程关联关系表", notes = "新增事项与流程关联关系表")
    @SysLog("新增事项与流程关联关系表" )
    @PostMapping
    public R save(@RequestBody XtApproveItemItemflow xtApproveItemItemflow) {
        return R.ok(xtApproveItemItemflowService.save(xtApproveItemItemflow));
    }

    /**
     * 修改事项与流程关联关系表
     * @param xtApproveItemItemflow 事项与流程关联关系表
     * @return R
     */
    @ApiOperation(value = "修改事项与流程关联关系表", notes = "修改事项与流程关联关系表")
    @SysLog("修改事项与流程关联关系表" )
    @PutMapping
    public R updateById(@RequestBody XtApproveItemItemflow xtApproveItemItemflow) {
        return R.ok(xtApproveItemItemflowService.updateById(xtApproveItemItemflow));
    }

    /**
     * 通过id删除事项与流程关联关系表
     * @param itemSxbm id
     * @return R
     */
    @ApiOperation(value = "通过id删除事项与流程关联关系表", notes = "通过id删除事项与流程关联关系表")
    @SysLog("通过id删除事项与流程关联关系表" )
    @DeleteMapping("/{itemSxbm}" )
    public R removeById(@PathVariable String itemSxbm) {
        return R.ok(xtApproveItemItemflowService.removeById(itemSxbm));
    }

}
