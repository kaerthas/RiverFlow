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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.inspur.workinfo.entity.TmzBzCremationInformation;
import com.inspur.workinfo.service.TmzBzCremationInformationService;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * ${comments}
 *
 * @author yunho code generator
 * @date 2023-01-09 12:26:25
 */
@RestController
@AllArgsConstructor
@RequestMapping("/tmzbzcremationinformation" )
@Api(value = "tmzbzcremationinformation", tags = "${comments}管理")
public class TmzBzCremationInformationController {

    private final  TmzBzCremationInformationService tmzBzCremationInformationService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param tmzBzCremationInformation ${comments}
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getTmzBzCremationInformationPage(Page page, TmzBzCremationInformation tmzBzCremationInformation) {
        return R.ok(tmzBzCremationInformationService.page(page, Wrappers.query(tmzBzCremationInformation)));
    }


    /**
     * 通过id查询${comments}
     * @param id id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{id}" )
    public R getById(@PathVariable("id" ) Integer id) {
        return R.ok(tmzBzCremationInformationService.getById(id));
    }

    /**
     * 新增${comments}
     * @param tmzBzCremationInformation ${comments}
     * @return R
     */
    @ApiOperation(value = "新增${comments}", notes = "新增${comments}")
    @SysLog("新增${comments}" )
    @PostMapping
    public R save(@RequestBody TmzBzCremationInformation tmzBzCremationInformation) {
        return R.ok(tmzBzCremationInformationService.save(tmzBzCremationInformation));
    }

    /**
     * 修改${comments}
     * @param tmzBzCremationInformation ${comments}
     * @return R
     */
    @ApiOperation(value = "修改${comments}", notes = "修改${comments}")
    @SysLog("修改${comments}" )
    @PutMapping
    public R updateById(@RequestBody TmzBzCremationInformation tmzBzCremationInformation) {
        return R.ok(tmzBzCremationInformationService.updateById(tmzBzCremationInformation));
    }

    /**
     * 通过id删除${comments}
     * @param id id
     * @return R
     */
    @ApiOperation(value = "通过id删除${comments}", notes = "通过id删除${comments}")
    @SysLog("通过id删除${comments}" )
    @DeleteMapping("/{id}" )
    public R removeById(@PathVariable Integer id) {
        return R.ok(tmzBzCremationInformationService.removeById(id));
    }


    /**
     * 给外部使用,根据姓名和身份证号获取殡仪信息
     */
    @ApiOperation(value = "根据姓名和身份证号获取殡仪信息", notes = "根据姓名和身份证号获取殡仪信息")
    @SysLog("殡仪信息获取" )
    @PostMapping("/getInfoByIdAndName")
    public R getInfoByIdAndName(@RequestParam(value = "cardCode") String cardCode,
                           @RequestParam(value = "name",required = false) String name) {
        JSONObject info = tmzBzCremationInformationService.getInfoByIdAndName(cardCode,name);
        Boolean flag = info.getBoolean("flag");
        String message = info.getString("message");
        if(flag) {
            return R.ok(info);
        }else {
            return R.failed(message);
        }
    }

}
