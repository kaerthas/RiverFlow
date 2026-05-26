package com.riverflow.admin.infra.plugin;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.service.SysPluginService;
import com.riverflow.api.entity.SysPlugin;
import com.riverflow.api.plugin.NodePlugin;
import com.riverflow.common.result.R;
import com.riverflow.common.spring.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点插件加载器
 * 支持从数据库和文件系统动态加载节点插件
 */
@Slf4j
@Component
public class NodePluginLoader implements SmartInitializingSingleton {

    @Value("${riverflow.plugin.dir:${user.home}/riverflow/plugins}")
    private String pluginDirConfig;

    @Value("${riverflow.plugin.enabled:true}")
    private boolean pluginEnabled;

    @Autowired
    private SysPluginService sysPluginService;

    @Autowired
    private ApplicationContext applicationContext;

    private String pluginDir;
    private final Map<String, NodePlugin> pluginMap = new ConcurrentHashMap<>();
    private final Map<String, URLClassLoader> classLoaderMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (!pluginEnabled) {
            log.info("插件系统已禁用");
            return;
        }

        pluginDir = pluginDirConfig;
        
        File pluginDirectory = new File(pluginDir);
        if (!pluginDirectory.exists()) {
            boolean created = pluginDirectory.mkdirs();
            if (created) {
                log.info("创建插件目录: {}", pluginDir);
            } else {
                log.error("无法创建插件目录: {}", pluginDir);
            }
        }

