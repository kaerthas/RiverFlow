import request from '@/utils/request'

export function listKnowledgeDocs(params) {
  return request({
    url: '/ai/knowledge/docs',
    method: 'get',
    params
  })
}

export function createKnowledgeDoc(data) {
  return request({
    url: '/ai/knowledge/docs',
    method: 'post',
    data
  })
}

export function deleteKnowledgeDoc(id) {
  return request({
    url: `/ai/knowledge/docs/${id}`,
    method: 'delete'
  })
}

export function getKnowledgeChunks(id) {
  return request({
    url: `/ai/knowledge/docs/${id}/chunks`,
    method: 'get'
  })
}

export function rebuildKnowledgeIndex(data) {
  return request({
    url: '/ai/knowledge/rebuild',
    method: 'post',
    data
  })
}

export function searchKnowledge(data) {
  return request({
    url: '/ai/knowledge/search',
    method: 'post',
    data
  })
}
