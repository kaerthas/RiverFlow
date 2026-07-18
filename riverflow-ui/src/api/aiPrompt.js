import request from '@/utils/request'

const BASE = '/ai/prompt'

/**
 * 分页查询 AI Prompt 模板
 */
export const getAiPromptList = (params) => request({ url: `${BASE}/list`, method: 'get', params })

/**
 * 获取 AI Prompt 详情
 */
export const getAiPromptById = (id) => request({ url: `${BASE}/${id}`, method: 'get' })

/**
 * 新增 AI Prompt
 */
export const saveAiPrompt = (data) => request({ url: BASE, method: 'post', data })

/**
 * 修改 AI Prompt
 */
export const updateAiPrompt = (data) => request({ url: BASE, method: 'put', data })

/**
 * 删除 AI Prompt
 */
export const deleteAiPrompt = (id) => request({ url: `${BASE}/${id}`, method: 'delete' })

/**
 * 刷新指定场景缓存
 */
export const refreshAiPrompt = (scene) => request({ url: `${BASE}/refresh/${scene}`, method: 'post' })

/**
 * 刷新全部缓存
 */
export const refreshAllAiPrompt = () => request({ url: `${BASE}/refresh/all`, method: 'post' })

/**
 * 获取场景列表
 */
export const getAiPromptScenes = () => request({ url: `${BASE}/scenes`, method: 'get' })

/**
 * 按 Prompt 版本统计成功率
 */
export const getAiPromptStats = (params) => request({ url: `${BASE}/stats/version`, method: 'get', params })

/**
 * 获取某场景下所有 Prompt 版本
 */
export const getAiPromptVersions = (scene) => request({ url: `${BASE}/versions`, method: 'get', params: { scene } })
