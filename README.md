# RiverFlow · 河狸流程编排平台

> **让数据像河水一样流动** —— 可视化、可编排、可观测的政务数据协同中枢

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vue.js)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

---

## 📖 项目简介

**RiverFlow（河狸）** 是一款面向政务数字化场景的**可视化流程编排与数据交换平台**。它解决了传统中间件在跨系统数据协同中的三大痛点：

- **流程黑盒化** → 可视化拖拽设计，流程即文档
- **节点硬编码** → 节点可配置化，新增流程零代码
- **数据孤岛化** → 上下文驱动，节点间数据自然流转

平台以**事项**为核心，围绕事项配置数据源、注册第三方接口、设计工作流，最终实现政务数据在不同系统间的自动化、智能化流转。

---

## 🏗️ 总体架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              前端层 (Vue 3)                                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │ 事项管理  │ │ 数据源   │ │ 动态表   │ │ 接口注册  │ │ 工作流   │          │
│  │         │ │ 管理     │ │ 设计器   │ │ 与调试   │ │ 设计器   │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
│                              ↕ REST API (JWT)                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                              网关层                                           │
│         Spring Security + JWT 认证 │ 统一异常处理 │ API 文档 (Knife4j)        │
├─────────────────────────────────────────────────────────────────────────────┤
│                           业务服务层 (Spring Boot)                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │ 事项管理模块 │ │ 动态数据模块 │ │ 接口管理模块 │ │   工作流引擎模块     │   │
│  │             │ │             │ │             │ │                     │   │
│  │ • 区划管理   │ │ • 数据源注册 │ │ • 接口目录   │ │ • 流程定义管理       │   │
│  │ • 事项录入   │ │ • 动态建表   │ │ • 参数树     │ │ • 可视化节点执行器   │   │
│  │ • 事项绑定   │ │ • 自动API    │ │ • 脚本库     │ │ • SpEL 条件引擎     │   │
│  │             │ │ • 库表交换   │ │ • 在线调试   │ │ • 数据上下文流转     │   │
│  └─────────────┘ └─────────────┘ └─────────────┘ │ • 内置调度引擎       │   │
│                                                  └─────────────────────┘   │
├─────────────────────────────────────────────────────────────────────────────┤
│                           基础设施层                                          │
│  HTTP 执行器 │ Groovy 沙箱 │ 动态数据源 (dynamic-datasource) │ Redis 缓存   │
├─────────────────────────────────────────────────────────────────────────────┤
│                              数据层                                           │
│                         MySQL 8.0 (主库)  +  Redis                            │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| **Spring Boot** | 2.7.18 | 核心框架 |
| **MyBatis-Plus** | 3.5.5 | ORM 框架 |
| **dynamic-datasource** | 3.6.1 | 多数据源动态切换 |
| **Druid** | 1.2.21 | 数据库连接池 |
| **Spring Security** | 5.7.x | 安全认证与授权 |
| **JJWT** | 0.11.x | JWT Token 生成与校验 |
| **Spring SpEL** | 内置 | 流程条件表达式引擎 |
| **Groovy** | 3.0.x | 动态脚本执行 |
| **Redis + Lettuce** | - | 分布式缓存与锁 |
| **Knife4j** | 4.3.0 | API 文档 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| **Vue** | 3.4.x | 前端框架 |
| **Vite** | 5.x | 构建工具 |
| **Element Plus** | 2.5.x | UI 组件库 |
| **LogicFlow** | 1.2.x | 工作流可视化画布 |
| **Pinia** | 2.1.x | 状态管理 |
| **Vue Router** | 4.x | 路由管理 |
| **Axios** | 1.6.x | HTTP 客户端 |
| **Monaco Editor** | 内置 | Groovy/SpEL 代码编辑器 |

---

## 📁 项目结构

