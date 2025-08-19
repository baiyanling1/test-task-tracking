import request from './request'

// 获取通知列表
export function getNotifications(params) {
  return request({
    url: '/notifications',
    method: 'get',
    params
  })
}

// 获取未读通知数量
export function getUnreadCount() {
  return request({
    url: '/notifications/unread-count',
    method: 'get'
  })
}

// 标记通知为已读
export function markAsRead(notificationId) {
  return request({
    url: `/notifications/${notificationId}/read`,
    method: 'put'
  })
}

// 标记所有通知为已读
export function markAllAsRead() {
  return request({
    url: '/notifications/mark-all-read',
    method: 'put'
  })
}

// 删除通知
export function deleteNotification(notificationId) {
  return request({
    url: `/notifications/${notificationId}`,
    method: 'delete'
  })
}

// 批量删除通知
export function deleteNotifications(notificationIds) {
  return request({
    url: '/notifications/batch',
    method: 'delete',
    data: notificationIds
  })
}

// 钉钉配置相关API
export function getDingTalkConfig() {
  return request({
    url: '/notifications/dingtalk-config',
    method: 'get'
  })
}

export function testDingTalkNotification() {
  return request({
    url: '/notifications/test-dingtalk',
    method: 'post'
  })
}

export function saveDingTalkConfig(config) {
  return request({
    url: '/notifications/dingtalk-config',
    method: 'post',
    data: config
  })
}
