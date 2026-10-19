<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><Monitor /></el-icon>
      运行监控
    </div>

    <div class="rf-card" style="margin-bottom: 16px;">
      <div class="instance-header">
        <h4 class="card-title" style="margin: 0;">应用实例监控</h4>
        <div class="instance-header-right">
          <el-select
            v-model="selectedInstance"
            size="small"
            placeholder="选择实例"
            style="width: 200px;"
            @change="loadCpuTrend"
          >
            <el-option
              v-for="inst in instances"
              :key="inst.instanceId"
              :label="inst.instanceId"
              :value="inst.instanceId"
            />
          </el-select>
          <span class="instance-count">共 {{ instanceTotal }} 个实例</span>
        </div>
      </div>
      <el-table :data="instances" size="small" style="margin-top: 12px;">
        <el-table-column prop="instanceId" label="实例地址" min-width="130" />
        <el-table-column label="进程CPU" min-width="130">
          <template #default="{ row }">
            <el-progress
              v-if="cpuPercent(row.processCpu) !== null"
              :percentage="cpuPercent(row.processCpu)"
              :color="progressColor(cpuPercent(row.processCpu))"
              :stroke-width="8"
            />
            <span v-else class="metric-empty">--</span>
          </template>
        </el-table-column>
        <el-table-column label="系统CPU" min-width="130">
          <template #default="{ row }">
            <el-progress
              v-if="cpuPercent(row.systemCpu) !== null"
              :percentage="cpuPercent(row.systemCpu)"
              :color="progressColor(cpuPercent(row.systemCpu))"
              :stroke-width="8"
            />
            <span v-else class="metric-empty">--</span>
          </template>
        </el-table-column>
        <el-table-column label="JVM堆内存" min-width="170">
          <template #default="{ row }">
            <el-progress
              :percentage="ratioPercent(row.jvmUsed, row.jvmMax)"
              :color="progressColor(ratioPercent(row.jvmUsed, row.jvmMax))"
              :stroke-width="8"
            />
            <span class="metric-detail">{{ formatMB(row.jvmUsed) }} / {{ formatMB(row.jvmMax) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="物理内存" min-width="170">
          <template #default="{ row }">
            <el-progress
              :percentage="ratioPercent(row.sysMemUsed, row.sysMemTotal)"
              :color="progressColor(ratioPercent(row.sysMemUsed, row.sysMemTotal))"
              :stroke-width="8"
            />
            <span class="metric-detail">{{ formatGB(row.sysMemUsed) }} / {{ formatGB(row.sysMemTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="启动时间" min-width="150">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="最后上报" min-width="150">
          <template #default="{ row }">{{ formatTime(row.reportTime) }}</template>
        </el-table-column>
        <template #empty><span class="metric-empty">暂无实例数据</span></template>
      </el-table>
      <el-row :gutter="16">
        <el-col :xs="24" :lg="12">
          <div class="chart-title">CPU 使用率（%）</div>
          <div ref="cpuChartRef" class="metric-chart"></div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-title">内存使用率（%）</div>
          <div ref="memChartRef" class="metric-chart"></div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="16" class="stats-row">
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
              <div class="num">{{ formatNum(apiStats.total) }}</div>
              <div class="label">总调用次数</div>
            </div>
            <div class="number-item">
              <div class="num success">{{ apiStats.successRate }}%</div>
              <div class="label">成功率</div>
            </div>
            <div class="number-item">
              <div class="num danger">{{ formatNum(apiStats.failed) }}</div>
              <div class="label">失败次数</div>
            </div>
          </div>
          <el-divider />
          <div class="api-rank">
            <div v-for="(api, idx) in topApis" :key="idx" class="rank-item">
              <span class="rank-index">{{ idx + 1 }}</span>
              <span class="rank-name">{{ api.name }}</span>
              <span class="rank-count">{{ formatNum(api.count) }}次</span>
            </div>
            <div v-if="topApis.length === 0" class="rank-empty">近24小时暂无调用</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="rf-card">
          <h4 class="card-title">最近异常</h4>
          <div class="recent-alerts">
            <el-timeline v-if="recentErrors.length > 0">
              <el-timeline-item
                v-for="alert in recentErrors"
                :key="alert.id"
                :type="alert.logType === 'error' ? 'danger' : 'warning'"
                :timestamp="formatTime(alert.createTime)"
              >
                <div class="alert-item">
                  <el-tag :type="alert.logType === 'error' ? 'danger' : 'warning'" size="small">{{ alert.logType }}</el-tag>
                  <p :title="formatLogContent(alert.logContent)">{{ formatLogContent(alert.logContent) }}</p>
                </div>
              </el-timeline-item>
            </el-timeline>
            <div v-else class="alert-empty">暂无异常</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="rf-card" style="margin-top: 16px;">
      <h4 class="card-title">实时日志</h4>
      <div class="log-console" ref="logConsoleRef">
        <div v-for="(log, idx) in logs" :key="idx" class="log-line">
          <span class="log-time">{{ formatTime(log.createTime) }}</span>
          <span class="log-level" :class="log.logType?.toUpperCase()">{{ log.logType?.toUpperCase() || 'INFO' }}</span>
          <span v-if="log.flowName || log.flowCode" class="log-flow">[{{ log.flowName || log.flowCode }}]</span>
          <span class="log-msg">{{ formatLogContent(log.logContent) }}</span>
        </div>
        <div v-if="logs.length === 0" class="empty-logs">暂无日志</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const stats = ref({ total: 0, running: 0, completed: 0, failed: 0 })
const apiStats = ref({ total: 0, failed: 0, successRate: 0 })
const topApis = ref([])
const recentErrors = ref([])
const logs = ref([])
const instances = ref([])
const instanceTotal = ref(0)
const selectedInstance = ref('')
const logConsoleRef = ref(null)
const cpuChartRef = ref(null)
const memChartRef = ref(null)
let cpuChart = null
let memChart = null
let timer = null

const runningPercent = computed(() => {
  const total = stats.value.total || 1
  return Math.round((stats.value.running / total) * 100)
})

const pendingTasksPercent = computed(() => {
  const total = stats.value.total || 1
  return Math.round((stats.value.pendingTasks || 0) / (total + (stats.value.pendingTasks || 0)) * 100)
})

function formatNum(n) {
  return Number(n || 0).toLocaleString('en-US')
}

// CPU 使用率（后端返回 0-1 小数的字符串，可能缺失），返回整数百分比或 null
function cpuPercent(v) {
  if (v === undefined || v === null || v === '') return null
  const n = parseFloat(v)
  if (isNaN(n) || n < 0) return null
  return Math.min(100, Math.round(n * 100))
}

// 已用/总量 的整数百分比
function ratioPercent(used, total) {
  const u = Number(used || 0)
  const t = Number(total || 0)
  if (t <= 0) return 0
  return Math.min(100, Math.round((u / t) * 100))
}

function progressColor(p) {
  if (p >= 90) return '#F5222D'
  if (p >= 60) return '#FAAD14'
  return '#1677FF'
}

function formatMB(bytes) {
  return (Number(bytes || 0) / 1048576).toFixed(0) + ' MB'
}

function formatGB(bytes) {
  return (Number(bytes || 0) / 1073741824).toFixed(1) + ' GB'
}

function formatTime(time) {
  if (!time) return '--'
  // 兼容 'yyyy-MM-dd HH:mm:ss' 与 ISO 'yyyy-MM-ddTHH:mm:ss'，完整显示年月日时分秒
  return String(time).replace('T', ' ').substring(0, 19)
}

// 历史日志中节点名被存成了 LogicFlow 的 text 对象 JSON，展示时提取 value
const NODE_TEXT_JSON_RE = /\{"x":\s*-?[\d.]+,\s*"y":\s*-?[\d.]+,\s*"value":"([^"]*)"\}/g

function formatLogContent(content) {
  if (!content) return ''
  return content.replace(NODE_TEXT_JSON_RE, '$1')
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
      recentErrors.value = res.filter(l => l.logType === 'error').slice(0, 3)
    }
  } catch (e) { /* ignore */ }
}

async function loadPendingTasks() {
  try {
    const res = await request({ url: '/monitor/pending-tasks', method: 'get' })
    if (res !== undefined) stats.value.pendingTasks = res
  } catch (e) { /* ignore */ }
}

async function loadApiCallStats() {
  try {
    const res = await request({ url: '/monitor/api-call-stats', method: 'get' })
    if (res) {
      apiStats.value = res
      topApis.value = res.topApis || []
    }
  } catch (e) { /* ignore */ }
}

async function loadInstances() {
  try {
    const res = await request({ url: '/monitor/instances', method: 'get' })
    if (res) {
      instances.value = res.instances || []
      instanceTotal.value = res.total || 0
      // 选中实例不在线（或尚未选择）时回退到第一个实例
      const ids = instances.value.map(i => i.instanceId)
      if (!ids.includes(selectedInstance.value)) {
        selectedInstance.value = ids[0] || ''
        loadCpuTrend()
      }
    }
  } catch (e) { /* ignore */ }
}

// CPU 采样点时间轴：epoch秒 -> HH:mm:ss
function formatChartTime(epochSec) {
  const d = new Date(epochSec * 1000)
  const pad = n => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function loadCpuTrend() {
  let trend = []
  if (selectedInstance.value) {
    try {
      const res = await request({
        url: '/monitor/cpu-trend',
        method: 'get',
        params: { instanceId: selectedInstance.value }
      })
      trend = Array.isArray(res) ? res : []
    } catch (e) { /* ignore */ }
  }
  await nextTick()
  renderMetricCharts(trend)
}

// 后端返回 0-1 小数，缺测为 null；转成保留 1 位小数的百分比
const toPercent = v => (v === null || v === undefined) ? null : Math.round(v * 1000) / 10

function makeLineSeries(trend, name, field, color, areaFrom) {
  return {
    name,
    type: 'line',
    smooth: true,
    showSymbol: false,
    connectNulls: true,
    data: trend.map(p => toPercent(p[field])),
    lineStyle: { width: 2, color },
    itemStyle: { color },
    areaStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: areaFrom },
        { offset: 1, color: 'rgba(255,255,255,0.02)' }
      ])
    }
  }
}

