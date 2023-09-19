package com.inspur.workinfo.task;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.entity.XtApproveBusinessinfo;
import com.inspur.workinfo.entity.XtApproveItemflowConfig;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.HttpClientUtils;
import com.inspur.workinfo.util.RedisCache;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;
/*********
 @Configuration
 @EnableScheduling
 @ConditionalOnProperty(name="task.getSignalDataEnable")
  * 后续修改为配置文件控制相关任务是否执行
 * *********/
@Slf4j
@Component
public class XtAcceptServerTask {
    private Logger logger  = LoggerFactory.getLogger(XtAcceptServerTask.class);

    @Autowired
    private  XtApproveBusinessinfoService businessInfoService;
    @Autowired
    private XtApproveBusinessXmlConfigService xmlConfigService;
    @Autowired
    private ApproveCallService approveCallService;
    @Autowired
    private ApproveCallResultService approveCallResultService;
    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;
    @Autowired
    private XtApproveBusinessCourseService courseService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    PropertyConfig propertyConfig;
    /**
     * 按照标准时间来算，每隔 30min 执行一次
     * 任务为办件查询和办件材料查询相关
     *
     */
    //@Scheduled(cron = "0 */1 * * * ? ")
    public void websocket() throws Exception {
        log.info("【推送消息XtAcceptServerTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_info =  redisCache.getCacheObject( CommonConstants.XT_BUSINESS_INFO_REDIS);
        if (StrUtil.isBlank(xt_business_info)) {
            //如果是空的先将数据插入
            redisCache.setCacheObject(CommonConstants.XT_BUSINESS_INFO_REDIS,uuid);
            try {
                Page page = new Page();
                IPage<XtApproveBusinessinfo> businessinfoList = businessInfoService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessinfo>()
                                .eq("IS_USED", "0"));
                if (businessinfoList!=null&&businessinfoList.getTotal()>0){
                    //流方式循环调用相关接口
                    businessinfoList.getRecords().stream().forEach(details ->{

                    try {
                        JSONObject callParams  = new JSONObject();
                        callParams.put("sblshShort",details.getSblshShort());
                        callParams.put("sxbm",details.getSxbm());
                        //创建接口调 用记录表
                        approveCallService.createCallBean(details.getSblshShort(),
                                propertyConfig.getDispatchUrl(),callParams.toJSONString(),"协同调度中心","POST","61002");


                        JSONObject busiApplyJson  =  this.businessInfoService.getBusinessApplyData(details.getSblshShort() , details.getSxbm());
                        String applyXmlStr  = "";
                        if(CommonConstants.API_SUCCESS.equals(busiApplyJson.getString("code"))){
                            applyXmlStr = busiApplyJson.getString("xmlStr");
                        }else{
                            throw new Exception("获取受理信息失败！  " +  busiApplyJson.getString("error"));
                        }
                        //分析数据办件信息字段，需要创建协同办件基本表XtApproveBusinessBase
                        //3.解析数据业务申办，预受理，受理信息
                        JSONObject applyJsonData = this.businessInfoService.analysisApplyData(details.getSxbm(),applyXmlStr);
                        if(!CommonConstants.API_SUCCESS.equals(applyJsonData.getString("code"))){
                            throw new Exception("解析业务申办，预受理，受理数据失败！  " +  applyJsonData.getString("error"));
                        }

                        //4.根据token和受理编码获取业务申请材料数据信息
                        JSONObject busiApplyMaterialJson = this.businessInfoService.getBusiApplyMaterial(details.getSblshShort(),details.getSxbm(),"0");
                        //5.解析业务申请材料数据
                        String materialXmlStr = "";
                        if(CommonConstants.API_SUCCESS.equals(busiApplyMaterialJson.getString("code"))){
                            materialXmlStr = busiApplyMaterialJson.getString("xmlStr");
                        }else{
                            throw new Exception("获取受理信息失败！  " +  busiApplyMaterialJson.getString("error"));
                        }
                        JSONObject materialJsonData = this.businessInfoService.analysisMaterial(materialXmlStr,details.getSblshShort());
                        if(!CommonConstants.API_SUCCESS.equals(materialJsonData.getString("code"))){
                            throw new Exception("解析材料数据失败！  " +  materialJsonData.getString("error"));
                        }
                        //6.处理流程，保存环节信息
                        //先查询环节信息 激活的ACTIVE
                        JSONObject courseJsonData  = this.courseService.analysisCourse(details.getSblshShort());
                        if(!CommonConstants.API_SUCCESS.equals(courseJsonData.getString("code"))){
                            throw new Exception("保存过程信息失败！"+  courseJsonData.getString("error"));
                        }

                        //7.最终处理is_used为1
                        details.setIsUsed("1");
                        businessInfoService.saveOrUpdate(details);



                        } catch (Exception e) {

                            //如果捕获到异常直接跳过，进入下次循环
                            return;

                        }
                    });

                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_INFO_REDIS);
            }
        }else{

            //空方法
        }
        log.info("【推送消息XtAcceptServerTask】执行结束：{}", DateUtil.formatDateTime(new Date()));
    }



}
