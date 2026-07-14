package com.riverflow.admin.infra.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 当前节点标识工具
 *
 * <p>用于分布式调度时标记哪个节点认领/执行了任务。</p>
 */
@Slf4j
public class NodeIdUtil {

    private static final String NODE_ID;

    static {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("无法获取主机名，使用默认值", e);
            host = "unknown";
        }
        NODE_ID = host + "-" + System.nanoTime();
    }

    /**
     * 获取当前节点唯一标识（主机名 + 启动时纳秒时间戳）
     */
    public static String getNodeId() {
        return NODE_ID;
    }
}
