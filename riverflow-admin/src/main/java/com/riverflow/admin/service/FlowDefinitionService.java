package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.FlowDefinition;

/**
 * 流程定义 Service
 */
public interface FlowDefinitionService extends IService<FlowDefinition> {

    /**
     * 获取某流程编码的最大版本号
     */
    Integer getMaxVersion(String flowCode);

    /**
     * 获取某流程编码最新已发布的版本
     */
    FlowDefinition getLatestPublished(String flowCode);

    /**
     * 复制指定流程定义为新版本（草稿状态）
     * @param id 源流程定义ID
     * @return 新流程定义ID
     */
    Long copyAsNewVersion(Long id);

    /**
     * 复制指定流程定义为全新流程（新流程编码，草稿状态）
     * @param id 源流程定义ID
     * @param newFlowCode 新流程编码（必须唯一）
     * @param newFlowName 新流程名称
     * @return 新流程定义ID
     */
    Long duplicateAsNewFlow(Long id, String newFlowCode, String newFlowName);
}
