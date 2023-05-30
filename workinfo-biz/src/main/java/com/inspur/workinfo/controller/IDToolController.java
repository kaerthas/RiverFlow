package com.inspur.workinfo.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdcardUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.inspur.workinfo.entity.RegionalInstitution;

import com.inspur.workinfo.service.RegionalInstitutionService;

import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/toolinfo")
@Api(value = "toolinfo", tags = "工具信息管理")
public class IDToolController {

    private final RegionalInstitutionService regionalInstitutionService;
    private final RestTemplate restTemplate;
    private static final String appidcs = "200861_app_16703133694014546";
    private static final String requrl004cs = "http://61.185.238.218:8043/svcreg/sxbus/api/application/api/sxzwfw/YJSYCB004_cs";
    private static final String requrl005cs = "http://61.185.238.218:8043/svcreg/sxbus/api/application/api/sxzwfw/YJSYCB005_cs";
//    private static final String requrl004cs = "http://59.218.251.18:33521/sxbus/api/application/api/sxzwfw/YJSYCB004_cs";
//    private static final String requrl005cs = "http://59.218.251.18:33521/sxbus/api/application/api/sxzwfw/YJSYCB005_cs";

    private static final String appidzs = "200861_app_20201118153052";
    private static final String requrl004zs = "http://59.218.251.20:33507/sxbus/api/application/api/sxzwfw/YJSYCB004";
    private static final String requrl005zs = "http://59.218.251.20:33507/sxbus/api/application/api/sxzwfw/YJSYCB005";

    @ApiOperation(value = "通过区域编码,下拉状态查询信息", notes = "通过区域编码,下拉状态查询信息")
    @PostMapping("/getRegInsInfo" )
    public R getRegionalInstitutionInfo(@RequestParam(value = "rid") String rid, @RequestParam(value = "isDown")String isDown){
        try{
            if (StringUtils.isBlank(isDown)){
               if (rid.length()!=12){
                    List<RegionalInstitution> list = regionalInstitutionService
                            .list(new QueryWrapper<RegionalInstitution>().eq("PID", rid.trim()));
                    if (list != null&& list.size()>0){
                        return R.ok().setData(list);
                    }else {
                        return R.ok().setMsg("未查询到相关数据");
                    }
               }else {

                   List<RegionalInstitution> list = regionalInstitutionService
                           .list(new QueryWrapper<RegionalInstitution>().eq("RID", rid.trim()));
                   if (list != null&& list.size()>0){
                       return R.ok().setData(list);
                   }else {
                       return R.ok().setMsg("未查询到相关数据");
                   }
               }
            } else{
                List<RegionalInstitution> list = new ArrayList<>();
               //isDown 3表示最低级 2表示区级粮食监管局 1 表示市级粮食监管局
               if (rid.length()!=12){
                   return R.failed("参数错误，请重新配置");
               }else{
                   switch (isDown){
                       case "2" :
                           list = regionalInstitutionService.list(new QueryWrapper<RegionalInstitution>().eq("RID",rid.substring(0,6)));

                           break; //
                       case "3" :
                           list =  regionalInstitutionService.list(new QueryWrapper<RegionalInstitution>().eq("RID",rid.substring(0,9)));

                           break; //
                       case "4" :
                           list =  regionalInstitutionService.list(new QueryWrapper<RegionalInstitution>().eq("RID",rid));
                           break; //
                   }
                   return R.ok().setData(list);

               }



            }
        }catch (Exception e){
            e.printStackTrace();
            return R.failed().setMsg("查询失败！！");
        }

    }

    @ApiOperation(value = "通过身份证号码查询退休人员相关信息", notes = "通过身份证号码查询退休人员相关信息")
    @GetMapping("/getTX004And005")
    public R getTX004And005(@RequestParam("idCard") String idCard, @RequestParam("env") String env){
        switch (env){
            case "test":
                try {
                    return R.ok(this.getTXinfo(idCard, appidcs, requrl004cs, requrl005cs), "查询成功");
                }catch (Exception e){
                    return R.failed(e.getMessage());
                }
            case "prov":
                try{
                    return R.ok(this.getTXinfo(idCard, appidzs, requrl004zs, requrl005zs), "查询成功");
                }catch (Exception e){
                    return R.failed(e.getMessage());
                }
            default:
                return R.failed("请选择正确的环境");
        }
    }

    private JSONObject getTXinfo(String idCard, String appid, String url004, String url005) throws Exception{
        //设置请求header信息
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");
        headers.add("C-Tenancy-Id","610000000000");
        headers.add("C-App-Id", appid);
        //设置请求参数
        JSONObject reqBody = new JSONObject();
        JSONObject userParam = new JSONObject();
        JSONObject requestDtoParam = new JSONObject();
        JSONObject commonInfoParam = new JSONObject();
        userParam.put("aae011", "车维琴");
        userParam.put("aaz692", "00008880");
        userParam.put("aab034", "610000");
        userParam.put("aab360", "610000");
        userParam.put("aaf018", "610000");
        userParam.put("aaa431", "1");
        userParam.put("aaa027", "610000");

        requestDtoParam.put("aac002", idCard.trim());

        commonInfoParam.put("aaa028", "20");
        commonInfoParam.put("aaz010", "10300000660");
        commonInfoParam.put("signature", "SRC211");

        reqBody.put("user", userParam);
        reqBody.put("requestDto", requestDtoParam);
        reqBody.put("commonInfo", commonInfoParam);
        //封装请求头和内容
        HttpEntity<JSONObject> httpEntity400 = new HttpEntity<JSONObject>(reqBody, headers);
        //发送请求
        ResponseEntity<String> resp400Entity = restTemplate.postForEntity(url004, httpEntity400, String.class);
        JSONObject jsonObject400 = JSONObject.parseObject(resp400Entity.getBody());
        JSONArray listArray = jsonObject400.getJSONObject("data").getJSONObject("result").getJSONArray("list");
        String aac001 = listArray.getJSONObject(0).getJSONObject("ac01").get("aac001").toString();
        String aac002 = listArray.getJSONObject(0).getJSONObject("ac01").get("aac002").toString();
        String aac003 = listArray.getJSONObject(0).getJSONObject("ac01").get("aac003").toString();
        String aab998 = listArray.getJSONObject(0).getJSONObject("ab01").get("aab998").toString();

        requestDtoParam.clear();
        requestDtoParam.put("aac001", aac001);
        requestDtoParam.put("aac002", aac002);
        requestDtoParam.put("aac003", aac003);
        requestDtoParam.put("aab998", aab998);
        reqBody.put("requestDto", requestDtoParam);
        //封装请求头和内容
        HttpEntity<JSONObject> httpEntity500 = new HttpEntity<JSONObject>(reqBody, headers);
        //发送请求
        ResponseEntity<String> resp500Entity = restTemplate.postForEntity(url005, httpEntity500, String.class);
        JSONObject jsonObject500 = JSONObject.parseObject(resp500Entity.getBody());
        return jsonObject500;
    }

}
