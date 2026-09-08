package com.riverflow.admin.modules.workflow.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.modules.workflow.engine.FlowInstanceStarter;
import com.riverflow.admin.service.FlowDefinitionService;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.api.entity.FlowDefinition;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.enums.FlowInstanceStatusEnum;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 流程级定时触发调度器
 * 周期性刷新"已发布 + triggerType=cron"的流程定义，动态注册 cron 触发任务，
 * 到点后启动流程实例（与手动启动共用 FlowInstanceStarter 链路，由 FlowScheduler 接力驱动节点执行）。
 * <p>
 * 分布式安全：
 * 1. ShedLock 编程式锁保证多节点部署时同一时刻只有一个节点触发；
 * 2. Redis 分钟级幂等键作为第二道防线，防止锁失效等极端场景下重复创建实例。
 * <p>
 * 重叠策略：若该流程上一次触发的实例仍在运行，则跳过本次触发，避免实例堆积。
 */
@Slf4j
@Component
public class FlowCronScheduler {

    private static final DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    @Autowired
    private FlowDefinitionService flowDefinitionService;
    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowInstanceStarter flowInstanceStarter;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private LockProvider lockProvider;

    /**
     * 动态 cron 任务调度器（独立于 @Scheduled 线程池，避免互相阻塞）
     */
    private ThreadPoolTaskScheduler cronTaskScheduler;
    private LockingTaskExecutor lockingTaskExecutor;

    /**
     * 已注册的定时任务：flowId -> ScheduledFuture
     */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    /**
     * 已注册的 cron 表达式：flowId -> cron（用于变更检测）
     */
    private final Map<Long, String> registeredCrons = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        cronTaskScheduler = new ThreadPoolTaskScheduler();
        cronTaskScheduler.setPoolSize(4);
        cronTaskScheduler.setThreadNamePrefix("flow-cron-");
        cronTaskScheduler.setWaitForTasksToCompleteOnShutdown(false);
        cronTaskScheduler.initialize();
        lockingTaskExecutor = new DefaultLockingTaskExecutor(lockProvider);
    }

    @PreDestroy
    public void destroy() {
        if (cronTaskScheduler != null) {
            cronTaskScheduler.shutdown();
        }
    }

    /**
     * 定时刷新定时流程注册表：新增注册、变更重注册、下线/删除取消。
     * 每个节点都需执行刷新（cron 任务注册在节点本地），故不加 ShedLock。
     */
    @Scheduled(fixedDelayString = "${riverflow.cron.refresh-interval:30000}", initialDelay = 15000)
    public void refreshCronTasks() {
        try {
            List<FlowDefinition> cronFlows = flowDefinitionService.list(new QueryWrapper<FlowDefinition>()
                    .eq("del_flag", 0)
                    .eq("status", 1)
                    .eq("trigger_type", "cron")
                    .eq("cron_enabled", 1)
                    .isNotNull("trigger_config")
                    .ne("trigger_config", ""));

            Set<Long> activeFlowIds = new HashSet<>();
            for (FlowDefinition def : cronFlows) {
                String cron = def.getTriggerConfig() == null ? "" : def.getTriggerConfig().trim();
                if (!CronExpression.isValidExpression(cron)) {
                    log.warn("定时流程 cron 表达式不合法，跳过注册: flowId={}, flowCode={}, cron={}",
                            def.getId(), def.getFlowCode(), cron);
                    continue;
                }
                activeFlowIds.add(def.getId());
                if (cron.equals(registeredCrons.get(def.getId()))) {
                    continue;
                }
                register(def, cron);
            }

            // 已下线 / 已删除 / 改为非定时触发 / cron 非法的流程：取消注册
            for (Long flowId : new ArrayList<>(scheduledTasks.keySet())) {
                if (!activeFlowIds.contains(flowId)) {
                    cancel(flowId, "流程已下线或定时配置已变更");
                }
            }
        } catch (Exception e) {
            log.error("刷新定时流程任务异常", e);
        }
    }

    private void register(FlowDefinition def, String cron) {
        cancel(def.getId(), "cron 配置变更");
        ScheduledFuture<?> future = cronTaskScheduler.schedule(() -> fire(def.getId()), new CronTrigger(cron));
        if (future == null) {
            log.error("定时流程注册失败: flowId={}, cron={}", def.getId(), cron);
            return;
        }
        scheduledTasks.put(def.getId(), future);
        registeredCrons.put(def.getId(), cron);
        log.info("注册定时流程: flowId={}, flowCode={}, cron={}", def.getId(), def.getFlowCode(), cron);
    }

    private void cancel(Long flowId, String reason) {
        ScheduledFuture<?> future = scheduledTasks.remove(flowId);
        registeredCrons.remove(flowId);
        if (future != null) {
            future.cancel(false);
            log.info("取消定时流程: flowId={}, 原因={}", flowId, reason);
        }
    }

    /**
     * cron 到点触发入口（每个节点都会执行，通过 ShedLock 保证只有一个节点真正触发）
     */
    private void fire(Long flowId) {
        FlowDefinition def = flowDefinitionService.getById(flowId);
        if (def == null || def.getStatus() == null || def.getStatus() != 1
                || !"cron".equals(def.getTriggerType())
                || def.getCronEnabled() == null || def.getCronEnabled() != 1) {
            return;
        }
        LockConfiguration lockConfig = new LockConfiguration(
                "FlowCronScheduler_fire_" + flowId, Instant.now().plus(Duration.ofMinutes(2)));
        try {
            // 锁未获取时 ShedLock 静默跳过；doFire 内的分钟级幂等键兜底防重复
            lockingTaskExecutor.executeWithLock((Runnable) () -> doFire(def), lockConfig);
        } catch (Exception e) {
            log.error("定时触发执行异常: flowId={}", flowId, e);
        }
    }

    private void doFire(FlowDefinition def) {
        // 分钟级幂等：防止分布式锁失效等极端场景下同一分钟内重复创建实例
        String minute = LocalDateTime.now().format(MINUTE_FORMAT);
        String idemKey = "riverflow:cron:fire:" + def.getId() + ":" + minute;
        Boolean firstFire = redisTemplate.opsForValue().setIfAbsent(idemKey, "1", Duration.ofMinutes(10));
        if (!Boolean.TRUE.equals(firstFire)) {
            log.info("定时流程本分钟已触发过，跳过: flowId={}, flowCode={}", def.getId(), def.getFlowCode());
            return;
        }

        // 重叠保护：上一次触发的实例仍在运行则跳过本次，避免实例堆积
        long runningCount = flowInstanceService.count(new QueryWrapper<FlowInstance>()
                .eq("flow_id", def.getId())
                .eq("status", FlowInstanceStatusEnum.RUNNING.getCode()));
        if (runningCount > 0) {
            log.warn("定时流程存在运行中的实例，跳过本次触发: flowId={}, flowCode={}, running={}",
                    def.getId(), def.getFlowCode(), runningCount);
            return;
        }

        String businessKey = "CRON_" + def.getFlowCode() + "_" + minute;
        log.info("定时触发流程: flowId={}, flowCode={}, businessKey={}", def.getId(), def.getFlowCode(), businessKey);
        try {
            flowInstanceStarter.start(def, businessKey, def.getItemCode(), "cron");
        } catch (Exception e) {
            log.error("定时触发流程启动失败: flowId={}, flowCode={}", def.getId(), def.getFlowCode(), e);
        }
    }
}
