import request from '@/utils/request'

/**
 * 获取动态表的列定义
 */
export function getDynamicCrudColumns(tableId) {
  return request({ url: `/dynamic-crud/${tableId}/columns`, method: 'get' })
}

/**
 * 查询动态表数据（分页）
 */
export function getDynamicCrudData(tableId, params) {
  return request({ url: `/dynamic-crud/${tableId}/data`, method: 'get', params })
}

/**
 * 获取单条数据详情
 */
export function getDynamicCrudDetail(tableId, id) {
  return request({ url: `/dynamic-crud/${tableId}/data/${id}`, method: 'get' })
}

/**
 * 保存数据（新增或更新）
 */
export function saveDynamicCrudData(tableId, data) {
  return request({ url: `/dynamic-crud/${tableId}/data`, method: 'post', data })
}

/**
 * 删除数据
 */
export function deleteDynamicCrudData(tableId, id) {
  return request({ url: `/dynamic-crud/${tableId}/data/${id}`, method: 'delete' })
}
