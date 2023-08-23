package com.inspur.workinfo.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/simulate" )
@Api(value = "simulate", tags = "模拟接口类")
public class ApiSimulateController {


    /**
     *模拟获取token接口
     * @return
     */
    @ApiOperation(value = "模拟获取token接口", notes = "模拟获取token接口")
    @PostMapping("/getToken" )
    public JSONObject getToken(@RequestParam("sysCode")String sysCode ,@RequestParam("sysPwd") String sysPwd) {

        return JSONObject.parseObject("{ \n" +
                "\"res_id\" :\"e0b004592dcc44fca43e6df622335b76\",\n" +
                "\"res_state\" :1,\n" +
                "\"res_time\" :\"2018-01-23 10:58:10 342\",\n" +
                "\"res_total_timeLen\" :384, \n" +
                "\"res_process_timeLen\" :375, \n" +
                "\"res_url\" :\" \", \n" +
                "\"res_ip\" :\"10.1.1.1\", \n" +
                "\"res_port\" :\"8080\", \n" +
                "\"res_data\" :{ \n" +
                "\"token\" :\"7DE244D81A6879F5FC3FAC42E46D574A\" \n" +
                "}\n" +
                "}");


    }
    /********
     * 模拟代理中残申请接口
     * ***********/
    @ApiOperation(value = "模拟代理中残申请接口", notes = "模拟代理中残申请接口")
    @PostMapping("/approveZC" )
    public JSONObject approveZC(@RequestBody String approveXml ) {

        return JSONObject.parseObject("{ \n" +
                "\"res_id\" :\"e0b004592dcc44fca43e6df622335b76\",\n" +
                "\"res_state\" :1,\n" +
                "\"res_time\" :\"2018-01-23 10:58:10 342\",\n" +
                "\"res_total_timeLen\" :384, \n" +
                "\"res_process_timeLen\" :375, \n" +
                "\"res_url\" :\" \", \n" +
                "\"res_ip\" :\"10.1.1.1\", \n" +
                "\"res_port\" :\"8080\", \n" +
                "\"res_data\" :{ \n" +
                "\"token\" :\"7DE244D81A6879F5FC3FAC42E46D574A\" \n" +
                "}\n" +
                "}");


    }


}
