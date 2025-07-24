import request from './request'

// 获取告警列表
export const getAlerts = (params) => {
  return request({
    url: '/notifications',
    method: 'get',
    params
  })
}

// 获取告警详情
export const getAlert = (id) => {
  return request({
    url: `/notifications/${id}`,
    method: 'get'
  })
}

// 创建告警
export const createAlert = (data) => {
  return request({
    url: '/notifications',
    method: 'post',
    data
  })
}

// 更新告警
export const updateAlert = (id, data) => {
  return request({
    url: `/notifications/${id}`,
    method: 'put',
    data
  })
}

// 删除告警
export const deleteAlert = (id) => {
  return request({
    url: `/notifications/${id}`,
    method: 'delete'
  })
}

// 标记告警为已读
export const markAlertAsRead = (id) => {
  return request({
    url: `/notifications/${id}/read`,
    method: 'post'
  })
}

// 批量标记告警为已读
export const markAlertsAsRead = (ids) => {
  return request({
    url: '/notifications/batch-read',
    method: 'post',
    data: { ids }
  })
}

// 获取未读告警数量
export const getUnreadCount = () => {
  return request({
    url: '/notifications/unread-count',
    method: 'get'
  })
} 