import request from '@/utils/request'

// 查询车辆列表
export function getVehicleList(query) {
  return request({
    url: '/system/vehicle/list',
    method: 'get',
    params: query
  })
}

// 新增车辆
export function addVehicle(data) {
  return request({
    url: '/system/vehicle',
    method: 'post',
    data: data
  })
}

// 修改车辆
export function updateVehicle(data) {
  return request({
    url: '/system/vehicle',
    method: 'put',
    data: data
  })
}

// 删除车辆
export function delVehicle(vehicleId,config) {
  return request({
    url: '/system/vehicle/' + vehicleId,
    method: 'delete',
    ...config   // 展开配置，使 _silentError 等属性进入请求配置
  })
}

// 获取车辆详情
export function getVehicleDetail(vehicleId) {
  return request({
    url: '/system/vehicle/' + vehicleId,
    method: 'get'
  })
}
/**
 * 解绑装置
 * @param {number} id 车辆ID
 */
export function unbindDevice(id) {
  return request({
    url: '/system/vehicle/unbindDevice/' + id,
    method: 'put'
  })
}
