import request from '@/utils/request'

// 流程定义
export function getFlowDefinitionList(params) {
  return request({
    url: '/workflow/definition/list',
    method: 'get',
    params
  })
}

export function getFlowDefinitionDetail(id) {
  return request({
    url: `/workflow/definition/${id}`,
    method: 'get'
  })
}

export function saveFlowDefinition(data) {
  return request({
    url: '/workflow/definition',
    method: 'post',
    data
  })
}

export function publishFlowDefinition(id) {
  return request({
    url: `/workflow/definition/${id}/publish`,
    method: 'put'
  })
}

export function validateFlowDefinition(id) {
  return request({
    url: `/workflow/definition/${id}/validate`,
    method: 'post'
  })
}

export function deleteFlowDefinition(id) {
  return request({
    url: `/workflow/definition/${id}`,
    method: 'delete'
  })
}

export function offlineFlowDefinition(id) {
  return request({
    url: `/workflow/definition/${id}/offline`,
    method: 'put'
  })
}

export function copyFlowDefinition(id) {
  return request({
    url: `/workflow/definition/${id}/copy`,
    method: 'post'
  })
}

export function getFlowVersions(flowCode) {
  return request({
    url: '/workflow/definition/versions',
    method: 'get',
    params: { flowCode }
  })
}

export function saveFlowGraph(flowId, data) {
  return request({
    url: `/workflow/definition/${flowId}/save-graph`,
    method: 'post',
    data
  })
}

export function getFlowNodes(flowId) {
  return request({
    url: `/workflow/definition/${flowId}/nodes`,
    method: 'get'
  })
}

export function getFlowEdges(flowId) {
  return request({
    url: `/workflow/definition/${flowId}/edges`,
    method: 'get'
  })
}

// 流程实例
export function getFlowInstanceList(params) {
  return request({
    url: '/workflow/instance/list',
    method: 'get',
    params
  })
}

export function getFlowInstanceDetail(id) {
  return request({
    url: `/workflow/instance/${id}`,
    method: 'get'
  })
}

export function startFlowInstance(flowId, businessKey) {
  return request({
    url: `/workflow/instance/${flowId}/start`,
    method: 'post',
    params: { businessKey }
  })
}

export function executeFlowInstance(instanceId) {
  return request({
    url: `/workflow/instance/${instanceId}/execute`,
    method: 'post'
  })
}

export function terminateFlowInstance(id) {
  return request({
    url: `/workflow/instance/${id}/terminate`,
    method: 'put'
  })
}

export function suspendFlowInstance(id) {
  return request({
    url: `/workflow/instance/${id}/suspend`,
    method: 'put'
  })
}

export function resumeFlowInstance(instanceId) {
  return request({
    url: `/workflow/instance/${instanceId}/resume`,
    method: 'post'
  })
}

export function retryFlowInstance(instanceId) {
  return request({
    url: `/workflow/instance/${instanceId}/retry`,
    method: 'post'
  })
}

export function getInstanceTasks(instanceId) {
  return request({
    url: `/workflow/instance/${instanceId}/tasks`,
    method: 'get'
  })
}

export function getInstanceLogs(instanceId, page = 1, size = 5) {
  return request({
    url: `/workflow/instance/${instanceId}/logs`,
    method: 'get',
    params: { page, size }
  })
}

export function getLoopProgress(instanceId, loopNodeId) {
  return request({
    url: `/workflow/instance/${instanceId}/loop-progress`,
    method: 'get',
    params: { loopNodeId }
  })
}

// ==================== 同步流程执行（对外接口，管理后台调试使用）====================

export function executeSyncFlow(data) {
  return request({
    url: '/open/flow/executeSync',
    method: 'post',
    data,
    timeout: 120000 // 同步执行最长支持 120 秒
  })
}
