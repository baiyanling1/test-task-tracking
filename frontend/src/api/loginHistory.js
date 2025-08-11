import request from './request'

// 获取当前用户的登录历史
export function getMyLoginHistory() {
  return request({
    url: '/login-history/my',
    method: 'get'
  })
}

// 分页获取当前用户的登录历史
export function getMyLoginHistoryPage(page = 0, size = 10) {
  return request({
    url: '/login-history/my/page',
    method: 'get',
    params: { page, size }
  })
}

// 获取当前用户最近登录记录
export function getMyRecentLoginHistory(limit = 5) {
  return request({
    url: '/login-history/my/recent',
    method: 'get',
    params: { limit }
  })
}

// 获取当前用户最后一次成功登录
export function getMyLastSuccessfulLogin() {
  return request({
    url: '/login-history/my/last-success',
    method: 'get'
  })
}

// 管理员获取指定用户的登录历史
export function getUserLoginHistory(userId) {
  return request({
    url: `/login-history/user/${userId}`,
    method: 'get'
  })
}

// 管理员分页获取指定用户的登录历史
export function getUserLoginHistoryPage(userId, page = 0, size = 10) {
  return request({
    url: `/login-history/user/${userId}/page`,
    method: 'get',
    params: { page, size }
  })
}

// 管理员清理旧的登录历史记录
export function cleanOldLoginHistory() {
  return request({
    url: '/login-history/clean-old',
    method: 'delete'
  })
}
