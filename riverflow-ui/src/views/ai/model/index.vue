<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">{{ $t('aiModel.AI模型管理_1') }}</h1>
        <p class="subtitle">{{ $t('aiModel.管理_LLM_Provider_与模型配置_a1b2c3d4') }}</p>
      </div>
      <div style="display: flex; gap: 12px;">
        <button class="btn-primary" @click="handleAdd"><el-icon><Plus /></el-icon>{{ $t('aiModel.新增模型_9') }}</button>
        <button class="btn-primary" style="background: linear-gradient(135deg, #10b981, #059669); box-shadow: 0 4px 14px rgba(16, 185, 129, 0.25);" @click="handleEmbeddingTest"><el-icon><Connection /></el-icon>Embedding 测试</button>
        <button class="btn-primary" style="background: linear-gradient(135deg, #f59e0b, #d97706); box-shadow: 0 4px 14px rgba(245, 158, 11, 0.25);" @click="handleReload"><el-icon><Refresh /></el-icon>{{ $t('aiModel.刷新运行时_10') }}</button>
      </div>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="query" inline>
          <el-form-item :label="$t('aiModel.模型编码_11')">
            <el-input
              v-model="query.keyword"
              :placeholder="$t('aiModel.请输入模型编码或名称_2')"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item :label="$t('aiModel.provider类型_3')">
            <el-select
              v-model="query.providerType"
              :placeholder="$t('aiModel.请选择_24')"
              clearable
              style="width: 150px"
            >
              <el-option label="OpenAI" value="openai" />
              <el-option label="Ollama" value="ollama" />
              <el-option label="Qwen" value="qwen" />
              <el-option label="Zhipu" value="zhipu" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('aiModel.状态_4')">
            <el-select
              v-model="query.status"
              :placeholder="$t('aiModel.请选择_24')"
              clearable
              style="width: 120px"
            >
              <el-option :label="$t('aiModel.启用_5')" :value="1" />
              <el-option :label="$t('aiModel.停用_6')" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch"><el-icon><Search /></el-icon>{{ $t('aiModel.查询_7') }}</button>
        <button class="btn-reset" @click="handleReset">{{ $t('aiModel.重置_8') }}</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableData" v-loading="loading" stripe class="rf-data-table" :empty-text="'暂无数据'">
        <el-table-column prop="modelCode" :label="$t('aiModel.模型编码_11')" width="140" />
        <el-table-column prop="modelName" :label="$t('aiModel.模型名称_12')" width="160" />
        <el-table-column prop="providerType" :label="$t('aiModel.provider类型_3')" width="120" />
        <el-table-column prop="providerName" :label="$t('aiModel.provider名称_13')" width="140" />
        <el-table-column prop="baseUrl" :label="$t('aiModel.基础URL_14')" show-overflow-tooltip />
        <el-table-column prop="apiKey" :label="$t('aiModel.API_Key_15')" width="160">
          <template #default="{ row }">
            <span>{{ row.apiKey || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('aiModel.状态_4')" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">{{ $t('aiModel.启用_5') }}</el-tag>
            <el-tag v-else type="info">{{ $t('aiModel.停用_6') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isDefault" :label="$t('aiModel.默认_16')" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault === 1" type="primary">{{ $t('aiModel.是_17') }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortNo" :label="$t('aiModel.排序_18')" width="80" />
        <el-table-column :label="$t('aiModel.操作_19')" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" :title="$t('aiModel.编辑_20')" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button class="action-btn danger" :title="$t('aiModel.删除_21')" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="rf-pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSearch"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="130px">
        <el-form-item :label="$t('aiModel.模型编码_11')" prop="modelCode">
          <el-input v-model="form.modelCode" :placeholder="$t('aiModel.唯一标识如_kimi_22')" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item :label="$t('aiModel.模型名称_12')" prop="modelName">
          <el-input v-model="form.modelName" :placeholder="$t('aiModel.显示名称如_moonshot_v1_8k_23')" />
        </el-form-item>
        <el-form-item :label="$t('aiModel.provider类型_3')" prop="providerType">
          <el-select v-model="form.providerType" :placeholder="$t('aiModel.请选择_24')" style="width: 100%">
            <el-option label="OpenAI" value="openai" />
            <el-option label="Ollama" value="ollama" />
            <el-option label="Qwen" value="qwen" />
            <el-option label="Zhipu" value="zhipu" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('aiModel.provider名称_13')" prop="providerName">
          <el-input v-model="form.providerName" :placeholder="$t('aiModel.用于前端分组展示_25')" />
        </el-form-item>
        <el-form-item :label="$t('aiModel.基础URL_14')" prop="baseUrl">
          <el-input v-model="form.baseUrl" :placeholder="$t('aiModel.如_https_api_moonshot_cn_v1_26')" />
        </el-form-item>
        <el-form-item :label="$t('aiModel.API_Key_15')" prop="apiKey">
          <el-input
            v-model="form.apiKey"
            type="password"
            :placeholder="isEdit ? $t('aiModel.不修改请留空_27') : $t('aiModel.请输入_API_Key_28')"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('aiModel.温度_29')" prop="temperature">
              <el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('aiModel.最大Token_30')" prop="maxTokens">
              <el-input-number v-model="form.maxTokens" :min="1" :max="100000" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('aiModel.超时毫秒_31')" prop="timeout">
          <el-input-number v-model="form.timeout" :min="1000" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('aiModel.默认模型_16')" prop="isDefault">
              <el-radio-group v-model="form.isDefault">
                <el-radio :label="1">{{ $t('aiModel.是_17') }}</el-radio>
                <el-radio :label="0">{{ $t('aiModel.否_32') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('aiModel.状态_4')" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">{{ $t('aiModel.启用_5') }}</el-radio>
                <el-radio :label="0">{{ $t('aiModel.停用_6') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('aiModel.排序_18')" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('aiModel.备注_33')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('aiModel.取消_34') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('aiModel.确定_35') }}</el-button>
      </template>
    </el-dialog>
    <!-- Embedding 连接测试弹窗 -->
    <el-dialog v-model="embeddingDialogVisible" title="Embedding 连接测试" width="560px" destroy-on-close>
      <el-form :model="embeddingForm" label-width="110px">
        <el-form-item label="Provider">
          <el-select v-model="embeddingForm.type" placeholder="请选择" style="width: 100%" @change="handleEmbeddingTypeChange">
            <el-option label="Ollama" value="ollama" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="Memory（测试）" value="memory" />
          </el-select>
        </el-form-item>
        <el-form-item label="Base URL">
          <el-input v-model="embeddingForm.baseUrl" placeholder="如 http://localhost:11434" />
        </el-form-item>
        <el-form-item v-if="embeddingForm.type !== 'ollama' && embeddingForm.type !== 'memory'" label="API Key">
          <el-input v-model="embeddingForm.apiKey" type="password" show-password placeholder="请输入 API Key" />
        </el-form-item>
        <el-form-item label="模型">
          <el-input v-model="embeddingForm.model" placeholder="如 nomic-embed-text" />
        </el-form-item>
        <el-form-item label="维度">
          <el-input-number v-model="embeddingForm.dimension" :min="1" :max="8192" style="width: 100%" />
        </el-form-item>
        <el-form-item label="超时(ms)">
          <el-input-number v-model="embeddingForm.timeout" :min="1000" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="测试文本">
          <el-input v-model="embeddingForm.text" type="textarea" :rows="2" placeholder="用于测试的文本" />
        </el-form-item>
      </el-form>
      <div v-if="embeddingResult" style="margin-top: 16px; padding: 12px; border-radius: 8px;" :style="{ background: embeddingResult.success ? '#d1fae5' : '#fee2e2', color: embeddingResult.success ? '#059669' : '#dc2626' }">
        <div style="font-weight: 600;">{{ embeddingResult.success ? '✅ 测试成功' : '❌ 测试失败' }}</div>
        <div>{{ embeddingResult.message }}</div>
        <div v-if="embeddingResult.success" style="margin-top: 4px; font-size: 12px;">
          模型：{{ embeddingResult.model }} | 维度：{{ embeddingResult.sampleDimension }} | 耗时：{{ embeddingResult.elapsedMs }}ms
        </div>
      </div>
      <template #footer>
        <el-button @click="embeddingDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="embeddingTesting" @click="handleSubmitEmbeddingTest">测试连接</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Cpu, Plus, Search, Refresh, Connection, Edit, Delete } from '@element-plus/icons-vue'
import {
  getAiModelList,
  saveAiModel,
  updateAiModel,
  deleteAiModel,
  reloadAiModel,
  getAiModelById
} from '@/api/aiModel'
import { testEmbeddingConfig } from '@/api/ai/vector'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const submitLoading = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const total = ref(0)

const embeddingDialogVisible = ref(false)
const embeddingTesting = ref(false)
const embeddingResult = ref(null)
const embeddingForm = reactive({
  type: 'ollama',
  baseUrl: 'http://localhost:11434',
  apiKey: '',
  model: 'nomic-embed-text',
  dimension: 768,
  timeout: 30000,
  text: 'RiverFlow 是一个流程编排平台'
})

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  providerType: '',
  status: ''
})

const form = reactive({
  id: null,
  modelCode: '',
  modelName: '',
  providerType: 'openai',
  providerName: '',
  baseUrl: '',
  apiKey: '',
  temperature: 0.2,
  maxTokens: 4096,
  timeout: 30000,
  isDefault: 0,
  status: 1,
  sortNo: 0,
  remark: ''
})

const apiKeyRequired = computed(() => form.providerType !== 'ollama' && !isEdit.value)
const formRules = computed(() => ({
  modelCode: [{ required: true, message: t('aiModel.请输入模型编码_36'), trigger: 'blur' }],
  modelName: [{ required: true, message: t('aiModel.请输入模型名称_37'), trigger: 'blur' }],
  providerType: [{ required: true, message: t('aiModel.请选择provider类型_38'), trigger: 'change' }],
  providerName: [{ required: true, message: t('aiModel.请输入provider名称_39'), trigger: 'blur' }],
  baseUrl: [{ required: true, message: t('aiModel.请输入基础URL_40'), trigger: 'blur' }],
  apiKey: apiKeyRequired.value ? [{ required: true, message: t('aiModel.请输入APIKey_41'), trigger: 'blur' }] : []
}))

onMounted(() => {
  loadData()
})

function handleSearch() {
  query.page = 1
  loadData()
}

function handleReset() {
  query.keyword = ''
  query.providerType = ''
  query.status = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAiModelList({
      page: query.page,
      size: query.size,
      keyword: query.keyword,
      providerType: query.providerType,
      status: query.status
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (err) {
    ElMessage.error(err.message || t('aiModel.加载失败_42'))
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = null
  form.modelCode = ''
  form.modelName = ''
  form.providerType = 'openai'
  form.providerName = ''
  form.baseUrl = ''
  form.apiKey = ''
  form.temperature = 0.2
  form.maxTokens = 4096
  form.timeout = 30000
  form.isDefault = 0
  form.status = 1
  form.sortNo = 0
  form.remark = ''
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogTitle.value = t('aiModel.新增模型_9')
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  resetForm()
  dialogTitle.value = t('aiModel.编辑模型_43')
  try {
    const res = await getAiModelById(row.id)
    Object.assign(form, res)
    dialogVisible.value = true
  } catch (err) {
    ElMessage.error(err.message || t('aiModel.加载详情失败_44'))
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(
    t('aiModel.确认删除模型_45', { name: row.modelName }),
    t('aiModel.提示_46'),
    { confirmButtonText: t('aiModel.确定_35'), cancelButtonText: t('aiModel.取消_34'), type: 'warning' }
  ).then(async () => {
    await deleteAiModel(row.id)
    ElMessage.success(t('aiModel.删除成功_47'))
    loadData()
  }).catch(() => {})
}

async function handleReload() {
  try {
    await reloadAiModel()
    ElMessage.success(t('aiModel.刷新成功_48'))
  } catch (err) {
    ElMessage.error(err.message || t('aiModel.刷新失败_49'))
  }
}

function handleEmbeddingTypeChange(type) {
  if (type === 'ollama') {
    embeddingForm.baseUrl = 'http://localhost:11434'
    embeddingForm.model = 'nomic-embed-text'
    embeddingForm.apiKey = ''
  } else if (type === 'openai') {
    embeddingForm.baseUrl = 'https://api.openai.com/v1'
    embeddingForm.model = 'text-embedding-3-small'
  } else if (type === 'memory') {
    embeddingForm.baseUrl = ''
    embeddingForm.apiKey = ''
    embeddingForm.model = ''
  }
}

function handleEmbeddingTest() {
  embeddingResult.value = null
  embeddingForm.type = 'ollama'
  handleEmbeddingTypeChange('ollama')
  embeddingDialogVisible.value = true
}

async function handleSubmitEmbeddingTest() {
  embeddingTesting.value = true
  embeddingResult.value = null
  try {
    const res = await testEmbeddingConfig({ ...embeddingForm })
    embeddingResult.value = res
    if (res && res.success) {
      ElMessage.success('Embedding 连接测试成功')
    }
  } catch (err) {
    ElMessage.error(err.message || '测试失败')
  } finally {
    embeddingTesting.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      await updateAiModel(data)
    } else {
      await saveAiModel(data)
    }
    ElMessage.success(isEdit.value ? t('aiModel.修改成功_50') : t('aiModel.新增成功_51'))
    dialogVisible.value = false
    loadData()
  } catch (err) {
    ElMessage.error(err.message || t('aiModel.提交失败_52'))
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
</style>
