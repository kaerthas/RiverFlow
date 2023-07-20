package com.inspur.workinfo.util;
/**
 * 文件上传公用类
 * @author wangwei
 * @date 2014-10-28
 * @version 1.0
 * @description 
 */

import com.inspur.workinfo.config.PropertyConfig;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


@Slf4j
public class UploadUtil {
	@Autowired
	PropertyConfig propertyConfig;

	public UploadUtil() {

	}


	public static String getBase64ByFilePath(String filePath,String downloadUrl,String appCode,String decryptKey) throws Exception {
		StringBuilder params = new StringBuilder();
		params.append("&appCode=")
				.append(appCode)
				.append("&token=")
				.append(UploadUtil.generateCheckToken(filePath,decryptKey));
		log.error("downloadUrl:" + downloadUrl + "?doc_id=" + filePath + params);
		URL url = new URL(downloadUrl + "?doc_id=" + filePath + params);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		InputStream inputStream = conn.getInputStream(); // 通过输入流获得文档数据
		byte[] getData = readInputStream(inputStream); // 获得文档的二进制数据
		String base64 = Base64.getEncoder().encodeToString(getData);
		if(StringUtils.isEmpty(base64)){
			return base64;
		}else {
			return "";
		}
	}


	public static String getDownLoadFilePath(String filePath,String downloadUrl,String appCode,String decryptKey) throws Exception {
		StringBuilder params = new StringBuilder();
		params.append("&appCode=")
				.append(appCode)
				.append("&token=")
				.append(UploadUtil.generateCheckToken(filePath, decryptKey));
		String url = downloadUrl + "?doc_id=" + filePath + params;
		return url;
	}

	public static byte[] readInputStream(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos.toByteArray();
	}

	/**
	 * 生成网盘下载token
	 */
	public static String generateCheckToken(String docId,String decryptKey) throws Exception {
		final String content = docId + ":" + (System.currentTimeMillis() + 3 * 60 * 60 * 1000);
		String token = AesEncryptUtil.aesEncrypt(content,decryptKey);
		String decryptToken=AesEncryptUtil.encrypt(token);
		return decryptToken;
	}



}


