package com.riverflow.admin.infra.groovy;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;

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

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 脚本执行线程池
     */
    private final ExecutorService scriptExecutor = new ThreadPoolExecutor(
        2, 10, 60L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100),
        r -> {
            Thread t = new Thread(r, "groovy-script-pool");
            t.setDaemon(true);
            return t;
        },
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * 脚本执行超时（秒）
     */
    @Value("${riverflow.groovy.timeout:5}")
    private int scriptTimeoutSeconds;

    private static final String SCRIPT_TEMPLATE_PREFIX =
        "import com.alibaba.fastjson2.JSON\n" +
        "import com.alibaba.fastjson2.JSONObject\n" +
        "import com.alibaba.fastjson2.JSONArray\n" +
        "import cn.hutool.core.util.StrUtil\n" +
        "import cn.hutool.core.date.DateUtil\n" +
        "import cn.hutool.crypto.SecureUtil\n" +
        "import cn.hutool.http.HttpUtil\n" +
        "import cn.hutool.http.HttpRequest\n" +
        "import com.riverflow.admin.infra.groovy.GroovyUtils\n" +
        "\n" +
        "def execute(Map args) {\n" +
        "    def context = args.context\n" +
        "    def ctx = args.ctx\n" +
        "    def params = args.params ?: args.ctx\n" +
        "    def instanceId = args.instanceId\n" +
        "    def redis = args.redis\n" +
        "    def utils = args.utils\n";

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

        // 注入 GroovyUtils 工具类，脚本中可通过 utils 或 GroovyUtils 直接调用
        args.putIfAbsent("utils", new GroovyUtils());

        String fullScript = SCRIPT_TEMPLATE_PREFIX + scriptContent + SCRIPT_TEMPLATE_SUFFIX;
        String scriptMd5 = DigestUtils.md5DigestAsHex(fullScript.getBytes());

        try {
            Future<JSONObject> future = scriptExecutor.submit(() -> {
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
            });

            return future.get(scriptTimeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("Groovy脚本执行超时（{}秒），已中断", scriptTimeoutSeconds);
            throw new RuntimeException("脚本执行超时（最大" + scriptTimeoutSeconds + "秒），请优化脚本或联系管理员");
        } catch (Exception e) {
            log.error("Groovy 脚本执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("脚本执行失败: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() {
        scriptExecutor.shutdownNow();
        log.info("Groovy脚本执行线程池已关闭");
    }

    /**
     * 清除脚本缓存
     */
    public void clearCache() {
        SCRIPT_CACHE.clear();
        log.info("Groovy 脚本缓存已清空");
    }

    /**
     * 安全检查（增强版：覆盖反射、脚本引擎、字符串拼接绕过等）
     */
    private boolean containsDangerousKeyword(String script) {
        String lower = script.toLowerCase();
        String[] dangers = {
            // 命令执行
            "runtime.exec", "processbuilder", "system.exit",
            // 类加载与反射
            "classloader", "defineclass", "loadclass",
            "class.forname", "getmethod(", "getdeclaredmethod(", "invoke(", "newinstance(",
            ".getclass()", ".class ", ".class\n", ".class\t",
            "getruntime", "runtime.get",
            // 文件系统
            "fileinputstream", "fileoutputstream", "file.delete",
            "randomaccessfile", "filewriter", "filereader", "file.create",
            // 网络
            "socket(", "serversocket(", "url(", "httpurlconnection", "openconnection",
            // 脚本引擎与代码执行
            "eval.me", "evaluate(", "groovyshell", "scriptengine",
            "nashorn", "javascript", "compilercallback",
            // 系统信息泄露
            "system.getproperty", "system.getenv", "system.console", "system.in",
            // 线程与并发
            "thread.sleep", "thread.start", "thread.run",
            "executor", "executors.", "scheduledexecutor",
            // 进程
            "process ", "process.", "processbuilder"
        };
        for (String danger : dangers) {
            if (lower.contains(danger)) {
                log.warn("脚本包含危险关键字: {}", danger);
                return true;
            }
        }

        // 检测字符串拼接的恶意组合（如 runt + ime）
        String stripped = lower.replaceAll("[\"'\\+\\s]", "");
        if (stripped.contains("runtime") || stripped.contains("processbuilder")
                || stripped.contains("fileoutputstream") || stripped.contains("fileinputstream")) {
            log.warn("脚本可能包含字符串拼接绕过的危险调用");
            return true;
        }

        return false;
    }
}
