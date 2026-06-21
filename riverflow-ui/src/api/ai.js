import i18n from '@/i18n'
const { t } = i18n.global
import request from '@/utils/request'
import { useUserStore } from '@/store/modules/user'

const AI_BASE = '/api/ai'

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

  fetch(`${AI_BASE}/chat/stream`, {
    method: 'POST',
    headers,
    body: JSON.stringify(data)
  }).then(response => {
    if (!response.ok) {
      throw new Error(t('ai._a_i流式调用失败_1') + response.status)
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

/**
 * 自然语言生成流程
 */
export const aiGenerateFlow = (data) => request.post(`${AI_BASE}/generate-flow`, data)

/**
 * 自然语言生成 SpEL 条件表达式
 */
export const aiGenerateCondition = (data) => request.post(`${AI_BASE}/generate-condition`, data)

/**
 * 智能推荐数据映射
 */
export const aiGenerateMapping = (data) => request.post(`${AI_BASE}/generate-mapping`, data)

/**
 * 自然语言生成 Groovy 脚本
 */
export const aiGenerateScript = (data) => request.post(`${AI_BASE}/generate-script`, data)

/**
 * 接口文档智能解析
 */
export const aiParseApiDoc = (data) => request.post(`${AI_BASE}/parse-api-doc`, data)
