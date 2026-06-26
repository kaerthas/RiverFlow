<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">{{ $t('instance.流程实例监控_963c32be') }}</h1>
        <p class="subtitle">{{ $t('instance.查看和管理流_e70d81e3') }}</p>
      </div>
      <button class="btn-primary" @click="handleStartDialog">
        <el-icon><VideoPlay /></el-icon>{{ $t('instance.启动实例_a4d87739') }}</button>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item :label="$t('instance.流程编码_45668968')">
            <el-input v-model="queryForm.flowCode" :placeholder="$t('instance.请输入流程编_bd43ad9c')" clearable />
          </el-form-item>
          <el-form-item :label="$t('instance.业务主键_21cb4583')">
            <el-input v-model="queryForm.businessKey" :placeholder="$t('instance.请输入业务主_42cfcf0b_1')" clearable />
          </el-form-item>
          <el-form-item :label="$t('instance.状态_3fea7ca7')">
            <el-select v-model="queryForm.status" :placeholder="$t('instance.全部状态_443483c9')" clearable style="width: 120px">
              <el-option :label="$t('instance.运行中_d679aea3_1')" value="running" />
              <el-option :label="$t('instance.已完成_fad5222c_1')" value="completed" />
              <el-option :label="$t('instance.已挂起_8f2b3e77_1')" value="suspended" />
              <el-option :label="$t('instance.失败_acd5cb84_1')" value="failed" />
              <el-option :label="$t('instance.已终止_2554120a_1')" value="terminated" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">{{ $t('instance.查询_bee912d7') }}</button>
        <button class="btn-reset" @click="handleReset">{{ $t('instance.重置_4b9c3271') }}</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableData" class="rf-data-table" :fit="false" v-loading="loading" :empty-text="$t('instance.暂无数据_21efd88b')">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column :label="$t('instance.状态_3fea7ca7_1')" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'running'" class="rf-status running"><span class="dot"></span>{{ $t('instance.运行中_d679aea3_1') }}</span>
            <span v-else-if="row.status === 'completed'" class="rf-status success"><span class="dot"></span>{{ $t('instance.已完成_fad5222c_1') }}</span>
            <span v-else-if="row.status === 'suspended'" class="rf-status warning"><span class="dot"></span>{{ $t('instance.已挂起_8f2b3e77_1') }}</span>
            <span v-else-if="row.status === 'failed'" class="rf-status failed"><span class="dot"></span>{{ $t('instance.失败_acd5cb84_1') }}</span>
            <span v-else-if="row.status === 'terminated'" class="rf-status offline"><span class="dot"></span>{{ $t('instance.已终止_2554120a_1') }}</span>
            <span v-else class="rf-status">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('instance.实例_1782d6af')" width="240">
          <template #default="{ row }">
            <span class="rf-code">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('instance.流程编码_45668968_1')" width="180">
          <template #default="{ row }">
            <span class="rf-code">{{ row.flowCode }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('instance.版本_fe2df04a')" width="80" align="center">
          <template #default="{ row }">
            <span class="rf-mono" style="font-size: 12px; color: var(--rf-text-muted)">v{{ row.version }}</span>
          </template>
        </el-table-column>
        <el-table-column width="300px" :label="$t('instance.业务主键_21cb4583_1')">
          <template #default="{ row }">
            <span class="rf-mono">{{ row.businessKey }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="currentNodeId" :label="$t('instance.当前节点_f9bad394')" min-width="220" show-overflow-tooltip />
        
        <el-table-column :label="$t('instance.启动时间_86cd8dce')" width="185">
          <template #default="{ row }">
            <span class="rf-time">{{ formatTime(row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('instance.操作_2b6bc0f2')" width="180" fixed="right">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" :title="$t('instance.详情_f26225bd')" @click="handleDetail(row)">
                <el-icon><View /></el-icon>
              </button>
              <!-- 运行中：执行、挂起、终止 -->
              <button class="action-btn success" :title="$t('instance.执行_1a6aa24e')" @click="handleExecute(row)" v-if="row.status === 'running'">
                <el-icon><VideoPlay /></el-icon>
              </button>
              <button class="action-btn warning" :title="$t('instance.挂起_65d1ff59')" @click="handleSuspend(row)" v-if="row.status === 'running'">
                <el-icon><VideoPause /></el-icon>
              </button>
              <button class="action-btn danger" :title="$t('instance.终止_ff6c6ad7')" @click="handleTerminate(row)" v-if="row.status === 'running'">
                <el-icon><CircleClose /></el-icon>
              </button>
              <!-- 已挂起：继续、终止 -->
              <button class="action-btn success" :title="$t('instance.继续_27ca568b')" @click="handleResume(row)" v-if="row.status === 'suspended'">
                <el-icon><RefreshRight /></el-icon>
              </button>
              <button class="action-btn danger" :title="$t('instance.终止_ff6c6ad7_1')" @click="handleTerminate(row)" v-if="row.status === 'suspended'">
                <el-icon><CircleClose /></el-icon>
              </button>
              <!-- 失败：重试、终止 -->
              <button class="action-btn success" :title="$t('instance.重试_132c5cdc')" @click="handleRetry(row)" v-if="row.status === 'failed'">
                <el-icon><Refresh /></el-icon>
              </button>
              <button class="action-btn danger" :title="$t('instance.终止_ff6c6ad7_1')" @click="handleTerminate(row)" v-if="row.status === 'failed'">
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
    <el-dialog v-model="startDialogVisible" :title="$t('instance.手动启动流程_d19581a2')" width="520px" destroy-on-close :close-on-click-modal="false">
      <el-form ref="startFormRef" :model="startForm" :rules="startFormRules" label-width="100px">
        <el-form-item :label="$t('instance.流程定义_300d6075')" prop="flowId">
          <el-select v-model="startForm.flowId" :placeholder="$t('instance.请选择要启动_1a7c8e52')" clearable style="width: 100%">
            <el-option
              v-for="flow in flowDefinitionOptions"
              :key="flow.id"
              :label="`${flow.flowName} (v${flow.version})`"
              :value="flow.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('instance.业务主键_21cb4583_1')" prop="businessKey">
          <el-input v-model="startForm.businessKey" :placeholder="$t('instance.如办件流水号_e28825ca')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">{{ $t('instance.取消_625fb26b') }}</el-button>
        <el-button type="primary" @click="handleStartSubmit">{{ $t('instance.启动_8e54ddfe') }}</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="$t('instance.流程实例详情_79caf999')" width="900px" class="edit-dialog" destroy-on-close>
      <div v-if="currentInstance" class="instance-detail">
        <el-row :gutter="16" class="info-row">
          <el-col :span="8"><span class="label">实例ID:</span> {{ currentInstance.id }}</el-col>
          <el-col :span="8"><span class="label">流程编码:</span> {{ currentInstance.flowCode }}</el-col>
          <el-col :span="8"><span class="label">版本:</span> v{{ currentInstance.version }}</el-col>
        </el-row>
        <el-row :gutter="16" class="info-row">
          <el-col :span="8"><span class="label">业务主键:</span> {{ currentInstance.businessKey }}</el-col>
          <el-col :span="8">
            <span class="label">状态:</span>
            <el-tag :type="statusType(currentInstance.status)">{{ statusText(currentInstance.status) }}</el-tag>
          </el-col>
          <el-col :span="8"><span class="label">当前节点:</span> {{ currentInstance.currentNodeId }}</el-col>
        </el-row>
        <el-row :gutter="16" class="info-row">
          <el-col :span="8"><span class="label">启动时间:</span> {{ currentInstance.startTime }}</el-col>
        </el-row>

        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane :label="$t('instance.执行日志_c84ddfe8')" name="logs">
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
            
            <div v-if="logHasMore" class="load-more-container">
              <el-button 
                type="primary" 
                link 
                :loading="logLoading"
                @click="handleLoadMoreLogs"
              >
                {{ logLoading ? '加载中...' : '加载更多' }}
              </el-button>
            </div>
            
            <div v-if="!logHasMore && instanceLogs.length > 0" class="no-more-container">
              <span class="no-more-text">{{ $t('instance.没有更多日志_e250a969') }}</span>
            </div>
            
            <el-empty v-if="instanceLogs.length === 0 && !logLoading" :description="$t('instance.暂无执行日志_392189d5')" />
          </el-tab-pane>
          <el-tab-pane :label="$t('instance.任务列表_ca27b7bc')" name="tasks">
            <el-table :data="instanceTasks" stripe size="small">
              <el-table-column prop="nodeName" :label="$t('instance.节点_3bf3c0a8')" />
              <el-table-column prop="nodeType" label="节点类型" width="110" />
              <el-table-column prop="status" :label="$t('instance.状态_3fea7ca7_1')" width="100">
                <template #default="{ row }">
                  <el-tag :type="taskStatusType(row.status)" size="small">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="loopNodeId" label="循环节点" width="140" show-overflow-tooltip />
              <el-table-column prop="iterationIndex" label="迭代" width="80" align="center">
                <template #default="{ row }">
                  <span>{{ row.iterationIndex != null ? row.iterationIndex : '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="isLoopInternal" label="循环内" width="80" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.isLoopInternal === 1" type="info" size="small">是</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="executeCount" :label="$t('instance.执行次数_d4aea8d7')" width="90" />
              <el-table-column prop="errorMsg" :label="$t('instance.错误信息_4604d502')" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="循环概览" name="loops">
            <LoopProgressPanel :instance-id="currentInstance?.id" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, VideoPlay, CircleClose, VideoPause, RefreshRight, Refresh } from '@element-plus/icons-vue'
import LoopProgressPanel from './components/LoopProgressPanel.vue'
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
  getLoopProgress,
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

const logLoading = ref(false)
const logHasMore = ref(true)
const logPage = ref(1)
const logPageSize = 5

const startForm = reactive({
  flowId: null,
  businessKey: ''
})
const startFormRef = ref(null)
const startFormRules = {
  flowId: [{ required: true, message: t('instance.请选择流程定_e3f35c6d'), trigger: 'change' }],
  businessKey: [{ required: true, message: t('instance.请输入业务主_42cfcf0b'), trigger: 'blur' }]
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
    console.error(t('instance.加载失败_866b795e'), e)
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
  
  logPage.value = 1
  logHasMore.value = true
  instanceLogs.value = []
  
  try {
    const [logsRes, tasksRes] = await Promise.all([
      getInstanceLogs(row.id, logPage.value, logPageSize),
      getInstanceTasks(row.id)
    ])
    
    if (logsRes && logsRes.records) {
      instanceLogs.value = logsRes.records
      logHasMore.value = logsRes.records.length < (logsRes.total || 0)
    } else {
      instanceLogs.value = []
      logHasMore.value = false
    }
    
    instanceTasks.value = tasksRes || []
  } catch (e) {
    console.error(t('instance.加载详情失败_69622815'), e)
  }
}

async function handleLoadMoreLogs() {
  if (logLoading.value || !logHasMore.value) return
  
  logLoading.value = true
  logPage.value++
  
  try {
    const logsRes = await getInstanceLogs(currentInstance.value.id, logPage.value, logPageSize)
    
    if (logsRes && logsRes.records && logsRes.records.length > 0) {
      instanceLogs.value = [...instanceLogs.value, ...logsRes.records]
      logHasMore.value = instanceLogs.value.length < (logsRes.total || 0)
    } else {
      logHasMore.value = false
    }
  } catch (e) {
    console.error(t('instance.加载更多日志_40e25990'), e)
    logPage.value--
  } finally {
    logLoading.value = false
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
    ElMessage.success(t('instance.流程实例启动_1ef978cb'))
    startDialogVisible.value = false
    handleSearch()
  } catch (e) {
    ElMessage.error(t('instance.启动失败_424bd33a') + (e.message || t('instance.未知错误_974e7484')))
  }
}

async function handleExecute(row) {
  try {
    await executeFlowInstance(row.id)
    ElMessage.success(t('instance.执行成功_f56c1d01'))
    handleSearch()
  } catch (e) {
    ElMessage.error(t('instance.执行失败_23cc6892') + e.message)
  }
}

async function handleTerminate(row) {
  try {
    await ElMessageBox.confirm(`确认终止实例「${row.id}」？`, t('instance.终止确认_5516ed2f'), { type: 'warning' })
    await terminateFlowInstance(row.id)
    ElMessage.success(t('instance.实例已终止_8d5ae6ef'))
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(t('instance.终止失败_40e4a83f') + e.message)
  }
}

async function handleSuspend(row) {
  try {
    await ElMessageBox.confirm(`确认挂起实例「${row.id}」？`, t('instance.挂起确认_e025812f'), { type: 'warning' })
    await suspendFlowInstance(row.id)
    ElMessage.success(t('instance.实例已挂起_c4708653'))
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(t('instance.挂起失败_b645ac59') + e.message)
  }
}

async function handleResume(row) {
  try {
    await ElMessageBox.confirm(`确认继续执行实例「${row.id}」？`, t('instance.继续确认_be045437'), { type: 'info' })
    await resumeFlowInstance(row.id)
    ElMessage.success(t('instance.实例已继续执_9042aaf4'))
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(t('instance.继续失败_f65fe785') + e.message)
  }
}

async function handleRetry(row) {
  try {
    await ElMessageBox.confirm(`确认重试实例「${row.id}」？`, t('instance.重试确认_fc813825'), { type: 'info' })
    await retryFlowInstance(row.id)
    ElMessage.success(t('instance.实例已重试_2bb577da'))
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(t('instance.重试失败_a1a820e0') + e.message)
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
  const map = { running: t('instance.运行中_d679aea3'), completed: t('instance.已完成_fad5222c'), waiting: t('instance.等待中_65dd9ef1'), failed: t('instance.失败_acd5cb84'), suspended: t('instance.已挂起_8f2b3e77'), terminated: t('instance.已终止_2554120a') }
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

  .load-more-container {
    text-align: center;
    padding: 16px 0;
    margin-top: 8px;
  }

  .no-more-container {
    text-align: center;
    padding: 12px 0;
    margin-top: 8px;
    
    .no-more-text {
      font-size: 13px;
      color: #8C8C8C;
    }
  }
}
</style>
