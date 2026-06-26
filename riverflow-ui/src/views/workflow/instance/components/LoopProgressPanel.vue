<template>
  <div class="loop-progress-panel" v-loading="loading">
    <el-empty v-if="!loading && loopList.length === 0" description="暂无循环节点" />

    <div v-for="loop in loopList" :key="loop.loopNodeId" class="loop-card">
      <div class="loop-header">
        <div class="loop-title">
          <el-tag :type="loop.nodeType === 'foreach' ? 'primary' : 'success'" size="small">
            {{ loop.nodeType === 'foreach' ? 'foreach' : 'while' }}
          </el-tag>
          <span class="loop-name">{{ loop.nodeName }}</span>
          <span class="loop-node-id">{{ loop.loopNodeId }}</span>
        </div>
        <div class="loop-summary">
          <span class="summary-item">进度 <strong>{{ loop.progress }}%</strong></span>
          <span class="summary-item">完成 <strong>{{ loop.completed }}</strong> / {{ loop.total }}</span>
          <span v-if="loop.failed > 0" class="summary-item failed">失败 <strong>{{ loop.failed }}</strong></span>
          <span v-if="loop.running > 0" class="summary-item running">运行中 <strong>{{ loop.running }}</strong></span>
          <span v-if="loop.pending > 0" class="summary-item pending">待执行 <strong>{{ loop.pending }}</strong></span>
        </div>
      </div>

      <el-progress
        :percentage="loop.progress"
        :status="loop.progress >= 100 ? 'success' : undefined"
        :stroke-width="10"
        class="loop-progress"
      />

      <el-collapse v-if="loop.iterations.length > 0" class="iteration-collapse">
        <el-collapse-item :title="`迭代明细 (${loop.iterations.length})`">
          <el-table :data="loop.iterations" stripe size="small" max-height="260">
            <el-table-column prop="iterationIndex" label="迭代" width="80" align="center">
              <template #default="{ row }">
                <span>{{ row.iterationIndex != null ? row.iterationIndex : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="nodeName" label="节点" min-width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="taskStatusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="executeCount" label="执行次数" width="90" align="center" />
            <el-table-column prop="errorMsg" label="错误信息" min-width="160" show-overflow-tooltip />
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getLoopProgress, getInstanceTasks } from '@/api/workflow'

const props = defineProps({
  instanceId: {
    type: [String, Number],
    required: true
  }
})

const loading = ref(false)
const loopList = ref([])

function taskStatusType(status) {
  const map = {
    pending: 'info',
    running: 'primary',
    success: 'success',
    fail: 'danger',
    waiting: 'warning',
    skipped: 'info'
  }
  return map[status] || 'info'
}

async function loadLoopProgress() {
  if (!props.instanceId) return
  loading.value = true
  try {
    const tasksRes = await getInstanceTasks(props.instanceId)
    const tasks = tasksRes || []
    const loopNodeIds = new Set()
    for (const task of tasks) {
      if (task.loopNodeId && (task.nodeType === 'foreach' || task.nodeType === 'while')) {
        loopNodeIds.add(task.loopNodeId)
      }
    }

    const loops = []
    for (const loopNodeId of loopNodeIds) {
      try {
        const progressRes = await getLoopProgress(props.instanceId, loopNodeId)
        if (progressRes) {
          loops.push({
            loopNodeId: progressRes.loopNodeId || loopNodeId,
            nodeName: tasks.find(t => t.nodeId === loopNodeId)?.nodeName || loopNodeId,
            nodeType: tasks.find(t => t.nodeId === loopNodeId)?.nodeType || 'foreach',
            parallel: progressRes.parallel,
            total: progressRes.total || 0,
            currentIndex: progressRes.currentIndex || 0,
            progress: progressRes.progress || 0,
            completed: progressRes.completed || 0,
            failed: progressRes.failed || 0,
            running: progressRes.running || 0,
            pending: progressRes.pending || 0,
            iterations: progressRes.iterations || []
          })
        }
      } catch (e) {
        console.warn('加载循环进度失败', loopNodeId, e)
      }
    }
    loopList.value = loops
  } catch (e) {
    console.error('加载循环概览失败', e)
    ElMessage.error('加载循环概览失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.instanceId, () => {
  loadLoopProgress()
}, { immediate: true })
</script>

<style scoped lang="scss">
.loop-progress-panel {
  .loop-card {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .loop-header {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .loop-title {
      display: flex;
      align-items: center;
      gap: 8px;

      .loop-name {
        font-weight: 600;
        color: #1e293b;
      }

      .loop-node-id {
        font-size: 12px;
        color: #94a3b8;
        font-family: monospace;
      }
    }

    .loop-summary {
      display: flex;
      gap: 12px;
      font-size: 13px;
      color: #475569;

      .summary-item {
        strong {
          color: #1e293b;
        }

        &.failed {
          color: #ef4444;
        }

        &.running {
          color: #3b82f6;
        }

        &.pending {
          color: #f59e0b;
        }
      }
    }
  }

  .loop-progress {
    margin-bottom: 12px;
  }

  .iteration-collapse {
    :deep(.el-collapse-item__header) {
      font-size: 13px;
      color: #475569;
      font-weight: 500;
    }
  }
}
</style>
