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

import com.inspur.workinfo.entity.XtApproveItemflowConfig;
import com.inspur.workinfo.service.XtApproveItemflowConfigService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 事项办理流程配置
 *
 * @author yunho code generator
 * @date 2023-07-12 11:06:41
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapproveitemflowconfig" )
@Api(value = "xtapproveitemflowconfig", tags = "事项办理流程配置管理")
public class XtApproveItemflowConfigController {

    private final XtApproveItemflowConfigService xtApproveItemflowConfigService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveItemflowConfig 事项办理流程配置
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveItemflowConfigPage(Page page, XtApproveItemflowConfig xtApproveItemflowConfig) {
        return R.ok(xtApproveItemflowConfigService.page(page, Wrappers.query(xtApproveItemflowConfig)));
    }


    /**
     * 通过id查询事项办理流程配置
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveItemflowConfigService.getById(seqId));
    }

    /**
     * 新增事项办理流程配置
     * @param xtApproveItemflowConfig 事项办理流程配置
     * @return R
     */
    @ApiOperation(value = "新增事项办理流程配置", notes = "新增事项办理流程配置")
    @PostMapping
    public R save(@RequestBody XtApproveItemflowConfig xtApproveItemflowConfig) {
        return R.ok(xtApproveItemflowConfigService.save(xtApproveItemflowConfig));
    }

    /**
     * 修改事项办理流程配置
     * @param xtApproveItemflowConfig 事项办理流程配置
     * @return R
     */
    @ApiOperation(value = "修改事项办理流程配置", notes = "修改事项办理流程配置")
    @PutMapping
    public R updateById(@RequestBody XtApproveItemflowConfig xtApproveItemflowConfig) {
        return R.ok(xtApproveItemflowConfigService.updateById(xtApproveItemflowConfig));
    }

    /**
     * 通过id删除事项办理流程配置
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除事项办理流程配置", notes = "通过id删除事项办理流程配置")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveItemflowConfigService.removeById(seqId));
    }

}
