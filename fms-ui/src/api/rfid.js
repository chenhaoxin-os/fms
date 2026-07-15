import request from '@/utils/request';

/**
 * 单次读取卡片
 * @returns {Promise} 返回卡片信息
 */
export function readCard() {
  return request({
    url: '/system/rfid/readCard',
    method: 'post',
    timeout: 10000
  });
}

/**
 * 启动持续读取
 * @returns {Promise}
 */
export function startRead() {
  return request({
    url: '/system/rfid/startRead',
    method: 'post'
  });
}

/**
 * 停止读取
 * @returns {Promise}
 */
export function stopRead() {
  return request({
    url: '/system/rfid/stopRead',
    method: 'post'
  });
}

/**
 * 获取当前所有标签
 * @returns {Promise}
 */
export function getTags() {
  return request({
    url: '/system/rfid/getTags',
    method: 'get'
  });
}

/**
 * 获取读写器状态
 * @returns {Promise}
 */
export function getReaderStatus() {
  return request({
    url: '/system/rfid/status',
    method: 'get'
  });
}

/**
 * 单次读取（返回原始数据）
 * @returns {Promise}
 */
export function readCardRaw() {
  return request({
    url: '/system/rfid/readCard',
    method: 'post',
    timeout: 10000
  });
}
/**
 * 写入EPC
 * @param {string} epc EPC十六进制字符串
 * @returns {Promise}
 */
export function writeEpc(data) {
  return request({
    url: '/system/rfid/writeEpc',
    method: 'post',
    data: data,
    timeout: 15000 // 写入可能稍慢
  });
}
