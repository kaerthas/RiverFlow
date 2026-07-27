package com.riverflow.ai.service;

import com.riverflow.ai.mapper.SysPluginTypeMapper;
import com.riverflow.api.modules.workflow.validate.FlowValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * AI 模块节点插件类型同步加载器
 *
 * <p>AI 服务独立部署时，不会执行 admin 模块的 {@code NodePluginLoader}，
 * 因此 {@link FlowValidator} 无法识别插件节点类型（如 {@code hw-auth}）。</p>
 *
 * <p>本组件在 AI 模块启动后从数据库读取已启用的节点插件类型，
 * 并注册到 {@link FlowValidator}，使 AI 生成的流程校验能识别这些节点。</p>
 */
@Slf4j
@Component
public class PluginTypeLoader implements SmartInitializingSingleton {

    private final SysPluginTypeMapper sysPluginTypeMapper;
    private final FlowValidator flowValidator;

    @Autowired(required = false)
    public PluginTypeLoader(SysPluginTypeMapper sysPluginTypeMapper, FlowValidator flowValidator) {
        this.sysPluginTypeMapper = sysPluginTypeMapper;
        this.flowValidator = flowValidator;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (sysPluginTypeMapper == null || flowValidator == null) {
            log.warn("[PluginTypeLoader] 依赖缺失，跳过插件类型注册");
            return;
        }

        try {
            List<String> nodePluginTypes = sysPluginTypeMapper.selectNodePluginTypes();
            if (nodePluginTypes == null || nodePluginTypes.isEmpty()) {
                log.info("[PluginTypeLoader] 未从数据库检索到已启用节点插件类型");
                return;
            }

            flowValidator.registerPluginNodeTypes(nodePluginTypes);
            log.info("[PluginTypeLoader] 已从数据库同步 {} 个节点插件类型到 FlowValidator: {}",
                    nodePluginTypes.size(), nodePluginTypes);
        } catch (Exception e) {
            log.error("[PluginTypeLoader] 从数据库同步节点插件类型失败", e);
        }
    }

    /**
     * 手动刷新插件类型（可用于外部触发同步）
     */
    public void refresh() {
        afterSingletonsInstantiated();
    }
}
