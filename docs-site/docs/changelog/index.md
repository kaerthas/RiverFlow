# 更新日志

## v1.0.0 (2024-05)

### 新增

- 可视化流程设计器（基于 LogicFlow）
- 7 种内置节点类型：start、end、api、db、script、condition、timer
- Java SPI 插件系统，支持运行时热加载
- 流程版本管理，发布新版本不影响运行中实例
- 分布式任务调度（基于 Redis 分布式锁）
- 动态数据源切换
- API 目录管理
- Groovy 脚本执行（沙箱安全）

### 插件

- `river-minio`：MinIO 文件存储插件
- `river-hw-auth`：华为云 Token 认证插件（支持 HmacSHA256 签名 + Redis 缓存）

### 优化

- 插件 `init(ApplicationContext)` 方法，支持获取 Spring 管理的 Bean
