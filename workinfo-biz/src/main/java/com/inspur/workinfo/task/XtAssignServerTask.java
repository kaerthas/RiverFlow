package com.inspur.workinfo.task;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.service.impl.ApiInputInfoServiceImpl;
import com.inspur.workinfo.util.DateUtils;
import com.inspur.workinfo.util.R;
import com.inspur.workinfo.util.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;

import static com.mysql.cj.conf.PropertyKey.logger;

@Slf4j
@Component
public class XtAssignServerTask {

    private Logger logger  = LoggerFactory.getLogger(XtAssignServerTask.class);

    @Autowired
    private XtApproveBusinessCourseService businessCourseService;
    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;
    @Autowired
    private ApiInputInfoService apiInputInfoService;
    @Autowired
    private ApiOutputInfoService apiOutputInfoService;
    @Autowired
    private XtApproveBusinessBaseService businessBaseService;
    @Autowired
    private XtApproveItemConfigService itemConfigService;
    @Autowired
    private XtApproveBusinessXmlConfigService businessXmlConfigService;
    @Autowired
    private ApiServiceCatalogService apiServiceCatalogService;
    @Autowired
    private ApproveCallService approveCallService;
    @Autowired
    private ApproveCallResultService approveCallResultService;

    @Autowired
    private RedisCache redisCache;

    //定时分发流程
    //@Scheduled(cron = "0 */1 * * * ? ")
    public void websocket() throws Exception {
        log.info("【推送消息XtAssignServerTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_info =  redisCache.getCacheObject( CommonConstants.XT_BUSINESS_ASSIGN_REDIS);
        if (StrUtil.isBlank(xt_business_info)) {
                //如果是空的先将数据插入
                redisCache.setCacheObject(CommonConstants.XT_BUSINESS_ASSIGN_REDIS,uuid);

                try{
                //获取环节Code为 Assign的流程数据 每次处理10条数据
                Page page = new Page();
                IPage<XtApproveBusinessCourse> businessCourse = businessCourseService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessCourse>()
                                .eq("ACTIVE","1")
                                .eq("CURRENT_NODE_CODE",CommonConstants.XT_BUSINESS_ASSIGN));
                if(businessCourse!=null){
                    for (int i = 0; i <businessCourse.getRecords().size() ; i++) {
                        try {
                            String sblshshort = businessCourse.getRecords().get(i).getSblshShort();
                            String currentNodeId = businessCourse.getRecords().get(i).getCurrentNodeId();
                            //1.根据当前流程获取节点配置的信息
                            XtApproveItemflowConfig xtApproveItemflowConfig = itemflowConfigService.getById(currentNodeId);
                            if (xtApproveItemflowConfig != null) {
                                //如果节点中配置了分发接口直接调用内部流程接口
                                if ("0".equals(xtApproveItemflowConfig.getExchangeType())) {
                                    if (StrUtil.isNotBlank(xtApproveItemflowConfig.getApiId())) {
                                        //获取api配置并调用，分发过程可以绑定多个接口
                                        //获取入参信息
                                        //创建接口记录相关代码
                                        ApiServiceCatalog apiServiceCatalog = apiServiceCatalogService.getById(xtApproveItemflowConfig.getApiId());
                                        ApproveCall callBean  =  new ApproveCall();
                                        String callId  = UUID.randomUUID().toString().replace("-","");
                                        callBean.setBsnum(sblshshort);//获取token接口不存在
                                        callBean.setCalledSystemAddr(apiServiceCatalog.getUrl());
                                        callBean.setCalledSystemCode("");
                                        callBean.setCallId(callId);
                                        callBean.setCalledSystemName(apiServiceCatalog.getName());
                                        callBean.setCallTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss",new Date()));


                                        Map<String, Object > map   = itemflowConfigService.getImportantXtMessage(xtApproveItemflowConfig,sblshshort);
                                        if (map!=null){
                                            callBean.setParameterValue(new Gson().toJson(map));
                                            //TODO 接口调用 为完成
                                            R result = apiInputInfoService.getServiceByMap(xtApproveItemflowConfig.getApiId(),map);
                                            //result判断固定参数为 code msg data
                                            //TODO 后期改为可以配合out表使用的参数
                                           JSONObject res   = (JSONObject) result.getData();
                                           //创建接口调用记录 后期用aop处理多余代码
                                            ApproveCallResult callResultBean  =  new ApproveCallResult();
                                           //判断res中封装值
                                            if(CommonConstants.API_SUCCESS.equals(res.getString("code"))){

                                                //并进入下一个流程
                                                this.businessCourseService.analysisCourse(sblshshort);

                                                //中间状态既不修改也不做处理，下次调用依旧处理
                                            }else{
                                                //TODO 通知查看是否对接短信或者如何
                                            }

                                            //接口调用记录处理
                                            callResultBean.setResultValue(res.toJSONString());
                                            callResultBean.setCalledSystemName(callBean.getCalledSystemName());
                                            callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
                                            callResultBean.setCallState(res.getString("code"));
                                            callResultBean.setCallTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss",new Date()));
                                            callResultBean.setCallId(callId);
                                            callBean.setCallState(res.getString("code"));

                                            callResultBean.setSeqId(UUID.randomUUID().toString().replace("-",""));
                                            approveCallService.saveOrUpdate(callBean);
                                            approveCallResultService.saveOrUpdate(callResultBean);

                                        }else{
                                            logger.error("办件信息不完整！事项编码为"+xtApproveItemflowConfig.getSxbm());
                                        }
                                    }else{
                                        logger.error("xml模板配置存在问题，请联系管理员处理！事项编码为"+xtApproveItemflowConfig.getSxbm());
                                    }
                                }else {
                                    //TODO 如果配置了数据交换就去交换 目前尚没有需求
                                    if (StrUtil.isNotBlank(xtApproveItemflowConfig.getTableId())) {


                                    }

                                }
                            }else{
                                logger.error("itemflow流程模板配置存在问题，请联系管理员处理！事项编码为"+xtApproveItemflowConfig.getSxbm());
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }else{
                    //空方法
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_ASSIGN_REDIS);

            }
       }
        log.info("【推送消息XtAssignServerTask】结束执行：{}", DateUtil.formatDateTime(new Date()));


    }



}
