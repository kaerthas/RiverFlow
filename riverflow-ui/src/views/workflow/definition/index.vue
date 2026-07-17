<template>
  <div class="rf-list-page">
    <!-- 页面头部 -->
    <div class="rf-list-header">
      <div>
        <h1 class="title">流程定义</h1>
        <p class="subtitle">管理和配置业务流程模板，支持拖拽编排与可视化设计</p>
      </div>
      <button class="btn-primary" @click="handleCreate">
        <el-icon :size="16"><Plus /></el-icon>
        <span>新建流程</span>
      </button>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="rf-search-bar">
      <div class="search-fields">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索流程名称或编码"
          clearable
          style="width: 260px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px" @change="handleSearch">
          <el-option label="已发布" :value="1" />
          <el-option label="草稿" :value="0" />
          <el-option label="已下线" :value="2" />
        </el-select>
        <el-select v-model="searchForm.triggerType" placeholder="全部触发方式" clearable style="width: 150px" @change="handleSearch">
          <el-option label="手动" value="manual" />
          <el-option label="定时" value="cron" />
          <el-option label="事件" value="event" />
        </el-select>
        <el-select v-model="searchForm.executionMode" placeholder="全部模式" clearable style="width: 130px" @change="handleSearch">
          <el-option label="异步" value="ASYNC" />
          <el-option label="同步" value="SYNC" />
        </el-select>
        <el-checkbox v-model="searchForm.showAllVersions" @change="handleSearch" style="margin-left: 8px">
          显示所有版本
        </el-checkbox>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">
          <el-icon><Search /></el-icon>
          <span>查询</span>
        </button>
        <button class="btn-reset" @click="handleReset">
          <el-icon><RefreshRight /></el-icon>
          <span>重置</span>
        </button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rf-table-card">
      <el-table :data="tableData" v-loading="loading" class="rf-data-table" :fit="false" max-height="480">
        <el-table-column type="index" label="#" width="52" align="center" />

        <el-table-column prop="flowCode" label="流程编码" width="260">
          <template #default="{ row }">
            <span class="rf-code">{{ row.flowCode }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="flowName" label="流程名称" min-width="200" show-overflow-tooltip />

        <el-table-column prop="itemCode" label="绑定事项" width="150">
          <template #default="{ row }">
            <span class="rf-mono" style="font-size: 12px; color: var(--rf-text-secondary)">{{ row.itemCode || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="triggerType" label="触发方式" width="100" align="center">
          <template #default="{ row }">
            <span :class="['rf-tag', row.triggerType || 'manual']">{{ triggerLabel(row.triggerType) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="executionMode" label="执行模式" width="100" align="center">
          <template #default="{ row }">
            <span :class="['rf-tag', row.executionMode === 'SYNC' ? 'sync' : 'async']">
              {{ executionModeLabel(row.executionMode) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="version" label="版本" width="80" align="center">
          <template #default="{ row }">
            <span class="rf-mono" style="font-size: 12px; color: var(--rf-text-muted)">v{{ row.version }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <span :class="['rf-status', statusClass(row.status)]">
              <span class="dot"></span>
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="195">
          <template #default="{ row }">
            <span class="rf-time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <el-tooltip content="设计" placement="top">
                <button class="action-btn primary" @click="handleDesign(row)">
                  <el-icon><EditPen /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 0" content="发布" placement="top">
                <button class="action-btn success" @click="handlePublish(row)">
                  <el-icon><Promotion /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 1" content="下线" placement="top">
                <button class="action-btn warning" @click="handleOffline(row)">
                  <el-icon><Download /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip content="创建新版本" placement="top">
                <button class="action-btn info" @click="handleCopyVersion(row)">
                  <el-icon><CopyDocument /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 1 && row.executionMode === 'SYNC'" content="同步调试" placement="top">
                <button class="action-btn success" @click="handleSyncDebug(row)">
                  <el-icon><VideoPlay /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip content="查看历史版本" placement="top">
                <button class="action-btn" @click="handleViewVersions(row)">
                  <el-icon><Clock /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <button class="action-btn" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <button class="action-btn danger" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                </button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <!-- 空状态插槽：替换表格默认的暂无数据 -->
        <template #empty>
          <div class="rf-empty">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.2" style="margin-bottom: 16px">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <path d="M3 9h18" />
              <path d="M9 21V9" />
            </svg>
            <div class="empty-title">暂无流程定义</div>
            <div class="empty-desc">点击右上角「新建流程」创建第一个业务流程</div>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="rf-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @update:page-size="handleSearch"
          @update:current-page="handleSearch"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑流程" width="860px" destroy-on-close class="edit-dialog">
      <el-form :model="editForm" label-width="100px" class="edit-form">
        <el-form-item label="流程编码">
          <el-input v-model="editForm.flowCode" disabled />
        </el-form-item>
        <el-form-item label="流程名称">
          <el-input v-model="editForm.flowName" placeholder="请输入流程名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="绑定事项">
          <el-input v-model="editForm.itemCode" placeholder="请输入事项编码" />
        </el-form-item>
        <el-form-item label="执行模式">
          <el-select v-model="editForm.executionMode" style="width: 100%">
            <el-option label="异步（支持定时/长流程）" value="ASYNC" />
            <el-option label="同步（仅短链路API编排）" value="SYNC" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发方式">
          <el-select v-model="editForm.triggerType" style="width: 100%">
            <el-option label="手动触发" value="manual" />
            <el-option label="定时触发" value="cron" />
            <el-option label="事件触发" value="event" />
          </el-select>
        </el-form-item>
        <el-form-item label="流程入参">
          <div class="param-table-wrapper">
            <div class="param-toolbar">
              <el-button type="primary" size="small" @click="addFlowParam">
                <el-icon><Plus /></el-icon> 添加参数
              </el-button>
            </div>
            <el-table :data="flowParams" size="small" border style="width: 100%">
              <el-table-column label="参数键" width="220">
                <template #default="{ row }">
                  <div :class="['param-key-cell', getFlowParamLevel(row) > 0 ? 'has-indent' : '']" :style="{ paddingLeft: (getFlowParamLevel(row) * 28 + 8) + 'px' }">
                    <el-input v-model="row.paramKey" size="small" placeholder="如：params.a0188" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="参数名称" width="100">
                <template #default="{ row }">
                  <el-input v-model="row.paramName" size="small" placeholder="名称" />
                </template>
              </el-table-column>
              <el-table-column label="数据类型" width="100">
                <template #default="{ row }">
                  <el-select v-model="row.dataType" size="small" style="width: 100%">
                    <el-option label="string" value="string" />
                    <el-option label="int" value="int" />
                    <el-option label="double" value="double" />
                    <el-option label="boolean" value="boolean" />
                    <el-option label="object" value="object" />
                    <el-option label="array" value="array" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="默认值" min-width="120">
                <template #default="{ row }">
                  <el-input v-model="row.defaultValue" size="small" placeholder="默认值" :disabled="row.dataType === 'object' || row.dataType === 'array'" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center">
                <template #default="{ row, $index }">
                  <el-button v-if="row.dataType === 'object' || row.dataType === 'array'" link type="primary" size="small" @click="addChildFlowParam($index)">+子项</el-button>
                  <el-button link type="danger" size="small" @click="removeFlowParam($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="flowParams.length === 0" class="param-empty">
              暂无参数，点击「添加参数」配置流程默认入参
            </div>
          </div>
        </el-form-item>
        <el-form-item label="流程出参">
          <div class="param-table-wrapper">
            <div class="param-toolbar">
              <el-button type="primary" size="small" @click="addFlowOutputParam">
                <el-icon><Plus /></el-icon> 添加参数
              </el-button>
            </div>
            <el-table :data="flowOutputParams" size="small" border style="width: 100%">
              <el-table-column label="参数键" width="220">
                <template #default="{ row }">
                  <div :class="['param-key-cell', getFlowOutputParamLevel(row) > 0 ? 'has-indent' : '']" :style="{ paddingLeft: (getFlowOutputParamLevel(row) * 28 + 8) + 'px' }">
                    <el-input v-model="row.paramKey" size="small" placeholder="如：result.code" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="参数名称" width="100">
                <template #default="{ row }">
                  <el-input v-model="row.paramName" size="small" placeholder="名称" />
                </template>
              </el-table-column>
              <el-table-column label="数据类型" width="100">
                <template #default="{ row }">
                  <el-select v-model="row.dataType" size="small" style="width: 100%">
                    <el-option label="string" value="string" />
                    <el-option label="int" value="int" />
                    <el-option label="double" value="double" />
                    <el-option label="boolean" value="boolean" />
                    <el-option label="object" value="object" />
                    <el-option label="array" value="array" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="默认值" min-width="120">
                <template #default="{ row }">
                  <el-input v-model="row.defaultValue" size="small" placeholder="默认值" :disabled="row.dataType === 'object' || row.dataType === 'array'" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center">
                <template #default="{ row, $index }">
                  <el-button v-if="row.dataType === 'object' || row.dataType === 'array'" link type="primary" size="small" @click="addChildFlowOutputParam($index)">+子项</el-button>
                  <el-button link type="danger" size="small" @click="removeFlowOutputParam($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="flowOutputParams.length === 0" class="param-empty">
              暂无参数，点击「添加参数」配置流程输出参数
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 同步调试弹窗 -->
    <el-dialog v-model="syncDebugVisible" title="同步流程调试" width="640px" destroy-on-close :close-on-click-modal="false">
      <el-form ref="syncDebugFormRef" :model="syncDebugForm" label-width="100px">
        <el-form-item label="流程编码">
          <el-input v-model="syncDebugForm.flowCode" disabled />
        </el-form-item>
        <el-form-item label="流程名称">
          <el-input v-model="syncDebugForm.flowName" disabled />
        </el-form-item>
        <el-form-item label="业务主键">
          <el-input v-model="syncDebugForm.businessKey" placeholder="可选，如办件流水号" />
        </el-form-item>
        <el-form-item label="超时(ms)">
          <el-input-number v-model="syncDebugForm.timeoutMs" :min="1000" :max="120000" :step="1000" style="width: 160px" />
          <span style="margin-left: 8px; color: var(--rf-text-muted); font-size: 12px">默认 30000ms，最大 120000ms</span>
        </el-form-item>
        <el-form-item label="上下文变量">
          <el-input
            v-model="syncDebugForm.variablesJson"
            type="textarea"
            :rows="6"
            placeholder="请输入 JSON 格式的上下文变量，例如：&#10;{&#10;  &quot;idCard&quot;: &quot;310101199001011234&quot;,&#10;  &quot;type&quot;: &quot;personal&quot;&#10;}"
          />
        </el-form-item>
      </el-form>

      <!-- 执行结果 -->
      <div v-if="syncDebugResult !== null" class="sync-result">
        <div class="sync-result-header">
          <span class="sync-result-title">执行结果</span>
          <el-tag v-if="syncDebugSuccess" type="success" size="small">成功</el-tag>
          <el-tag v-else type="danger" size="small">失败</el-tag>
        </div>
        <pre class="sync-result-body">{{ JSON.stringify(syncDebugResult, null, 2) }}</pre>
      </div>

      <template #footer>
        <el-button @click="syncDebugVisible = false">关闭</el-button>
        <el-button type="primary" @click="confirmSyncDebug" :loading="syncDebugLoading">执行</el-button>
      </template>
    </el-dialog>

    <!-- 历史版本弹窗 -->
    <el-dialog v-model="versionVisible" :title="`历史版本 - ${versionFlowName}`" width="720px" destroy-on-close>
      <el-table :data="versionData" v-loading="versionLoading" size="small">
        <el-table-column prop="version" label="版本" width="80" align="center">
          <template #default="{ row }">
            <span class="rf-mono">v{{ row.version }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="flowName" label="流程名称" min-width="180" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['rf-status', statusClass(row.status)]">
              <span class="dot"></span>
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            <span class="rf-time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDesign(row)">设计</el-button>
            <el-button link type="primary" size="small" @click="handleCopyVersion(row)">复制</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFlowDefinitionList,
  publishFlowDefinition,
  offlineFlowDefinition,
  deleteFlowDefinition,
  saveFlowDefinition,
  copyFlowDefinition,
  getFlowVersions,
  executeSyncFlow
} from '@/api/workflow'

const router = useRouter()
const loading = ref(false)
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({ id: null, flowCode: '', flowName: '', itemCode: '', executionMode: 'ASYNC', triggerType: 'manual', inputParams: '', outputParams: '' })

// 流程入参表格数据（与 editForm.inputParams 双向转换）
const flowParams = ref([])

// 流程出参表格数据（与 editForm.outputParams 双向转换）
const flowOutputParams = ref([])

const searchForm = reactive({ keyword: '', status: '', triggerType: '', executionMode: '', showAllVersions: false })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

// 同步调试
const syncDebugVisible = ref(false)
const syncDebugLoading = ref(false)
const syncDebugFormRef = ref(null)
const syncDebugForm = reactive({ flowCode: '', flowName: '', businessKey: '', timeoutMs: 30000, variablesJson: '' })
const syncDebugResult = ref(null)
const syncDebugSuccess = ref(false)

// 历史版本
const versionVisible = ref(false)
const versionLoading = ref(false)
const versionFlowName = ref('')
const versionData = ref([])

function triggerLabel(type) {
  const map = { manual: '手动', cron: '定时', event: '事件' }
  return map[type] || '手动'
}

function executionModeLabel(mode) {
  const map = { ASYNC: '异步', SYNC: '同步' }
  return map[mode] || '异步'
}

function statusLabel(status) {
  const map = { 1: '已发布', 0: '草稿', 2: '已下线' }
  return map[status] || '未知'
}

function statusClass(status) {
  const map = { 1: 'published', 0: 'draft', 2: 'offline' }
  return map[status] || 'draft'
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

async function handleSearch() {
  loading.value = true
  try {
    const res = await getFlowDefinitionList({
      page: pagination.page,
      size: pagination.size,
      flowName: searchForm.keyword || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined,
      triggerType: searchForm.triggerType || undefined,
      executionMode: searchForm.executionMode || undefined,
      showAllVersions: searchForm.showAllVersions || undefined
    })
    if (res && res.records) {
      tableData.value = res.records
      pagination.total = Number(res.total) || 0
    }
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.triggerType = ''
  searchForm.executionMode = ''
  searchForm.showAllVersions = false
  pagination.page = 1
  handleSearch()
}

function handleCreate() {
  router.push('/workflow/designer')
}

function handleDesign(row) {
  router.push({ path: '/workflow/designer', query: { id: row.id } })
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm(`确认发布流程「${row.flowName}」v${row.version}？发布后该版本将不可修改。`, '发布确认', { type: 'warning' })
    const newId= await publishFlowDefinition(row.id)
    // 如果返回了新的ID，说明创建了新版，跳转过去
    if (newId && newId !== row.id) {
      ElMessage.success('已创建新版本并发布')
      router.push({ path: '/workflow/designer', query: { id: newId } })
    } else {
      row.status = 1
      ElMessage.success('流程发布成功')
    }
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('发布失败: ' + (e.message || e))
  }
}

async function handleOffline(row) {
  try {
    await ElMessageBox.confirm(`确认下线流程「${row.flowName}」v${row.version}？`, '下线确认', { type: 'warning' })
    await offlineFlowDefinition(row.id)
    row.status = 2
    ElMessage.success('流程已下线')
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('下线失败: ' + (e.message || e))
  }
}

async function handleCopyVersion(row) {
  try {
    await ElMessageBox.confirm(`基于「${row.flowName}」v${row.version} 创建新版本？`, '创建新版本', { type: 'info' })
    const newId = await copyFlowDefinition(row.id)
    ElMessage.success('新版本创建成功')
    router.push({ path: '/workflow/designer', query: { id: newId } })
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('创建失败: ' + (e.message || e))
  }
}

async function handleViewVersions(row) {
  versionFlowName.value = row.flowName
  versionVisible.value = true
  versionLoading.value = true
  try {
    const res = await getFlowVersions(row.flowCode)
    versionData.value = res || []
  } catch (e) {
    ElMessage.error('加载历史版本失败')
  } finally {
    versionLoading.value = false
  }
}

function handleEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    flowCode: row.flowCode,
    flowName: row.flowName,
    itemCode: row.itemCode,
    executionMode: row.executionMode || 'ASYNC',
    triggerType: row.triggerType || 'manual',
    version: row.version,
    inputParams: row.inputParams || '',
    outputParams: row.outputParams || ''
  })
  flowParams.value = parseInputParamsToTable(row.inputParams)
  flowOutputParams.value = parseInputParamsToTable(row.outputParams)
  editVisible.value = true
}

function convertParamValue(val, type) {
  if (type === 'int') return parseInt(val, 10) || 0
  if (type === 'double') return parseFloat(val) || 0
  if (type === 'boolean') return val === 'true' || val === '1' || val === true
  if (type === 'object') return {}
  if (type === 'array') return []
  return val
}

function buildObjectValue(params, parentIdx) {
  const parent = params[parentIdx]
  const isArray = parent && parent.dataType === 'array'
  const item = {}
  params.forEach((p, idx) => {
    if (p.parentIndex !== parentIdx) return
    if (!p.paramKey || !p.paramKey.trim()) return
    if (p.dataType === 'object' || p.dataType === 'array') {
      item[p.paramKey] = buildObjectValue(params, idx)
    } else {
      item[p.paramKey] = convertParamValue(p.defaultValue, p.dataType)
    }
  })
  return isArray ? (Object.keys(item).length > 0 ? [item] : []) : item
}

function unflattenParams(params) {
  const result = {}
  params.forEach((p, idx) => {
    if (p.parentIndex !== undefined && p.parentIndex !== null) return
    if (!p.paramKey || !p.paramKey.trim()) return
    if (p.dataType === 'object' || p.dataType === 'array') {
      result[p.paramKey] = buildObjectValue(params, idx)
    } else {
      result[p.paramKey] = convertParamValue(p.defaultValue, p.dataType)
    }
  })
  return result
}

function flattenObjectToTable(obj, parentIndex = null, result = []) {
  if (obj === null || typeof obj !== 'object') return result
  if (Array.isArray(obj)) {
    const currentIndex = result.length
    result.push({ paramKey: '', paramName: '', dataType: 'array', defaultValue: '', parentIndex })
    if (obj.length > 0 && obj[0] !== null && typeof obj[0] === 'object' && !Array.isArray(obj[0])) {
      flattenObjectToTable(obj[0], currentIndex, result)
    }
    return result
  }
  for (const [key, value] of Object.entries(obj)) {
    const currentIndex = result.length
    if (Array.isArray(value)) {
      result.push({ paramKey: key, paramName: '', dataType: 'array', defaultValue: '', parentIndex })
      if (value.length > 0 && value[0] !== null && typeof value[0] === 'object' && !Array.isArray(value[0])) {
        flattenObjectToTable(value[0], currentIndex, result)
      }
    } else if (value !== null && typeof value === 'object') {
      result.push({ paramKey: key, paramName: '', dataType: 'object', defaultValue: '', parentIndex })
      flattenObjectToTable(value, currentIndex, result)
    } else {
      let dataType = 'string'
      if (typeof value === 'number') dataType = Number.isInteger(value) ? 'int' : 'double'
      else if (typeof value === 'boolean') dataType = 'boolean'
      result.push({ paramKey: key, paramName: '', dataType, defaultValue: String(value), parentIndex })
    }
  }
  return result
}

function parseInputParamsToTable(inputParams) {
  if (!inputParams) return []
  try {
    const obj = JSON.parse(inputParams)
    if (typeof obj !== 'object' || obj === null || Array.isArray(obj)) return []
    return flattenObjectToTable(obj)
  } catch (e) {
    return []
  }
}

function addFlowParam() {
  flowParams.value.push({
    paramKey: '',
    paramName: '',
    dataType: 'string',
    defaultValue: '',
    parentIndex: undefined
  })
}

function getFlowParamLevel(row) {
  let level = 0
  let current = row
  while (current && current.parentIndex !== undefined && current.parentIndex !== null) {
    const parent = flowParams.value[current.parentIndex]
    if (!parent) break
    level++
    current = parent
  }
  return level
}

function addChildFlowParam(parentIndex) {
  const parent = flowParams.value[parentIndex]
  if (!parent) return
  flowParams.value.splice(parentIndex + 1, 0, {
    paramKey: '',
    paramName: '',
    dataType: 'string',
    defaultValue: '',
    parentIndex: parentIndex
  })
}

function removeFlowParam(index) {
  const target = flowParams.value[index]
  if (!target) return
  // 收集所有要删除的索引（自身 + 所有后代）
  const toDelete = new Set([index])
  function collect(parentIdx) {
    flowParams.value.forEach((p, idx) => {
      if (toDelete.has(idx)) return
      if (p.parentIndex === parentIdx) {
        toDelete.add(idx)
        collect(idx)
      }
    })
  }
  collect(index)
  // 按从大到小排序，依次删除
  const sorted = Array.from(toDelete).sort((a, b) => b - a)
  sorted.forEach(idx => {
    flowParams.value.splice(idx, 1)
  })
  // 更新幸存者的 parentIndex
  flowParams.value.forEach(p => {
    if (p.parentIndex === undefined || p.parentIndex === null) return
    let offset = 0
    sorted.forEach(d => {
      if (d < p.parentIndex) offset++
    })
    p.parentIndex -= offset
  })
}

// ========== 流程出参相关方法 ==========
function addFlowOutputParam() {
  flowOutputParams.value.push({
    paramKey: '',
    paramName: '',
    dataType: 'string',
    defaultValue: '',
    parentIndex: undefined
  })
}

function getFlowOutputParamLevel(row) {
  let level = 0
  let current = row
  while (current && current.parentIndex !== undefined && current.parentIndex !== null) {
    const parent = flowOutputParams.value[current.parentIndex]
    if (!parent) break
    level++
    current = parent
  }
  return level
}

function addChildFlowOutputParam(parentIndex) {
  const parent = flowOutputParams.value[parentIndex]
  if (!parent) return
  flowOutputParams.value.splice(parentIndex + 1, 0, {
    paramKey: '',
    paramName: '',
    dataType: 'string',
    defaultValue: '',
    parentIndex: parentIndex
  })
}

function removeFlowOutputParam(index) {
  const target = flowOutputParams.value[index]
  if (!target) return
  const toDelete = new Set([index])
  function collect(parentIdx) {
    flowOutputParams.value.forEach((p, idx) => {
      if (toDelete.has(idx)) return
      if (p.parentIndex === parentIdx) {
        toDelete.add(idx)
        collect(idx)
      }
    })
  }
  collect(index)
  const sorted = Array.from(toDelete).sort((a, b) => b - a)
  sorted.forEach(idx => {
    flowOutputParams.value.splice(idx, 1)
  })
  flowOutputParams.value.forEach(p => {
    if (p.parentIndex === undefined || p.parentIndex === null) return
    let offset = 0
    sorted.forEach(d => {
      if (d < p.parentIndex) offset++
    })
    p.parentIndex -= offset
  })
}

async function confirmEdit() {
  editLoading.value = true
  try {
    // 将参数表格组装为 JSON
    const built = unflattenParams(flowParams.value)
    const inputParamsJson = Object.keys(built).length > 0 ? JSON.stringify(built) : ''
    editForm.inputParams = inputParamsJson

    const outputBuilt = unflattenParams(flowOutputParams.value)
    const outputParamsJson = Object.keys(outputBuilt).length > 0 ? JSON.stringify(outputBuilt) : ''
    editForm.outputParams = outputParamsJson

    await saveFlowDefinition({
      id: editForm.id,
      flowName: editForm.flowName,
      itemCode: editForm.itemCode,
      executionMode: editForm.executionMode,
      triggerType: editForm.triggerType,
      version: editForm.version,
      inputParams: editForm.inputParams || undefined,
      outputParams: editForm.outputParams || undefined
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    handleSearch()
  } catch (e) {
    ElMessage.error('保存失败: ' + (e.message || e))
  } finally {
    editLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除流程「${row.flowName}」v${row.version}？删除后不可恢复。`, '删除确认', { type: 'warning' })
    await deleteFlowDefinition(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败: ' + (e.message || e))
  }
}

function handleSyncDebug(row) {
  // 如果流程定义了默认入参，格式化后带入调试窗口
  let defaultVars = ''
  if (row.inputParams) {
    try {
      defaultVars = JSON.stringify(JSON.parse(row.inputParams), null, 2)
    } catch (e) {
      defaultVars = row.inputParams
    }
  }
  Object.assign(syncDebugForm, {
    flowCode: row.flowCode,
    flowName: row.flowName,
    businessKey: '',
    timeoutMs: 30000,
    variablesJson: defaultVars
  })
  syncDebugResult.value = null
  syncDebugSuccess.value = false
  syncDebugVisible.value = true
}

async function confirmSyncDebug() {
  let variables = null
  if (syncDebugForm.variablesJson && syncDebugForm.variablesJson.trim()) {
    try {
      variables = JSON.parse(syncDebugForm.variablesJson.trim())
      if (typeof variables !== 'object' || variables === null || Array.isArray(variables)) {
        ElMessage.warning('上下文变量必须是 JSON 对象')
        return
      }
    } catch (e) {
      ElMessage.warning('上下文变量 JSON 格式错误: ' + e.message)
      return
    }
  }

  syncDebugLoading.value = true
  syncDebugResult.value = null
  try {
    const res = await executeSyncFlow({
      flowCode: syncDebugForm.flowCode,
      businessKey: syncDebugForm.businessKey || undefined,
      variables: variables || undefined,
      timeoutMs: syncDebugForm.timeoutMs
    })
    syncDebugResult.value = res
    syncDebugSuccess.value = true
    ElMessage.success('同步执行成功')
  } catch (e) {
    syncDebugResult.value = { error: e.message || '执行失败' }
    syncDebugSuccess.value = false
  } finally {
    syncDebugLoading.value = false
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.edit-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px 12px;
    margin-right: 0;
    border-bottom: 1px solid #f1f5f9;

    .el-dialog__title {
      font-size: 16px;
      font-weight: 700;
      color: var(--rf-text-main);
      letter-spacing: -0.01em;
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 12px 24px 20px;
    border-top: 1px solid #f1f5f9;
  }
}

.edit-form {
  :deep(.el-form-item__label) {
    font-size: 13px;
    font-weight: 500;
    color: var(--rf-text-secondary);
  }
}

// 空状态插槽样式
.rf-data-table {
  :deep(.el-table__empty-block) {
    // 去掉全局样式带来的虚线圆圈伪元素
    &::before {
      display: none !important;
    }
  }
}

.param-table-wrapper {
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
}
.param-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}
.param-empty {
  text-align: center;
  color: #94a3b8;
  font-size: 12px;
  padding: 16px 0;
}
.param-key-cell {
  position: relative;
}
.param-key-cell.has-indent {
  border-left: 2px solid #94a3b8;
  margin-left: 4px;
  padding-left: 10px !important;
}

.rf-actions {
  .action-btn.info {
    color: #3b82f6;
    &:hover { background: #dbeafe; }
  }
}

.sync-result {
  margin-top: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;

  .sync-result-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;

    .sync-result-title {
      font-size: 13px;
      font-weight: 600;
      color: var(--rf-text-main);
    }
  }

  .sync-result-body {
    margin: 0;
    padding: 14px;
    max-height: 320px;
    overflow: auto;
    font-size: 12px;
    line-height: 1.6;
    color: var(--rf-text-secondary);
    background: #ffffff;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  }
}
</style>
