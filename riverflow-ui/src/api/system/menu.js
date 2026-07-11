import request from '@/utils/request'

export function getMenuList(params) {
  return request({
    url: '/system/menu/list',
    method: 'get',
    params
  })
}

export function getAllMenus() {
  return request({
    url: '/system/menu/all',
    method: 'get'
  })
}

export function getMenuDetail(id) {
  return request({
    url: `/system/menu/${id}`,
    method: 'get'
  })
}

export function createMenu(data) {
  return request({
    url: '/system/menu',
    method: 'post',
    data
  })
}

export function updateMenu(data) {
  return request({
    url: '/system/menu',
    method: 'put',
    data
  })
}

export function deleteMenu(id) {
  return request({
    url: `/system/menu/${id}`,
    method: 'delete'
  })
}
