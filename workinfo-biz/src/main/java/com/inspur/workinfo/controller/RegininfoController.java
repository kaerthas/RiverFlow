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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.inspur.workinfo.entity.Regininfo;
import com.inspur.workinfo.service.RegininfoService;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;


/**
 * ${comments}
 *
 * @author Jason
 * @date 2020-06-17 10:15:03
 */
@ApiIgnore
@RestController
@AllArgsConstructor
@RequestMapping("/regininfo" )
@Api(value = "regininfo", tags = "${comments}管理",hidden = true)
public class RegininfoController {

    private final  RegininfoService regininfoService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param regininfo ${comments}
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getRegininfoPage(Page page, Regininfo regininfo) {
        return R.ok(regininfoService.page(page, Wrappers.query(regininfo)));
    }


    /**
     * 通过id查询${comments}
     * @param reginId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{reginId}" )
    public R getById(@PathVariable("reginId" ) String reginId) {
        return R.ok(regininfoService.getById(reginId));
    }

    /**
     * 新增${comments}
     * @param regininfo ${comments}
     * @return R
     */
    @ApiOperation(value = "新增${comments}", notes = "新增${comments}")
    @SysLog("新增${comments}" )
    @PostMapping
    public R save(@RequestBody Regininfo regininfo) {
        return R.ok(regininfoService.save(regininfo));
    }

    /**
     * 修改${comments}
     * @param regininfo ${comments}
     * @return R
     */
    @ApiOperation(value = "修改${comments}", notes = "修改${comments}")
    @SysLog("修改${comments}" )
    @PutMapping
    public R updateById(@RequestBody Regininfo regininfo) {
        return R.ok(regininfoService.updateById(regininfo));
    }

    /**
     * 通过id删除${comments}
     * @param reginId id
     * @return R
     */
    @ApiOperation(value = "通过id删除${comments}", notes = "通过id删除${comments}")
    @SysLog("通过id删除${comments}" )
    @DeleteMapping("/{reginId}" )
    public R removeById(@PathVariable String reginId) {
        return R.ok(regininfoService.removeById(reginId));
    }

}
