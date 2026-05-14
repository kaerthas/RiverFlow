import request from '@/utils/request'

export function getTableList(params) {
  return request({ url: '/dynamic-table/list', method: 'get', params })
}

export function createTable(data) {
  return request({ url: '/dynamic-table', method: 'post', data })
}

export function getTableDetail(id) {
  return request({ url: `/dynamic-table/${id}`, method: 'get' })
}

export function generateApi(id) {
  return request({ url: `/dynamic-table/${id}/gen-api`, method: 'post' })
}

export function deleteTable(id) {
  return request({ url: `/dynamic-table/${id}`, method: 'delete' })
}

export function getTableColumns(id) {
  return request({ url: `/dynamic-table/${id}/columns`, method: 'get' })
}

export function saveTableColumns(id, data) {
  return request({ url: `/dynamic-table/${id}/columns`, method: 'post', data })
}