function buildTrendOption(trend, seriesDefs) {
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: { color: '#1f2937', fontSize: 12 },
      padding: [10, 14],
      extraCssText: 'box-shadow: 0 10px 25px -5px rgba(0,0,0,0.08); border-radius: 12px;',
      valueFormatter: v => (v === null || v === undefined) ? '--' : v + '%'
    },
    legend: {
      right: 8,
      top: 0,
      itemWidth: 12,
      itemHeight: 8,
      textStyle: { color: '#6b7280', fontSize: 12 }
    },
    grid: { left: '1%', right: '2%', bottom: '2%', top: 36, containLabel: true },
    xAxis: {
      type: 'category',
      data: trend.map(p => formatChartTime(p.t)),
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisTick: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      name: '%',
      nameTextStyle: { color: '#9ca3af', fontSize: 11 },
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } },
      axisLabel: { color: '#9ca3af', fontSize: 11 }
    },
    series: seriesDefs.map(d => makeLineSeries(trend, d.name, d.field, d.color, d.areaFrom))
  }
}

// init 时若容器尺寸为 0，canvas 会卡在 0x0，渲染前先 resize 自愈
function renderChart(chartRef, chart, option) {
  if (!chartRef.value) return null
  if (!chart) {
    chart = echarts.init(chartRef.value)
  } else {
    chart.resize()
  }
  chart.setOption(option)
  return chart
}

