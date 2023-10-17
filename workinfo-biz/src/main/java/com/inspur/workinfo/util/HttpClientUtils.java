package com.inspur.workinfo.util;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

import com.inspur.workinfo.config.PropertyConfig;
import com.inspur.workinfo.constant.CommonConstants;
import com.inspur.workinfo.entity.ApproveCall;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Title: HttpClientUtils.java
 * @Package com.inspur.utils
 * @Description: TODO
 * @author KL
 * @date 2015-1-16上午10:40:56
 * @version 1.0
 */
@Slf4j
@Component
public class HttpClientUtils {

	private static PropertyConfig propertyConfig;

	@Autowired
	public HttpClientUtils(PropertyConfig myDependency) {
		HttpClientUtils.propertyConfig = myDependency;
	}

	MyX509TrustManager xtm = new MyX509TrustManager();
    MyHostnameVerifier hnv = new MyHostnameVerifier();
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
	public HttpClientUtils() {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
			X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };
			sslContext.init(null, xtmArray, new SecureRandom());
		} catch (GeneralSecurityException gse) {
		}
		if (sslContext != null) {
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
					.getSocketFactory());
		}
		HttpsURLConnection.setDefaultHostnameVerifier(hnv);
	}
	static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * POST请求
	 *
	 * @param urlStr 请求地址
	 * @param params 参数字典
	 * @param headers 编码，最好传“UTF-8”
	 * @return
	 * @exception  RuntimeException
	 */
	public  static String sendPostByHttpUrlConnection(String urlStr, Map<String, Object> params, Map<String,Object> headers) {
		StringBuffer resultBuffer = null;
		// 构建请求参数
		String sbParams= JoiningTogetherParams(params);
		HttpURLConnection con=null;
		OutputStreamWriter osw = null;
		BufferedReader br = null;
		// 发送请求
		try {

			URL url = new URL(urlStr);
			con = (HttpURLConnection) url.openConnection();

			if (MapUtil.isNotEmpty(headers)) {
				HttpURLConnection finalCon = con;
				headers.forEach((k, v) ->
                        finalCon.setRequestProperty(k, String.valueOf(v)));
			}
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			if (sbParams != null && sbParams.length() > 0) {
				osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
				osw.write(sbParams);
				osw.flush();
			}
			// 读取返回内容
			resultBuffer = new StringBuffer();
			int contentLength = Integer.parseInt(con.getHeaderField("Content-Length"));
			if (contentLength > 0) {
				br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
				String temp;
				while ((temp = br.readLine()) != null) {
					resultBuffer.append(temp);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e) {
					osw = null;
					throw new RuntimeException(e);
				} finally {
					if (con != null) {
						con.disconnect();
						con = null;
					}
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					br = null;
					throw new RuntimeException(e);
				} finally {
					if (con != null) {
						con.disconnect();
						con = null;
					}
				}
			}
		}

		return resultBuffer.toString();
	}

	public static String postWithParamsForString(String url,Map<String,Object> param, Map<String, Object> headers) {

		List<NameValuePair> params = new ArrayList<>();
		HttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		String msg = "";
		try {
			if (MapUtil.isNotEmpty(headers)){
				headers.forEach((k,v) ->
						httpPost.addHeader(k,String.valueOf(v)));
			}
			if (param != null && param.size() != 0) {
				for (Map.Entry<String, Object> entry : param.entrySet()) {
					params.add(new NameValuePair() {
						@Override
						public String getName() {
							return entry.getKey();
						}

						@Override
						public String getValue() {
							return entry.getValue().toString();
						}
					});
				}
			}

			httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
			httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
			HttpResponse response = client.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (HttpURLConnection.HTTP_OK == statusCode) {
				HttpEntity entity = response.getEntity();
				msg = EntityUtils.toString(entity);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
	}

	private static String JoiningTogetherParams(Map<String, Object> params){
		StringBuffer sbParams = new StringBuffer();
		if (params != null && params.size() > 0) {
			for (Map.Entry<String, Object> e : params.entrySet()) {
				sbParams.append(e.getKey());
				sbParams.append("=");
				sbParams.append(e.getValue());
				sbParams.append("&");
			}
			return sbParams.substring(0, sbParams.length() - 1);
		}
		return "";
	}

	public static String inputStreamToString(InputStream is, String charSet)
			throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(is,
				charSet));
		StringBuffer buffer = new StringBuffer();
		String line = "";
		while ((line = in.readLine()) != null) {
			buffer.append(line);
			buffer.append("\r\n");
		}
		in.close();
		return buffer.toString();
	}

    /**
     *   获取REST 服务结果
     * @author KL 2015-1-16 上午10:44:29
     * @param url 地址 | 可以地址传参（http://ip:port?a=1&b=2）
     * @param param 参数
     * @return
     * @throws Exception
     */
    public String getResult(String url,String param) throws Exception {
   	 return getResult(url,param,false);
    }

    /**
     * 获取REST 服务结果
     * @author KL 2015-1-16 上午10:44:43
     * @param url 地址  可以地址传参（http://ip:port?a=1&b=2）
     * @param postType Boolean 是否是POST
     * @return
     * @throws Exception
     */
    public String getResult(String url,Boolean postType) throws Exception {
   	 return getResult(url,"",postType);
    }

    /**
     *  获取REST 服务结果
     * @author KL 2015-1-16 上午10:43:32
     * @param url 请地址  | 可以地址传参（http://ip:port?a=1&b=2）
     * @param param 参数
     * @param postType  Boolean 是否是POST 方式
     * @return
     * @throws Exception
     */
    public String getResult(String url,String param,Boolean postType) throws Exception {
   	 String content = "";
		HttpURLConnection connection = null;
		OutputStreamWriter outer=null;
		try {
			URL restURL  = null;

			try {
				if((param == null) || (param.length() <= 1)){
					int firstIndex = url.indexOf("?");
					if(firstIndex>-1){
						String params = url.substring(firstIndex+1);
						String[] arrParam= params.split("&");
						StringBuffer newParams= new StringBuffer(30);
						for(int i=0;i<arrParam.length;i++){
							String[] p = arrParam[i].split("=");
							newParams.append(p[0]);
							if(p.length==2){
								newParams.append( "="+p[1]);
							}else if(p.length==1){
								newParams.append( "=" );
							}
							if(i!=arrParam.length-1){
								newParams.append("&");
							}

						}
						url = url.substring(0,firstIndex)+"?"+newParams.toString();
					}
				}else if(!postType ){
					url = url + "?" + param;
				}
				restURL  = new URL(url);
			}catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			if(restURL != null){
	 			connection = (HttpURLConnection) restURL.openConnection();
	 			connection.setConnectTimeout(1000*6);
	 			connection.setReadTimeout(1000*300);

	 			if(postType){
	 				connection.setRequestMethod("POST");
	 				connection.setDoOutput(true);
	 			}else{
	 				connection.setRequestMethod("GET");
	 			}

	 			connection.setRequestProperty("Content-Type", "text/html;charset=UTF-8");
	 			if ((param != null) && (param.length() > 1)&&postType) {
	 				 outer = new OutputStreamWriter(
	 						connection.getOutputStream(), "UTF-8");
	 				outer.write(param);
	 				outer.flush();
	 				outer.close();
	 			}
	 			connection.connect();
	 			InputStream ips = connection.getInputStream();
	 			content = inputStreamToString(ips, "UTF-8");
	 			ips.close();
	 			connection.disconnect();
			}
		} catch(RuntimeException e){
			logger.error(e.getMessage(),e);
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		finally{
			if(connection != null){
				connection.disconnect();
			}
			if(null!=outer){
				outer.close();
			}
		}
		return content;
    }

    @SuppressWarnings({ "unused", "rawtypes" })
	public String httpInvoke(String url, String methodm, String data)
			throws IOException
		{
	        Map invokeResult = null;
			StringBuilder response = new StringBuilder();
			URL httpurl = new URL(url);
			HttpURLConnection hc = (HttpURLConnection) httpurl.openConnection();
			String Method = methodm.toUpperCase();
			hc.setRequestMethod(Method);
			hc.setDoInput(true);
			if ("POST".equals(Method)) {
				hc.setDoOutput(true);
				if (data != null) {
					hc.setRequestProperty("Content-Length", String.valueOf(data
							.length()));
				}
			}
			hc.setRequestProperty("Content-Type", "application/json");
			hc.setRequestProperty("Charset", "UTF-8");

			hc.connect();
			if ("POST".equals(Method)) {
				OutputStream ops = hc.getOutputStream();
				byte[] buff;
				if (data != null) {
					buff = data.getBytes("UTF-8");
					ops.write(buff);
				}
				ops.flush();
				ops.close();
			}
			int code = hc.getResponseCode();
			if (code == 200) {
				InputStream ins = null;
				InputStreamReader isr = null;
				try {
					ins = hc.getInputStream();

					 isr = new InputStreamReader(ins, "UTF-8");
					char[] cbuf = new char[1024];
					int i = isr.read(cbuf);
					while (i > 0) {
						response.append(new String(cbuf, 0, i));
						i = isr.read(cbuf);
					}
					isr.close();
					ins.close();
				} catch(RuntimeException e){
					logger.error(e.getMessage(),e);
				}catch (Exception e) {
					logger.error(e.getMessage(),e);
				}finally{
					if(null!=isr){
						isr.close();
					}
					if(null!=ins){
						ins.close();
					}
				}

			} else {
				InputStream ins = null;
				InputStreamReader isr = null;
				try {
					ins = hc.getErrorStream();
					isr = new InputStreamReader(ins, "UTF-8");
					char[] cbuf = new char[1024];
					int i = isr.read(cbuf);
					while (i > 0) {
						response.append(new String(cbuf, 0, i));
						i = isr.read(cbuf);
					}
					isr.close();
					ins.close();
				} catch(RuntimeException e){
					logger.error(e.getMessage(),e);
				}catch (Exception e) {
					logger.error(e.getMessage(),e);
				}finally{
					if(null!=isr){
						isr.close();
					}
					if(null!=ins){
						ins.close();
					}
				}
			}
			hc.disconnect();
			return response.toString();
		}

	/**
	 * 从服务器获得文件保存本地
	 * @param remoteFilePath
	 * @param realpath
	 */
	public static File downloadFile(String remoteFilePath,String realpath) {
		URL urlfile = null;
		HttpURLConnection httpUrl = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		File f = new File(realpath);
		try {
			urlfile = new URL(remoteFilePath);
			httpUrl = (HttpURLConnection) urlfile.openConnection();
			httpUrl.connect();
			bis = new BufferedInputStream(httpUrl.getInputStream());
			bos = new BufferedOutputStream(new FileOutputStream(f));
			int len = 2048;
			byte[] b = new byte[len];
			while ((len = bis.read(b)) != -1) {
				bos.write(b, 0, len);
			}
			bos.flush();
			bis.close();
			httpUrl.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(bos!=null){
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(bis!=null){
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return f;
	}
	/**
	 * @param httpUrl
	 *            :请求接口
	 * @param httpArg
	 *            :参数
	 * @return 返回结果
	 */
	public String requestSY(String httpUrl, String httpArg,String appKey) {
	    BufferedReader reader = null;
	    String result = null;
	    StringBuffer sbf = new StringBuffer();
	    httpUrl = httpUrl + "?" + httpArg;

	    try {
	        URL url = new URL(httpUrl);
	        HttpURLConnection connection = (HttpURLConnection) url
	                .openConnection();
	        // 填入api的请求方法
	        connection.setRequestMethod("GET");
	        // 填入appkey到HTTP header
	        connection.setRequestProperty("AppKey", appKey);
	        connection.connect();
	        InputStream is = connection.getInputStream();
	        reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	        String strRead = null;
	        while ((strRead = reader.readLine()) != null) {
	            sbf.append(strRead);
	            sbf.append("\r\n");
	        }
	        reader.close();
	        result = sbf.toString();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	/**
	 * post请求,form请求参数
	 *
	 * @param path
	 * @param params 1550728928902
	 *               1550728928902
	 */
	public static String sendFormPost(String path, Map<String, Object> params) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String responseContent = null;
		try {
			HttpPost method = new HttpPost(path);
			List<NameValuePair> pairs = covertParams(params);
			method.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
			CloseableHttpResponse httpResponse = httpclient.execute(method);
			responseContent = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseContent;
	}

	/**
	 * Title:参数转换
	 */
	public static List<NameValuePair> covertParams(Map<String, Object> params) {
		List<NameValuePair> list = new ArrayList<>();
		if (params != null && params.size() > 0) {
			for (Map.Entry<String, Object> param : params.entrySet()) {
				list.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
			}
		}
		return list;
	}


	/**
	*
	* 描述：
	* 2015-2-6 上午9:02:07 zhaopp
	* @param error
	* @return
	*/
	private JSONObject error(String error){
	   JSONObject resultJson=new JSONObject();
	   resultJson.put("state", CommonConstants.API_SUCCESS);
	   resultJson.put("error", error);
	   return resultJson;
	}

	public static String getRandomString(int length){
		String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random random=new Random();
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<length;i++){
			int number=random.nextInt(52);
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}
	/**
	 * HttpPost application/json
	 */
	public String sendJsonHttpPost(String url, String json) {
		return this.sendApplicationJsonWithHeader(url, json, null);
	}

	/**
	 * HttpPost application/json addHeader自定义 超时时间自定义
	 */
	public static String sendApplicationJsonWithHeader(String url, String json, Map<String, String> headers) {
		HttpPost httpPost = new HttpPost(url);
		/** 添加请求头 */
		if (MapUtil.isNotEmpty(headers)){
			headers.forEach((k,v) -> httpPost.addHeader(k,v));
		}
		httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
		ContentType contentType = null;
		String responseInfo="";
		try {
			contentType = ContentType.create("application/json", CharsetUtils.get("UTF-8"));


			CloseableHttpClient httpclient = HttpClients.createDefault();
			//默认值 300秒
//			String readTimeout=GlobalConfig.getInstance().getConfig("app.read.timeout","300");
//			String connectionTimeout=GlobalConfig.getInstance().getConfig("app.connection.timeout", "300");
			RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(Integer.parseInt("300") * 1000)
					.setConnectTimeout(Integer.parseInt("300") * 1000)
					.setConnectionRequestTimeout(Integer.parseInt("5") * 1000).build();//设置请求和传输超时时间
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(new StringEntity(json, contentType));
			CloseableHttpResponse response = null;

			response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				if (null != entity) {
					responseInfo = EntityUtils.toString(entity,"utf-8");
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return responseInfo;
	}


	public static String sendGetWithHeader(String url,Map<String, Object> params,Map<String, Object> headers,boolean isInternet){
		String responseInfo = "";
		try {
			URL urlString = new URL(url);
			String ip = urlString.getHost(); // 获取IP地址
			int port = urlString.getPort(); // 获取端口号
			CloseableHttpClient httpClient = HttpClients.createDefault();
			if (MapUtil.isNotEmpty(params)) {
				url = url + "?" + JoiningTogetherParams(params);
			}
			HttpGet httpGet = new HttpGet(url);
			RequestConfig requestConfig = null;
			CloseableHttpResponse response =null;

			if (MapUtil.isNotEmpty(headers)) {
				headers.forEach((k, v) -> httpGet.addHeader(k, (String)v));
			}

			if (url.contains("https")) {
				//增加正向代理先转发到代理服务器
				if (isInternet) {
					HttpHost target = new HttpHost(ip, port,
							"https");
					logger.error("使用代理模式,host:" + propertyConfig.getHttpProxyIP() +",port:"+ propertyConfig.getHttpPort());

					requestConfig = RequestConfig.custom().setSocketTimeout(5000)
							.setConnectTimeout(5000)
							.setConnectionRequestTimeout(5000).setProxy(new HttpHost(propertyConfig.getHttpProxyIP(),Integer.valueOf(propertyConfig.getHttpPort()),"HTTPS")).build();//设置请求和传输超时时间
//					httpGet.setConfig(requestConfig);
//
//					SSLClient client = new SSLClient();
//					response = client.execute(target,httpGet);
				}else{
					requestConfig = RequestConfig.custom().setSocketTimeout(3000)
							.setConnectTimeout(3000)
							.setConnectionRequestTimeout(3000).build();//设置请求和传输超时时间



				}
				httpGet.setConfig(requestConfig);
				SSLClient client = new SSLClient();
				response = client.execute(httpGet);

			}else{

				if (isInternet) {
					HttpHost target = new HttpHost(ip, port,
							"http");
					logger.error("使用代理模式,host:" + propertyConfig.getHttpProxyIP() +",port:"+ propertyConfig.getHttpPort());

					requestConfig = RequestConfig.custom().setSocketTimeout(5000)
							.setConnectTimeout(5000)
							.setConnectionRequestTimeout(5000).setProxy(new HttpHost(propertyConfig.getHttpProxyIP(),Integer.valueOf(propertyConfig.getHttpPort()),"HTTP")).build();//设置请求和传输超时时间
					httpGet.setConfig(requestConfig);

					SSLClient client = new SSLClient();
					response = client.execute(target,httpGet);
				}else{
					requestConfig = RequestConfig.custom().setSocketTimeout(3000)
							.setConnectTimeout(3000)
							.setConnectionRequestTimeout(3000).build();//设置请求和传输超时时间
					httpGet.setConfig(requestConfig);

					response = httpClient.execute(httpGet);
				}

			}



			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				responseInfo = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} catch (Exception e) {
			try {
				logger.error(new String(e.getMessage().getBytes("GB2312"),"UTF-8"),e);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		return responseInfo;
	}


	public static String sendPostByHttpURLConnection(String url,String params,Map<String, Object> headers,boolean isInternet)  {
		String result = "";
		BufferedReader reader = null;
		HttpURLConnection conn = null;
		try {

			URL urlPath = new URL(url);
			if (isInternet) {
				String proxyIP = propertyConfig.getHttpProxyIP();
				int proxyPort = Integer.valueOf(propertyConfig.getHttpPort());
				logger.error("使用代理模式,host:" + proxyIP + ",port:" + proxyPort);
				Proxy.Type var10002 = Proxy.Type.DIRECT;
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIP, proxyPort));
				if (url.contains("https")) {
					HttpsURLConnection httpsConn = (HttpsURLConnection)urlPath.openConnection(proxy);
					httpsConn.setHostnameVerifier(DO_NOT_VERIFY);
					conn = httpsConn;
				} else {
					conn = (HttpURLConnection)urlPath.openConnection(proxy);
				}
			} else if (url.substring(0, 5).equals("https")) {
				HttpsURLConnection httpsConn = (HttpsURLConnection)urlPath.openConnection();
				httpsConn.setHostnameVerifier(DO_NOT_VERIFY);
				conn = httpsConn;
			} else {
				conn = (HttpURLConnection)urlPath.openConnection();
			}
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Charset", "UTF-8");
			// 设置文件类型:
			conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");

			/** 添加请求头 */
			if (MapUtil.isNotEmpty(headers)){
				HttpURLConnection finalConn = conn;
				headers.forEach((k, v) ->
						finalConn.setRequestProperty(k,String.valueOf(v)));
			}

			// 设置接收类型否则返回415错误
			//conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
			//conn.setRequestProperty("accept","application/json");
			// 往服务器里面发送数据
			if (params != null && !StrUtil.isEmpty(params)) {
				byte[] writebytes = params.getBytes();
				// 设置文件长度
				conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
				OutputStream outwritestream = conn.getOutputStream();
				outwritestream.write(params.getBytes());
				outwritestream.flush();
				outwritestream.close();
				logger.error("hlhupload", "doJsonPost: conn"+conn.getResponseCode());
			}
			if (conn.getResponseCode() == 200) {
				reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), "UTF-8"));
				result = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}



	public static String sendPostWithHeader(String url,String params,Map<String, Object> headers,boolean isInternet)  {
		String responseInfo="";
		try {
			URL urlString = new URL(url);
			String ip = urlString.getHost(); // 获取IP地址
			int port = urlString.getPort(); // 获取端口号

//			Proxy proxy = new Proxy(Proxy.Type.DIRECT.HTTP,new InetSocketAddress(httpProxyIP, Integer.valueOf(httpPort)));

			HttpPost httpPost = new HttpPost(url);
			/** 添加请求头 */
			if (MapUtil.isNotEmpty(headers)){
				headers.forEach((k,v) ->
						httpPost.addHeader(k,String.valueOf(v)));
			}
			httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
			ContentType contentType = null;
			RequestConfig requestConfig=null;

			contentType = ContentType.create("application/json", CharsetUtils.get("UTF-8"));
			CloseableHttpClient httpclient = HttpClients.createDefault();


			httpPost.setEntity(new StringEntity(params, contentType));
			CloseableHttpResponse response = null;

			if (url.contains("https")) {
				//增加正向代理先转发到代理服务器
				if (isInternet) {
					logger.error("使用者url:"+ip+port+",使用者url:"+url+"使用代理模式,host:" + propertyConfig.getHttpProxyIP() +",port:"+ propertyConfig.getHttpPort());

//					HttpHost target = new HttpHost(ip, port,
//							"https");

					requestConfig = RequestConfig.custom().setSocketTimeout(30000)
							.setConnectTimeout(5000)
							.setConnectionRequestTimeout(5000)
							.setProxy(new HttpHost(propertyConfig.getHttpProxyIP(),Integer.valueOf(propertyConfig.getHttpPort()),"HTTPS"))
							.build();//设置请求和传输超时时间

//					httpPost.setConfig(requestConfig);
//
//					SSLClient client = new SSLClient();
//					response = client.execute(target,httpPost);

				}else{
					requestConfig = RequestConfig.custom().setSocketTimeout(3000)
							.setConnectTimeout(3000)
							.setConnectionRequestTimeout(3000).build();//设置请求和传输超时时间

				}
				httpPost.setConfig(requestConfig);
				SSLClient client = new SSLClient();
				response = client.execute(httpPost);

			}else{

				if (isInternet) {
//					HttpHost target = new HttpHost(ip, port,
//							"http");
					logger.error("使用代理模式,host:" + propertyConfig.getHttpProxyIP() +",port:"+ propertyConfig.getHttpPort());

					requestConfig = RequestConfig.custom().setSocketTimeout(5000)
							.setConnectTimeout(5000)
							.setConnectionRequestTimeout(5000).setProxy(new HttpHost(propertyConfig.getHttpProxyIP(),Integer.valueOf(propertyConfig.getHttpPort()),"HTTP")).build();//设置请求和传输超时时间

				}else{

					 requestConfig = RequestConfig.custom().setSocketTimeout(3000)
							.setConnectTimeout(3000)
							.setConnectionRequestTimeout(3000).build();//设置请求和传输超时时间
				}
				httpPost.setConfig(requestConfig);
				response = httpclient.execute(httpPost);


			}
			HttpEntity entity = response.getEntity();
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				if (null != entity) {
					responseInfo = EntityUtils.toString(entity,"utf-8");
				}
			}
		}catch (Exception e) {
			try {
				logger.error(new String(e.getMessage().getBytes("GBK"),"UTF-8"),e);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		return responseInfo;
	}

	/**
	 * post请求,form请求参数
	 *
	 */
	public static String sendFormPostWithHeader(String url, Map<String, Object> params,Map<String, Object> headers,boolean isInternet) {


		CloseableHttpClient httpclient = HttpClients.createDefault();
		String responseContent = null;
		try {
			URL urlString = new URL(url);
			String ip = urlString.getHost(); // 获取IP地址
			int port = urlString.getPort(); // 获取端口号

			HttpPost httpPost = new HttpPost(url);
			if (MapUtil.isNotEmpty(headers)){
				headers.forEach((k,v) -> httpPost.addHeader(k,String.valueOf(v)));
			}
			List<NameValuePair> pairs = covertParams(params);
			httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
			RequestConfig requestConfig = null;
			CloseableHttpResponse response = null;

			if (url.contains("https")) {
				//增加正向代理先转发到代理服务器
				if (isInternet) {

					HttpHost target = new HttpHost(ip, port,
							"https");
					logger.error("使用代理模式,host:" + propertyConfig.getHttpProxyIP() +",port:"+ propertyConfig.getHttpPort());
					requestConfig = RequestConfig.custom().setSocketTimeout(10000)
							.setConnectTimeout(10000)
							.setConnectionRequestTimeout(10000).setProxy(new HttpHost(propertyConfig.getHttpProxyIP(),Integer.valueOf(propertyConfig.getHttpPort()),"HTTPS")).build();//设置请求和传输超时时间
					httpPost.setConfig(requestConfig);
					SSLClient client = new SSLClient();
					response = client.execute(target,httpPost);
				}else{
					requestConfig = RequestConfig.custom().setSocketTimeout(10000)
							.setConnectTimeout(10000)
							.setConnectionRequestTimeout(10000).build();//设置请求和传输超时时间
					httpPost.setConfig(requestConfig);
					SSLClient client = new SSLClient();
					response = client.execute(httpPost);
				}



			}else{

				if (isInternet) {

					HttpHost target = new HttpHost(ip, port,
							"http");
					logger.error("使用代理模式,host:" + propertyConfig.getHttpProxyIP() +",port:"+ propertyConfig.getHttpPort());

					requestConfig = RequestConfig.custom().setSocketTimeout(10000)
							.setConnectTimeout(10000)
							.setConnectionRequestTimeout(10000).setProxy(new HttpHost(propertyConfig.getHttpProxyIP(),Integer.valueOf(propertyConfig.getHttpPort()),"HTTP")).build();//设置请求和传输超时时间
					httpPost.setConfig(requestConfig);
					response = httpclient.execute(target,httpPost);
				}else{

					requestConfig = RequestConfig.custom().setSocketTimeout(-1)
							.setConnectTimeout(-1)
							.setConnectionRequestTimeout(-1).build();//设置请求和传输超时时间
					httpPost.setConfig(requestConfig);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					logger.error("######################中残推送交换时间开始"+sdf.format(new Date()));
					logger.error("######################中残推送交换时间开始"+System.currentTimeMillis());

					response = httpclient.execute(httpPost);
					logger.error("######################中残推送交换时间结束"+sdf.format(new Date()));
					logger.error("######################中残推送交换时间开始"+System.currentTimeMillis());

				}
			}
			HttpEntity entity = response.getEntity();
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				if (null != entity) {
					responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		log.info("中国残联推送结果返回值{}"+responseContent);
		return responseContent;
	}

	public static String postXmlRequest(String url, String xml , Map<String, Object> headers,boolean isInternet) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String responseContent = null;
		try {

			URL urlString = new URL(url);
			String ip = urlString.getHost(); // 获取IP地址
			int port = urlString.getPort(); // 获取端口号
			HttpPost post = new HttpPost(url);
			if (MapUtil.isNotEmpty(headers)){
				headers.forEach((k,v) -> post.addHeader(k,(String)v));
			}
			RequestConfig requestConfig = null;
//			RequestConfig.custom().setSocketTimeout(Integer.parseInt("300") * 1000)
//					.setConnectTimeout(Integer.parseInt("300") * 1000).build();//设置请求和传输超时时间
//			post.setConfig(requestConfig);
//
			post.setHeader("Content-type", "text/xml");
			post.setEntity(new StringEntity(xml, "UTF-8"));
			CloseableHttpResponse response = null;
//			if (url.contains("https")) {
//				SSLClient client = new SSLClient();
//				response = client.execute(post);
//			}else{
//				response = httpclient.execute(post);
//			}
			if (url.contains("https")) {
				//增加正向代理先转发到代理服务器
				if (isInternet) {
					HttpHost target = new HttpHost(ip, port,
							"https");

					logger.error("使用代理模式,host:" + propertyConfig.getHttpProxyIP() +",port:"+ propertyConfig.getHttpPort());

					requestConfig = RequestConfig.custom().setSocketTimeout(5000)
							.setConnectTimeout(5000)
							.setConnectionRequestTimeout(5000)
							.setProxy(new HttpHost(propertyConfig.getHttpProxyIP(),Integer.valueOf(propertyConfig.getHttpPort()),"HTTPS")).build();//设置请求和传输超时时间
					post.setConfig(requestConfig);

					SSLClient client = new SSLClient();
					response = client.execute(target,post);
				}else{
					requestConfig = RequestConfig.custom().setSocketTimeout(3000)
							.setConnectTimeout(3000)
							.setConnectionRequestTimeout(3000).build();//设置请求和传输超时时间
					post.setConfig(requestConfig);

					SSLClient client = new SSLClient();
					response = client.execute(post);
				}

			}else{

				if (isInternet) {
					HttpHost target = new HttpHost(ip, port,
							"http");
					logger.error("使用代理模式,host:" + propertyConfig.getHttpProxyIP() +",port:"+ propertyConfig.getHttpPort());

					requestConfig = RequestConfig.custom().setSocketTimeout(5000)
							.setConnectTimeout(5000)
							.setConnectionRequestTimeout(5000).setProxy(new HttpHost(propertyConfig.getHttpProxyIP(),Integer.valueOf(propertyConfig.getHttpPort()),"HTTP")).build();//设置请求和传输超时时间
					post.setConfig(requestConfig);
					response = httpclient.execute(target,post);
				}else{

					requestConfig = RequestConfig.custom().setSocketTimeout(3000)
							.setConnectTimeout(3000)
							.setConnectionRequestTimeout(3000).build();//设置请求和传输超时时间
					post.setConfig(requestConfig);
					response = httpclient.execute(post);
				}


			}
			HttpEntity entity = response.getEntity();
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				if (null != entity) {
					responseContent = EntityUtils.toString(response.getEntity(), "UTF-8");
				}
			}
		}catch (Exception e){
			logger.error(e.getMessage(),e);
		}
		return responseContent;
	}

}


class MyX509TrustManager implements X509TrustManager{
  public MyX509TrustManager(){}
  public void checkClientTrusted(X509Certificate[] chain,   String authType) {}
     public void checkServerTrusted(X509Certificate[] chain,String authType) {
      //System.out.println("cert: " + chain[0].toString() + ", authType: " + authType);
     }
     public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
     }
}


class MyHostnameVerifier implements HostnameVerifier{
public MyHostnameVerifier(){}
  public boolean verify(String hostname,SSLSession session) {
   //System.out.println("hostname: " + hostname);
   return true;
  }
}

