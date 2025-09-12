import request from './request'

// 获取所有任务数据（统一接口，返回完整数据）
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

// ========================================
// 子任务管理API
// ========================================

// getTaskTree已移除，统一使用getTasks接口

// 为指定主任务创建子任务
export const createSubTask = (parentTaskId, data) => {
  return request({
    url: `/tasks/${parentTaskId}/subtasks`,
    method: 'post',
    data
  })
}

// 获取指定主任务的子任务列表
export const getSubTasks = (parentTaskId) => {
  return request({
    url: `/tasks/${parentTaskId}/subtasks`,
    method: 'get'
  })
}

// 更新子任务进度
export const updateSubTaskProgress = (subTaskId, progressPercentage) => {
  return request({
    url: `/tasks/subtasks/${subTaskId}/progress`,
    method: 'put',
    data: { progressPercentage }
  })
}

// 主任务创建者手动更新主任务进度
export const updateMainTaskProgressManually = (mainTaskId, progressPercentage) => {
  return request({
    url: `/tasks/${mainTaskId}/progress/manual`,
    method: 'put',
    data: { progressPercentage }
  })
}

// 重新计算主任务进度
export const recalculateMainTaskProgress = (mainTaskId) => {
  return request({
    url: `/tasks/${mainTaskId}/recalculate-progress`,
    method: 'post'
  })
}

// 将任务转换为主任务
export const convertToMainTask = (taskId) => {
  return request({
    url: `/tasks/${taskId}/convert-to-main`,
    method: 'put'
  })
}

// 删除子任务
export const deleteSubTask = (subTaskId) => {
  return request({
    url: `/tasks/subtasks/${subTaskId}`,
    method: 'delete'
  })
}

// 获取用户的子任务统计
export const getUserSubTaskStatistics = () => {
  return request({
    url: '/tasks/statistics/user-subtasks',
    method: 'get'
  })
}

// 管理员获取所有用户的子任务统计
export const getAllUserSubTaskStatistics = () => {
  return request({
    url: '/tasks/statistics/all-user-subtasks',
    method: 'get'
  })
}

// 获取子任务责任人分配统计
export const getSubTaskAssigneeStatistics = () => {
  return request({
    url: '/tasks/statistics/subtask-assignees',
    method: 'get'
  })
}

// 批量更新子任务状态
export const batchUpdateSubTaskStatus = (subTaskIds, status) => {
  return request({
    url: '/tasks/subtasks/batch-update-status',
    method: 'put',
    data: { subTaskIds, status }
  })
} 