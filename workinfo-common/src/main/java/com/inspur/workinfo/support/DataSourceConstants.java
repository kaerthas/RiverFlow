package com.inspur.workinfo.support;

/**
 * @author yunho
 * @date 2019-04-01
 * <p>
 * 数据源相关常量
 */
public interface DataSourceConstants {

	/**
	 * 数据源名称
	 */
	String DS_NAME = "name";

	/**
	 * 默认数据源（master）
	 */
	String DS_MASTER = "master";

	/**
	 * jdbcurl
	 */
	String DS_JDBC_URL = "url";

	/**
	 * 用户名
	 */
	String DS_USER_NAME = "USER_NAME";

	/**
	 * 密码
	 */
	String DS_USER_PWD = "password";

	/**
	 * 默认驱动名称
	 */
	String DS_DRIVER = "com.mysql.cj.jdbc.Driver";
}
