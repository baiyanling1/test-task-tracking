import request from './request'

// 获取白名单配置
export function getWhitelist() {
  return request({
    url: '/task-tracking-config/whitelist',
    method: 'get'
  })
}

// 保存白名单配置
export function saveWhitelist(usernames) {
  return request({
    url: '/task-tracking-config/whitelist',
    method: 'post',
    data: { usernames }
  })
}

// 检查用户是否在白名单中
export function checkUserInWhitelist(username) {
  return request({
    url: `/task-tracking-config/whitelist/check/${username}`,
    method: 'get'
  })
}

// 获取所有活跃用户列表
export function getAllActiveUsers() {
  return request({
    url: '/task-tracking-config/users',
    method: 'get'
  })
}
