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

import com.inspur.workinfo.entity.XtApproveBusinessXmlConfig;
import com.inspur.workinfo.service.XtApproveBusinessXmlConfigService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 业务表单字段配置进入
 *
 * @author yunho code generator
 * @date 2023-07-10 16:25:35
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapprovebusinessxmlconfig" )
@Api(value = "xtapprovebusinessxmlconfig", tags = "业务表单字段配置进入管理")
public class XtApproveBusinessXmlConfigController {

    private final XtApproveBusinessXmlConfigService xtApproveBusinessXmlConfigService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveBusinessXmlConfig 业务表单字段配置进入
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveBusinessXmlConfigPage(Page page, XtApproveBusinessXmlConfig xtApproveBusinessXmlConfig) {
        return R.ok(xtApproveBusinessXmlConfigService.page(page, Wrappers.query(xtApproveBusinessXmlConfig)));
    }


    /**
     * 通过id查询业务表单字段配置进入
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveBusinessXmlConfigService.getById(seqId));
    }

    /**
     * 新增业务表单字段配置进入
     * @param xtApproveBusinessXmlConfig 业务表单字段配置进入
     * @return R
     */
    @ApiOperation(value = "新增业务表单字段配置进入", notes = "新增业务表单字段配置进入")
    @PostMapping
    public R save(@RequestBody XtApproveBusinessXmlConfig xtApproveBusinessXmlConfig) {
        return R.ok(xtApproveBusinessXmlConfigService.save(xtApproveBusinessXmlConfig));
    }

    /**
     * 修改业务表单字段配置进入
     * @param xtApproveBusinessXmlConfig 业务表单字段配置进入
     * @return R
     */
    @ApiOperation(value = "修改业务表单字段配置进入", notes = "修改业务表单字段配置进入")
    @PutMapping
    public R updateById(@RequestBody XtApproveBusinessXmlConfig xtApproveBusinessXmlConfig) {
        return R.ok(xtApproveBusinessXmlConfigService.updateById(xtApproveBusinessXmlConfig));
    }

    /**
     * 通过id删除业务表单字段配置进入
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除业务表单字段配置进入", notes = "通过id删除业务表单字段配置进入")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveBusinessXmlConfigService.removeById(seqId));
    }

}
