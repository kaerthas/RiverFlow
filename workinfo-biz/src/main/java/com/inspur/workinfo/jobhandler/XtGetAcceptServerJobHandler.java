package com.inspur.workinfo.jobhandler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.service.*;
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

import java.util.*;

/*********
 * 获取受理信息接口
 * **********/
@JobHandler(value = "xtGetAcceptServerJobHandler")
@Component
@Slf4j
public class XtGetAcceptServerJobHandler extends IJobHandler {

    private Logger logger = LoggerFactory.getLogger(XtGetAcceptServerJobHandler.class);

    @Autowired
    private XtApproveBusinessinfoService businessInfoService;

    @Autowired
    private ApproveCallService approveCallService;
    @Autowired
    private ApproveCallResultService approveCallResultService;
    @Autowired
    private ApiInputInfoService apiInputInfoService;
    @Autowired
    private XtApproveBusinessBaseService businessBaseService;

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
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        // XxlJobLogger.log("Job 001 start timemark:"+new Date());
        boolean isok = false;
        XxlJobLogger.log("【获取消息XtGetAcceptServerTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
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
                for (int i = 0; i <businessCourseOld.getRecords().size() ; i++) {
                    String sblshShort  =  businessCourseOld.getRecords().get(i).getSblshShort();
                    String currentNodeId = businessCourseOld.getRecords().get(i).getCurrentNodeId();
                    //1.查询当前流程绑定的相关接口，或者数据库表
                    XtApproveItemflowConfig itemflowConfig = itemflowConfigService.getById(currentNodeId);
                    //判断流程是否存在
                    if(itemflowConfig!=null){
                        //判断是否绑定接口
                        if("0".equals(itemflowConfig.getExchangeType())){
                            //接口代理基本完成现在开始处理
                            String apiId   = itemflowConfig.getApiId();
                            //通过申办流水号获取接口入参，主要传递基本信息和脚本信息
                            if(StrUtil.isNotBlank(apiId)){
                                //Map作为请求进度的默认入参
                                Map<String, Object> map  =  itemflowConfigService.getImportantXtMessage(itemflowConfig,sblshShort);
                                R result  =  apiInputInfoService.getServiceByMap(apiId,map);
                                if (result.getCode()==0){//0表示成功
                                    Object res =  result.getData();
                                    JSONObject resObj =   JSONObject.parseObject(res.toString());
                                    System.out.println("###"+resObj.toJSONString());
                                    businessAcceptService.saveFormApi(resObj,sblshShort);
                                }else{
                                    logger.error("接口查询失败，获取消息XtGetAcceptServerTask失败！");
                                }

                            }
                        }else{//判断是否绑定数据库表
                            //获取绑定关系表对应
                            //抽出通用方法
                            JSONObject dataExchangeObj = apiDataTableExchangeService.analysisDataExchange(itemflowConfig,sblshShort);
                            if(!CommonConstants.API_SUCCESS.equals(dataExchangeObj.getString("code"))){
                                logger.error("查询失败，获取消息XtGetAcceptServerTask失败！");
                            }else{
                                List<Map<String,Object>> list = (List<Map<String, Object>>) dataExchangeObj.get("data");
                                String tableId   = (String) dataExchangeObj.get("tableId");
                                //数据保存受理信息表
                                businessAcceptService.saveFromTable(list,businessCourseOld.getRecords().get(i),tableId);
                            }
                        }
                    }else{
                        logger.error("当前流程不存在请联系管理员处理");
                    }

                }
                isok=true;

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_GET_ACCEPT_REDIS);

            }
        }else{
            isok = true;
        }
        XxlJobLogger.log("【获取消息XtGetAcceptServerTask】结束执行：{}", DateUtil.formatDateTime(new Date()));

        if(isok){
            return SUCCESS;
        }else{
            return FAIL;
        }
    }

}
