import request from '@/utils/request'

export function getDeptList(params) {
  return request({
    url: '/system/dept/list',
    method: 'get',
    params
  })
}

export function getAllDepts() {
  return request({
    url: '/system/dept/all',
    method: 'get'
  })
}

export function getDeptDetail(id) {
  return request({
    url: `/system/dept/${id}`,
    method: 'get'
  })
}

export function createDept(data) {
  return request({
    url: '/system/dept',
    method: 'post',
    data
  })
}

export function updateDept(data) {
  return request({
    url: '/system/dept',
    method: 'put',
    data
  })
}

export function deleteDept(id) {
  return request({
    url: `/system/dept/${id}`,
    method: 'delete'
  })
}
