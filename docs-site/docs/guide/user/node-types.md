# 节点类型

RiverFlow 内置 7 种节点类型，同时支持通过插件扩展新类型。

## 内置节点

| 节点类型 | 说明 | 用途 |
|---------|------|------|
| `start` | 开始节点 | 流程起点，标识流程开始 |
| `end` | 结束节点 | 流程终点，标识流程结束 |
| `api` | API 调用 | 调用已注册的 HTTP 接口 |
| `db` | 数据库 | 执行 SQL 语句（select/insert/update/delete） |
| `script` | 脚本 | 执行 Groovy 脚本 |
| `condition` | 条件 | 基于 SpEL 表达式进行分支判断 |
| `timer` | 定时器 | 延迟等待或定点等待 |

## API 节点

调用已注册的 `ApiCatalog` HTTP 接口，支持：

- Header、Body、Query 参数配置
- 动态参数引用（`${context.xxx}`）
- 结果输出映射

## DB 节点

执行 SQL 语句，支持：

- `#{context.xxx}` SpEL 占位符
- 动态数据源切换
- 增删改查四种操作

## Script 节点

执行 Groovy 脚本，支持：

- 读写 `context` 上下文
- 使用 `import` 导入类
- 沙箱安全限制（禁用危险关键字）

## Condition 节点

基于 SpEL 表达式进行分支判断，例如：

```
context.status == 'success'
context.count > 10
context.list.?[name == 'test'].size() > 0
```

## Timer 节点

支持两种等待模式：

- **延迟等待**：`delaySeconds` 秒后继续执行
- **定点等待**：等待到指定的 `fixedTime` 后继续执行
