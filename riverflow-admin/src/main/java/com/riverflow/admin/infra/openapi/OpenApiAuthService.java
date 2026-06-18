package com.riverflow.admin.infra.openapi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.service.ApiAppService;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.api.entity.ApiApp;
import com.riverflow.api.entity.ApiCatalog;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 开放接口认证服务
 * <p>
 * 负责按接口配置执行认证校验，支持：
 * - none：放行
 * - sign：AppKey + AppSecret + HmacSHA256 签名
 * 同时校验调用方 IP 白名单。
 */
@Slf4j
@Service
public class OpenApiAuthService {

    private static final String HEADER_APP_KEY = "X-AppKey";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String HEADER_SIGNATURE = "X-Signature";
    private static final String NONCE_CACHE_PREFIX = "riverflow:openapi:nonce:";

    @Autowired
    private ApiCatalogService apiCatalogService;
    @Autowired
    private ApiAppService apiAppService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private OpenApiAuthProperties authProperties;
    @Autowired
    private StringEncryptor stringEncryptor;

    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";

    /**
     * 动态接口认证
     *
     * @param openPath 开放路径（如 /user/list）
     * @param method   请求方法 GET/POST/PUT/DELETE
     * @param request  包装后的请求
     * @return 认证结果
     */
    public OpenApiAuthResult authenticateDynamic(String openPath, String method,
                                                 RepeatableReadRequestWrapper request) {
        ApiCatalog api = apiCatalogService.getOne(
                new QueryWrapper<ApiCatalog>()
                        .eq("open_path", openPath)
                        .eq("open_method", method)
                        .eq("status", 1)
                        .eq("del_flag", 0)
                        .last("LIMIT 1")
        );

        if (api == null) {
            // 接口未注册，不在这里处理 404，交给 Controller 返回
            return OpenApiAuthResult.ok();
        }

        return authenticate(api, request);
    }

    /**
     * 固定流程接口认证（/open/flow/start、/open/flow/executeSync）
     *
     * @param request 包装后的请求
     * @return 认证结果
     */
    public OpenApiAuthResult authenticateFlow(RepeatableReadRequestWrapper request) {
        if (!authProperties.isFlowAuthEnabled()) {
            return OpenApiAuthResult.ok();
        }

        // 流程接口统一要求签名认证，只需 appKey 有效且应用启用即可
        String clientIp = RealIpExtractor.extract(request, authProperties.getTrustedProxies());
        if (!checkIpWhitelist(null, clientIp)) {
            return OpenApiAuthResult.fail(403, "IP 不在白名单内");
        }

        return verifySignature(request, null);
    }

    /**
     * 按 ApiCatalog 配置执行认证
     */
    public OpenApiAuthResult authenticate(ApiCatalog api, RepeatableReadRequestWrapper request) {
        String clientIp = RealIpExtractor.extract(request, authProperties.getTrustedProxies());
        if (!checkIpWhitelist(api.getAllowedIps(), clientIp)) {
            return OpenApiAuthResult.fail(403, "IP 不在白名单内，clientIp=" + clientIp);
        }

        String authType = api.getAuthType();
        if (!StringUtils.hasText(authType)) {
            authType = authProperties.getDefaultAuthType();
        }

        if ("none".equalsIgnoreCase(authType)) {
            return OpenApiAuthResult.ok();
        }

        if ("sign".equalsIgnoreCase(authType)) {
            return verifySignature(request, api);
        }

        if ("basic".equalsIgnoreCase(authType)) {
            return OpenApiAuthResult.fail(401, "Basic 认证暂未实现");
        }

        if ("oauth2".equalsIgnoreCase(authType)) {
            return OpenApiAuthResult.fail(401, "OAuth2 认证暂未实现");
        }

        return OpenApiAuthResult.fail(401, "不支持的认证方式: " + authType);
    }

