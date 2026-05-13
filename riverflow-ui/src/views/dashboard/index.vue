<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><Odometer /></el-icon>
      数据大盘
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon blue"><el-icon><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">128</div>
            <div class="stat-label">政务事项</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon green"><el-icon><Share /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">36</div>
            <div class="stat-label">流程定义</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon orange"><el-icon><Loading /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">1,024</div>
            <div class="stat-label">运行中实例</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon red"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">12</div>
            <div class="stat-label">异常告警</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="16">
        <div class="rf-card chart-card">
          <div class="chart-header">
            <h3>流程实例趋势</h3>
            <el-radio-group v-model="trendRange" size="small">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-placeholder">
            <el-empty description="图表组件待接入">
              <el-icon :size="48" color="#D9D9D9"><DataAnalysis /></el-icon>
            </el-empty>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="rf-card chart-card">
          <div class="chart-header">
            <h3>节点类型分布</h3>
          </div>
          <div class="chart-placeholder">
            <el-empty description="图表组件待接入">
              <el-icon :size="48" color="#D9D9D9"><PieChart /></el-icon>
            </el-empty>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近实例 -->
    <div class="rf-card instance-table">
      <div class="table-header">
        <h3>最近运行的流程实例</h3>
        <el-button type="primary" text size="small" @click="$router.push('/workflow/instance')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-table :data="recentInstances" stripe size="small">
        <el-table-column prop="id" label="实例ID" width="180" />
        <el-table-column prop="flowName" label="流程名称" />
        <el-table-column prop="currentNode" label="当前节点" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="启动时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default>
            <el-button link type="primary" size="small">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const trendRange = ref('week')

const recentInstances = ref([
  { id: '1847293847561', flowName: '残疾人证新办流程', currentNode: '调用省里受理接口', status: 'running', statusText: '运行中', startTime: '2024-05-12 10:23:45' },
  { id: '1847293847562', flowName: '火化信息推送流程', currentNode: '数据校验节点', status: 'success', statusText: '已完成', startTime: '2024-05-12 09:15:22' },
  { id: '1847293847563', flowName: '低保申请协同流程', currentNode: '等待定时节点', status: 'waiting', statusText: '等待中', startTime: '2024-05-12 08:00:00' },
  { id: '1847293847564', flowName: '残疾评定结果回传', currentNode: '调用第三方接口', status: 'failed', statusText: '失败', startTime: '2024-05-11 16:45:10' },
  { id: '1847293847565', flowName: '两补资金发放流程', currentNode: '数据库写入', status: 'running', statusText: '运行中', startTime: '2024-05-11 14:30:00' }
])

function statusType(status) {
  const map = { running: 'primary', success: 'success', waiting: 'warning', failed: 'danger' }
  return map[status] || 'info'
}
</script>

<style scoped lang="scss">
.stat-row {
  margin-bottom: 16px;

  .stat-card {
    background: #FFFFFF;
    border-radius: 8px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
    margin-bottom: 16px;

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;

      &.blue { background: #E6F4FF; color: #1677FF; }
      &.green { background: #F6FFED; color: #52C41A; }
      &.orange { background: #FFF7E6; color: #FAAD14; }
      &.red { background: #FFF1F0; color: #F5222D; }
    }

    .stat-info {
      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #262626;
        line-height: 1.2;
      }
      .stat-label {
        font-size: 14px;
        color: #8C8C8C;
        margin-top: 4px;
      }
    }
  }
}

.chart-row {
  margin-bottom: 16px;

  .chart-card {
    margin-bottom: 16px;

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }
    }

    .chart-placeholder {
      height: 280px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #FAFAFA;
      border-radius: 6px;
    }
  }
}

.instance-table {
  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #262626;
    }
  }
}
</style>
