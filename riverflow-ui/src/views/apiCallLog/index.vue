<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">接口调用日志</h1>
        <p class="subtitle">查看开放接口的调用记录、入参与出参</p>
      </div>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="接口编码">
            <el-input v-model="queryForm.apiCode" placeholder="请输入接口编码" clearable />
          </el-form-item>
          <el-form-item label="调用状态">
            <el-select v-model="queryForm.callStatus" placeholder="全部状态" clearable style="width: 120px">
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="调用时间">
            <el-date-picker
              v-model="timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 360px"
            />
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
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.callStatus === 1" class="rf-status success"><span class="dot"></span>成功</span>
            <span v-else class="rf-status failed"><span class="dot"></span>失败</span>
          </template>
        </el-table-column>
        <el-table-column label="接口编码" width="180">
          <template #default="{ row }">
            <span class="rf-code">{{ row.apiCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="请求方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="methodTagType(row.requestMethod)" effect="plain">{{ row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求地址" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态码" width="90" align="center">
          <template #default="{ row }">
            <span class="rf-mono">{{ row.statusCode ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100" align="center">
          <template #default="{ row }">
            <span class="rf-mono">{{ row.costTime != null ? row.costTime + ' ms' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="错误信息" min-width="180">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.errorMsg"
              :content="row.errorMsg"
              placement="top"
              :show-after="200"
              popper-class="error-msg-tooltip"
            >
              <div class="error-msg-cell">{{ row.errorMsg }}</div>
            </el-tooltip>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="调用时间" width="185">
          <template #default="{ row }">
            <span class="rf-time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" title="详情" @click="handleDetail(row)">
                <el-icon><View /></el-icon>
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
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @update:page-size="handleSearch"
          @update:current-page="handleSearch"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="调用日志详情" width="860px" class="edit-dialog" destroy-on-close>
      <div v-if="currentLog" class="log-detail">
        <el-row :gutter="16" class="info-row">
          <el-col :span="8"><span class="label">接口编码:</span> {{ currentLog.apiCode }}</el-col>
          <el-col :span="8"><span class="label">请求方式:</span> {{ currentLog.requestMethod }}</el-col>
          <el-col :span="8">
            <span class="label">调用状态:</span>
            <el-tag :type="currentLog.callStatus === 1 ? 'success' : 'danger'" size="small">
              {{ currentLog.callStatus === 1 ? '成功' : '失败' }}
            </el-tag>
          </el-col>
        </el-row>
        <el-row :gutter="16" class="info-row">
          <el-col :span="24"><span class="label">请求地址:</span> {{ currentLog.requestUrl }}</el-col>
        </el-row>
        <el-row :gutter="16" class="info-row">
          <el-col :span="8"><span class="label">状态码:</span> {{ currentLog.statusCode ?? '-' }}</el-col>
          <el-col :span="8"><span class="label">耗时:</span> {{ currentLog.costTime != null ? currentLog.costTime + ' ms' : '-' }}</el-col>
          <el-col :span="8"><span class="label">调用时间:</span> {{ formatTime(currentLog.createTime) }}</el-col>
        </el-row>
        <el-row :gutter="16" class="info-row" v-if="currentLog.errorMsg">
          <el-col :span="24">
            <span class="label">错误信息:</span>
            <span style="color: var(--el-color-danger)">{{ currentLog.errorMsg }}</span>
          </el-col>
        </el-row>

        <el-tabs class="detail-tabs">
          <el-tab-pane label="入参">
            <pre class="json-view">{{ prettyJson(currentLog.requestBody) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="出参">
            <pre class="json-view">{{ prettyJson(currentLog.responseBody) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="请求头">
            <pre class="json-view">{{ prettyJson(currentLog.requestHeaders) }}</pre>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Delete } from '@element-plus/icons-vue'
import { getApiCallLogList, getApiCallLogDetail, deleteApiCallLog } from '@/api/apiCallLog'

const loading = ref(false)
const detailVisible = ref(false)
const currentLog = ref(null)
const timeRange = ref([])

const queryForm = reactive({
  apiCode: '',
  callStatus: null
})

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

async function handleSearch() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      apiCode: queryForm.apiCode,
      callStatus: queryForm.callStatus
    }
    if (timeRange.value && timeRange.value.length === 2) {
      params.startTime = timeRange.value[0]
      params.endTime = timeRange.value[1]
    }
    const res = await getApiCallLogList(params)
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
  queryForm.apiCode = ''
  queryForm.callStatus = null
  timeRange.value = []
  handleSearch()
}

async function handleDetail(row) {
  detailVisible.value = true
  currentLog.value = row
  try {
    const res = await getApiCallLogDetail(row.id)
    if (res) currentLog.value = res
  } catch (e) {
    console.error('加载详情失败', e)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条调用日志？', '删除确认', { type: 'warning' })
    await deleteApiCallLog(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败: ' + e.message)
  }
}

function prettyJson(str) {
  if (!str) return '（空）'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch (e) {
    return str
  }
}

function methodTagType(method) {
  const map = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return map[method] || 'info'
}

function formatTime(time) {
  return time ? time.replace('T', ' ').substring(0, 19) : '-'
}

handleSearch()
</script>

<style scoped lang="scss">
// 错误信息单元格截断（公共样式强制 .cell overflow:visible，需在内部 div 上截断）
.error-msg-cell {
  color: var(--el-color-danger);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-detail {
  .info-row {
    margin-bottom: 12px;
    .label { color: #8C8C8C; margin-right: 8px; }
  }

  .detail-tabs {
    margin-top: 20px;
  }

  .json-view {
    margin: 0;
    padding: 12px;
    max-height: 360px;
    overflow: auto;
    background: #f6f8fa;
    border-radius: 6px;
    font-size: 12px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>

<style lang="scss">
// 错误信息悬停 tooltip（popper 挂载在 body，不能用 scoped）
.error-msg-tooltip {
  max-width: 480px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
