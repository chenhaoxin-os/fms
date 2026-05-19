import request from '@/utils/request'

// 查询发卡列表
export function listCard(query) {
  return request({
    url: '/system/cardIssue/list',
    method: 'get',
    params: query
  })
}

// 发卡（写卡+绑定）
export function issueCard(data) {
  return request({
    url: '/system/cardIssue/issue',
    method: 'post',
    params: data
  })
}

// 注销
export function cancelCard(data) {
  return request({
    url: '/system/cardIssue/cancel',
    method: 'post',
    params: data
  })
}
