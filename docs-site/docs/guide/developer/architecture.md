# 架构设计

## 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue 3)                         │
│              LogicFlow 设计器 + Element Plus UI               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      控制器层 (REST API)                      │
│         FlowController / PluginController / ...              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                       业务层 (Service)                        │
│         FlowEngine / FlowScheduler / PluginLoader            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      数据层 (MyBatis Plus)                    │
│    FlowDefinition / FlowInstance / FlowTask / SysPlugin      │
└─────────────────────────────────────────────────────────────┘
```

## 核心模块

### FlowEngine

流程执行引擎，负责：
- 节点执行路由
- 上下文管理
- 结果转换

### FlowScheduler

流程任务调度器，基于 `@Scheduled` 定时扫描任务表：
- 扫描间隔：10 秒
- 线程池：核心 4，最大 16，队列 200
- 分布式锁：按 `instanceId` 加锁

### TransitionEngine

流程流转引擎，负责：
- 边优先级排序
- 条件表达式求值
- 下一节点创建

### NodePluginLoader

插件加载器，负责：
- 从数据库加载插件记录
- 使用 URLClassLoader 加载 JAR
- 调用 `init(ApplicationContext)` 初始化插件
- 支持热加载、热卸载

## 数据模型

```
FlowDefinition（流程定义）
  ├── FlowNode（节点定义）
  ├── FlowEdge（边定义）
  └── FlowInstance（流程实例）
        └── FlowTask（任务）
```

## 插件架构

```
┌─────────────────────────────────────────────────────────────┐
│                     主项目 ClassLoader                         │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              URLClassLoader（插件隔离）                  │  │
│  │  ┌─────────────┐    ┌─────────────┐                  │  │
│  │  │  Plugin A   │    │  Plugin B   │                  │  │
│  │  │  JAR + 类   │    │  JAR + 类   │                  │  │
│  │  └─────────────┘    └─────────────┘                  │  │
│  └───────────────────────────────────────────────────────┘  │
│                          ↑ parent                            │
│         插件可通过 init() 获取主项目 Spring Bean              │
└─────────────────────────────────────────────────────────────┘
```

## 执行流程

```
1. FlowScheduler 扫描 pending/waiting 任务
2. 提交到 ThreadPoolExecutor
3. FlowEngine.executeNode()
   a. Redis 分布式锁（按 instanceId）
   b. 二次校验（查库确认状态）
   c. NodeExecutorFactory 路由执行器
   d. 内置节点 → NodeExecutor 执行
      插件节点 → PluginExecutorAdapter → NodePlugin.execute()
   e. 节点执行成功 → TransitionEngine 流转
   f. 更新 instance.contextJson + 创建下一节点 pending 任务
```
