// src/api/event.js
import request from '@/utils/request'

/**
 * 查询事件列表（分页）
 * @param {Object} query 查询参数
 * @returns {Promise}
 */
export function getEventList(query) {
  return request({
    url: '/system/event/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取事件统计信息
 * @returns {Promise}
 */
export function getEventStatistics() {
  return request({
    url: '/system/event/statistics',
    method: 'get'
  })
}

/**
 * 获取事件详情
 * @param {Number} id 事件ID
 * @returns {Promise}
 */
export function getEventDetail(id) {
  return request({
    url: `/system/event/${id}`,
    method: 'get'
  })
}

/**
 * 新增事件
 * @param {Object} data 事件数据
 * @returns {Promise}
 */
export function addEvent(data) {
  return request({
    url: '/system/event',
    method: 'post',
    data: data
  })
}

/**
 * 修改事件
 * @param {Object} data 事件数据
 * @returns {Promise}
 */
export function updateEvent(data) {
  return request({
    url: '/system/event',
    method: 'put',
    data: data
  })
}

/**
 * 修改事件状态
 * @param {Number} id 事件ID
 * @param {String} status 状态
 * @returns {Promise}
 */
export function changeEventStatus(id, status) {
  return request({
    url: '/system/event/changeStatus',
    method: 'put',
    params: { id, status }
  })
}

/**
 * 删除事件
 * @param {Array|String} ids 事件ID数组或逗号分隔字符串
 * @returns {Promise}
 */
export function delEvent(ids) {
  return request({
    url: `/system/event/${ids}`,
    method: 'delete'
  })
}

/**
 * 导出事件数据
 * @param {Object} query 查询参数
 * @returns {Promise}
 */
export function exportEvent(query) {
  return request({
    url: '/system/event/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}
