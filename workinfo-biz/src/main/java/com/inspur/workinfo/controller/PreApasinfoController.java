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

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.entity.EaJcStepDone;
import com.inspur.workinfo.entity.EaJcStepSpecialnode;
import com.inspur.workinfo.enums.ProjectLinkType;
import com.inspur.workinfo.enums.ProjectStateType;
import com.inspur.workinfo.service.PreApasinfoVoService;
import com.inspur.workinfo.vo.PreApasinfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.inspur.workinfo.entity.PreApasinfo;
import com.inspur.workinfo.service.PreApasinfoService;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 登记（申报）信息
 *
 * @author Jason
 * @date 2020-06-17 10:15:05
 */
@RestController
@AllArgsConstructor
@RequestMapping("/preapasinfo" )
@Api(value = "preapasinfo", tags = "登记（申报）信息管理")
public class PreApasinfoController {

    private final  PreApasinfoService preApasinfoService;
    private final PreApasinfoVoService preApasinfoVoService;

//    /**
//     * 分页查询
//     * @param page 分页对象
//     * @param preApasinfo 登记（申报）信息
//     * @return
//     */
//    @ApiOperation(value = "分页查询", notes = "分页查询")
//    @GetMapping("/page" )
//    public R getPreApasinfoPage(Page page, PreApasinfo preApasinfo) {
//        return R.ok(preApasinfoService.page(page, Wrappers.query(preApasinfo)));
//    }

    /**
     * 分页查询
     * @param page 分页对象
     * @param preApasinfo 登记（申报）信息,projectstateType
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "projectstateType 1-受理中，2-已办结，3-已撤销,null-所有")
    @GetMapping("/page" )
    public R getPreApasinfoPage(Page page, PreApasinfoVo preApasinfo) {
        if (preApasinfo == null || StrUtil.isBlank(preApasinfo.getProjectstateType())){
           return R.ok(preApasinfoVoService.page(page, Wrappers.query(preApasinfo)));
        }
        QueryWrapper<PreApasinfoVo> wrapper = new QueryWrapper(preApasinfo);
        if(ProjectLinkType.accepted.getValue().equals(preApasinfo.getProjectstateType())){
            wrapper.in("PROJECTSTATE", ProjectStateType.accepted.getValue(),
                    ProjectStateType.subcorrected.getValue(),
//                    ProjectStateType.unaccepted.getValue(),
                    ProjectStateType.doing.getValue(),
                    ProjectStateType.dospecilup.getValue());
        }else if(ProjectLinkType.done.getValue().equals(preApasinfo.getProjectstateType())){
//            wrapper.in("PROJECTSTATE", ProjectStateType.doing.getValue(),
//                    ProjectStateType.dospecilup.getValue());
            wrapper.in("PROJECTSTATE", ProjectStateType.done.getValue(),
                    ProjectStateType.turndone.getValue(),
                    ProjectStateType.baddone.getValue(),
                    ProjectStateType.backdone.getValue());
        }else if(ProjectLinkType.canceled.getValue().equals(preApasinfo.getProjectstateType())){
//            wrapper.eq("DATASTATE", "0");
            wrapper.in("PROJECTSTATE", ProjectStateType.unaccepted.getValue()
                    ,ProjectStateType.preacceptedback.getValue());
        } else if (ProjectLinkType.submited.getValue().equals(preApasinfo.getProjectstateType())){
            wrapper.in("PROJECTSTATE", ProjectStateType.preaccepted.getValue());
        }
        return R.ok(preApasinfoVoService.page(page, wrapper));
    }


    /**
     * 通过办件编号查询登记（申报）信息
     * @param projid projid
     * @return R
     */
    @ApiOperation(value = "通过办件编号查询", notes = "通过办件编号查询")
    @GetMapping("/getbyprojid" )
    public R getById(String projid) {
        QueryWrapper<PreApasinfoVo> wrapper = new QueryWrapper();
        wrapper.eq("PROJID",projid);
        wrapper.orderByDesc("LOCALTIME");
        return R.ok(preApasinfoVoService.getOne(wrapper));
    }

    /**
     * 新增登记（申报）信息
     * @param preApasinfo 登记（申报）信息
     * @return R
     */
    @ApiOperation(value = "新增登记（申报）信息", notes = "新增登记（申报）信息")
    @SysLog("新增登记（申报）信息" )
    @PostMapping
    public R save(@RequestBody PreApasinfo preApasinfo) {
        return R.ok(preApasinfoService.save(preApasinfo));
    }

    /**
     * 修改登记（申报）信息
     * @param preApasinfo 登记（申报）信息
     * @return R
     */
    @ApiOperation(value = "修改登记（申报）信息", notes = "修改登记（申报）信息")
    @SysLog("修改登记（申报）信息" )
    @PostMapping("/updateById")
    public R updateById(@RequestBody PreApasinfo preApasinfo) {
        return R.ok(preApasinfoService.updateById(preApasinfo));
    }

//    /**
//     * 通过id删除登记（申报）信息
//     * @param projid id
//     * @return R
//     */
//    @ApiOperation(value = "通过id删除登记（申报）信息", notes = "通过id删除登记（申报）信息")
//    @SysLog("通过id删除登记（申报）信息" )
//    @DeleteMapping("/{projid}" )
//    public R removeById(@PathVariable String projid) {
//        return R.ok(preApasinfoService.removeById(projid));
//    }

}
