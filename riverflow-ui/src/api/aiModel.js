import request from '@/utils/request'

const BASE = '/ai/model'

/**
 * 分页查询 AI 模型配置
 */
export const getAiModelList = (params) => request({ url: `${BASE}/list`, method: 'get', params })

/**
 * 获取 AI 模型详情
 */
export const getAiModelById = (id) => request({ url: `${BASE}/${id}`, method: 'get' })

/**
 * 新增 AI 模型
 */
export const saveAiModel = (data) => request({ url: BASE, method: 'post', data })

/**
 * 修改 AI 模型
 */
export const updateAiModel = (data) => request({ url: BASE, method: 'put', data })

/**
 * 删除 AI 模型
 */
export const deleteAiModel = (id) => request({ url: `${BASE}/${id}`, method: 'delete' })

/**
 * 刷新运行时 Provider
 */
export const reloadAiModel = () => request({ url: `${BASE}/reload`, method: 'post' })

/**
 * 获取可用模型选项（给 AI 助手页面使用）
 */
export const getAiModelOptions = () => request({ url: `${BASE}/options`, method: 'get' })
