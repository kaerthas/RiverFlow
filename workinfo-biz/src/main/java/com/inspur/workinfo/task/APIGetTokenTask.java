package com.inspur.workinfo.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.ApiServiceCatalog;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.entity.XtApproveItemflowConfig;
import com.inspur.workinfo.service.ApiServiceCatalogService;
import com.inspur.workinfo.util.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*******
 * 获取某些api前置token
 * ******/
@Component
@Slf4j
public class APIGetTokenTask {

    @Autowired
    private ApiServiceCatalogService serviceCatalogService;
    @Autowired
    private RedisCache redisCache;
    /**
     * 获取某些api前置token 每天凌晨执行一次
     */
    @Scheduled(cron = "0 0 1 * * ? ")
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
                    //TODO 补全调用



                    //存放redis
                }





            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }

        log.info("【获取消息APIGetTokenTask】结束执行：{}", DateUtil.formatDateTime(new Date()));


    }



}
