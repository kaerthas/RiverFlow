package com.inspur.workinfo.controller;

import com.alibaba.fastjson.JSONObject;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.util.SM2Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/yjsUtils")
@Api(value = "yjsUtils", tags = "一件事根据传参获取相关加密后的值")
public class YjsUtilsController {
//    private static String publicKey = "04A5BFFA48FACB5F48E928A68046183AABFFEBB904A53CCEF2C834C73A639FE1FB43FBF54F89E94B16C0B6BD7EEB6C6E91123166005C91CEDFA92E12B1AC0F70A5";
//    private static String privateKey =  "00B0A2E24F56B672E291D3DA16283952C26FA2EB196318723E1838C3073CB0F364";


    @ApiOperation(value = "一件事根据传参获取相关加密后的值", notes = "一件事根据传参获取相关加密后的值")
    @PostMapping("/getSM2ForYjs" )
    public R getSM2ForYjs(@RequestBody JSONObject jso){
        try{

            String  publicKey  = jso.getString("publicKey");

            JSONObject  params      = jso.getJSONObject("params");

           return R.ok().setData( SM2Utils.encrypt(publicKey, params.toJSONString()));
        }catch (Exception e){
            e.printStackTrace();
            return R.failed().setMsg("查询失败！！");
        }

    }

    @ApiOperation(value = "一件事根据传参获取相关加密后的值", notes = "一件事根据传参获取相关加密后的值")
    @PostMapping("/decryptedSM2ForYjs" )
    public R getSM2ForYjs(@RequestParam("jso") String  jso,@RequestParam("privateKey") String privateKey){
        try{
            return R.ok().setData( SM2Utils.decrypt(privateKey, jso));
        }catch (Exception e){
            e.printStackTrace();
            return R.failed().setMsg("查询失败！！");
        }

    }

}
