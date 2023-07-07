package com.inspur.workinfo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {
	
	private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	
	
	/**
	 * 初始化一个HttpsURLConnection
	 * 
	 * @param urlStr
	 * @param requestMethod
	 *            "GET" 或者 "POST" 方法
	 * @return HttpsURLConnection
	 * @throws IOException
	 */
	public static HttpURLConnection initHttpConnection(String urlStr,
			String requestMethod) throws IOException {

		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod(requestMethod);
		conn.setRequestProperty("content-type",
				"application/x-www-form-urlencoded");

		return conn;
	}
	
	public static void writeHttpContent(HttpURLConnection conn,
			String content, String characterCode) throws IOException {
		if (content != null && conn != null) {
			OutputStream os = conn.getOutputStream();
			os.write(content.getBytes(characterCode));
			os.flush();
			os.close();
		}
	}
	
	/**
	 * 从HttpsURLConnection读取返回的内容
	 * 
	 * @param conn
	 * @param characterCode
	 *            编码： "utf-8"， "gbk" 等等
	 * @return
	 * @throws IOException
	 */
	public static String getHttpContent(HttpURLConnection conn,
			String characterCode) throws IOException {
		InputStream inputStream = conn.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream, characterCode);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		String str = null;
		StringBuffer buffer = new StringBuffer();
		while ((str = bufferedReader.readLine()) != null) {
			buffer.append(str);
		}
		// 释放资源
		bufferedReader.close();
		inputStreamReader.close();
		inputStream.close();
		conn.disconnect();

		return buffer.toString();
	}
	
	

	
	

}
