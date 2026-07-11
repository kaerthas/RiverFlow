import request from '@/utils/request'

export function getRoleList(params) {
  return request({
    url: '/system/role/list',
    method: 'get',
    params
  })
}

export function getRoleDetail(id) {
  return request({
    url: `/system/role/${id}`,
    method: 'get'
  })
}

export function getRoleMenus(id) {
  return request({
    url: `/system/role/${id}/menus`,
    method: 'get'
  })
}

export function createRole(data) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: '/system/role',
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: `/system/role/${id}`,
    method: 'delete'
  })
}

export function assignRoleMenus(id, menuIds) {
  return request({
    url: `/system/role/${id}/menus`,
    method: 'post',
    data: menuIds
  })
}
