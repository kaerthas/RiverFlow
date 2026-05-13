<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><Monitor /></el-icon>
      运行监控
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="8">
        <div class="rf-card">
          <h4 class="card-title">实例统计</h4>
          <div class="stat-number">
            <div class="number-item">
              <div class="num">{{ stats.total }}</div>
              <div class="label">总实例</div>
            </div>
            <div class="number-item">
              <div class="num success">{{ stats.completed }}</div>
              <div class="label">已完成</div>
            </div>
            <div class="number-item">
              <div class="num danger">{{ stats.failed }}</div>
              <div class="label">失败</div>
            </div>
          </div>
          <el-divider />
          <div class="monitor-item">
            <span>运行中</span>
            <el-progress :percentage="runningPercent" :color="'#1677FF'" />
          </div>
          <div class="monitor-item">
            <span>待执行任务</span>
            <el-progress :percentage="pendingTasksPercent" :color="'#FAAD14'" />
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="rf-card">
          <h4 class="card-title">接口调用统计（近24小时）</h4>
          <div class="stat-number">
            <div class="number-item">
              <div class="num">12,456</div>
              <div class="label">总调用次数</div>
            </div>
            <div class="number-item">
              <div class="num success">98.5%</div>
              <div class="label">成功率</div>
            </div>
            <div class="number-item">
              <div class="num danger">186</div>
              <div class="label">失败次数</div>
            </div>
          </div>
          <el-divider />
          <div class="api-rank">
            <div v-for="(api, idx) in topApis" :key="idx" class="rank-item">
              <span class="rank-index">{{ idx + 1 }}</span>
              <span class="rank-name">{{ api.name }}</span>
              <span class="rank-count">{{ api.count }}次</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="rf-card">
          <h4 class="card-title">最近异常</h4>
          <el-timeline>
            <el-timeline-item
              v-for="alert in recentErrors"
              :key="alert.id"
              :type="alert.logType === 'error' ? 'danger' : 'warning'"
              :timestamp="alert.createTime"
            >
              <div class="alert-item">
                <el-tag :type="alert.logType === 'error' ? 'danger' : 'warning'" size="small">{{ alert.logType }}</el-tag>
                <p>{{ alert.logContent }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
    </el-row>

    <div class="rf-card" style="margin-top: 16px;">
      <h4 class="card-title">实时日志</h4>
      <div class="log-console" ref="logConsoleRef">
        <div v-for="(log, idx) in logs" :key="idx" class="log-line">
          <span class="log-time">{{ formatTime(log.createTime) }}</span>
          <span class="log-level" :class="log.logType?.toUpperCase()">{{ log.logType?.toUpperCase() || 'INFO' }}</span>
          <span class="log-msg">{{ log.logContent }}</span>
        </div>
        <div v-if="logs.length === 0" class="empty-logs">暂无日志</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import request from '@/utils/request'

const stats = ref({ total: 0, running: 0, completed: 0, failed: 0 })
const recentErrors = ref([])
const logs = ref([])
const logConsoleRef = ref(null)
let timer = null

const runningPercent = computed(() => {
  const total = stats.value.total || 1
  return Math.round((stats.value.running / total) * 100)
})

const pendingTasksPercent = computed(() => {
  const total = stats.value.total || 1
  return Math.round((stats.value.pendingTasks || 0) / (total + (stats.value.pendingTasks || 0)) * 100)
})

const topApis = ref([
  { name: '省里统一认证平台', count: 3456 },
  { name: '协同调度中心', count: 2890 },
  { name: '中残申请接口', count: 1567 },
  { name: '查询办件列表', count: 1234 },
  { name: '材料上传接口', count: 890 }
])

function formatTime(time) {
  if (!time) return '--'
  return time.substring(11, 19)
}

async function loadStats() {
  try {
    const res = await request({ url: '/monitor/stats', method: 'get' })
    if (res) stats.value = res
  } catch (e) { /* ignore */ }
}

async function loadRecentLogs() {
  try {
    const res = await request({ url: '/monitor/recent-logs?limit=50', method: 'get' })
    if (res) {
      logs.value = res
      recentErrors.value = res.filter(l => l.logType === 'error').slice(0, 10)
      nextTick(() => {
        if (logConsoleRef.value) {
          logConsoleRef.value.scrollTop = logConsoleRef.value.scrollHeight
        }
      })
    }
  } catch (e) { /* ignore */ }
}

async function loadPendingTasks() {
  try {
    const res = await request({ url: '/monitor/pending-tasks', method: 'get' })
    if (res !== undefined) stats.value.pendingTasks = res
  } catch (e) { /* ignore */ }
}

function loadAll() {
  loadStats()
  loadRecentLogs()
  loadPendingTasks()
}

onMounted(() => {
  loadAll()
  timer = setInterval(loadAll, 5000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
.card-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #262626;
}

.monitor-item {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;

  span {
    font-size: 13px;
    color: #595959;
  }
}

.stat-number {
  display: flex;
  justify-content: space-around;
  text-align: center;
  margin-bottom: 16px;

  .number-item {
    .num {
      font-size: 28px;
      font-weight: 600;
      color: #262626;

      &.success { color: #52C41A; }
      &.danger { color: #F5222D; }
    }
    .label {
      font-size: 13px;
      color: #8C8C8C;
      margin-top: 4px;
    }
  }
}

.api-rank {
  .rank-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid #F5F5F5;

    .rank-index {
      width: 24px;
      height: 24px;
      line-height: 24px;
      text-align: center;
      border-radius: 50%;
      background: #F0F2F5;
      font-size: 12px;
      color: #595959;
      margin-right: 12px;
    }

    .rank-name {
      flex: 1;
      font-size: 13px;
      color: #262626;
    }

    .rank-count {
      font-size: 13px;
      color: #8C8C8C;
    }
  }
}

.alert-item {
  p {
    margin: 6px 0 0;
    font-size: 13px;
    color: #595959;
    line-height: 1.5;
  }
}

.log-console {
  background: #1E1E1E;
  border-radius: 6px;
  padding: 12px 16px;
  max-height: 300px;
  overflow-y: auto;
  font-family: 'Consolas', 'Monaco', monospace;

  .log-line {
    line-height: 1.8;
    font-size: 13px;

    .log-time { color: #6B7280; margin-right: 12px; }
    .log-level {
      display: inline-block;
      width: 48px;
      text-align: center;
      margin-right: 12px;
      border-radius: 3px;
      font-size: 11px;
      font-weight: 600;

      &.INFO { color: #3B82F6; }
      &.WARN { color: #F59E0B; }
      &.ERROR { color: #EF4444; }
    }
    .log-msg { color: #E5E7EB; }
  }

  .empty-logs {
    text-align: center;
    color: #6B7280;
    padding: 40px 0;
  }
}
</style>
