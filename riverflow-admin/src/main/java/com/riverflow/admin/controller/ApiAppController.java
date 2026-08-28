package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.ApiAppService;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.api.entity.ApiApp;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口应用/目录管理
 */
@Slf4j
@RestController
@RequestMapping("/api-app")
public class ApiAppController {

    @Autowired
    private ApiAppService apiAppService;
    @Autowired
    private ApiCatalogService apiCatalogService;
    @Autowired
    private StringEncryptor stringEncryptor;

    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";

    @GetMapping("/list")
    public R<Page<ApiApp>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "100") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<ApiApp> pageParam = new Page<>(page, size);
        QueryWrapper<ApiApp> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like("app_code", keyword).or().like("app_name", keyword));
        }
        qw.orderByAsc("sort_no").orderByDesc("create_time");
        Page<ApiApp> result = apiAppService.page(pageParam, qw);
        result.getRecords().forEach(app -> {
            if (app.getAppSecret() != null) {
                app.setAppSecret(maskSecret(decryptIfNeeded(app.getAppSecret())));
            }
        });
        return R.ok(result);
    }

    @GetMapping("/list-all")
    public R<List<ApiApp>> listAll(@RequestParam(value = "status", required = false) Integer status) {
        QueryWrapper<ApiApp> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByAsc("sort_no").orderByDesc("create_time");
        List<ApiApp> list = apiAppService.list(qw);
        list.forEach(app -> {
            if (app.getAppSecret() != null) {
                app.setAppSecret(maskSecret(decryptIfNeeded(app.getAppSecret())));
            }
        });
        return R.ok(list);
    }

    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 8) {
            return "********";
        }
        return secret.substring(0, 4) + "********" + secret.substring(secret.length() - 4);
    }

    private boolean isMasked(String secret) {
        return secret != null && secret.contains("********");
    }

    @GetMapping("/{id}")
    public R<ApiApp> getById(@PathVariable Long id) {
        ApiApp app = apiAppService.getById(id);
        if (app != null && app.getAppSecret() != null) {
            // 详情返回解密后的真实密钥，供管理员查看/复制
            app.setAppSecret(decryptIfNeeded(app.getAppSecret()));
        }
        return R.ok(app);
    }

    @PostMapping
    public R<String> save(@RequestBody ApiApp apiApp) {
        encryptSecretIfNeeded(apiApp);
        apiAppService.saveOrUpdate(apiApp);
        return R.ok(String.valueOf(apiApp.getId()));
    }

    @PutMapping
    public R<String> update(@RequestBody ApiApp apiApp) {
        // 前端编辑时看到的 appSecret 是脱敏后的，未修改时不应覆盖原值
        if (isMasked(apiApp.getAppSecret())) {
            ApiApp old = apiAppService.getById(apiApp.getId());
            if (old != null) {
                apiApp.setAppSecret(old.getAppSecret());
            } else {
                apiApp.setAppSecret(null);
            }
        } else {
            encryptSecretIfNeeded(apiApp);
        }
        apiAppService.updateById(apiApp);
        return R.ok(String.valueOf(apiApp.getId()));
    }

    /**
     * 对 appSecret 进行 Jasypt 加密（若尚未加密）
     */
    private void encryptSecretIfNeeded(ApiApp apiApp) {
        String secret = apiApp.getAppSecret();
        if (!StringUtils.hasText(secret)) {
            return;
        }
        String trimmed = secret.trim();
        if (trimmed.startsWith(ENC_PREFIX) && trimmed.endsWith(ENC_SUFFIX)) {
            return;
        }
        try {
            String encrypted = stringEncryptor.encrypt(secret);
            apiApp.setAppSecret(ENC_PREFIX + encrypted + ENC_SUFFIX);
        } catch (Exception e) {
            log.error("AppSecret 加密失败: {}", e.getMessage());
            throw new RuntimeException("AppSecret 加密失败", e);
        }
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

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 检查是否有关联的接口
        long count = apiCatalogService.count(
                new QueryWrapper<com.riverflow.api.entity.ApiCatalog>()
                        .eq("app_id", id)
                        .eq("del_flag", 0));
        if (count > 0) {
            return R.fail("该应用下存在 " + count + " 个接口，请先移除或迁移接口后再删除应用");
        }
        apiAppService.removeById(id);
        return R.ok();
    }

    /**
     * 批量获取应用下的接口数量
     */
    @GetMapping("/api-counts")
    public R<Map<Long, Long>> getApiCounts(@RequestParam String appIds) {
        if (appIds == null || appIds.isEmpty()) {
            return R.ok(new HashMap<>());
        }
        List<Long> idList = Arrays.stream(appIds.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        List<Map<String, Object>> list = apiCatalogService.listMaps(
                new QueryWrapper<com.riverflow.api.entity.ApiCatalog>()
                        .select("app_id, count(*) as cnt")
                        .in("app_id", idList)
                        .eq("del_flag", 0)
                        .groupBy("app_id"));
        Map<Long, Long> result = list.stream()
                .collect(Collectors.toMap(
                        m -> Long.valueOf(String.valueOf(m.get("app_id"))),
                        m -> Long.valueOf(String.valueOf(m.get("cnt"))),
                        (a, b) -> a));
        return R.ok(result);
    }
}
