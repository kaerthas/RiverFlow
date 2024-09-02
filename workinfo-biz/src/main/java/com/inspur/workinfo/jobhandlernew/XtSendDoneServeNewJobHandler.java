package com.inspur.workinfo.jobhandlernew;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.entity.XtApproveBusinessDone;
import com.inspur.workinfo.service.AtgBizAffairRecevieService;
import com.inspur.workinfo.service.XtApproveBusinessCourseService;
import com.inspur.workinfo.service.XtApproveBusinessDoneService;
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

/*********
 * 根据过程信息表推送办结信息到协同调度
 * SendDone 标准字段
 * *******/
@JobHandler(value = "xtSendDoneServeNewJobHandler")
@Component
@Slf4j
public class XtSendDoneServeNewJobHandler  extends IJobHandler {
    private final Logger logger  = LoggerFactory.getLogger(XtSendDoneServeNewJobHandler.class);
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private XtApproveBusinessCourseService businessCourseService;
    @Autowired
    private AtgBizAffairRecevieService affairRecevieService;
    @Autowired
    private XtApproveBusinessDoneService businessDoneService;


    @Override
    public ReturnT<String> execute(String s) throws Exception {
        boolean isflag = false;
        XxlJobLogger.log("【推送消息XtSendDoneServeNewTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_send_accept =  redisCache.getCacheObject( CommonConstants.XT_BUSINESS_SEND_DONE_NEW_REDIS);
        if (StrUtil.isBlank(xt_business_send_accept)) {
            //如果是空的先将数据插入
            redisCache.setCacheObject(CommonConstants.XT_BUSINESS_SEND_DONE_NEW_REDIS,uuid);
            try{
                Page page = new Page();
                IPage<XtApproveBusinessCourse> businessCourseOld = businessCourseService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessCourse>()
                                .eq("ACTIVE","1")
                                .eq("CURRENT_NODE_CODE",CommonConstants.XT_BUSINESS_SEND_DONE));
                businessCourseOld.getRecords().stream().forEach(detail->{
                    //1.查询当前数据是否已经交换到accept表中
                    XtApproveBusinessDone businessAcceptBean = businessDoneService.getOne(new QueryWrapper<XtApproveBusinessDone>()
                            .eq("SBLSH_SHORT",detail.getSblshShort()));
                    if (businessAcceptBean!=null){
                        //2.调用协同61004接口推送受理信息
                        JSONObject acceptData  =  affairRecevieService.sendBusinessFinish(businessAcceptBean);
                    }else{
                        logger.error("暂时未接收到办结信息！流水号为{}",detail.getSblshShort());
                        return ;
                    }
                });
                isflag=true;
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_SEND_DONE_NEW_REDIS);

            }
        }else{
            isflag=true;
        }
        XxlJobLogger.log("【推送消息XtSendDoneServeNewTask】结束执行：{}", DateUtil.formatDateTime(new Date()));

        if(isflag){
            return SUCCESS;
        }else{
            return FAIL;
        }
    }
}
