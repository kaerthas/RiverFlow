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
import com.inspur.workinfo.entity.EaJcFeeinfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.inspur.workinfo.entity.EaJcServeval;
import com.inspur.workinfo.service.EaJcServevalService;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 服务评价信息
 *
 * @author Jason
 * @date 2020-06-17 11:38:08
 */
@RestController
@AllArgsConstructor
@RequestMapping("/eajcserveval" )
@Api(value = "eajcserveval", tags = "服务评价信息管理")
public class EaJcServevalController {

    private final EaJcServevalService eaJcServevalService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param eaJcServeval 服务评价信息
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getEaJcServevalPage(Page page, EaJcServeval eaJcServeval) {
        return R.ok(eaJcServevalService.page(page, Wrappers.query(eaJcServeval)));
    }


    /**
     * 通过办件编号查询服务评价信息
     * @param projid projid
     * @return R
     */
    @ApiOperation(value = "通过办件编号查询", notes = "通过办件编号查询")
    @GetMapping("/{projid}" )
    public R getById(@PathVariable("projid" ) String projid) {
        QueryWrapper<EaJcServeval> wrapper = new QueryWrapper();
        wrapper.eq("PROJID",projid);
        return R.ok(eaJcServevalService.list(wrapper));
    }

    /**
     * 新增服务评价信息
     * @param eaJcServeval 服务评价信息
     * @return R
     */
    @ApiOperation(value = "新增服务评价信息", notes = "新增服务评价信息")
    @SysLog("新增服务评价信息" )
    @PostMapping
    public R save(@RequestBody EaJcServeval eaJcServeval) {
        return R.ok(eaJcServevalService.save(eaJcServeval));
    }

    /**
     * 修改服务评价信息
     * @param eaJcServeval 服务评价信息
     * @return R
     */
    @ApiOperation(value = "修改服务评价信息", notes = "修改服务评价信息")
    @SysLog("修改服务评价信息" )
    @PutMapping
    public R updateById(@RequestBody EaJcServeval eaJcServeval) {
        return R.ok(eaJcServevalService.updateById(eaJcServeval));
    }
//
//    /**
//     * 通过id删除服务评价信息
//     * @param orgbusno id
//     * @return R
//     */
//    @ApiOperation(value = "通过id删除服务评价信息", notes = "通过id删除服务评价信息")
//    @SysLog("通过id删除服务评价信息" )
//    @DeleteMapping("/{orgbusno}" )
//    public R removeById(@PathVariable String orgbusno) {
//        return R.ok(eaJcServevalService.removeById(orgbusno));
//    }

}
