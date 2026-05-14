import request from '@/utils/request'

export function getMonitorStats() {
  return request({ url: '/monitor/stats', method: 'get' })
}

export function getRecentLogs(limit = 20) {
  return request({ url: '/monitor/recent-logs', method: 'get', params: { limit } })
}

export function getPendingTasks() {
  return request({ url: '/monitor/pending-tasks', method: 'get' })
}
