import request from '@/utils/request'

export function getDatasourceList(params) {
  return request({ url: '/datasource/list', method: 'get', params })
}

export function createDatasource(data) {
  return request({ url: '/datasource', method: 'post', data })
}

export function updateDatasource(data) {
  return request({ url: '/datasource', method: 'put', data })
}

export function deleteDatasource(id) {
  return request({ url: `/datasource/${id}`, method: 'delete' })
}

export function testConnection(id) {
  return request({ url: `/datasource/${id}/test`, method: 'get' })
}

/**
 * 上传 JDBC 驱动 JAR 包
 * @param {FormData} formData 包含 dsCode、driverClass、file
 */
export function uploadDriverJar(formData) {
  return request({
    url: '/datasource/uploadDriverJar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
