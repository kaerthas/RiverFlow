package com.inspur.workinfo.util;


import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;

/**
 * <p>Copyright: Copyright (c) 2016 易海网络</p>
 *
 * @author Oskar
 * @version 1.0
 * @description
 * @date 2022-06-08
 */
public class SM4Util extends com.inspur.workinfo.util.GMBaseUtil {


    private static final String ALGORITHM_NAME = "SM4";
    private static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
    private static final int DEFAULT_KEY_SIZE = 128;
    private static final String PROVIDER_ERROR = "sm4 NoSuchProviderException,please confirm import package of org.bouncycastle,  msg={},exception={}";

    private SM4Util() {
        throw new IllegalStateException("Utility class");
    }

    public static void generateKeyAndSave(String keyPath) throws Throwable {
        if (null == keyPath) {
            throw new IllegalArgumentException("keyPath must not be null");
        } else {
            String sm4Key = generateKey();
            if (null == sm4Key) {
              //  logger.error("generate sm4key failed,please try again");
            } else {
                writeFile(keyPath, sm4Key.getBytes());
              //  logger.info("sm4对称密钥保存成功,保存路径={}", keyPath);
            }
        }
    }

    public static String generateKey() throws NoSuchProviderException, NoSuchAlgorithmException {
        return generateKey(128);
    }

    public static String generateKey(int keySize) throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyGenerator kg = null;


            kg = KeyGenerator.getInstance("SM4", "BC");
            kg.init(keySize, new SecureRandom());
            return byte2Base64(kg.generateKey().getEncoded());

    }

    public static String getKey(String filePath) throws IOException {
        return loadKeyFromFile(filePath);
    }

    public static String encrypt(String key, String data) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, IOException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = null;


            cipher = generateEcbCipher("SM4/ECB/PKCS5Padding", 1, key);
            return byte2Base64(cipher.doFinal(data.getBytes()));

    }

    public static String decrypt(String key, String cipherText) throws InvalidKeyException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = null;


            cipher = generateEcbCipher("SM4/ECB/PKCS5Padding", 2, key);
            return new String(cipher.doFinal(base642Byte(cipherText)));

    }

    private static Cipher generateEcbCipher(String algorithmName, int mode, String key) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException {
        Cipher cipher = Cipher.getInstance(algorithmName, "BC");
        Key sm4Key = new SecretKeySpec(base642Byte(key), "SM4");
        cipher.init(mode, sm4Key);
        return cipher;
    }
}
