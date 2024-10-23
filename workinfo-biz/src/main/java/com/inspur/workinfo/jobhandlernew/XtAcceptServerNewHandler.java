package com.inspur.workinfo.jobhandlernew;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.XtApproveBusinessinfo;
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

@JobHandler(value = "xtAcceptServerNewHandler")
@Component
@Slf4j
public class XtAcceptServerNewHandler extends IJobHandler {

    private Logger logger  = LoggerFactory.getLogger(XtAcceptServerNewHandler.class);

    @Autowired
    private XtApproveBusinessinfoService businessInfoService;
    @Autowired
    private AtgBizAffairRecevieService affairRecevieService;
    @Autowired
    private XtApproveBusinessCourseService courseService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    PropertyConfig propertyConfig;
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        // XxlJobLogger.log("Job 001 start timemark:"+new Date());
        boolean isok = false;
        XxlJobLogger.log("【推送消息XtAcceptServerNewHandler】开始执行：{}", DateUtil.formatDateTime(new Date()));
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_info = redisCache.getCacheObject(CommonConstants.XT_BUSINESS_INFO_NEW_REDIS);
        if (StrUtil.isBlank(xt_business_info)) {
            //如果是空的先将数据插入
            redisCache.setCacheObject(CommonConstants.XT_BUSINESS_INFO_NEW_REDIS, uuid);
            try {
                Page page = new Page(1,400);
                IPage<XtApproveBusinessinfo> businessinfoList = businessInfoService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessinfo>()
                                .eq("IS_USED", "0"));
                if (businessinfoList != null && businessinfoList.getTotal() > 0) {
                    //流方式循环调用相关接口
                    for (int i = 0; i <businessinfoList.getRecords().size() ; i++) {

                        try {
                            XtApproveBusinessinfo businessinfo =  businessinfoList.getRecords().get(i);
                            String sblshShort  = businessinfoList.getRecords().get(i).getSblshShort();
                            String sxbm        = businessinfoList.getRecords().get(i).getSxbm();
                            String businessInfo = businessinfoList.getRecords().get(i).getBusinessInfo();

                            //分析数据办件信息字段，需要创建协同办件基本表XtApproveBusinessBase
                            //1.解析数据业务businessInfo中信息存到相关库表结构中
                            JSONObject applyJsonData = affairRecevieService.analysisApplyData(sxbm, businessInfo);
                            if (!CommonConstants.API_SUCCESS.equals(applyJsonData.getString("code"))) {
                                throw new Exception("解析业务申办，预受理，受理数据失败！  " + applyJsonData.getString("error"));
                            }

                            //2.处理流程，保存环节信息
                            //先查询环节信息 激活的ACTIVE
                            JSONObject courseJsonData = this.courseService.analysisCourse(sblshShort);
                            if (!CommonConstants.API_SUCCESS.equals(courseJsonData.getString("code"))) {
                                throw new Exception("保存过程信息失败！" + courseJsonData.getString("error"));
                            }

                            //3.最终处理is_used为1
                            businessinfo.setIsUsed("1");
                            businessInfoService.saveOrUpdate(businessinfo);


                        } catch (Exception e) {
                            e.printStackTrace();
                            //如果捕获到异常直接跳过，进入下次循环
                            continue;

                        }
                    }
                }
                isok = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_INFO_NEW_REDIS);
            }
        } else {

            //空方法
            isok = true;
        }
        XxlJobLogger.log("【推送消息XtAcceptServerNewHandler】执行结束：{}", DateUtil.formatDateTime(new Date()));

        if (isok) {
            return SUCCESS;
        } else {
            return FAIL;
        }
    }

}
