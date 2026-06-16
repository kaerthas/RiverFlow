<template>
  <div class="table-designer">
    <div class="designer-toolbar">
      <el-button type="primary" size="small" @click="handleAddColumn">
        <el-icon><Plus /></el-icon> 添加字段
      </el-button>
      <el-button size="small" @click="handleImportSql">
        <el-icon><Document /></el-icon> 从SQL导入
      </el-button>
    </div>

    <el-table :data="columns" stripe size="small" border max-height="480">
      <el-table-column type="index" label="序号" width="50" align="center" />
      <el-table-column label="字段编码" width="160">
        <template #default="{ row, $index }">
          <el-input v-model="row.columnCode" size="small" placeholder="column_code" />
        </template>
      </el-table-column>
      <el-table-column label="字段名称" width="140">
        <template #default="{ row }">
          <el-input v-model="row.columnName" size="small" placeholder="字段名称" />
        </template>
      </el-table-column>
      <el-table-column label="数据类型" width="130">
        <template #default="{ row }">
          <el-select v-model="row.dataType" size="small" placeholder="类型" style="width: 100%">
            <el-option label="VARCHAR" value="varchar" />
            <el-option label="INT" value="int" />
            <el-option label="BIGINT" value="bigint" />
            <el-option label="DECIMAL" value="decimal" />
            <el-option label="DATETIME" value="datetime" />
            <el-option label="DATE" value="date" />
            <el-option label="TEXT" value="text" />
            <el-option label="JSON" value="json" />
            <el-option label="TINYINT" value="tinyint" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="长度" width="80">
        <template #default="{ row }">
          <el-input-number v-model="row.length" size="small" :min="0" :controls="false" style="width: 100%" />
        </template>
      </el-table-column>
      <el-table-column label="小数位" width="70">
        <template #default="{ row }">
          <el-input-number v-model="row.decimalScale" size="small" :min="0" :controls="false" style="width: 100%" />
        </template>
      </el-table-column>
      <el-table-column label="主键" width="60" align="center">
        <template #default="{ row }">
          <el-checkbox v-model="row.isPk" :true-label="1" :false-label="0" />
        </template>
      </el-table-column>
      <el-table-column label="必填" width="60" align="center">
        <template #default="{ row }">
          <el-checkbox v-model="row.isRequired" :true-label="1" :false-label="0" />
        </template>
      </el-table-column>
      <el-table-column label="索引" width="60" align="center">
        <template #default="{ row }">
          <el-checkbox v-model="row.isIndex" :true-label="1" :false-label="0" />
        </template>
      </el-table-column>
      <el-table-column label="默认值" width="120">
        <template #default="{ row }">
          <el-input v-model="row.defaultValue" size="small" placeholder="默认值" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="70" align="center" fixed="right">
        <template #default="{ $index }">
          <el-button link type="danger" size="small" @click="handleRemove($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- SQL导入弹窗 -->
    <el-dialog v-model="sqlDialogVisible" title="从 CREATE TABLE SQL 导入字段" width="600px" destroy-on-close>
      <el-input
        v-model="sqlText"
        type="textarea"
        :rows="10"
        placeholder="粘贴 CREATE TABLE 语句，如：\nCREATE TABLE t_demo (\n  id BIGINT PRIMARY KEY,\n  name VARCHAR(100) NOT NULL\n)"
      />
      <template #footer>
        <el-button @click="sqlDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="parseSql">解析</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const columns = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const sqlDialogVisible = ref(false)
const sqlText = ref('')

function handleAddColumn() {
  const newCol = {
    columnCode: '',
    columnName: '',
    dataType: 'varchar',
    length: 255,
    decimalScale: 0,
    isPk: 0,
    isRequired: 0,
    isIndex: 0,
    defaultValue: '',
    sortNo: columns.value.length + 1
  }
  columns.value.push(newCol)
}

function handleRemove(index) {
  columns.value.splice(index, 1)
  // 重新计算排序号
  columns.value.forEach((col, idx) => {
    col.sortNo = idx + 1
  })
}

