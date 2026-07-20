<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">AI 知识库管理</h1>
        <p class="subtitle">管理知识文档、分块与向量索引</p>
      </div>
      <div class="header-actions">
        <button class="btn-primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增文档
        </button>
        <button class="btn-primary" @click="rebuildIndex">
          <el-icon><Refresh /></el-icon>重建索引
        </button>
      </div>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="queryForm" inline>
          <el-form-item label="来源类型">
            <el-select v-model="queryForm.sourceType" placeholder="全部" clearable style="width: 160px">
              <el-option label="流程" value="flow" />
              <el-option label="接口" value="api" />
              <el-option label="数据源" value="datasource" />
              <el-option label="上传" value="upload" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="queryForm.keyword" placeholder="标题/内容" clearable />
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="loadDocs">
          <el-icon><Search /></el-icon>查询
        </button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="docList" v-loading="loading" class="rf-data-table" :empty-text="'暂无数据'">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sourceType" label="来源类型" width="120" />
        <el-table-column prop="chunkCount" label="分块数" width="100" />
        <el-table-column prop="vectorStatus" label="索引状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.vectorStatus === 0" type="info">未索引</el-tag>
            <el-tag v-else-if="row.vectorStatus === 1" type="warning">索引中</el-tag>
            <el-tag v-else-if="row.vectorStatus === 2" type="success">已索引</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" title="分块" @click="viewChunks(row)">
                <el-icon><View /></el-icon>
              </button>
              <button class="action-btn danger" title="删除" @click="deleteDoc(row)">
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
          @update:current-page="loadDocs"
          @update:page-size="loadDocs"
        />
      </div>
    </div>

    <!-- 新增文档 -->
    <el-dialog v-model="addDialogVisible" title="新增知识文档" width="600px" destroy-on-close>
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="addForm.title" />
        </el-form-item>
        <el-form-item label="来源类型" prop="sourceType">
          <el-select v-model="addForm.sourceType" placeholder="请选择" style="width: 100%">
            <el-option label="流程" value="flow" />
            <el-option label="接口" value="api" />
            <el-option label="数据源" value="datasource" />
            <el-option label="上传" value="upload" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源ID" prop="sourceId">
          <el-input v-model="addForm.sourceId" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="addForm.content" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="btn-reset" @click="addDialogVisible = false">取消</button>
        <button class="btn-search" :disabled="submitting" @click="submitAdd">
          <el-icon v-if="submitting"><Loading /></el-icon>
          <span v-else>确定</span>
        </button>
      </template>
    </el-dialog>

    <!-- 分块预览 -->
    <el-dialog v-model="chunkDialogVisible" title="分块预览" width="700px" destroy-on-close>
      <el-timeline>
        <el-timeline-item
          v-for="(chunk, index) in chunkList"
          :key="chunk.id"
          :timestamp="'分块 #' + chunk.chunkIndex"
        >
          <el-card>
            <p style="white-space: pre-wrap;">{{ chunk.content }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <!-- 检索测试 -->
    <div class="rf-list-header" style="margin-top: 24px;">
      <div>
        <h1 class="title">语义检索测试</h1>
        <p class="subtitle">输入文本查询相似知识块</p>
      </div>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="searchForm" inline>
          <el-form-item label="查询文本">
            <el-input v-model="searchForm.query" placeholder="输入查询文本" style="width: 320px" />
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" :disabled="searching" @click="testSearch">
          <el-icon><Search /></el-icon>检索
        </button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="searchResults" class="rf-data-table" :empty-text="'暂无数据'">
        <el-table-column type="index" label="#" width="52" align="center" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="score" label="相似度" width="120" />
        <el-table-column prop="metadata.sourceType" label="来源类型" width="120" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Delete, View, Loading } from '@element-plus/icons-vue'
import {
  listKnowledgeDocs,
  createKnowledgeDoc,
  deleteKnowledgeDoc,
  getKnowledgeChunks,
  rebuildKnowledgeIndex,
  searchKnowledge
} from '@/api/ai/knowledge'

const loading = ref(false)
const submitting = ref(false)
const searching = ref(false)
const addDialogVisible = ref(false)
const chunkDialogVisible = ref(false)
const docList = ref([])
const total = ref(0)
const chunkList = ref([])
const searchResults = ref([])
const addFormRef = ref(null)

const queryForm = reactive({
  page: 1,
  size: 10,
  sourceType: '',
  keyword: ''
})

const addForm = reactive({
  title: '',
  sourceType: 'upload',
  sourceId: '',
  content: ''
})

const addRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  sourceType: [{ required: true, message: '请选择来源类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const searchForm = reactive({
  query: ''
})

onMounted(() => {
  loadDocs()
})

const loadDocs = async () => {
  loading.value = true
  try {
    const res = await listKnowledgeDocs(queryForm)
    docList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载文档失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.sourceType = ''
  queryForm.keyword = ''
  queryForm.page = 1
  loadDocs()
}

const openAddDialog = () => {
  addForm.title = ''
  addForm.sourceType = 'upload'
  addForm.sourceId = ''
  addForm.content = ''
  addDialogVisible.value = true
}

const submitAdd = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate()
  submitting.value = true
  try {
    await createKnowledgeDoc(addForm)
    ElMessage.success('新增文档成功')
    addDialogVisible.value = false
    loadDocs()
  } catch (e) {
    ElMessage.error('新增文档失败')
  } finally {
    submitting.value = false
  }
}

const deleteDoc = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该文档及其索引？', '提示', { type: 'warning' })
    await deleteKnowledgeDoc(row.id)
    ElMessage.success('删除成功')
    loadDocs()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const viewChunks = async (row) => {
  try {
    const res = await getKnowledgeChunks(row.id)
    chunkList.value = res.data || []
    chunkDialogVisible.value = true
  } catch (e) {
    ElMessage.error('加载分块失败')
  }
}

const rebuildIndex = async () => {
  try {
    await ElMessageBox.confirm('确定重建全部知识库索引？', '提示', { type: 'warning' })
    await rebuildKnowledgeIndex({})
    ElMessage.success('重建任务已启动')
    loadDocs()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('重建失败')
    }
  }
}

const testSearch = async () => {
  if (!searchForm.query) {
    ElMessage.warning('请输入查询文本')
    return
  }
  searching.value = true
  try {
    const res = await searchKnowledge({ query: searchForm.query })
    searchResults.value = res.data || []
  } catch (e) {
    ElMessage.error('检索失败')
  } finally {
    searching.value = false
  }
}
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 10px;
}
</style>
