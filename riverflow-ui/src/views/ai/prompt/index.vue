<template>
  <div class="rf-list-page">
    <div class="rf-list-header">
      <div>
        <h1 class="title">AI Prompt 管理</h1>
        <p class="subtitle">管理各场景的 Prompt 模板、系统 Prompt、Few-shot 示例与输出 Schema</p>
      </div>
      <div style="display: flex; gap: 12px;">
        <button class="btn-primary" style="background: linear-gradient(135deg, #10b981, #059669); box-shadow: 0 4px 14px rgba(16, 185, 129, 0.25);" @click="helpDialogVisible = true"><el-icon><QuestionFilled /></el-icon>使用说明</button>
        <button class="btn-primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增 Prompt</button>
        <button class="btn-primary" style="background: linear-gradient(135deg, #f59e0b, #d97706); box-shadow: 0 4px 14px rgba(245, 158, 11, 0.25);" @click="handleRefreshAll"><el-icon><Refresh /></el-icon>刷新全部缓存</button>
      </div>
    </div>

    <div class="rf-search-bar">
      <div class="search-fields">
        <el-form :model="query" inline>
          <el-form-item label="场景">
            <el-select v-model="query.scene" placeholder="请选择场景" clearable style="width: 180px" @change="handleSearch">
              <el-option v-for="s in sceneOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型">
            <el-input v-model="query.model" placeholder="请输入模型" clearable style="width: 150px" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="场景/描述" clearable style="width: 200px" @keyup.enter="handleSearch" />
          </el-form-item>
        </el-form>
      </div>
      <div class="search-actions">
        <button class="btn-search" @click="handleSearch"><el-icon><Search /></el-icon>查询</button>
        <button class="btn-reset" @click="handleReset">重置</button>
      </div>
    </div>

    <div class="rf-table-card">
      <el-table :data="tableData" v-loading="loading" stripe class="rf-data-table" :empty-text="'暂无数据'">
        <el-table-column prop="scene" label="场景" width="160" />
        <el-table-column prop="model" label="模型" width="140" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.enabled === 1" type="success">启用</el-tag>
            <el-tag v-else type="info">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortNo" label="排序" width="80" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <div class="rf-actions">
              <button class="action-btn primary" title="编辑" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </button>
              <button class="action-btn warning" title="刷新缓存" @click="handleRefresh(row)">
                <el-icon><Refresh /></el-icon>
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

    <!-- Prompt 版本 A/B 统计 -->
    <div class="rf-table-card" style="margin-top: 20px;">
      <div class="rf-list-header" style="margin-bottom: 16px;">
        <div>
          <h2 class="title" style="font-size: 18px;">Prompt 版本 A/B 统计</h2>
          <p class="subtitle">按 Prompt 版本统计调用成功率，用于评估不同版本效果</p>
        </div>
        <div style="display: flex; gap: 12px;">
          <el-date-picker
            v-model="statsDateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="loadStats"
          />
          <button class="btn-primary" @click="loadStats"><el-icon><Search /></el-icon>查询</button>
        </div>
      </div>
      <el-table :data="statsData" v-loading="statsLoading" stripe class="rf-data-table">
        <el-table-column prop="promptVersion" label="Prompt 版本" width="220" />
        <el-table-column prop="scene" label="场景" width="120" />
        <el-table-column prop="model" label="模型" width="120" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="totalCount" label="总调用" width="100" />
        <el-table-column prop="successCount" label="成功" width="100" />
        <el-table-column prop="failCount" label="失败" width="100" />
        <el-table-column prop="successRate" label="成功率" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.successRate" :status="row.successRate >= 80 ? 'success' : row.successRate >= 50 ? '' : 'exception'" />
          </template>
        </el-table-column>
        <el-table-column prop="avgResponseTimeMs" label="平均耗时(ms)" width="140" />
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="场景" prop="scene">
              <el-input v-model="form.scene" placeholder="flow-generation" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模型" prop="model">
              <el-input v-model="form.model" placeholder="default" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="版本" prop="version">
              <el-input v-model="form.version" placeholder="v1" :disabled="isEdit" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" placeholder="Prompt 用途描述" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态" prop="enabled">
              <el-radio-group v-model="form.enabled">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortNo">
              <el-input-number v-model="form.sortNo" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="系统 Prompt" prop="systemPrompt">
          <el-input v-model="form.systemPrompt" type="textarea" :rows="4" placeholder="定义 AI 角色和任务目标的系统 Prompt，为空时使用默认系统 Prompt" />
        </el-form-item>
        <el-form-item label="用户模板" prop="template">
          <el-input v-model="form.template" type="textarea" :rows="10" placeholder="用户 Prompt 模板，支持 ${变量名} 和 #{SpEL} 占位符" />
        </el-form-item>
        <el-form-item label="Few-shot" prop="examples">
          <el-input v-model="form.examples" type="textarea" :rows="4" placeholder='JSON 数组格式，例如 [{"input":"...","output":"..."}]' />
        </el-form-item>
        <el-form-item label="输出 Schema" prop="outputSchema">
          <el-input v-model="form.outputSchema" type="textarea" :rows="6" placeholder='JSON Schema 字符串，用于校验 LLM 输出' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 使用说明弹窗 -->
    <el-dialog v-model="helpDialogVisible" title="AI Prompt 使用说明" width="720px" destroy-on-close>
      <div class="prompt-help-content">
        <h4>一、Prompt 匹配规则</h4>
        <p>系统通过 <strong>scene + model + version</strong> 三个字段唯一匹配一条 Prompt：</p>
        <ul>
          <li><strong>scene</strong>：功能场景，代码中固定，如 <code>flow-generation</code></li>
          <li><strong>model</strong>：模型标识，来自 AI 助手页面选择或请求传入</li>
          <li><strong>version</strong>：Prompt 版本，AI 助手页面可选择</li>
        </ul>
        <p>优先级：<strong>数据库 &gt; classpath 默认文件 &gt; 内存缓存</strong></p>

        <h4>二、Scene 与功能对应表</h4>
        <el-table :data="helpSceneList" size="small" border>
          <el-table-column prop="scene" label="Scene" width="180" />
          <el-table-column prop="function" label="功能入口" />
        </el-table>

        <h4>三、快速使用步骤</h4>
        <ol>
          <li>点击「新增 Prompt」</li>
          <li>选择对应 scene、model、version（通常用 <code>default</code> / <code>v1</code>）</li>
          <li>填写系统 Prompt、用户模板、输出 JSON Schema</li>
          <li>保存后点击「刷新全部缓存」</li>
          <li>进入 <strong>AI 助手</strong> 对应功能，选择相同 model 和 version，测试生成效果</li>
        </ol>

        <h4>四、模板可用变量</h4>
        <p>不同 scene 可用变量不同，常见变量包括：</p>
        <ul>
          <li><code>${userPrompt}</code>：用户输入的自然语言描述</li>
          <li><code>${outputSchema}</code>：输出 JSON Schema</li>
          <li><code>${examples}</code>：Few-shot 示例 JSON 字符串</li>
        </ul>
        <p>具体变量请查看各生成服务的 <code>buildPromptVariables</code> 方法。</p>

        <h4>五、A/B 测试</h4>
        <p>在 AI 助手页面切换「Prompt 版本」，可对比不同版本 Prompt 的生成效果。审计日志和本页面下方的「Prompt 版本 A/B 统计」会记录各版本成功率。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, QuestionFilled } from '@element-plus/icons-vue'
