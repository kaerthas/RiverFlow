package com.inspur.workinfo.util;
/**
 * 文件上传公用类
 * @author wangwei
 * @date 2014-10-28
 * @version 1.0
 * @description 
 */

import cn.hutool.core.util.StrUtil;
import com.inspur.workinfo.config.PropertyConfig;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

//	public static void main(String[] args) {
//		try {
//			String lo = UploadUtil.getBase64ByFilePath("http://59.218.251.19:8680/WebDiskServerDemo/doc?doc_id=2b1bb49c-b1f8-4391-9177-3591c5636e74&appCode=INSPUR-DZZW-QYSL&token=354c7446466e454c7835424677754d654c7635357278657177414a2b396f6c683878537250515963684b656f306b4470582b50576c69526d4774306d752f4747423938553770576d627342460a6c65b7f71bd3f1d46eab62c5");
//			System.out.println(lo);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public static String getBase64ByFilePath(String filePath){
		String base64 ="";
		try {
			URL url = new URL(filePath);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream inputStream = conn.getInputStream(); // 通过输入流获得文档数据
			byte[] getData = readInputStream(inputStream); // 获得文档的二进制数据
			 base64= Base64.getEncoder().encodeToString(getData);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
		return base64;
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


