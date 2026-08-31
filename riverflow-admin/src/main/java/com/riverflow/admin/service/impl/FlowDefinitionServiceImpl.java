package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.FlowDefinitionMapper;
import com.riverflow.admin.service.FlowDefinitionService;
import com.riverflow.admin.service.FlowEdgeService;
import com.riverflow.admin.service.FlowNodeService;
import com.riverflow.api.entity.FlowDefinition;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlowDefinitionServiceImpl extends ServiceImpl<FlowDefinitionMapper, FlowDefinition> implements FlowDefinitionService {

    @Autowired
    private FlowNodeService flowNodeService;
    @Autowired
    private FlowEdgeService flowEdgeService;

    @Override
    public Integer getMaxVersion(String flowCode) {
        FlowDefinition def = getOne(
                new QueryWrapper<FlowDefinition>()
                        .eq("flow_code", flowCode)
                        .eq("del_flag", 0)
                        .orderByDesc("version")
                        .last("LIMIT 1")
        );
        return def == null ? 0 : def.getVersion();
    }

    @Override
    public FlowDefinition getLatestPublished(String flowCode) {
        return getOne(
                new QueryWrapper<FlowDefinition>()
                        .eq("flow_code", flowCode)
                        .eq("status", 1)
                        .eq("del_flag", 0)
                        .orderByDesc("version")
                        .last("LIMIT 1")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyAsNewVersion(Long id) {
        FlowDefinition source = getById(id);
        if (source == null) {
            throw new RuntimeException("流程定义不存在");
        }

        Integer maxVersion = getMaxVersion(source.getFlowCode());
        int newVersion = (maxVersion == null ? 0 : maxVersion) + 1;

        // 1. 复制流程定义
        FlowDefinition target = new FlowDefinition();
        target.setFlowCode(source.getFlowCode());
        target.setFlowName(source.getFlowName());
        target.setVersion(newVersion);
        target.setItemCode(source.getItemCode());
        target.setTriggerType(source.getTriggerType());
        target.setTriggerConfig(source.getTriggerConfig());
        target.setStatus(0); // 草稿
        target.setExecutionMode(source.getExecutionMode());
        target.setInputParams(source.getInputParams());
        target.setOutputParams(source.getOutputParams());
        target.setGraphJson(source.getGraphJson());
        target.setCreateTime(LocalDateTime.now());
        target.setUpdateTime(LocalDateTime.now());
        target.setDelFlag(0);
        save(target);

        // 2. 复制节点
        List<FlowNode> nodes = flowNodeService.getNodesByFlowId(id);
        if (nodes != null && !nodes.isEmpty()) {
            for (FlowNode node : nodes) {
                node.setId(null);
                node.setFlowId(target.getId());
                node.setCreateTime(LocalDateTime.now());
                node.setUpdateTime(LocalDateTime.now());
                node.setDelFlag(0);
            }
            flowNodeService.saveBatch(nodes);
        }

        // 3. 复制边
        List<FlowEdge> edges = flowEdgeService.getEdgesByFlowId(id);
        if (edges != null && !edges.isEmpty()) {
            for (FlowEdge edge : edges) {
                edge.setId(null);
                edge.setFlowId(target.getId());
                edge.setCreateTime(LocalDateTime.now());
                edge.setUpdateTime(LocalDateTime.now());
                edge.setDelFlag(0);
            }
            flowEdgeService.saveBatch(edges);
        }

        return target.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long duplicateAsNewFlow(Long id, String newFlowCode, String newFlowName) {
        FlowDefinition source = getById(id);
        if (source == null) {
            throw new RuntimeException("流程定义不存在");
        }
        if (newFlowCode == null || newFlowCode.trim().isEmpty()) {
            throw new RuntimeException("新流程编码不能为空");
        }
        if (newFlowName == null || newFlowName.trim().isEmpty()) {
            throw new RuntimeException("新流程名称不能为空");
        }
        long count = count(
                new QueryWrapper<FlowDefinition>()
                        .eq("flow_code", newFlowCode.trim())
                        .eq("del_flag", 0)
        );
        if (count > 0) {
            throw new RuntimeException("流程编码已存在: " + newFlowCode);
        }

        // 1. 复制流程定义为全新流程（草稿状态，版本从1开始）
        FlowDefinition target = new FlowDefinition();
        target.setFlowCode(newFlowCode.trim());
        target.setFlowName(newFlowName.trim());
        target.setVersion(1);
        target.setItemCode(source.getItemCode());
        target.setTriggerType(source.getTriggerType());
        target.setTriggerConfig(source.getTriggerConfig());
        target.setStatus(0);
        target.setExecutionMode(source.getExecutionMode());
        target.setInputParams(source.getInputParams());
        target.setOutputParams(source.getOutputParams());
        target.setGraphJson(source.getGraphJson());
        target.setCreateTime(LocalDateTime.now());
        target.setUpdateTime(LocalDateTime.now());
        target.setDelFlag(0);
        save(target);

        // 2. 复制节点
        List<FlowNode> nodes = flowNodeService.getNodesByFlowId(id);
        if (nodes != null && !nodes.isEmpty()) {
            for (FlowNode node : nodes) {
                node.setId(null);
                node.setFlowId(target.getId());
                node.setCreateTime(LocalDateTime.now());
                node.setUpdateTime(LocalDateTime.now());
                node.setDelFlag(0);
            }
            flowNodeService.saveBatch(nodes);
        }

        // 3. 复制边
        List<FlowEdge> edges = flowEdgeService.getEdgesByFlowId(id);
        if (edges != null && !edges.isEmpty()) {
            for (FlowEdge edge : edges) {
                edge.setId(null);
                edge.setFlowId(target.getId());
                edge.setCreateTime(LocalDateTime.now());
                edge.setUpdateTime(LocalDateTime.now());
                edge.setDelFlag(0);
            }
            flowEdgeService.saveBatch(edges);
        }

        return target.getId();
    }
}
