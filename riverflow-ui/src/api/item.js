import request from '@/utils/request'

export function getItemList(params) {
  return request({
    url: '/item/list',
    method: 'get',
    params
  })
}

export function getItemDetail(id) {
  return request({
    url: `/item/${id}`,
    method: 'get'
  })
}

export function createItem(data) {
  return request({
    url: '/item',
    method: 'post',
    data
  })
}

export function updateItem(data) {
  return request({
    url: '/item',
    method: 'put',
    data
  })
}

export function deleteItem(id) {
  return request({
    url: `/item/${id}`,
    method: 'delete'
  })
}

export function getRegionTree() {
  return request({
    url: '/region/tree',
    method: 'get'
  })
}
