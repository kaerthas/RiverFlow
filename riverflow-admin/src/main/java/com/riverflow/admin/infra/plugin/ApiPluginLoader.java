package com.riverflow.admin.infra.plugin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.service.SysPluginService;
import com.riverflow.api.entity.SysPlugin;
import com.riverflow.api.plugin.ApiPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口插件加载器
 * 支持从数据库和文件系统动态加载接口插件（ApiPlugin）
 */
@Slf4j
@Component
public class ApiPluginLoader implements SmartInitializingSingleton {

    @Value("${riverflow.plugin.dir:${user.home}/riverflow/plugins}")
    private String pluginDirConfig;

    @Value("${riverflow.plugin.enabled:true}")
    private boolean pluginEnabled;

    @Autowired
    private SysPluginService sysPluginService;

    @Autowired
    private ApplicationContext applicationContext;

    private String pluginDir;
    private final Map<String, ApiPlugin> pluginMap = new ConcurrentHashMap<>();
    private final Map<String, URLClassLoader> classLoaderMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (!pluginEnabled) {
            log.info("接口插件系统已禁用");
            return;
        }
        pluginDir = pluginDirConfig;
        File pluginDirectory = new File(pluginDir);
        if (!pluginDirectory.exists()) {
            boolean created = pluginDirectory.mkdirs();
            if (created) {
                log.info("创建接口插件目录: {}", pluginDir);
            }
        }
        log.info("接口插件目录: {}", pluginDir);
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!pluginEnabled) {
            return;
        }
        log.info("所有Spring Bean初始化完成，开始加载接口插件...");
        loadPluginsFromDatabase();
    }

    private void loadPluginsFromDatabase() {
        QueryWrapper<SysPlugin> wrapper = new QueryWrapper<>();
        wrapper.in("plugin_scope", Arrays.asList("api", "both"))
               .eq("status", "enabled")
               .eq("del_flag", 0);

        List<SysPlugin> plugins = sysPluginService.list(wrapper);
        log.info("从数据库加载 {} 个接口插件记录", plugins.size());

        for (SysPlugin plugin : plugins) {
            try {
                File jarFile = new File(plugin.getJarPath());
                if (!jarFile.exists()) {
                    log.warn("接口插件JAR文件不存在: {}", plugin.getJarPath());
                    continue;
                }
                loadPluginFromJar(jarFile, plugin.getPluginType());
                log.info("接口插件加载成功: {}", plugin.getPluginType());
            } catch (Exception e) {
                log.error("加载接口插件失败: {}", plugin.getPluginName(), e);
            }
        }
        log.info("接口插件加载完成，共加载 {} 个", pluginMap.size());
    }

    public ApiPlugin loadPluginFromJar(File jarFile, String expectedType) throws MalformedURLException {
        log.info("加载接口插件JAR: {}, expectedType={}", jarFile.getName(), expectedType);

        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{jarUrl},
            getClass().getClassLoader()
        );

        ServiceLoader<ApiPlugin> serviceLoader = ServiceLoader.load(ApiPlugin.class, classLoader);
        Iterator<ApiPlugin> iterator = serviceLoader.iterator();

        int foundCount = 0;
        ApiPlugin loadedPlugin = null;
        while (iterator.hasNext()) {
            foundCount++;
            try {
                ApiPlugin plugin = iterator.next();
                String pluginType = plugin.getPluginType();
                log.info("发现接口插件实现: type={}, class={}", pluginType, plugin.getClass().getName());

                if (expectedType != null && !expectedType.equals(pluginType)) {
                    log.info("插件类型不匹配，跳过: expected={}, actual={}", expectedType, pluginType);
                    continue;
                }

                if (pluginMap.containsKey(pluginType)) {
                    log.warn("接口插件类型冲突: {} 已存在，将被覆盖", pluginType);
                    unloadPlugin(pluginType);
                }

                if (applicationContext != null) {
                    try {
                        plugin.init(applicationContext);
                        log.info("接口插件初始化完成: type={}", pluginType);
                    } catch (Exception e) {
                        log.error("接口插件初始化失败: type={}, error={}", pluginType, e.getMessage(), e);
                    }
                }

                pluginMap.put(pluginType, plugin);
                classLoaderMap.put(pluginType, classLoader);
                loadedPlugin = plugin;

                log.info("注册接口插件成功: type={}, name={}, category={}",
                    pluginType, plugin.getPluginName(), plugin.getCategory());
            } catch (Error e) {
                log.error("实例化接口插件失败: {}, error={}", jarFile.getName(), e.getMessage(), e);
            }
        }

        if (foundCount == 0) {
            log.warn("JAR [{}] 中未找到任何 ApiPlugin 实现，请检查 META-INF/services/com.riverflow.api.plugin.ApiPlugin 是否存在", jarFile.getName());
        } else if (loadedPlugin == null) {
            log.warn("JAR [{}] 中找到 {} 个 ApiPlugin 实现，但均不符合 expectedType={}", jarFile.getName(), foundCount, expectedType);
        }

        return loadedPlugin;
    }

    public boolean unloadPlugin(String pluginType) {
        URLClassLoader classLoader = classLoaderMap.remove(pluginType);
        if (classLoader != null) {
            try {
                classLoader.close();
                log.info("关闭接口插件ClassLoader: {}", pluginType);
            } catch (IOException e) {
                log.error("关闭接口插件ClassLoader失败", e);
            }
        }

        ApiPlugin removed = pluginMap.remove(pluginType);
        if (removed != null) {
            log.info("卸载接口插件: {}", pluginType);
            return true;
        }
        return false;
    }

    public ApiPlugin getPlugin(String pluginType) {
        return pluginMap.get(pluginType);
    }

    public boolean hasPlugin(String pluginType) {
        return pluginMap.containsKey(pluginType);
    }

    public Map<String, ApiPlugin> getAllPlugins() {
        return Collections.unmodifiableMap(pluginMap);
    }

    public int getPluginCount() {
        return pluginMap.size();
    }

    public String getPluginDir() {
        return pluginDir;
    }
}
