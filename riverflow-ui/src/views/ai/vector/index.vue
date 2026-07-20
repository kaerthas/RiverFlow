<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">向量库管理</h1>
        <p class="subtitle">管理向量集合配置与 Embedding 接入方式</p>
      </div>
      <div class="header-actions">
        <button class="btn-primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增集合
        </button>
      </div>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="集合名称">
            <el-input v-model="queryForm.keyword" placeholder="集合名称/描述" clearable @keyup.enter="loadData" />
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="loadData">
          <el-icon><Search /></el-icon>查询
        </button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableData" v-loading="loading" class="rf-data-table" :empty-text="'暂无数据'">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="collection" label="集合名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="storeType" label="向量库类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="storeTypeTagType(row.storeType)">{{ row.storeType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dimension" label="维度" width="100" align="center" />
        <el-table-column prop="distanceMetric" label="距离度量" width="120" align="center" />
        <el-table-column prop="embeddingType" label="Embedding 类型" width="140" align="center" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.enabled === 1" type="success">启用</el-tag>
            <el-tag v-else type="info">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn info" title="测试连接" @click="handleTestStore(row)">
                <el-icon><Connection /></el-icon>
              </button>
              <button class="action-btn primary" title="编辑" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button class="action-btn danger" title="删除" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="rf-pagination">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @update:current-page="loadData"
          @update:page-size="loadData"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="集合名称" prop="collection">
          <el-input v-model="form.collection" placeholder="如 riverflow_default" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="向量库类型" prop="storeType">
          <el-select v-model="form.storeType" placeholder="请选择" style="width: 100%">
            <el-option label="Milvus" value="milvus" />
            <el-option label="PGVector" value="pgvector" />
            <el-option label="Memory（测试）" value="memory" />
          </el-select>
        </el-form-item>
        <el-form-item label="向量维度" prop="dimension">
          <el-input-number v-model="form.dimension" :min="1" :max="8192" style="width: 100%" />
        </el-form-item>
        <el-form-item label="距离度量" prop="distanceMetric">
          <el-select v-model="form.distanceMetric" placeholder="请选择" style="width: 100%">
            <el-option label="COSINE" value="COSINE" />
            <el-option label="IP" value="IP" />
            <el-option label="L2" value="L2" />
          </el-select>
        </el-form-item>
        <el-form-item label="Embedding 类型" prop="embeddingType">
          <el-select v-model="form.embeddingType" placeholder="请选择" style="width: 100%">
            <el-option label="Ollama" value="ollama" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="Memory（测试）" value="memory" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="集合用途描述" />
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-radio-group v-model="form.enabled">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Connection } from '@element-plus/icons-vue'
import {
  listVectorCollections,
  getVectorCollection,
  createVectorCollection,
  updateVectorCollection,
  deleteVectorCollection,
  testVectorStore
} from '@/api/ai/vector'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const submitting = ref(false)
const isEdit = computed(() => !!form.id)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const form = reactive({
  id: null,
  collection: '',
  storeType: 'memory',
  dimension: 768,
  distanceMetric: 'COSINE',
  embeddingType: 'ollama',
  description: '',
  enabled: 1
})

const formRules = {
  collection: [{ required: true, message: '请输入集合名称', trigger: 'blur' }],
  storeType: [{ required: true, message: '请选择向量库类型', trigger: 'change' }],
  dimension: [{ required: true, message: '请输入向量维度', trigger: 'blur' }],
  distanceMetric: [{ required: true, message: '请选择距离度量', trigger: 'change' }],
  embeddingType: [{ required: true, message: '请选择 Embedding 类型', trigger: 'change' }]
}

onMounted(() => {
  loadData()
})

const storeTypeTagType = (type) => {
  switch (type) {
    case 'milvus': return 'primary'
    case 'pgvector': return 'success'
    case 'memory': return 'info'
    default: return ''
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await listVectorCollections({
      page: queryForm.page,
      size: queryForm.size,
      keyword: queryForm.keyword
    })
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    console.error('加载向量集合失败', e)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.page = 1
  loadData()
}

const resetForm = () => {
  form.id = null
  form.collection = ''
  form.storeType = 'memory'
  form.dimension = 768
  form.distanceMetric = 'COSINE'
  form.embeddingType = 'ollama'
  form.description = ''
  form.enabled = 1
}

const openAddDialog = () => {
  resetForm()
  dialogTitle.value = '新增向量集合'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  dialogTitle.value = '编辑向量集合'
  try {
    const res = await getVectorCollection(row.id)
    Object.assign(form, res)
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除集合「${row.collection}」？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteVectorCollection(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

const handleTestStore = async (row) => {
  try {
    const res = await testVectorStore(row.id)
    if (res) {
      ElMessage.success(res)
    } else {
      ElMessage.warning('测试返回为空')
    }
  } catch (e) {
    console.error('测试向量库失败', e)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      await updateVectorCollection(form.id, data)
    } else {
      await createVectorCollection(data)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error('保存向量集合失败', e)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 10px;
}
</style>
