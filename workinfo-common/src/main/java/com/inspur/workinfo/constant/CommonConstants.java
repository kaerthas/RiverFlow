/*
 *
 *      Copyright (c) 2018-2025, yunho All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the yunho.io developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: yunho
 *
 */

package com.inspur.workinfo.constant;

/**
 * @author yunho
 * @date 2017/10/29
 */
public interface CommonConstants {

	/**
	 * header 中租户ID
	 */
	String TENANT_ID = "TENANT-ID";

	/**
	 * header 中版本信息
	 */
	String VERSION = "VERSION";

	/**
	 * 租户ID
	 */
	Integer TENANT_ID_1 = 1;

	/**
	 * 删除
	 */
	String STATUS_DEL = "1";

	/**
	 * 正常
	 */
	String STATUS_NORMAL = "0";

	/**
	 * 锁定
	 */
	String STATUS_LOCK = "9";

	/**
	 * 菜单树根节点
	 */
	Integer MENU_TREE_ROOT_ID = -1;

	/**
	 * 编码
	 */
	String UTF8 = "UTF-8";

	/**
	 * 前端工程名
	 */
	String FRONT_END_PROJECT = "yunho-ui";

	/**
	 * 后端工程名
	 */
	String BACK_END_PROJECT = "yunho";

	/**
	 * 公共参数
	 */
	String YUNHO_PUBLIC_PARAM_KEY = "YUNHO_PUBLIC_PARAM_KEY";

	/**
	 * 成功标记
	 */
	Integer SUCCESS = 0;

	/***
	 * 请求成功标识
	 * **/
	String  API_SUCCESS  = "200";

	/***
	 * 请求出现服务降级字样，介于成功失败的中间状态
	 * **/
	String  API_STOP  = "100";

	/***
	 * 请求成功标识
	 * **/
	String  API_FAIL  = "300";

	/**
	 * 执行状态 ：成功
	 */
	public static final String APPROVAL_SUCCESS = "1";
	/**
	 * 执行失败
	 */
	public static final String APPROVAL_ERROR = "0";
	/**
	 * 失败标记
	 */
	Integer FAIL = 1;

	/**
	 * 默认存储bucket
	 */
	String BUCKET_NAME = "yunho";

	/**
	 * 滑块验证码
	 */
	String IMAGE_CODE_TYPE = "blockPuzzle";

	String XSS_EXCLUDES = ",/role/getRoleList,/route/*,/apimanage/*";

	/**
	 * oracleDriverCLass
	 */
	String DRIVER_ORACLE = "oracle.jdbc.driver.OracleDriver";
	String SELECT_COLUMN = "column" ;
	String SELECT_CONDITION = "condition";
	String SELECT_CONSTANTS =  "constants";
	/*******
	 * 增加接口redis锁
	 * */
	String XT_BUSINESS_INFO_REDIS = "xt:businessinfo";
	String XT_BUSINESS_ASSIGN_REDIS = "xt:businessassign";
	String XT_BUSINESS_MATERIAL_REDIS = "xt:businessmaterial";

	String XT_BUSINESS_GET_ACCEPT_REDIS  = "xt:businessgetaccept";
	String XT_BUSINESS_GET_DONE_REDIS = "xt:businessgetdone";
	String XT_BUSINESS_GET_OUTCOME_REDIS = "xt:businessgetoutcome";
	String XT_BUSINESS_GET_NEED_CORRECTION_REDIS = "xt:businessgetneedcorrection";

	String XT_BUSINESS_SEND_DONE_REDIS = "xt:businesssenddone";
	String XT_BUSINESS_SEND_ACCEPT_REDIS = "xt:businesssendaccept";
	String XT_BUSINESS_SEND_OUTCOME_REDIS = "xt:businesssendoutcome";
	String XT_BUSINESS_SEND_NEED_CORRECTION_REDIS = "xt:businesssendneedcorrection";

	String XT_BUSINESS_APPROVE = "APPROVE";
	String XT_BUSINESS_ASSIGN  = "ASSIGN";
	String XT_BUSINESS_MATERIAL = "MATERIAL";
	String XT_BUSINESS_DONE    = "DONE";


	String XT_BUSINESS_GET_OUTCOME  = "GETOUTCOME";
	String XT_BUSINESS_GET_ACCEPT = "GETACCEPT";
	String XT_BUSINESS_GET_DONE    = "GETDONE";
	String XT_BUSINESS_GET_NEED_CORRECTION = "GETNEEDCORRECTION";



	String XT_BUSINESS_SEND_DONE    = "SENDDONE";
	String XT_BUSINESS_SEND_ACCEPT = "SENDACCEPT";
	String XT_BUSINESS_SEND_OUTCOME = "SENDOUTCOME";
	String XT_BUSINESS_SEND_NEED_CORRECTION ="SENDNEEDCORRECTION";

	//分发脚本入参 固定key
	String XT_BUSINESS_BASE = "businessBase"; //JSONOBJECT
	String XT_BUSINESS_XML  = "businessXML";//JSONOBJECT
	String XT_BUSINESS_FILE = "businessFile";//JSONARRARY
	String XT_BUSINESS_ITEM = "businessItem";//JSONOBJECT


	String API_TOKEN = "token";
	String API_PROXY = "proxy";
	String API_RESULT ="result";


	//API_INTPUT_INFO参数类型
	String API_INPUT_NORMAL= "NORMAL";
	String API_INPUT_CONSTANT = "CONSTANT";
	String API_INPUT_SCRIPT   = "SCRIPT";
	String API_INPUT_HEADER   = "HEADER";

}
