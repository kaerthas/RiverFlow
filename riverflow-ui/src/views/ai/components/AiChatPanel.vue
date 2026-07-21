<template>
  <div class="ai-chat-panel">
    <div class="chat-header">
      <div class="header-icon">
        <el-icon><MagicStick /></el-icon>
      </div>
      <div class="header-info">
        <div class="header-title">{{ $t('aiChatPanel._a_i智能对话_26') }}</div>
        <div class="header-desc">{{ sceneDesc }}</div>
      </div>
    </div>

    <div class="chat-body">
      <div class="chat-messages" ref="chatMessagesRef">
        <div v-for="(msg, idx) in messages" :key="idx" :class="['chat-message', msg.role, { error: msg.isError }]">
          <div class="message-avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
          <div :class="['message-content', { error: msg.isError }]">
            <span v-if="msg.isError" class="error-tag">{{ $t('aiChatPanel.调用失败_26') }}</span>
            {{ msg.content }}
            <div v-if="msg.role === 'assistant' && !msg.isError && msg.references && msg.references.length > 0" class="message-references">
              <div class="references-title">{{ $t('aiChatPanel.引用知识_30') }}</div>
              <div v-for="(ref, rIdx) in msg.references" :key="rIdx" class="reference-item">
                <span class="reference-index">[{{ rIdx + 1 }}]</span>
                <span class="reference-type">{{ ref.sourceType }}</span>
                <span class="reference-title">{{ ref.title }}</span>
                <span v-if="ref.score !== undefined" class="reference-score">{{ formatScore(ref.score) }}</span>
              </div>
            </div>
            <div v-if="msg.role === 'assistant' && !msg.isError" class="message-actions">
              <el-icon class="msg-action" @click="copyMessage(msg.content)" :title="$t('aiChatPanel.复制_24')"><DocumentCopy /></el-icon>
              <el-icon class="msg-action" @click="regenerate(idx)" :title="$t('aiChatPanel.重新生成_25')"><RefreshRight /></el-icon>
            </div>
          </div>
        </div>
        <div v-if="streaming" class="chat-message assistant">
          <div class="message-avatar">AI</div>
          <div class="message-content streaming">{{ streamingContent }}<span class="cursor">|</span></div>
        </div>
      </div>

      <div class="chat-input-area">
        <div class="input-toolbar">
          <span class="toolbar-label">{{ $t('aiChatPanel.知识库集合_31') }}</span>
          <el-select v-model="selectedCollectionId" placeholder="默认集合" clearable size="small" class="collection-select">
            <el-option
              v-for="item in vectorCollections"
              :key="item.id"
              :label="item.collection"
              :value="item.id"
            />
          </el-select>
        </div>
        <el-input
          v-model="chatInput"
          type="textarea"
          :rows="3"
          :placeholder="inputPlaceholder"
          @keydown.enter.prevent="handleSend"
        />
        <div class="input-actions">
          <el-button link type="primary" size="small" @click="clearHistory">
            <el-icon><Delete /></el-icon>{{ $t('aiChatPanel.清空对话_27') }}</el-button>
          <el-button type="primary" :loading="sending" :disabled="!chatInput.trim()" @click="handleSend">
            <el-icon><Promotion /></el-icon>{{ $t('aiChatPanel.发送_28') }}</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, nextTick, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, DocumentCopy, RefreshRight, Promotion, Delete } from '@element-plus/icons-vue'
import { aiChatStream } from '@/api/ai'
import { listAllVectorCollections } from '@/api/ai/vector'

const props = defineProps({
  scene: {
    type: String,
    default: 'chat'
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
  }
})

