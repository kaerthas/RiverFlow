<template>
  <div class="rf-list-page">
    <!-- 页面头部 -->
    <div class="rf-list-header">
      <div>
        <h1 class="title">流程定义</h1>
        <p class="subtitle">管理和配置业务流程模板，支持拖拽编排与可视化设计</p>
      </div>
      <button class="btn-primary" @click="handleCreate">
        <el-icon :size="16"><Plus /></el-icon>
        <span>新建流程</span>
      </button>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="rf-search-bar">
      <div class="search-fields">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索流程名称或编码"
          clearable
          style="width: 260px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px" @change="handleSearch">
          <el-option label="已发布" :value="1" />
          <el-option label="草稿" :value="0" />
          <el-option label="已下线" :value="2" />
        </el-select>
        <el-select v-model="searchForm.triggerType" placeholder="全部触发方式" clearable style="width: 150px" @change="handleSearch">
          <el-option label="手动" value="manual" />
          <el-option label="定时" value="cron" />
          <el-option label="事件" value="event" />
        </el-select>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch">
          <el-icon><Search /></el-icon>
          <span>查询</span>
        </button>
        <button class="btn-reset" @click="handleReset">
          <el-icon><RefreshRight /></el-icon>
          <span>重置</span>
        </button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rf-table-card">
      <el-table :data="tableData" v-loading="loading" class="rf-data-table" :fit="false" max-height="480">
        <el-table-column type="index" label="#" width="52" align="center" />

        <el-table-column prop="flowCode" label="流程编码" width="260">
          <template #default="{ row }">
            <span class="rf-code">{{ row.flowCode }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="flowName" label="流程名称" min-width="200" show-overflow-tooltip />

        <el-table-column prop="itemCode" label="绑定事项" width="150">
          <template #default="{ row }">
            <span class="rf-mono" style="font-size: 12px; color: var(--rf-text-secondary)">{{ row.itemCode || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="triggerType" label="触发方式" width="120" align="center">
          <template #default="{ row }">
            <span :class="['rf-tag', row.triggerType || 'manual']">{{ triggerLabel(row.triggerType) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="version" label="版本" width="100" align="center">
          <template #default="{ row }">
            <span class="rf-mono" style="font-size: 12px; color: var(--rf-text-muted)">v{{ row.version }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <span :class="['rf-status', statusClass(row.status)]">
              <span class="dot"></span>
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="195">
          <template #default="{ row }">
            <span class="rf-time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <el-tooltip content="设计" placement="top">
                <button class="action-btn primary" @click="handleDesign(row)">
                  <el-icon><EditPen /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 0" content="发布" placement="top">
                <button class="action-btn success" @click="handlePublish(row)">
                  <el-icon><Promotion /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip v-if="row.status === 1" content="下线" placement="top">
                <button class="action-btn warning" @click="handleOffline(row)">
                  <el-icon><Download /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <button class="action-btn" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <button class="action-btn danger" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                </button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <!-- 空状态插槽：替换表格默认的暂无数据 -->
        <template #empty>
          <div class="rf-empty">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.2" style="margin-bottom: 16px">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <path d="M3 9h18" />
              <path d="M9 21V9" />
            </svg>
            <div class="empty-title">暂无流程定义</div>
            <div class="empty-desc">点击右上角「新建流程」创建第一个业务流程</div>
          </div>
        </template>
      </el-table>

      <!-- 分页 -->
      <div class="rf-pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @update:page-size="handleSearch"
          @update:current-page="handleSearch"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑流程" width="520px" destroy-on-close class="edit-dialog">
      <el-form :model="editForm" label-width="100px" class="edit-form">
        <el-form-item label="流程编码">
          <el-input v-model="editForm.flowCode" disabled />
        </el-form-item>
        <el-form-item label="流程名称">
          <el-input v-model="editForm.flowName" placeholder="请输入流程名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="绑定事项">
          <el-input v-model="editForm.itemCode" placeholder="请输入事项编码" />
        </el-form-item>
        <el-form-item label="触发方式">
          <el-select v-model="editForm.triggerType" style="width: 100%">
            <el-option label="手动触发" value="manual" />
            <el-option label="定时触发" value="cron" />
            <el-option label="事件触发" value="event" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFlowDefinitionList, publishFlowDefinition, offlineFlowDefinition, deleteFlowDefinition, saveFlowDefinition } from '@/api/workflow'

const router = useRouter()
const loading = ref(false)
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({ id: null, flowCode: '', flowName: '', itemCode: '', triggerType: 'manual' })

const searchForm = reactive({ keyword: '', status: '', triggerType: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

function triggerLabel(type) {
  const map = { manual: '手动', cron: '定时', event: '事件' }
  return map[type] || '手动'
}

function statusLabel(status) {
  const map = { 1: '已发布', 0: '草稿', 2: '已下线' }
  return map[status] || '未知'
}

function statusClass(status) {
  const map = { 1: 'published', 0: 'draft', 2: 'offline' }
  return map[status] || 'draft'
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

async function handleSearch() {
  loading.value = true
  try {
    const res = await getFlowDefinitionList({
      page: pagination.page,
      size: pagination.size,
      flowName: searchForm.keyword || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined,
      triggerType: searchForm.triggerType || undefined
    })
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
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.triggerType = ''
  pagination.page = 1
  handleSearch()
}

function handleCreate() {
  router.push('/workflow/designer')
}

function handleDesign(row) {
  router.push({ path: '/workflow/designer', query: { id: row.id } })
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm(`确认发布流程「${row.flowName}」？`, '发布确认', { type: 'warning' })
    await publishFlowDefinition(row.id)
    row.status = 1
    ElMessage.success('流程发布成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('发布失败: ' + e.message)
  }
}

async function handleOffline(row) {
  try {
    await ElMessageBox.confirm(`确认下线流程「${row.flowName}」？`, '下线确认', { type: 'warning' })
    await offlineFlowDefinition(row.id)
    row.status = 2
    ElMessage.success('流程已下线')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('下线失败: ' + e.message)
  }
}

function handleEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    flowCode: row.flowCode,
    flowName: row.flowName,
    itemCode: row.itemCode,
    triggerType: row.triggerType || 'manual'
  })
  editVisible.value = true
}

async function confirmEdit() {
  editLoading.value = true
  try {
    await saveFlowDefinition({
      id: editForm.id,
      flowName: editForm.flowName,
      itemCode: editForm.itemCode,
      triggerType: editForm.triggerType
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    handleSearch()
  } catch (e) {
    ElMessage.error('保存失败: ' + e.message)
  } finally {
    editLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除流程「${row.flowName}」？删除后不可恢复。`, '删除确认', { type: 'warning' })
    await deleteFlowDefinition(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败: ' + e.message)
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.edit-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px 12px;
    margin-right: 0;
    border-bottom: 1px solid #f1f5f9;

    .el-dialog__title {
      font-size: 16px;
      font-weight: 700;
      color: var(--rf-text-main);
      letter-spacing: -0.01em;
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 12px 24px 20px;
    border-top: 1px solid #f1f5f9;
  }
}

.edit-form {
  :deep(.el-form-item__label) {
    font-size: 13px;
    font-weight: 500;
    color: var(--rf-text-secondary);
  }
}

// 空状态插槽样式
.rf-data-table {
  :deep(.el-table__empty-block) {
    // 去掉全局样式带来的虚线圆圈伪元素
    &::before {
      display: none !important;
    }
  }
}
</style>
