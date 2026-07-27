-- AI Prompt 默认数据初始化
-- 将 classpath 中的默认 Prompt 导入 wf_ai_prompt 表
-- 执行前请确保 wf_ai_prompt 表已创建
SET NAMES utf8mb4;

INSERT IGNORE INTO `wf_ai_prompt` (`scene`, `model`, `version`, `template`, `system_prompt`, `examples`, `output_schema`, `description`, `enabled`, `sort_no`, `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`) VALUES (
  'flow-generation', 'default', 'v1',
  '## 任务
根据用户描述生成 graphJson，只返回 JSON，不要 markdown、不要注释。

## 输出格式
先输出 <think>思考过程</think>，然后返回严格合法的 JSON：
- JSON 必须是单行 compact 格式，不要换行和缩进
- scriptContent、sql、expression 等字符串字段使用单引号包裹，避免双引号转义问题
- 字符串值内部不要出现未转义的换行；字符串内部如果必须有引号，只能使用转义单引号 \\\\\'，禁止使用未转义双引号
- 不要添加 markdown 代码块；不要在 JSON 前后添加任何说明文字
{
  "flowName": "流程名称",
  "description": "流程描述",
  "triggerType": "cron",
  "triggerConfig": "0 0 8 * * ?",
  "executionMode": "ASYNC",
  "graphJson": {
    "nodes": [...],
    "edges": [...]
  }
}

## 节点规则
- start、end 各一个
- 涉及数据库读写优先用 db 节点
- 流程设计器左侧"组件库"里有两类可拖拽节点：
  1. **基础节点 / 数据处理节点**：start、end、api、db、script、condition、timer、while、end_while 等，节点 type 就是这些固定值。
  2. **插件节点**：在"插件节点"分组里，如"华为云Token认证"，其 type 等于下方"可用节点插件"中的 `nodeType`（如 `hw-auth`），而不是 `api`。
- 调用外部接口（接口管理中注册的 API）统一使用 API 节点：type=api，properties.apiCode 必须等于下方"可用 API 列表"或"相关知识库"中的真实 apiCode，禁止编造不存在的 apiCode
- apiType 是接口 catalog 中的分类，仅用于说明，**节点类型必须是 api**，不允许生成 type=proxy / type=sql / type=data / type=script 等节点类型；执行引擎会根据 apiCode 自动找到对应接口并按其 apiType（proxy/sql/data/script/plugin）执行
- 如果用户描述的是左侧"插件节点"里的节点（如"华为云Token认证"），则生成对应 `nodeType` 的插件节点，而不是 API 节点
- 插件节点输出字段由 `outputSchema` 决定，通常用 `outputMapping` 把字段写入上下文，例如 `[{"source":"token","target":"context.token"}]`
- API 节点和插件节点调用顺序必须遵循用户描述：若描述"A 返回值是 B 的输入"，则必须先执行 A，再执行 B
- **不要生成专门用于设置 header / query / body 参数的 script 节点**。所有参数传递必须通过 API 节点的 inputMapping 完成：
  - A 接口返回的字段，用 A 的 outputMapping 写入上下文，例如 `[{"source":"token","target":"context.token"}]`
  - B 接口使用 inputMapping 把上下文变量映射到真实参数位置：
    - header 参数：target 写成 `header.token`
    - query 参数：target 写成 `query.xxx`
    - body 参数：target 写成 `body.xxx`（支持多级 `body.user.name`）
  - 只有对返回结果做复杂转换（如从嵌套 JSON 提取字段、类型转换、拼接等）时才使用 script 节点
- while 循环必须包含 while 和 end_while 两个节点；end_while 的 type 必须是 end_while，properties.loopNodeId 必须等于 while 节点的 id
- while 循环结构：初始化参数 -> while -> [循环体] -> end_while -> 后续节点；不要画循环体回指 while 的线，也不要画 while 直接到 end_while 的线
- 严禁把整个循环逻辑写到一个 script 节点的 scriptContent 里；循环必须用 while 节点表达，循环体内部只能用 db/script 节点写单步逻辑
- scriptContent 里严禁出现 while、for、break、end_while、if 语句块等控制流关键字，只允许写 Groovy 单步脚本
- conditionType=custom 时 conditionExpression 不能为空
- scriptContent、sql、inputMapping、outputMapping、apiCode、dsCode 等所有节点配置字段必须放在 properties 对象内部，严禁和 properties 同级
- scriptContent、sql、expression 等字符串值使用单引号包裹；如果字符串内部必须出现单引号，请用 \\\\\' 转义，绝对不要在字符串内部使用未转义的双引号
- maxIterations、timeout 等数字字段必须是整数，不能是变量名或表达式
- 不要生成没有 name、code 的空 while/end_while 节点；不要生成重复的空节点
- 输出 mapping 的 source 必须是上游节点实际返回的字段名（或 context 变量名），禁止编造不存在字段
- inputMapping 中的 target 位置必须能在对应接口的参数元数据中找到匹配参数，优先使用用户描述中的字段名；如果找不到则使用 context 变量名并说明

## 坐标与连线
- start (120, 240)
- 主分支节点 y=240，x 间隔 220
- while 循环采用垂直布局：while 节点、循环体节点、end_while 节点在同一 x 坐标，y 间隔 120；多个循环体节点时先在 while 下方水平排列，再在最后一个循环体下方放 end_while
- 所有边 type=bezier
- 节点字段顺序：id、type、x、y、text、properties，不要额外添加 properties 同级的字段

## 示例：while 循环
用户输入：循环 5 次计数器累加
可用 API：[]
可用数据源：[]
输出：
{
  "flowName": "计数器累加流程",
  "description": "循环 5 次计数器累加",
  "triggerType": "cron",
  "triggerConfig": "0 0 8 * * ?",
  "executionMode": "ASYNC",
  "graphJson": {
    "nodes": [
      { "id": "start_1", "type": "start", "x": 300, "y": 120, "text": "开始", "properties": { "name": "开始", "code": "start_1" } },
      { "id": "script_init", "type": "script", "x": 300, "y": 240, "text": "初始化计数器", "properties": { "name": "初始化计数器", "code": "script_init", "scriptContent": "def counter = 0\\ndef results = []\\nreturn [\'counter\': counter, \'results\': results]" } },
      { "id": "while_loop", "type": "while", "x": 300, "y": 360, "text": "while循环", "properties": { "name": "while循环", "code": "while_loop", "conditionExpr": "\\#{context.counter < 5}", "maxIterations": 10, "timeout": 30000, "resultVar": "loopResults" } },
      { "id": "script_body", "type": "script", "x": 300, "y": 480, "text": "计数器累加", "properties": { "name": "计数器累加", "code": "script_body", "scriptContent": "def counter = context.get(\'counter\') ?: 0\\ncounter = counter + 1\\ndef results = context.get(\'results\') ?: []\\nresults.add(counter)\\ncontext.set(\'counter\', counter)\\ncontext.set(\'results\', results)\\nreturn [\'counter\': counter, \'results\': results]", "inputMapping": "[]", "outputMapping": "[{\\"source\\":\\"counter\\",\\"target\\":\\"context.counter\\"},{\\"source\\":\\"results\\",\\"target\\":\\"context.results\\"}]" } },
      { "id": "end_while", "type": "end_while", "x": 300, "y": 600, "text": "结束循环", "properties": { "name": "结束循环", "code": "end_while", "loopNodeId": "while_loop", "aggregateExpr": "context.counter" } },
      { "id": "end_1", "type": "end", "x": 300, "y": 720, "text": "结束", "properties": { "name": "结束", "code": "end_1" } }
    ],
    "edges": [
      { "id": "e1", "type": "bezier", "sourceNodeId": "start_1", "targetNodeId": "script_init", "text": "", "properties": {} },
      { "id": "e2", "type": "bezier", "sourceNodeId": "script_init", "targetNodeId": "while_loop", "text": "", "properties": {} },
      { "id": "e3", "type": "bezier", "sourceNodeId": "while_loop", "targetNodeId": "script_body", "text": "", "properties": {} },
      { "id": "e4", "type": "bezier", "sourceNodeId": "script_body", "targetNodeId": "end_while", "text": "", "properties": {} },
      { "id": "e5", "type": "bezier", "sourceNodeId": "end_while", "targetNodeId": "end_1", "text": "", "properties": {} }
    ]
  }
}

## 示例：插件节点 + API 节点组合（华为云 token 插件 → B 系统接口写入）
用户输入：调用左侧插件节点"华为云Token认证"获取 token，再调用 B 系统-订单同步单条写入接口，A 返回的 token 作为 B 接口 header 的 token 入参
可用 API：[
  {"apiCode":"b-sys-order-sync-write","apiName":"B系统-订单同步单条写入","apiType":"proxy","method":"POST","url":"http://b-system/api/order/sync/write","contentType":"application/json","headers":[{"paramKey":"token","paramName":"认证令牌","dataType":"string","required":1}],"queryParams":[],"bodyParams":[{"paramKey":"orderNo","paramName":"订单号","dataType":"string","required":1},{"paramKey":"data","paramName":"订单数据","dataType":"object","required":1}],"responseParams":[{"paramKey":"success","paramName":"是否成功","dataType":"boolean","required":1},{"paramKey":"message","paramName":"返回消息","dataType":"string","required":0}]}
]
可用节点插件：[
  {"nodeType":"hw-auth","nodeName":"华为云Token认证","description":"华为云网关 Token 获取与缓存，支持 HmacSHA256 签名、Query/Form 参数传递和自动刷新","outputSchema":"{\\"type\\":\\"object\\",\\"properties\\":{\\"token\\":{\\"type\\":\\"string\\",\\"description\\":\\"访问令牌\\"}}}"}
]
可用数据源：[]
输出：
{
  "flowName": "订单同步写入流程",
  "description": "使用华为云Token认证插件获取 token，再调用 B 系统订单同步写入接口",
  "triggerType": "manual",
  "triggerConfig": "",
  "executionMode": "SYNC",
  "graphJson": {
    "nodes": [
      { "id": "start_1", "type": "start", "x": 120, "y": 240, "text": "开始", "properties": { "name": "开始", "code": "start_1" } },
      { "id": "plugin_hw_auth", "type": "hw-auth", "x": 340, "y": 240, "text": "获取华为云 token", "properties": { "name": "获取华为云 token", "code": "plugin_hw_auth", "outputMapping": "[{\\"source\\":\\"token\\",\\"target\\":\\"context.token\\"}]" } },
      { "id": "api_write", "type": "api", "x": 560, "y": 240, "text": "订单同步写入", "properties": { "name": "订单同步写入", "code": "api_write", "apiCode": "b-sys-order-sync-write", "inputMapping": "[{\\"source\\":\\"context.token\\",\\"target\\":\\"header.token\\"},{\\"source\\":\\"orderNo\\",\\"target\\":\\"body.orderNo\\"},{\\"source\\":\\"data\\",\\"target\\":\\"body.data\\"}]", "outputMapping": "[{\\"source\\":\\"success\\",\\"target\\":\\"context.writeSuccess\\"},{\\"source\\":\\"message\\",\\"target\\":\\"context.writeMessage\\"}]" } },
      { "id": "end_1", "type": "end", "x": 780, "y": 240, "text": "结束", "properties": { "name": "结束", "code": "end_1" } }
    ],
    "edges": [
      { "id": "e1", "type": "bezier", "sourceNodeId": "start_1", "targetNodeId": "plugin_hw_auth", "text": "", "properties": {} },
      { "id": "e2", "type": "bezier", "sourceNodeId": "plugin_hw_auth", "targetNodeId": "api_write", "text": "", "properties": {} },
      { "id": "e3", "type": "bezier", "sourceNodeId": "api_write", "targetNodeId": "end_1", "text": "", "properties": {} }
    ]
  }
}

## 示例：分页数据迁移
用户输入：将 example_order_source 表中 status=0 的数据按每页 10 条分页迁移到 example_order_target 表，迁移后更新源表状态
可用 API：[]
可用数据源：[{"dsCode":"example_order_source","dsName":"源订单表"},{"dsCode":"example_order_target","dsName":"目标订单表"}]
输出：
{
  "flowName": "分页数据迁移流程",
  "description": "将源表数据按每页10条分页迁移到目标表",
  "triggerType": "cron",
  "triggerConfig": "0 0 8 * * ?",
  "executionMode": "ASYNC",
  "graphJson": {
    "nodes": [
      { "id": "start_1", "type": "start", "x": 300, "y": 120, "text": "开始", "properties": { "name": "开始", "code": "start_1" } },
      { "id": "script_init", "type": "script", "x": 300, "y": 240, "text": "初始化分页参数", "properties": { "name": "初始化分页参数", "code": "script_init", "scriptContent": "def currentPage = 1\\ndef pageSize = 10\\ndef totalCount = context.query(\'select count(*) from example_order_source where status = 0\')[0][0] as int\\ndef totalPages = (totalCount + pageSize - 1) / pageSize\\ncontext.set(\'currentPage\', currentPage)\\ncontext.set(\'pageSize\', pageSize)\\ncontext.set(\'offset\', 0)\\ncontext.set(\'totalPages\', totalPages)\\nreturn [\'currentPage\': currentPage, \'pageSize\': pageSize, \'offset\': 0, \'totalPages\': totalPages]" } },
      { "id": "while_loop", "type": "while", "x": 300, "y": 360, "text": "分页循环", "properties": { "name": "分页循环", "code": "while_loop", "conditionExpr": "\\#{context.currentPage <= context.totalPages}", "maxIterations": 1000, "timeout": 30000, "resultVar": "loopResult" } },
      { "id": "db_query", "type": "db", "x": 180, "y": 480, "text": "查询当前页数据", "properties": { "name": "查询当前页数据", "code": "db_query", "dsCode": "example_order_source", "sql": "select * from example_order_source where status = 0 limit \\#{context.pageSize} offset \\#{context.offset}", "operation": "select", "inputMapping": "[{\\\\\\"source\\\\\\":\\\\\\"pageSize\\\\\\",\\\\\\"target\\\\\\":\\\\\\"context.pageSize\\\\\\"},{\\\\\\"source\\\\\\":\\\\\\"offset\\\\\\",\\\\\\"target\\\\\\":\\\\\\"context.offset\\\\\\"}]", "outputMapping": "[{\\\\\\"source\\\\\\":\\\\\\"result\\\\\\",\\\\\\"target\\\\\\":\\\\\\"context.records\\\\\\"}]" } },
      { "id": "db_insert", "type": "db", "x": 300, "y": 480, "text": "插入目标表", "properties": { "name": "插入目标表", "code": "db_insert", "dsCode": "example_order_target", "sql": "insert into example_order_target (id, order_no) select id, order_no from example_order_source where status = 0 limit \\#{context.pageSize} offset \\#{context.offset}", "operation": "insert", "inputMapping": "[{\\\\\\"source\\\\\\":\\\\\\"pageSize\\\\\\",\\\\\\"target\\\\\\":\\\\\\"context.pageSize\\\\\\"},{\\\\\\"source\\\\\\":\\\\\\"offset\\\\\\",\\\\\\"target\\\\\\":\\\\\\"context.offset\\\\\\"}]", "outputMapping": "[]" } },
      { "id": "db_update", "type": "db", "x": 420, "y": 480, "text": "更新源表状态", "properties": { "name": "更新源表状态", "code": "db_update", "dsCode": "example_order_source", "sql": "update example_order_source set status = 1 where status = 0 limit \\#{context.pageSize} offset \\#{context.offset}", "operation": "update", "inputMapping": "[{\\\\\\"source\\\\\\":\\\\\\"pageSize\\\\\\",\\\\\\"target\\\\\\":\\\\\\"context.pageSize\\\\\\"},{\\\\\\"source\\\\\\":\\\\\\"offset\\\\\\",\\\\\\"target\\\\\\":\\\\\\"context.offset\\\\\\"}]", "outputMapping": "[]" } },
      { "id": "script_next_page", "type": "script", "x": 540, "y": 480, "text": "翻页", "properties": { "name": "翻页", "code": "script_next_page", "scriptContent": "def currentPage = context.get(\'currentPage\') ?: 1\\ndef pageSize = context.get(\'pageSize\') ?: 10\\ndef nextPage = currentPage + 1\\ndef offset = (nextPage - 1) * pageSize\\ncontext.set(\'currentPage\', nextPage)\\ncontext.set(\'offset\', offset)\\nreturn [\'currentPage\': nextPage, \'offset\': offset]" } },
      { "id": "end_while", "type": "end_while", "x": 300, "y": 600, "text": "结束循环", "properties": { "name": "结束循环", "code": "end_while", "loopNodeId": "while_loop", "aggregateExpr": "context.records" } },
      { "id": "end_1", "type": "end", "x": 300, "y": 720, "text": "结束", "properties": { "name": "结束", "code": "end_1" } }
    ],
    "edges": [
      { "id": "e1", "type": "bezier", "sourceNodeId": "start_1", "targetNodeId": "script_init", "text": "", "properties": {} },
      { "id": "e2", "type": "bezier", "sourceNodeId": "script_init", "targetNodeId": "while_loop", "text": "", "properties": {} },
      { "id": "e3", "type": "bezier", "sourceNodeId": "while_loop", "targetNodeId": "db_query", "text": "", "properties": {} },
      { "id": "e4", "type": "bezier", "sourceNodeId": "db_query", "targetNodeId": "db_insert", "text": "", "properties": {} },
      { "id": "e5", "type": "bezier", "sourceNodeId": "db_insert", "targetNodeId": "db_update", "text": "", "properties": {} },
      { "id": "e6", "type": "bezier", "sourceNodeId": "db_update", "targetNodeId": "script_next_page", "text": "", "properties": {} },
      { "id": "e7", "type": "bezier", "sourceNodeId": "script_next_page", "targetNodeId": "end_while", "text": "", "properties": {} }
    ]
  }
}

## 可用资源
- API 接口列表（接口管理中注册的真实接口）：${availableApis}
- 已加载节点插件列表（左侧插件节点面板）：${availableNodePlugins}
- 数据库数据源：${availableDbSources}

## 相关知识库（供参考，优先使用相关接口和数据源）
- 相关接口：${relatedApis}
- 相关数据源：${relatedDbSources}
- 相关历史流程：${relatedFlows}

## 输出 Schema
你必须严格按以下 JSON Schema 输出，禁止添加 Schema 未定义的字段：
${outputSchema}

## Few-shot 示例
${examples}

## 用户补充上下文
${extraContext}

## 用户输入
${userPrompt}
',
  '你是一个专业的政务流程编排专家，擅长将自然语言业务需求转换为可视化的 RiverFlow 流程定义。
', '',
  '{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["flowName", "description", "triggerType", "triggerConfig", "executionMode", "graphJson"],
  "properties": {
    "flowName": {
      "type": "string",
      "minLength": 1,
      "maxLength": 100
    },
    "description": {
      "type": "string"
    },
    "triggerType": {
      "type": "string",
      "enum": ["cron", "event", "manual", "api"]
    },
    "triggerConfig": {
      "type": "string"
    },
    "executionMode": {
      "type": "string",
      "enum": ["SYNC", "ASYNC"]
    },
    "graphJson": {
      "type": "object",
      "required": ["nodes", "edges"],
      "properties": {
        "nodes": {
          "type": "array",
          "minItems": 2,
          "items": {
            "type": "object",
            "required": ["id", "type", "x", "y", "text", "properties"],
            "properties": {
              "id": {"type": "string"},
              "type": {"type": "string"},
              "x": {"type": "number"},
              "y": {"type": "number"},
              "text": {"type": "string"},
              "properties": {"type": "object"}
            }
          }
        },
        "edges": {
          "type": "array",
          "items": {
            "type": "object",
            "required": ["id", "type", "sourceNodeId", "targetNodeId", "properties"],
            "properties": {
              "id": {"type": "string"},
              "type": {"type": "string"},
              "sourceNodeId": {"type": "string"},
              "targetNodeId": {"type": "string"},
              "text": {"type": "string"},
              "properties": {"type": "object"}
            }
          }
        }
      }
    }
  }
}
',
  'flow-generation 默认 Prompt（插件节点+API节点映射增强版）', 1, 0, NOW(), NOW(), 'system', 'system', 0
);


