import request from '@/utils/request'

export function getApiCatalogList(params) {
  return request({ url: '/api-catalog/list', method: 'get', params })
}

export function getApiCatalogDetail(id) {
  return request({ url: `/api-catalog/${id}`, method: 'get' })
}

export function saveApiCatalog(data) {
  return request({ url: '/api-catalog', method: 'post', data })
}

export function updateApiCatalog(data) {
  return request({ url: '/api-catalog', method: 'put', data })
}

export function deleteApiCatalog(id) {
  return request({ url: `/api-catalog/${id}`, method: 'delete' })
}

export function getApiParams(id) {
  return request({ url: `/api-catalog/${id}/params`, method: 'get' })
}

export function saveApiParams(id, data) {
  return request({ url: `/api-catalog/${id}/params`, method: 'post', data })
}

export function getApiScriptList(params) {
  return request({ url: '/api-script/list', method: 'get', params })
}

export function getApiScriptDetail(id) {
  return request({ url: `/api-script/${id}`, method: 'get' })
}

export function saveApiScript(data) {
  return request({ url: '/api-script', method: 'post', data })
}

export function updateApiScript(data) {
  return request({ url: '/api-script', method: 'put', data })
}

export function deleteApiScript(id) {
  return request({ url: `/api-script/${id}`, method: 'delete' })
}
