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
package com.inspur.workinfo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.entity.PreApasinfo;
import com.inspur.workinfo.mapper.PreApasinfoMapper;
import com.inspur.workinfo.service.ApiInputInfoService;
import com.inspur.workinfo.service.DisabilityService;
import com.inspur.workinfo.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
public class DisabilityServiceImpl extends ServiceImpl<PreApasinfoMapper, PreApasinfo> implements DisabilityService {

    @Autowired
    PropertyConfig propertyConfig;
    @Autowired
    ApiInputInfoService apiInputInfoService;

    public JSONObject getCities(String provinceid){
        JSONObject info = new JSONObject();
        try {
            String url = propertyConfig.getDisabilityAllowanceUrl() +"/v1/getCities";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("provinceid", provinceid);
            JSONObject json= getResult(params,url);
            Boolean success = json.getBoolean("success");
            if(true == success){
                info.put("state","200");
                info.put("cityList",json.getJSONArray("cityList"));
                info.put("message","查询成功");
            }else{
                info.put("state","300");
                info.put("message",json.getString("message"));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.put("state","300");
            info.put("message","查询出错");
        }
        return info;
    }



    public JSONObject getCounty(String cityid){
        JSONObject info = new JSONObject();
        try {
            String url = propertyConfig.getDisabilityAllowanceUrl() +"/v1/getCounty";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("cityid", cityid);
            JSONObject json= getResult(params,url);
            Boolean success = json.getBoolean("success");
            if(true == success){
                info.put("state","200");
                info.put("countyList",json.getJSONArray("countyList"));
                info.put("message","查询成功");
            }else{
                info.put("state","300");
                info.put("message",json.getString("message"));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.put("state","300");
            info.put("message","查询出错");
        }
        return info;
    }


    public JSONObject getTown(String countyid){
        JSONObject info = new JSONObject();
        try {
            String url = propertyConfig.getDisabilityAllowanceUrl() +"/v1/getTown";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("countyid", countyid);
            JSONObject json= getResult(params,url);
            Boolean success = json.getBoolean("success");
            if(true == success){
                info.put("state","200");
                info.put("townList",json.getJSONArray("townList"));
                info.put("message","查询成功");
            }else{
                info.put("state","300");
                info.put("message",json.getString("message"));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.put("state","300");
            info.put("message","查询出错");
        }
        return info;
    }


    public JSONObject applyCheck(String name,String idcard,String mobile,String provinceid,String cityid, String countyid,String townid){
        JSONObject info = new JSONObject();
        try {
            String url = propertyConfig.getDisabilityAllowanceUrl() +"/v1/applyCheck";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", name);
            params.put("idcard", idcard);
            params.put("mobile", mobile);
            params.put("provinceid", provinceid);
            params.put("cityid", cityid);
            params.put("countyid", countyid);
            params.put("townid", townid);

            JSONObject json= getResult(params,url);
            Boolean success = json.getBoolean("success");
            if(true == success){
                info.put("state","200");
                info.put("message","校验成功");
            }else{
                info.put("state","300");
                info.put("message",json.getString("message"));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.put("state","300");
            info.put("message","校验出错");
        }
        return info;
    }


    public JSONObject upLoadImg(String base64,String type,String idCard){
        JSONObject info = new JSONObject();
        try {
            String url = propertyConfig.getDisabilityAllowanceUrl() +"/v1/upLoadImg";
            String downloadUrl = propertyConfig.getDownloadUrl();
            String webDiskAppCode = propertyConfig.getWebDiskAppCode();
            String webDiskDecryptKey = propertyConfig.getWebDiskDecryptKey();

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("base64", base64);
            params.put("idCard", idCard);
            params.put("type", type);

            JSONObject json= getResult(params,url);
            Boolean success = json.getBoolean("success");
            if(true == success){
                info.put("state","200");
                info.put("fileName",json.getString("fileName"));
                info.put("imgId",json.getString("imgId"));
                info.put("fileUrl",json.getString("fileUrl"));
                info.put("message","上传成功");
            }else{
                info.put("state","300");
                info.put("message",json.getString("message"));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.put("state","300");
            info.put("message","上传出错");
        }
        return info;
    }


    public JSONObject deleteImg(String imgType,String idCard,String imgId){
        JSONObject info = new JSONObject();
        try {
            String url = propertyConfig.getDisabilityAllowanceUrl() +"/v1/deleteImg";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("imgType", imgType);
            params.put("idCard", idCard);
            params.put("imgId", imgId);

            JSONObject json= getResult(params,url);
            Boolean success = json.getBoolean("success");
            if(true == success){
                info.put("state","200");
                info.put("message","删除成功");
            }else{
                info.put("state","300");
                info.put("message",json.getString("message"));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.put("state","300");
            info.put("message","删除出错");
        }
        return info;
    }


    public JSONObject submitApply(String name,String idcard,String mobile,String provinceid,String cityid, String countyid,String townid,String clientType){
        JSONObject info = new JSONObject();
        try {
            String url = propertyConfig.getDisabilityAllowanceUrl() +"/v1/submitApply";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", name);
            params.put("idcard", idcard);
            params.put("mobile", mobile);
            params.put("provinceid", provinceid);
            params.put("cityid", cityid);
            params.put("countyid", countyid);
            params.put("townid", townid);
            params.put("clientType", clientType);//pc/app

            JSONObject json= getResult(params,url);
            Boolean success = json.getBoolean("success");
            if(true == success){
                info.put("state","200");
                info.put("onlineApplyId",json.getString("onlineApplyId"));
                info.put("message","申请提交成功");
            }else{
                info.put("state","300");
                info.put("message",json.getString("message"));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.put("state","300");
            info.put("message","申请提交出错");
        }
        return info;
    }

    public JSONObject cancelSubmit(String idcard,String onlineApplyId){
        JSONObject info = new JSONObject();
        try {

            String url = propertyConfig.getDisabilityAllowanceUrl() +"/v1/cancelSubmit";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("idcard", idcard);
            params.put("onlineApplyId", onlineApplyId);

            JSONObject json= getResult(params,url);
            Boolean success = json.getBoolean("success");
            if(true == success){
                info.put("state","200");
                info.put("message","撤回成功");
            }else{
                info.put("state","300");
                info.put("message",json.getString("message"));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.put("state","300");
            info.put("message","撤回出错");
        }
        return info;
    }


    public JSONObject modifyApply(String mobile,String onlineApplyId,String cityid,String countyid,String townid){
        JSONObject info = new JSONObject();
        try {

            String url = propertyConfig.getDisabilityAllowanceUrl() +"/v1/modifyApply";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", onlineApplyId);
            params.put("mobile", mobile);
            params.put("cityid", cityid);
            params.put("countyid", countyid);
            params.put("townid", townid);

            JSONObject json= getResult(params,url);
            Boolean success = json.getBoolean("success");
            if(true == success){
                info.put("state","200");
                info.put("message","修改成功");
            }else{
                info.put("state","300");
                info.put("message",json.getString("message"));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.put("state","300");
            info.put("message","修改出错");
        }
        return info;
    }


    public JSONObject getApplyProcess(String userName,String idCard,String provinceid){
        JSONObject info = new JSONObject();
        try {

            String url = propertyConfig.getDisabilityAllowanceUrl() +"/v1/getApplyProcess";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("userName", userName);
            params.put("idCard", idCard);
            params.put("provinceid", provinceid);

            JSONObject json= getResult(params,url);
            Boolean success = json.getBoolean("success");
            if(true == success){
                info.put("state","200");
                info.put("message","查询成功");
                info.put("ProcessState",json.getString("state"));
            }else{
                info.put("state","300");
                info.put("message",json.getString("message"));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            info.put("state","300");
            info.put("message","查询出错");
        }
        return info;
    }






    public JSONObject getResult(Map<String, Object> params,String url) throws Exception{
        String appid = propertyConfig.getDisabilityAllowanceAppId();
        String appkey = propertyConfig.getDisabilityAllowanceAppKey();
        String nonce = UUID.randomUUID().toString().replaceAll("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = sign(appid, appkey, nonce, timestamp);

        params.put("appid", appid);
        params.put("appkey", appkey);
        params.put("nonce", nonce);
        params.put("timestamp", timestamp);
        params.put("signature", signature);

        HttpURLConnection conn = HttpUtil.initHttpConnection(url, "POST");
        conn.setRequestProperty("Content-Type", "application/json");
        HttpUtil.writeHttpContent(conn, JSONObject.toJSONString(params), "utf-8");
        String result = HttpUtil.getHttpContent(conn, "utf-8");
        JSONObject json= JSONObject.parseObject(result);
        return  json;
    }

    public  String sign(String appid, String appkey, String nonce, String timestamp) throws Exception {
        StringBuilder sb = new StringBuilder("appid=").append(appid).append("&appkey=")
                .append(appkey).append("&nonce=").append(nonce).append("&timestamp=").append(timestamp);
        return sign(sb.toString());
    }

    public String sign(String content) throws Exception {
        String ciphertext = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            // 对接后的字符串进行sha1 hash
            byte[] digest = md.digest(content.toString().getBytes());
            ciphertext = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(),e);
        }

        return ciphertext != null ? ciphertext.toLowerCase() : null;
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    public  String byteToStr(byte[] byteArray) throws Exception {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }

    public  String byteToHexStr(byte mByte) throws Exception {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];	// 取一个字节的高4位，然后获得其对应的十六进制字符
        tempArr[1] = Digit[mByte & 0X0F];	//  取一个字节的低4位，然后获得其对应的十六进制字符

        String s = new String(tempArr);
        return s;
    }

}
