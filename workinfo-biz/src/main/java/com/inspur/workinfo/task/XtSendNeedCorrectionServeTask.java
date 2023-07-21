package com.inspur.workinfo.task;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.entity.XtApproveBusinessNcorrect;
import com.inspur.workinfo.service.XtApproveBusinessCourseService;
import com.inspur.workinfo.service.XtApproveBusinessNcorrectService;
import com.inspur.workinfo.util.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/********
 *发送补齐补正信息到协同调度系统（中残和两补没有相关业务，待使用）
 * **********/
@Component
@Slf4j
public class XtSendNeedCorrectionServeTask {

    private Logger logger = LoggerFactory.getLogger(XtSendNeedCorrectionServeTask.class);

    @Autowired
    private XtApproveBusinessNcorrectService businessNeedCorrectService;
    @Autowired
    private XtApproveBusinessCourseService businessCourseService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    PropertyConfig propertyConfig;

    /**
     * 按照标准时间来算，每隔 30min 执行一次
     * 任务为办件查询和办件材料查询相关
     */
//    @Scheduled(cron = "0 */1 * * * ? ")
    public void websocket() throws Exception {
        log.info("【推送消息XtSendNeedCorrectionServeTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_send_outcome = redisCache.getCacheObject(CommonConstants.XT_BUSINESS_SEND_NEED_CORRECTION_REDIS);
        if (StrUtil.isBlank(xt_business_send_outcome)) {
            //如果是空的先将数据插入
            redisCache.setCacheObject(CommonConstants.XT_BUSINESS_SEND_NEED_CORRECTION_REDIS, uuid);
            try {
                Page page = new Page();
                IPage<XtApproveBusinessCourse> businessCourseOld = businessCourseService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessCourse>()
                                .eq("ACTIVE", "1")
                                .eq("CURRENT_NODE_CODE", CommonConstants.XT_BUSINESS_SEND_NEED_CORRECTION));
                businessCourseOld.getRecords().stream().forEach(detail -> {
                    //1.查询当前数据是否已经交换到结果物表中
                    List<XtApproveBusinessNcorrect> businessNcorrectList = businessNeedCorrectService.list(new QueryWrapper<XtApproveBusinessNcorrect>()
                            .eq("SBLSH_SHORT", detail.getSblshShort()));
                    if (businessNcorrectList != null&&businessNcorrectList.size()>0) {
                        //2.调用协同61009接口推送结果物信息
                        businessNeedCorrectService.sendNeedCorrectionData(businessNcorrectList,detail.getSblshShort());
                    } else {
                        logger.error("暂时未接收到结果物信息！流水号为{}", detail.getSblshShort());
                        return;
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_SEND_NEED_CORRECTION_REDIS);

            }
        }
        log.info("【推送消息XtSendNeedCorrectionServeTask】结束执行：{}", DateUtil.formatDateTime(new Date()));

    }

}
