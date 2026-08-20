import request from '@/utils/request'

export function getApiCallLogList(params) {
  return request({ url: '/api-call-log/list', method: 'get', params })
}

export function getApiCallLogDetail(id) {
  return request({ url: `/api-call-log/${id}`, method: 'get' })
}

export function deleteApiCallLog(id) {
  return request({ url: `/api-call-log/${id}`, method: 'delete' })
}
