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
import com.inspur.workinfo.entity.EaJcStepDone;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.inspur.workinfo.entity.EaJcFeeinfo;
import com.inspur.workinfo.service.EaJcFeeinfoService;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 收费明细信息
 *
 * @author Jason
 * @date 2020-06-17 11:38:08
 */
@RestController
@AllArgsConstructor
@RequestMapping("/eajcfeeinfo" )
@Api(value = "eajcfeeinfo", tags = "收费明细信息管理")
public class EaJcFeeinfoController {

    private final EaJcFeeinfoService eaJcFeeinfoService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param eaJcFeeinfo 收费明细信息
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getEaJcFeeinfoPage(Page page, EaJcFeeinfo eaJcFeeinfo) {
        return R.ok(eaJcFeeinfoService.page(page, Wrappers.query(eaJcFeeinfo)));
    }


    /**
     * 通过办件编号查询收费明细信息
     * @param projid projid
     * @return R
     */
    @ApiOperation(value = "通过办件编号查询", notes = "通过办件编号查询")
    @GetMapping("/getbyprojid" )
    public R getById(String projid) {
        QueryWrapper<EaJcFeeinfo> wrapper = new QueryWrapper();
        wrapper.eq("PROJID",projid);
        return R.ok(eaJcFeeinfoService.list(wrapper));
    }

    /**
     * 新增收费明细信息
     * @param eaJcFeeinfo 收费明细信息
     * @return R
     */
    @ApiOperation(value = "新增收费明细信息", notes = "新增收费明细信息")
    @SysLog("新增收费明细信息" )
    @PostMapping
    public R save(@RequestBody EaJcFeeinfo eaJcFeeinfo) {
        return R.ok(eaJcFeeinfoService.save(eaJcFeeinfo));
    }

    /**
     * 修改收费明细信息
     * @param eaJcFeeinfo 收费明细信息
     * @return R
     */
    @ApiOperation(value = "修改收费明细信息", notes = "修改收费明细信息")
    @SysLog("修改收费明细信息" )
    @PutMapping
    public R updateById(@RequestBody EaJcFeeinfo eaJcFeeinfo) {
        return R.ok(eaJcFeeinfoService.updateById(eaJcFeeinfo));
    }

//    /**
//     * 通过id删除收费明细信息
//     * @param orgbusno id
//     * @return R
//     */
//    @ApiOperation(value = "通过id删除收费明细信息", notes = "通过id删除收费明细信息")
//    @SysLog("通过id删除收费明细信息" )
//    @DeleteMapping("/{orgbusno}" )
//    public R removeById(@PathVariable String orgbusno) {
//        return R.ok(eaJcFeeinfoService.removeById(orgbusno));
//    }

}
