import request from '@/utils/request'

export function getApiAppList(params) {
  return request({ url: '/api-app/list', method: 'get', params })
}

export function getApiAppListAll(params) {
  return request({ url: '/api-app/list-all', method: 'get', params })
}

export function getApiAppDetail(id) {
  return request({ url: `/api-app/${id}`, method: 'get' })
}

export function saveApiApp(data) {
  return request({ url: '/api-app', method: 'post', data })
}

export function updateApiApp(data) {
  return request({ url: '/api-app', method: 'put', data })
}

export function deleteApiApp(id) {
  return request({ url: `/api-app/${id}`, method: 'delete' })
}

export function getApiAppCounts(appIds) {
  return request({ url: '/api-app/api-counts', method: 'get', params: { appIds: appIds.join(',') } })
}