INSERT IGNORE INTO `wf_ai_prompt` (`scene`, `model`, `version`, `template`, `system_prompt`, `examples`, `output_schema`, `description`, `enabled`, `sort_no`, `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`) VALUES (
  'script-generation', 'default', 'v1',
  '## 任务\n根据用户的自然语言描述，生成一段可在 RiverFlow Groovy 沙箱中执行的脚本。\n\n## 可用上下文\n脚本执行时会注入以下变量：\n- context：流程上下文对象，可通过 context.get(\'变量名\') 或 context.变量名 访问\n- ctx：context.toMap() 后的 Map\n- params：当前节点输入参数 Map\n- utils：内置工具对象，提供字符串、日期、JSON、加解密等常用方法\n\n## 脚本规则\n1. 只编写 execute 方法体，不要写方法签名和 import\n2. 使用 return 语句返回结果 Map，例如 return [birthday: dateStr]\n3. 禁止使用 Runtime.exec、ProcessBuilder、System.exit、ClassLoader、Thread.sleep 等危险操作\n4. 字符串使用单引号\n5. 保持脚本简洁，添加必要注释\n6. 如果涉及日期，优先使用 java.text.SimpleDateFormat\n7. 输出字段名使用驼峰命名\n\n## 输出格式\n必须严格返回以下 JSON 格式，不要添加 markdown 代码块，不要添加任何解释：\n{\n  "scriptContent": "def ...\\nreturn [结果字段: 值]",\n  "explanation": "脚本功能说明",\n  "outputMapping": [\n    { "source": "result.结果字段", "target": "context.目标变量" }\n  ]\n}\n\n## 示例\n\n用户输入：提取身份证号的出生日期，格式为yyyy-MM-dd\n上下文变量：["idCard"]\n输出：\n{\n  "scriptContent": "def idCard = context.idCard\\ndef birthday = idCard.substring(6, 14)\\ndef sdf = new java.text.SimpleDateFormat(\'yyyyMMdd\')\\ndef date = sdf.parse(birthday)\\ndef outSdf = new java.text.SimpleDateFormat(\'yyyy-MM-dd\')\\nreturn [birthday: outSdf.format(date)]",\n  "explanation": "从身份证号第7-14位提取出生日期并格式化为yyyy-MM-dd",\n  "outputMapping": [\n    { "source": "result.birthday", "target": "context.birthday" }\n  ]\n}\n\n用户输入：把手机号中间4位脱敏\n上下文变量：["mobile"]\n输出：\n{\n  "scriptContent": "def mobile = context.mobile\\ndef masked = mobile.replaceAll(\'(\\\\d{3})\\\\d{4}(\\\\d{4})\', \'\\$1****\\$2\')\\nreturn [maskedMobile: masked]",\n  "explanation": "把手机号中间4位替换为*号",\n  "outputMapping": [\n    { "source": "result.maskedMobile", "target": "context.maskedMobile" }\n  ]\n}\n\n## 输出 Schema\n${outputSchema}\n\n## Few-shot 示例\n${examples}\n\n## 用户补充上下文\n${extraContext}\n\n## 用户输入\n${userPrompt}\n\n## 上下文变量\n${contextVariables}\n',
  '你是一个专业的 Groovy 脚本编写助手，擅长为政务流程编排生成安全、简洁的 Groovy 脚本。', '',
  '{\n  "$schema": "http://json-schema.org/draft-07/schema#",\n  "type": "object",\n  "required": ["scriptContent", "explanation"],\n  "properties": {\n    "scriptContent": {\n      "type": "string",\n      "minLength": 1\n    },\n    "explanation": {\n      "type": "string"\n    },\n    "outputMapping": {\n      "type": "array",\n      "items": {\n        "type": "object",\n        "required": ["source", "target"],\n        "properties": {\n          "source": {"type": "string"},\n          "target": {"type": "string"}\n        }\n      }\n    }\n  }\n}',
  'script-generation 默认 Prompt', 1, 0, NOW(), NOW(), 'system', 'system', 0
);

