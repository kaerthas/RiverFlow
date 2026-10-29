package com.riverflow.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 应用实例监控
 * 每个实例定时将本机 CPU/内存指标写入共享 Redis（带 TTL），
 * 在线实例数 = 未过期的 key 数，实例宕机后约 30s 自动从列表消失。
 * CPU 使用率采用差分法计算（两次采样间的 CPU 时间差 / 墙钟时间差），
 * 避免部分平台 OperatingSystemMXBean.getProcessCpuLoad() 持续返回 -1 导致无数据。
 */
@Slf4j
@Service
public class InstanceMonitorService {

    private static final String KEY_PREFIX = "riverflow:monitor:instance:";
    private static final String HISTORY_KEY_PREFIX = "riverflow:monitor:cpu:history:";
    private static final long KEY_TTL_SECONDS = 30;
    private static final int HISTORY_MAX_POINTS = 150;
    private static final long HISTORY_TTL_SECONDS = 60;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final boolean IS_LINUX = System.getProperty("os.name", "").toLowerCase().contains("linux");

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${server.port:8080}")
    private int serverPort;

    /** 是否开启实例监控采集上报 */
    @Value("${riverflow.monitor.enabled:true}")
    private boolean monitorEnabled;

    /** 是否记录 CPU/内存历史趋势数据 */
    @Value("${riverflow.monitor.history-enabled:true}")
    private boolean historyEnabled;

    private String instanceId;
    private String startTime;

    /** 上一次采样的墙钟时间（纳秒）与进程 CPU 时间（纳秒） */
    private volatile long lastNanoTime = -1;
    private volatile long lastProcessCpuTime = -1;
    /** 上一次 /proc/stat 采样的总 jiffies 与空闲 jiffies */
    private volatile long lastProcStatTotal = -1;
    private volatile long lastProcStatIdle = -1;
    /** oshi 中央处理器与上一次系统 ticks，初始化失败时为 null（自动回退 MXBean） */
    private oshi.hardware.CentralProcessor centralProcessor;
    private volatile long[] prevCpuTicks;
    /** oshi 物理内存视图，初始化失败时为 null（自动回退 MXBean 口径） */
    private oshi.hardware.GlobalMemory globalMemory;

