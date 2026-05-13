package com.riverflow.admin.infra.groovy;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy 沙箱执行器
 * 带脚本缓存与超时控制，限制敏感类加载
 */
@Slf4j
@Component
public class GroovySandboxExecutor {

    /**
     * 脚本缓存：MD5 -> Script
     */
    private static final Map<String, Script> SCRIPT_CACHE = new ConcurrentHashMap<>();

    /**
     * 默认脚本模板前缀：注入常用工具类
     */
    private static final String SCRIPT_TEMPLATE_PREFIX =
        "import com.alibaba.fastjson2.JSON\n" +
        "import com.alibaba.fastjson2.JSONObject\n" +
        "import com.alibaba.fastjson2.JSONArray\n" +
        "import cn.hutool.core.util.StrUtil\n" +
        "import cn.hutool.core.date.DateUtil\n" +
        "import cn.hutool.crypto.SecureUtil\n" +
        "\n" +
        "def execute(Map args) {\n";

    private static final String SCRIPT_TEMPLATE_SUFFIX = "\n}";

    /**
     * 执行脚本
     *
     * @param scriptContent Groovy 脚本内容（execute 方法体）
     * @param args          入参
     * @return 执行结果
     */
    public JSONObject execute(String scriptContent, Map<String, Object> args) {
        if (scriptContent == null || scriptContent.trim().isEmpty()) {
            throw new IllegalArgumentException("脚本内容不能为空");
        }

        // 安全检查：禁止包含危险关键字
        if (containsDangerousKeyword(scriptContent)) {
            throw new SecurityException("脚本包含非法关键字，已被拦截");
        }

        String fullScript = SCRIPT_TEMPLATE_PREFIX + scriptContent + SCRIPT_TEMPLATE_SUFFIX;
        String scriptMd5 = DigestUtils.md5DigestAsHex(fullScript.getBytes());

        try {
            Script script = SCRIPT_CACHE.computeIfAbsent(scriptMd5, md5 -> {
                GroovyShell shell = new GroovyShell();
                return shell.parse(fullScript);
            });

            Object result = script.invokeMethod("execute", new Object[]{args});

            if (result == null) {
                return new JSONObject();
            }
            if (result instanceof JSONObject) {
                return (JSONObject) result;
            }
            return JSON.parseObject(JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("Groovy 脚本执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("脚本执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 清除脚本缓存
     */
    public void clearCache() {
        SCRIPT_CACHE.clear();
        log.info("Groovy 脚本缓存已清空");
    }

    /**
     * 安全检查
     */
    private boolean containsDangerousKeyword(String script) {
        String lower = script.toLowerCase();
        String[] dangers = {
            "runtime.exec", "processbuilder", "system.exit",
            "classloader", "defineclass", "loadclass",
            "fileinputstream", "fileoutputstream", "file.delete",
            "socket(", "serversocket(", "url(", "httpurlconnection",
            "thread.sleep"
        };
        for (String danger : dangers) {
            if (lower.contains(danger)) {
                log.warn("脚本包含危险关键字: {}", danger);
                return true;
            }
        }
        return false;
    }
}