```
riverflow/
├── 📄 README.md                          # 项目说明
├── 📄 pom.xml                            # Maven 根 POM
│
├── 🟦 riverflow-common/                  # 公共模块
│   └── src/main/java/com/riverflow/common/
│       ├── base/                         # BaseEntity、BaseDTO
│       ├── result/                       # 统一响应体 R<T>
│       ├── exception/                    # 业务异常体系
│       ├── util/                         # JSON、加密、日期、SpEL 工具
│       └── constant/                     # 全局常量、枚举
│
├── 🟦 riverflow-api/                     # API 契约模块
│   └── src/main/java/com/riverflow/api/
│       ├── entity/                       # 数据库实体（与表一一对应）
│       ├── dto/                          # 请求/响应 DTO
│       ├── vo/                           # 视图对象
│       └── enums/                        # 业务枚举
│
├── 🟩 riverflow-admin/                   # 管理后台服务（Spring Boot）
│   └── src/main/java/com/riverflow/admin/
│       ├── AdminApplication.java         # 启动类
│       ├── config/                       # 配置类（MyBatisPlus、Security、CORS）
│       ├── controller/                   # REST Controller
│       ├── service/                      # 业务逻辑
│       ├── mapper/                       # MyBatis Mapper 接口
│       └── modules/
│           ├── item/                     # 事项管理
│           ├── datasource/               # 数据源管理
│           ├── dynamic/                  # 动态表与自动 API
│           ├── api/                      # 第三方接口注册与管理
│           └── workflow/                 # ⭐ 工作流引擎核心
│               ├── definition/           # 流程定义 CRUD
│               ├── instance/             # 流程实例管理
│               ├── engine/               # 流程执行引擎
│               │   ├── FlowEngine.java   # 引擎入口
│               │   └── TransitionEngine.java # 流转引擎
│               ├── node/                 # 节点执行器（策略模式）
│               │   ├── NodeExecutor.java # 执行器接口
│               │   ├── StartNodeExecutor.java
│               │   ├── ApiNodeExecutor.java
│               │   ├── DbNodeExecutor.java
│               │   ├── ScriptNodeExecutor.java
│               │   ├── ConditionNodeExecutor.java
│               │   ├── TimerNodeExecutor.java
│               │   └── EndNodeExecutor.java
│               ├── context/              # 流程数据上下文
│               │   └── FlowContext.java
│               ├── scheduler/            # 内置调度引擎
│               │   └── FlowScheduler.java
│               └── expression/           # SpEL 表达式工具
│                   └── SpelEngine.java
│       └── infra/
│           ├── http/                     # HTTP 请求执行器（工厂模式）
│           ├── groovy/                   # Groovy 沙箱执行器
│           ├── dynamicds/                # 动态数据源生命周期管理
│           └── security/                 # JWT 认证过滤器
│
├── 🟨 riverflow-ui/                      # 前端工程（Vue3）
│   ├── public/
│   ├── src/
│   │   ├── main.js                       # 入口
│   │   ├── App.vue
│   │   ├── router/                       # 路由配置
│   │   ├── store/                        # Pinia Store
│   │   ├── api/                          # Axios 封装（按模块）
│   │   ├── views/                        # 页面视图
│   │   │   ├── login/
│   │   │   ├── dashboard/
│   │   │   ├── item/                     # 事项管理
│   │   │   ├── datasource/               # 数据源管理
│   │   │   ├── dynamicTable/             # 动态表设计器
│   │   │   ├── apiMgr/                   # 接口注册与调试
│   │   │   ├── workflow/                 # ⭐ 工作流模块
│   │   │   │   ├── designer/             # 流程设计器页面
│   │   │   │   ├── definition/           # 流程定义列表
│   │   │   │   └── instance/             # 流程实例监控
│   │   │   └── monitor/                  # 运行监控大盘
│   │   ├── components/                   # 公共组件
│   │   │   ├── WorkflowDesigner/         # 工作流画布（基于 LogicFlow）
│   │   │   ├── ApiDebugger/              # 接口调试面板
│   │   │   ├── TableDesigner/            # 表结构设计组件
│   │   │   └── MappingEditor/            # 数据映射编辑器
│   │   ├── composables/                  # 组合式函数
│   │   └── utils/                        # 前端工具
│   ├── package.json
│   └── vite.config.js
│
└── 📁 db/                                # 数据库脚本
    ├── riverflow_init.sql                # 初始化表结构
    └── riverflow_data.sql                # 初始数据
```

---

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+

### 1. 克隆项目

```bash
git clone https://github.com/your-org/riverflow.git
cd riverflow
```

### 2. 初始化数据库

```bash
mysql -u root -p < db/riverflow_init.sql
mysql -u root -p < db/riverflow_data.sql
```

### 3. 启动后端

```bash
cd riverflow-admin
# 修改 src/main/resources/application.yml 中的数据库和 Redis 配置
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

API 文档地址：`http://localhost:8080/doc.html`

### 4. 启动前端

