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
import com.inspur.workinfo.entity.PreApasinfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.inspur.workinfo.entity.PreCommForm;
import com.inspur.workinfo.service.PreCommFormService;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 登记（申报）信息业务表单信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:04
 */
@RestController
@AllArgsConstructor
@RequestMapping("/precommform" )
@Api(value = "precommform", tags = "登记（申报）信息业务表单信息管理")
public class PreCommFormController {

    private final  PreCommFormService preCommFormService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param preCommForm 登记（申报）信息业务表单信息
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getPreCommFormPage(Page page, PreCommForm preCommForm) {
        return R.ok(preCommFormService.page(page, Wrappers.query(preCommForm)));
    }


    /**
     * 通过办件编号查询登记（申报）信息业务表单信息
     * @param projid projid
     * @return R
     */
    @ApiOperation(value = "通过办件编号查询", notes = "通过办件编号查询")
    @GetMapping("/getbyprojid" )
    public R getById(String projid) {
        QueryWrapper<PreCommForm> wrapper = new QueryWrapper();
        wrapper.eq("PROJID",projid);
        return R.ok(preCommFormService.list(wrapper));
    }

    /**
     * 新增登记（申报）信息业务表单信息
     * @param preCommForm 登记（申报）信息业务表单信息
     * @return R
     */
    @ApiOperation(value = "新增登记（申报）信息业务表单信息", notes = "新增登记（申报）信息业务表单信息")
    @SysLog("新增登记（申报）信息业务表单信息" )
    @PostMapping
    public R save(@RequestBody PreCommForm preCommForm) {
        return R.ok(preCommFormService.save(preCommForm));
    }

    /**
     * 修改登记（申报）信息业务表单信息
     * @param preCommForm 登记（申报）信息业务表单信息
     * @return R
     */
    @ApiOperation(value = "修改登记（申报）信息业务表单信息", notes = "修改登记（申报）信息业务表单信息")
    @SysLog("修改登记（申报）信息业务表单信息" )
    @PutMapping
    public R updateById(@RequestBody PreCommForm preCommForm) {
        return R.ok(preCommFormService.updateById(preCommForm));
    }

//    /**
//     * 通过id删除登记（申报）信息业务表单信息
//     * @param unid id
//     * @return R
//     */
//    @ApiOperation(value = "通过id删除登记（申报）信息业务表单信息", notes = "通过id删除登记（申报）信息业务表单信息")
//    @SysLog("通过id删除登记（申报）信息业务表单信息" )
//    @DeleteMapping("/{unid}" )
//    public R removeById(@PathVariable String unid) {
//        return R.ok(preCommFormService.removeById(unid));
//    }

}
