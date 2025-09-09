import request from './request'

// 获取任务列表
export const getTasks = (params) => {
  return request({
    url: '/tasks',
    method: 'get',
    params
  })
}

// 获取任务详情
export const getTask = (id) => {
  return request({
    url: `/tasks/${id}`,
    method: 'get'
  })
}

// 创建任务
export const createTask = (data) => {
  return request({
    url: '/tasks',
    method: 'post',
    data
  })
}

// 更新任务
export const updateTask = (id, data) => {
  return request({
    url: `/tasks/${id}`,
    method: 'put',
    data
  })
}

// 删除任务
export const deleteTask = (id) => {
  return request({
    url: `/tasks/${id}`,
    method: 'delete'
  })
}

// 获取任务统计
export const getTaskStats = () => {
  return request({
    url: '/tasks/stats',
    method: 'get'
  })
}

// 批量操作任务
export const batchUpdateTasks = (data) => {
  return request({
    url: '/tasks/batch',
    method: 'put',
    data
  })
}

// 获取任务进度列表
export const getTaskProgress = async (taskId, params = {}) => {
  return request.get(`/tasks/${taskId}/progress`, { params })
}

// 添加任务进度
export const addTaskProgress = async (taskId, data) => {
  return request.post(`/tasks/${taskId}/progress`, data)
}

// 更新任务进度
export const updateTaskProgress = async (taskId, progressId, data) => {
  return request.put(`/tasks/${taskId}/progress/${progressId}`, data)
}

// 删除任务进度
export const deleteTaskProgress = async (taskId, progressId) => {
  return request.delete(`/tasks/${taskId}/progress/${progressId}`)
}

// 获取近6个月工时统计
export const getMonthlyManDaysStats = () => {
  return request({
    url: '/dashboard/monthly-man-days',
    method: 'get'
  })
}

// 获取上周没有填写任务的用户（默认）
export const getLastWeekInactiveUsers = () => {
  return request({
    url: '/dashboard/inactive-users',
    method: 'get'
  })
}

// 获取指定时间范围内没有填写任务的用户
export const getInactiveUsersByDateRange = (startDate, endDate) => {
  return request({
    url: '/dashboard/inactive-users/range',
    method: 'get',
    params: {
      startDate,
      endDate
    }
  })
} 