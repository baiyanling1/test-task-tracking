import request from './request'

// 获取通知列表（作为告警使用）
export function getAlerts(params = {}) {
  return request({
    url: '/notifications',
    method: 'get',
    params
  })
}

// 根据状态获取通知
export function getAlertsByStatus(status, params = {}) {
  return request({
    url: `/notifications/status/${status}`,
    method: 'get',
    params
  })
}

// 根据优先级获取通知
export function getAlertsByPriority(priority, params = {}) {
  return request({
    url: `/notifications/priority/${priority}`,
    method: 'get',
    params
  })
}

// 搜索通知
export function searchAlerts(keyword, params = {}) {
  return request({
    url: '/notifications/search',
    method: 'get',
    params: { keyword, ...params }
  })
}

// 获取通知详情
export function getAlertById(alertId) {
  return request({
    url: `/notifications/${alertId}`,
    method: 'get'
  })
}

// 标记通知为已读
export function markAlertAsRead(alertId) {
  return request({
    url: `/notifications/${alertId}/read`,
    method: 'put'
  })
}

// 删除通知
export function deleteAlert(alertId) {
  return request({
    url: `/notifications/${alertId}`,
    method: 'delete'
  })
}

// 批量删除通知
export function deleteAlerts(alertIds) {
  return request({
    url: '/notifications/batch',
    method: 'delete',
    data: alertIds
  })
}

// 获取未读通知数量
export function getUnreadAlertCount() {
  return request({
    url: '/notifications/unread-count',
    method: 'get'
  })
}

// 批量标记为已读
export function markAllAlertsAsRead() {
  return request({
    url: '/notifications/mark-all-read',
    method: 'put'
  })
}

// 获取超时任务通知
export function getOverdueTaskAlerts(params = {}) {
  return request({
    url: '/notifications/overdue-tasks',
    method: 'get',
    params
  })
}

// 根据任务ID获取通知
export function getAlertsByTaskId(taskId) {
  return request({
    url: `/notifications/task/${taskId}`,
    method: 'get'
  })
}

// 测试钉钉连接
export function testDingTalkConnection() {
  return request({
    url: '/notifications/test-dingtalk',
    method: 'post',
    data: {
      message: '这是一条测试消息，用于验证钉钉webhook配置是否正确。',
      type: 'TEST'
    }
  })
}

// 获取钉钉配置
export function getDingTalkConfig() {
  return request({
    url: '/notifications/dingtalk-config',
    method: 'get'
  })
}

// 保存钉钉配置
export function saveDingTalkConfig(config) {
  return request({
    url: '/notifications/dingtalk-config',
    method: 'post',
    data: config
  })
} 