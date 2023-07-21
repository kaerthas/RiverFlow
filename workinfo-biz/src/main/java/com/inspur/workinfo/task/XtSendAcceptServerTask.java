package com.inspur.workinfo.task;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.XtApproveBusinessAccept;
import com.inspur.workinfo.entity.XtApproveBusinessBase;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.entity.XtApproveItemflowConfig;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/*********
 * 根据过程信息表获取受理信息到协同调度
 * SendAccept 标准字段
 * *******/
@Slf4j
@Component
public class XtSendAcceptServerTask {

    private Logger logger  = LoggerFactory.getLogger(XtAcceptServerTask.class);

    @Autowired
    private XtApproveBusinessinfoService businessInfoService;
    @Autowired
    private XtApproveBusinessXmlConfigService xmlConfigService;
    @Autowired
    private ApproveCallService approveCallService;
    @Autowired
    private ApproveCallResultService approveCallResultService;
    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;
    @Autowired
    private XtApproveBusinessAcceptService businessAcceptService;
    @Autowired
    private XtApproveBusinessCourseService businessCourseService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    PropertyConfig propertyConfig;
    /**
     * 按照标准时间来算，每隔 30min 执行一次
     * 任务为办件查询和办件材料查询相关
     *
     */
//    @Scheduled(cron = "0 */1 * * * ? ")
    public void websocket() throws Exception {
        log.info("【推送消息XtSendAcceptServerTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_send_accept =  redisCache.getCacheObject( CommonConstants.XT_BUSINESS_SEND_ACCEPT_REDIS);
        if (StrUtil.isBlank(xt_business_send_accept)) {
            //如果是空的先将数据插入
            redisCache.setCacheObject(CommonConstants.XT_BUSINESS_SEND_ACCEPT_REDIS,uuid);
            try{
                Page page = new Page();
                IPage<XtApproveBusinessCourse> businessCourseOld = businessCourseService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessCourse>()
                                .eq("ACTIVE","1")
                                .eq("CURRENT_NODE_CODE",CommonConstants.XT_BUSINESS_SEND_ACCEPT));
                businessCourseOld.getRecords().stream().forEach(detail->{
                    //1.查询当前数据是否已经交换到accept表中
                    XtApproveBusinessAccept businessAcceptBean = businessAcceptService.getOne(new QueryWrapper<XtApproveBusinessAccept>()
                            .eq("SBLSH_SHORT",detail.getSblshShort()));
                    if (businessAcceptBean!=null){
                        //2.调用协同61004接口推送受理信息
                        JSONObject acceptData  =  businessAcceptService.sendBusinessAccept(businessAcceptBean);
                    }else{
                        logger.error("暂时未接收到受理信息！流水号为{}",detail.getSblshShort());
                        return ;
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_SEND_ACCEPT_REDIS);

            }
        }
        log.info("【推送消息XtAssignServerTask】结束执行：{}", DateUtil.formatDateTime(new Date()));


}








}
