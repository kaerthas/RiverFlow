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

import com.inspur.workinfo.entity.XtApproveBusinessEmail;
import com.inspur.workinfo.service.XtApproveBusinessEmailService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 申报邮寄信息
 *
 * @author yunho code generator
 * @date 2023-07-11 14:16:19
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapprovebusinessemail" )
@Api(value = "xtapprovebusinessemail", tags = "申报邮寄信息管理")
public class XtApproveBusinessEmailController {

    private final XtApproveBusinessEmailService xtApproveBusinessEmailService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveBusinessEmail 申报邮寄信息
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveBusinessEmailPage(Page page, XtApproveBusinessEmail xtApproveBusinessEmail) {
        return R.ok(xtApproveBusinessEmailService.page(page, Wrappers.query(xtApproveBusinessEmail)));
    }


    /**
     * 通过id查询申报邮寄信息
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveBusinessEmailService.getById(seqId));
    }

    /**
     * 新增申报邮寄信息
     * @param xtApproveBusinessEmail 申报邮寄信息
     * @return R
     */
    @ApiOperation(value = "新增申报邮寄信息", notes = "新增申报邮寄信息")
    @PostMapping
    public R save(@RequestBody XtApproveBusinessEmail xtApproveBusinessEmail) {
        return R.ok(xtApproveBusinessEmailService.save(xtApproveBusinessEmail));
    }

    /**
     * 修改申报邮寄信息
     * @param xtApproveBusinessEmail 申报邮寄信息
     * @return R
     */
    @ApiOperation(value = "修改申报邮寄信息", notes = "修改申报邮寄信息")
    @PutMapping
    public R updateById(@RequestBody XtApproveBusinessEmail xtApproveBusinessEmail) {
        return R.ok(xtApproveBusinessEmailService.updateById(xtApproveBusinessEmail));
    }

    /**
     * 通过id删除申报邮寄信息
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除申报邮寄信息", notes = "通过id删除申报邮寄信息")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveBusinessEmailService.removeById(seqId));
    }

}