const SCENE_CONFIG = {
  chat: {
    desc: t('aiChatPanel.通用问题咨询流程_1'),
    placeholder: t('aiChatPanel.输入问题例如如何_2'),
    welcome: t('aiChatPanel.你好我是_rive_3')
  },
  flow: {
    desc: t('aiChatPanel.通过自然语言生成_4'),
    placeholder: t('aiChatPanel.描述你要生成的流_5'),
    welcome: t('aiChatPanel.已切换到流程生成_6')
  },
  condition: {
    desc: t('aiChatPanel.将自然语言转换为_7'),
    placeholder: t('aiChatPanel.描述条件例如当上_8'),
    welcome: t('aiChatPanel.已切换到条件生成_9')
  },
  script: {
    desc: t('aiChatPanel.将自然语言转换为_10'),
    placeholder: t('aiChatPanel.描述脚本逻辑例如_11'),
    welcome: t('aiChatPanel.已切换到脚本生成_12')
  },
  mapping: {
    desc: t('aiChatPanel.智能推荐_a_p_i参_13'),
    placeholder: t('aiChatPanel.描述映射需求例如_14'),
    welcome: t('aiChatPanel.已切换到映射推荐_15')
  },
  'api-doc': {
    desc: t('aiChatPanel.解析接口文档并自_16'),
    placeholder: t('aiChatPanel.粘贴_open_a_p_17'),
    welcome: t('aiChatPanel.已切换到接口文档_18')
  }
}

const sceneConfig = computed(() => SCENE_CONFIG[props.scene] || SCENE_CONFIG.chat)
const sceneDesc = computed(() => sceneConfig.value.desc)
const inputPlaceholder = computed(() => sceneConfig.value.placeholder)
const welcomeMessage = computed(() => sceneConfig.value.welcome)

const STORAGE_KEY = computed(() => `riverflow_ai_chat_history_${props.scene}`)
const messages = ref([
  { role: 'assistant', content: welcomeMessage.value }
])

onMounted(() => {
  loadHistory()
  loadVectorCollections()
})

const loadVectorCollections = async () => {
  try {
    const res = await listAllVectorCollections()
    vectorCollections.value = res || []
  } catch (e) {
    console.error('加载向量集合失败', e)
  }
}

watch(welcomeMessage, (newVal) => {
  if (messages.value.length === 1 && messages.value[0].role === 'assistant') {
    messages.value[0].content = newVal
  }
})

watch(messages, () => {
  saveHistory()
}, { deep: true })

function loadHistory() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY.value)
    if (stored) {
      const parsed = JSON.parse(stored)
      if (Array.isArray(parsed) && parsed.length > 0) {
        messages.value = parsed
      }
    }
  } catch (e) {
    console.warn(t('aiChatPanel.读取_a_i对话历史_19'), e)
  }
}

function saveHistory() {
  try {
    localStorage.setItem(STORAGE_KEY.value, JSON.stringify(messages.value))
  } catch (e) {
    console.warn(t('aiChatPanel.保存_a_i对话历史_20'), e)
  }
}

function clearHistory() {
  messages.value = [{ role: 'assistant', content: welcomeMessage.value }]
  localStorage.removeItem(STORAGE_KEY.value)
}

const chatInput = ref('')
const sending = ref(false)
const streaming = ref(false)
const streamingContent = ref('')
const streamingReferences = ref([])
const chatMessagesRef = ref(null)
const vectorCollections = ref([])
const selectedCollectionId = ref(null)

function handleSend() {
  const text = chatInput.value.trim()
  if (!text || sending.value) return
  sendMessage(text)
}

