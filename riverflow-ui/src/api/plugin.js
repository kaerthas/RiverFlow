import request from '@/utils/request'

export function uploadPlugin(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/plugin/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getPluginList(params) {
  return request({
    url: '/plugin/list',
    method: 'get',
    params
  })
}

export function getPluginDetail(id) {
  return request({
    url: `/plugin/detail/${id}`,
    method: 'get'
  })
}

export function getLoadedPlugins() {
  return request({
    url: '/plugin/loaded',
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

export function enablePlugin(id) {
  return request({
    url: `/plugin/enable/${id}`,
    method: 'post'
  })
}

export function disablePlugin(id) {
  return request({
    url: `/plugin/disable/${id}`,
    method: 'post'
  })
}

export function deletePlugin(id) {
  return request({
    url: `/plugin/delete/${id}`,
    method: 'delete'
  })
}

export function reloadPlugin(id) {
  return request({
    url: `/plugin/reload/${id}`,
    method: 'post'
  })
}

export function getCategories() {
  return request({
    url: '/plugin/categories',
    method: 'get'
  })
}
