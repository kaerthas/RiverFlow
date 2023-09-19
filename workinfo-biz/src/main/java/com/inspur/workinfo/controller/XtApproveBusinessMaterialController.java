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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.inspur.workinfo.entity.XtApproveBusinessMaterial;
import com.inspur.workinfo.service.XtApproveBusinessMaterialService;
import com.inspur.workinfo.util.HttpClientUtils;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;


/**
 * 业务材料信息表
 *
 * @author yunho code generator
 * @date 2023-07-11 18:09:24
 */
@RestController
@AllArgsConstructor
@RequestMapping("/xtapprovebusinessmaterial" )
@Api(value = "xtapprovebusinessmaterial", tags = "业务材料信息表管理")
public class XtApproveBusinessMaterialController {

    private final XtApproveBusinessMaterialService xtApproveBusinessMaterialService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param xtApproveBusinessMaterial 业务材料信息表
     * @return
     */
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/page" )
    public R getXtApproveBusinessMaterialPage(Page page, XtApproveBusinessMaterial xtApproveBusinessMaterial) {
        return R.ok(xtApproveBusinessMaterialService.page(page, Wrappers.query(xtApproveBusinessMaterial)));
    }


    /**
     * 通过id查询业务材料信息表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/{seqId}" )
    public R getById(@PathVariable("seqId" ) String seqId) {
        return R.ok(xtApproveBusinessMaterialService.getById(seqId));
    }

    /**
     * 新增业务材料信息表
     * @param xtApproveBusinessMaterial 业务材料信息表
     * @return R
     */
    @ApiOperation(value = "新增业务材料信息表", notes = "新增业务材料信息表")
    @PostMapping
    public R save(@RequestBody XtApproveBusinessMaterial xtApproveBusinessMaterial) {
        return R.ok(xtApproveBusinessMaterialService.save(xtApproveBusinessMaterial));
    }

    /**
     * 修改业务材料信息表
     * @param xtApproveBusinessMaterial 业务材料信息表
     * @return R
     */
    @ApiOperation(value = "修改业务材料信息表", notes = "修改业务材料信息表")
    @PutMapping
    public R updateById(@RequestBody XtApproveBusinessMaterial xtApproveBusinessMaterial) {
        return R.ok(xtApproveBusinessMaterialService.updateById(xtApproveBusinessMaterial));
    }

    /**
     * 通过id删除业务材料信息表
     * @param seqId id
     * @return R
     */
    @ApiOperation(value = "通过id删除业务材料信息表", notes = "通过id删除业务材料信息表")
    @DeleteMapping("/{seqId}" )
    public R removeById(@PathVariable String seqId) {
        return R.ok(xtApproveBusinessMaterialService.removeById(seqId));
    }

    /**
     * 通过固定条件查询业务材料信息表
     * @param materialName id
     * @return R
     */
    @SneakyThrows
    @ApiOperation(value = "通过id查询", notes = "通过id查询")
    @GetMapping("/WebDiskServerDemo/doc" )
    public void WebDiskServerDemo(@RequestParam("MaterialName" ) String materialName,
                     @RequestParam("ProjectNo") String projectNo,
                     @RequestParam("MaterialCode") String materialCode,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        QueryWrapper wrapper  = new QueryWrapper();
        wrapper.eq("SBLSH_SHORT",projectNo);
        wrapper.eq("STUFF_SEQ",materialCode);
        XtApproveBusinessMaterial businessMaterial =  xtApproveBusinessMaterialService.getOne(wrapper);
        if (businessMaterial!=null){
           String url  =  businessMaterial.getAttachBody();
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            GetMethod getMethod = new GetMethod(url);
            try {
                client.executeMethod(getMethod);
            } catch (HttpException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            InputStream is = null;
            try {
                is = getMethod.getResponseBodyAsStream();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                arrayOutputStream.write(buffer, 0, length);
            }

            // 将 ByteArrayOutputStream 的数据转换为 byte[] 数组
            byte[] bytes = arrayOutputStream.toByteArray();

//            if (materialName.contains("png")){
//                response.setContentType("image/jpeg");
//            }else{
//                response.setContentType("application/octet-stream");
//            }

            response.setHeader("Content-Disposition", "attachment; filename=\"" +java.net.URLEncoder.encode(materialName, "UTF-8")  + "\"");
            // 获取 HttpServletResponse 的 OutputStream
            OutputStream outputStream = response.getOutputStream();

// 创建一个 BufferedOutputStream 对象，用于缓存 OutputStream
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

// 将 byte 数据写入 BufferedOutputStream
            bufferedOutputStream.write(bytes);

// 刷新 BufferedOutputStream，将缓冲区的数据写入 OutputStream
            bufferedOutputStream.flush();

// 关闭 BufferedOutputStream 和 OutputStream
            bufferedOutputStream.close();
            outputStream.close();

        }

    }

}
