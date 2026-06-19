package com.riverflow.admin.infra.plugin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.SysPluginService;
import com.riverflow.api.entity.SysPlugin;
import com.riverflow.api.plugin.ApiPlugin;
import com.riverflow.api.plugin.NodePlugin;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件管理接口
 * 提供插件上传、查询、启用/禁用、删除等功能
 * 支持流程节点插件（NodePlugin）和接口插件（ApiPlugin）两种类型
 */
@Slf4j
@RestController
@RequestMapping("/plugin")
public class PluginController {

    @Autowired
    private NodePluginLoader nodePluginLoader;

    @Autowired
    private ApiPluginLoader apiPluginLoader;

    @Autowired
    private SysPluginService sysPluginService;

    @Autowired
    private PluginFileValidator pluginFileValidator;

    @Value("${riverflow.plugin.dir:${user.home}/riverflow/plugins}")
    private String pluginDir;

    @PostMapping("/upload")
    public R<String> uploadPlugin(@RequestParam("file") MultipartFile file) {
        R<String> validateResult = pluginFileValidator.validate(file);
        if (validateResult != null) {
            return validateResult;
        }

        String originalFilename = file.getOriginalFilename();
        Path filePath = null;
        try {
            long fileSize = file.getSize();
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            filePath = Paths.get(pluginDir, fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            log.info("插件JAR已保存: {}", filePath);

            File jarFile = filePath.toFile();

            // 1. 加载 NodePlugin
            NodePlugin nodePlugin = null;
            try {
                nodePlugin = nodePluginLoader.loadPluginFromJar(jarFile, null);
            } catch (Exception e) {
                log.warn("JAR中未找到NodePlugin实现: {}", originalFilename);
            }

            // 2. 加载 ApiPlugin
            ApiPlugin apiPlugin = null;
            try {
                apiPlugin = apiPluginLoader.loadPluginFromJar(jarFile, null);
            } catch (Exception e) {
                log.warn("JAR中未找到ApiPlugin实现: {}", originalFilename);
            }

            if (nodePlugin == null && apiPlugin == null) {
                Files.deleteIfExists(filePath);
                return R.fail("JAR包中未找到有效的插件实现（需实现 NodePlugin 或 ApiPlugin 接口）");
            }

            // 3. 确定 pluginScope
            String pluginScope;
            String pluginType;
            String pluginName;
            String category;
            String description;
            String icon;
            String configTemplate;

            if (nodePlugin != null && apiPlugin != null) {
                pluginScope = "both";
                pluginType = nodePlugin.getNodeType();
                pluginName = nodePlugin.getNodeName();
                category = nodePlugin.getCategory();
                description = nodePlugin.getDescription();
                icon = nodePlugin.getIcon();
                configTemplate = nodePlugin.getConfigTemplate();
            } else if (nodePlugin != null) {
                pluginScope = "node";
                pluginType = nodePlugin.getNodeType();
                pluginName = nodePlugin.getNodeName();
                category = nodePlugin.getCategory();
                description = nodePlugin.getDescription();
                icon = nodePlugin.getIcon();
                configTemplate = nodePlugin.getConfigTemplate();
            } else {
                pluginScope = "api";
                pluginType = apiPlugin.getPluginType();
                pluginName = apiPlugin.getPluginName();
                category = apiPlugin.getCategory();
                description = apiPlugin.getDescription();
                icon = apiPlugin.getIcon();
                configTemplate = apiPlugin.getConfigSchema();
            }

            // 4. 保存/更新数据库记录
            SysPlugin sysPlugin = new SysPlugin();
            sysPlugin.setPluginName(pluginName);
            sysPlugin.setPluginType(pluginType);
            sysPlugin.setPluginScope(pluginScope);
            sysPlugin.setPluginVersion("1.0.0");
            sysPlugin.setCategory(category);
            sysPlugin.setDescription(description);
            sysPlugin.setJarFile(originalFilename);
            sysPlugin.setJarPath(filePath.toString());
            sysPlugin.setFileSize(fileSize);
            sysPlugin.setIcon(icon);
            sysPlugin.setStatus("enabled");
            sysPlugin.setLoaded(true);
            sysPlugin.setConfigTemplate(configTemplate);

            QueryWrapper<SysPlugin> wrapper = new QueryWrapper<>();
            wrapper.eq("plugin_type", pluginType);
            SysPlugin existing = sysPluginService.getOne(wrapper);

            if (existing != null) {
                sysPlugin.setId(existing.getId());
                sysPluginService.updateById(sysPlugin);
                log.info("更新已存在的插件: {}, scope={}", pluginType, pluginScope);
            } else {
                sysPluginService.save(sysPlugin);
                log.info("保存新插件: {}, scope={}", pluginType, pluginScope);
            }

            return R.ok("插件上传并加载成功，作用域: " + pluginScope);

        } catch (Exception e) {
            log.error("插件上传失败", e);
            if (filePath != null) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException ignored) {
                }
            }
            return R.fail("插件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public R<Page<SysPlugin>> listPlugins(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String pluginName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {

        Page<SysPlugin> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysPlugin> wrapper = new QueryWrapper<>();

        if (pluginName != null && !pluginName.isEmpty()) {
            wrapper.like("plugin_name", pluginName);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        return R.ok(sysPluginService.page(page, wrapper));
    }

    @GetMapping("/detail/{id}")
    public R<SysPlugin> getPluginDetail(@PathVariable Long id) {
        SysPlugin plugin = sysPluginService.getById(id);
        if (plugin == null) {
            return R.fail("插件不存在");
        }
        return R.ok(plugin);
    }

    @GetMapping("/loaded")
    public R<Map<String, Object>> getLoadedPlugins() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> plugins = new ArrayList<>();

        for (Map.Entry<String, NodePlugin> entry : nodePluginLoader.getAllPlugins().entrySet()) {
            NodePlugin plugin = entry.getValue();
            Map<String, Object> pluginInfo = new HashMap<>();
            pluginInfo.put("nodeType", plugin.getNodeType());
            pluginInfo.put("nodeName", plugin.getNodeName());
            pluginInfo.put("icon", plugin.getIcon());
            pluginInfo.put("category", plugin.getCategory());
            pluginInfo.put("description", plugin.getDescription());
            pluginInfo.put("configTemplate", plugin.getConfigTemplate());
            pluginInfo.put("configSchema", plugin.getConfigSchema());
            pluginInfo.put("outputSchema", plugin.getOutputSchema());
            plugins.add(pluginInfo);
        }

        result.put("total", plugins.size());
        result.put("plugins", plugins);
        return R.ok(result);
    }

    @GetMapping("/api-loaded")
    public R<Map<String, Object>> getApiLoadedPlugins() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> plugins = new ArrayList<>();

        for (Map.Entry<String, ApiPlugin> entry : apiPluginLoader.getAllPlugins().entrySet()) {
            ApiPlugin plugin = entry.getValue();
            Map<String, Object> pluginInfo = new HashMap<>();
            pluginInfo.put("pluginType", plugin.getPluginType());
            pluginInfo.put("pluginName", plugin.getPluginName());
            pluginInfo.put("icon", plugin.getIcon());
            pluginInfo.put("category", plugin.getCategory());
            pluginInfo.put("description", plugin.getDescription());
            pluginInfo.put("configSchema", plugin.getConfigSchema());
            plugins.add(pluginInfo);
        }

        result.put("total", plugins.size());
        result.put("plugins", plugins);
        return R.ok(result);
    }

