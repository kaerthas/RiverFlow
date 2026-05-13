<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><Monitor /></el-icon>
      流程实例监控
    </div>

    <div class="rf-card">
      <el-form :model="queryForm" inline class="search-form">
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe size="small" v-loading="loading">
        <el-table-column prop="id" label="实例ID" width="180" />
        <el-table-column prop="flowCode" label="流程编码" width="140" />
        <el-table-column prop="businessKey" label="业务主键" width="160" />
        <el-table-column prop="currentNodeId" label="当前节点" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="启动时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="handleExecute(row)" v-if="row.status === 'running'">执行</el-button>
            <el-button v-if="row.status === 'running'" link type="warning" size="small" @click="handleTerminate(row)">终止</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="流程实例详情" width="900px" destroy-on-close>
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
              <el-table-column prop="status" label="状态" width="100">
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
import {
  getFlowInstanceList,
  getFlowInstanceDetail,
  executeFlowInstance,
  terminateFlowInstance,
  getInstanceTasks,
  getInstanceLogs
} from '@/api/workflow'

const loading = ref(false)
const detailVisible = ref(false)
const currentInstance = ref(null)
const activeTab = ref('logs')
const instanceLogs = ref([])
const instanceTasks = ref([])

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
      pagination.total = res.total
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
.search-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }

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
