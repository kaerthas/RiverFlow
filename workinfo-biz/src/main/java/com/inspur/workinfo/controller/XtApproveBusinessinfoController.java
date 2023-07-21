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

import com.inspur.workinfo.entity.XtApproveBusinessinfo;
import com.inspur.workinfo.service.XtApproveBusinessinfoService;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-07-07 17:04:51
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapprovebusinessinfo" )
@Api(value = "xtapprovebusinessinfo", tags = "${comments}管理")
public class XtApproveBusinessinfoController {

    private final XtApproveBusinessinfoService xtApproveBusinessinfoService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveBusinessinfo ${comments}
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveBusinessinfoPage(Page page, XtApproveBusinessinfo xtApproveBusinessinfo) {
        return R.ok(xtApproveBusinessinfoService.page(page, Wrappers.query(xtApproveBusinessinfo)));
    }


    /**
     * 通过id查询${comments}
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveBusinessinfoService.getById(seqId));
    }

    /**
     * 新增${comments}
     * @param xtApproveBusinessinfo ${comments}
     * @return R
     */
    @ApiOperation(value = "新增${comments}", notes = "新增${comments}")
    @PostMapping
    public R save(@RequestBody XtApproveBusinessinfo xtApproveBusinessinfo) {
        return R.ok(xtApproveBusinessinfoService.save(xtApproveBusinessinfo));
    }

    /**
     * 修改${comments}
     * @param xtApproveBusinessinfo ${comments}
     * @return R
     */
    @ApiOperation(value = "修改${comments}", notes = "修改${comments}")
    @PutMapping
    public R updateById(@RequestBody XtApproveBusinessinfo xtApproveBusinessinfo) {
        return R.ok(xtApproveBusinessinfoService.updateById(xtApproveBusinessinfo));
    }

    /**
     * 通过id删除${comments}
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除${comments}", notes = "通过id删除${comments}")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveBusinessinfoService.removeById(seqId));
    }

}
