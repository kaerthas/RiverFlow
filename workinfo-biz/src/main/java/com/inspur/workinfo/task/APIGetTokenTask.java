package com.inspur.workinfo.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.service.ApiInputInfoService;
import com.inspur.workinfo.service.ApiOutputInfoService;
import com.inspur.workinfo.service.ApiServiceCatalogService;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.util.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*******
 * 获取某些api前置token
 * ******/
@Component
@Slf4j
public class APIGetTokenTask {

    @Autowired
    private ApiServiceCatalogService serviceCatalogService;
    @Autowired
    private ApiInputInfoService inputInfoService;
    @Autowired
    private ApiOutputInfoService outputInfoService;
    @Autowired
    private RedisCache redisCache;
    /**
     * 获取某些api前置token 每天凌晨执行一次
     */
//    @Scheduled(cron = "0 0 1 * * ? ")
//    @Scheduled(cron = "0 */1 * * * ? ")
    public void websocket() throws Exception {
        log.info("【获取消息APIGetTokenTask】开始执行：{}", DateUtil.formatDateTime(new Date()));

        try{
            //获取api类型为token并且需要定时的

            List<ApiServiceCatalog> apiServiceCatalogs =  serviceCatalogService
                    .list(new QueryWrapper<ApiServiceCatalog>()
                            .eq("TYPE",CommonConstants.API_TOKEN)
                            .eq("IS_SCHEDULE","1"));
            //判断是否
            if (apiServiceCatalogs!=null&&apiServiceCatalogs.size()>0){

                for (ApiServiceCatalog apiServiceCatalog:apiServiceCatalogs
                     ) {
                    //从表里获取input参数
                   List<ApiInputInfo> apiInputInfos =  inputInfoService
                           .list(new QueryWrapper<ApiInputInfo>().eq("API_ID",apiServiceCatalog.getId()));
                   Map<String , Object> map   =   new HashMap<>();
                   apiInputInfos.stream().forEach(n->
                        map.put(n.getKey(),n.getValue())
                   );
                   //包括后台脚本处理
                   R jsonObject = inputInfoService.getServiceByMap(apiServiceCatalog.getId(),map);
                   //获取出参配置
                   ApiOutputInfo apiOutputInfo =  outputInfoService
                           .getOne(new QueryWrapper<ApiOutputInfo>().eq("API_ID",apiServiceCatalog.getId()));
                    if (apiOutputInfo != null) {
                        Object objectData =jsonObject.getData();
                        JSONObject res =   JSONObject.parseObject(objectData.toString());
                        //存放redis
                        //计算过期时间然后处理
                        redisCache.setCacheObject(CommonConstants.BACK_END_PROJECT+"_"+apiServiceCatalog.getId(), res.get(CommonConstants.API_TOKEN),Integer.valueOf(getRemainSecondsOneDay(new Date())+""), TimeUnit.SECONDS);
                   }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }

        log.info("【获取消息APIGetTokenTask】结束执行：{}", DateUtil.formatDateTime(new Date()));


    }

    //控制当天零点过期
    private long getRemainSecondsOneDay(Date currentDate) {
        //使用plusDays加传入的时间加1天，将时分秒设置成0
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        //使用ChronoUnit.SECONDS.between方法，传入两个LocalDateTime对象即可得到相差的秒数
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
        return  seconds;
    }

}