    @GetMapping("/template")
    public R<Map<String, Object>> getConfigTemplate(String nodeType) {
        Map<String, Object> result = new HashMap<>();

        NodePlugin plugin = nodePluginLoader.getPlugin(nodeType);
        if (plugin == null) {
            return R.fail("插件不存在: " + nodeType);
        }

        result.put("nodeType", plugin.getNodeType());
        result.put("nodeName", plugin.getNodeName());
        result.put("configTemplate", plugin.getConfigTemplate());
        return R.ok(result);
    }

    @PostMapping("/enable/{id}")
    public R<String> enablePlugin(@PathVariable Long id) {
        SysPlugin plugin = sysPluginService.getById(id);
        if (plugin == null) {
            return R.fail("插件不存在");
        }

        plugin.setStatus("enabled");
        sysPluginService.updateById(plugin);

        try {
            File jarFile = new File(plugin.getJarPath());
            String scope = plugin.getPluginScope();

            if (scope == null || "node".equals(scope) || "both".equals(scope)) {
                nodePluginLoader.loadPluginFromJar(jarFile, plugin.getPluginType());
            }
            if (scope == null || "api".equals(scope) || "both".equals(scope)) {
                apiPluginLoader.loadPluginFromJar(jarFile, plugin.getPluginType());
            }

            plugin.setLoaded(true);
            sysPluginService.updateById(plugin);
            return R.ok("插件启用成功");
        } catch (Exception e) {
            log.error("加载插件失败", e);
            return R.fail("插件启用失败: " + e.getMessage());
        }
    }