import {
  getAiPromptList,
  saveAiPrompt,
  updateAiPrompt,
  deleteAiPrompt,
  refreshAiPrompt,
  refreshAllAiPrompt,
  getAiPromptById,
  getAiPromptScenes,
  getAiPromptStats
} from '@/api/aiPrompt'

const loading = ref(false)
const statsLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = computed(() => !!form.id)
const sceneOptions = ref([])
const statsDateRange = ref([])
const statsData = ref([])
const helpDialogVisible = ref(false)
const helpSceneList = [
  { scene: 'flow-generation', function: 'AI 助手 → 自然语言生成流程' },
  { scene: 'condition-generation', function: 'AI 助手 → 生成条件表达式' },
  { scene: 'script-generation', function: 'AI 助手 → 生成 Groovy 脚本' },
  { scene: 'mapping-recommendation', function: 'AI 助手 → 智能推荐映射' },
  { scene: 'api-doc-parse', function: 'AI 助手 → 接口文档解析' }
]

const query = reactive({
  page: 1,
  size: 10,
  scene: '',
  model: '',
  keyword: ''
})

const form = reactive({
  id: null,
  scene: '',
  model: 'default',
  version: 'v1',
  template: '',
  systemPrompt: '',
  examples: '',
  outputSchema: '',
  description: '',
  enabled: 1,
  sortNo: 0
})