    /**
     * 校验签名
     *
     * @param request 请求
     * @param api     动态接口配置；固定接口认证时可为 null
     */
    private OpenApiAuthResult verifySignature(RepeatableReadRequestWrapper request, ApiCatalog api) {
        String appKey = request.getHeader(HEADER_APP_KEY);
        String timestamp = request.getHeader(HEADER_TIMESTAMP);
        String nonce = request.getHeader(HEADER_NONCE);
        String signature = request.getHeader(HEADER_SIGNATURE);

        if (!StringUtils.hasText(appKey) || !StringUtils.hasText(timestamp)
                || !StringUtils.hasText(nonce) || !StringUtils.hasText(signature)) {
            return OpenApiAuthResult.fail(401, "缺少签名参数，请携带 X-AppKey、X-Timestamp、X-Nonce、X-Signature");
        }

        // 校验时间戳
        long now = System.currentTimeMillis() / 1000;
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            return OpenApiAuthResult.fail(401, "X-Timestamp 格式错误");
        }
        long tolerance = authProperties.getTimestampTolerance();
        if (Math.abs(now - ts) > tolerance) {
            return OpenApiAuthResult.fail(401, "请求已过期，请校验本机时间与服务器时间是否一致");
        }

        // 查询应用
        ApiApp app = apiAppService.getOne(
                new QueryWrapper<ApiApp>()
                        .eq("app_key", appKey)
                        .eq("status", 1)
                        .eq("del_flag", 0)
                        .last("LIMIT 1")
        );
        if (app == null || !StringUtils.hasText(app.getAppSecret())) {
            return OpenApiAuthResult.fail(401, "应用不存在或已被禁用");
        }

        // 如果 appSecret 使用 Jasypt 加密，先解密再验签
        String appSecret = decryptIfNeeded(app.getAppSecret());

        // 动态接口校验：该接口是否属于此应用（可选，增强隔离）
        if (api != null && api.getAppId() != null && !api.getAppId().equals(app.getId())) {
            return OpenApiAuthResult.fail(403, "应用无权访问该接口");
        }

        // nonce 防重放：使用 setIfAbsent 原子操作，避免并发竞态
        String nonceKey = NONCE_CACHE_PREFIX + nonce;
        Boolean setSuccess = redisTemplate.opsForValue()
                .setIfAbsent(nonceKey, "1", authProperties.getNonceExpire(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(setSuccess)) {
            return OpenApiAuthResult.fail(401, "请求重复提交");
        }

        // 校验签名
        String bodyString = request.getBodyString();
        boolean pass = OpenApiSignatureUtil.verify(appKey, appSecret, timestamp, nonce, bodyString, signature);
        if (!pass) {
            return OpenApiAuthResult.fail(401, "签名错误");
        }

        return OpenApiAuthResult.ok();
    }

    /**
     * 校验 IP 白名单
     *
     * @param allowedIps 接口级白名单；固定接口认证时传 null 表示不限制
     */
    private boolean checkIpWhitelist(String allowedIps, String clientIp) {
        // 固定流程接口暂不在接口级配置白名单，全局不限制
        if (!StringUtils.hasText(allowedIps)) {
            return true;
        }
        return IpWhitelistChecker.check(allowedIps, clientIp);
    }

    /**
     * 对 Jasypt 加密后的 appSecret 进行解密（若已加密）
     */
    private String decryptIfNeeded(String secret) {
        if (!StringUtils.hasText(secret)) {
            return secret;
        }
        String trimmed = secret.trim();
        if (trimmed.startsWith(ENC_PREFIX) && trimmed.endsWith(ENC_SUFFIX)) {
            try {
                String encrypted = trimmed.substring(ENC_PREFIX.length(), trimmed.length() - ENC_SUFFIX.length());
                return stringEncryptor.decrypt(encrypted);
            } catch (Exception e) {
                log.error("AppSecret 解密失败: {}", e.getMessage());
                return secret;
            }
        }
        return secret;
    }
}