function handleImportSql() {
  sqlText.value = ''
  sqlDialogVisible.value = true
}

function parseSql() {
  try {
    const text = sqlText.value.replace(/\n/g, ' ').trim()
    // 提取括号内的内容
    const match = text.match(/\((.*)\)/s)
    if (!match) {
      ElMessage.warning('无法解析 SQL，请确保包含括号内的字段定义')
      return
    }
    const inner = match[1]
    // 按逗号分割字段定义（简单解析，不考虑嵌套）
    const defs = inner.split(',').map(s => s.trim()).filter(Boolean)
    const parsed = []
    defs.forEach(def => {
      // 跳过约束定义（PRIMARY KEY、FOREIGN KEY、INDEX 等）
      if (/^(PRIMARY|FOREIGN|UNIQUE|INDEX|KEY|CONSTRAINT)\b/i.test(def)) return
      // 解析字段名和类型
      const tokens = def.split(/\s+/)
      if (tokens.length < 2) return
      const colName = tokens[0].replace(/[`"\[\]]/g, '')
      const typeToken = tokens[1].toLowerCase()
      let dataType = 'varchar'
      let length = 255
      let decimalScale = 0

      // 提取类型和长度
      const typeMatch = typeToken.match(/(\w+)\s*\(?([^)]*)\)?/)
      if (typeMatch) {
        const rawType = typeMatch[1]
        const args = typeMatch[2]
        if (rawType.includes('varchar') || rawType.includes('char')) dataType = 'varchar'
        else if (rawType.includes('bigint')) dataType = 'bigint'
        else if (rawType.includes('int') && !rawType.includes('bigint')) dataType = 'int'
        else if (rawType.includes('decimal') || rawType.includes('numeric')) dataType = 'decimal'
        else if (rawType.includes('datetime')) dataType = 'datetime'
        else if (rawType.includes('date') && !rawType.includes('datetime')) dataType = 'date'
        else if (rawType.includes('text')) dataType = 'text'
        else if (rawType.includes('json')) dataType = 'json'
        else if (rawType.includes('tinyint')) dataType = 'tinyint'

        if (args) {
          const parts = args.split(',').map(s => parseInt(s.trim()))
          if (parts[0]) length = parts[0]
          if (parts[1]) decimalScale = parts[1]
        }
      }

      const isPk = /primary\s*key/i.test(def) ? 1 : 0
      const isRequired = /not\s*null/i.test(def) && !/default\s+null/i.test(def) ? 1 : 0
      const isIndex = /index|unique/i.test(def) ? 1 : 0

      // 字段名称：优先从 COMMENT 解析，否则使用字段编码
      let columnName = colName
      const commentMatch = def.match(/comment\s+['"]([^'"]*)['"]/i)
      if (commentMatch) {
        columnName = commentMatch[1].trim() || colName
      }

      // 默认值（支持带空格的字符串，如 '2024-01-01 00:00:00'）
      let defaultValue = ''
      const defaultMatch = def.match(/default\s+('[^']*'|"[^"]*"|\S+)/i)
      if (defaultMatch) {
        defaultValue = defaultMatch[1].replace(/^['"]|['"]$/g, '')
      }

      parsed.push({
        columnCode: colName,
        columnName,
        dataType,
        length,
        decimalScale,
        isPk,
        isRequired,
        isIndex,
        defaultValue,
        sortNo: parsed.length + 1
      })
    })

    if (parsed.length === 0) {
      ElMessage.warning('未解析到有效字段')
      return
    }
    columns.value = [...columns.value, ...parsed]
    ElMessage.success(`成功导入 ${parsed.length} 个字段`)
    sqlDialogVisible.value = false
  } catch (e) {
    ElMessage.error('SQL 解析失败：' + e.message)
  }
}

function getValidColumns() {
  return columns.value.filter(col => col.columnCode && col.columnName && col.dataType)
}

defineExpose({ getValidColumns })
</script>

<style scoped lang="scss">
.table-designer {
  .designer-toolbar {
    margin-bottom: 12px;
    display: flex;
    gap: 8px;
  }
}
</style>
