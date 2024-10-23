package com.inspur.workinfo.jobhandler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.entity.XtApproveItemflowConfig;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.task.XtGetSendDoneServeTask;
import com.inspur.workinfo.util.R;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*********
 * 获取办结信息（拉方式），通过接口获取或者库表交换 ，字段配置为实体字段名
 * **********/
@JobHandler(value = "xtGetSendDoneServeJobHandler")
@Component
@Slf4j
public class XtGetSendDoneServeJobHandler extends IJobHandler {

    private Logger logger = LoggerFactory.getLogger(XtGetSendDoneServeTask.class);

    @Autowired
    private XtApproveBusinessinfoService businessInfoService;

    @Autowired
    private ApproveCallService approveCallService;
    @Autowired
    private ApproveCallResultService approveCallResultService;
    @Autowired
    private ApiInputInfoService apiInputInfoService;
    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;

    @Autowired
    private XtApproveBusinessCourseService businessCourseService;

    @Autowired
    private ApiDataTableExchangeService apiDataTableExchangeService;
    @Autowired
    private XtApproveBusinessDoneService businessDoneService;

    @Autowired
    private RedisCache redisCache;
    @Autowired
    PropertyConfig propertyConfig;

    /**
     * 按照标准时间来算，每隔 30min 执行一次
     * 任务为办件查询和办件材料查询相关
     */

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("【获取消息XtGetSendDoneServeTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
        boolean isok = false;
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_get_done = redisCache.getCacheObject(CommonConstants.XT_BUSINESS_GET_DONE_REDIS);
        if (StrUtil.isBlank(xt_business_get_done)) {
            //如果是空的先将数据插入
            redisCache.setCacheObject(CommonConstants.XT_BUSINESS_GET_DONE_REDIS,uuid);
            try{
                Page page = new Page(1,400);
                IPage<XtApproveBusinessCourse> businessCourseOld = businessCourseService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessCourse>()
                                .eq("ACTIVE","1")
                                .eq("CURRENT_NODE_CODE",CommonConstants.XT_BUSINESS_GET_DONE));
                for(int i = 0;i<businessCourseOld.getRecords().size(); i++) {
                    XtApproveBusinessCourse detail = businessCourseOld.getRecords().get(i);
                    try {
//                businessCourseOld.getRecords().stream().forEach(detail->{
                        //1.查询当前流程绑定的相关接口，或者数据库表
                        XtApproveItemflowConfig itemflowConfig = itemflowConfigService.getById(detail.getCurrentNodeId());
                        //判断流程是否存在
                        if (itemflowConfig != null) {
                            //判断是否绑定接口
                            if ("0".equals(itemflowConfig.getExchangeType())) {

                                //Map作为请求进度的默认入参

                                try {
                                    Map<String, Object> map = itemflowConfigService.getImportantXtMessage(itemflowConfig, detail.getSblshShort());

                                    R result = apiInputInfoService.getServiceByMap(itemflowConfig.getApiId(), map, detail.getSblshShort());
                                    if (result.getCode() == 0) {//0表示成功
                                        Object res = result.getData();
                                        JSONObject resObj = JSONObject.parseObject(res.toString());
                                        businessDoneService.saveFormApi(resObj, detail.getSblshShort());
                                    } else {
                                        logger.error("接口查询失败，获取消息XtGetAcceptServerTask失败！");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            } else {//判断是否绑定数据库表
                                //获取绑定关系表对应
                                //抽出通用方法
                                JSONObject dataExchangeObj = apiDataTableExchangeService.analysisDataExchange(itemflowConfig, detail.getSblshShort());
                                if (!CommonConstants.API_SUCCESS.equals(dataExchangeObj.getString("code"))) {
                                    logger.error("查询失败，获取消息XtGetSendDoneServeTask失败！");
                                } else {
                                    List<Map<String, Object>> list = (List<Map<String, Object>>) dataExchangeObj.get("data");
                                    String tableId = (String) dataExchangeObj.get("tableId");
                                    //数据保存结果信息表 下一环节为发送结果物
                                    businessDoneService.saveFromTable(list, detail, tableId);
                                }
                            }
                        } else {
                            logger.error("当前流程不存在请联系管理员处理");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        logger.error("流水号："+detail.getSblshShort());
                        logger.error(ex.getMessage(),ex);
                        continue;
                    }
//                });
                }
                isok =true;
            }catch (Exception e){
                e.printStackTrace();

            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_GET_DONE_REDIS);

            }
        }else{
            isok =true;

        }
        XxlJobLogger.log("【获取消息XtGetSendDoneServeTask】结束执行：{}", DateUtil.formatDateTime(new Date()));

        if(isok){
            return SUCCESS;
        }else{
            return FAIL;
        }
    }
}
