package com.riverflow.admin.modules.workflow.node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 节点执行器工厂
 * 管理所有节点执行器，根据节点类型路由到对应实现
 */
@Slf4j
@Component
public class NodeExecutorFactory {

    @Autowired
    private List<NodeExecutor> executors;

    private final Map<String, NodeExecutor> executorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (NodeExecutor executor : executors) {
            executorMap.put(executor.getNodeType(), executor);
            log.info("注册节点执行器: type={}", executor.getNodeType());
        }
    }

    /**
     * 获取执行器
     */
    public NodeExecutor getExecutor(String nodeType) {
        NodeExecutor executor = executorMap.get(nodeType);
        if (executor == null) {
            throw new IllegalArgumentException("不支持的节点类型: " + nodeType);
        }
        return executor;
    }
}
