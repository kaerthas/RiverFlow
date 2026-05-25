<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">脚本管理</h1>
        <p class="subtitle">维护 Groovy 脚本库，供接口注册与工作流节点复用</p>
      </div>
      <button class="btn-primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增脚本
      </button>
    </div>

    <!-- 搜索栏 -->
    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="脚本编码">
            <el-input v-model="queryForm.scriptCode" placeholder="请输入脚本编码" clearable />
          </el-form-item>
          <el-form-item label="脚本名称">
            <el-input v-model="queryForm.scriptName" placeholder="请输入脚本名称" clearable />
          </el-form-item>
          <el-form-item label="脚本类型">
            <el-select v-model="queryForm.scriptType" placeholder="请选择" clearable style="width: 160px">
              <el-option label="结果处理" value="result" />
              <el-option label="格式化" value="format" />
              <el-option label="请求头" value="header" />
              <el-option label="条件判断" value="condition" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">
          <el-icon><Search /></el-icon> 查询
        </button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="scriptList" stripe v-loading="loading" class="rf-data-table" :fit="false" empty-text="暂无数据">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="scriptCode" label="脚本编码" width="200">
          <template #default="{ row }">
            <span class="rf-code">{{ row.scriptCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="scriptName" label="脚本名称" min-width="320" show-overflow-tooltip />
        <el-table-column prop="scriptType" label="类型" width="110" align="center">
          <template #default="{ row }">
            <span class="rf-tag" :class="row.scriptType">
              {{ typeMap[row.scriptType] || row.scriptType }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center">
          <template #default="{ row }">
            <span class="rf-time">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" title="编辑" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button class="action-btn danger" title="删除" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="rf-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @update:page-size="handleSearch"
          @update:current-page="handleSearch"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" top="5vh" destroy-on-close :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="脚本编码" prop="scriptCode">
              <el-input v-model="form.scriptCode" placeholder="如 SCRIPT_001" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="脚本名称" prop="scriptName">
              <el-input v-model="form.scriptName" placeholder="如 统一结果处理" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="脚本类型" prop="scriptType">
              <el-select v-model="form.scriptType" placeholder="请选择" style="width: 100%">
                <el-option label="结果处理" value="result" />
                <el-option label="格式化" value="format" />
                <el-option label="请求头" value="header" />
                <el-option label="条件判断" value="condition" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="脚本内容" prop="scriptContent">
          <el-input
            v-model="form.scriptContent"
            type="textarea"
            :rows="16"
            placeholder="请输入 Groovy 脚本内容..."
            style="font-family: monospace; font-size: 13px;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getApiScriptList,
  getApiScriptDetail,
  saveApiScript,
  updateApiScript,
  deleteApiScript
} from '@/api/apiMgr'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增脚本')
const formRef = ref(null)
const submitLoading = ref(false)

const queryForm = reactive({
  scriptCode: '',
  scriptName: '',
  scriptType: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const scriptList = ref([])

const typeMap = {
  result: '结果处理',
  format: '格式化',
  header: '请求头',
  condition: '条件判断'
}

const form = reactive({
  id: null,
  scriptCode: '',
  scriptName: '',
  scriptType: 'result',
  scriptContent: '',
  status: 1
})

const formRules = {
  scriptCode: [{ required: true, message: '请输入脚本编码', trigger: 'blur' }],
  scriptName: [{ required: true, message: '请输入脚本名称', trigger: 'blur' }],
  scriptType: [{ required: true, message: '请选择脚本类型', trigger: 'change' }],
  scriptContent: [{ required: true, message: '请输入脚本内容', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (queryForm.scriptCode) params.scriptCode = queryForm.scriptCode
    if (queryForm.scriptName) params.scriptName = queryForm.scriptName
    if (queryForm.scriptType) params.scriptType = queryForm.scriptType
    const res = await getApiScriptList(params)
    scriptList.value = res.list || res.records || res || []
    pagination.total = Number(res.total) || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadList()
}

function handleReset() {
  queryForm.scriptCode = ''
  queryForm.scriptName = ''
  queryForm.scriptType = ''
  pagination.page = 1
  loadList()
}

function handleAdd() {
  dialogTitle.value = '新增脚本'
  Object.assign(form, {
    id: null,
    scriptCode: '',
    scriptName: '',
    scriptType: 'result',
    scriptContent: '',
    status: 1
  })
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogTitle.value = `编辑脚本 - ${row.scriptName}`
  try {
    const res = await getApiScriptDetail(row.id)
    Object.assign(form, {
      id: res.id,
      scriptCode: res.scriptCode,
      scriptName: res.scriptName,
      scriptType: res.scriptType,
      scriptContent: res.scriptContent || '',
      status: res.status ?? 1
    })
    dialogVisible.value = true
  } catch (e) {
    // 错误已由 request 拦截器提示
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateApiScript(form)
      ElMessage.success('修改成功')
    } else {
      await saveApiScript(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除脚本「${row.scriptName}」？`, '删除确认', { type: 'warning' })
    await deleteApiScript(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    // 取消或失败
  }
}

async function handleStatusChange(row) {
  try {
    await updateApiScript({ id: row.id, status: row.status })
    ElMessage.success(`脚本已${row.status === 1 ? '启用' : '停用'}`)
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped lang="scss">
.rf-tag {
  &.result {
    background: #dbeafe;
    color: #2563eb;
  }
  &.format {
    background: #fef3c7;
    color: #d97706;
  }
  &.header {
    background: #d1fae5;
    color: #059669;
  }
  &.condition {
    background: #f3e8ff;
    color: #7c3aed;
  }
}
</style>
