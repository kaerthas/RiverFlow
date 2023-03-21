package com.inspur.workinfo.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdcardUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.inspur.workinfo.entity.RegionalInstitution;

import com.inspur.workinfo.service.RegionalInstitutionService;

import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/toolinfo")
@Api(value = "toolinfo", tags = "工具信息管理")
public class IDToolController {

    private final RegionalInstitutionService regionalInstitutionService;


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
                       case "1" :
                           list = regionalInstitutionService.list(new QueryWrapper<RegionalInstitution>().eq("RID",rid.substring(0,6)));

                           break; //
                       case "2" :
                           list =  regionalInstitutionService.list(new QueryWrapper<RegionalInstitution>().eq("RID",rid.substring(0,9)));

                           break; //
                       case "3" :
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

}
