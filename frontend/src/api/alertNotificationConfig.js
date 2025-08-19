import request from './request'

// 获取所有告警通知配置
export function getAlertNotificationConfigs() {
  return request({
    url: '/admin/alert-notification-config',
    method: 'get'
  })
}

// 更新单个告警通知配置
export function updateAlertNotificationConfig(alertType, dingtalkEnabled) {
  return request({
    url: `/admin/alert-notification-config/${alertType}`,
    method: 'put',
    data: {
      dingtalkEnabled
    }
  })
}

// 批量更新告警通知配置
export function updateAlertNotificationConfigs(configs) {
  return request({
    url: '/admin/alert-notification-config/batch',
    method: 'put',
    data: configs
  })
}
