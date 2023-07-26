package com.inspur.workinfo.util;

import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * 编码工具类
 * 1.AES加密
 * 2.AES加密为16进制字符串
 * 3.AES解密
 * 4.将16进制字符串解密
 * @author uikoo9
 * @version 0.0.7.20140601
 */
public class AesEncryptUtil {


    public static String sign(String content) {
        String ciphertext = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            // 对接后的字符串进行sha1 hash
            byte[] digest = md.digest(content.toString().getBytes());
            ciphertext = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return ciphertext != null ? ciphertext.toLowerCase() : null;
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    public static String byteToStr(byte[] byteArray) {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }

    public static String byteToHexStr(byte mByte) {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];	// 取一个字节的高4位，然后获得其对应的十六进制字符
        tempArr[1] = Digit[mByte & 0X0F];	//  取一个字节的低4位，然后获得其对应的十六进制字符

        String s = new String(tempArr);
        return s;
    }


    /**
     * base 64 encode
     * @param bytes 待编码的byte[]
     * @return 编码后的base 64 code
     */
    public static String base64Encode(byte[] bytes){
        return new BASE64Encoder().encode(bytes);
    }

    /**
     * base 64 decode
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     * @throws Exception
     */
    public static byte[] base64Decode(String base64Code) throws Exception{
        return StringUtils.isEmpty(base64Code) ? null : new BASE64Decoder().decodeBuffer(base64Code);
    }

    /**
     * AES加密
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的byte[]
     * @throws Exception
     */
    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        //1.构造密钥生成器，指定为AES算法,不区分大小写
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        //2.根据ecnodeRules规则初始化密钥生成器

        SecureRandom random=SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey.getBytes());
        //生成一个128位的随机源,根据传入的字节数组
        kgen.init(128, random);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));

        return cipher.doFinal(content.getBytes("utf-8"));
    }

    /**
     * AES加密为token（16进制）
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的token（16进制）
     * @throws Exception
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        //将加密后的密文中的+用|替换，否则会在url传输中+被替换为空格造成解密失败
        return base64Encode(aesEncryptToBytes(content, encryptKey)).replace("+", "_");
    }

    /**
     * AES解密
     * @param encryptBytes 待解密的byte[]
     * @param decryptKey 解密密钥
     * @return 解密后的String
     * @throws Exception
     */
    public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random=SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(decryptKey.getBytes());
        kgen.init(128, random);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);

        return new String(decryptBytes,"utf-8");
    }

    /**
     * 将token AES解密
     * @param encryptStr 待解密的token
     * @param decryptKey 解密密钥
     * @return 解密后的string
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return StringUtils.isEmpty(encryptStr) ? null : aesDecryptByBytes(base64Decode(encryptStr.replace("_", "+")), decryptKey);
    }

    /**转换成16进制
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }



    public static final String decrypt(String var0) {
        if (var0 == null) {
            return "";
        } else if (var0.length() == 0) {
            return "";
        } else {
            BigInteger var1 = new BigInteger("0933910847463829232312312");

            try {
                BigInteger var2 = new BigInteger(var0, 16);
                BigInteger var3 = var2.xor(var1);
                return new String(var3.toByteArray());
            } catch (Exception var4) {
                return "";
            }
        }
    }

    public static final String encrypt(String var0) {
        if (var0 == null) {
            return "";
        } else if (var0.length() == 0) {
            return "";
        } else {
            BigInteger var1 = new BigInteger(var0.getBytes());
            BigInteger var2 = new BigInteger("0933910847463829232312312");
            BigInteger var3 = var2.xor(var1);
            return var3.toString(16);
        }
    }

}