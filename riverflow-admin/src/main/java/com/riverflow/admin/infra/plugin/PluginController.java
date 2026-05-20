package com.riverflow.admin.infra.plugin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.SysPluginService;
import com.riverflow.api.entity.SysPlugin;
import com.riverflow.api.plugin.NodePlugin;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件管理接口
 * 提供插件上传、查询、启用/禁用、删除等功能
 */
@Slf4j
@RestController
@RequestMapping("/plugin")
public class PluginController {

    @Autowired
    private NodePluginLoader pluginLoader;

    @Autowired
    private SysPluginService sysPluginService;

    @PostMapping("/upload")
    public R<String> uploadPlugin(@RequestParam("file") MultipartFile file) {
        return pluginLoader.uploadAndLoad(file);
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

        for (Map.Entry<String, NodePlugin> entry : pluginLoader.getAllPlugins().entrySet()) {
            NodePlugin plugin = entry.getValue();
            Map<String, Object> pluginInfo = new HashMap<>();
            pluginInfo.put("nodeType", plugin.getNodeType());
            pluginInfo.put("nodeName", plugin.getNodeName());
            pluginInfo.put("icon", plugin.getIcon());
            pluginInfo.put("category", plugin.getCategory());
            pluginInfo.put("description", plugin.getDescription());
            pluginInfo.put("configTemplate", plugin.getConfigTemplate());
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
        
        NodePlugin plugin = pluginLoader.getPlugin(nodeType);
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
            java.io.File jarFile = new java.io.File(plugin.getJarPath());
            pluginLoader.loadPluginFromJar(jarFile, plugin.getPluginType());
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

        pluginLoader.unloadPlugin(plugin.getPluginType());
        return R.ok("插件已禁用");
    }

    @DeleteMapping("/delete/{id}")
    public R<String> deletePlugin(@PathVariable Long id) {
        SysPlugin plugin = sysPluginService.getById(id);
        if (plugin == null) {
            return R.fail("插件不存在");
        }

        boolean success = pluginLoader.deletePlugin(plugin.getPluginType());
        if (success) {
            return R.ok("插件删除成功");
        } else {
            return R.fail("插件删除失败");
        }
    }

    @PostMapping("/reload/{id}")
    public R<String> reloadPlugin(@PathVariable Long id) {
        SysPlugin plugin = sysPluginService.getById(id);
        if (plugin == null) {
            return R.fail("插件不存在");
        }

        try {
            pluginLoader.unloadPlugin(plugin.getPluginType());
            
            java.io.File jarFile = new java.io.File(plugin.getJarPath());
            pluginLoader.loadPluginFromJar(jarFile, plugin.getPluginType());
            
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