function sendMessage(text, replaceIndex = -1) {
  if (replaceIndex === -1) {
    messages.value.push({ role: 'user', content: text })
  }
  sending.value = true
  streaming.value = true
  streamingContent.value = ''
  chatInput.value = ''
  scrollToBottom()

  const history = messages.value
    .filter(m => m.role === 'user' || m.role === 'assistant')
    .slice(-6)
    .map(m => m.content)
    .join('\n')

  aiChatStream(
    {
      message: text,
      history,
      collectionId: selectedCollectionId.value || undefined,
      scene: props.scene,
      context: props.context,
      provider: props.provider || undefined,
      model: props.model || undefined
    },
    (data) => {
      if (data.startsWith('[REF]')) {
        try {
          const refs = JSON.parse(data.substring(5))
          streamingReferences.value = refs || []
        } catch (e) {
          console.error('解析引用事件失败', e)
        }
      } else {
        streamingContent.value += data
      }
      scrollToBottom()
    },
    (err) => {
      streaming.value = false
      sending.value = false
      streamingReferences.value = []
      const errText = err && err.message ? err.message : t('aiChatPanel._a_i对话失败_21')
      ElMessage.error(errText)
      // 优先把错误信息展示在对话中；若已有流式内容则追加在错误信息后
      let errorMsg = t('aiChatPanel.调用失败_26') + '：' + errText
      if (streamingContent.value) {
        errorMsg += '\n' + streamingContent.value
      }
      if (replaceIndex === -1) {
        messages.value.push({ role: 'assistant', content: errorMsg, isError: true })
      } else {
        messages.value[replaceIndex] = { role: 'assistant', content: errorMsg, isError: true }
      }
      streamingContent.value = ''
      scrollToBottom()
    },
    () => {
      streaming.value = false
      sending.value = false
      if (replaceIndex === -1) {
        messages.value.push({ role: 'assistant', content: streamingContent.value, references: streamingReferences.value })
      } else {
        messages.value[replaceIndex] = { role: 'assistant', content: streamingContent.value, references: streamingReferences.value }
      }
      streamingContent.value = ''
      streamingReferences.value = []
      scrollToBottom()
    }
  )
}

function regenerate(assistantIndex) {
  let userIndex = -1
  for (let i = assistantIndex - 1; i >= 0; i--) {
    if (messages.value[i].role === 'user') {
      userIndex = i
      break
    }
  }
  if (userIndex === -1) return
  const userText = messages.value[userIndex].content
  messages.value.splice(assistantIndex, 1)
  sendMessage(userText, assistantIndex - 1)
}

function copyMessage(content) {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success(t('aiChatPanel.已复制_22'))
  }).catch(() => {
    ElMessage.error(t('aiChatPanel.复制失败_23'))
  })
}

function scrollToBottom() {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

function formatScore(score) {
  if (score === undefined || score === null) return ''
  return (score * 100).toFixed(1) + '%'
}
</script>

<style scoped>
.ai-chat-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}
.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
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
.chat-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  background: #f5f7fa;
  padding: 16px 20px;
}
.chat-message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.chat-message.user {
  flex-direction: row-reverse;
}
.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}
.chat-message.assistant .message-avatar {
  background: #67c23a;
}
.message-content {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  background: #fff;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.chat-message.user .message-content {
  background: #409eff;
  color: #fff;
}
.message-content.streaming {
  background: #e6f7ff;
}
.message-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
  opacity: 0.6;
}
.msg-action {
  cursor: pointer;
  font-size: 13px;
}
.msg-action:hover {
  opacity: 1;
  color: #409eff;
}
.cursor {
  animation: blink 1s infinite;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
.chat-input-area {
  padding: 16px 20px;
  border-top: 1px solid #e4e7ed;
  background: #fff;
}
.message-content.error {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  color: #cf1322;
}
.error-tag {
  display: inline-block;
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 6px;
  font-weight: 600;
}
.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.input-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.toolbar-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.collection-select {
  width: 220px;
}

.message-references {
  margin-top: 10px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
}

.references-title {
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.reference-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.5;
}

.reference-index {
  color: #409eff;
  font-weight: 600;
  flex-shrink: 0;
}

.reference-type {
  background: #e6f7ff;
  color: #409eff;
  padding: 1px 5px;
  border-radius: 4px;
  flex-shrink: 0;
}

.reference-title {
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reference-score {
  color: #909399;
  margin-left: auto;
  flex-shrink: 0;
}
</style>
