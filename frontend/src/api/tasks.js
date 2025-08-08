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