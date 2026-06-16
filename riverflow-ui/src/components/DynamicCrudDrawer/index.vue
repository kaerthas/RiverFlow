<template>
  <el-drawer
    v-model="visible"
    :title="drawerTitle"
    size="90%"
    destroy-on-close
    :close-on-click-modal="false"
    class="dynamic-crud-drawer"
  >
    <div class="crud-container">
      <!-- 搜索栏 -->
      <div class="crud-search">
        <el-form :inline="true" :model="queryForm" class="search-form">
          <el-form-item
            v-for="col in visibleQueryColumns"
            :key="col.columnCode"
            :label="col.columnName"
          >
            <el-input
              v-if="isTextType(col)"
              v-model="queryForm[col.columnCode]"
              :placeholder="`请输入${col.columnName}`"
              clearable
            />
            <el-input-number
              v-else-if="isNumberType(col)"
              v-model="queryForm[col.columnCode]"
              :placeholder="`请输入${col.columnName}`"
              controls-position="right"
              style="width: 180px"
            />
            <el-date-picker
              v-else-if="isDateType(col)"
              v-model="queryForm[col.columnCode]"
              type="datetime"
              :placeholder="`请选择${col.columnName}`"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 180px"
            />
            <el-select
              v-else-if="isBooleanType(col)"
              v-model="queryForm[col.columnCode]"
              :placeholder="`请选择${col.columnName}`"
              clearable
              style="width: 180px"
            >
              <el-option label="是" :value="1" />
              <el-option label="否" :value="0" />
            </el-select>
            <el-input
              v-else
              v-model="queryForm[col.columnCode]"
              :placeholder="`请输入${col.columnName}`"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
            <el-button type="success" :icon="Plus" @click="handleCreate">新增</el-button>
            <el-button
              v-if="queryColumns.length > QUERY_COLLAPSE_COUNT"
              link
              type="info"
              @click="showAllQuery = !showAllQuery"
            >
              {{ showAllQuery ? '收起' : '展开' }}
              <el-icon class="el-icon--right">
                <ArrowUp v-if="showAllQuery" />
                <ArrowDown v-else />
              </el-icon>
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <div class="crud-table">
        <el-table
          :data="tableData"
          v-loading="loading"
          stripe
          border
          height="100%"
          class="crud-data-table"
          style="width: 100%"
        >
          <el-table-column type="index" label="#" width="50" align="center" fixed="left" />
          <el-table-column
            v-for="col in displayColumns"
            :key="col.columnCode"
            :prop="col.columnCode"
            :label="col.columnName"
            min-width="140"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              <span>{{ formatCellValue(row[col.columnCode], col) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="crud-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="formTitle"
      width="700px"
      top="5vh"
      destroy-on-close
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="crud-form"
      >
        <el-form-item
          v-for="col in formColumns"
          :key="col.columnCode"
          :label="col.columnName"
          :prop="col.columnCode"
        >
          <el-input
            v-if="isTextType(col) && !isLongText(col)"
            v-model="formData[col.columnCode]"
            :placeholder="`请输入${col.columnName}`"
            clearable
          />
          <el-input
            v-else-if="isLongText(col)"
            v-model="formData[col.columnCode]"
            type="textarea"
            :rows="4"
            :placeholder="`请输入${col.columnName}`"
          />
          <el-input-number
            v-else-if="isNumberType(col)"
            v-model="formData[col.columnCode]"
            :placeholder="`请输入${col.columnName}`"
            controls-position="right"
            style="width: 100%"
          />
          <el-date-picker
            v-else-if="isDateType(col)"
            v-model="formData[col.columnCode]"
            type="datetime"
            :placeholder="`请选择${col.columnName}`"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
          <el-date-picker
            v-else-if="isDateOnlyType(col)"
            v-model="formData[col.columnCode]"
            type="date"
            :placeholder="`请选择${col.columnName}`"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
          <el-switch
            v-else-if="isBooleanType(col)"
            v-model="formData[col.columnCode]"
            :active-value="1"
            :inactive-value="0"
          />
          <el-input
            v-else-if="isJsonType(col)"
            v-model="formData[col.columnCode]"
            type="textarea"
            :rows="6"
            :placeholder="`请输入JSON格式${col.columnName}`"
          />
          <el-input
            v-else
            v-model="formData[col.columnCode]"
            :placeholder="`请输入${col.columnName}`"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, RefreshRight, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import {
  getDynamicCrudColumns,
  getDynamicCrudData,
  saveDynamicCrudData,
  deleteDynamicCrudData
} from '@/api/dynamicCrud'

const props = defineProps({
  tableId: { type: [String, Number], default: null },
  tableCode: { type: String, default: '' },
  tableName: { type: String, default: '' }
})

const visible = ref(false)
const loading = ref(false)
const showAllQuery = ref(false)
const QUERY_COLLAPSE_COUNT = 3
const submitLoading = ref(false)
const formVisible = ref(false)
const formTitle = ref('新增数据')
const formRef = ref(null)

const columns = ref([])
const tableData = ref([])
const queryForm = reactive({})
const formData = reactive({})
const formRules = reactive({})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const drawerTitle = computed(() => {
  return `数据管理 - ${props.tableName || props.tableCode || ''}`
})

// 主键字段
const pkColumn = computed(() => {
  const pk = columns.value.find(c => c.isPk === 1)
  return pk ? pk.columnCode : 'id'
})

// 可作为查询条件的字段：varchar/number/date 类型，且不是主键
const queryColumns = computed(() => {
  return columns.value.filter(c => {
    if (c.isPk === 1) return false
    const type = (c.dataType || '').toLowerCase()
    return ['varchar', 'int', 'integer', 'bigint', 'datetime', 'timestamp', 'date', 'decimal', 'tinyint'].includes(type)
  })
})

// 默认显示前 N 个查询条件，超出可展开
const visibleQueryColumns = computed(() => {
  if (showAllQuery.value) return queryColumns.value
  return queryColumns.value.slice(0, QUERY_COLLAPSE_COUNT)
})

// 表格展示字段：排除超长文本，主键放前面
const displayColumns = computed(() => {
  return columns.value.filter(c => {
    const type = (c.dataType || '').toLowerCase()
    return !['longtext', 'text'].includes(type)
  })
})

// 表单字段：排除自增主键
const formColumns = computed(() => {
  return columns.value.filter(c => {
    // 自增主键在新增时不显示，编辑时只读（这里简化处理，直接不显示主键）
    if (c.isPk === 1) return false
    return true
  })
})

// 打开抽屉
function open() {
  visible.value = true
  showAllQuery.value = false
  resetQuery()
  loadColumns().then(() => {
    loadData()
  })
}

// 加载列定义
async function loadColumns() {
  try {
    const res = await getDynamicCrudColumns(props.tableId)
    columns.value = Array.isArray(res) ? res : []
    initQueryForm()
    initFormRules()
  } catch (e) {
    columns.value = []
  }
}

// 初始化查询表单
function initQueryForm() {
  Object.keys(queryForm).forEach(key => delete queryForm[key])
  queryColumns.value.forEach(col => {
    const type = (col.dataType || '').toLowerCase()
    if (isNumberType(col) || isBooleanType(col)) {
      queryForm[col.columnCode] = null
    } else {
      queryForm[col.columnCode] = ''
    }
  })
}

// 初始化表单校验规则
function initFormRules() {
  Object.keys(formRules).forEach(key => delete formRules[key])
  formColumns.value.forEach(col => {
    const rules = []
    if (col.isRequired === 1) {
      rules.push({ required: true, message: `${col.columnName}不能为空`, trigger: 'blur' })
    }
    if (col.length && col.length > 0 && isTextType(col)) {
      rules.push({ max: col.length, message: `长度不能超过${col.length}个字符`, trigger: 'blur' })
    }
    if (rules.length) {
      formRules[col.columnCode] = rules
    }
  })
}

// 查询数据
async function loadData() {
  if (!props.tableId) return
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    queryColumns.value.forEach(col => {
      const value = queryForm[col.columnCode]
      if (value !== '' && value !== null && value !== undefined) {
        params[col.columnCode] = value
      }
    })
    const res = await getDynamicCrudData(props.tableId, params)
    tableData.value = res.list || []
    pagination.total = Number(res.total) || 0
  } catch (e) {
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadData()
}

// 重置
function handleReset() {
  initQueryForm()
  handleSearch()
}

// 新增
function handleCreate() {
  formTitle.value = '新增数据'
  Object.keys(formData).forEach(key => delete formData[key])
  formColumns.value.forEach(col => {
    const type = (col.dataType || '').toLowerCase()
    if (type === 'tinyint') {
      formData[col.columnCode] = 0
    } else if (type === 'int' || type === 'integer' || type === 'bigint' || type === 'decimal' || type === 'double' || type === 'float') {
      formData[col.columnCode] = null
    } else {
      formData[col.columnCode] = ''
    }
  })
  formVisible.value = true
}

// 编辑
function handleEdit(row) {
  formTitle.value = '编辑数据'
  Object.keys(formData).forEach(key => delete formData[key])
  // 复制当前行数据到表单
  formColumns.value.forEach(col => {
    const value = row[col.columnCode]
    if (isJsonType(col) && (typeof value === 'object' || value === null)) {
      formData[col.columnCode] = value ? JSON.stringify(value, null, 2) : ''
    } else {
      formData[col.columnCode] = value !== null && value !== undefined ? value : ''
    }
  })
  // 把主键也带进去，用于后端判断是更新
  const pk = pkColumn.value
  if (row[pk] !== undefined && row[pk] !== null) {
    formData[pk] = row[pk]
  }
  formVisible.value = true
}

// 提交
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = {}
    Object.keys(formData).forEach(key => {
      payload[key] = formData[key]
    })
    // 处理 JSON 字段：把字符串转成对象
    formColumns.value.forEach(col => {
      if (isJsonType(col) && typeof payload[col.columnCode] === 'string') {
        try {
          payload[col.columnCode] = JSON.parse(payload[col.columnCode])
        } catch (e) {
          // 保持字符串
        }
      }
    })
    await saveDynamicCrudData(props.tableId, payload)
    ElMessage.success('保存成功')
    formVisible.value = false
    loadData()
  } catch (e) {
    // 失败已由 request 拦截器提示
  } finally {
    submitLoading.value = false
  }
}

