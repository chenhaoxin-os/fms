import request from '@/utils/request'

// 查询发卡列表
export function listCard(query) {
  return request({
    url: '/system/cardIssue/list',
    method: 'get',
    params: query
  })
}

// 新增卡片记录（待发卡）
export function addCard(data) {
  return request({
    url: '/system/cardIssue',
    method: 'post',
    data: data
  });
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

// api
export function generateCardCode(personCode, cardType) {
  return request({
    url: '/system/cardIssue/generateCode',
    method: 'get',
    params: { personCode, cardType }
  });
}
