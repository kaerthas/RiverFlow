# comBusinessNotify 业务推送接口使用说明

## 一、本次改造内容

1. **OpenApiController 支持 `application/x-www-form-urlencoded`**
   - `POST /api/open/{apiCode}` 现在会根据请求 Content-Type 自动识别：
     - `application/json` → 走 `@RequestBody` 解析
     - `application/x-www-form-urlencoded` → 走 `HttpServletRequest` 嵌套参数解析

2. **嵌套参数解析**
   - 支持点号路径：`baseInfo.person.name`
   - 支持中括号路径：`baseInfo[person][name]`
   - SQL 占位符 `#{baseInfo}` 可整体取到嵌套对象，自动序列化为 JSON 存入 MySQL JSON 列

3. **动态表支持 Content-Type 选择**
   - `POST /dynamic-table/{id}/gen-api?contentType=application/x-www-form-urlencoded`
   - 生成的接口注册到 `wf_api_catalog` 时会带上指定的 Content-Type

4. **动态表支持一键创建物理表**
   - `POST /dynamic-table/{id}/create-table`
   - 根据 `wf_dynamic_table` + `wf_dynamic_table_column` 元数据自动生成并执行 `CREATE TABLE` DDL

---

## 二、使用方式（推荐通过 SQL 脚本快速初始化）

### 步骤 1：执行初始化 SQL

直接执行 `db/init_com_business_notify.sql`，完成以下四件事：
1. 创建物理表 `com_business_notify`
2. 写入动态表元数据 `wf_dynamic_table`
3. 写入动态表字段元数据 `wf_dynamic_table_column`
4. 注册开放接口 `wf_api_catalog`（INSERT + SELECT）

### 步骤 2：验证接口

A系统调用示例：

```bash
curl -X POST http://localhost:8080/api/open/COM_BUSINESS_NOTIFY_INSERT \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "businessId=BIZ20240001" \
  -d "itemId=ITEM001" \
  -d "itemCode=CODE001" \
  -d "itemName=事项名称" \
  -d "orgName=市住建局" \
  -d "orgCode=ORG001" \
  -d "source=2" \
  -d "regionCode=330102" \
  -d "applySubject=主题名称" \
  -d "type=Accept" \
  -d "businessType=1" \
  -d "baseInfo.person.name=张三" \
  -d "baseInfo.person.idCardNo=330102199001011234" \
  -d "baseInfo.person.linkPhone=13800138000" \
  -d "baseInfo.company.companyName=测试企业" \
  -d "baseInfo.company.creditIdentifier=91330000MA0000000X" \
  -d "form.FORM_ID=F001" \
  -d "form.DATA_ID=D001" \
  -d "material.DATA[0].FILE_NAME=身份证.pdf" \
  -d "material.DATA[0].FILE_PATH=http://xxx/file/1.pdf" \
  -d "emsInfo.isEms=1" \
  -d "emsInfo.emsName=张三" \
  -d "emsInfo.emsPhone=13800138000"
```

### 步骤 3：返回结果

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "affectedRows": 1
  }
}
```

---

## 三、使用方式（通过 UI 手动配置）

如果不想执行 SQL，也可以通过管理后台界面操作：

1. **动态表设计** → 新建表
   - 表编码：`com_business_notify`
   - 表名称：业务推送通知表
   - 按需求配置字段（参考 SQL 中的字段定义）

2. **保存字段后** → 点击【创建物理表】
   - 调用 `POST /dynamic-table/{id}/create-table`
   - 系统自动生成并执行 DDL

3. **点击【生成接口】**
   - 如需支持 form-urlencoded，前端传参：`contentType=application/x-www-form-urlencoded`
   - 系统会自动注册 `com_business_notify_INSERT` 等接口到 `wf_api_catalog`

4. **接口管理** → 找到生成的接口，确认状态为【已发布】

---

## 四、表结构设计说明

采用"关键字段独立列 + 嵌套对象 JSON 化"的混合设计：

| 类型 | 字段 | 说明 |
|------|------|------|
| 独立列 | business_id, item_id, item_code, org_code, source, region_code... | 用于查询、索引、路由分发 |
| JSON 列 | base_info, form_info, material_info, ems_info | 保持 A 系统原始报文完整，便于后续向各市级部门做数据转换和转发 |

**优势**：
- 关键字段可建索引，协同调度中心能根据 `org_code`、`region_code`、`source` 快速做路由分发
- JSON 列保留完整原始结构，向下游系统转发时无需重新组装报文
- 不会因为 A 系统字段增减而频繁改表结构

---

## 五、SQL 占位符映射规则

A系统 form 参数名 → SQL `#{占位符}` → 实际取值：

| A系统参数 | SQL 占位符 | 说明 |
|-----------|-----------|------|
| `businessId` | `#{businessId}` | 平级参数，直接替换 |
| `baseInfo.person.name` | `#{baseInfo.person.name}` | 嵌套参数，解析器递归取值 |
| `baseInfo`（整体） | `#{baseInfo}` | 取到整个嵌套对象，自动序列化为 JSON 字符串 |

> 如果后续需要把 `person.name` 单独存一列（而非整体 JSON），只需在表上加字段 `person_name`，SQL 中写 `#{baseInfo.person.name}` 即可。

---

## 六、注意事项

1. **物理表必须先存在**：`gen-api` 只生成接口配置，不会自动建表。需先调用 `/create-table` 或手动执行 DDL。
2. **数据源**：默认走主库（`ds_id = 0`）。如需指定其他数据源，在动态表设计时选择对应数据源。
3. **防 SQL 注入**：`resolveSql` 已做基础转义（单引号转双单引号），但复杂场景建议后续接入参数化查询（PreparedStatement）。
4. **返回值规范**：当前 SQL 类型接口返回统一包装体，若 A 系统要求特定返回值格式（如 `{"state":"200","receiveNum":"xxx"}`），可在 `wf_api_catalog` 中绑定 Groovy 脚本做结果格式化。
