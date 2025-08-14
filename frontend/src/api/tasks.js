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

// 获取所有版本任务
export const getVersionTasks = () => {
  return request({
    url: '/tasks/versions',
    method: 'get'
  })
}

// 获取指定版本下的需求任务
export const getRequirementTasksByVersion = (versionTaskId) => {
  return request({
    url: `/tasks/versions/${versionTaskId}/requirements`,
    method: 'get'
  })
}

// 计算版本任务的总体进度
export const calculateVersionProgress = (versionTaskId) => {
  return request({
    url: `/tasks/versions/${versionTaskId}/calculate-progress`,
    method: 'post'
  })
}

// 获取任务的层级结构
export const getTaskHierarchy = () => {
  return request({
    url: '/tasks/hierarchy',
    method: 'get'
  })
} 