    @PostMapping("/disable/{id}")
    public R<String> disablePlugin(@PathVariable Long id) {
        SysPlugin plugin = sysPluginService.getById(id);
        if (plugin == null) {
            return R.fail("插件不存在");
        }

        plugin.setStatus("disabled");
        plugin.setLoaded(false);
        sysPluginService.updateById(plugin);

        String scope = plugin.getPluginScope();
        if (scope == null || "node".equals(scope) || "both".equals(scope)) {
            nodePluginLoader.unloadPlugin(plugin.getPluginType());
        }
        if (scope == null || "api".equals(scope) || "both".equals(scope)) {
            apiPluginLoader.unloadPlugin(plugin.getPluginType());
        }

        return R.ok("插件已禁用");
    }

    @DeleteMapping("/delete/{id}")
    public R<String> deletePlugin(@PathVariable Long id) {
        SysPlugin plugin = sysPluginService.getById(id);
        if (plugin == null) {
            return R.fail("插件不存在");
        }

        String scope = plugin.getPluginScope();
        String pluginType = plugin.getPluginType();

        if (scope == null || "node".equals(scope) || "both".equals(scope)) {
            nodePluginLoader.unloadPlugin(pluginType);
        }
        if (scope == null || "api".equals(scope) || "both".equals(scope)) {
            apiPluginLoader.unloadPlugin(pluginType);
        }

        sysPluginService.getBaseMapper().deleteById(id);
        log.info("删除插件记录: {}", pluginType);
        return R.ok("插件删除成功");
    }

    @PostMapping("/reload/{id}")
    public R<String> reloadPlugin(@PathVariable Long id) {
        SysPlugin plugin = sysPluginService.getById(id);
        if (plugin == null) {
            return R.fail("插件不存在");
        }

        try {
            String scope = plugin.getPluginScope();
            String pluginType = plugin.getPluginType();

            if (scope == null || "node".equals(scope) || "both".equals(scope)) {
                nodePluginLoader.unloadPlugin(pluginType);
            }
            if (scope == null || "api".equals(scope) || "both".equals(scope)) {
                apiPluginLoader.unloadPlugin(pluginType);
            }

            File jarFile = new File(plugin.getJarPath());

            if (scope == null || "node".equals(scope) || "both".equals(scope)) {
                nodePluginLoader.loadPluginFromJar(jarFile, pluginType);
            }
            if (scope == null || "api".equals(scope) || "both".equals(scope)) {
                apiPluginLoader.loadPluginFromJar(jarFile, pluginType);
            }

            plugin.setLoaded(true);
            sysPluginService.updateById(plugin);

            return R.ok("插件重新加载成功");
        } catch (Exception e) {
            log.error("重新加载插件失败", e);
            plugin.setLoaded(false);
            sysPluginService.updateById(plugin);
            return R.fail("插件重新加载失败: " + e.getMessage());
        }
    }

    @GetMapping("/categories")
    public R<List<String>> getCategories() {
        QueryWrapper<SysPlugin> wrapper = new QueryWrapper<>();
        wrapper.select("category").groupBy("category");
        List<SysPlugin> plugins = sysPluginService.list(wrapper);

        List<String> categories = new ArrayList<>();
        for (SysPlugin plugin : plugins) {
            if (plugin.getCategory() != null) {
                categories.add(plugin.getCategory());
            }
        }
        return R.ok(categories);
    }
}
