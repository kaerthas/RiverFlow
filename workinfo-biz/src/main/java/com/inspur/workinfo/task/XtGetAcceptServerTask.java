package com.inspur.workinfo.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.ApiDataTableExchange;
import com.inspur.workinfo.entity.XtApproveBusinessAccept;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.entity.XtApproveItemflowConfig;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.RedisCache;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*********
 * 获取受理信息接口
 * **********/
@Component
@Slf4j
public class XtGetAcceptServerTask {

    private Logger logger = LoggerFactory.getLogger(XtAcceptServerTask.class);

    @Autowired
    private XtApproveBusinessinfoService businessInfoService;

    @Autowired
    private ApproveCallService approveCallService;
    @Autowired
    private ApproveCallResultService approveCallResultService;
    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;

    @Autowired
    private XtApproveBusinessCourseService businessCourseService;

    @Autowired
    private ApiDataTableExchangeService apiDataTableExchangeService;
    @Autowired
    private XtApproveBusinessAcceptService businessAcceptService;

    @Autowired
    private RedisCache redisCache;
    @Autowired
    PropertyConfig propertyConfig;

    /**
     * 按照标准时间来算，每隔 30min 执行一次
     * 任务为办件查询和办件材料查询相关
     */
   @Scheduled(cron = "0 */1 * * * ? ")
    public void websocket() throws Exception {
        log.info("【获取消息XtGetAcceptServerTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_get_accept = redisCache.getCacheObject(CommonConstants.XT_BUSINESS_GET_ACCEPT_REDIS);
        if (StrUtil.isBlank(xt_business_get_accept)) {
            //如果是空的先将数据插入
            redisCache.setCacheObject(CommonConstants.XT_BUSINESS_GET_ACCEPT_REDIS,uuid);
            try{
                Page page = new Page();
                IPage<XtApproveBusinessCourse> businessCourseOld = businessCourseService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessCourse>()
                                .eq("ACTIVE","1")
                                .eq("CURRENT_NODE_CODE",CommonConstants.XT_BUSINESS_GET_ACCEPT));
                businessCourseOld.getRecords().stream().forEach(detail->{
                    //1.查询当前流程绑定的相关接口，或者数据库表
                    XtApproveItemflowConfig itemflowConfig = itemflowConfigService.getById(detail.getCurrentNodeId());
                    //判断流程是否存在
                    if(itemflowConfig!=null){
                        //判断是否绑定接口
                        if("0".equals(itemflowConfig.getExchangeType())){
                            //TODO 等待接口代理完成



                        }else{//判断是否绑定数据库表
                          //获取绑定关系表对应
                          //抽出通用方法
                          JSONObject dataExchangeObj = apiDataTableExchangeService.analysisDataExchange(itemflowConfig,detail.getSblshShort());
                          if(!CommonConstants.API_SUCCESS.equals(dataExchangeObj.getString("code"))){
                              logger.error("查询失败，获取消息XtGetAcceptServerTask失败！");
                          }else{
                              List<Map<String,Object>> list = (List<Map<String, Object>>) dataExchangeObj.get("data");
                              String tableId   = (String) dataExchangeObj.get("tableId");
                              //数据保存受理信息表
                              businessAcceptService.saveFromTable(list,detail,tableId);
                          }
                        }
                    }else{
                        logger.error("当前流程不存在请联系管理员处理");
                    }

                });

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_GET_ACCEPT_REDIS);

            }
        }
        log.info("【获取消息XtGetAcceptServerTask】结束执行：{}", DateUtil.formatDateTime(new Date()));


    }



}
