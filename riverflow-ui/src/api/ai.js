import i18n from '@/i18n'
const { t } = i18n.global
import request from '@/utils/request'
import { useUserStore } from '@/store/modules/user'

const AI_BASE = '/ai'
const AI_STREAM_URL = '/api/ai/chat/stream'

/**
 * 获取可用的 LLM Provider 列表
 */
export const aiGetProviders = () => request.get(`${AI_BASE}/providers`)

/**
 * AI 通用对话
 */
export const aiChat = (data) => request.post(`${AI_BASE}/chat`, data)

/**
 * AI 通用对话（SSE 流式）
 * @param {Object} data - { message, history, provider }
 * @param {Function} onMessage - 每次收到流式数据的回调
 * @param {Function} onError - 错误回调
 * @param {Function} onComplete - 完成回调
 */
export function aiChatStream(data, onMessage, onError, onComplete) {
  const userStore = useUserStore()
  const headers = {
    'Content-Type': 'application/json'
  }
  if (userStore.token) {
    headers['Authorization'] = 'Bearer ' + userStore.token
  }

  fetch(AI_STREAM_URL, {
    method: 'POST',
    headers,
    body: JSON.stringify(data)
  }).then(response => {
    if (!response.ok) {
      return response.text().then(text => {
        const detail = text || `HTTP ${response.status}`
        throw new Error(`${t('ai._a_i流式调用失败_1')} (${response.status}): ${detail}`)
      })
    }
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    function read() {
      reader.read().then(({ done, value }) => {
        if (done) {
          onComplete && onComplete()
          return
        }
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() // 保留未完整的一行

        for (const line of lines) {
          const trimmed = line.trim()
          if (!trimmed || !trimmed.startsWith('data:')) continue
          const dataStr = trimmed.substring(5).trim()
          if (dataStr === '[DONE]') {
            onComplete && onComplete()
            return
          }
          // 后端错误事件
          if (dataStr.startsWith('[ERROR]')) {
            onError && onError(new Error(dataStr.substring(7)))
            return
          }
          // SSE data 可能是裸字符串，也可能是 JSON
          onMessage && onMessage(dataStr)
        }
        read()
      }).catch(err => {
        onError && onError(err)
      })
    }
    read()
  }).catch(err => {
    onError && onError(err)
  })
}

// AI 生成类接口超时 10 分钟，避免模型推理慢导致 axios 30 秒超时
const AI_GENERATE_TIMEOUT = 600000
const AI_STREAM_URL_FLOW = '/api/ai/generate-flow/stream'

/**
 * 自然语言生成流程
 */
export const aiGenerateFlow = (data) => request.post(`${AI_BASE}/generate-flow`, data, { timeout: AI_GENERATE_TIMEOUT })

/**
 * 自然语言生成流程（SSE 流式输出）
 */
export function aiGenerateFlowStream(data, onThink, onResult, onError, onComplete) {
  const userStore = useUserStore()
  const headers = {
    'Content-Type': 'application/json'
  }
  if (userStore.token) {
    headers['Authorization'] = 'Bearer ' + userStore.token
  }

  fetch(AI_STREAM_URL_FLOW, {
    method: 'POST',
    headers,
    body: JSON.stringify(data)
  }).then(response => {
    if (!response.ok) {
      return response.text().then(text => {
        const detail = text || `HTTP ${response.status}`
        throw new Error(`流程生成流式调用失败 (${response.status}): ${detail}`)
      })
    }
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    function read() {
      reader.read().then(({ done, value }) => {
        if (done) {
          onComplete && onComplete()
          return
        }
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop()

        for (const line of lines) {
          const trimmed = line.trim()
          if (!trimmed || !trimmed.startsWith('data:')) continue
          const dataStr = trimmed.substring(5).trim()
          if (dataStr === '[DONE]') {
            onComplete && onComplete()
            return
          }
          if (dataStr.startsWith('[ERROR]')) {
            onError && onError(new Error(dataStr.substring(7)))
            return
          }
          if (dataStr.startsWith('[THINK]')) {
            onThink && onThink(dataStr.substring(7))
            continue
          }
          if (dataStr.startsWith('[JSON]')) {
            try {
              const json = JSON.parse(dataStr.substring(6))
              onResult && onResult(json)
            } catch (e) {
              onError && onError(new Error('解析流程 JSON 失败: ' + e.message))
            }
            continue
          }
        }
        read()
      }).catch(err => {
        onError && onError(err)
      })
    }
    read()
  }).catch(err => {
    onError && onError(err)
  })
}

/**
 * 自然语言生成 SpEL 条件表达式
 */
export const aiGenerateCondition = (data) => request.post(`${AI_BASE}/generate-condition`, data, { timeout: AI_GENERATE_TIMEOUT })

/**
 * 智能推荐数据映射
 */
export const aiGenerateMapping = (data) => request.post(`${AI_BASE}/generate-mapping`, data, { timeout: AI_GENERATE_TIMEOUT })

/**
 * 自然语言生成 Groovy 脚本
 */
export const aiGenerateScript = (data) => request.post(`${AI_BASE}/generate-script`, data, { timeout: AI_GENERATE_TIMEOUT })

/**
 * 接口文档智能解析
 */
export const aiParseApiDoc = (data) => request.post(`${AI_BASE}/parse-api-doc`, data, { timeout: AI_GENERATE_TIMEOUT })
