<template>
  <div class="dashboard-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">数据大盘</h1>
        <p class="page-subtitle">接口、应用与调用量实时总览</p>
      </div>
      <div class="header-actions">
        <div class="live-indicator">
          <span class="pulse-dot"></span>
          <span class="live-text">实时更新</span>
        </div>
      </div>
    </div>

    <!-- 接口服务统计 -->
    <h3 class="section-title">接口服务</h3>
    <div class="stats-grid">
      <div class="bento-card">
        <div class="card-content">
          <div class="card-meta">
            <div class="meta-icon blue">
              <el-icon :size="20"><Connection /></el-icon>
            </div>
            <span class="meta-label">注册接口数</span>
          </div>
          <div class="card-value rf-mono">{{ apiStats.apiCount }}</div>
        </div>
      </div>

      <div class="bento-card">
        <div class="card-content">
          <div class="card-meta">
            <div class="meta-icon green">
              <el-icon :size="20"><Grid /></el-icon>
            </div>
            <span class="meta-label">接入应用数</span>
          </div>
          <div class="card-value rf-mono">{{ apiStats.appCount }}</div>
        </div>
      </div>

      <div class="bento-card">
        <div class="card-content">
          <div class="card-meta">
            <div class="meta-icon blue">
              <el-icon :size="20"><Share /></el-icon>
            </div>
            <span class="meta-label">流程总数</span>
          </div>
          <div class="card-value rf-mono">
            {{ apiStats.flowCount }}
            <span class="flow-sub">同步 {{ apiStats.syncFlowCount }} · 异步 {{ apiStats.asyncFlowCount }}</span>
          </div>
        </div>
      </div>

      <div class="bento-card">
        <div class="card-content">
          <div class="card-meta">
            <div class="meta-icon purple">
              <el-icon :size="20"><DataAnalysis /></el-icon>
            </div>
            <span class="meta-label">接口调用总量</span>
          </div>
          <div class="card-value rf-mono">{{ apiStats.callTotal }}</div>
        </div>
      </div>

      <div class="bento-card">
        <div class="card-content">
          <div class="card-meta">
            <div class="meta-icon orange">
              <el-icon :size="20"><TrendCharts /></el-icon>
            </div>
            <span class="meta-label">今日调用量</span>
          </div>
          <div class="card-value rf-mono">{{ apiStats.callToday }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card chart-card--wide">
        <div class="chart-header">
          <div>
            <h3 class="chart-title">接口调用趋势</h3>
            <p class="chart-desc">近7天接口调用量</p>
          </div>
          <div class="chart-legend">
            <span class="legend-item"><span class="dot blue"></span>调用量</span>
          </div>
        </div>
        <div ref="barChartRef" class="chart-body"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <div>
            <h3 class="chart-title">调用结果占比</h3>
            <p class="chart-desc">接口调用成功/失败分布</p>
          </div>
        </div>
        <div ref="pieChartRef" class="chart-body"></div>
      </div>
    </div>

    <!-- 最新接口调用记录 -->
    <div class="log-card">
      <div class="log-header">
        <div>
          <h3 class="log-title">最新接口调用记录</h3>
          <p class="log-desc">系统最近接收的接口调用请求</p>
        </div>
        <el-button type="primary" text size="small" class="view-all-btn" @click="$router.push('/api-call-log')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-table :data="recentCalls" size="default" v-loading="callLoading" class="rf-table" empty-text="暂无数据">
        <el-table-column prop="apiCode" label="接口编码" width="200">
          <template #default="{ row }">
            <span class="id-badge">{{ row.apiCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="方式" width="90" align="center">
          <template #default="{ row }">
            <span :class="['method-tag', (row.requestMethod || '').toLowerCase()]">
              {{ row.requestMethod || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求地址" min-width="240" show-overflow-tooltip />
        <el-table-column prop="statusCode" label="状态码" width="90" align="center">
          <template #default="{ row }">
            <span class="rf-mono">{{ row.statusCode || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时" width="100" align="center">
          <template #default="{ row }">
            <span class="rf-mono">{{ row.costTime != null ? row.costTime + ' ms' : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="callStatus" label="结果" width="90" align="center">
          <template #default="{ row }">
            <span :class="['type-tag', row.callStatus === 1 ? 'success' : 'error']">
              {{ row.callStatus === 1 ? '成功' : '失败' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getCallTrend, getRecentCalls } from '@/api/monitor'

const apiStats = reactive({
  apiCount: 0,
  appCount: 0,
  callTotal: 0,
  callToday: 0,
  callFailed: 0,
  flowCount: 0,
  syncFlowCount: 0,
  asyncFlowCount: 0
})
const recentCalls = ref([])
const callLoading = ref(false)

const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null

const formatTime = (time) => time ? String(time).replace('T', ' ').substring(0, 19) : '-'

function initBarChart(trend) {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: { color: '#1f2937', fontSize: 12 },
      padding: [10, 14],
      extraCssText: 'box-shadow: 0 10px 25px -5px rgba(0,0,0,0.08); border-radius: 12px;'
    },
    grid: { left: '2%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: trend.map(t => t.date),
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisTick: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } },
      axisLabel: { color: '#9ca3af', fontSize: 11 }
    },
    series: [{
      name: '调用量',
      data: trend.map(t => t.count),
      type: 'bar',
      barWidth: '36%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#3b82f6' },
          { offset: 1, color: '#60a5fa' }
        ]),
        borderRadius: [6, 6, 0, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2563eb' },
            { offset: 1, color: '#3b82f6' }
          ])
        }
      }
    }]
  }
  barChart.setOption(option)
}

