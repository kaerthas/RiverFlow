package com.inspur.workinfo.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.gov.api.domain.ApplicantVO;
import com.alibaba.gov.api.domain.PickUpAddressInfoVO;
import com.inspur.workinfo.entity.XtApproveBusinessAccept;
import com.inspur.workinfo.entity.XtApproveBusinessDone;
import com.inspur.workinfo.entity.XtApproveBusinessNcorrect;

import java.util.List;

/***
 * 数浙对接接口
 * */
public interface AtgBizAffairRecevieService {

    //保存相关数据到数据库表中，包括解析表单数据 成功返回
    public void parser(String projId, String mattercode, String areaCode, String affairType, String applyOrigin, String appId, String bizType,String businessInfo)throws Exception;


    JSONObject analysisApplyData(String sxbm, String businessInfo)throws Exception;

    JSONObject sendBusinessAccept(XtApproveBusinessAccept businessAcceptBean);

    JSONObject sendBusinessFinish(XtApproveBusinessDone businessAcceptBean);

    void sendNeedCorrectionData(List<XtApproveBusinessNcorrect> businessNcorrectList, String sblshShort);

    JSONObject sendBusinessFinishNew(XtApproveBusinessAccept businessAcceptBean);
}
