<template>
  <div class="ai-assistant-page">
    <div class="ai-sidebar">
      <div class="sidebar-header">
        <el-icon class="header-icon"><MagicStick /></el-icon>
        <span>{{ $t('ai._a_i助手_9') }}</span>
      </div>
      <div class="menu-list">
        <div
          v-for="menu in menuList"
          :key="menu.key"
          :class="['menu-item', { active: activeMenu === menu.key }]"
          @click="activeMenu = menu.key"
        >
          <el-icon><component :is="menu.icon" /></el-icon>
          <span>{{ menu.label }}</span>
        </div>
      </div>

      <div class="model-selector">
        <div class="selector-title">{{ $t('ai.模型选择_10') }}</div>
        <el-select
          v-model="selectedProvider"
          :placeholder="$t('ai.选择_provider_11')"
          clearable
          size="small"
          class="provider-select"
          @change="handleProviderChange"
        >
          <el-option
            v-for="p in providerList"
            :key="p.modelCode"
            :label="`${p.modelName} (${p.providerType})`"
            :value="p.modelCode"
          />
        </el-select>
        <el-input
          v-model="selectedModel"
          :placeholder="$t('ai.自定义模型_12')"
          size="small"
          clearable
          class="model-input"
        />
      </div>
    </div>

    <div class="ai-content">
      <AiChatPanel v-if="activeMenu === 'chat'" :scene="chatScene" :context="chatContext" :provider="selectedProvider" :model="selectedModel" />
      <AiFlowHelper v-else-if="activeMenu === 'flow'" :provider="selectedProvider" :model="selectedModel" />
      <AiPromptHelper v-else-if="['condition', 'script', 'mapping'].includes(activeMenu)" :scene="activeMenu" :context="chatContext" :provider="selectedProvider" :model="selectedModel" />
      <AiApiParser v-else-if="activeMenu === 'api-doc'" :provider="selectedProvider" :model="selectedModel" />
      <AiStatsPanel v-else-if="activeMenu === 'stats'" />
      <AiAuditLogPanel v-else-if="activeMenu === 'audit'" />
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  MagicStick,
  ChatRound,
  Share,
  QuestionFilled,
  DocumentCopy,
  Connection,
  Document,
  DataLine,
  List
} from '@element-plus/icons-vue'
import AiChatPanel from './components/AiChatPanel.vue'
import AiFlowHelper from './components/AiFlowHelper.vue'
import AiPromptHelper from './components/AiPromptHelper.vue'
import AiApiParser from './components/AiApiParser.vue'
import AiStatsPanel from './components/AiStatsPanel.vue'
import AiAuditLogPanel from './components/AiAuditLogPanel.vue'
import { getAiModelOptions } from '@/api/aiModel'
import { aiGetProviders } from '@/api/ai'

const route = useRoute()

const menuList = [
  { key: 'chat', label: t('ai.智能对话_1'), icon: 'ChatRound' },
  { key: 'flow', label: t('ai.流程生成_2'), icon: 'Share' },
  { key: 'condition', label: t('ai.条件生成_3'), icon: 'QuestionFilled' },
  { key: 'script', label: t('ai.脚本生成_4'), icon: 'DocumentCopy' },
  { key: 'mapping', label: t('ai.映射推荐_5'), icon: 'Connection' },
  { key: 'api-doc', label: t('ai.接口文档解析_6'), icon: 'Document' },
  { key: 'stats', label: t('ai.调用统计_7'), icon: 'DataLine' },
  { key: 'audit', label: t('ai.审计日志_8'), icon: 'List' }
]

const activeMenu = ref('chat')

const chatScene = computed(() => {
  const scene = route.query.scene
  return ['flow', 'condition', 'script', 'mapping', 'api-doc', 'chat'].includes(scene) ? scene : 'chat'
})

const chatContext = computed(() => {
  try {
    const ctx = route.query.context
    return ctx ? JSON.parse(decodeURIComponent(ctx)) : {}
  } catch (e) {
    return {}
  }
})

const providerList = ref([])
const selectedProvider = ref('')
const selectedModel = ref('')

async function loadProviders() {
  try {
    // 优先从模型管理接口读取（支持 DB 配置）
    const res = await getAiModelOptions()
    providerList.value = (res || []).map(m => ({
      modelCode: m.modelCode,
      modelName: m.modelName,
      providerType: m.providerType
    }))
    setDefaultProvider()
    return
  } catch (err) {
    console.warn('模型管理接口加载失败，回退到 AI 服务配置: ', err)
  }
  try {
    // 回退到 YAML 配置的 provider 列表
    const res = await aiGetProviders()
    providerList.value = (res || []).map(p => ({
      modelCode: p.name,
      modelName: p.defaultModel,
      providerType: p.type
    }))
    setDefaultProvider()
  } catch (err) {
    ElMessage.warning(err.message || t('ai.加载模型列表失败_13'))
  }
}

function setDefaultProvider() {
  if (!selectedProvider.value && providerList.value.length > 0) {
    const first = providerList.value[0]
    selectedProvider.value = first.modelCode || ''
    selectedModel.value = first.modelName || ''
  }
}

function handleProviderChange(providerCode) {
  const p = providerList.value.find(item => item.modelCode === providerCode)
  selectedModel.value = p ? (p.modelName || '') : ''
}

onMounted(() => {
  const scene = route.query.scene
  if (scene && menuList.some(m => m.key === scene)) {
    activeMenu.value = scene
  }
  loadProviders()
})
</script>

<style scoped>
.ai-assistant-page {
  display: flex;
  height: 100%;
  background: #f5f7fa;
  padding: 16px;
  gap: 16px;
  box-sizing: border-box;
}
.ai-sidebar {
  width: 220px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.header-icon {
  color: #409eff;
  font-size: 20px;
}
.menu-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  transition: all 0.2s;
}
.menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
}
.menu-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}
.model-selector {
  padding: 12px;
  border-top: 1px solid #e4e7ed;
  background: #fafafa;
}
.selector-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.provider-select,
.model-input {
  width: 100%;
}
.model-input {
  margin-top: 8px;
}
.ai-content {
  flex: 1;
  overflow: hidden;
  min-width: 0;
}
</style>