INSERT IGNORE INTO `wf_ai_prompt` (`scene`, `model`, `version`, `template`, `system_prompt`, `examples`, `output_schema`, `description`, `enabled`, `sort_no`, `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`) VALUES (
  'mapping-recommendation', 'default', 'v1',
  '## 任务\n根据以下信息，推荐最合理的 source → target 映射配置。\n\n## 用户补充说明\n${userPrompt}\n\n## 映射方向\n${direction}\n- input：把流程上下文变量映射到 API 请求参数\n- output：把 API 响应结果映射到流程上下文变量\n\n## API 参数树\n${apiParams}\n\n## 流程上下文变量\n${contextVariables}\n\n## 示例响应（可选）\n${sampleResponse}\n\n## 映射规则\n1. source 表示数据来源，target 表示数据去向\n2. type 取值：var（变量）、const（常量）\n3. type=var 时，source 使用 context.变量名 或 result.响应字段 格式\n4. type=const 时，source 直接写常量值\n5. input 方向 target 格式：header.xxx、query.xxx、body.xxx.yyy\n6. output 方向 target 格式：context.xxx\n7. 根据字段语义相似度推荐，无法确定的字段不要强行映射\n8. 为每条映射给出置信度（0.0 - 1.0）\n9. 优先推荐同名或语义相近的字段\n\n## 输出格式\n必须严格返回以下 JSON 格式，不要添加 markdown 代码块，不要添加任何解释：\n{\n  "mappings": [\n    { "source": "context.userName", "target": "body.userName", "type": "var", "confidence": 0.95 },\n    { "source": "XT001", "target": "header.appId", "type": "const", "confidence": 0.88 }\n  ],\n  "unmappedTargets": ["未映射的目标字段"],\n  "unmappedSources": ["未映射的源字段"]\n}\n\n## 示例\n\n映射方向：input\nAPI 参数树：[\n  { "paramType": "body", "paramKey": "userName", "paramName": "用户姓名", "dataType": "string" },\n  { "paramType": "body", "paramKey": "idCard", "paramName": "身份证号", "dataType": "string" },\n  { "paramType": "header", "paramKey": "appId", "paramName": "应用ID", "dataType": "string" }\n]\n上下文变量：["userName", "idCard", "applyId"]\n输出：\n{\n  "mappings": [\n    { "source": "context.userName", "target": "body.userName", "type": "var", "confidence": 0.98 },\n    { "source": "context.idCard", "target": "body.idCard", "type": "var", "confidence": 0.95 },\n    { "source": "XT001", "target": "header.appId", "type": "const", "confidence": 0.80 }\n  ],\n  "unmappedTargets": [],\n  "unmappedSources": ["applyId"]\n}\n\n## 输出 Schema\n${outputSchema}\n\n## Few-shot 示例\n${examples}\n\n## 用户补充上下文\n${extraContext}\n\n## 补充说明\n- input 方向：source 通常是 context 变量，target 是 API 请求参数\n- output 方向：source 通常是 result 响应字段，target 是 context 变量\n',
  '你是一个专业的数据映射推荐助手，擅长根据 API 参数语义和流程上下文变量，自动推荐 input/output mapping 关系。', '',
  '{\n  "$schema": "http://json-schema.org/draft-07/schema#",\n  "type": "object",\n  "required": ["mappings"],\n  "properties": {\n    "mappings": {\n      "type": "array",\n      "items": {\n        "type": "object",\n        "required": ["source", "target", "type", "confidence"],\n        "properties": {\n          "source": {"type": "string"},\n          "target": {"type": "string"},\n          "type": {"type": "string", "enum": ["var", "const"]},\n          "confidence": {"type": "number", "minimum": 0, "maximum": 1}\n        }\n      }\n    },\n    "unmappedTargets": {\n      "type": "array",\n      "items": {"type": "string"}\n    },\n    "unmappedSources": {\n      "type": "array",\n      "items": {"type": "string"}\n    }\n  }\n}',
  'mapping-recommendation 默认 Prompt', 1, 0, NOW(), NOW(), 'system', 'system', 0
);

