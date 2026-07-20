import request from '@/utils/request'

export function listOperationLogs(params) {
  return request({
    url: '/operation-log/list',
    method: 'get',
    params
  })
}
