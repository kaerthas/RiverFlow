import request from '@/utils/request'

export function listVectorCollections(params) {
  return request({
    url: '/ai/vector/collections',
    method: 'get',
    params
  })
}

export function listAllVectorCollections() {
  return request({
    url: '/ai/vector/collections/all',
    method: 'get'
  })
}

export function getVectorCollection(id) {
  return request({
    url: `/ai/vector/collections/${id}`,
    method: 'get'
  })
}

export function createVectorCollection(data) {
  return request({
    url: '/ai/vector/collections',
    method: 'post',
    data
  })
}

export function updateVectorCollection(id, data) {
  return request({
    url: `/ai/vector/collections/${id}`,
    method: 'put',
    data
  })
}

export function deleteVectorCollection(id) {
  return request({
    url: `/ai/vector/collections/${id}`,
    method: 'delete'
  })
}

export function testVectorStore(id) {
  return request({
    url: `/ai/vector/collections/${id}/test`,
    method: 'post'
  })
}

export function setDefaultVectorCollection(id) {
  return request({
    url: `/ai/vector/collections/${id}/default`,
    method: 'post'
  })
}

export function testEmbeddingConfig(data) {
  return request({
    url: '/ai/embedding/test',
    method: 'post',
    data
  })
}