function initPieChart() {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: { color: '#1f2937', fontSize: 12 },
      padding: [10, 14],
      extraCssText: 'box-shadow: 0 10px 25px -5px rgba(0,0,0,0.08); border-radius: 12px;'
    },
    legend: {
      bottom: '0%',
      left: 'center',
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 16,
      textStyle: { color: '#6b7280', fontSize: 11 }
    },
    series: [{
      name: '调用结果',
      type: 'pie',
      radius: ['44%', '72%'],
      center: ['50%', '46%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: { show: false, position: 'center' },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 700,
          color: '#1f2937',
          formatter: '{b}\n{d}%'
        },
        scale: true,
        scaleSize: 8
      },
      data: [
        { value: Math.max(apiStats.callTotal - apiStats.callFailed, 0), name: '成功', itemStyle: { color: '#10b981' } },
        { value: apiStats.callFailed, name: '失败', itemStyle: { color: '#ef4444' } }
      ].filter(d => d.value > 0)
    }]
  }
  pieChart.setOption(option)
}

async function loadOverview() {
  try {
    const res = await getOverview()
    Object.assign(apiStats, res)
  } catch (e) {
    // 静默忽略，保持默认值
  }
  await nextTick()
  initPieChart()
}

async function loadTrend() {
  let trend = []
  try {
    const res = await getCallTrend()
    trend = Array.isArray(res) ? res : []
  } catch (e) {
    // 静默忽略，展示空图表
  }
  await nextTick()
  initBarChart(trend)
}

async function loadRecentCalls() {
  callLoading.value = true
  try {
    const res = await getRecentCalls(10)
    recentCalls.value = Array.isArray(res) ? res : []
  } finally {
    callLoading.value = false
  }
}

function handleResize() {
  barChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  loadOverview()
  loadTrend()
  loadRecentCalls()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-page {
  padding: 28px 32px;
  min-height: calc(100dvh - var(--rf-header-height));
  background: var(--rf-bg-page);
}

// -------- 页面头部 --------
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;

  .page-title {
    font-size: 26px;
    font-weight: 700;
    color: var(--rf-text-main);
    margin: 0 0 6px;
    letter-spacing: -0.03em;
    line-height: 1.2;
  }

  .page-subtitle {
    font-size: 14px;
    color: var(--rf-text-muted);
    margin: 0;
    font-weight: 400;
  }

  .header-actions {
    .live-indicator {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 14px;
      background: var(--rf-bg-card);
      border-radius: var(--radius-full);
      border: 1px solid var(--rf-border-light);
      box-shadow: var(--shadow-sm);

      .pulse-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #10b981;
        position: relative;

        &::after {
          content: '';
          position: absolute;
          inset: -4px;
          border-radius: 50%;
          background: #10b981;
          animation: pulse-ring 2s cubic-bezier(0.215, 0.61, 0.355, 1) infinite;
          opacity: 0.4;
        }
      }

      .live-text {
        font-size: 12px;
        font-weight: 600;
        color: var(--rf-text-secondary);
      }
    }
  }
}

@keyframes pulse-ring {
  0% { transform: scale(0.6); opacity: 0.5; }
  100% { transform: scale(2.2); opacity: 0; }
}

// -------- 分区标题 --------
.section-title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--rf-text-secondary);
  letter-spacing: -0.01em;
}

// -------- 统计卡片网格 --------
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

