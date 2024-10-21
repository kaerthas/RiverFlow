package com.inspur.workinfo.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.gov.api.client.AtgBusClient;
import com.alibaba.gov.api.client.DefaultAtgBusClient;
import com.alibaba.gov.api.domain.ApplicantVO;
import com.alibaba.gov.api.domain.AtgBusSecretKey;
import com.alibaba.gov.api.domain.HallVO;
import com.alibaba.gov.api.domain.PickUpAddressInfoVO;
import com.alibaba.gov.api.request.AtgBizAffairAcceptRequest;
import com.alibaba.gov.api.request.AtgBizAffairFinishRequest;
import com.alibaba.gov.api.request.AtgBizAffairSupplementNotifyRequest;
import com.alibaba.gov.api.response.AtgBizAffairAcceptResponse;
import com.alibaba.gov.api.response.AtgBizAffairFinishResponse;
import com.alibaba.gov.api.response.AtgBizAffairSupplementNotifyResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.service.*;
import com.inspur.workinfo.util.DateUtils;
import com.inspur.workinfo.util.HttpClientUtils;
import com.inspur.workinfo.util.UploadUtil;
import com.inspur.workinfo.util.XmlHandleUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class AtgBizAffairRecevieServiceImpl implements AtgBizAffairRecevieService{
    @Autowired
    private XtApproveBusinessinfoService xtApproveBusinessinfoService;
    @Autowired
    private XtApproveBusinessCourseService courseService;
    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;
    @Autowired
    private XtApproveItemItemflowService itemItemflowService;
    @Autowired
    private XtApproveItemConfigService xtApproveItemConfigService;

    @Autowired
    private XtApproveBusinessBaseService xtApproveBusinessBaseService;

    @Autowired
    private XtApproveBusinessEmailService xtApproveBusinessEmailService;

    @Autowired
    private XtApproveBusinessMaterialService materialService;
    @Autowired
    private PropertyConfig propertyConfig;
    @Autowired
    private XtApproveExchangeService exchangeService;
    @Autowired
    private ApproveCallService callService;
    @Autowired
    private ApproveCallResultService callResultService;
    @Autowired
    private XtApproveBusinessSpecialService businessSpecialService;


    private final Logger logger = LoggerFactory.getLogger(AtgBizAffairRecevieServiceImpl.class);


    @Autowired
    private XtApproveBusinessXmlConfigService xmlConfigService;
    @Override
    @Transactional
    public void parser(String projId, String mattercode, String areaCode, String affairType, String applyOrigin, String appId, String bizType,String businessInfo) throws Exception {
        //启用事务控制相关保存，保证事务的一致性
        XtApproveBusinessinfo xtApproveBusinessinfo = new XtApproveBusinessinfo();
        try{

            XtApproveBusinessinfo businessinfoOld=xtApproveBusinessinfoService.getOne(new QueryWrapper<XtApproveBusinessinfo>()
                    .eq("SBLSH_SHORT",projId));
            if (businessinfoOld!=null){
                throw new Exception("当前业务已成功推送到审批，无需多次推送该业务！");
            }else{
                xtApproveBusinessinfo.setSeqId(UUID.randomUUID().toString());
                xtApproveBusinessinfo.setIsUsed("0");
                xtApproveBusinessinfo.setSblshShort(projId);
                xtApproveBusinessinfo.setChannelCode(applyOrigin);
                xtApproveBusinessinfo.setExpressType(affairType);
                xtApproveBusinessinfo.setBizType(bizType);
                xtApproveBusinessinfo.setSxbm(mattercode);
                xtApproveBusinessinfo.setXzqhdm(areaCode);
                xtApproveBusinessinfo.setAppId(appId);
                xtApproveBusinessinfo.setBusinessInfo(businessInfo);
                xtApproveBusinessinfoService.save(xtApproveBusinessinfo);
            }
            //查询关联关系表
            List<XtApproveItemItemflow> itemItemflows =  itemItemflowService.getBaseMapper()
                    .selectList(new QueryWrapper<XtApproveItemItemflow>()
                            .eq("ITEM_SXBM",mattercode));
            if (itemItemflows!=null && itemItemflows.size()==1) {
                //根据配置的流程信息，写入业务过程信息表

                XtApproveItemflowConfig itemflowBean = itemflowConfigService.getBaseMapper()
                        .selectOne(new QueryWrapper<XtApproveItemflowConfig>()
                                .eq("PARENT_ID", "#").eq("SXBM", itemItemflows.get(0).getItemflowSxbm()));
                if (itemflowBean == null) {
                    throw new Exception("流程配置错误，无需多次推送该业务！");
                }
                XtApproveBusinessCourse courseBean = new XtApproveBusinessCourse();
                courseBean.setSeqId(UUID.randomUUID().toString());
                courseBean.setCurrentNodeCode(itemflowBean.getNodeCode());
                courseBean.setCurrentNodeId(itemflowBean.getSeqId());
                courseBean.setActive("1");//1表示激活状态
                courseBean.setSblshShort(projId);
                courseService.save(courseBean);//保存过程信息
            }else{
                throw new Exception("流程关联关系配置错误，请联系管理员！");
            }
        }catch (Exception e){
            e.printStackTrace();
            if (xtApproveBusinessinfo!=null&& StrUtil.isNotBlank(xtApproveBusinessinfo.getSeqId())) {
                xtApproveBusinessinfoService.removeById(xtApproveBusinessinfo.getSeqId());
            }
            throw  e;
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JSONObject analysisApplyData(String sxbm, String businessInfo)throws Exception {
        //处理申报数据保存到相关材料表办件表等等
        //初始化返回值
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "请求成功！");
        try {

            ObjectMapper objectMapper  = new ObjectMapper();
            //第一步根据是事项编码查询，事项垂管id
            XtApproveItemConfig xtApproveItemConfig = xtApproveItemConfigService.getOne(new QueryWrapper<XtApproveItemConfig>()
                    .eq("sxbm",sxbm));

            String  itemId  = xtApproveItemConfig.getItemId();
            String  serviceObj = xtApproveItemConfig.getServiceObj();
            if (StrUtil.isNotBlank(itemId)){
                //开始分析
                JSONObject  applyJsonData  = (JSONObject) JSONObject.parse(businessInfo);
                if(applyJsonData!=null) {
                    String projId = applyJsonData.getString("projId");//办件编号
                    String matterCode = applyJsonData.getString("matterCode");//事项编码
                    String deptCode = applyJsonData.getString("deptCode");//部门编码
                    String bizType = applyJsonData.getString("bizType");//业务类型
                    String affairType = applyJsonData.getString("affairType");//办件类型
                    String relBizId = applyJsonData.getString("relBizId");//下一个业务标识
                    ApplicantVO applicantVO = objectMapper.readValue(applyJsonData //办件人信息
                            .getJSONObject("applicantVO").toJSONString(), ApplicantVO.class);
                    String projectName = applyJsonData.getString("projectName");//事项名称
                    String projectNature = applyJsonData.getString("projectNature");//项目性质
                    String recvDeptCode = applyJsonData.getString("recvDeptCode");//收件部门编码
                    String recvDeptName = applyJsonData.getString("recvDeptName");//收件部门名称
                    String execDeptOrgCode = applyJsonData.getString("execDeptOrgCode");//统一社会信用代码
                    String applyOrigin = applyJsonData.getString("applyOrigin");//申报来源
                    String approveType = applyJsonData.getString("approveType");//审批性质
                    String recvUserType = applyJsonData.getString("recvUserType");//创建用户类型
                    String recvUserId = applyJsonData.getString("recvUserId");//用户唯一标识
                    String recvUserName = applyJsonData.getString("recvUserName");//用户名称

                    String affFormInfo = applyJsonData.getString("affFormInfo");//表单信息

                    JSONArray suffInfoList = applyJsonData.getJSONArray("suffInfoList");//材料信息
                    String memo = applyJsonData.getString("memo");//办件摘要
                    Date gmtApply = applyJsonData.getString("gmtApply") != null ? new Date(applyJsonData.getString("gmtApply")) : null;
                    //办件申请时间
                    String appId = applyJsonData.getString("appId");//统一分配应用id
                    String areaCode = applyJsonData.getString("areaCode");//收件部门行政区划
                    JSONObject extInfo = applyJsonData.getJSONObject("extInfo");


                    HallVO hallVO = applyJsonData.getJSONObject("hallVO") != null ? objectMapper.readValue(applyJsonData.getJSONObject("hallVO").toJSONString(), HallVO.class) : null;

                    //                PickUpAddressInfoVO pickUpAddressInfoVO = objectMapper.readValue(applyJsonData.getJSONObject("pickUpAddressInfoVO").toJSONString(),PickUpAddressInfoVO.class);


                    //第二步 插入基本信息表，批次表，调用定时调用批次表 分发配置表redis 组装模型
                    //创建基本信息表  applyAcceptData
                    // TODO 后续应该按照接口对返回参数进行可配置，目前对接协同写死
                    /*******************************************保存基本表*********************************************/
                    XtApproveBusinessBase businessBase = new XtApproveBusinessBase();
                    String baseInfoId = java.util.UUID.randomUUID().toString();

                    businessBase.setSeqId(baseInfoId);//绑定其他业务信息
                    businessBase.setSblshShort(projId);
                    businessBase.setSxbm(sxbm);
                    businessBase.setSxmc(projectName);
                    businessBase.setSxqxbm("");
                    businessBase.setXzqhdm(areaCode);
                    businessBase.setBmmc(recvDeptName);
                    businessBase.setBmzzjgdm(recvDeptCode);
                    businessBase.setYwly(applyOrigin);//业务来源
                    businessBase.setSbsj(gmtApply);


                    //个人存入人员信息
                    if ("0".equals(serviceObj)) {
                        businessBase.setServiceObj(serviceObj);
                        businessBase.setGrName(applicantVO.getApplyName());
                        businessBase.setGrIdcardno(applicantVO.getApplyCardNo());
                        businessBase.setGrIdentitytype(applicantVO.getApplyCardType());
                        businessBase.setGrLinkphone(applicantVO.getApplyTelNo());
                    }
                    //法人存入法人信息
                    if ("1".equals(serviceObj)) {
                        businessBase.setServiceObj(serviceObj);
                        businessBase.setQyOrgName(applicantVO.getLegalMan());
                        businessBase.setQyOrgCode(applicantVO.getLegalCardNo());
                        businessBase.setQyHandlerName(applicantVO.getContactName());
                        businessBase.setQyHandlerPhone(applicantVO.getContactTelNo());
                        businessBase.setQyHandlerId(applicantVO.getContactCardNo());
                        businessBase.setQyHandlerIdtype(applicantVO.getContactCardType());
                    }
                    xtApproveBusinessBaseService.saveOrUpdate(businessBase);
                    /*******************************************保存邮寄信息表*********************************************/
                    if (extInfo != null && extInfo.getJSONObject("deliveryInfo") != null) {
                        JSONObject toAddress = extInfo.getJSONObject("deliveryInfo").getJSONObject("toAddress");
                        XtApproveBusinessEmail businessEmail = new XtApproveBusinessEmail();

                        businessEmail.setSeqId(java.util.UUID.randomUUID().toString());
                        businessEmail.setBaseInfoId(baseInfoId);
                        //                    businessEmail.setMailType(toAddress.getString("mailType"));
                        businessEmail.setSendMailAddress(toAddress.getString("address"));
                        businessEmail.setSendMailPostCode(toAddress.getString("postCode"));
                        businessEmail.setSendMailName(toAddress.getString("name"));
                        businessEmail.setSendMailProvince(toAddress.getString("provinceName"));
                        businessEmail.setSendMailCity(toAddress.getString("cityName"));
                        businessEmail.setSendMailCounty(toAddress.getString("districtName"));
                        businessEmail.setSendMailPhone(toAddress.getString("phone"));
                        xtApproveBusinessEmailService.saveOrUpdate(businessEmail);
                    }


                    //处理表单信息并动态保存
                    if (StrUtil.isNotBlank(affFormInfo)) {//表单字段不为空
                        JSONObject formJson = (JSONObject) JSONObject.parse(affFormInfo);

                        if (formJson != null) {//判断有表单数据
                            //不对接产品表单按数据存库
                            List<XtApproveBusinessXmlConfig> xmlConfigs = xmlConfigService.getBaseMapper().selectList(new QueryWrapper<XtApproveBusinessXmlConfig>()
                                    .eq("ITEM_ID", itemId));
                            Map<String, Object> params = new HashMap<>();
                            String[] colums = new String[xmlConfigs.size()];
                            for (int i = 0; i < xmlConfigs.size(); i++) {
                                if ("table".equals(xmlConfigs.get(i).getType())) {
                                    //将表名插入map
                                    params.put("tableName", xmlConfigs.get(i).getXmlCode());
                                } else if ("column".equals(xmlConfigs.get(i).getType())) {
                                    //将字段插入数组
                                    colums[i] = xmlConfigs.get(i).getXmlCode();
                                    //将值遍历插入
                                    if (StrUtil.isNotBlank(formJson.getString(colums[i]))) {
                                        params.put(colums[i].toString(), formJson.getString(colums[i]));
                                    } else {
                                        if (!"onlineApplyId".equals(xmlConfigs.get(i).getXmlCode())) {
                                            params.put(colums[i].toString(), "");
                                        }
                                    }
                                    if ("idcard".equals(colums[i])) {
                                        List<XtApproveBusinessSpecial> specials = businessSpecialService.getBaseMapper().selectList(
                                                new QueryWrapper<XtApproveBusinessSpecial>().eq("IDCARD", formJson.get(colums[i]))
                                        );
                                        if (specials != null && specials.size() > 0) {
                                            params.put("onlineApplyId", specials.get(0).getOnlineApplyId());
                                        }
                                    }


                                } else if ("keyword".equals(xmlConfigs.get(i).getType())) {
                                    //TODO 后续修改为可配置的关联关系
                                    params.put("keyword", xmlConfigs.get(i).getXmlCode());
                                    params.put("keywordvalue", projId);
                                }
                                //                            else if("custom".equals(xmlConfigs.get(i).getXmlType())){
                                //                                params.put("custom",xmlConfigs.get(i).getXmlCode());
                                //                                //将值遍历插入
                                //                                if(StrUtil.isNotBlank(formJson.getString(colums[i]))){
                                //                                    params.put("customvalue",formJson.getString(colums[i]));
                                //                                }else{
                                //                                    params.put("customvalue","");
                                //                                }
                                //                            }

                            }
                            //循环结束将字段名数组插入map
                            params.put("columns", colums);
                            //拼装完成后插入相关
                            //                        xmlConfigService.selectXmlByCustomProvider(params);
                            xmlConfigService.insertXmlDataProvider(params);
                        }

                    }
                    /**********************************保存材料信息表**************************************/
                    if (!suffInfoList.isEmpty()) {
                        //保存材料信息
                        //创建一个材料的list
                        List<XtApproveBusinessMaterial> materials = new ArrayList<>();

                        for (int i = 0; i < suffInfoList.size(); i++) {
                            String attachName = suffInfoList.getJSONObject(i).getString("attachName");
                            if(attachName!= null && attachName != "") {
                                List<String> attachNameList = Arrays.asList(attachName.split(";"));
                                String attachPath = suffInfoList.getJSONObject(i).getString("attachPath");
                                List<String> attachPathList = Arrays.asList(attachPath.split(";"));
                                for (int j = 0; j < attachNameList.size(); j++) {
                                    XtApproveBusinessMaterial material = new XtApproveBusinessMaterial();
                                    material.setSeqId(java.util.UUID.randomUUID().toString());
                                    material.setSblshShort(projId);//业务办理编号

                                    material.setStuffSeq(StrUtil.isNotBlank(suffInfoList.getJSONObject(i).getString("stuffUniId")) ? suffInfoList.getJSONObject(i).getString("stuffUniId") : "");//事项中心材料唯一编码

                                    material.setClmc(StrUtil.isNotBlank(suffInfoList.getJSONObject(i).getString("stuffName")) ? suffInfoList.getJSONObject(i).getString("stuffName") : "");//材料名称

                                    material.setWjlx(StrUtil.isNotBlank(suffInfoList.getJSONObject(i).getString("stuffType")) ? suffInfoList.getJSONObject(i).getString("stuffType") : "");

                                    material.setCllx(StrUtil.isNotBlank(suffInfoList.getJSONObject(i).getString("fetchMode")) ? suffInfoList.getJSONObject(i).getString("fetchMode") : "");//材料类型

                                    material.setClsl(Integer.valueOf(suffInfoList.getJSONObject(i).getString("stuffNum")));//材料数量

                                    String attachNameSingle = attachNameList.get(j);
                                    material.setAttachName(attachNameSingle);//材料标准名称
                                    //                        material.setAttachId(suffInfoList.getJSONObject(i).getString("attachId"));
                                    material.setRemark(suffInfoList.getJSONObject(i).getString("memo"));
                                    if (StrUtil.isNotBlank(attachNameSingle) && attachNameSingle.contains(".")) {
                                        material.setAttachType(attachNameSingle.substring(attachNameSingle.lastIndexOf(".") + 1));
                                    }

                                    material.setAttachBody(attachPathList.get(j));//存储路径
                                    material.setAttachPath(attachPathList.get(j));
                                    String base64 = UploadUtil.getBase64ByFilePath(attachPathList.get(j));
                                    material.setBase64(base64);
                                    materials.add(material);
                                }
                            }
                        }
                        materialService.saveBatch(materials, materials.size());
                    }
                }



            }else{
                throw new Exception("请联系管理员，获取事项垂管id为空！");
            }

            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }

    }
    /*****
     *@deprecated 调用数浙的受理推送接口
     *@param businessAccept
     *@author kaerthas
     *@Date 2024-08-12 10:00:00
     * *****/
    @Override
    @Transactional
    public JSONObject sendBusinessAccept(XtApproveBusinessAccept businessAccept) {
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("code", CommonConstants.API_SUCCESS);
        jsonResult.put("error", "");
        ApproveCall callBean=new ApproveCall();
        ApproveCallResult callResultBean=new ApproveCallResult();
        String callRestXml = "";

        try {
            String gatewayUrl =propertyConfig.getGatewayUrl();//获取数浙网关地址
            String gatewayAppId =propertyConfig.getGatewayAppId();//获取APPID
            String gatewaySecret=propertyConfig.getGatewaySercret();//获取secret
            String keyId =propertyConfig.getGatewayKeyId();//获取keyId

            //封装数浙的接口
            List<AtgBusSecretKey> secretKeys = new ArrayList<AtgBusSecretKey>();
            AtgBusSecretKey atgBusSecretKey = new AtgBusSecretKey(keyId, gatewaySecret);
            secretKeys.add(atgBusSecretKey);
            //2. 初始化客⼾端
            AtgBusClient atgBusClient = new DefaultAtgBusClient(gatewayUrl, gatewayAppId, secretKeys);
            AtgBizAffairAcceptRequest affairAcceptRequest   = new AtgBizAffairAcceptRequest();
            affairAcceptRequest.setProjId(businessAccept.getSblshShort());//办件编号
            affairAcceptRequest.setMemo(businessAccept.getYwslyj());//业务处理意见
            affairAcceptRequest.setAreaCode(businessAccept.getYwslqhbm());//办理区划
            affairAcceptRequest.setGmtAccept(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss",businessAccept.getYwslsj()));//业务受理时间
            affairAcceptRequest.setAppId(gatewayAppId);
            //TODO 没有相关必填字段
            affairAcceptRequest.setOperatorUid("111111111111111111");///业务部门操作人员id
            affairAcceptRequest.setOperatorName("审批人员");//业务部门操作人员名称
            affairAcceptRequest.setPromiseTime(null);//承诺办结时间
            affairAcceptRequest.setDeptCode(businessAccept.getYwslbmbm());//业务部门代码
            affairAcceptRequest.setDeptName(businessAccept.getYwslqhmc());//业务部门名称
            //组装调用记录信息
            callBean.setBsnum(businessAccept.getSblshShort());
            callBean.setCalledSystemAddr(gatewayUrl);
            callBean.setCalledSystemCode("");
            callBean.setCalledSystemName("高效办成一件事系统");
            callBean.setCallId(java.util.UUID.randomUUID().toString());
            callBean.setCallParameter("atg.biz.affair.accept");
            callBean.setCallTime(new Date());
            callBean.setCallTimes(1);
            callBean.setInterfaceName("atg.biz.affair.accept");
            callBean.setParameterValue(affairAcceptRequest.toString());
            //组装调用结果信息
            callResultBean.setSeqId(java.util.UUID.randomUUID().toString());
            callResultBean.setCallTime(new Date());
            callResultBean.setCalledSystemName(callBean.getCalledSystemName());
            callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
            callResultBean.setCallId(callBean.getCallId());
            AtgBizAffairAcceptResponse response = atgBusClient.execute(affairAcceptRequest);
            ObjectMapper objectMapper = new ObjectMapper();


            //TODO 后续用注解抽出多余代码
            if(!CommonConstants.ATG_SUCCESS.equals(response.getResultStatus())){
                jsonResult.put("code", CommonConstants.API_FAIL);
                jsonResult.put("error", "接口返回失败");
                callBean.setCallState(CommonConstants.API_FAIL);
                callResultBean.setResultValue(objectMapper.writeValueAsString(response));
            }else {
                    //3.判断业务是否推送成功,如果成功变更流程
                    courseService.analysisCourse(businessAccept.getSblshShort()) ;
                    callBean.setCallState(CommonConstants.API_SUCCESS);
                    callResultBean.setResultValue(objectMapper.writeValueAsString(response));
            }
            callResultBean.setCallState(callBean.getCallState());
            callService.saveOrUpdate(callBean);
            callResultService.saveOrUpdate(callResultBean);
        } catch (Exception e) {
            jsonResult.put("code", CommonConstants.API_FAIL);
            jsonResult.put("error", "调用失败" + e.getMessage());
            e.printStackTrace();
            try{
                callBean.setCallState(CommonConstants.API_FAIL);
                callResultBean.setCallState(callBean.getCallState());
                callResultBean.setResultValue("返回结果：" + callRestXml + "----------异常原因：" + e.getMessage());
                callService.saveOrUpdate(callBean);
                callResultService.saveOrUpdate(callResultBean);
            }catch (Exception e1){
                jsonResult.put("code", "300");
                jsonResult.put("error", "保存接口调用记录失败！" + e.getMessage());
                logger.error("保存接口调用记录失败！", e);
                e.printStackTrace();
            }
        }
        return jsonResult;
    }

    @Override
    public JSONObject sendBusinessFinish(XtApproveBusinessDone businessDone) {
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "");
        ApproveCall callBean=new ApproveCall();
        ApproveCallResult callResultBean=new ApproveCallResult();
        String callRestXml = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String gatewayUrl =propertyConfig.getGatewayUrl();//获取数浙网关地址
            String gatewayAppId =propertyConfig.getGatewayAppId();//获取APPID
            String gatewaySecret=propertyConfig.getGatewaySercret();//获取secret
            String keyId =propertyConfig.getGatewayKeyId();//获取keyId

            //封装数浙的接口
            List<AtgBusSecretKey> secretKeys = new ArrayList<AtgBusSecretKey>();
            AtgBusSecretKey atgBusSecretKey = new AtgBusSecretKey(keyId, gatewaySecret);
            secretKeys.add(atgBusSecretKey);
            //封装数浙参数
            AtgBusClient atgBusClient = new DefaultAtgBusClient(gatewayUrl, gatewayAppId, secretKeys);
            //第一步根据是事项编码查询，事项垂管id
            XtApproveBusinessinfo businessinfo = xtApproveBusinessinfoService.getOne(new QueryWrapper<XtApproveBusinessinfo>()
                    .eq("SBLSH_SHORT",businessDone.getSblshShort()));
            AtgBizAffairFinishRequest affairFinishRequest = new AtgBizAffairFinishRequest();
            affairFinishRequest.setProjId(businessDone.getSblshShort());
            affairFinishRequest.setAppId(gatewayAppId);
            affairFinishRequest.setAreaCode(businessinfo.getXzqhdm());//获取区划信息
            affairFinishRequest.setGmtService(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(businessDone.getBjsj()));
            affairFinishRequest.setDeptCode(businessDone.getBjbmbm());//部门编码
            affairFinishRequest.setDeptName(businessDone.getBjbmmc());//部门名称
            //办结结果代码 10：办结 09：作废办结。13：退件。07：不予受理。
            affairFinishRequest.setResult("6".equals(businessDone.getBjjgdm())?"10":"07");
            affairFinishRequest.setResultDesc(businessDone.getBjjgms());
            affairFinishRequest.setMemo("你的申请已办结");
            affairFinishRequest.setOperatorName(businessDone.getSprxm());
            affairFinishRequest.setOperatorUid(businessDone.getSprdm());
            affairFinishRequest.setResultCode("");//默认传空



            //组装调用记录信息
            callBean.setBsnum(businessDone.getSblshShort());
            callBean.setCalledSystemAddr(gatewayUrl);
            callBean.setCalledSystemCode("");
            callBean.setCalledSystemName("高效办成一件事系统");
            callBean.setCallId(java.util.UUID.randomUUID().toString());
            callBean.setCallParameter("atg.biz.affair.finish");
            callBean.setCallTime(new Date());
            callBean.setCallTimes(1);
            callBean.setInterfaceName("atg.biz.affair.finish");
            callBean.setParameterValue(objectMapper.writeValueAsString(affairFinishRequest));
            //组装调用结果信息
            callResultBean.setSeqId(java.util.UUID.randomUUID().toString());
            callResultBean.setCallTime(new Date());
            callResultBean.setCalledSystemName(callBean.getCalledSystemName());
            callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
            callResultBean.setCallId(callBean.getCallId());


            AtgBizAffairFinishResponse response = atgBusClient.execute(affairFinishRequest);

            if(!CommonConstants.ATG_SUCCESS.equals(response.getResultStatus())){
                result.put("code",CommonConstants.API_FAIL);
                result.put("error", "接口返回失败");
                callBean.setCallState("0");
                callResultBean.setResultValue(objectMapper.writeValueAsString(response));
            }else {
                    //调用成功后
                    //3.判断业务是否推送成功,如果成功变更流程
                courseService.analysisCourse(businessDone.getSblshShort()) ;
                callBean.setCallState("1");
                callResultBean.setResultValue(objectMapper.writeValueAsString(response));

            }
            callResultBean.setCallState(callBean.getCallState());
            callService.saveOrUpdate(callBean);
            callResultService.saveOrUpdate(callResultBean);
        } catch (Exception e) {
            result.put("code",  CommonConstants.API_FAIL);
            result.put("error", "调用失败" + e.getMessage());
            e.printStackTrace();
            try{
                callBean.setCallState("0");
                callResultBean.setCallState(callBean.getCallState());
                callResultBean.setResultValue("返回结果：" + callRestXml + "----------异常原因：" + e.getMessage());
                callService.saveOrUpdate(callBean);
                callResultService.saveOrUpdate(callResultBean);
            }catch (Exception e1){
                result.put("code", "300");
                result.put("error", "保存接口调用记录失败！" + e.getMessage());
                logger.error("保存接口调用记录失败！", e);
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public void sendNeedCorrectionData(List<XtApproveBusinessNcorrect> businessNcorrectList, String sblshShort) {
        //接口调用记录表
        ApproveCall callBean=new ApproveCall();
        ApproveCallResult callResultBean=new ApproveCallResult();
        String callRestXml = "";

        //获取办件基本信息
        XtApproveBusinessBase businessBase =  xtApproveBusinessBaseService
                .getOne(new QueryWrapper<XtApproveBusinessBase>().eq("SBLSH_SHORT",sblshShort));
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String gatewayUrl =propertyConfig.getGatewayUrl();//获取数浙网关地址
            String gatewayAppId =propertyConfig.getGatewayAppId();//获取APPID
            String gatewaySecret=propertyConfig.getGatewaySercret();//获取secret
            String keyId =propertyConfig.getGatewayKeyId();//获取keyId

            //封装数浙的接口
            List<AtgBusSecretKey> secretKeys = new ArrayList<AtgBusSecretKey>();
            AtgBusSecretKey atgBusSecretKey = new AtgBusSecretKey(keyId, gatewaySecret);
            secretKeys.add(atgBusSecretKey);
            //封装数浙参数
            AtgBusClient atgBusClient = new DefaultAtgBusClient(gatewayUrl, gatewayAppId, secretKeys);
            //组装数浙相关补齐补正参数
            AtgBizAffairSupplementNotifyRequest  supplementNotifyRequest  =  new AtgBizAffairSupplementNotifyRequest();
            //projId,operatorUid,operatorName,deptCode,deptName,areaCode,stuffInfos,formFields,appId,memo,extInfo,gmtPatch,patchTimeLimit,timeUnit,patchReason,patchMode,receptionVO
            //TODO 不成型未封装完全
            supplementNotifyRequest.setProjId(businessBase.getSblshShort());
            supplementNotifyRequest.setOperatorUid("");
            supplementNotifyRequest.setOperatorName("");

            //组装调用记录信息
            callBean.setBsnum(businessBase.getSblshShort());
            callBean.setCalledSystemAddr(gatewayUrl);
            callBean.setCalledSystemCode("");
            callBean.setCalledSystemName("高效办成一件事系统");
            callBean.setCallId(java.util.UUID.randomUUID().toString());
            callBean.setCallParameter("atg.biz.affair.supplementNotify");
            callBean.setCallTime(new Date());
            callBean.setCallTimes(1);
            callBean.setInterfaceName("atg.biz.affair.supplementNotify");
            callBean.setParameterValue(objectMapper.writeValueAsString(supplementNotifyRequest));
            //组装调用结果信息
            callResultBean.setSeqId(java.util.UUID.randomUUID().toString());
            callResultBean.setCallTime(new Date());
            callResultBean.setCalledSystemName(callBean.getCalledSystemName());
            callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
            callResultBean.setCallId(callBean.getCallId());

            //拼接参数
            if (businessNcorrectList.size()>0){
                StringBuilder materialNames   =  new StringBuilder();
                StringBuilder materialNumbers  =  new StringBuilder();
                for (int i = 0; i <businessNcorrectList.size() ; i++) {
                    materialNames.append(businessNcorrectList.get(i).getBzclqd()).append(";");
                    materialNumbers.append(businessNcorrectList.get(i).getBqbzclbm()).append(";");

                }
                //去掉末尾多余拼接
                materialNames.deleteCharAt(materialNames.lastIndexOf(";"));
                materialNumbers.deleteCharAt(materialNumbers.lastIndexOf(";"));
                //材料清单
                AtgBizAffairSupplementNotifyResponse response = atgBusClient.execute(supplementNotifyRequest);

                if(!CommonConstants.ATG_SUCCESS.equals(response.getResultStatus())){
                    callBean.setCallState("0");
                    callResultBean.setResultValue(callRestXml);
                }else {

                        callBean.setCallState("1");
                        callResultBean.setResultValue(callRestXml);
                }
                callResultBean.setCallState(callBean.getCallState());
                callService.saveOrUpdate(callBean);
                callResultService.saveOrUpdate(callResultBean);
            }else{
                callBean.setCallState("0");
                callResultBean.setCallState(callBean.getCallState());
                callResultBean.setResultValue("返回结果：数据库不存在该办件的补正告知信息");
                callService.saveOrUpdate(callBean);
                callResultService.saveOrUpdate(callResultBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try{
                callBean.setCallState("0");
                callResultBean.setCallState(callBean.getCallState());
                callResultBean.setResultValue("返回结果：" + callRestXml + "----------异常原因：" + e.getMessage());
                callService.saveOrUpdate(callBean);
                callResultService.saveOrUpdate(callResultBean);
            }catch (Exception e1){
                logger.error("保存接口调用记录失败！", e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public JSONObject sendBusinessFinishNew(XtApproveBusinessAccept businessAccept) {
        JSONObject result = new JSONObject();
        result.put("code", CommonConstants.API_SUCCESS);
        result.put("error", "");
        ApproveCall callBean=new ApproveCall();
        ApproveCallResult callResultBean=new ApproveCallResult();
        String callRestXml = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String gatewayUrl =propertyConfig.getGatewayUrl();//获取数浙网关地址
            String gatewayAppId =propertyConfig.getGatewayAppId();//获取APPID
            String gatewaySecret=propertyConfig.getGatewaySercret();//获取secret
            String keyId =propertyConfig.getGatewayKeyId();//获取keyId
            //封装数浙的接口
            List<AtgBusSecretKey> secretKeys = new ArrayList<AtgBusSecretKey>();
            AtgBusSecretKey atgBusSecretKey = new AtgBusSecretKey(keyId, gatewaySecret);
            secretKeys.add(atgBusSecretKey);
            //封装数浙参数
            AtgBusClient atgBusClient = new DefaultAtgBusClient(gatewayUrl, gatewayAppId, secretKeys);

            AtgBizAffairFinishRequest affairFinishRequest = new AtgBizAffairFinishRequest();
            affairFinishRequest.setProjId(businessAccept.getSblshShort());
            affairFinishRequest.setAppId(gatewayAppId);
            affairFinishRequest.setAreaCode(businessAccept.getYwslqhbm());//获取区划信息
            affairFinishRequest.setGmtService(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(businessAccept.getYwslsj()));
            affairFinishRequest.setDeptCode(businessAccept.getYwslbmbm());//部门编码
            affairFinishRequest.setDeptName(businessAccept.getYwslbmmc());//部门名称
            affairFinishRequest.setResult("07");//办结结果代码 10：办结 09：作废办结。13：退件。07：不予受理。
            affairFinishRequest.setResultDesc(businessAccept.getYwslyj());
            affairFinishRequest.setMemo(StrUtil.isNotBlank(businessAccept.getYwslyj())?businessAccept.getYwslyj():"您的申请已办结");
            affairFinishRequest.setOperatorName(businessAccept.getYwslbmbm());
            affairFinishRequest.setOperatorUid(businessAccept.getSeqId());
            affairFinishRequest.setResultCode("");//默认传空



            //组装调用记录信息
            callBean.setBsnum(businessAccept.getSblshShort());
            callBean.setCalledSystemAddr(gatewayUrl);
            callBean.setCalledSystemCode("");
            callBean.setCalledSystemName("高效办成一件事系统");
            callBean.setCallId(java.util.UUID.randomUUID().toString());
            callBean.setCallParameter("atg.biz.affair.finish");
            callBean.setCallTime(new Date());
            callBean.setCallTimes(1);
            callBean.setInterfaceName("atg.biz.affair.finish");
            callBean.setParameterValue(objectMapper.writeValueAsString(affairFinishRequest));
            //组装调用结果信息
            callResultBean.setSeqId(java.util.UUID.randomUUID().toString());
            callResultBean.setCallTime(new Date());
            callResultBean.setCalledSystemName(callBean.getCalledSystemName());
            callResultBean.setCalledSystemAddr(callBean.getCalledSystemAddr());
            callResultBean.setCallId(callBean.getCallId());


            AtgBizAffairFinishResponse response = atgBusClient.execute(affairFinishRequest);

            if(!CommonConstants.ATG_SUCCESS.equals(response.getResultStatus())){
                result.put("code",CommonConstants.API_FAIL);
                result.put("error", "接口返回失败");
                callBean.setCallState("0");
                callResultBean.setResultValue(objectMapper.writeValueAsString(response));
            }else {
                //调用成功后
                //3.判断业务是否推送成功,如果成功变更流程
                courseService.analysisCourse(businessAccept.getSblshShort()) ;
                callBean.setCallState("1");
                callResultBean.setResultValue(objectMapper.writeValueAsString(response));

            }
            callResultBean.setCallState(callBean.getCallState());
            callService.saveOrUpdate(callBean);
            callResultService.saveOrUpdate(callResultBean);
        } catch (Exception e) {
            result.put("code",  CommonConstants.API_FAIL);
            result.put("error", "调用失败" + e.getMessage());
            e.printStackTrace();
            try{
                callBean.setCallState("0");
                callResultBean.setCallState(callBean.getCallState());
                callResultBean.setResultValue("返回结果：" + callRestXml + "----------异常原因：" + e.getMessage());
                callService.saveOrUpdate(callBean);
                callResultService.saveOrUpdate(callResultBean);
            }catch (Exception e1){
                result.put("code", "300");
                result.put("error", "保存接口调用记录失败！" + e.getMessage());
                logger.error("保存接口调用记录失败！", e);
                e.printStackTrace();
            }
        }
        return result;
    }
}
