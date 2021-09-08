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
import com.inspur.workinfo.entity.EaJcStepBasicinfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.inspur.workinfo.entity.EaJcStepProc;
import com.inspur.workinfo.service.EaJcStepProcService;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 审批过程信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:07
 */
@RestController
@AllArgsConstructor
@RequestMapping("/eajcstepproc" )
@Api(value = "eajcstepproc", tags = "审批过程信息管理")
public class EaJcStepProcController {

    private final EaJcStepProcService eaJcStepProcService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param eaJcStepProc 审批过程信息
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getEaJcStepProcPage(Page page, EaJcStepProc eaJcStepProc) {
        QueryWrapper<EaJcStepProc> wrapper = new QueryWrapper(eaJcStepProc);
        wrapper.orderByAsc("occurtime");
        return R.ok(eaJcStepProcService.page(page, wrapper));
    }


    /**
     * 通过办件编号查询审批过程信息
     * @param projid projid
     * @return R
     */
    @ApiOperation(value = "通过办件编号查询", notes = "通过办件编号查询")
    @GetMapping("/getbyprojid" )
    public R getById(String projid) {
        QueryWrapper<EaJcStepProc> wrapper = new QueryWrapper();
        wrapper.like("PROJID",projid);
        return R.ok(eaJcStepProcService.list(wrapper));
    }

    /**
     * 新增审批过程信息
     * @param eaJcStepProc 审批过程信息
     * @return R
     */
    @ApiOperation(value = "新增审批过程信息", notes = "新增审批过程信息")
    @SysLog("新增审批过程信息" )
    @PostMapping
    public R save(@RequestBody EaJcStepProc eaJcStepProc) {
        return R.ok(eaJcStepProcService.save(eaJcStepProc));
    }

    /**
     * 修改审批过程信息
     * @param eaJcStepProc 审批过程信息
     * @return R
     */
    @ApiOperation(value = "修改审批过程信息", notes = "修改审批过程信息")
    @SysLog("修改审批过程信息" )
    @PutMapping
    public R updateById(@RequestBody EaJcStepProc eaJcStepProc) {
        return R.ok(eaJcStepProcService.updateById(eaJcStepProc));
    }

//    /**
//     * 通过id删除审批过程信息
//     * @param orgbusno id
//     * @return R
//     */
//    @ApiOperation(value = "通过id删除审批过程信息", notes = "通过id删除审批过程信息")
//    @SysLog("通过id删除审批过程信息" )
//    @DeleteMapping("/{orgbusno}" )
//    public R removeById(@PathVariable String orgbusno) {
//        return R.ok(eaJcStepProcService.removeById(orgbusno));
//    }

}