// 流程卡片内联小字：同步/异步分布
.flow-sub {
  margin-left: 8px;
  font-size: 12px;
  font-weight: 500;
  color: var(--rf-text-muted);
  letter-spacing: 0;
  white-space: nowrap;
}

.bento-card {
  position: relative;
  background: var(--rf-bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--rf-border-light);
  overflow: hidden;
  transition: transform var(--duration-base) var(--ease-spring),
    box-shadow var(--duration-base) var(--ease-out-quart);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }

  &:active {
    transform: scale(0.99);
  }

  .card-content {
    position: relative;
    z-index: 1;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .card-meta {
    display: flex;
    align-items: center;
    gap: 10px;

    .meta-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;

      &.blue { background: linear-gradient(135deg, #dbeafe, #bfdbfe); color: #2563eb; }
      &.green { background: linear-gradient(135deg, #d1fae5, #a7f3d0); color: #10b981; }
      &.orange { background: linear-gradient(135deg, #fef3c7, #fde68a); color: #f59e0b; }
      &.red { background: linear-gradient(135deg, #fee2e2, #fecaca); color: #ef4444; }
      &.purple { background: linear-gradient(135deg, #ede9fe, #ddd6fe); color: #7c3aed; }
    }

    .meta-label {
      font-size: 13px;
      font-weight: 500;
      color: var(--rf-text-secondary);
    }
  }

  .card-value {
    font-size: 32px;
    font-weight: 700;
    color: var(--rf-text-main);
    letter-spacing: -0.03em;
    line-height: 1;
  }
}

// -------- 图表区域 --------
.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.chart-card {
  background: var(--rf-bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--rf-border-light);
  padding: 20px;
  transition: box-shadow var(--duration-base) var(--ease-out-quart);

  &:hover {
    box-shadow: var(--shadow-md);
  }

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;

    .chart-title {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
      color: var(--rf-text-main);
      letter-spacing: -0.01em;
    }

    .chart-desc {
      margin: 4px 0 0;
      font-size: 12px;
      color: var(--rf-text-muted);
    }

    .chart-legend {
      display: flex;
      gap: 12px;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 11px;
        color: var(--rf-text-muted);

        .dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;

          &.blue { background: #3b82f6; }
          &.green { background: #10b981; }
          &.orange { background: #f59e0b; }
        }
      }
    }
  }

  .chart-body {
    height: 260px;
  }
}

// -------- 日志表格 --------
.log-card {
  background: var(--rf-bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--rf-border-light);
  padding: 20px;
  transition: box-shadow var(--duration-base) var(--ease-out-quart);

  &:hover {
    box-shadow: var(--shadow-md);
  }

  .log-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;

    .log-title {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
      color: var(--rf-text-main);
      letter-spacing: -0.01em;
    }

    .log-desc {
      margin: 4px 0 0;
      font-size: 12px;
      color: var(--rf-text-muted);
    }

    .view-all-btn {
      font-weight: 500;
    }
  }
}

// -------- 表格样式覆盖 --------
:deep(.rf-table) {
  .el-table__header th {
    background: var(--rf-neutral-50);
    font-weight: 600;
    font-size: 12px;
    color: var(--rf-text-secondary);
    height: 40px;
  }

  .el-table__row {
    transition: background 0.15s;

    &:hover {
      background: var(--rf-neutral-50);
    }
  }

  .el-table__cell {
    font-size: 13px;
    color: var(--rf-text-main);
    padding: 10px 0;
  }
}

.id-badge {
  font-family: var(--font-mono, monospace);
  font-size: 12px;
  font-weight: 500;
  color: var(--rf-primary);
  background: var(--rf-primary-light);
  padding: 3px 8px;
  border-radius: 6px;
}

.method-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  background: var(--rf-neutral-100);
  color: var(--rf-text-secondary);

  &.get {
    background: #d1fae5;
    color: #059669;
  }

  &.post {
    background: #dbeafe;
    color: #2563eb;
  }

  &.put {
    background: #fef3c7;
    color: #d97706;
  }

  &.delete {
    background: #fee2e2;
    color: #dc2626;
  }
}

.type-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.03em;

  &.info {
    background: #e0e7ff;
    color: #4f46e5;
  }

  &.error, &.danger {
    background: #fee2e2;
    color: #dc2626;
  }

  &.warn, &.warning {
    background: #fef3c7;
    color: #d97706;
  }

  &.success {
    background: #d1fae5;
    color: #059669;
  }
}

.time-text {
  font-size: 12px;
  color: var(--rf-text-muted);
  font-family: var(--font-mono, monospace);
}

// 响应式
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }

  .charts-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-page {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
