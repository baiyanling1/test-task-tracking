import request from './request'

// 获取定时任务列表
export function getScheduledTasks() {
  return request({
    url: '/admin/scheduled-tasks',
    method: 'get'
  })
}

// 手动触发任务
export function triggerTask(taskName) {
  return request({
    url: `/admin/scheduled-tasks/${taskName}/trigger`,
    method: 'post'
  })
}



// 切换任务状态
export function toggleTask(taskName, enabled) {
  return request({
    url: `/admin/scheduled-tasks/${taskName}/toggle`,
    method: 'put',
    params: { enabled }
  })
}

// 更新任务执行计划
export function updateTaskSchedule(taskName, cronExpression) {
  return request({
    url: `/admin/scheduled-tasks/${taskName}/schedule`,
    method: 'post',
    data: { cronExpression }
  })
}