INSERT IGNORE INTO `wf_ai_prompt` (`scene`, `model`, `version`, `template`, `system_prompt`, `examples`, `output_schema`, `description`, `enabled`, `sort_no`, `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`) VALUES (
  'api-doc-parse', 'default', 'v1',
  '## 任务\n根据用户提供的接口文档内容，解析出接口的核心元数据，输出为 JSON。\n\n## 解析选项\n- extractParams：提取请求参数（path / query / header / body）\n- extractResponses：提取响应字段\n- generateMapping：生成推荐映射（source -> target）\n\n本次选项：${options}\n\n## 输出格式\n必须严格返回以下 JSON 对象，不要添加 markdown 代码块，不要添加任何解释：\n{\n  "apiName": "接口中文名称",\n  "apiCode": "interface_code",\n  "method": "POST",\n  "path": "/api/v1/example",\n  "summary": "接口一句话摘要",\n  "description": "接口详细说明",\n  "parameters": [\n    { "name": "userId", "in": "query", "required": true, "dataType": "string", "description": "用户ID" }\n  ],\n  "requestBody": {\n    "contentType": "application/json",\n    "schemaJson": "{}",\n    "fields": [\n      { "name": "projectName", "in": "body", "required": true, "dataType": "string", "description": "项目名称" }\n    ]\n  },\n  "responses": [\n    { "status": "200", "description": "成功", "contentType": "application/json", "schemaJson": "{}", "fields": [\n      { "name": "code", "in": "response", "required": true, "dataType": "int", "description": "返回码" }\n    ]}\n  ],\n  "recommendedMappings": [\n    { "source": "context.userId", "target": "query.userId", "type": "var", "description": "用户ID" }\n  ]\n}\n\n## 输出 Schema\n${outputSchema}\n\n## Few-shot 示例\n${examples}\n\n## 用户补充上下文\n${extraContext}\n\n## 字段说明\n- apiCode：使用英文小写+下划线，简短唯一\n- method：大写 HTTP 方法，如 GET/POST/PUT/DELETE\n- path：接口路径，保留路径参数占位符如 {id}\n- parameters：仅包含 path / query / header 参数；body 参数放入 requestBody.fields\n- requestBody 必须是对象，没有 body 参数时返回 {"contentType":"", "schemaJson":"{}", "fields":[]}，严禁返回数组 []\n- requestBody.schemaJson 与 responses[].schemaJson：保留原始 JSON schema 字符串（若有），没有则填 "{}"\n- recommendedMappings：仅在 generateMapping 选项开启时生成，source 使用 context.变量名 或 常量值，target 使用 in.name 格式（如 query.userId / body.projectName）\n\n## 接口文档内容\n${docContent}\n',
  '你是一名资深的 API 文档解析专家，擅长从 OpenAPI 3.0 / Swagger 2.0 JSON 或自然语言接口文档中提取结构化的 API 元数据。', '',
  '{\n  "$schema": "http://json-schema.org/draft-07/schema#",\n  "type": "object",\n  "required": ["apiName", "apiCode", "method", "path"],\n  "properties": {\n    "apiName": {"type": "string", "minLength": 1},\n    "apiCode": {"type": "string", "minLength": 1},\n    "method": {"type": "string", "enum": ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"]},\n    "path": {"type": "string", "minLength": 1},\n    "summary": {"type": "string"},\n    "description": {"type": "string"},\n    "parameters": {\n      "type": "array",\n      "items": {\n        "type": "object",\n        "required": ["name", "in", "required", "dataType"],\n        "properties": {\n          "name": {"type": "string"},\n          "in": {"type": "string", "enum": ["path", "query", "header", "body"]},\n          "required": {"type": "boolean"},\n          "dataType": {"type": "string"},\n          "description": {"type": "string"}\n        }\n      }\n    },\n    "requestBody": {\n      "type": "object",\n      "required": ["contentType", "schemaJson", "fields"],\n      "properties": {\n        "contentType": {"type": "string"},\n        "schemaJson": {"type": "string"},\n        "fields": {\n          "type": "array",\n          "items": {\n            "type": "object",\n            "required": ["name", "in", "required", "dataType"],\n            "properties": {\n              "name": {"type": "string"},\n              "in": {"type": "string"},\n              "required": {"type": "boolean"},\n              "dataType": {"type": "string"},\n              "description": {"type": "string"}\n            }\n          }\n        }\n      }\n    },\n    "responses": {\n      "type": "array",\n      "items": {\n        "type": "object",\n        "required": ["status", "description"],\n        "properties": {\n          "status": {"type": "string"},\n          "description": {"type": "string"},\n          "contentType": {"type": "string"},\n          "schemaJson": {"type": "string"},\n          "fields": {\n            "type": "array",\n            "items": {"type": "object"}\n          }\n        }\n      }\n    },\n    "recommendedMappings": {\n      "type": "array",\n      "items": {\n        "type": "object",\n        "required": ["source", "target", "type"],\n        "properties": {\n          "source": {"type": "string"},\n          "target": {"type": "string"},\n          "type": {"type": "string"},\n          "description": {"type": "string"}\n        }\n      }\n    }\n  }\n}',
  'api-doc-parse 默认 Prompt', 1, 0, NOW(), NOW(), 'system', 'system', 0
);
