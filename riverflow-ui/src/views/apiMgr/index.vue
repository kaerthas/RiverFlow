<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">接口注册与调试</h1>
        <p class="subtitle">管理系统对外暴露的 API 接口</p>
      </div>
      <button class="btn-primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 注册接口
      </button>
    </div>

    <!-- 搜索栏 -->
    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="接口编码">
            <el-input v-model="queryForm.apiCode" placeholder="请输入接口编码" clearable />
          </el-form-item>
          <el-form-item label="接口名称">
            <el-input v-model="queryForm.apiName" placeholder="请输入接口名称" clearable />
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
      <el-table :data="apiList" stripe v-loading="loading" class="rf-data-table" :fit="false" empty-text="暂无数据">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="apiCode" label="接口编码" width="240">
          <template #default="{ row }">
            <span class="rf-code">{{ row.apiCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="apiName" label="接口名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方式" width="120" align="center">
          <template #default="{ row }">
            <span :class="['rf-tag', row.method?.toLowerCase()]">{{ row.method }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="apiType" label="类型" width="110" align="center">
          <template #default="{ row }">
            <span :class="['rf-tag', row.apiType]">
              {{ row.apiType === 'proxy' ? '代理' : row.apiType === 'sql' ? 'SQL' : row.apiType === 'script' ? '脚本' : '数据' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="请求地址" min-width="280" class-name="cell-wrap" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn success" title="调试" @click="handleDebug(row)">
                <el-icon><Promotion /></el-icon>
              </button>
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

    <!-- 注册/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="780px" top="5vh" destroy-on-close :close-on-click-modal="false" class="edit-dialog">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="base">
          <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" class="edit-form">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="接口编码" prop="apiCode">
                  <el-input v-model="form.apiCode" placeholder="如 API_001" :disabled="!!form.id" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="接口名称" prop="apiName">
                  <el-input v-model="form.apiName" placeholder="如 统一认证平台" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="接口类型" prop="apiType">
                  <el-select v-model="form.apiType" placeholder="请选择" style="width: 100%">
                    <el-option label="代理接口" value="proxy" />
                    <el-option label="SQL服务" value="sql" />
                    <el-option label="数据服务" value="data" />
                    <el-option label="脚本服务" value="script" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="请求方式" prop="method">
                  <el-select v-model="form.method" placeholder="请选择" style="width: 100%">
                    <el-option label="GET" value="GET" />
                    <el-option label="POST" value="POST" />
                    <el-option label="PUT" value="PUT" />
                    <el-option label="DELETE" value="DELETE" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="请求地址" prop="url">
              <el-input v-model="form.url" placeholder="http(s)://... 或 SQL 语句" />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="Content-Type">
                  <el-select v-model="form.contentType" placeholder="请选择" style="width: 100%">
                    <el-option label="application/json" value="application/json" />
                    <el-option label="application/x-www-form-urlencoded" value="application/x-www-form-urlencoded" />
                    <el-option label="multipart/form-data" value="multipart/form-data" />
                    <el-option label="text/xml" value="text/xml" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="认证方式">
                  <el-select v-model="form.authType" placeholder="请选择" style="width: 100%">
                    <el-option label="无" value="none" />
                    <el-option label="Basic Auth" value="basic" />
                    <el-option label="Token" value="token" />
                    <el-option label="OAuth2" value="oauth2" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="数据源">
                  <el-select v-model="form.dsId" placeholder="SQL类型时选择" clearable style="width: 100%">
                    <el-option v-for="ds in datasourceOptions" :key="ds.id" :label="ds.dsName" :value="ds.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="超时(ms)">
                  <el-input-number v-model="form.timeout" :min="1000" :max="120000" :step="1000" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="代理设置">
              <el-switch v-model="form.proxyEnabled" :active-value="1" :inactive-value="0" />
              <template v-if="form.proxyEnabled === 1">
                <el-input v-model="form.proxyHost" placeholder="代理主机" style="width: 180px; margin-left: 12px" />
                <el-input-number v-model="form.proxyPort" placeholder="端口" :min="1" :max="65535" style="width: 120px; margin-left: 8px" />
              </template>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="参数配置" name="params">
          <div class="param-toolbar">
            <el-radio-group v-model="paramTab" size="small">
              <el-radio-button label="header">Header</el-radio-button>
              <el-radio-button label="query">Query</el-radio-button>
              <el-radio-button label="body">Body</el-radio-button>
              <el-radio-button label="response">Response</el-radio-button>
            </el-radio-group>
            <el-button type="primary" size="small" @click="addParam">
              <el-icon><Plus /></el-icon> 添加
            </el-button>
          </div>
          <el-table :data="filteredParams" stripe size="small" border>
            <el-table-column label="参数键" width="160">
              <template #default="{ row }">
                <el-input v-model="row.paramKey" size="small" placeholder="key" />
              </template>
            </el-table-column>
            <el-table-column label="参数名称" width="140">
              <template #default="{ row }">
                <el-input v-model="row.paramName" size="small" placeholder="名称" />
              </template>
            </el-table-column>
            <el-table-column label="数据类型" width="110">
              <template #default="{ row }">
                <el-select v-model="row.dataType" size="small" style="width: 100%">
                  <el-option label="string" value="string" />
                  <el-option label="int" value="int" />
                  <el-option label="long" value="long" />
                  <el-option label="double" value="double" />
                  <el-option label="boolean" value="boolean" />
                  <el-option label="object" value="object" />
                  <el-option label="array" value="array" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="必填" width="60" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isRequired" :true-label="1" :false-label="0" />
              </template>
            </el-table-column>
            <el-table-column label="默认值" width="140">
              <template #default="{ row }">
                <el-input v-model="row.defaultValue" size="small" placeholder="默认值" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" size="small" @click="removeParam($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 调试弹窗 -->
    <el-dialog v-model="debugDialogVisible" title="接口调试" width="700px" destroy-on-close>
      <ApiDebugger :url="debugRow?.url" :method="debugRow?.method" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getApiCatalogList,
  saveApiCatalog,
  updateApiCatalog,
  deleteApiCatalog,
  getApiParams,
  saveApiParams
} from '@/api/apiMgr'
import { getDatasourceList } from '@/api/datasource'
import ApiDebugger from '@/components/ApiDebugger/index.vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('注册接口')
const formRef = ref(null)
const submitLoading = ref(false)
const activeTab = ref('base')
const paramTab = ref('header')
const debugDialogVisible = ref(false)
const debugRow = ref(null)

const queryForm = reactive({
  apiCode: '',
  apiName: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const apiList = ref([])
const datasourceOptions = ref([])

const form = reactive({
  id: null,
  apiCode: '',
  apiName: '',
  apiType: 'proxy',
  method: 'POST',
  url: '',
  contentType: 'application/json',
  authType: 'none',
  dsId: null,
  timeout: 30000,
  retryTimes: 0,
  proxyEnabled: 0,
  proxyHost: '',
  proxyPort: null,
  status: 0
})

const formRules = {
  apiCode: [{ required: true, message: '请输入接口编码', trigger: 'blur' }],
  apiName: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  apiType: [{ required: true, message: '请选择接口类型', trigger: 'change' }],
  method: [{ required: true, message: '请选择请求方式', trigger: 'change' }],
  url: [{ required: true, message: '请输入请求地址', trigger: 'blur' }]
}

const allParams = ref([])

const filteredParams = computed(() => {
  return allParams.value.filter(p => p.paramType === paramTab.value)
})

function addParam() {
  allParams.value.push({
    paramType: paramTab.value,
    paramKey: '',
    paramName: '',
    dataType: 'string',
    isRequired: 0,
    defaultValue: '',
    sortNo: allParams.value.length + 1
  })
}

function removeParam(index) {
  // 找到在 allParams 中的真实索引
  const typeParams = allParams.value.filter(p => p.paramType === paramTab.value)
  const target = typeParams[index]
  const realIndex = allParams.value.indexOf(target)
  if (realIndex > -1) allParams.value.splice(realIndex, 1)
}

async function loadList() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size }
    if (queryForm.apiCode) params.apiCode = queryForm.apiCode
    if (queryForm.apiName) params.apiName = queryForm.apiName
    const res = await getApiCatalogList(params)
    apiList.value = res.list || res.records || res || []
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
  queryForm.apiCode = ''
  queryForm.apiName = ''
  pagination.page = 1
  loadList()
}

async function loadDatasourceOptions() {
  try {
    const res = await getDatasourceList({ page: 1, size: 999 })
    datasourceOptions.value = res.list || res.records || res || []
  } catch (e) {
    datasourceOptions.value = []
  }
}

function handleAdd() {
  dialogTitle.value = '注册接口'
  activeTab.value = 'base'
  paramTab.value = 'header'
  Object.assign(form, {
    id: null,
    apiCode: '',
    apiName: '',
    apiType: 'proxy',
    method: 'POST',
    url: '',
    contentType: 'application/json',
    authType: 'none',
    dsId: null,
    timeout: 30000,
    retryTimes: 0,
    proxyEnabled: 0,
    proxyHost: '',
    proxyPort: null,
    status: 0
  })
  allParams.value = []
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogTitle.value = `编辑接口 - ${row.apiName}`
  activeTab.value = 'base'
  paramTab.value = 'header'
  Object.assign(form, { ...row })
  allParams.value = []
  dialogVisible.value = true
  await nextTick()
  try {
    const params = await getApiParams(row.id)
    allParams.value = Array.isArray(params) ? params : []
  } catch (e) {
    allParams.value = []
  }
}

async function handleSubmit() {
  if (activeTab.value !== 'base') {
    activeTab.value = 'base'
    await nextTick()
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    let apiId = form.id
    if (form.id) {
      await updateApiCatalog(form)
    } else {
      const res = await saveApiCatalog(form)
      apiId = res
    }
    // 保存参数
    const validParams = allParams.value.filter(p => p.paramKey)
    if (apiId && validParams.length) {
      await saveApiParams(apiId, validParams)
    }
    ElMessage.success('保存成功')
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
    await ElMessageBox.confirm(`确认删除接口「${row.apiName}」？`, '删除确认', { type: 'warning' })
    await deleteApiCatalog(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    // 取消或失败
  }
}

async function handleStatusChange(row) {
  try {
    await updateApiCatalog({ ...row, status: row.status })
    ElMessage.success(`接口已${row.status === 1 ? '启用' : '停用'}`)
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
  }
}

function handleDebug(row) {
  debugRow.value = row
  debugDialogVisible.value = true
}

onMounted(() => {
  loadDatasourceOptions()
  loadList()
})
</script>

<style scoped lang="scss">
.param-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
</style>