const tableData = ref([])
const total = ref(0)

const formRules = {
  scene: [{ required: true, message: '场景不能为空', trigger: 'blur' }],
  model: [{ required: true, message: '模型不能为空', trigger: 'blur' }],
  version: [{ required: true, message: '版本不能为空', trigger: 'blur' }],
  template: [{ required: true, message: '用户模板不能为空', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    scene: '',
    model: 'default',
    version: 'v1',
    template: '',
    systemPrompt: '',
    examples: '',
    outputSchema: '',
    description: '',
    enabled: 1,
    sortNo: 0
  })
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAiPromptList(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const loadScenes = async () => {
  try {
    const res = await getAiPromptScenes()
    sceneOptions.value = res || []
  } catch (e) {
    // 忽略错误，下拉框为空时用户可手动输入
  }
}

const handleSearch = () => {
  query.page = 1
  loadData()
}

const handleReset = () => {
  query.page = 1
  query.size = 10
  query.scene = ''
  query.model = ''
  query.keyword = ''
  loadData()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增 Prompt'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  dialogTitle.value = '编辑 Prompt'
  try {
    const res = await getAiPromptById(row.id)
    Object.assign(form, res)
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (form.id) {
      await updateAiPrompt(form)
    } else {
      await saveAiPrompt(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除 [${row.scene} / ${row.model} / ${row.version}] 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteAiPrompt(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleRefresh = async (row) => {
  await refreshAiPrompt(row.scene)
  ElMessage.success(`已刷新 [${row.scene}] 缓存`)
}

const handleRefreshAll = async () => {
  await refreshAllAiPrompt()
  ElMessage.success('已刷新全部缓存')
}

const loadStats = async () => {
  statsLoading.value = true
  try {
    const params = {}
    if (query.scene) params.scene = query.scene
    if (statsDateRange.value && statsDateRange.value.length === 2) {
      params.startTime = statsDateRange.value[0]
      params.endTime = statsDateRange.value[1]
    }
    const res = await getAiPromptStats(params)
    statsData.value = res || []
  } finally {
    statsLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadScenes()
  loadStats()
})
</script>

<style scoped>
.prompt-help-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
  line-height: 1.7;
  color: #303133;
}
.prompt-help-content h4 {
  margin: 16px 0 8px;
  color: #409eff;
  font-size: 15px;
}
.prompt-help-content h4:first-child {
  margin-top: 0;
}
.prompt-help-content p {
  margin: 8px 0;
}
.prompt-help-content ul,
.prompt-help-content ol {
  margin: 8px 0;
  padding-left: 20px;
}
.prompt-help-content li {
  margin: 4px 0;
}
.prompt-help-content code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', Consolas, monospace;
  color: #409eff;
}
</style>