```bash
cd riverflow-ui
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 5. 默认账号

- 用户名：`admin`
- 密码：`admin123`

---

## 🎯 核心功能详解

### 5.1 事项管理

以**行政区划 → 事项**两级结构管理政务服务事项。每个事项可绑定：
- 一个**工作流定义**（必选）
- N 个**第三方接口**（可选）
- N 个**动态数据表**（可选）

### 5.2 动态数据源与动态表

- **数据源注册**：支持 MySQL、Oracle、SQL Server、PostgreSQL。提供在线连接测试。
- **可视化建表**：前端表单设计字段（名称、类型、长度、索引、约束），后端自动生成 DDL。
- **自动 API 生成**：表创建后自动生成 RESTful CRUD 接口，无需手写代码。

### 5.3 第三方接口注册

- **接口目录**：统一管理内部/外部 HTTP 接口。
- **参数树**：支持嵌套对象、数组、Header、Query、Body 参数可视化配置。
- **脚本库**：Groovy 脚本用于请求前格式化、响应后处理。
- **在线调试**：内置 Postman 式调试面板，即时验证接口可用性。

### 5.4 工作流引擎（核心）

#### 节点类型

| 节点 | 图标 | 功能 |
|------|------|------|
| **开始** | ⭕ | 流程起点，可配置触发方式（定时/事件/手动） |
| **接口节点** | ☁️ | 调用已注册的第三方接口 |
| **数据库节点** | 🛢️ | 执行 SQL（查询/插入/更新/删除） |
| **脚本节点** | `</>` | 执行 Groovy 脚本进行复杂数据处理 |
| **条件节点** | ◇ | 使用 **SpEL 表达式** 判断分支走向 |
| **定时节点** | ⏰ | 延迟执行或按 Cron 等待 |
| **结束** | 🏁 | 流程终点 |

#### 数据流转机制

每个节点通过 **输入映射（Input Mapping）** 从上下文中提取数据，执行完成后通过 **输出映射（Output Mapping）** 将结果写回上下文。下游节点即可使用上游数据。

示例：
```
节点A（调Token接口）输出映射：res.data.token → context.authToken
节点B（调业务接口）输入映射：context.authToken → header.Authorization
```

#### 条件表达式

使用 Spring SpEL 引擎，支持：
- 上下文变量：`#{context.authToken != null}`
- 数值比较：`#{context.resultCode == 200}`
- 正则匹配：`#{context.status matches '[12]0\\d{2}'}`
- 集合操作：`#{context.list.?[status == 'pending'].size() > 0}`

#### 调度机制

- **内嵌调度引擎**：基于 Spring `ThreadPoolTaskScheduler`，流程发布即自动注册/更新定时任务。
- **分布式支持**：集群环境下通过 Redis 分布式锁防止任务重复执行。
- **触发方式**：Cron 定时 / 外部事件推送 / 管理后台手动触发。

---

## 📐 设计原则

1. **配置优于编码**：新增流程、接口、表结构尽可能通过前端配置完成，减少 Java 代码编写。
2. **数据驱动流程**：流程的推进由数据状态决定，而非硬编码的 if-else。
3. **失败透明化**：任何节点失败都记录完整上下文快照，支持单节点重试或人工干预。
4. **向后兼容**：保留原项目的 Groovy 脚本能力、加密方案（Jasypt）、HTTP 执行器设计模式。

---

## 🔐 安全说明

- **JWT 认证**：无状态 Token，支持 Token 刷新与黑名单。
- **接口鉴权**：基于 RBAC，细粒度到按钮级。
- **SQL 注入防护**：所有动态 SQL 使用 MyBatis `#{}` 参数化，彻底废弃字符串拼接。
- **Groovy 沙箱**：脚本执行限制类加载范围，禁止访问文件系统、网络等敏感操作。
- **密码加密**：数据源密码等敏感配置使用 Jasypt 加密存储。

---

## 📈 路线图

- [x] 项目骨架搭建与 README
- [ ] Phase 1：底座搭建（事项管理、数据源管理）
- [ ] Phase 2：动态表与自动 API
- [ ] Phase 3：第三方接口注册与调试
- [ ] Phase 4：工作流引擎核心（设计器 + 执行器 + 上下文）
- [ ] Phase 5：内置调度引擎与流程监控
- [ ] Phase 6：性能优化、数据迁移、生产部署文档

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 📄 License

本项目基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源协议。

---

> **RiverFlow** — 筑坝导流，智驭数据之河。
