<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon total">
              <el-icon><List /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalTasks }}</div>
              <div class="stats-label">总任务数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.pendingTasks }}</div>
              <div class="stats-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon in-progress">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.inProgressTasks }}</div>
              <div class="stats-label">进行中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon completed">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.completedTasks }}</div>
              <div class="stats-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon overdue">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.overdueTasks }}</div>
              <div class="stats-label">超时任务</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon man-days">
              <el-icon><User /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalManDays }}</div>
              <div class="stats-label">总人天</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>任务状态分布</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="pieChartOption" style="height: 300px" />
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>部门任务分布</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="departmentChartOption" style="height: 300px" />
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>本周任务趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="lineChartOption" style="height: 300px" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近任务和提醒 -->
    <el-row :gutter="20" class="lists-row">
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="list-card">
          <template #header>
            <div class="card-header">
              <span>最近任务</span>
              <el-button text @click="$router.push('/tasks')">查看全部</el-button>
            </div>
          </template>
          <div class="task-list">
            <div v-for="task in recentTasks" :key="task.id" class="task-item">
              <div class="task-info">
                <div class="task-header">
                  <div class="task-name" :title="task.taskName">{{ task.taskName }}</div>
                  <el-tag :type="getStatusType(task.status)" size="small" class="status-tag">
                    {{ getStatusText(task.status) }}
                  </el-tag>
                </div>
                <div class="task-meta">
                  <div class="assignee-info">
                    <el-icon><User /></el-icon>
                    <span>{{ task.assignedToName }}</span>
                  </div>
                  <div class="task-time" v-if="task.createdTime">
                    {{ formatTime(task.createdTime) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="list-card">
          <template #header>
            <div class="card-header">
              <span>超时任务</span>
              <el-button text @click="$router.push('/tasks')">查看全部</el-button>
            </div>
          </template>
          <div class="task-list">
            <div v-for="task in overdueTasks" :key="task.id" class="task-item overdue">
              <div class="task-info">
                <div class="task-header">
                  <div class="task-name" :title="task.taskName">{{ task.taskName }}</div>
                  <el-tag type="danger" size="small" class="overdue-tag">超时</el-tag>
                </div>
                <div class="task-meta">
                  <div class="assignee-info">
                    <el-icon><User /></el-icon>
                    <span>{{ task.assignedToName }}</span>
                  </div>
                  <div class="status-info">
                    <el-tag :type="getStatusType(task.status)" size="small">
                      {{ getStatusText(task.status) }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="list-card">
          <template #header>
            <div class="card-header">
              <span>个人任务统计</span>
              <el-button text @click="$router.push('/tasks')">查看全部</el-button>
            </div>
          </template>
          <div class="chart-container">
            <v-chart :option="userTaskChartOption" style="height: 300px" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import * as echarts from 'echarts/core'
import dayjs from 'dayjs'
import { getTaskStats, getTasks } from '@/api/tasks'
import { getAlerts } from '@/api/alerts'
import request from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import { User } from '@element-plus/icons-vue'

use([
  CanvasRenderer,
  PieChart,
  LineChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const stats = ref({
  totalTasks: 0,
  plannedTasks: 0,      // 计划中
  inProgressTasks: 0,   // 进行中
  completedTasks: 0,    // 已完成
  onHoldTasks: 0,       // 已暂停
  cancelledTasks: 0,    // 已取消
  overdueTasks: 0,      // 超时任务
  totalManDays: 0,      // 总人天
  departmentStats: [],  // 部门统计
  weeklyTrend: [],      // 本周趋势
  userTaskStats: []     // 个人任务统计
})

const recentTasks = ref([])
const recentAlerts = ref([])
const overdueTasks = ref([])

// 饼图配置
const pieChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '任务状态',
      type: 'pie',
      radius: '50%',
      data: [
        { value: stats.value.plannedTasks || 0, name: '计划中' },
        { value: stats.value.inProgressTasks || 0, name: '进行中' },
        { value: stats.value.completedTasks || 0, name: '已完成' },
        { value: stats.value.onHoldTasks || 0, name: '已暂停' },
        { value: stats.value.cancelledTasks || 0, name: '已取消' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
}))

// 部门任务分布图配置
const departmentChartOption = computed(() => {
  // 从stats中获取部门数据，如果没有则使用默认数据
  const departmentData = stats.value.departmentStats || [
    { value: 0, name: '运营商' },
    { value: 0, name: '创新业务' },
    { value: 0, name: 'RedteaReady' },
    { value: 0, name: 'xSIM' },
    { value: 0, name: '车联网' }
  ]
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '部门任务分布',
        type: 'pie',
        radius: '50%',
        data: departmentData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
})

// 折线图配置
const lineChartOption = computed(() => {
  // 从stats中获取趋势数据，如果没有则使用默认数据
  const trendData = stats.value.weeklyTrend || [0, 0, 0, 0, 0, 0, 0]
  
  return {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '新增任务',
        type: 'line',
        data: trendData,
        smooth: true
      }
    ]
  }
})

// 个人任务统计柱状图配置
const userTaskChartOption = computed(() => {
  // 从stats中获取个人任务统计数据，如果没有则使用默认数据
  const userTaskData = stats.value.userTaskStats || [
    { name: '张三', value: 0 },
    { name: '李四', value: 0 },
    { name: '王五', value: 0 }
  ]

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: userTaskData.map(item => item.name),
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '任务数量',
        type: 'bar',
        barWidth: '60%',
        data: userTaskData.map(item => item.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
})

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    PENDING: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    PENDING: '待处理',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('MM-DD HH:mm')
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取提醒类型
const getAlertType = (level) => {
  const types = {
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'info'
  }
  return types[level] || 'info'
}

// 获取提醒级别文本
const getAlertLevelText = (level) => {
  const texts = {
    'HIGH': '高',
    'MEDIUM': '中',
    'LOW': '低'
  }
  return texts[level] || level
}

// 加载数据
const loadData = async () => {
  try {
    // 检查认证状态
    const authStore = useAuthStore()
    console.log('当前用户:', authStore.user)
    console.log('Token存在:', !!authStore.token)
    
    // 先更新超时任务状态
    try {
      await request.post('/tasks/update-overdue-status')
    } catch (error) {
      console.error('更新超时状态失败:', error)
    }
    
    // 加载任务统计
    const taskStats = await getTaskStats()
    if (taskStats) {
      stats.value = {
        totalTasks: taskStats.totalTasks || 0,
        plannedTasks: taskStats.plannedTasks || 0,
        inProgressTasks: taskStats.inProgressTasks || 0,
        completedTasks: taskStats.completedTasks || 0,
        onHoldTasks: taskStats.onHoldTasks || 0,
        cancelledTasks: taskStats.cancelledTasks || 0,
        overdueTasks: taskStats.overdueTasks || 0,
        totalManDays: taskStats.totalManDays || 0,
        departmentStats: taskStats.departmentStats || [],
        weeklyTrend: taskStats.weeklyTrend || [0, 0, 0, 0, 0, 0, 0],
        userTaskStats: taskStats.userTaskStats || [
          { name: '张三', value: 0 },
          { name: '李四', value: 0 },
          { name: '王五', value: 0 }
        ]
      }
    }
    
    // 加载最近任务
    try {
      const tasksResponse = await getTasks({ page: 0, size: 5 })
      recentTasks.value = tasksResponse?.content || []
    } catch (error) {
      console.error('加载最近任务失败:', error)
      recentTasks.value = []
    }
    
    // 加载最近提醒
    try {
      const alertsResponse = await getAlerts({ page: 0, size: 5 })
      recentAlerts.value = alertsResponse?.content || []
    } catch (error) {
      console.error('加载最近提醒失败:', error)
      recentAlerts.value = []
    }

    // 加载超时任务 - 直接从任务列表中获取超时任务
    try {
      const tasksResponse = await request.get('/tasks', { params: { size: 1000 } })
      if (tasksResponse && tasksResponse.content) {
        // 过滤出超时任务，并按创建时间排序，取前10个
        const overdueTasksList = tasksResponse.content
          .filter(task => task.isOverdue)
          .sort((a, b) => new Date(b.createdTime) - new Date(a.createdTime))
          .slice(0, 10)
          .map(task => ({
            id: task.id,
            taskName: task.taskName,
            assignedToName: task.assignedToName,
            status: task.status
          }))
        overdueTasks.value = overdueTasksList
      } else {
        overdueTasks.value = []
      }
    } catch (error) {
      console.error('加载超时任务失败:', error)
      overdueTasks.value = []
    }

    // 加载个人任务统计
    try {
      const userTaskStatsData = await request.get('/tasks/statistics/user-tasks')
      stats.value.userTaskStats = userTaskStatsData || []
    } catch (error) {
      console.error('加载个人任务统计失败:', error)
      stats.value.userTaskStats = []
    }
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  height: 120px;
}

.stats-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.stats-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.in-progress {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.completed {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stats-info {
  flex: 1;
}

.stats-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart-container {
  height: 300px;
}

.lists-row {
  margin-bottom: 20px;
}

.list-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-list {
  max-height: 300px;
  overflow-y: auto;
}

.alert-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-item.unread {
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 12px;
  margin: 0 -12px;
}

.alert-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.alert-message {
  color: #666;
  font-size: 14px;
  margin-bottom: 4px;
}

.alert-time {
  color: #999;
  font-size: 12px;
}

.task-list {
  max-height: 300px;
  overflow-y: auto;
}

.task-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.task-item:last-child {
  border-bottom: none;
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.task-name {
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  flex: 1;
  word-break: break-word;
}

.status-tag {
  flex-shrink: 0;
}

.overdue-tag {
  flex-shrink: 0;
  font-weight: bold;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.assignee-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
}

.assignee-info .el-icon {
  font-size: 12px;
  color: #999;
}

.task-time {
  color: #999;
  font-size: 11px;
}

.status-info {
  display: flex;
  align-items: center;
}

.task-item {
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.task-item:hover {
  border-color: #d9d9d9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.task-item.overdue {
  border-left: 4px solid #f56c6c;
  background-color: #fef0f0;
}

.task-item.overdue:hover {
  background-color: #fde2e2;
}
</style> 