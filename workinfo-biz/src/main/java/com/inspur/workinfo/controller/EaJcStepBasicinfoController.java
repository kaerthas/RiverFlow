/*
 *    Copyright (c) 2019-2025, jason All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the yunho.top developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: jason (jj@163.com)
 */

package com.inspur.workinfo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.entity.EaJcServeval;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.inspur.workinfo.entity.EaJcStepBasicinfo;
import com.inspur.workinfo.service.EaJcStepBasicinfoService;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 基本信息
 *
 * @author Jason
 * @date 2020-06-17 11:38:07
 */
@RestController
@AllArgsConstructor
@RequestMapping("/eajcstepbasicinfo" )
@Api(value = "eajcstepbasicinfo", tags = "基本信息管理")
public class EaJcStepBasicinfoController {

    private final EaJcStepBasicinfoService eaJcStepBasicinfoService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param eaJcStepBasicinfo 基本信息
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getEaJcStepBasicinfoPage(Page page, EaJcStepBasicinfo eaJcStepBasicinfo) {
        return R.ok(eaJcStepBasicinfoService.page(page, Wrappers.query(eaJcStepBasicinfo)));
    }


    /**
     * 通过办件编号查询基本信息
     * @param projid projid
     * @return R
     */
    @ApiOperation(value = "通过办件编号查询", notes = "通过办件编号查询")
    @GetMapping("/{projid}" )
    public R getById(@PathVariable("projid" ) String projid) {
        QueryWrapper<EaJcStepBasicinfo> wrapper = new QueryWrapper();
        wrapper.eq("PROJID",projid);
        return R.ok(eaJcStepBasicinfoService.list(wrapper));
    }

    /**
     * 新增基本信息
     * @param eaJcStepBasicinfo 基本信息
     * @return R
     */
    @ApiOperation(value = "新增基本信息", notes = "新增基本信息")
    @SysLog("新增基本信息" )
    @PostMapping
    public R save(@RequestBody EaJcStepBasicinfo eaJcStepBasicinfo) {
        return R.ok(eaJcStepBasicinfoService.save(eaJcStepBasicinfo));
    }

    /**
     * 修改基本信息
     * @param eaJcStepBasicinfo 基本信息
     * @return R
     */
    @ApiOperation(value = "修改基本信息", notes = "修改基本信息")
    @SysLog("修改基本信息" )
    @PutMapping
    public R updateById(@RequestBody EaJcStepBasicinfo eaJcStepBasicinfo) {
        return R.ok(eaJcStepBasicinfoService.updateById(eaJcStepBasicinfo));
    }
//
//    /**
//     * 通过id删除基本信息
//     * @param orgbusno id
//     * @return R
//     */
//    @ApiOperation(value = "通过id删除基本信息", notes = "通过id删除基本信息")
//    @SysLog("通过id删除基本信息" )
//    @DeleteMapping("/{orgbusno}" )
//    public R removeById(@PathVariable String orgbusno) {
//        return R.ok(eaJcStepBasicinfoService.removeById(orgbusno));
//    }

}
