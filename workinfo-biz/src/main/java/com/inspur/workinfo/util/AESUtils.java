package com.inspur.workinfo.util;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class AESUtils {
    private static final String CipherMode = "AES/CBC/PKCS5Padding";
    private static final String refreshappsecret_url = "http://59.218.251.18/sysapi/refreshappsecret";


    // /** 解密字节数组 **/
    public static byte[] decrypt(byte[] content, String password, String iv) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.DECRYPT_MODE, key, createIV(iv));
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    // /** 解密 **/
    public static JSONObject decrypt(String content, String password, String iv) {
        JSONObject result = new JSONObject();
        result.put("isPass", false);
        byte[] data = null;

        try {
            data = new Base64().decode(content);// 先用base64解密
            data = decrypt(data, password, iv);
            if (null == data) {
                result.put("isPass", false);
                return result;
            }
            String results = null;
            results = new String(data, "UTF-8");
            result.put("isPass", true);
            result.put("datas", results);
        } catch (Exception e) {
            result.put("isPass", false);
        }
        return result;
    }

    /**
     * 获取主键
     *
     * @return
     */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replaceAll("-", "");
        return uuid;
    }

    /**
     * 随机生成秘钥
     */
    public static String getKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128);
            //要生成多少位，只需要修改这里即可128, 192或256
            SecretKey sk = kg.generateKey();
            byte[] b = sk.getEncoded();
            return byteToHexString(b);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 使用指定的字符串生成秘钥
     */
    public static String getKeyByPass(String password) {
        //生成秘钥
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            // kg.init(128);//要生成多少位，只需要修改这里即可128, 192或256
            //SecureRandom是生成安全随机数序列，password.getBytes()是种子，只要种子相同，序列就一样，所以生成的秘钥就一样。
            kg.init(128, new SecureRandom(password.getBytes()));
            SecretKey sk = kg.generateKey();
            byte[] b = sk.getEncoded();
            return byteToHexString(b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * byte数组转化为16进制字符串
     *
     * @param bytes
     * @return
     */
    public static String byteToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String strHex = Integer.toHexString(bytes[i]);
            if (strHex.length() > 3) {
                sb.append(strHex.substring(6));
            } else {
                if (strHex.length() < 2) {
                    sb.append("0" + strHex);
                } else {
                    sb.append(strHex);
                }
            }
        }
        return sb.toString();
    }

    /**
     * aes加密
     *
     * @param appKey
     * secret
     * @return
     */
    public static String AESEncode(String appKey, String content) {
        try {
            // 1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            // 2.根据ecnodeRules规则初始化密钥生成器
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(appKey.getBytes());
            // 生成一个128位的随机源,根据传入的字节数组
            keygen.init(128, secureRandom);
            // 3.产生原始对称密钥
            SecretKey original_key = keygen.generateKey();
            // 4.获得原始对称密钥的字节数组
            byte[] raw = original_key.getEncoded();
            // 5.根据字节数组生成AES密钥
            SecretKey key = new SecretKeySpec(raw, "AES");
            // 6.根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance("AES");
            // 7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.ENCRYPT_MODE, key);
            // 8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
            byte[] byte_encode = content.getBytes("utf-8");
            // 9.根据密码器的初始化方式--加密：将数据加密
            byte[] byte_AES = cipher.doFinal(byte_encode);
            // 10.将加密后的数据转换为字符串
            // 这里用Base64Encoder中会找不到包
            // 解决办法：
            // 在项目的Build path中先移除JRE System Library，再添加库JRE System
            // Library，重新编译后就一切正常了。
            String AES_encode = new BASE64Encoder().encode(byte_AES);
            // 11.将字符串返回
            return AES_encode;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 如果有错就返加nulll
        return null;
    }

    /**
     * aes解密
     *
     *  appKey
     *  secret
     * @return
     */
    public static String AESDncode(String appkey, String content) {
        try {
            // 1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            // 2.根据ecnodeRules规则初始化密钥生成器
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(appkey.getBytes());
            // 生成一个128位的随机源,根据传入的字节数组
            keygen.init(128, secureRandom);
            // 3.产生原始对称密钥
            SecretKey original_key = keygen.generateKey();
            // 4.获得原始对称密钥的字节数组
            byte[] raw = original_key.getEncoded();
            // 5.根据字节数组生成AES密钥
            SecretKey key = new SecretKeySpec(raw, "AES");
            // 6.根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance("AES");
            // 7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.DECRYPT_MODE, key);
            // 8.将加密并编码后的内容解码成字节数组
            byte[] byte_content = new BASE64Decoder().decodeBuffer(content);
            /*
             * 解密
             */
            byte[] byte_decode = cipher.doFinal(byte_content);
            String AES_decode = new String(byte_decode, "utf-8");
            return AES_decode;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        // 如果有错就返加null
        return null;
    }

    /**
     * 验签方法
     *
     *  sid
     *  rid
     *  rtime
     * @param appkey
     * @param sign
     * @return
     */
    public static boolean verifySign(String content, String appkey, String sign) {

        String result = null;
        result = sign(content, appkey);
        if (result.equals(sign)) {
            return true;
        }
        return false;
    }

    /**
     * 验签方法
     *
     *  sid
     *  rid
     *  rtime
     * @param appkey
     *  sign
     * @return
     */
    public static String sign(String content, String appkey) {

        String result = null;
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            byte[] keyBytes = appkey.getBytes("UTF-8");
            hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256"));
            byte[] hmacSha256Bytes = hmacSha256.doFinal(content.getBytes("UTF-8"));
            result = new String(Base64.encodeBase64(hmacSha256Bytes), "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
            * hmacsha256计算并进行base64转码获得sign值
	 * @return
             * @throws Exception
	 */
    public static String getSign(String sid,String rid,String rtime,String appsecret) throws Exception{
        String result=null;
        try{
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            if(StrUtil.isBlank(appsecret)){
                throw new Exception("未刷新appsercret，请执行apigetToken执行任务");
            }
            byte[] keyBytes = appsecret.getBytes("UTF-8");
            hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256"));
            String inputString = sid + rid + rtime;
            byte[] hmacSha256Bytes = hmacSha256.doFinal(inputString.getBytes("UTF-8"));
            result = new String(Base64.encodeBase64(hmacSha256Bytes), "UTF-8");
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        return result;
    }


    /**
     * 人社厅工具
     * @param
     */
    public static String encrypt(String content, String password) {
        byte[] data = null;
        try {
            data = content.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        data = encrypt(data, password, "UTF-8");
        String result = new Base64().encodeToString(data);
        return result;
    }

    public static byte[] encrypt(byte[] content, String password, String iv) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.ENCRYPT_MODE, key, createIV(iv));
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static SecretKeySpec createKey(String key) {
        byte[] data = null;
        if (key == null) {
            key = "";
        }
        StringBuffer sb = new StringBuffer(16);
        sb.append(key);
        while (sb.length() < 16) {
            sb.append("0");
        }
        if (sb.length() > 16) {
            sb.setLength(16);
        }

        try {
            data = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new SecretKeySpec(data, "AES");
    }

    private static IvParameterSpec createIV(String password) {
        byte[] data = null;
        if (password == null) {
            password = "";
        }
        StringBuffer sb = new StringBuffer(16);
        sb.append(password);
        while (sb.length() < 16) {
            sb.append("0");
        }
        if (sb.length() > 16) {
            sb.setLength(16);
        }

        try {
            data = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new IvParameterSpec(data);
    }

   public static String getMd5(String s) throws UnsupportedEncodingException {
	   if(s == null) 
		   return "";	   
	   else
		   return getMD5(s.getBytes("utf-8"));
   }
    
   public static String getMD5(byte abyte0[]) {
	   String s = null;
	   	char ac[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	   	try{
	   			MessageDigest messagedigest = MessageDigest.getInstance("MD5");
	   			messagedigest.update(abyte0);
	   			byte abyte1[] = messagedigest.digest();
	   			char ac1[] = new char[32];
	   			int i = 0;
	   			for(int j = 0; j < 16; j++){
	   				byte byte0 = abyte1[j];
	   				ac1[i++] = ac[byte0 >>> 4 & 15];
	   				ac1[i++] = ac[byte0 & 15];
	   			}
	   			s = new String(ac1);
	   			}
	   		catch(Exception exception)
	   		{
	   			exception.printStackTrace();
	   			}
	   		return s;
	   		}
   /**
            *     西部接口获取令牌AccessToken
    * @author shawn
    * @param client_id
    * @param client_secret
    * @return
    */
   
//   public String getAccessToken(String client_id, String client_secret) {
//	   String url = "http://59.218.251.14:20001/auth/token";
//	   String param = "client_id=19610600352310491R&client_secret=565186b901ca481da82f9e99d171ae01";
//	   // 构造HttpClient的实例
//       HttpClient client = new HttpClient();
//       //设置参数
//       // 创建GET方法的实例
//       GetMethod method = new GetMethod(url + "?" + param);
//       // 使用系统提供的默认的恢复策略
//       method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//               new DefaultHttpMethodRetryHandler());
//       try {
//           // 执行getMethod
//           client.executeMethod(method);
//           if (method.getStatusCode() == HttpStatus.SC_OK) {
//               String jsonresult = StreamUtils.copyToString(method.getResponseBodyAsStream(), Charset.forName("utf-8"));
//        	   JSONObject jsonObject = JSONObject.parseObject(jsonresult);
//        	   String result = (String) jsonObject.get("accessToken");
//        	   return result;
//           }
//       } catch (IOException e) {
//           e.printStackTrace();
//       } finally {
//           method.releaseConnection();
//       }
//       return null;
//   }
   
   /**
	*     西部接口民政厅加密方法
	* @author shawn
	* @return
	*/
   public String getSHA(String sxmz_client_secret, String sxmz_nonce) {
	   Date d = new Date();
	   SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	   String todayString = sdf.format(d);	   
	   String psw = sxmz_client_secret + todayString + sxmz_nonce;
	   System.out.println("todayString----"+todayString);
	   System.out.println("sxmz_nonce----"+sxmz_nonce);
	   System.out.println("psw----"+psw);
	   return DigestUtils.shaHex(psw);
   }
}