# 快速开始

## 项目介绍

RiverFlow（河狸流程编排平台）是一款面向政务场景的可视化流程编排与数据交换平台。它提供了拖拽式的流程设计器、丰富的内置节点类型、强大的插件扩展能力，以及分布式任务调度支持。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + LogicFlow |
| 后端 | Spring Boot 2.7 + MyBatis Plus |
| 数据库 | MySQL 8.0 + Druid |
| 缓存 | Redis |
| 脚本 | Groovy |

## 环境要求

- JDK 1.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+

## 项目结构

```
RiverFlow/
├── riverflow-common/     # 公共模块：工具类、常量、异常
├── riverflow-api/        # API 契约：实体、DTO、枚举
├── riverflow-admin/      # 管理后台：Spring Boot 核心应用
├── river-minio/          # MinIO 存储插件示例
├── river-hw-auth/        # 华为云 Token 认证插件示例
├── docs-site/            # 文档站点
├── db/                   # 数据库初始化脚本
└── docs/                 # 项目文档
```

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < db/riverflow_init.sql
mysql -u root -p < db/init_data.sql
```

### 2. 配置 Redis

修改 `riverflow-admin/src/main/resources/application.yml`：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

### 3. 启动后端

```bash
mvn clean install -DskipTests
cd riverflow-admin
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd riverflow-ui
npm install
npm run dev
```

访问 `http://localhost:8080` 即可进入管理后台。

## 第一个流程

1. 进入「流程设计」页面，点击「新建流程」
2. 从左侧节点面板拖拽节点到画布：
   - `start` → `api` → `end`
3. 配置 `api` 节点，填写接口地址和参数
4. 点击「保存并发布」
5. 进入「流程实例」，点击「启动」运行流程
