package com.inspur.workinfo.jobhandler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.task.XtMaterialServerTask;
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JobHandler(value = "xtMaterialServerJobHandler")
@Component
@Slf4j
public class XtMaterialServerJobHandler extends IJobHandler {

    private Logger logger  = LoggerFactory.getLogger(XtMaterialServerJobHandler.class);

    @Autowired
    private ApproveCallService approveCallService;
    @Autowired
    private ApproveCallResultService approveCallResultService;
    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;
    @Autowired
    private XtApproveBusinessCourseService courseService;
    @Autowired
    private ApiServiceCatalogService apiServiceCatalogService;
    @Autowired
    private XtApproveBusinessMaterialService materialService;
    @Autowired
    private ApiInputInfoService apiInputInfoService;

    @Autowired
    private RedisCache redisCache;
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
        XxlJobLogger.log("【XtMaterialServerTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_info =  redisCache.getCacheObject( CommonConstants.XT_BUSINESS_MATERIAL_REDIS);
        if (StrUtil.isBlank(xt_business_info)) {
            //如果是空的先将数据插入
            redisCache.setCacheObject(CommonConstants.XT_BUSINESS_MATERIAL_REDIS,uuid);
            try {
                Page page = new Page(1,200);
                //1获取流程为材料类型的环节信息
                IPage<XtApproveBusinessCourse> businessCourse = courseService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessCourse>()
                                .eq("ACTIVE","1")
                                .eq("CURRENT_NODE_CODE",CommonConstants.XT_BUSINESS_MATERIAL));

                if (businessCourse!=null&&businessCourse.getTotal()>0) {
                    for (int i = 0; i < businessCourse.getRecords().size(); i++) {
                        //获取当前流水号和当前流水号对应流程
                        String sblshshort = businessCourse.getRecords().get(i).getSblshShort();
                        String currentNodeId = businessCourse.getRecords().get(i).getCurrentNodeId();

                        try {
                            //1.根据当前流程获取节点配置的信息
                            XtApproveItemflowConfig xtApproveItemflowConfig = itemflowConfigService.getById(currentNodeId);
                            if (xtApproveItemflowConfig != null) {
                                //如果节点中配置了分发接口直接调用内部流程接口
                                if ("0".equals(xtApproveItemflowConfig.getExchangeType())) {
                                    if (StrUtil.isNotBlank(xtApproveItemflowConfig.getApiId())) {
                                        //获取api配置并调用，分发过程可以绑定多个接口
                                        //获取入参信息
                                        //创建接口记录相关代码
                                        //获取材料列表信息，最终解析成为map
                                        List<XtApproveBusinessMaterial> materialList  = materialService
                                                .list(new QueryWrapper<XtApproveBusinessMaterial>().eq("SBLSH_SHORT",sblshshort));

                                        //判断materialList 是否存在材料数据
                                        if (materialList!=null&&materialList.size()>0){
                                            boolean isok = true;
                                            for (int j = 0; j <materialList.size() ; j++) {
                                                ApiServiceCatalog apiServiceCatalog = apiServiceCatalogService.getById(xtApproveItemflowConfig.getApiId());
                                                ApproveCall callBean = new ApproveCall();
                                                //创建接口调用记录 后期用aop处理多余代码
                                                ApproveCallResult callResultBean = new ApproveCallResult();
                                                String callId = UUID.randomUUID().toString().replace("-", "");
                                                callBean.setBsnum(sblshshort);//获取token接口不存在
                                                callBean.setCalledSystemAddr(apiServiceCatalog.getUrl());
                                                callBean.setCalledSystemCode(materialList.get(j).getStuffSeq());
                                                callBean.setCallId(callId);
                                                callBean.setCalledSystemName(apiServiceCatalog.getCatalogName());
                                                callBean.setCallTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss", new Date()));

                                                try {
                                                    //创建map,复用办件分发接口
                                                    Map<String, Object> map =  itemflowConfigService.getImportantXtMessage(xtApproveItemflowConfig,sblshshort);
                                                    map.put(CommonConstants.XT_BUSINESS_MATERRIAL,materialList.get(j));
                                                    if (map != null) {
                                                        callBean.setParameterValue(new Gson().toJson(map));
                                                        //TODO 接口调用 为完成
                                                        R result = apiInputInfoService.getServiceByMap(xtApproveItemflowConfig.getApiId(), map,sblshshort);
                                                        //result判断固定参数为 code msg data
                                                        //TODO 后期改为可以配合out表使用的参数
                                                        JSONObject res = (JSONObject) result.getData();
                                                        if (!CommonConstants.API_SUCCESS.equals(res.getString("code"))) {

                                                            throw new Exception("接口调用不成功，办件编号为"+sblshshort+",材料编码为"+materialList.get(j).getStuffSeq()+",返回值："+res);

                                                        }
                                                        //接口调用记录处理
                                                        callResultBean.setResultValue(res.toJSONString());
                                                        callResultBean.setCalledSystemName(callBean.getCalledSystemName());
                                                        callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
                                                        callResultBean.setCallState(res.getString("code"));
                                                        callResultBean.setCallTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss", new Date()));
                                                        callResultBean.setCallId(callId);
                                                        callResultBean.setSeqId(UUID.randomUUID().toString().replace("-", ""));

                                                        callBean.setCallState(CommonConstants.API_SUCCESS);

                                                        approveCallService.saveOrUpdate(callBean);
                                                        approveCallResultService.saveOrUpdate(callResultBean);


                                                    } else {
                                                        logger.error("材料信息不完整！事项编码为" + xtApproveItemflowConfig.getSxbm());
                                                        throw  new Exception("材料信息不完整！事项编码为" + xtApproveItemflowConfig.getSxbm());
                                                    }
                                                }catch(Exception e){
                                                    isok = false;
                                                    //接口调用记录处理
                                                    callResultBean.setResultValue(e.getMessage());
                                                    callResultBean.setCalledSystemName(callBean.getCalledSystemName());
                                                    callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
                                                    callResultBean.setCallState(CommonConstants.API_FAIL);
                                                    callResultBean.setCallTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss", new Date()));
                                                    callResultBean.setCallId(callId);
                                                    callResultBean.setSeqId(UUID.randomUUID().toString().replace("-", ""));
                                                    callBean.setCallState(CommonConstants.API_FAIL);
                                                    approveCallService.saveOrUpdate(callBean);
                                                    approveCallResultService.saveOrUpdate(callResultBean);
                                                    break;

                                                }
                                            }
                                            //判断res中封装值
                                            if (isok) {

                                                //并进入下一个流程
                                                this.courseService.analysisCourse(sblshshort);

                                                //中间状态既不修改也不做处理，下次调用依旧处理
                                            } else {
                                                throw new Exception("接口调用不成功，办件编号为"+sblshshort);

                                            }

                                        }


                                    }else{
                                        logger.error("事项流程存在问题未配置调用接口，请联系管理员处理！事项编码为"+xtApproveItemflowConfig.getSxbm());
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
                            continue;
                        }


                    }

                }
                isflag=true;
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_MATERIAL_REDIS);
            }
        }else{

            //空方法
            isflag=true;
        }
        XxlJobLogger.log("【推送消息XtMaterialServerTask】执行结束：{}", DateUtil.formatDateTime(new Date()));

        if(isflag){
            return SUCCESS;
        }else{
            return FAIL;
        }
    }
}
