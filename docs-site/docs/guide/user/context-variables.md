# 上下文变量

## 系统内置变量

| 变量名 | 说明 |
|--------|------|
| `_instanceId` | 流程实例 ID |
| `_businessKey` | 业务主键 |
| `_flowCode` | 流程编码 |
| `_currentTime` | 当前时间戳 |

## 自定义变量

节点执行结果可以通过 `outputMapping` 写入上下文，供下游节点使用：

```json
{
  "outputMapping": [
    { "source": "data.token", "target": "context.myToken" }
  ]
}
```

下游节点通过 `${context.myToken}` 引用。

## 变量引用语法

在节点配置中，支持以下引用方式：

| 语法 | 说明 | 示例 |
|------|------|------|
| `${context.xxx}` | 引用上下文变量 | `${context.userId}` |
| `${context.xxx.yyy}` | 引用嵌套属性 | `${context.user.name}` |
| `${context.list[0]}` | 引用数组元素 | `${context.items[0]}` |

## JSONPath 支持

部分场景支持 JSONPath 路径访问：

```
context.apiResult.data[0].name
```

## 变量持久化

每次节点执行后，上下文会序列化为 JSON 保存到数据库 `wf_flow_instance.context_json` 字段，确保流程中断后可恢复。
