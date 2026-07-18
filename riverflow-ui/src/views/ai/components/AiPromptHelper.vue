<template>
  <div class="ai-prompt-helper">
    <div class="helper-header">
      <div class="header-icon">
        <el-icon><MagicStick /></el-icon>
      </div>
      <div class="header-info">
        <div class="header-title">{{ config.title }}</div>
        <div class="header-desc">{{ config.desc }}</div>
      </div>
    </div>

    <div class="helper-body">
      <el-form label-position="top">
        <el-form-item :label="config.promptLabel">
          <el-input
            v-model="userPrompt"
            type="textarea"
            :rows="4"
            :placeholder="config.placeholder"
            clearable
          />
        </el-form-item>
      </el-form>

      <div class="ai-actions">
        <el-button type="primary" :loading="loading" @click="handleGenerate">
          <el-icon><MagicStick /></el-icon>{{ $t('aiPromptHelper._a_i生成_17') }}</el-button>
      </div>

      <el-divider v-if="result" content-position="left">{{ $t('aiPromptHelper.生成结果_18') }}</el-divider>

      <div v-if="result" class="ai-result">
        <pre>{{ formattedResult }}</pre>
      </div>

      <div v-if="explanation" class="ai-explanation">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>{{ explanation }}</template>
        </el-alert>
      </div>

      <div v-if="result" class="result-actions">
        <el-button type="primary" plain @click="copyResult">
          <el-icon><DocumentCopy /></el-icon>{{ $t('aiPromptHelper.复制结果_cde0ad30') }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, DocumentCopy } from '@element-plus/icons-vue'
import {
  aiGenerateCondition,
  aiGenerateScript,
  aiGenerateMapping
} from '@/api/ai'

const props = defineProps({
  scene: {
    type: String,
    default: 'condition'
  },
  context: {
    type: Object,
    default: () => ({})
  },
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

const SCENE_CONFIG = {
  condition: {
    title: t('aiPromptHelper._a_i条件表达式生_1'),
    desc: t('aiPromptHelper.将自然语言描述转_2'),
    promptLabel: t('aiPromptHelper.用自然语言描述分_3'),
    placeholder: t('aiPromptHelper.例如当上一个节点_4'),
    api: aiGenerateCondition
  },
  script: {
    title: t('aiPromptHelper._a_i_groovy_5'),
    desc: t('aiPromptHelper.将自然语言描述转_6'),
    promptLabel: t('aiPromptHelper.用自然语言描述脚_7'),
    placeholder: t('aiPromptHelper.例如从上下文中取_8'),
    api: aiGenerateScript
  },
  mapping: {
    title: t('aiPromptHelper._a_i数据映射推荐_9'),
    desc: t('aiPromptHelper.根据上下文智能推_10'),
    promptLabel: t('aiPromptHelper.描述映射需求_11'),
    placeholder: t('aiPromptHelper.例如将上游返回的_12'),
    api: aiGenerateMapping
  }
}

const config = computed(() => SCENE_CONFIG[props.scene] || SCENE_CONFIG.condition)

const userPrompt = ref('')
const loading = ref(false)
const result = ref(null)
const explanation = ref('')

const formattedResult = computed(() => {
  if (!result.value) return ''
  if (typeof result.value === 'string') return result.value
  return JSON.stringify(result.value, null, 2)
})

async function handleGenerate() {
  if (!userPrompt.value.trim()) {
    ElMessage.warning(t('aiPromptHelper.请输入描述_13'))
    return
  }
  loading.value = true
  try {
    const payload = buildRequestPayload()
    const res = await config.value.api(payload)
    result.value = res
    explanation.value = res?.explanation || ''
  } catch (err) {
    ElMessage.error(err.message || t('aiPromptHelper._a_i生成失败_14'))
  } finally {
    loading.value = false
  }
}

function buildRequestPayload() {
  const base = {
    userPrompt: userPrompt.value.trim(),
    contextVariables: props.context?.variables || [],
    provider: props.provider || undefined,
    model: props.model || undefined,
    promptVersion: props.promptVersion || undefined
  }
  if (props.scene === 'mapping') {
    return {
      ...base,
      direction: props.context?.direction || 'input',
      apiParams: props.context?.apiParams || [],
      sampleResponse: {}
    }
  }
  if (props.scene === 'script') {
    return {
      ...base,
      expectedOutput: props.context?.expectedOutput || ''
    }
  }
  return base
}

function copyResult() {
  if (!result.value) return
  navigator.clipboard.writeText(formattedResult.value).then(() => {
    ElMessage.success(t('aiPromptHelper.已复制结果_15'))
  }).catch(() => {
    ElMessage.error(t('aiPromptHelper.复制失败_16'))
  })
}
</script>

<style scoped>
.ai-prompt-helper {
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
.ai-result {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}
.ai-result pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 13px;
  color: #303133;
}
.ai-explanation {
  margin-bottom: 12px;
}
.result-actions {
  display: flex;
  gap: 10px;
}
</style>
