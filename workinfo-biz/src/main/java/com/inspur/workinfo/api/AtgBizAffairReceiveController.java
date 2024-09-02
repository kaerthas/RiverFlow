package com.inspur.workinfo.api;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

import com.alibaba.gov.api.domain.ApplicantVO;
import com.alibaba.gov.callbackapi.request.CallbackAtgBizAffairReceiveRequest;
import com.alibaba.gov.callbackapi.response.CallbackAtgBizAffairReceiveResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;

import com.inspur.workinfo.service.*;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 针对业务对接新标准api 接口
 *
 * @author 对接协同调度中心配置相关表结构
 *
 */
@RestController
@Api(value = "osi", tags = "协同调度系统管理")
@RequestMapping("/___atg/biz/affair")
@Slf4j
public class AtgBizAffairReceiveController {

    private Logger logger  = LoggerFactory.getLogger(AtgBizAffairReceiveController.class);
    //	@Autowired
//	private DispatchService dispatchService;
    @Autowired
    private ApproveCallService callService; //接口调用记录表

    @Autowired
    private ApproveCallResultService approveCallResultService;
    @Autowired
    private XtApproveBusinessinfoService xtApproveBusinessinfoService;
    @Autowired
    private XtApproveBusinessCourseService courseService;
    @Autowired
    private XtApproveItemflowConfigService itemflowConfigService;
    @Autowired
    private XtApproveItemItemflowService itemItemflowService;
    @Autowired
    private XtApproveBusinessCorrectionService businessCorrectionService;
    @Autowired
    private XtApproveBusinessCorrectMService businessCorrectMService;
    @Autowired
    private StringEncryptor stringEncryptor;
    @Autowired
    private AtgBizAffairRecevieService affairRecevieService;

    @Autowired
    PropertyConfig propertyConfig;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    /****
     * @deprecated 对接数浙sdk
     * @author kaerthas
     * @Date 2024-8-9 10:21:00
     * ***/
    @SuppressWarnings("unchecked")
    @RequestMapping(value="/callback/receive")
    public CallbackAtgBizAffairReceiveResponse receive(@RequestBody CallbackAtgBizAffairReceiveRequest affairReceiveRequest) {
        //读取应用名称
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //准备好返回值
        CallbackAtgBizAffairReceiveResponse receiveResponse = new CallbackAtgBizAffairReceiveResponse();
        try {

            String requestStr =  new  Gson().toJson(affairReceiveRequest);//将实体类转换成jsonString
            JSONObject jsonObject = JSONObject.parseObject(requestStr);//转换成JSONObject

            String projId  = jsonObject.getString("projId");//办件编号

            Assert.isTrue(StrUtil.isNotBlank(projId),"【projId】办件编号不能为空！");

            String matterCode = jsonObject.getString("matterCode");//事项编码

            Assert.isTrue(StrUtil.isNotBlank(matterCode),"【matterCode】事项编码不能为空！");

//            String deptCode   = jsonObject.getString("deptCode");//部门编码
            String bizType	   = jsonObject.getString("bizType");//业务类型
            Assert.isTrue(StrUtil.isNotBlank(bizType),"【bizType】业务类型不能为空！");

            String affairType  = jsonObject.getString("affairType");//办件类型
            Assert.isTrue(StrUtil.isNotBlank(affairType),"【affairType】办件类型不能为空！");
            ObjectMapper o  = new ObjectMapper();

//            String relBizId	  = jsonObject.getString("relBizId");//下一个业务标识
            ApplicantVO applicantVO = o.readValue(jsonObject //办件人信息
                    .getJSONObject("applicantVO").toJSONString(),ApplicantVO.class);

            Assert.isTrue(applicantVO!=null,"【applicantVO】办件人信息不能为空！");

            String projectName	= jsonObject.getString("projectName");//事项名称
            Assert.isTrue(StrUtil.isNotBlank(projectName),"【projectName】事项名称不能为空！");


//            String projectNature = jsonObject.getString("projectNature");//项目性质
            String recvDeptCode  = jsonObject.getString("recvDeptCode");//收件部门编码
            Assert.isTrue(StrUtil.isNotBlank(recvDeptCode),"【recvDeptCode】收件部门编码不能为空！");

            String recvDeptName	 = jsonObject.getString("recvDeptName");//收件部门名称
            Assert.isTrue(StrUtil.isNotBlank(recvDeptName),"【recvDeptName】收件部门名称不能为空！");
            String execDeptOrgCode  =jsonObject.getString("execDeptOrgCode");//统一社会信用代码
            Assert.isTrue(StrUtil.isNotBlank(execDeptOrgCode),"【execDeptOrgCode】收件部门名称不能为空！");

            String applyOrigin	 = jsonObject.getString("applyOrigin");//申报来源
            Assert.isTrue(StrUtil.isNotBlank(applyOrigin),"【applyOrigin】申报来源不能为空！");

//            String approveType   = jsonObject.getString("approveType");//审批性质
//            String recvUserType	 = jsonObject.getString("recvUserType");//创建用户类型
//            String recvUserId	 = jsonObject.getString("recvUserId");//用户唯一标识
//            String recvUserName	 = jsonObject.getString("recvUserName");//用户名称
//
            //String affFormInfo	= jsonObject.getString("affFormInfo");//表单信息
            JSONObject affFormInfoObj = jsonObject.getJSONObject("affFormInfo");
            Assert.isTrue(affFormInfoObj.size()>0,"严重报错【affFormInfo】表单信息不能为空！");
            String affFormInfo  = affFormInfoObj.toJSONString();
//
//            JSONArray suffInfoList   =jsonObject.getJSONArray("suffInfoList");//材料信息
//            String memo   = jsonObject.getString("memo");//办件摘要
            String gmtApplyStr = jsonObject.getString("gmtApply");
            Assert.isTrue(StrUtil.isNotBlank(gmtApplyStr),"【gmtApply】办结时间不能为空！");

            Date gmtApply = StrUtil.isNotBlank(gmtApplyStr)?new Date(gmtApplyStr):null;


            //办件申请时间
            String appId  = jsonObject.getString("appId");//统一分配应用id
            String areaCode	 = jsonObject.getString("areaCode");//收件部门行政区划
            Assert.isTrue(StrUtil.isNotBlank(areaCode),"【areaCode】收件部门行政区划不能为空！");
//            JSONObject extInfo	 = jsonObject.getJSONObject("extInfo");


//            HallVO hallVO = o.readValue(jsonObject.getJSONObject("hallVO").toJSONString(),HallVO.class);
//
//            PickUpAddressInfoVO pickUpAddressInfoVO = o.readValue(jsonObject.getJSONObject("pickUpAddressInfoVO").toJSONString(),PickUpAddressInfoVO.class);

            affairRecevieService.parser(projId,matterCode,areaCode,affairType,applyOrigin,appId ,bizType,jsonObject.toJSONString());

            receiveResponse.setResultStatus(CommonConstants.ATG_SUCCESS);
            receiveResponse.setResultCode(CommonConstants.ATG_CODE_SUCCESS);
            return receiveResponse;
        } catch (Exception e) {
            e.printStackTrace();
            receiveResponse.setResultStatus(CommonConstants.ATG_FAIL);
            receiveResponse.setResultCode(CommonConstants.ATG_CODE_FAIL);
            receiveResponse.setIsMatterRoute(false);
            receiveResponse.setResultMsg(e.getMessage());
            return receiveResponse;
        }
    }


}
