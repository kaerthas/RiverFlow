import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params
  })
}

export function getUserDetail(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'get'
  })
}

export function getAllUsers() {
  return request({
    url: '/system/user/all',
    method: 'get'
  })
}

export function createUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'delete'
  })
}
