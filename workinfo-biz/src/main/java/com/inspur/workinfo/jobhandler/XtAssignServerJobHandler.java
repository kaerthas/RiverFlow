package com.inspur.workinfo.jobhandler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.DateUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/****
 *分发代码可以复用
 * *****/
@JobHandler(value = "xtAssignServerJobHandler")
@Component
@Slf4j
public class XtAssignServerJobHandler extends IJobHandler {

    private Logger logger  = LoggerFactory.getLogger(XtAssignServerJobHandler.class);

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


    @Override
    public ReturnT<String> execute(String param) throws Exception {
        // XxlJobLogger.log("Job 001 start timemark:"+new Date());
        boolean isok = false;
        XxlJobLogger.log("【推送消息XtAssignServerTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
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
                        String sblshshort = businessCourse.getRecords().get(i).getSblshShort();
                        try {
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
                                        ApproveCallResult callResultBean = new ApproveCallResult();
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
                                            R result = apiInputInfoService.getServiceByMap(xtApproveItemflowConfig.getApiId(),map,sblshshort);
                                            //result判断固定参数为 code msg data
                                            //TODO 后期改为可以配合out表使用的参数
                                            if (CommonConstants.SUCCESS.equals(result.getCode())) {
                                                JSONObject res = (JSONObject) result.getData();
                                                //创建接口调用记录 后期用aop处理多余代码
                                                //调用out表处理回填参数
                                                List<ApiOutputInfo> apiOutputInfos =  apiOutputInfoService
                                                        .list(new QueryWrapper<ApiOutputInfo>().eq("API_ID",apiServiceCatalog.getId()));
                                                if (apiOutputInfos != null&&apiOutputInfos.size()>0) {
                                                    if (CommonConstants.API_SUCCESS.equals(res.getString("code"))) {
                                                        Map<String,Object> objectMap = new HashMap<>();
                                                        for (ApiOutputInfo outputInfo:apiOutputInfos){

                                                            logger.error("@@@@@@@@@@@@@@@@@@@@@@@"+res.getString(outputInfo.getKey()));
                                                            objectMap.put(outputInfo.getKey(), res.getString(outputInfo.getKey()));
                                                        }
                                                        //并进入下一个流程
                                                        this.businessCourseService.analysisCourseSuccess(sblshshort,objectMap);

                                                        //中间状态既不修改也不做处理，下次调用依旧处理
                                                    } else {
                                                        //保存到受理信息表，回推不予受理状态给一件事系统
                                                        //创建受理信息表
                                                        //查询基本表信息
                                                        this.businessCourseService.analysisCourseError(sblshshort,res);

                                                    }
                                                }else {
                                                    //判断res中封装值
                                                    //测试发现接口超时的情况发生如果超时办件已经上报给国家
                                                    // 应该来说要进入下一个流程并改变办件过程
                                                    if (CommonConstants.API_SUCCESS.equals(res.getString("code"))) {

                                                        //并进入下一个流程
                                                        this.businessCourseService.analysisCourse(sblshshort);

                                                        //中间状态既不修改也不做处理，下次调用依旧处理
                                                    } else {

                                                        //保存到受理信息表，回推不予受理状态给一件事系统
                                                        //创建受理信息表
                                                        //查询基本表信息
                                                        this.businessCourseService.analysisCourseError(sblshshort,res);
                                                    }
                                                }

                                                //接口调用记录处理
                                                callResultBean.setResultValue(res.toJSONString());
                                                callResultBean.setCalledSystemName(callBean.getCalledSystemName());
                                                callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
                                                callResultBean.setCallState(res.getString("code"));
                                                callResultBean.setCallTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss", new Date()));
                                                callResultBean.setCallId(callId);
                                                callBean.setCallState(res.getString("code"));

                                                callResultBean.setSeqId(UUID.randomUUID().toString().replace("-", ""));
                                            }else{
                                                //JSONObject res = (JSONObject) result.getData();
                                                //接口调用记录处理
                                                callResultBean.setResultValue(result.getMsg());
                                                callResultBean.setCalledSystemName(callBean.getCalledSystemName());
                                                callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
                                                callResultBean.setCallState(CommonConstants.API_FAIL);
                                                callResultBean.setCallTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss", new Date()));
                                                callResultBean.setCallId(callId);
                                                callBean.setCallState(CommonConstants.API_FAIL);

                                                callResultBean.setSeqId(UUID.randomUUID().toString().replace("-", ""));

                                            }
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
                            e.printStackTrace();
                            logger.error("流水号"+sblshshort);
                            logger.error(e.getMessage(),e);
                            continue;
                        }
                    }
                    isok = true;
                }else{
                    //空方法
                    isok = true;
                }
            }catch (Exception e){
                e.printStackTrace();
                logger.error(e.getMessage(),e);
            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_ASSIGN_REDIS);

            }
        }else{
                isok = true;
            }
            XxlJobLogger.log("【推送消息XtAssignServerTask】结束执行：{}", DateUtil.formatDateTime(new Date()));

            if (isok) {
                return SUCCESS;
            } else {
                return FAIL;
            }
    }

//    public static void main(String[] args) {
//        String str = "Hello 你好 World!笨蛋";
//
//        str=str.substring(0,5200);
//
//        // 定义正则表达式，匹配所有的中文字符
//        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
//        Matcher matcher = pattern.matcher(str);
//        System.out.println("str"+str);
//        while (matcher.find()) {
//            System.out.print(matcher.group());
//        }
//    }

}
