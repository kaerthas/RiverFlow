package com.inspur.workinfo.jobhandlernew;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.XtApproveBusinessAccept;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.RedisCache;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@JobHandler(value = "xtSendAcceptServerNewJobHandler")
@Component
@Slf4j
public class XtSendAcceptServerNewJobHandler extends IJobHandler {
    private Logger logger  = LoggerFactory.getLogger(XtSendAcceptServerNewJobHandler.class);

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
    private AtgBizAffairRecevieService  affairRecevieService;
    @Autowired
    PropertyConfig propertyConfig;
    /**
     * 按照标准时间来算，每隔 30min 执行一次
     * 任务为办件查询和办件材料查询相关
     *
     */
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        boolean isflag = false;
        XxlJobLogger.log("【推送消息xtSendAcceptServerNewJobHandler】开始执行：{}", DateUtil.formatDateTime(new Date()));
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_send_accept =  redisCache.getCacheObject( CommonConstants.XT_BUSINESS_SEND_ACCEPT_NEW_REDIS);
        if (StrUtil.isBlank(xt_business_send_accept)) {
            //如果是空的先将数据插入
            redisCache.setCacheObject(CommonConstants.XT_BUSINESS_SEND_ACCEPT_NEW_REDIS,uuid);
            try{
                Page page = new Page();
                IPage<XtApproveBusinessCourse> businessCourseOld = businessCourseService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessCourse>()
                                .eq("ACTIVE","1")
                                .eq("CURRENT_NODE_CODE",CommonConstants.XT_BUSINESS_SEND_ACCEPT));
                for (int i = 0; i <businessCourseOld.getRecords().size() ; i++) {
                    try{
                        String sblshShort  = businessCourseOld.getRecords().get(i).getSblshShort();
                        //1.查询当前数据是否已经交换到accept表中
                        XtApproveBusinessAccept businessAcceptBean = businessAcceptService.getOne(new QueryWrapper<XtApproveBusinessAccept>()
                                .eq("SBLSH_SHORT",sblshShort));
                        if (businessAcceptBean!=null){
                            //调用数浙相关接口完成受理信息的调用
                            JSONObject acceptData  =  affairRecevieService.sendBusinessAccept(businessAcceptBean);
                        }else{
                            throw new Exception("暂时未接收到受理信息！流水号为"+sblshShort);
                        }
                    }catch (Exception e){
                        logger.error("推送受理信息报错"+e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
                }
                isflag = true;
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_SEND_ACCEPT_NEW_REDIS);

            }
        }else{
            isflag = true;
        }
        XxlJobLogger.log("【推送消息xtSendAcceptServerNewJobHandler】结束执行：{}", DateUtil.formatDateTime(new Date()));
        if(isflag){
            return SUCCESS;
        }else{
            return FAIL;
        }
    }
}
