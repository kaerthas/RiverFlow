---
pageLayout: home
config:
  - type: hero
    full: true
    background: tint-plate
    hero:
      name: RiverFlow
      tagline: 可视化流程编排与政务数据交换平台
      text: 基于 LogicFlow 的拖拽式流程设计器，内置 API、数据库、脚本、条件分支等多种节点，支持 Java SPI 插件化扩展
      actions:
        - text: 快速开始
          link: /guide/start/quickstart.html
          theme: brand
        - text: 查看文档
          link: /guide/user/workflow-design.html
          theme: alt

  - type: features
    features:
      - title: 可视化设计
        icon: 🎨
        details: 基于 LogicFlow 的拖拽式节点编排，支持 API、数据库、脚本、条件分支等多种节点类型
      - title: 插件化扩展
        icon: 🔌
        details: 基于 Java SPI 的插件系统，支持运行时热加载，插件可使用 Spring 管理的 Bean
      - title: 分布式调度
        icon: ⚡
        details: 支持多节点部署，基于 Redis 分布式锁实现任务调度，避免重复执行
      - title: 政务数据交换
        icon: 🏛️
        details: 内置动态数据源、API 代理、数据转换等能力，适配政务场景的数据交换需求
      - title: 版本管理
        icon: 📦
        details: 流程定义支持多版本管理，发布新版本自动下线旧版本，运行中的实例不受影响
      - title: Spring Bean 支持
        icon: 🍃
        details: 插件可通过 init(ApplicationContext) 获取主项目的 RedisTemplate、JdbcTemplate 等 Bean
---