        log.info("插件目录: {}", pluginDir);
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!pluginEnabled) {
            return;
        }
        log.info("所有Spring Bean初始化完成，开始加载插件...");
        loadPluginsFromDatabase();
    }

    private void loadPluginsFromDatabase() {
        QueryWrapper<SysPlugin> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "enabled")
               .eq("del_flag", 0);

        List<SysPlugin> plugins = sysPluginService.list(wrapper);
        log.info("从数据库加载 {} 个插件记录", plugins.size());

        for (SysPlugin plugin : plugins) {
            try {
                File jarFile = new File(plugin.getJarPath());
                if (!jarFile.exists()) {
                    log.warn("插件JAR文件不存在: {}", plugin.getJarPath());
                    continue;
                }

                loadPluginFromJar(jarFile, plugin.getPluginType());
                plugin.setLoaded(true);
                sysPluginService.updateById(plugin);
            } catch (Exception e) {
                log.error("加载插件失败: {}", plugin.getPluginName(), e);
                plugin.setLoaded(false);
                sysPluginService.updateById(plugin);
            }
        }

        log.info("插件加载完成，共加载 {} 个插件", pluginMap.size());
    }

    public R<String> uploadAndLoad(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".jar")) {
            return R.fail("只支持JAR文件");
        }

        try {
            long fileSize = file.getSize();
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = Paths.get(pluginDir, fileName);
            Files.createDirectories(filePath.getParent());
            
            byte[] bytes = file.getBytes();
            Files.write(filePath, bytes);

            log.info("插件JAR已保存: {}", filePath);

            NodePlugin plugin = loadPluginFromJar(filePath.toFile(), null);
            if (plugin == null) {
                Files.deleteIfExists(filePath);
                return R.fail("JAR包中未找到有效的插件实现");
            }

            SysPlugin sysPlugin = new SysPlugin();
            sysPlugin.setPluginName(plugin.getNodeName());
            sysPlugin.setPluginType(plugin.getNodeType());
            sysPlugin.setPluginVersion("1.0.0");
            sysPlugin.setCategory(plugin.getCategory());
            sysPlugin.setDescription(plugin.getDescription());
            sysPlugin.setJarFile(originalFilename);
            sysPlugin.setJarPath(filePath.toString());
            sysPlugin.setFileSize(fileSize);
            sysPlugin.setIcon(plugin.getIcon());
            sysPlugin.setStatus("enabled");
            sysPlugin.setLoaded(true);
            sysPlugin.setConfigTemplate(plugin.getConfigTemplate());

            QueryWrapper<SysPlugin> wrapper = new QueryWrapper<>();
            wrapper.eq("plugin_type", plugin.getNodeType());
            SysPlugin existing = sysPluginService.getOne(wrapper);
            
            if (existing != null) {
                sysPlugin.setId(existing.getId());
                sysPluginService.updateById(sysPlugin);
                log.info("更新已存在的插件: {}", plugin.getNodeType());
            } else {
                sysPluginService.save(sysPlugin);
                log.info("保存新插件: {}", plugin.getNodeType());
            }

            return R.ok("插件上传并加载成功");

        } catch (Exception e) {
            log.error("插件上传失败", e);
            return R.fail("插件上传失败: " + e.getMessage());
        }
    }

    public NodePlugin loadPluginFromJar(File jarFile, String expectedType) throws MalformedURLException {
        log.info("加载插件JAR: {}", jarFile.getName());

        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{jarUrl},
            getClass().getClassLoader()
        );

        ServiceLoader<NodePlugin> serviceLoader = ServiceLoader.load(NodePlugin.class, classLoader);
        Iterator<NodePlugin> iterator = serviceLoader.iterator();

        NodePlugin loadedPlugin = null;
        while (iterator.hasNext()) {
            try {
                NodePlugin plugin = iterator.next();
                String nodeType = plugin.getNodeType();

                if (expectedType != null && !expectedType.equals(nodeType)) {
                    continue;
                }

                if (pluginMap.containsKey(nodeType)) {
                    log.warn("插件类型冲突: {} 已存在，将被覆盖", nodeType);
                    unloadPlugin(nodeType);
                }

                // 初始化插件，注入 Spring 上下文
                if (applicationContext != null) {
                    try {
                        plugin.init(applicationContext);
                        log.info("插件初始化完成: type={}", nodeType);
                    } catch (Exception e) {
                        log.error("插件初始化失败: type={}, error={}", nodeType, e.getMessage(), e);
                    }
                } else {
                    log.warn("ApplicationContext 未就绪，插件 {} 未初始化", nodeType);
                }

                pluginMap.put(nodeType, plugin);
                classLoaderMap.put(nodeType, classLoader);
                loadedPlugin = plugin;

                log.info("注册插件: type={}, name={}, category={}", 
                    nodeType, plugin.getNodeName(), plugin.getCategory());
            } catch (Error e) {
                log.error("实例化插件失败: {}", jarFile.getName(), e);
            }
        }

        return loadedPlugin;
    }

    public boolean unloadPlugin(String nodeType) {
        URLClassLoader classLoader = classLoaderMap.remove(nodeType);
        if (classLoader != null) {
            try {
                classLoader.close();
                log.info("关闭插件ClassLoader: {}", nodeType);
            } catch (IOException e) {
                log.error("关闭ClassLoader失败", e);
            }
        }

        NodePlugin removed = pluginMap.remove(nodeType);
        if (removed != null) {
            log.info("卸载插件: {}", nodeType);
            
            QueryWrapper<SysPlugin> wrapper = new QueryWrapper<>();
            wrapper.eq("plugin_type", nodeType);
            SysPlugin sysPlugin = sysPluginService.getOne(wrapper);
            if (sysPlugin != null) {
                sysPlugin.setLoaded(false);
                sysPluginService.updateById(sysPlugin);
            }
            return true;
        }
        return false;
    }

    public boolean deletePlugin(String nodeType) {
        unloadPlugin(nodeType);

        QueryWrapper<SysPlugin> wrapper = new QueryWrapper<>();
        wrapper.eq("plugin_type", nodeType);
        SysPlugin sysPlugin = sysPluginService.getOne(wrapper);
        
        if (sysPlugin != null) {
            try {
                Path jarPath = Paths.get(sysPlugin.getJarPath());
                Files.deleteIfExists(jarPath);
                log.info("删除插件JAR文件: {}", jarPath);
            } catch (IOException e) {
                log.error("删除JAR文件失败", e);
            }

            sysPluginService.removeById(sysPlugin.getId());
            return true;
        }
        return false;
    }

    public NodePlugin getPlugin(String nodeType) {
        return pluginMap.get(nodeType);
    }

    public boolean hasPlugin(String nodeType) {
        return pluginMap.containsKey(nodeType);
    }

    public Map<String, NodePlugin> getAllPlugins() {
        return Collections.unmodifiableMap(pluginMap);
    }

    public int getPluginCount() {
        return pluginMap.size();
    }
}