    @PostConstruct
    public void init() {
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            ip = "127.0.0.1";
        }
        this.instanceId = ip + ":" + serverPort;
        this.startTime = LocalDateTime.now().format(TIME_FMT);
        try {
            oshi.SystemInfo systemInfo = new oshi.SystemInfo();
            centralProcessor = systemInfo.getHardware().getProcessor();
            globalMemory = systemInfo.getHardware().getMemory();
            prevCpuTicks = centralProcessor.getSystemCpuLoadTicks();
        } catch (Throwable t) {
            log.warn("oshi 初始化失败，系统CPU/物理内存将回退到 MXBean 采集: {}", t.getMessage());
        }
        log.info("实例监控已启动: instanceId={}", instanceId);
        report();
    }

    /**
     * 定时采集并上报本实例指标
     */
    @Scheduled(initialDelay = 2000, fixedDelay = 2000)
    public void report() {
        if (!monitorEnabled) {
            return;
        }
        try {
            Map<String, String> metrics = new HashMap<>();
            metrics.put("instanceId", instanceId);
            metrics.put("startTime", startTime);
            metrics.put("reportTime", LocalDateTime.now().format(TIME_FMT));

            com.sun.management.OperatingSystemMXBean osBean =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

            Double processCpu = sampleProcessCpu(osBean);
            if (processCpu != null) {
                metrics.put("processCpu", String.valueOf(processCpu));
            }
            Double systemCpu = sampleSystemCpu(osBean);
            if (systemCpu != null) {
                metrics.put("systemCpu", String.valueOf(systemCpu));
            }

            long sysMemTotal;
            long sysMemUsed;
            if (globalMemory != null) {
                // OSHI 的 available 在 Linux 上取 MemAvailable（含可回收页缓存），
                // MXBean 的 free 只取 MemFree，会把页缓存算作已用导致常年接近 100%
                sysMemTotal = globalMemory.getTotal();
                sysMemUsed = sysMemTotal - globalMemory.getAvailable();
            } else {
                sysMemTotal = osBean.getTotalPhysicalMemorySize();
                sysMemUsed = sysMemTotal - osBean.getFreePhysicalMemorySize();
            }
            metrics.put("sysMemTotal", String.valueOf(sysMemTotal));
            metrics.put("sysMemUsed", String.valueOf(sysMemUsed));

            Runtime runtime = Runtime.getRuntime();
            long jvmUsed = runtime.totalMemory() - runtime.freeMemory();
            long jvmMax = runtime.maxMemory();
            metrics.put("jvmUsed", String.valueOf(jvmUsed));
            metrics.put("jvmMax", String.valueOf(jvmMax));

            String key = KEY_PREFIX + instanceId;
            stringRedisTemplate.opsForHash().putAll(key, metrics);
            stringRedisTemplate.expire(key, KEY_TTL_SECONDS, TimeUnit.SECONDS);

            Double jvmRatio = jvmMax > 0 ? (double) jvmUsed / jvmMax : null;
            Double sysMemRatio = sysMemTotal > 0 ? (double) sysMemUsed / sysMemTotal : null;
            appendHistory(processCpu, systemCpu, jvmRatio, sysMemRatio);
        } catch (Exception e) {
            log.warn("实例监控指标上报失败: instanceId={}, error={}", instanceId, e.getMessage());
        }
    }

    /**
     * 进程 CPU 使用率（0-1）：差分法，两次采样间的进程 CPU 时间差 / (墙钟时间差 × 核数)。
     * 首次采样无上次值时返回 null，下一次采样即有数据。
     */
    private Double sampleProcessCpu(com.sun.management.OperatingSystemMXBean osBean) {
        long now = System.nanoTime();
        long cpuTime = osBean.getProcessCpuTime();
        Double result = null;
        if (cpuTime >= 0 && lastProcessCpuTime >= 0) {
            long cpuDelta = cpuTime - lastProcessCpuTime;
            long timeDelta = now - lastNanoTime;
            int cores = Runtime.getRuntime().availableProcessors();
            if (timeDelta > 0) {
                result = (double) cpuDelta / ((double) timeDelta * cores);
                if (result < 0) {
                    result = 0.0;
                } else if (result > 1) {
                    result = 1.0;
                }
            }
        }
        if (cpuTime >= 0) {
            lastProcessCpuTime = cpuTime;
            lastNanoTime = now;
        }
        return result;
    }

    /**
     * 系统 CPU 使用率（0-1）：优先 oshi（JNA 直调系统 API），
     * 不可用回退 MXBean，再失败 Linux 下用 /proc/stat 差分兜底
     */
    private Double sampleSystemCpu(com.sun.management.OperatingSystemMXBean osBean) {
        Double oshiLoad = oshiSystemCpu();
        if (oshiLoad != null) {
            return oshiLoad;
        }
        double load = osBean.getSystemCpuLoad();
        if (load >= 0 && !Double.isNaN(load)) {
            return load;
        }
        return linuxProcStatCpu();
    }

    /**
     * oshi 差分计算系统 CPU 使用率，初始化失败或读取异常返回 null
     */
    private synchronized Double oshiSystemCpu() {
        if (centralProcessor == null) {
            return null;
        }
        try {
            long[] ticks = centralProcessor.getSystemCpuLoadTicks();
            long[] prev = prevCpuTicks;
            prevCpuTicks = ticks;
            if (prev == null) {
                return null;
            }
            double load = centralProcessor.getSystemCpuLoadBetweenTicks(prev);
            return (load >= 0 && !Double.isNaN(load)) ? load : null;
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Linux /proc/stat 差分计算系统 CPU 使用率，非 Linux 或读取失败返回 null
     */
    private Double linuxProcStatCpu() {
        if (!IS_LINUX) {
            return null;
        }
        try {
            String cpuLine = null;
            for (String line : Files.readAllLines(Paths.get("/proc/stat"))) {
                if (line.startsWith("cpu ")) {
                    cpuLine = line;
                    break;
                }
            }
            if (cpuLine == null) {
                return null;
            }
            String[] parts = cpuLine.trim().split("\\s+");
            long idle = Long.parseLong(parts[4]) + Long.parseLong(parts[5]);
            long total = 0;
            for (int i = 1; i < parts.length; i++) {
                total += Long.parseLong(parts[i]);
            }
            Double result = null;
            if (lastProcStatTotal >= 0) {
                long totalDelta = total - lastProcStatTotal;
                long idleDelta = idle - lastProcStatIdle;
                if (totalDelta > 0) {
                    result = 1.0 - (double) idleDelta / totalDelta;
                    if (result < 0) {
                        result = 0.0;
                    }
                }
            }
            lastProcStatTotal = total;
            lastProcStatIdle = idle;
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将本次采样点写入 Redis 历史序列（最新在前），仅保留最近 HISTORY_MAX_POINTS 个点。
     * 各比率字段均为 0-1 小数，缺测为 null：p=进程CPU s=系统CPU jm=JVM堆内存 sm=物理内存
     */
    private void appendHistory(Double processCpu, Double systemCpu, Double jvmRatio, Double sysMemRatio) {
        if (!historyEnabled) {
            return;
        }
        try {
            Map<String, Object> point = new HashMap<>();
            point.put("t", System.currentTimeMillis() / 1000);
            point.put("p", processCpu);
            point.put("s", systemCpu);
            point.put("jm", jvmRatio);
            point.put("sm", sysMemRatio);
            String hKey = HISTORY_KEY_PREFIX + instanceId;
            stringRedisTemplate.opsForList().leftPush(hKey, OBJECT_MAPPER.writeValueAsString(point));
            stringRedisTemplate.opsForList().trim(hKey, 0, HISTORY_MAX_POINTS - 1);
            stringRedisTemplate.expire(hKey, HISTORY_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("CPU历史数据写入失败: instanceId={}, error={}", instanceId, e.getMessage());
        }
    }

    /**
     * 查询指定实例的资源使用历史采样点（时间正序）
     */
    public List<Map<String, Object>> cpuHistory(String instId) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            List<String> raw = stringRedisTemplate.opsForList().range(HISTORY_KEY_PREFIX + instId, 0, -1);
            if (raw == null || raw.isEmpty()) {
                return list;
            }
            Collections.reverse(raw);
            for (String s : raw) {
                @SuppressWarnings("unchecked")
                Map<String, Object> point = OBJECT_MAPPER.readValue(s, Map.class);
                list.add(point);
            }
        } catch (Exception e) {
            log.warn("查询CPU历史数据失败: {}", e.getMessage());
        }
        return list;
    }

    /**
     * 汇总所有在线实例的指标
     */
    public List<Map<String, String>> listInstances() {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            Set<String> keys = stringRedisTemplate.keys(KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return list;
            }
            for (String key : keys) {
                Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
                if (entries == null || entries.isEmpty()) {
                    continue;
                }
                Map<String, String> item = new HashMap<>();
                entries.forEach((k, v) -> item.put(String.valueOf(k), String.valueOf(v)));
                item.putIfAbsent("instanceId", key.substring(KEY_PREFIX.length()));
                list.add(item);
            }
            list.sort(Comparator.comparing(m -> m.get("instanceId")));
        } catch (Exception e) {
            log.warn("查询实例监控数据失败: {}", e.getMessage());
        }
        return list;
    }
}
