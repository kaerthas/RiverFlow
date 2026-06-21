<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><Monitor /></el-icon>{{ $t('monitor.运行监控_8bf81f31') }}</div>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="8">
        <div class="rf-card">
          <h4 class="card-title">{{ $t('monitor.实例统计_0417f7db') }}</h4>
          <div class="stat-number">
            <div class="number-item">
              <div class="num">{{ stats.total }}</div>
              <div class="label">{{ $t('monitor.总实例_6ca5089b') }}</div>
            </div>
            <div class="number-item">
              <div class="num success">{{ stats.completed }}</div>
              <div class="label">{{ $t('monitor.已完成_fad5222c') }}</div>
            </div>
            <div class="number-item">
              <div class="num danger">{{ stats.failed }}</div>
              <div class="label">{{ $t('monitor.失败_acd5cb84') }}</div>
            </div>
          </div>
          <el-divider />
          <div class="monitor-item">
            <span>{{ $t('monitor.运行中_d679aea3') }}</span>
            <el-progress :percentage="runningPercent" :color="'#1677FF'" />
          </div>
          <div class="monitor-item">
            <span>{{ $t('monitor.待执行任务_1d3ec277') }}</span>
            <el-progress :percentage="pendingTasksPercent" :color="'#FAAD14'" />
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="rf-card">
          <h4 class="card-title">{{ $t('monitor.接口调用统计近24小时_dafasdqw') }}</h4>
          <div class="stat-number">
            <div class="number-item">
              <div class="num">12,456</div>
              <div class="label">{{ $t('monitor.总调用次数_7f6f35c4') }}</div>
            </div>
            <div class="number-item">
              <div class="num success">98.5%</div>
              <div class="label">{{ $t('monitor.成功率_b664352f') }}</div>
            </div>
            <div class="number-item">
              <div class="num danger">186</div>
              <div class="label">{{ $t('monitor.失败次数_d3e480c8') }}</div>
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
          <h4 class="card-title">{{ $t('monitor.最近异常_bf45b0c5') }}</h4>
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
      <h4 class="card-title">{{ $t('monitor.实时日志_a94180c2') }}</h4>
      <div class="log-console" ref="logConsoleRef">
        <div v-for="(log, idx) in logs" :key="idx" class="log-line">
          <span class="log-time">{{ formatTime(log.createTime) }}</span>
          <span class="log-level" :class="log.logType?.toUpperCase()">{{ log.logType?.toUpperCase() || 'INFO' }}</span>
          <span class="log-msg">{{ log.logContent }}</span>
        </div>
        <div v-if="logs.length === 0" class="empty-logs">{{ $t('monitor.暂无日志_8b9c17a1') }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
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
  { name: t('monitor.省里统一认证_d41157ee'), count: 3456 },
  { name: t('monitor.协同调度中心_5b915bec'), count: 2890 },
  { name: t('monitor.中残申请接口_f870da4c'), count: 1567 },
  { name: t('monitor.查询办件列表_a651dddd'), count: 1234 },
  { name: t('monitor.材料上传接口_7caf26c0'), count: 890 }
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
