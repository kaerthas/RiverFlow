package com.riverflow.admin.infra.groovy;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Groovy 脚本工具类
 * <p>
 * 提供脚本节点和脚本接口中常用的工具方法，全部静态方法，
 * 脚本中可直接通过 GroovyUtils.xxx() 调用。
 * </p>
 *
 * <pre>
 * // 示例用法（在 Groovy 脚本中）：
 * def name = GroovyUtils.defaultIfBlank(params.name, "匿名")
 * def sign = GroovyUtils.md5(name + GroovyUtils.timestamp())
 * def json = GroovyUtils.parseObj('{"a":1}')
 * def today = GroovyUtils.formatDate(new Date(), "yyyy-MM-dd")
 * </pre>
 */
@Slf4j
public class GroovyUtils {

    // ==================== 字符串操作 ====================

    /**
     * 判断字符串是否为空（null、空串、纯空白字符）
     */
    public static boolean isBlank(CharSequence str) {
        return StrUtil.isBlank(str);
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotBlank(CharSequence str) {
        return StrUtil.isNotBlank(str);
    }

    /**
     * 字符串为空时返回默认值
     */
    public static String defaultIfBlank(CharSequence str, String defaultValue) {
        return StrUtil.isBlank(str) ? defaultValue : str.toString();
    }

    /**
     * 截取字符串（支持负索引从末尾开始）
     */
    public static String sub(String str, int fromIndex, int toIndex) {
        return StrUtil.sub(str, fromIndex, toIndex);
    }

    /**
     * 字符串替换（全部替换）
     */
    public static String replace(String str, String searchStr, String replacement) {
        if (str == null || searchStr == null)
            return str;
        return str.replace(searchStr, replacement);
    }

    /**
     * 按正则分割字符串
     */
    public static List<String> split(String str, String separator) {
        if (str == null)
            return Collections.emptyList();
        return Arrays.asList(str.split(separator));
    }

    /**
     * 去除前后空白
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 判断字符串是否包含子串
     */
    public static boolean contains(String str, CharSequence search) {
        return str != null && str.contains(search);
    }

    /**
     * 格式化字符串（类似 Java String.format）
     */
    public static String format(String template, Object... params) {
        return StrUtil.format(template, params);
    }

    // ==================== 日期时间操作 ====================

    /**
     * 获取当前时间
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 格式化日期
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null || pattern == null)
            return null;
        return DateUtil.format(date, pattern);
    }

    /**
     * 按默认格式解析日期（yyyy-MM-dd HH:mm:ss 等常见格式自动识别）
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null)
            return null;
        return DateUtil.parse(dateStr);
    }

    /**
     * 按指定格式解析日期
     */
    public static Date parseDate(String dateStr, String pattern) {
        if (dateStr == null || pattern == null)
            return null;
        return DateUtil.parse(dateStr, pattern);
    }

    /**
     * 日期偏移（field 取值对应 DateField 枚举: YEAR/MONTH/DAY/HOUR/MINUTE/SECOND）
     */
    public static Date dateOffset(Date date, String field, int offset) {
        if (date == null || field == null)
            return null;
        DateField df;
        try {
            df = DateField.valueOf(field.toUpperCase());
        } catch (IllegalArgumentException e) {
            return date;
        }
        return DateUtil.offset(date, df, offset);
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    public static long timestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 计算两个日期相差的天数
     */
    public static long daysBetween(Date start, Date end) {
        if (start == null || end == null)
            return 0;
        return DateUtil.betweenDay(start, end, true);
    }

    // ==================== JSON 操作 ====================

    /**
     * 解析 JSON 字符串为 JSONObject
     */
    public static JSONObject parseObj(String json) {
        if (json == null || json.trim().isEmpty())
            return new JSONObject();
        return JSON.parseObject(json);
    }

    /**
     * 解析 JSON 字符串为 JSONArray
     */
    public static JSONArray parseArr(String json) {
        if (json == null || json.trim().isEmpty())
            return new JSONArray();
        return JSON.parseArray(json);
    }

    /**
     * 对象转为 JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null)
            return "null";
        return JSON.toJSONString(obj);
    }

    /**
     * 美化输出 JSON 字符串
     */
    public static String toJsonPretty(Object obj) {
        if (obj == null)
            return "null";
        return JSON.toJSONString(obj, com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat);
    }

    /**
     * 从 JSONObject 按路径取值（支持点号路径，如 data.user.name）
     */
    public static Object getByPath(Object jsonObj, String path) {
        if (jsonObj == null || path == null)
            return null;
        if (jsonObj instanceof JSONObject) {
            return ((JSONObject) jsonObj).getByPath(path);
        }
        return null;
    }

    // ==================== 加密 / 编码 ====================

    /**
     * MD5 加密
     */
    public static String md5(String data) {
        if (data == null)
            return null;
        return SecureUtil.md5(data);
    }

    /**
     * SHA256 加密
     */
    public static String sha256(String data) {
        if (data == null)
            return null;
        return SecureUtil.sha256(data);
    }

    /**
     * Base64 编码
     */
    public static String base64Encode(String data) {
        if (data == null)
            return null;
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    /**
     * Base64 解码
     */
    public static String base64Decode(String data) {
        if (data == null)
            return null;
        return new String(Base64.getDecoder().decode(data));
    }

    /**
     * 生成 UUID（无横线）
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成随机字符串
     *
     * @param length 长度
     */
    public static String randomStr(int length) {
        return RandomUtil.randomString(length);
    }

    /**
     * 生成随机数字字符串
     */
    public static String randomNum(int length) {
        return RandomUtil.randomNumbers(length);
    }

    // ==================== 集合操作 ====================

    /**
     * 判断集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否不为空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断 Map 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断 Map 是否不为空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 快速创建 List
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        if (elements == null)
            return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(elements));
    }

    /**
     * 快速创建 Map（key1, value1, key2, value2...）
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(Object... kvs) {
        Map<K, V> map = new LinkedHashMap<>();
        if (kvs == null || kvs.length % 2 != 0) {
            throw new IllegalArgumentException("mapOf 参数必须为偶数个（key1, value1, key2, value2...）");
        }
        for (int i = 0; i < kvs.length; i += 2) {
            map.put((K) kvs[i], (V) kvs[i + 1]);
        }
        return map;
    }

    /**
     * 过滤集合中的 null 元素
     */
    public static <T> List<T> filterNotNull(List<T> list) {
        if (list == null)
            return Collections.emptyList();
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    // ==================== 类型转换 ====================

    /**
     * 转为 int（失败返回默认值）
     */
    public static int toInt(Object obj, int defaultValue) {
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转为 int（失败返回 0）
     */
    public static int toInt(Object obj) {
        return toInt(obj, 0);
    }

    /**
     * 转为 long（失败返回默认值）
     */
    public static long toLong(Object obj, long defaultValue) {
        try {
            return Long.parseLong(String.valueOf(obj));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转为 long（失败返回 0L）
     */
    public static long toLong(Object obj) {
        return toLong(obj, 0L);
    }

    /**
     * 转为 double（失败返回默认值）
     */
    public static double toDouble(Object obj, double defaultValue) {
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转为 double（失败返回 0.0）
     */
    public static double toDouble(Object obj) {
        return toDouble(obj, 0.0);
    }

    /**
     * 转为 BigDecimal（失败返回 null）
     */
    public static BigDecimal toDecimal(Object obj) {
        try {
            return NumberUtil.toBigDecimal(String.valueOf(obj));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 转为 String
     */
    public static String toStr(Object obj) {
        if (obj == null)
            return null;
        return String.valueOf(obj);
    }

    /**
     * 转为 String（为空时返回默认值）
     */
    public static String toStr(Object obj, String defaultValue) {
        if (obj == null)
            return defaultValue;
        return String.valueOf(obj);
    }

    /**
     * 转为 boolean
     */
    public static boolean toBool(Object obj) {
        if (obj == null)
            return false;
        String s = String.valueOf(obj).trim().toLowerCase();
        return "true".equals(s) || "1".equals(s) || "yes".equals(s) || "on".equals(s);
    }

    // ==================== 数学操作 ====================

    /**
     * 取最大值
     */
    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    /**
     * 取最小值
     */
    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    /**
     * 四舍五入
     */
    public static double round(double value, int scale) {
        return NumberUtil.round(value, scale).doubleValue();
    }

    /**
     * 向上取整
     */
    public static double ceil(double value) {
        return Math.ceil(value);
    }

    /**
     * 向下取整
     */
    public static double floor(double value) {
        return Math.floor(value);
    }

    /**
     * 保留 scale 位小数（不进行四舍五入，直接截断）
     */
    public static double truncate(double value, int scale) {
        if (scale < 0)
            throw new IllegalArgumentException("scale 必须 >= 0");
        BigDecimal bd = BigDecimal.valueOf(value);
        return bd.setScale(scale, RoundingMode.DOWN).doubleValue();
    }

    // ==================== HTTP 快捷 ====================

    /**
     * 发送 GET 请求
     */
    public static String httpGet(String url) {
        if (url == null)
            return null;
        return HttpUtil.get(url);
    }

    /**
     * 发送 POST 请求（JSON 请求体）
     */
    public static String httpPost(String url, String body) {
        if (url == null)
            return null;
        return HttpUtil.post(url, body);
    }

    // ==================== 对象判空 ====================

    /**
     * 判断对象是否为 null
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * 判断对象是否不为 null
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * 对象为空时返回默认值
     */
    public static <T> T defaultIfNull(T obj, T defaultValue) {
        return obj == null ? defaultValue : obj;
    }

    /**
     * 深拷贝对象（通过 JSON 序列化/反序列化）
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T obj) {
        if (obj == null)
            return null;
        return (T) JSON.parseObject(JSON.toJSONString(obj), obj.getClass());
    }

    /**
     * 对象转 Map（基于 fastjson2）
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object obj) {
        if (obj == null)
            return new LinkedHashMap<>();
        if (obj instanceof Map)
            return (Map<String, Object>) obj;
        return JSON.parseObject(JSON.toJSONString(obj), LinkedHashMap.class);
    }

    /**
     * 睡眠（毫秒），用于脚本中的延时逻辑
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * RSA 私钥签名（SHA256withRSA）
     * <p>
     * 支持两种私钥格式：
     * 1. 纯 base64 字符串（PKCS#8）
     * 2. PEM 格式（含 -----BEGIN PRIVATE KEY----- 头尾）
     * </p>
     *
     * @param source           待签名字符串
     * @param privateKeyContent 私钥内容
     * @return Base64 编码的签名
     */
    public static String sign(String source, String privateKeyContent) {
        if (StrUtil.isBlank(source) || StrUtil.isBlank(privateKeyContent)) {
            return null;
        }
        try {
            // 处理 PEM 格式：去掉头尾和换行/空格
            String cleanedKey = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] encodedKey = Base64.getDecoder().decode(cleanedKey);
            java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(encodedKey);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            java.security.Signature signature = java.security.Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(source.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            log.error("RSA 签名失败: {}", e.getMessage(), e);
            throw new RuntimeException("RSA 签名失败: " + e.getMessage(), e);
        }
    }

    public static String generateSignSource(Map params) {
        Set<String> keySet = params.keySet();
        List<String> keys = new ArrayList<>();
        for (String key : keySet) {
            if (params.get(key) != null && StrUtil.isNotBlank(params.get(key).toString())) {
                keys.add(key);
            }
        }
        Collections.sort(keys);
        StringBuilder builder = new StringBuilder();
        for (int i = 0, size = keys.size(); i < size; i++) {
            String key = keys.get(i);
            Object value = params.get(key);
            builder.append(key);
            builder.append("=");
            builder.append(value);
            if (i != size - 1) {
                builder.append("&");
            }
        }
        return builder.toString();
    }
    /**
     * 仅根据 clientSecret、date、nonce 计算 SHA-1 token。
     */
    public static String computeToken(String clientSecret, String date, String nonce) {
        try {
            String tokenStr = clientSecret + date + nonce;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(tokenStr.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }



}
