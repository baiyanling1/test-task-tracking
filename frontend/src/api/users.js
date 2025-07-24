import request from './request'

// 获取用户列表
export const getUsers = (params) => {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

// 获取用户详情
export const getUser = (id) => {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

// 创建用户
export const createUser = (data) => {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

// 更新用户
export const updateUser = (id, data) => {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export const deleteUser = (id) => {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

// 禁用用户
export const disableUser = (id) => {
  return request({
    url: `/users/${id}/disable`,
    method: 'post'
  })
}

// 启用用户
export const enableUser = (id) => {
  return request({
    url: `/users/${id}/enable`,
    method: 'post'
  })
}

// 重置用户密码
export const resetUserPassword = (id) => {
  return request({
    url: `/users/${id}/reset-password`,
    method: 'post'
  })
}

// 获取用户统计
export const getUserStats = () => {
  return request({
    url: '/users/stats',
    method: 'get'
  })
} 