package com.inspur.workinfo.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.XtApproveBusinessBase;
import com.inspur.workinfo.entity.XtApproveBusinessCourse;
import com.inspur.workinfo.entity.XtApproveBusinessMaterial;
import com.inspur.workinfo.entity.XtApproveBusinessinfo;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class XtMaterialServerTask {
    private Logger logger  = LoggerFactory.getLogger(XtMaterialServerTask.class);

    @Autowired
    private XtApproveBusinessinfoService businessInfoService;
    @Autowired
    private ApproveCallService approveCallService;
    @Autowired
    private ApproveCallResultService approveCallResultService;
    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;
    @Autowired
    private XtApproveBusinessCourseService courseService;
    @Autowired
    private XtApproveBusinessBaseService businessBaseService;
    @Autowired
    private XtApproveBusinessMaterialService materialService;
    @Autowired
    private DisabilityService disabilityService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    PropertyConfig propertyConfig;
    /**
     * 按照标准时间来算，每隔 30min 执行一次
     * 任务为办件查询和办件材料查询相关
     *
     */
//    @Scheduled(cron = "0 */1 * * * ? ")
    public void websocket() throws Exception {
        logger.info("【XtMaterialServerTask】开始执行：{}", DateUtil.formatDateTime(new Date()));
        String uuid = UUID.randomUUID().toString();
        //控制每次任务最多处理10条 , 并加数据库锁采用redis
        String xt_business_info =  redisCache.getCacheObject( CommonConstants.XT_BUSINESS_MATERIAL_REDIS);
        if (StrUtil.isBlank(xt_business_info)) {
            //如果是空的先将数据插入
            redisCache.setCacheObject(CommonConstants.XT_BUSINESS_MATERIAL_REDIS,uuid);
            try {
                Page page = new Page();
                //1获取流程为材料类型的环节信息
                IPage<XtApproveBusinessCourse> businessCourse = courseService.getBaseMapper()
                        .selectPage(page, new QueryWrapper<XtApproveBusinessCourse>()
                                .eq("ACTIVE","1")
                                .eq("CURRENT_NODE_CODE",CommonConstants.XT_BUSINESS_MATERIAL));

                if (businessCourse!=null&&businessCourse.getTotal()>0){
                    businessCourse.getRecords().stream().forEach(details ->{
                        XtApproveBusinessBase businessBase =  businessBaseService.getOne(new QueryWrapper<XtApproveBusinessBase>().eq("SBLSH_SHORT",details.getSblshShort()));
                        try {
                            //如果表是空，则没有拉取完毕数据
                            if(businessBase!=null) {
                                //2推送对应材料信息，改材料状态，查询未交换的材料
                                IPage<XtApproveBusinessMaterial> businessMaterial = materialService.getBaseMapper()
                                        .selectPage(page, new QueryWrapper<XtApproveBusinessMaterial>()
                                                .eq("EXCHANGE","0")
                                                .eq("SBLSH_SHORT",details.getSblshShort()));

                                //存下交换成功的文件
                                List<XtApproveBusinessMaterial>  businessMaterialDone = new LinkedList<>();
                                businessMaterial.getRecords().stream().forEach(material ->{

                                    JSONObject callParams = new JSONObject();
                                    callParams.put("docId", material.getAttachPath());
                                    callParams.put("fileName", material.getClmc());
                                    callParams.put("idCard", businessBase.getGrIdcardno());
                                    //创建接口调 用记录表
                                    approveCallService.createCallBean(details.getSblshShort(),
                                            propertyConfig.getDispatchUrl(), callParams.toJSONString(), "两补", "POST", "材料上传接口");

                                    JSONObject materialResult = disabilityService.upLoadImg(material.getAttachPath(),material.getClmc(),businessBase.getGrIdcardno());

                                    //成功的加入已经成功的序列里
                                });


                                //3.处理流程，保存环节信息，材料处理完毕的情况
                                //先查询环节信息 激活的ACTIVE
                                JSONObject courseJsonData = this.courseService.analysisCourse(details.getSblshShort());
                                if (!CommonConstants.API_SUCCESS.equals(courseJsonData.getString("code"))) {
                                    throw new Exception("保存过程信息失败！" + courseJsonData.getString("error"));
                                }
                            }
                        } catch (Exception e) {

                            //如果捕获到异常直接跳过，进入下次循环
                            return;

                        }
                    });

                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisCache.deleteObject(CommonConstants.XT_BUSINESS_MATERIAL_REDIS);
            }
        }else{

            //空方法
        }
        logger.info("【推送消息XtMaterialServerTask】执行结束：{}", DateUtil.formatDateTime(new Date()));
    }
}
