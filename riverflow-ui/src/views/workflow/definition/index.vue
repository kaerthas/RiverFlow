<template>
  <div class="rf-page">
    <div class="rf-page-title">
      <el-icon><Share /></el-icon>
      流程定义
    </div>

    <div class="rf-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon> 新建流程
        </el-button>
      </div>

      <el-table :data="tableData" stripe size="small" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="flowCode" label="流程编码" width="160" />
        <el-table-column prop="flowName" label="流程名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="itemCode" label="绑定事项" width="160" />
        <el-table-column prop="triggerType" label="触发方式" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.triggerType === 'cron'" size="small" type="primary">定时</el-tag>
            <el-tag v-else-if="row.triggerType === 'event'" size="small" type="success">事件</el-tag>
            <el-tag v-else size="small">手动</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">已发布</el-tag>
            <el-tag v-else-if="row.status === 0" size="small">草稿</el-tag>
            <el-tag v-else type="info" size="small">已下线</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDesign(row)">
              <el-icon><Edit /></el-icon> 设计
            </el-button>
            <el-button v-if="row.status === 0" link type="success" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 1" link type="warning" size="small" @click="handleOffline(row)">下线</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑流程" width="500px" destroy-on-close>
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="流程编码">
          <el-input v-model="editForm.flowCode" disabled />
        </el-form-item>
        <el-form-item label="流程名称">
          <el-input v-model="editForm.flowName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="绑定事项">
          <el-input v-model="editForm.itemCode" placeholder="请输入事项编码" />
        </el-form-item>
        <el-form-item label="触发方式">
          <el-select v-model="editForm.triggerType" style="width: 100%">
            <el-option label="手动" value="manual" />
            <el-option label="定时" value="cron" />
            <el-option label="事件" value="event" />
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

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

async function handleSearch() {
  loading.value = true
  try {
    const res = await getFlowDefinitionList({
      page: pagination.page,
      size: pagination.size
    })
    if (res && res.records) {
      tableData.value = res.records
      pagination.total = res.total
    }
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
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
    await ElMessageBox.confirm(`确认删除流程「${row.flowName}」？`, '删除确认', { type: 'warning' })
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
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
