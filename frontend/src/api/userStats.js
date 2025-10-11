import request from './request'

/**
 * 获取用户工作统计数据（仅管理员）
 */
export const getUsersWorkStats = () => {
  return request({
    url: '/dashboard/users-work-stats',
    method: 'get'
  })
}
