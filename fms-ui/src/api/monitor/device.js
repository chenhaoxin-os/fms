import request from '@/utils/request'

// 查询装置列表
export function listDevice(query) {
  return request({
    url: '/device/list',
    method: 'get',
    params: query
  })
}

// 查询装置详细
export function getDevice(id) {
  return request({
    url: '/device/' + id,
    method: 'get'
  })
}

// 新增装置
export function addDevice(data) {
  return request({
    url: '/device',
    method: 'post',
    data: data
  })
}

// 修改装置
export function updateDevice(data) {
  return request({
    url: '/device',
    method: 'put',
    data: data
  })
}

// 删除装置
export function delDevice(ids) {
  return request({
    url: '/device/' + ids,
    method: 'delete'
  })
}

// 更改装置状态
export function changeDeviceStatus(id, status) {
  const data = { id, status }
  return request({
    url: '/device/changeStatus',
    method: 'put',
    params: { id, status }
  })
}

// 导出装置
export function exportDevice(query) {
  return request({
    url: '/device/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}


// 获取装置统计信息
export function getDeviceStatistics() {
  return request({
    url: '/device/statistics',
    method: 'get'
  })
}
