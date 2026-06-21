<template>
  <div class="dashboard-page">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ $t('dashboard.数据大盘_8a3adf76') }}</h1>
        <p class="page-subtitle">{{ $t('dashboard.实时监控流程_2438e3dd') }}</p>
      </div>
      <div class="header-actions">
        <div class="live-indicator">
          <span class="pulse-dot"></span>
          <span class="live-text">{{ $t('dashboard.实时更新_aca43234') }}</span>
        </div>
      </div>
    </div>

    <!-- Bento 统计卡片：非对称布局 -->
    <div class="bento-grid">
      <!-- 主卡片：总数 -->
      <div class="bento-card bento-card--primary">
        <div class="card-bg-glow"></div>
        <div class="card-content">
          <div class="card-meta">
            <div class="meta-icon blue">
              <el-icon :size="20"><Document /></el-icon>
            </div>
            <span class="meta-label">{{ $t('dashboard.流程实例总数_bf47a6ae') }}</span>
          </div>
          <div class="card-value rf-mono">{{ stats.total }}</div>
          <div class="card-trend">
            <span class="trend-badge up">
              <el-icon><ArrowUp /></el-icon> 12.5%
            </span>
            <span class="trend-label">{{ $t('dashboard.较上月_bfb0700f') }}</span>
          </div>
        </div>
      </div>

      <!-- 次卡片：已完成 -->
      <div class="bento-card bento-card--success">
        <div class="card-content">
          <div class="card-meta">
            <div class="meta-icon green">
              <el-icon :size="20"><CircleCheck /></el-icon>
            </div>
            <span class="meta-label">{{ $t('dashboard.已完成_fad5222c_1') }}</span>
          </div>
          <div class="card-value rf-mono">{{ stats.completed }}</div>
          <div class="card-trend">
            <span class="trend-badge up">
              <el-icon><ArrowUp /></el-icon> 8.3%
            </span>
            <span class="trend-label">{{ $t('dashboard.较上月_bfb0700f_1') }}</span>
          </div>
        </div>
      </div>

      <!-- 小卡片：运行中 -->
      <div class="bento-card bento-card--small">
        <div class="card-content">
          <div class="card-meta">
            <div class="meta-icon orange">
              <el-icon :size="18"><Loading /></el-icon>
            </div>
            <span class="meta-label">{{ $t('dashboard.运行中_d679aea3_1') }}</span>
          </div>
          <div class="card-value rf-mono" style="font-size: 28px;">{{ stats.running }}</div>
        </div>
      </div>

      <!-- 小卡片：失败 -->
      <div class="bento-card bento-card--small">
        <div class="card-content">
          <div class="card-meta">
            <div class="meta-icon red">
              <el-icon :size="18"><Warning /></el-icon>
            </div>
            <span class="meta-label">{{$t('dashboard.失败或异常_d679aea3')}}</span>
          </div>
          <div class="card-value rf-mono" style="font-size: 28px;">{{ stats.failed }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card chart-card--wide">
        <div class="chart-header">
          <div>
            <h3 class="chart-title">{{ $t('dashboard.流程实例状态_851dcbd9') }}</h3>
            <p class="chart-desc">{{ $t('dashboard.近30天实例创建与完成趋势_d679aea') }}</p>
          </div>
          <div class="chart-legend">
            <span class="legend-item"><span class="dot blue"></span>{{ $t('dashboard.总数_9ed7d3ad') }}</span>
            <span class="legend-item"><span class="dot green"></span>{{ $t('dashboard.已完成_fad5222c_1') }}</span>
            <span class="legend-item"><span class="dot orange"></span>{{ $t('dashboard.运行中_d679aea3_1') }}</span>
          </div>
        </div>
        <div ref="barChartRef" class="chart-body"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <div>
            <h3 class="chart-title">{{ $t('dashboard.实例状态占比_f2656a33') }}</h3>
            <p class="chart-desc">{{ $t('dashboard.当前实例分布_f27e68e6') }}</p>
          </div>
        </div>
        <div ref="pieChartRef" class="chart-body"></div>
      </div>
    </div>

    <!-- 最近日志 -->
    <div class="log-card">
      <div class="log-header">
        <div>
          <h3 class="log-title">{{ $t('dashboard.最近运行日志_21701682') }}</h3>
          <p class="log-desc">{{ $t('dashboard.系统最近执行_c2d90fd0') }}</p>
        </div>
        <el-button type="primary" text size="small" class="view-all-btn" @click="$router.push('/workflow/instance')">{{ $t('dashboard.查看全部_0467cc92') }}<el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-table :data="recentLogs" size="default" v-loading="logLoading" class="rf-table">
        <el-table-column prop="instanceId" :label="$t('dashboard.实例_1782d6af')" width="160">
          <template #default="{ row }">
            <span class="id-badge">{{ row.instanceId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nodeName" :label="$t('dashboard.节点名称_b1785ef0')" min-width="160" show-overflow-tooltip />
        <el-table-column prop="logType" :label="$t('dashboard.类型_226b0912')" width="90">
          <template #default="{ row }">
            <span :class="['type-tag', row.logType || 'info']">
              {{ row.logType || 'info' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="message" :label="$t('dashboard.日志内容_a19a72d2')" min-width="240" show-overflow-tooltip />
        <el-table-column prop="createTime" :label="$t('dashboard.时间_19fcb9eb')" width="160">
          <template #default="{ row }">
            <span class="time-text">{{ row.createTime }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getMonitorStats, getRecentLogs } from '@/api/monitor'

const stats = reactive({
  total: 1247,
  running: 23,
  completed: 1189,
  failed: 35
})
const recentLogs = ref([])
const logLoading = ref(false)

const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null

function initBarChart() {
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
      data: [t('dashboard.周一_1603b069'), t('dashboard.周二_b5a6a07e'), t('dashboard.周三_e60725e7'), t('dashboard.周四_170fc8e2'), t('dashboard.周五_eb79cea6'), t('dashboard.周六_24575130'), t('dashboard.周日_562d7476')],
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisTick: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } },
      axisLabel: { color: '#9ca3af', fontSize: 11 }
    },
    series: [{
      data: [120, 182, 151, 194, 230, 180, 210],
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
      name: t('dashboard.实例状态_2eeb27f9'),
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
        { value: stats.completed, name: t('dashboard.已完成_fad5222c'), itemStyle: { color: '#10b981' } },
        { value: stats.running, name: t('dashboard.运行中_d679aea3'), itemStyle: { color: '#f59e0b' } },
        { value: stats.failed, name: t('dashboard.失败_acd5cb84'), itemStyle: { color: '#ef4444' } }
      ].filter(d => d.value > 0)
    }]
  }
  pieChart.setOption(option)
}

