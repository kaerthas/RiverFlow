# 常见问题

## 流程执行

### Q1: 流程执行卡住了，如何排查？

1. 检查 `wf_flow_task` 表，查看任务状态是否为 `pending` 或 `waiting`
2. 检查 `wf_flow_instance` 表，查看实例状态是否为 `running`
3. 查看日志中 `FlowScheduler` 的扫描记录
4. 检查 Redis 连接是否正常（分布式锁依赖 Redis）

### Q2: 节点执行报错，如何查看详细错误？

1. 在流程实例详情页查看执行日志
2. 查看后端日志中 `FlowEngine.executeNode` 的异常堆栈
3. Script 节点的错误会在 `ScriptNodeExecutor` 日志中输出

### Q3: 上下文变量没有传递成功？

1. 检查 `outputMapping` 配置是否正确，`source` 和 `target` 路径是否匹配
2. 检查变量名是否拼写正确，区分大小写
3. 确认上游节点执行成功（失败节点的输出不会写入上下文）

## 插件相关

### Q4: 插件可以使用 Spring 管理的 Bean 吗？

可以。从 v1.0.0 开始，插件实现 `init(ApplicationContext)` 方法后，可通过 `applicationContext.getBean()` 获取主项目的任意 Spring Bean。详见 [插件开发指南](/guide/plugin/develop-guide.html)。

### Q5: 插件加载失败怎么办？

1. 检查 JAR 包是否在 plugins 目录
2. 检查 SPI 配置文件 `META-INF/services/com.riverflow.api.plugin.NodePlugin` 是否正确
3. 检查实现类是否实现 `NodePlugin` 接口
4. 查看启动日志中的错误信息

### Q6: 多个流程共用同一个 Token，如何管理？

推荐使用 `river-hw-auth` 插件，它内置了：
- Token 缓存（Redis）
- 过期前自动刷新
- 分布式锁防止并发刷新

流程设计：`[start] → [hw-auth] → [api-业务接口] → [end]`

## 部署相关

### Q7: 单机部署和分布式部署有什么区别？

| 特性 | 单机部署 | 分布式部署 |
|------|---------|-----------|
| 适用场景 | 开发测试、小规模 | 生产环境、高并发 |
| 调度器 | 单实例扫描 | 多实例扫描 + Redis 分布式锁 |
| 数据库压力 | 较小 | 需要优化（引入行锁/缓存） |
| 配置复杂度 | 简单 | 需要配置 Redis、负载均衡 |

详见 [分布式部署指南](/guide/deploy/distributed-deploy.html)。

### Q8: 如何升级流程而不影响正在运行的实例？

RiverFlow 支持流程版本管理：
1. 修改流程定义后点击「保存并发布」
2. 系统自动创建新版本，旧版本自动下线
3. 已运行的实例绑定到具体版本，不受影响
4. 新启动的实例使用最新版本
