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
