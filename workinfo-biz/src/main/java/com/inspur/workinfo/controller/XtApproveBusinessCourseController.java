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

import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.service.XtApproveBusinessCourseService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 业务过程信息表
 *
 * @author yunho code generator
 * @date 2023-07-12 11:06:40
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapprovebusinesscourse" )
@Api(value = "xtapprovebusinesscourse", tags = "业务过程信息表管理")
public class XtApproveBusinessCourseController {

    private final XtApproveBusinessCourseService xtApproveBusinessCourseService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveBusinessCourse 业务过程信息表
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveBusinessCoursePage(Page page, XtApproveBusinessCourse xtApproveBusinessCourse) {
        return R.ok(xtApproveBusinessCourseService.page(page, Wrappers.query(xtApproveBusinessCourse)));
    }


    /**
     * 通过id查询业务过程信息表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveBusinessCourseService.getById(seqId));
    }

    /**
     * 新增业务过程信息表
     * @param xtApproveBusinessCourse 业务过程信息表
     * @return R
     */
    @ApiOperation(value = "新增业务过程信息表", notes = "新增业务过程信息表")
    @PostMapping
    public R save(@RequestBody XtApproveBusinessCourse xtApproveBusinessCourse) {
        return R.ok(xtApproveBusinessCourseService.save(xtApproveBusinessCourse));
    }

    /**
     * 修改业务过程信息表
     * @param xtApproveBusinessCourse 业务过程信息表
     * @return R
     */
    @ApiOperation(value = "修改业务过程信息表", notes = "修改业务过程信息表")
    @PutMapping
    public R updateById(@RequestBody XtApproveBusinessCourse xtApproveBusinessCourse) {
        return R.ok(xtApproveBusinessCourseService.updateById(xtApproveBusinessCourse));
    }

    /**
     * 通过id删除业务过程信息表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除业务过程信息表", notes = "通过id删除业务过程信息表")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveBusinessCourseService.removeById(seqId));
    }

}
