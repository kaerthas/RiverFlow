<template>
  <div class="ai-flow-helper">
    <div class="helper-header">
      <div class="header-icon">
        <el-icon><Share /></el-icon>
      </div>
      <div class="header-info">
        <div class="header-title">{{ $t('aiFlowHelper._a_i流程生成_11') }}</div>
        <div class="header-desc">{{ $t('aiFlowHelper.用自然语言描述业_12') }}</div>
      </div>
    </div>

    <div class="helper-body">
      <el-form label-position="top">
        <el-form-item :label="$t('aiFlowHelper.用自然语言描述你_5')">
          <el-input
            v-model="userPrompt"
            type="textarea"
            :rows="4"
            :placeholder="$t('aiFlowHelper.例如每天早上8点_6')"
            clearable
          />
        </el-form-item>
      </el-form>

      <div class="ai-actions">
        <el-button type="primary" :loading="loading" @click="handleGenerate">
          <el-icon><MagicStick /></el-icon>{{ loading ? 'AI 生成中...' : $t('aiFlowHelper.生成流程草稿_13') }}</el-button>
      </div>

      <div v-if="loading" class="ai-progress">
        <el-icon class="progress-icon is-loading"><Loading /></el-icon>
        <div class="progress-text">
          <div class="progress-title">{{ currentStep }}</div>
          <div class="progress-time">已用时 {{ elapsedTime }} 秒</div>
        </div>
      </div>

      <div v-if="thinkingContent" class="ai-thinking-stream">
        <div class="thinking-title">AI 思考过程</div>
        <pre>{{ thinkingContent }}</pre>
      </div>

      <template v-if="result">
        <el-divider content-position="left">{{ $t('aiFlowHelper.流程概览_14') }}</el-divider>

        <el-descriptions :column="2" border>
          <el-descriptions-item :label="$t('aiFlowHelper.流程名称_7')">{{ result.flowName }}</el-descriptions-item>
          <el-descriptions-item :label="$t('aiFlowHelper.触发方式_8')">{{ result.triggerType }}</el-descriptions-item>
          <el-descriptions-item :label="$t('aiFlowHelper.节点数_9')">{{ result.nodes?.length || 0 }}</el-descriptions-item>
          <el-descriptions-item :label="$t('aiFlowHelper.边数_10')">{{ result.edges?.length || 0 }}</el-descriptions-item>
        </el-descriptions>

        <el-alert v-if="result.description" class="ai-desc" type="info" :closable="false" show-icon>
          <template #title>{{ result.description }}</template>
        </el-alert>

        <div class="result-actions">
          <el-button type="primary" plain @click="copyJson">
            <el-icon><DocumentCopy /></el-icon>{{ $t('aiFlowHelper.复制_ffe38355') }}</el-button>
          <el-button type="primary" @click="openInDesigner">
            <el-icon><Share /></el-icon>{{ $t('aiFlowHelper.去设计器导入_cd38e361') }}</el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick, Share, DocumentCopy, Loading } from '@element-plus/icons-vue'
import { aiGenerateFlowStream } from '@/api/ai'

const router = useRouter()
const props = defineProps({
  provider: {
    type: String,
    default: ''
  },
  model: {
    type: String,
    default: ''
  },
  promptVersion: {
    type: String,
    default: 'v1'
  }
})
const userPrompt = ref('')
const loading = ref(false)
const result = ref(null)
const thinkingContent = ref('')
const elapsedTime = ref(0)
const currentStep = ref('')
let timer = null

function startProgress() {
  elapsedTime.value = 0
  currentStep.value = '正在理解需求并设计流程节点...'
  timer = setInterval(() => {
    elapsedTime.value++
    if (elapsedTime.value === 5) {
      currentStep.value = '正在生成流程定义 JSON...'
    } else if (elapsedTime.value === 15) {
      currentStep.value = '模型推理中，请耐心等待...'
    } else if (elapsedTime.value === 30) {
      currentStep.value = '当前模型加载较慢，仍在努力生成中...'
    }
  }, 1000)
}

function stopProgress() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

onUnmounted(() => {
  stopProgress()
})

function handleGenerate() {
  if (!userPrompt.value.trim()) {
    ElMessage.warning(t('aiFlowHelper.请输入流程描述_1'))
    return
  }
  loading.value = true
  thinkingContent.value = ''
  result.value = null
  startProgress()
  aiGenerateFlowStream(
    {
      userPrompt: userPrompt.value.trim(),
      provider: props.provider || undefined,
      model: props.model || undefined,
      promptVersion: props.promptVersion || undefined
    },
    (text) => {
      thinkingContent.value += text
    },
    (res) => {
      result.value = res
      // 流式过程中若未收集到思考内容，用后端返回的 thinking 兜底
      if (!thinkingContent.value && res.thinking) {
        thinkingContent.value = res.thinking
      }
      ElMessage.success('流程生成完成')
    },
    (err) => {
      ElMessage.error(err.message || t('aiFlowHelper.流程生成失败_2'))
    },
    () => {
      loading.value = false
      stopProgress()
    }
  )
}

function copyJson() {
  if (!result.value) return
  navigator.clipboard.writeText(JSON.stringify(result.value, null, 2)).then(() => {
    ElMessage.success(t('aiFlowHelper.已复制_j_s_o_n_3'))
  }).catch(() => {
    ElMessage.error(t('aiFlowHelper.复制失败_4'))
  })
}

function openInDesigner() {
  if (!result.value) return
  router.push({
    path: '/workflow/designer',
    query: {
      aiFlow: encodeURIComponent(JSON.stringify(result.value))
    }
  })
}
</script>

<style scoped>
.ai-flow-helper {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.helper-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}
.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.header-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.ai-actions {
  text-align: right;
  margin-bottom: 12px;
}
.ai-desc {
  margin-top: 12px;
}
.result-actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}
.ai-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  margin-top: 12px;
}
.progress-icon {
  font-size: 24px;
  color: #409eff;
}
.progress-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.progress-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.ai-thinking {
  margin-bottom: 12px;
}
.ai-thinking pre {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}
.ai-thinking-stream {
  margin-top: 12px;
  margin-bottom: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}
.ai-thinking-stream .thinking-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.ai-thinking-stream pre {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
  margin: 0;
}
</style>
