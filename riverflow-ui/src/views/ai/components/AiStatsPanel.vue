<template>
  <div class="ai-stats-page">
    <div class="stats-header">
      <div class="header-title">{{ $t('aiStatsPanel._a_i调用统计_9') }}</div>
      <el-radio-group v-model="dateRange" size="small" @change="handleDateChange">
        <el-radio-button label="today">{{ $t('aiStatsPanel.今天_10') }}</el-radio-button>
        <el-radio-button label="week">{{ $t('aiStatsPanel.近7天_11') }}</el-radio-button>
        <el-radio-button label="month">{{ $t('aiStatsPanel.近30天_12') }}</el-radio-button>
      </el-radio-group>
    </div>

    <div class="stats-overview">
      <div class="stats-card">
        <div class="stats-value">{{ stats.totalCalls || 0 }}</div>
        <div class="stats-label">{{ $t('aiStatsPanel.总调用次数_13') }}</div>
      </div>
      <div class="stats-card success">
        <div class="stats-value">{{ stats.successCalls || 0 }}</div>
        <div class="stats-label">{{ $t('aiStatsPanel.成功次数_14') }}</div>
      </div>
      <div class="stats-card danger">
        <div class="stats-value">{{ stats.failCalls || 0 }}</div>
        <div class="stats-label">{{ $t('aiStatsPanel.失败次数_15') }}</div>
      </div>
      <div class="stats-card info">
        <div class="stats-value">{{ stats.totalTokens || 0 }}</div>
        <div class="stats-label">{{ $t('aiStatsPanel.总_token数_16') }}</div>
      </div>
      <div class="stats-card warning">
        <div class="stats-value">{{ Math.round(stats.avgResponseTime || 0) }}</div>
        <div class="stats-label">{{ $t('aiStatsPanel.平均耗时ms_17') }}</div>
      </div>
    </div>

    <div class="stats-charts">
      <div class="chart-box">
        <div class="chart-title">{{ $t('aiStatsPanel.按场景分布_18') }}</div>
        <div class="bar-list">
          <div v-for="item in stats.byScene" :key="item.name" class="bar-item">
            <span class="bar-name">{{ sceneLabel(item.name) }}</span>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: barWidth(item.value, maxSceneValue) }"></div>
            </div>
            <span class="bar-value">{{ item.value }}</span>
          </div>
        </div>
      </div>

      <div class="chart-box">
        <div class="chart-title">{{ $t('aiStatsPanel.按_provide_19') }}</div>
        <div class="bar-list">
          <div v-for="item in stats.byProvider" :key="item.name" class="bar-item">
            <span class="bar-name">{{ item.name || 'unknown' }}</span>
            <div class="bar-track">
              <div class="bar-fill provider" :style="{ width: barWidth(item.value, maxProviderValue) }"></div>
            </div>
            <span class="bar-value">{{ item.value }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const dateRange = ref('week')
const stats = reactive({
  totalCalls: 0,
  successCalls: 0,
  failCalls: 0,
  totalTokens: 0,
  avgResponseTime: 0,
  byScene: [],
  byDate: [],
  byProvider: []
})

const maxSceneValue = computed(() => {
  const values = stats.byScene.map(i => i.value)
  return values.length > 0 ? Math.max(...values) : 1
})

const maxProviderValue = computed(() => {
  const values = stats.byProvider.map(i => i.value)
  return values.length > 0 ? Math.max(...values) : 1
})

onMounted(() => {
  handleDateChange()
})

function handleDateChange() {
  const now = new Date()
  let start
  if (dateRange.value === 'today') {
    start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
  } else if (dateRange.value === 'week') {
    start = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
  } else {
    start = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
  }
  loadStats(formatDate(start), formatDate(now))
}

function formatDate(date) {
  const pad = n => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

async function loadStats(startTime, endTime) {
  try {
    const res = await request.get('/ai/audit/stats', {
      params: { startTime, endTime }
    })
    Object.assign(stats, res)
  } catch (err) {
    ElMessage.error(err.message || t('aiStatsPanel.加载统计数据失败_1'))
  }
}

function sceneLabel(name) {
  const map = {
    flow: t('aiStatsPanel.流程生成_2'),
    condition: t('aiStatsPanel.条件生成_3'),
    mapping: t('aiStatsPanel.映射推荐_4'),
    script: t('aiStatsPanel.脚本生成_5'),
    'api-doc': t('aiStatsPanel.接口文档解析_6'),
    chat: t('aiStatsPanel.对话_7'),
    'chat-stream': t('aiStatsPanel.流式对话_8')
  }
  return map[name] || name
}

function barWidth(value, max) {
  return Math.max((value / max) * 100, 5) + '%'
}
</script>

<style scoped>
.ai-stats-page {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.stats-overview {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}
.stats-card {
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}
.stats-card.success {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
}
.stats-card.danger {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
}
.stats-card.info {
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
}
.stats-card.warning {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
}
.stats-value {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}
.stats-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}
.stats-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.chart-box {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
}
.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 12px;
}
.bar-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.bar-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
.bar-name {
  width: 80px;
  color: #475569;
  flex-shrink: 0;
}
.bar-track {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  border-radius: 4px;
  transition: width 0.5s ease;
}
.bar-fill.provider {
  background: linear-gradient(90deg, #8b5cf6, #a78bfa);
}
.bar-value {
  width: 40px;
  text-align: right;
  color: #334155;
  font-weight: 600;
}
</style>
