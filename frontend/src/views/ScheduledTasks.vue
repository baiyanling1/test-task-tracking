<template>
  <div class="scheduled-tasks-container">
    <div class="page-header">
      <h1>定时任务管理</h1>
      <el-button type="primary" @click="refreshTasks">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 任务列表 -->
    <div class="tasks-table">
      <el-table :data="tasks" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="taskName" label="任务名称" width="200" />
        <el-table-column prop="taskDescription" label="描述" min-width="300" />
        <el-table-column prop="cronExpression" label="执行计划" width="200">
          <template #default="{ row }">
            <div v-if="editingCron === row.taskName">
              <el-input
                v-model="editCronForm.cronExpression"
                size="small"
                placeholder="请输入cron表达式"
                style="width: 150px;"
              />
              <el-button size="small" type="primary" @click="saveCron(row.taskName)" style="margin-left: 4px;">
                保存
              </el-button>
              <el-button size="small" @click="cancelEditCron" style="margin-left: 4px;">
                取消
              </el-button>
            </div>
            <div v-else>
              <span>{{ row.cronExpression }}</span>
              <el-button size="small" type="text" @click="editCron(row)" style="margin-left: 8px;">
                编辑
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastExecuteTime" label="最后执行时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.lastExecuteTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastExecuteResult" label="执行结果" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.lastExecuteResult === 'SUCCESS'" type="success">成功</el-tag>
            <el-tag v-else-if="row.lastExecuteResult === 'FAILED'" type="danger">失败</el-tag>
            <el-tag v-else type="info">从未执行</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nextExecuteTime" label="下次执行时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.nextExecuteTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="triggerTask(row.taskName)" :loading="triggeringTask === row.taskName">
              手动执行
            </el-button>
            <el-switch
              v-model="row.enabled"
              @change="toggleTask(row.taskName, row.enabled)"
              :loading="togglingTask === row.taskName"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>


  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getScheduledTasks, triggerTask as triggerTaskApi, toggleTask as toggleTaskApi, updateTaskSchedule } from '../api/scheduledTasks'

// 响应式数据
const tasks = ref([])
const loading = ref(false)
const triggeringTask = ref(null)
const togglingTask = ref(null)
const editingCron = ref(null)
const editCronForm = ref({
  cronExpression: ''
})

// 加载任务列表
const loadTasks = async () => {
  try {
    loading.value = true
    const response = await getScheduledTasks()
    tasks.value = response
  } catch (error) {
    ElMessage.error('加载定时任务列表失败')
    console.error('Load tasks error:', error)
  } finally {
    loading.value = false
  }
}

// 刷新任务列表
const refreshTasks = () => {
  loadTasks()
}

// 手动触发任务
const triggerTask = async (taskName) => {
  try {
    triggeringTask.value = taskName
    await triggerTaskApi(taskName)
    ElMessage.success('任务执行成功')
    // 刷新任务列表
    loadTasks()
  } catch (error) {
    ElMessage.error('任务执行失败: ' + (error.response?.data || error.message))
  } finally {
    triggeringTask.value = null
  }
}



// 切换任务状态
const toggleTask = async (taskName, enabled) => {
  try {
    togglingTask.value = taskName
    await toggleTaskApi(taskName, enabled)
    ElMessage.success('任务状态更新成功')
  } catch (error) {
    ElMessage.error('任务状态更新失败: ' + (error.response?.data || error.message))
    // 恢复原状态
    const task = tasks.value.find(t => t.taskName === taskName)
    if (task) {
      task.enabled = !enabled
    }
  } finally {
    togglingTask.value = null
  }
}

// 编辑cron表达式
const editCron = (row) => {
  editingCron.value = row.taskName
  editCronForm.value.cronExpression = row.cronExpression
}

// 保存cron表达式
const saveCron = async (taskName) => {
  try {
    await updateTaskSchedule(taskName, editCronForm.value.cronExpression)
    ElMessage.success('执行计划更新成功')
    editingCron.value = null
    // 刷新任务列表以获取更新后的下次执行时间
    loadTasks()
  } catch (error) {
    ElMessage.error('执行计划更新失败: ' + (error.response?.data || error.message))
  }
}

// 取消编辑cron表达式
const cancelEditCron = () => {
  editingCron.value = null
  editCronForm.value.cronExpression = ''
}

// 格式化日期时间（统一按本地时区展示）
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  try {
    let d
    // 数字时间戳（毫秒）
    if (typeof dateTime === 'number') {
      d = new Date(dateTime)
    } else if (typeof dateTime === 'string') {
      const s = dateTime.trim()
      const isoBasic = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/
      const isoWithMs = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{1,3}$/
      const hasZone = /[zZ]|[+-]\d{2}:?\d{2}$/
      const spaceSep = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}(:\d{2})?$/

      if (hasZone.test(s)) {
        // 已包含时区信息，直接解析
        d = new Date(s)
      } else if (isoBasic.test(s) || isoWithMs.test(s)) {
        // ISO 无时区，按 UTC 解析再转本地
        d = new Date(s + 'Z')
      } else if (spaceSep.test(s)) {
        // 空格分隔，按 UTC 解析
        d = new Date(s.replace(' ', 'T') + 'Z')
      } else if (/^\d+$/.test(s)) {
        // 纯数字字符串，当作毫秒时间戳
        d = new Date(parseInt(s, 10))
      } else {
        // 兜底
        d = new Date(s)
      }
    } else {
      d = new Date(dateTime)
    }

    if (isNaN(d.getTime())) return '-'

    return d.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    console.error('格式化时间失败:', dateTime, error)
    return '-'
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadTasks()
})

// 移除自动刷新，只在用户手动刷新时更新数据
</script>

<style scoped>
.scheduled-tasks-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.tasks-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.el-table {
  border-radius: 8px;
}

.el-button {
  margin-right: 8px;
}

.el-switch {
  margin-left: 8px;
}
</style>
