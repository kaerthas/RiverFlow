import request from '@/utils/request'

export function getPluginList() {
  return request({
    url: '/plugin/list',
    method: 'get'
  })
}

export function getPluginConfig(nodeType) {
  return request({
    url: '/plugin/template',
    method: 'get',
    params: { nodeType }
  })
}

export function reloadPlugins() {
  return request({
    url: '/plugin/reload',
    method: 'get'
  })
}