async function loadStats() {
  try {
    const res = await getMonitorStats()
    Object.assign(stats, res)
    await nextTick()
    initBarChart()
    initPieChart()
  } catch (e) {
    // 静默忽略，保持默认演示数据
    await nextTick()
    initBarChart()
    initPieChart()
  }
}

async function loadLogs() {
  logLoading.value = true
  try {
    const res = await getRecentLogs(10)
    recentLogs.value = Array.isArray(res) ? res : []
  } finally {
    logLoading.value = false
  }
}

function handleResize() {
  barChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  loadStats()
  loadLogs()
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
  margin-bottom: 28px;

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

// -------- Bento 网格 --------
.bento-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
  height: 220px;
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

  .card-bg-glow {
    position: absolute;
    top: -60%;
    right: -20%;
    width: 200px;
    height: 200px;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.15;
    pointer-events: none;
  }

  .card-content {
    position: relative;
    z-index: 1;
    padding: 20px;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
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
    }

    .meta-label {
      font-size: 13px;
      font-weight: 500;
      color: var(--rf-text-secondary);
    }
  }

  .card-value {
    font-size: 36px;
    font-weight: 700;
    color: var(--rf-text-main);
    letter-spacing: -0.03em;
    line-height: 1;
    margin: 8px 0;
  }

  .card-trend {
    display: flex;
    align-items: center;
    gap: 8px;

    .trend-badge {
      display: inline-flex;
      align-items: center;
      gap: 3px;
      font-size: 12px;
      font-weight: 600;
      padding: 3px 8px;
      border-radius: 6px;

      &.up {
        background: #d1fae5;
        color: #059669;
      }
    }

    .trend-label {
      font-size: 12px;
      color: var(--rf-text-muted);
    }
  }
}

.bento-card--primary {
  grid-row: 1 / 3;

  .card-bg-glow {
    background: #3b82f6;
  }

  .card-value {
    font-size: 44px;
  }
}

.bento-card--success {
  grid-row: 1 / 3;

  .card-bg-glow {
    background: #10b981;
  }
}

.bento-card--small {
  .card-content {
    padding: 16px;
    justify-content: center;
    gap: 8px;
  }

  .card-meta {
    margin-bottom: 4px;
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

.type-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
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
  .bento-grid {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto;
    height: auto;

    .bento-card--primary,
    .bento-card--success {
      grid-row: auto;
    }
  }

  .charts-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-page {
    padding: 16px;
  }

  .bento-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