function renderMetricCharts(trend) {
  cpuChart = renderChart(cpuChartRef, cpuChart, buildTrendOption(trend, [
    { name: '进程CPU', field: 'p', color: '#1677FF', areaFrom: 'rgba(22,119,255,0.22)' },
    { name: '系统CPU', field: 's', color: '#FAAD14', areaFrom: 'rgba(250,173,20,0.20)' }
  ]))
  memChart = renderChart(memChartRef, memChart, buildTrendOption(trend, [
    { name: 'JVM内存', field: 'jm', color: '#52C41A', areaFrom: 'rgba(82,196,26,0.18)' },
    { name: '物理内存', field: 'sm', color: '#722ED1', areaFrom: 'rgba(114,46,209,0.18)' }
  ]))
}

function loadAll() {
  loadStats()
  loadRecentLogs()
  loadPendingTasks()
  loadApiCallStats()
  loadInstances()
  loadCpuTrend()
}

function handleResize() {
  cpuChart?.resize()
  memChart?.resize()
}

onMounted(() => {
  loadAll()
  timer = setInterval(loadAll, 5000)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  cpuChart?.dispose()
  memChart?.dispose()
  cpuChart = null
  memChart = null
})
</script>

<style scoped lang="scss">
.card-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #262626;
}

.instance-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .instance-header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .instance-count {
    font-size: 13px;
    color: #8C8C8C;
  }
}

.chart-title {
  margin-top: 16px;
  font-size: 13px;
  font-weight: 600;
  color: #595959;
}

.metric-chart {
  margin-top: 8px;
  height: 260px;
}

.metric-detail {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: #8C8C8C;
}

.metric-empty {
  font-size: 13px;
  color: #8C8C8C;
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

  .rank-empty {
    padding: 24px 0;
    text-align: center;
    font-size: 13px;
    color: #8C8C8C;
  }
}

.recent-alerts {
  .alert-empty {
    padding: 40px 0;
    text-align: center;
    font-size: 13px;
    color: #8C8C8C;
  }
}

// 三个统计卡片等高拉伸（以内容最高的卡片为准）
.stats-row {
  .el-col {
    display: flex;
    flex-direction: column;

    > .rf-card {
      flex: 1;
    }
  }
}

.alert-item {
  p {
    margin: 6px 0 0;
    font-size: 13px;
    color: #595959;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    word-break: break-all;
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
    .log-flow { color: #22D3EE; margin-right: 12px; }
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
