package com.inspur.workinfo.task;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.service.impl.ApiInputInfoServiceImpl;
import com.inspur.workinfo.util.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private XtApproveBusinessBaseService businessBaseService;
    @Autowired
    private XtApproveItemConfigService itemConfigService;
    @Autowired
    private XtApproveBusinessXmlConfigService businessXmlConfigService;
    @Autowired
    private XtApproveBusinessMaterialService businessMaterialService;

    @Autowired
    private RedisCache redisCache;

    //定时分发流程
//    @Scheduled(cron = "0 */1 * * * ? ")
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
                                        //获取api配置并调用，一个过程可以绑定多个接口
                                        //获取入参信息
                                        Map<String ,Object> map  =  new HashMap<>();
                                        XtApproveBusinessBase businessBase = businessBaseService
                                                .getOne(new QueryWrapper<XtApproveBusinessBase>().eq("SBLSH_SHORT",sblshshort));
                                        //脚本转JSONObject
                                        JSONObject businessBaseJson  = JSONObject.parseObject(JSON.toJSONString(businessBase));
                                        //获取xmlData配置
                                        map.put(CommonConstants.XT_BUSINESS_BASE,businessBaseJson);
                                        //获取事项模型id
                                        XtApproveItemConfig itemConfig  = itemConfigService.getOne(new QueryWrapper<XtApproveItemConfig>()
                                                .eq("SXBM",xtApproveItemflowConfig.getSxbm()));
                                        //根据模型id获取xmlData模板
                                        List<XtApproveBusinessXmlConfig> xmlConfigs  = businessXmlConfigService.getBaseMapper()
                                                .selectList(new QueryWrapper<XtApproveBusinessXmlConfig>().eq("ITEM_ID",itemConfig.getItemId()));
                                        if (xmlConfigs!=null&&xmlConfigs.size()>0){
                                            //拼接查询需要用到的参数放到map中
                                            Map<String, Object> params  =  new HashMap<>();
                                            String[] colums  = new String[]{};
                                            for (int j= 0; j < xmlConfigs.size(); j++) {
                                                if ("table".equals(xmlConfigs.get(j).getType())){
                                                    //将表名插入map
                                                    params.put("tableName",xmlConfigs.get(j).getXmlCode());
                                                }else if("column".equals(xmlConfigs.get(i).getType())){
                                                    //将字段插入数组
                                                    colums[i] = xmlConfigs.get(i).getXmlCode();
                                                }else if("keyword".equals(xmlConfigs.get(i).getType())){
                                                    //将条件插入
                                                    params.put("keyword",xmlConfigs.get(i).getXmlCode());
                                                    params.put("keywordValue",sblshshort);
                                                }
                                            }
                                            //循环结束将字段名数组插入map
                                            params.put("columns",colums);
                                            Map<String,Object> xmlMap  = businessXmlConfigService.selectXmlDataByKeyWord(params);
                                            //传入xml 的JSONOBJECT
                                            JSONObject XMLObject = JSONObject.parseObject(JSON.toJSONString(map));
                                            map.put(CommonConstants.XT_BUSINESS_XML,XMLObject);
                                            //查询材料信息，传入map中
                                            List<XtApproveBusinessMaterial> businessMaterials   = businessMaterialService
                                                    .list(new QueryWrapper<XtApproveBusinessMaterial>().eq("SBLSH_SHORT",sblshshort));
                                            if (businessMaterials!=null && businessMaterials.size()>0){

                                                JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(businessMaterials));
                                                map.put(CommonConstants.XT_BUSINESS_FILE,jsonArray);

                                            }else{
                                                map.put(CommonConstants.XT_BUSINESS_FILE,null);
                                            }



                                        }else{
                                            logger.error("xml模板配置存在问题，请联系管理员处理！事项编码为"+xtApproveItemflowConfig.getSxbm());
                                        }


                                      //  apiInputInfoService.getServiceByMap(xtApproveItemflowConfig.getApiId(),map);
                                        //判断是否处理完成相关条件
                                        //获取接口完成将完成数据保存到物化表



                                        //并进入下一个流程
                                        this.businessCourseService.analysisCourse(sblshshort);
                                    }
                                } else {
                                    //TODO 如果配置了数据交换就去交换 目前尚没有需求
                                    if (StrUtil.isNotBlank(xtApproveItemflowConfig.getTableId())) {


                                    }

                                }


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