// 删除
async function handleDelete(row) {
  const pk = pkColumn.value
  const id = row[pk]
  if (id === undefined || id === null) {
    ElMessage.warning('无法获取主键值')
    return
  }
  try {
    await ElMessageBox.confirm('确认删除该条数据？', '删除确认', { type: 'warning' })
    await deleteDynamicCrudData(props.tableId, id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 取消或失败
  }
}

// 格式化单元格显示
function formatCellValue(value, col) {
  if (value === null || value === undefined) return '-'
  if (isJsonType(col)) {
    if (typeof value === 'object') {
      return JSON.stringify(value).substring(0, 100)
    }
    return String(value).substring(0, 100)
  }
  if (isDateType(col) || isDateOnlyType(col)) {
    return String(value).replace('T', ' ')
  }
  if (isBooleanType(col)) {
    return value === 1 || value === true || value === '1' ? '是' : '否'
  }
  return value
}

// 类型判断工具函数
function getType(col) {
  return (col.dataType || '').toLowerCase()
}

function isTextType(col) {
  const type = getType(col)
  return ['varchar', 'char', 'text', 'longtext'].includes(type)
}

function isLongText(col) {
  const type = getType(col)
  return ['text', 'longtext'].includes(type)
}

function isNumberType(col) {
  const type = getType(col)
  return ['int', 'integer', 'bigint', 'decimal', 'double', 'float'].includes(type)
}

function isDateType(col) {
  const type = getType(col)
  return ['datetime', 'timestamp'].includes(type)
}

function isDateOnlyType(col) {
  return getType(col) === 'date'
}

function isBooleanType(col) {
  return getType(col) === 'tinyint'
}

function isJsonType(col) {
  return getType(col) === 'json'
}

function resetQuery() {
  pagination.page = 1
  pagination.size = 10
  pagination.total = 0
}

// 暴露 open 方法给父组件
defineExpose({ open })
</script>

<style scoped lang="scss">
.dynamic-crud-drawer {
  :deep(.el-drawer__body) {
    padding: 0;
  }
}

.crud-container {
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.crud-search {
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.crud-table {
  flex: 1;
  overflow: auto;
  min-height: 0;
}

.crud-data-table {
  min-width: 100%;
}

.crud-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.crud-form {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 12px;
}
</style>
