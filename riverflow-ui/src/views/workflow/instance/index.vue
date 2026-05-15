<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">流程实例监控</h1>
        <p class="subtitle">查看和管理流程实例的运行状态</p>
      </div>
      <button class="btn-primary" @click="handleStartDialog">
        <el-icon><VideoPlay /></el-icon> 启动实例
      </button>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="流程编码">
            <el-input v-model="queryForm.flowCode" placeholder="请输入流程编码" clearable />
          </el-form-item>
          <el-form-item label="业务主键">
            <el-input v-model="queryForm.businessKey" placeholder="请输入业务主键" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 120px">
              <el-option label="运行中" value="running" />
              <el-option label="已完成" value="completed" />
              <el-option label="已挂起" value="suspended" />
              <el-option label="失败" value="failed" />
              <el-option label="已终止" value="terminated" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">查询</button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableData" class="rf-data-table" :fit="false" v-loading="loading" empty-text="暂无数据">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column label="实例ID" width="240">
          <template #default="{ row }">
            <span class="rf-code">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="流程编码" width="220">
          <template #default="{ row }">
            <span class="rf-code">{{ row.flowCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="业务主键">
          <template #default="{ row }">
            <span class="rf-mono">{{ row.businessKey }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="currentNodeId" label="当前节点" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'running'" class="rf-status running"><span class="dot"></span>运行中</span>
            <span v-else-if="row.status === 'completed'" class="rf-status success"><span class="dot"></span>已完成</span>
            <span v-else-if="row.status === 'suspended'" class="rf-status warning"><span class="dot"></span>已挂起</span>
            <span v-else-if="row.status === 'failed'" class="rf-status failed"><span class="dot"></span>失败</span>
            <span v-else-if="row.status === 'terminated'" class="rf-status offline"><span class="dot"></span>已终止</span>
            <span v-else class="rf-status">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="启动时间" width="185">
          <template #default="{ row }">
            <span class="rf-time">{{ formatTime(row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" title="详情" @click="handleDetail(row)">
                <el-icon><View /></el-icon>
              </button>
              <!-- 运行中：执行、挂起、终止 -->
              <button class="action-btn success" title="执行" @click="handleExecute(row)" v-if="row.status === 'running'">
                <el-icon><VideoPlay /></el-icon>
              </button>
              <button class="action-btn warning" title="挂起" @click="handleSuspend(row)" v-if="row.status === 'running'">
                <el-icon><VideoPause /></el-icon>
              </button>
              <button class="action-btn danger" title="终止" @click="handleTerminate(row)" v-if="row.status === 'running'">
                <el-icon><CircleClose /></el-icon>
              </button>
              <!-- 已挂起：继续、终止 -->
              <button class="action-btn success" title="继续" @click="handleResume(row)" v-if="row.status === 'suspended'">
                <el-icon><RefreshRight /></el-icon>
              </button>
              <button class="action-btn danger" title="终止" @click="handleTerminate(row)" v-if="row.status === 'suspended'">
                <el-icon><CircleClose /></el-icon>
              </button>
              <!-- 失败：重试、终止 -->
              <button class="action-btn success" title="重试" @click="handleRetry(row)" v-if="row.status === 'failed'">
                <el-icon><Refresh /></el-icon>
              </button>
              <button class="action-btn danger" title="终止" @click="handleTerminate(row)" v-if="row.status === 'failed'">
                <el-icon><CircleClose /></el-icon>
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
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @update:page-size="handleSearch"
          @update:current-page="handleSearch"
        />
      </div>
    </div>

    <!-- 启动实例弹窗 -->
    <el-dialog v-model="startDialogVisible" title="手动启动流程实例" width="520px" destroy-on-close :close-on-click-modal="false">
      <el-form ref="startFormRef" :model="startForm" :rules="startFormRules" label-width="100px">
        <el-form-item label="流程定义" prop="flowId">
          <el-select v-model="startForm.flowId" placeholder="请选择要启动的流程" clearable style="width: 100%">
            <el-option
              v-for="flow in flowDefinitionOptions"
              :key="flow.id"
              :label="flow.flowName"
              :value="flow.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="业务主键" prop="businessKey">
          <el-input v-model="startForm.businessKey" placeholder="如办件流水号、 receiptNo 等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStartSubmit">启动</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="流程实例详情" width="900px" class="edit-dialog" destroy-on-close>
      <div v-if="currentInstance" class="instance-detail">
        <el-row :gutter="16" class="info-row">
          <el-col :span="8"><span class="label">实例ID:</span> {{ currentInstance.id }}</el-col>
          <el-col :span="8"><span class="label">流程编码:</span> {{ currentInstance.flowCode }}</el-col>
          <el-col :span="8"><span class="label">业务主键:</span> {{ currentInstance.businessKey }}</el-col>
        </el-row>
        <el-row :gutter="16" class="info-row">
          <el-col :span="8">
            <span class="label">状态:</span>
            <el-tag :type="statusType(currentInstance.status)">{{ statusText(currentInstance.status) }}</el-tag>
          </el-col>
          <el-col :span="8"><span class="label">当前节点:</span> {{ currentInstance.currentNodeId }}</el-col>
          <el-col :span="8"><span class="label">启动时间:</span> {{ currentInstance.startTime }}</el-col>
        </el-row>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="执行日志" name="logs">
            <el-timeline>
              <el-timeline-item
                v-for="log in instanceLogs"
                :key="log.id"
                :type="log.logType === 'error' ? 'danger' : 'primary'"
                :timestamp="log.createTime"
              >
                <div class="log-item">
                  <strong>{{ log.nodeId || log.logType }}</strong>
                  <p class="log-msg">{{ log.logContent }}</p>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
          <el-tab-pane label="任务列表" name="tasks">
            <el-table :data="instanceTasks" stripe size="small">
              <el-table-column prop="nodeName" label="节点" />
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="taskStatusType(row.status)" size="small">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="executeCount" label="执行次数" width="90" />
              <el-table-column prop="errorMsg" label="错误信息" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, VideoPlay, CircleClose, VideoPause, RefreshRight, Refresh } from '@element-plus/icons-vue'
import {
  getFlowInstanceList,
  getFlowInstanceDetail,
  executeFlowInstance,
  terminateFlowInstance,
  suspendFlowInstance,
  resumeFlowInstance,
  retryFlowInstance,
  getInstanceTasks,
  getInstanceLogs,
  getFlowDefinitionList,
  startFlowInstance
} from '@/api/workflow'

const loading = ref(false)
const detailVisible = ref(false)
const startDialogVisible = ref(false)
const currentInstance = ref(null)
const activeTab = ref('logs')
const instanceLogs = ref([])
const instanceTasks = ref([])
const flowDefinitionOptions = ref([])

const startForm = reactive({
  flowId: null,
  businessKey: ''
})
const startFormRef = ref(null)
const startFormRules = {
  flowId: [{ required: true, message: '请选择流程定义', trigger: 'change' }],
  businessKey: [{ required: true, message: '请输入业务主键', trigger: 'blur' }]
}

const queryForm = reactive({
  flowCode: '',
  businessKey: '',
  status: ''
})

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

async function handleSearch() {
  loading.value = true
  try {
    const res = await getFlowInstanceList({
      page: pagination.page,
      size: pagination.size,
      ...queryForm
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
  Object.assign(queryForm, { flowCode: '', businessKey: '', status: '' })
  handleSearch()
}

async function handleDetail(row) {
  currentInstance.value = row
  detailVisible.value = true
  try {
    const [logsRes, tasksRes] = await Promise.all([
      getInstanceLogs(row.id),
      getInstanceTasks(row.id)
    ])
    instanceLogs.value = logsRes || []
    instanceTasks.value = tasksRes || []
  } catch (e) {
    console.error('加载详情失败', e)
  }
}

async function handleStartDialog() {
  startForm.flowId = null
  startForm.businessKey = ''
  startDialogVisible.value = true
  // 加载已发布的流程定义
  try {
    const res = await getFlowDefinitionList({ page: 1, size: 999, status: 1 })
    flowDefinitionOptions.value = res.list || res.records || res || []
  } catch (e) {
    flowDefinitionOptions.value = []
  }
}

async function handleStartSubmit() {
  const valid = await startFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await startFlowInstance(startForm.flowId, startForm.businessKey)
    ElMessage.success('流程实例启动成功')
    startDialogVisible.value = false
    handleSearch()
  } catch (e) {
    ElMessage.error('启动失败: ' + (e.message || '未知错误'))
  }
}

async function handleExecute(row) {
  try {
    await executeFlowInstance(row.id)
    ElMessage.success('执行成功')
    handleSearch()
  } catch (e) {
    ElMessage.error('执行失败: ' + e.message)
  }
}

async function handleTerminate(row) {
  try {
    await ElMessageBox.confirm(`确认终止实例「${row.id}」？`, '终止确认', { type: 'warning' })
    await terminateFlowInstance(row.id)
    ElMessage.success('实例已终止')
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('终止失败: ' + e.message)
  }
}

async function handleSuspend(row) {
  try {
    await ElMessageBox.confirm(`确认挂起实例「${row.id}」？`, '挂起确认', { type: 'warning' })
    await suspendFlowInstance(row.id)
    ElMessage.success('实例已挂起')
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('挂起失败: ' + e.message)
  }
}

async function handleResume(row) {
  try {
    await ElMessageBox.confirm(`确认继续执行实例「${row.id}」？`, '继续确认', { type: 'info' })
    await resumeFlowInstance(row.id)
    ElMessage.success('实例已继续执行')
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('继续失败: ' + e.message)
  }
}

async function handleRetry(row) {
  try {
    await ElMessageBox.confirm(`确认重试实例「${row.id}」？`, '重试确认', { type: 'info' })
    await retryFlowInstance(row.id)
    ElMessage.success('实例已重试')
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('重试失败: ' + e.message)
  }
}

function formatTime(time) {
  return time ? time.replace('T', ' ').substring(0, 19) : '-'
}

function statusType(status) {
  const map = { running: 'primary', completed: 'success', waiting: 'warning', failed: 'danger', suspended: 'info', terminated: 'info' }
  return map[status] || 'info'
}

function statusText(status) {
  const map = { running: '运行中', completed: '已完成', waiting: '等待中', failed: '失败', suspended: '已挂起', terminated: '已终止' }
  return map[status] || status
}

function taskStatusType(status) {
  const map = { pending: 'info', running: 'primary', success: 'success', fail: 'danger', waiting: 'warning', skipped: 'info' }
  return map[status] || 'info'
}

handleSearch()
</script>

<style scoped lang="scss">
.instance-detail {
  .info-row {
    margin-bottom: 12px;
    .label { color: #8C8C8C; margin-right: 8px; }
  }

  .detail-tabs {
    margin-top: 20px;
  }

  .log-item {
    strong { margin-right: 8px; color: #262626; }
    .log-msg { margin: 6px 0 0; font-size: 13px; color: #595959; line-height: 1.5; }
  }
}
</style>
