package com.inspur.workinfo.api;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.*;
import com.inspur.workinfo.service.*;

import com.inspur.workinfo.service.impl.XtApproveBusinessCourseServiceImpl;
import com.inspur.workinfo.util.DateUtils;
import com.inspur.workinfo.util.HttpUtil;
import com.inspur.workinfo.util.R;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 针对业务对接新标准api 接口
 *
 * @author 对接协同调度中心配置相关表结构
 *
 */
@RestController
@Api(value = "osi", tags = "协同调度系统管理")
@RequestMapping("/___osi/___ddpt/ddpt")
@Slf4j
public class DispatchController{

	private Logger logger  = LoggerFactory.getLogger(DispatchController.class);
//	@Autowired
//	private DispatchService dispatchService;
	@Autowired
	private  ApproveCallService callService; //接口调用记录表

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
	PropertyConfig propertyConfig;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/ddpt61001",method = RequestMethod.POST)
	public JSONObject ddpt61001(HttpServletRequest request, HttpServletResponse response ){
		ApproveCall callBean=null;
		ApproveCallResult callResultBean=null;
		XtApproveBusinessinfo xtApproveBusinessinfo = new XtApproveBusinessinfo();
		JSONObject result = new JSONObject();
		result.put("C-Response-Desc", "success");
		result.put("C-API-Status", "00");
		result.put("C-Response-Code", "000000000000");
		//读取应用名称
//		String appName= propertyConfig.get;
		try{
			BufferedReader br=new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
			StringBuffer str=new StringBuffer("");
			String tempP;
			while ((tempP=br.readLine())!=null){
				str.append(tempP);
			}
			br.close();

			if(logger.isInfoEnabled()) {
				logger.info("str:"+str);
			}
			if (StrUtil.isBlank(str.toString())) {
				throw new Exception("参数不能为空！");
			}
			JSONObject paramJson= JSONObject.parseObject(str.toString());
			String url = request.getRequestURL().toString();
			String param="body="+str.toString();
//			callBean=callService.createCallBean("", url, param,"协同调度中心", "POST", "ddpt61001");
//			callResultBean=callService.createCallResultBean(callBean,"浪潮政务服务转接平台");

			JSONObject txnBodyComJson = paramJson.getJSONObject("txnBodyCom");
			String receiveNumber = txnBodyComJson.getString("sblshShort");//业务流水号
			String sxbm  = txnBodyComJson.getString("sxbm"); //事项编码
			String expressType  = txnBodyComJson.getString("expressType"); //0：业务受理  1：补齐补正  9:审批退回后修改再次提交
			String channelCode  = txnBodyComJson.getString("channelCode");
			String xzqhdm   = txnBodyComJson.getString("xzqhdm");
//			callBean.setBsnum(receiveNumber);
			//根据业务受理编码获取业务申办，业务预受理，业务受理信息
			//1.调用调度接口获取系统token
			if("0".equals(expressType)||"3".equals(expressType)) {//0:业务受理；3：收件后待受理业务；
				XtApproveBusinessinfo  businessinfoOld=xtApproveBusinessinfoService.getOne(new QueryWrapper<XtApproveBusinessinfo>()
						.eq("SBLSH_SHORT",receiveNumber));
				if (businessinfoOld!=null){
					throw new Exception("当前业务已成功推送到审批，无需多次推送该业务！");
				}else{
					xtApproveBusinessinfo.setSeqId(UUID.randomUUID().toString());
					xtApproveBusinessinfo.setIsUsed("0");
					xtApproveBusinessinfo.setSblshShort(receiveNumber);
					xtApproveBusinessinfo.setChannelCode(channelCode);
					xtApproveBusinessinfo.setExpressType(expressType);
					xtApproveBusinessinfo.setSxbm(sxbm);
					xtApproveBusinessinfo.setXzqhdm(xzqhdm);
					xtApproveBusinessinfoService.save(xtApproveBusinessinfo);
				}
				//查询关联关系表
				List<XtApproveItemItemflow> itemItemflows =  itemItemflowService.getBaseMapper()
						.selectList(new QueryWrapper<XtApproveItemItemflow>()
								.eq("ITEM_SXBM",sxbm));
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
					courseBean.setSblshShort(receiveNumber);
					courseService.save(courseBean);//保存过程信息
				}else{
					throw new Exception("流程关联关系配置错误，请联系管理员！");
				}

			}else{
				//空方法
				throw new Exception("当前业务ExpressType不是0或3！");
			}
			return result;
		}catch (Exception e){
			e.printStackTrace();
			if (xtApproveBusinessinfo!=null&&StrUtil.isNotBlank(xtApproveBusinessinfo.getSeqId())) {
				xtApproveBusinessinfoService.removeById(xtApproveBusinessinfo.getSeqId());
			}
			JSONObject jsonObject =new JSONObject();
			jsonObject.put("msg",e.getMessage());
			result.put("C-Response-Desc", "fail");
			result.put("C-API-Status", "01");
			result.put("C-Response-Code", "000000000000");
			result.put("C-Response-Body", JSON.toJSON(jsonObject));
			return result;
		}

	}


	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/ddpt61006",method = RequestMethod.POST)
	public JSONObject ddpt61006(HttpServletRequest request, HttpServletResponse response ){
		ApproveCall callBean=null;
		ApproveCallResult callResultBean=null;
		JSONObject result = new JSONObject();
		result.put("C-Response-Desc", "success");
		result.put("C-API-Status", "00");
		result.put("C-Response-Code", "000000000000");
		//读取应用名称
		try{
			BufferedReader br=new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
			StringBuffer str=new StringBuffer("");
			String temp;
			while ((temp=br.readLine())!=null){
				str.append(temp);
			}
			br.close();

			if(logger.isInfoEnabled()) {
				logger.info("str:"+str);
			}
			if (StrUtil.isBlank(str.toString())) {
				throw new Exception("参数不能为空！");
			}
			JSONObject paramJson=JSONObject.parseObject(str.toString());
			String url = request.getRequestURL().toString();
			String param="body="+str.toString();
			callBean=callService.createCallBean("", url, param,"协同调度中心", "POST", "ddpt61006");
			callResultBean=approveCallResultService.createCallResultBean(callBean,"浪潮政务服务通用审批平台");

			JSONObject txnBodyComJson = paramJson.getJSONObject("txnBodyCom");
			String receiveNumber = txnBodyComJson.getString("sblshShort");//业务流水号
			String itemCode  = txnBodyComJson.getString("sxbm"); //事项编码
			//创建主表信息
			XtApproveBusinessCorrection businessCorrection = new XtApproveBusinessCorrection();
			String seqId  = UUID.randomUUID().toString();
			businessCorrection.setSeqId(seqId);
			businessCorrection.setOpinion(txnBodyComJson.getString("opinion"));
			businessCorrection.setSblshShort(receiveNumber);
			businessCorrection.setSxbm(itemCode);
			businessCorrection.setUserCode(txnBodyComJson.getString("usercode"));
			businessCorrection.setUserName(txnBodyComJson.getString("username"));
			businessCorrection.setEndTime(DateUtils.formatDate("yyyy-MM-dd HH:mm:ss",txnBodyComJson.getString("endtime")));
			businessCorrection.setOrgCode(txnBodyComJson.getString("orgcode"));
			businessCorrection.setOrgName(txnBodyComJson.getString("orgname"));
			businessCorrectionService.saveOrUpdate(businessCorrection);
			//保存材料表
			List<XtApproveBusinessCorrectM> businessCorrectMList  =new ArrayList<>();

			JSONArray correctionMaterial = txnBodyComJson.getJSONArray("correctionmaterial");
			if (correctionMaterial!=null&&correctionMaterial.size()>0){
				for (int i = 0; i < correctionMaterial.size(); i++) {
					XtApproveBusinessCorrectM businessCorrectM = new XtApproveBusinessCorrectM();
					JSONObject correctobj  = correctionMaterial.getJSONObject(i);
					businessCorrectM.setSeqId(correctobj.getString("seq"));
					businessCorrectM.setAttachId(correctobj.getString("attachId"));
					businessCorrectM.setAttachBody(correctobj.getString("attachBody"));
					businessCorrectM.setAttachName(correctobj.getString("attachName"));
					businessCorrectM.setAttachPath(correctobj.getString("attachPath"));
					businessCorrectM.setStuffSeq(correctobj.getString("stuffSeq"));
					businessCorrectM.setWjlx(correctobj.getString("wjlx"));
					businessCorrectM.setRemark(correctobj.getString("remark"));
					businessCorrectM.setClsl(correctobj.getString("clsl"));
					businessCorrectM.setClmc(correctobj.getString("clmc"));
					businessCorrectM.setCllx(correctobj.getString("cllx"));

					businessCorrectM.setCorrectId(seqId);

					businessCorrectMList.add(businessCorrectM);
				}
			}
			businessCorrectMService.saveBatch(businessCorrectMList);

			callBean.setBsnum(receiveNumber);
			JSONObject resultBody = new JSONObject();
			resultBody.put("code", "00");
			resultBody.put("msg", "通办办件提交成功");
			result.put("C-Response-Body", resultBody.toString());
			if(null!=callBean && null!=callResultBean){
				callBean.setCallState(CommonConstants.API_SUCCESS);
				callResultBean.setResultValue(result.toString());
				callResultBean.setCallState(CommonConstants.API_SUCCESS);
				callService.save(callBean);
				approveCallResultService.save(callResultBean);
			}
			return resultBody;
		}catch(Exception e){
			JSONObject resultBody = new JSONObject();
			resultBody.put("code", "11");
			resultBody.put("msg", e.getMessage());
			logger.error("接受业务受理数据失败！"+e.getMessage(), e);
			result.put("C-Response-Body", resultBody.toString());
			if(null!=callBean && null!=callResultBean){
				callBean.setCallState(CommonConstants.API_FAIL);
				callResultBean.setResultValue(result.toString());
				callResultBean.setCallState(CommonConstants.API_FAIL);
				callService.save(callBean);
				approveCallResultService.save(callResultBean);
			}
			return resultBody;
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/jaspyt/test",method = RequestMethod.POST)
	public R test(@RequestParam("word") String word){

	String next = "https://zwfw.shaanxi.gov.cn/sx/icity/chain/qa/index?regionCode=610102000000&modelCode=A0008&loginSuccess=1&code=506b43ab030344eb8b4f611bbf63f063&sysType=0&loginType=0&client_id=000000001";
String redirect_uri  ="xxx";
	//log.info("登录回调方法login接收到的参数：renew："+renew + "  || flag:"+flag
	//		+ "  || next:"+next + "  || loginSuccess:"+loginSuccess + "  || code:"+code);

	URLEncodeUtils.encodeURL(next);
	String checkurl = "";
		try {
		checkurl = new URL(next).toString();
	}
		catch (MalformedURLException e) {
		checkurl = URLEncodeUtils.decodeURL(next);
	}








		String renewurl = "http://sssss:8080/" + "/sysauthserver/authorize?hide_login=true&client_id=" + "000000001"
				+ "&response_type=code&redirect_uri=";
		renewurl += URLEncodeUtils.encodeURL(redirect_uri + "?goto=" + URLEncodeUtils.encodeURL(next));
		//log.info("登录回调方法login,进入loginSuccess=1的判断中，重定向地址："+renewurl);
		return R.ok(renewurl);

	}



	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/jaspyt/encrypt",method = RequestMethod.POST)
	public R encrypt(@RequestParam("word") String word){

		//读取应用名称
		try{
			if (StrUtil.isNotBlank(word)){
				JSONObject jsonObject  = new JSONObject();
				jsonObject.put("encEncrypt",stringEncryptor.encrypt(word));
				PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
				encryptor.setConfig(cryptor("yunhom"));
				jsonObject.put("normalEncrypt",encryptor.encrypt(word));


				return R.ok(jsonObject);
			}else{
				return R.failed("输入参数不能空！");
			}


		}catch(Exception e){
			e.printStackTrace();
			return R.failed("加密失败！！");
		}
	}

	/**
	 * 配置,对应yml中的配置
	 * @param password 盐值
	 * @return SimpleStringPBEConfig
	 */
	public  SimpleStringPBEConfig cryptor(String password){
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		//设置盐值
		config.setPassword(password);
		//设置算法
		config.setAlgorithm("PBEWithMD5AndDES");
		config.setKeyObtentionIterations("1000");
		config.setPoolSize("1");
		config.setProviderName("SunJCE");
		//  config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
		config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
		config.setStringOutputType("base64");
		return config;
	}

}
