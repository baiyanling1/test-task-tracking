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
            <div class="stats-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.onHoldTasks }}</div>
              <div class="stats-label">已暂停</div>
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

    <!-- 未活跃用户统计区域 - 仅管理员和经理可见 -->
    <el-row v-if="canViewInactiveUsers()" :gutter="20" class="inactive-users-row">
      <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
        <el-card class="inactive-users-card">
          <template #header>
            <div class="card-header">
              <span>未填写任务统计</span>
              <div class="date-range-selector">
                <el-radio-group v-model="inactiveUsersTimeRange" @change="onTimeRangeChange">
                  <el-radio-button label="lastWeek">上周</el-radio-button>
                  <el-radio-button label="custom">自定义</el-radio-button>
                </el-radio-group>
                <div v-if="inactiveUsersTimeRange === 'custom'" class="custom-date-range">
                  <el-date-picker
                    v-model="customDateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    @change="onCustomDateRangeChange"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    size="small"
                    style="margin-left: 10px;"
                  />
                </div>
              </div>
            </div>
          </template>
          
          <div class="inactive-users-content">
            <div class="inactive-users-summary">
              <el-row :gutter="20">
                <el-col :span="6">
                  <div class="summary-item">
                    <div class="summary-number">{{ inactiveUsersStats.totalUsers || 0 }}</div>
                    <div class="summary-label">总用户数</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="summary-item">
                    <div class="summary-number inactive-count">{{ inactiveUsersStats.inactiveCount || 0 }}</div>
                    <div class="summary-label">未活跃用户</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="summary-item">
                    <div class="summary-number">{{ ((inactiveUsersStats.totalUsers - inactiveUsersStats.inactiveCount) || 0) }}</div>
                    <div class="summary-label">活跃用户</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="summary-item">
                    <div class="summary-number">{{ inactiveUsersStats.totalUsers > 0 ? Math.round((inactiveUsersStats.totalUsers - inactiveUsersStats.inactiveCount) / inactiveUsersStats.totalUsers * 100) : 0 }}%</div>
                    <div class="summary-label">活跃率</div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <div v-if="inactiveUsersStats.inactiveUsers && inactiveUsersStats.inactiveUsers.length > 0" class="inactive-users-list">
              <el-table :data="inactiveUsersStats.inactiveUsers" style="width: 100%">
                <el-table-column prop="realName" label="姓名" min-width="100" />
                <el-table-column prop="username" label="用户名" min-width="120" />
                <el-table-column prop="department" label="部门" min-width="120" />
                <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
                <el-table-column prop="assignedTaskCount" label="分配任务数" width="120" align="center">
                  <template #default="scope">
                    <el-tag :type="scope.row.assignedTaskCount > 0 ? 'warning' : 'info'">
                      {{ scope.row.assignedTaskCount }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="100" align="center">
                  <template #default="scope">
                    <el-tag type="danger">未活跃</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div v-else class="no-inactive-users">
              <el-empty description="该时间段内所有用户都有活动记录" />
            </div>

            <div v-if="inactiveUsersStats.startDate && inactiveUsersStats.endDate" class="time-range-info">
              <span class="time-range-text">
                检查时间范围：{{ inactiveUsersStats.startDate }} 至 {{ inactiveUsersStats.endDate }}
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

         <!-- 个人任务统计 -->
     <el-row :gutter="20" class="lists-row">
       <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
         <el-card class="list-card">
           <template #header>
             <div class="card-header">
               <el-tabs v-model="activePersonalTab" class="personal-stats-tabs">
                 <el-tab-pane label="本月任务" name="currentMonth" />
                 <el-tab-pane label="上月任务" name="lastMonth" />
               </el-tabs>
             </div>
           </template>
           <div class="personal-stats-container">
             <div v-if="activePersonalTab === 'currentMonth'" class="chart-container">
               <v-chart :option="currentMonthUserTaskChartOption" style="height: 300px" />
             </div>
             <div v-else-if="activePersonalTab === 'lastMonth'" class="chart-container">
               <v-chart :option="lastMonthUserTaskChartOption" style="height: 300px" />
             </div>
           </div>
         </el-card>
       </el-col>
     </el-row>

    <!-- 近6个月工时统计 -->
    <el-row :gutter="20" class="lists-row">
      <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
        <el-card class="list-card">
          <template #header>
            <div class="card-header">
              <span>近6个月工时统计</span>
            </div>
          </template>
          <div class="monthly-man-days-container">
            <el-tabs v-model="activeMonthlyTab" class="monthly-tabs">
              <el-tab-pane 
                v-for="month in monthlyManDaysData" 
                :key="month.month" 
                :label="month.monthName" 
                :name="month.month"
              />
            </el-tabs>
            <div class="chart-container">
              <v-chart :key="chartKey" :option="monthlyManDaysChartOption" style="height: 400px" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
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
import { getTaskStats, getTasks, getMonthlyManDaysStats, getLastWeekInactiveUsers, getInactiveUsersByDateRange } from '@/api/tasks'
import { getAlerts } from '@/api/alerts'
import request from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import { User, Warning, List, Loading, Check, Clock } from '@element-plus/icons-vue'

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
const activePersonalTab = ref('currentMonth')
const currentMonthUserTaskStats = ref([])
const lastMonthUserTaskStats = ref([])

// 近6个月工时统计相关数据
const activeMonthlyTab = ref('')
const monthlyManDaysData = ref([])
const chartKey = ref(0) // 用于强制刷新图表

// 未活跃用户统计相关数据
const inactiveUsersStats = ref({
  totalUsers: 0,
  inactiveUsers: [],
  inactiveCount: 0,
  startDate: '',
  endDate: ''
})
const inactiveUsersTimeRange = ref('lastWeek')
const customDateRange = ref([])

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

// 本月个人任务统计柱状图配置
const currentMonthUserTaskChartOption = computed(() => {
  const userTaskData = currentMonthUserTaskStats.value || []
  
  if (userTaskData.length === 0) {
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [{ name: '任务数量', type: 'bar', data: [] }]
    }
  }

  // 使用分组柱状图，每个用户显示4种状态
  const xAxisData = userTaskData.map(item => item.name)
  const completedData = userTaskData.map(item => item.completed || 0)
  const inProgressData = userTaskData.map(item => item.inProgress || 0)
  const onHoldData = userTaskData.map(item => item.onHold || 0)
  const plannedData = userTaskData.map(item => item.planned || 0)

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['已完成', '进行中', '计划中', '暂停'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '20%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisTick: { 
        alignWithLabel: true
      },
      axisLabel: {
        fontSize: 10,
        rotate: 45,
        interval: 0
      }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '已完成',
        type: 'bar',
        data: completedData,
        itemStyle: { color: '#67C23A' },
        barWidth: '8%'
      },
      {
        name: '进行中',
        type: 'bar',
        data: inProgressData,
        itemStyle: { color: '#E6A23C' },
        barWidth: '8%'
      },
      {
        name: '计划中',
        type: 'bar',
        data: plannedData,
        itemStyle: { color: '#909399' },
        barWidth: '8%'
      },
      {
        name: '暂停',
        type: 'bar',
        data: onHoldData,
        itemStyle: { color: '#F56C6C' },
        barWidth: '8%'
      }
    ]
  }
})

// 上月个人任务统计柱状图配置
const lastMonthUserTaskChartOption = computed(() => {
  const userTaskData = lastMonthUserTaskStats.value || []
  
  if (userTaskData.length === 0) {
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [{ name: '任务数量', type: 'bar', data: [] }]
    }
  }

  // 使用分组柱状图，每个用户显示4种状态
  const xAxisData = userTaskData.map(item => item.name)
  const completedData = userTaskData.map(item => item.completed || 0)
  const inProgressData = userTaskData.map(item => item.inProgress || 0)
  const onHoldData = userTaskData.map(item => item.onHold || 0)
  const plannedData = userTaskData.map(item => item.planned || 0)

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['已完成', '进行中', '计划中', '暂停'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '20%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisTick: { 
        alignWithLabel: true
      },
      axisLabel: {
        fontSize: 10,
        rotate: 45,
        interval: 0
      }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '已完成',
        type: 'bar',
        data: completedData,
        itemStyle: { color: '#67C23A' },
        barWidth: '8%'
      },
      {
        name: '进行中',
        type: 'bar',
        data: inProgressData,
        itemStyle: { color: '#E6A23C' },
        barWidth: '8%'
      },
      {
        name: '计划中',
        type: 'bar',
        data: plannedData,
        itemStyle: { color: '#909399' },
        barWidth: '8%'
      },
      {
        name: '暂停',
        type: 'bar',
        data: onHoldData,
        itemStyle: { color: '#F56C6C' },
        barWidth: '8%'
      }
    ]
  }
})

// 近6个月工时统计柱状图配置
const monthlyManDaysChartOption = computed(() => {
  const currentMonthData = monthlyManDaysData.value.find(month => month.month === activeMonthlyTab.value)
  
  console.log('当前月份数据:', currentMonthData)
  
  if (!currentMonthData || !currentMonthData.users || currentMonthData.users.length === 0) {
    console.log('没有找到当前月份数据或用户数据为空')
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [{ name: '工时(人天)', type: 'bar', data: [] }]
    }
  }

  const xAxisData = currentMonthData.users.map(user => user.userName)
  const manDaysData = currentMonthData.users.map(user => user.manDays)

  const chartOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function(params) {
        const data = params[0]
        return `${data.name}<br/>工时: ${data.value} 人天`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '40%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisTick: { 
        alignWithLabel: true,
        show: true
      },
      axisLabel: {
        fontSize: 11,
        rotate: 60,
        interval: 0,
        show: true,
        textStyle: {
          color: '#333'
        },
        margin: 20
      },
      axisLine: {
        show: true
      }
    },
    yAxis: { 
      type: 'value',
      name: '工时(人天)',
      nameLocation: 'middle',
      nameGap: 40,
      axisLabel: {
        show: true,
        textStyle: {
          color: '#333'
        }
      },
      axisLine: {
        show: true
      },
      axisTick: {
        show: true
      }
    },
    series: [
      {
        name: '工时(人天)',
        type: 'bar',
        data: manDaysData,
        itemStyle: { 
          color: function(params) {
            // 根据工时值设置不同颜色
            const value = params.value
            if (value >= 20) return '#67C23A' // 绿色 - 高工时
            if (value >= 10) return '#E6A23C' // 橙色 - 中等工时
            return '#909399' // 灰色 - 低工时
          }
        },
        barWidth: '25%',
        label: {
          show: true,
          position: 'top',
          formatter: '{c}'
        }
      }
    ]
  }
  
  console.log('图表配置:', chartOption)
  return chartOption
})

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    PLANNED: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    ON_HOLD: 'danger',
    CANCELLED: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    PLANNED: '计划中',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    ON_HOLD: '已暂停',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

// 格式化时间
const formatTime = (time) => {
  return dayjs.utc(time).tz('Asia/Shanghai').format('MM-DD HH:mm')
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
      console.log('最近任务API响应:', tasksResponse)
      recentTasks.value = tasksResponse?.content || []
      console.log('最近任务数据:', recentTasks.value)
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

    // 加载本月个人任务统计
    try {
      const currentMonthData = await request.get('/tasks/statistics/user-tasks-by-month', {
        params: { month: 'current' }
      })
      currentMonthUserTaskStats.value = currentMonthData || []
    } catch (error) {
      console.error('加载本月个人任务统计失败:', error)
      currentMonthUserTaskStats.value = []
    }

    // 加载上月个人任务统计
    try {
      const lastMonthData = await request.get('/tasks/statistics/user-tasks-by-month', {
        params: { month: 'last' }
      })
      lastMonthUserTaskStats.value = lastMonthData || []
    } catch (error) {
      console.error('加载上月个人任务统计失败:', error)
      lastMonthUserTaskStats.value = []
    }

    // 加载近6个月工时统计
    try {
      const monthlyManDaysResponse = await getMonthlyManDaysStats()
      console.log('近6个月工时统计API响应:', monthlyManDaysResponse)
      if (monthlyManDaysResponse && monthlyManDaysResponse.monthlyData) {
        monthlyManDaysData.value = monthlyManDaysResponse.monthlyData
        console.log('近6个月工时统计数据:', monthlyManDaysData.value)
        // 设置默认选中的月份为当前月
        if (monthlyManDaysData.value.length > 0) {
          activeMonthlyTab.value = monthlyManDaysData.value[monthlyManDaysData.value.length - 1].month
          console.log('设置默认选中月份:', activeMonthlyTab.value)
          // 强制刷新图表
          chartKey.value++
        }
      }
    } catch (error) {
      console.error('加载近6个月工时统计失败:', error)
      monthlyManDaysData.value = []
    }
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
  }
}

// 时间范围变化处理
const onTimeRangeChange = () => {
  if (!canViewInactiveUsers()) return
  
  if (inactiveUsersTimeRange.value === 'lastWeek') {
    loadInactiveUsersStats()
  } else {
    // 自定义时间范围时，等待用户选择日期
    if (customDateRange.value && customDateRange.value.length === 2) {
      loadInactiveUsersStatsByRange()
    }
  }
}

// 自定义日期范围变化处理
const onCustomDateRangeChange = () => {
  if (!canViewInactiveUsers()) return
  
  if (customDateRange.value && customDateRange.value.length === 2) {
    loadInactiveUsersStatsByRange()
  }
}

// 加载未活跃用户统计（上周）
const loadInactiveUsersStats = async () => {
  try {
    const response = await getLastWeekInactiveUsers()
    inactiveUsersStats.value = response || {
      totalUsers: 0,
      inactiveUsers: [],
      inactiveCount: 0,
      startDate: '',
      endDate: ''
    }
    console.log('上周未活跃用户统计:', inactiveUsersStats.value)
  } catch (error) {
    console.error('加载上周未活跃用户统计失败:', error)
    inactiveUsersStats.value = {
      totalUsers: 0,
      inactiveUsers: [],
      inactiveCount: 0,
      startDate: '',
      endDate: ''
    }
  }
}

// 加载指定时间范围的未活跃用户统计
const loadInactiveUsersStatsByRange = async () => {
  try {
    const [startDate, endDate] = customDateRange.value
    const response = await getInactiveUsersByDateRange(startDate, endDate)
    inactiveUsersStats.value = response || {
      totalUsers: 0,
      inactiveUsers: [],
      inactiveCount: 0,
      startDate: '',
      endDate: ''
    }
    console.log('自定义时间范围未活跃用户统计:', inactiveUsersStats.value)
  } catch (error) {
    console.error('加载自定义时间范围未活跃用户统计失败:', error)
    inactiveUsersStats.value = {
      totalUsers: 0,
      inactiveUsers: [],
      inactiveCount: 0,
      startDate: '',
      endDate: ''
    }
  }
}

// 检查用户是否可以查看未活跃用户统计
const canViewInactiveUsers = () => {
  const authStore = useAuthStore()
  const userRole = authStore.user?.role
  return userRole === 'ADMIN' || userRole === 'MANAGER'
}

// 监听月份切换，强制刷新图表
watch(activeMonthlyTab, () => {
  chartKey.value++
})

onMounted(() => {
  loadData()
  // 只有管理员和经理才加载未活跃用户统计
  if (canViewInactiveUsers()) {
    loadInactiveUsersStats()
  }
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

.stats-icon.overdue {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
}

.stats-icon.man-days {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
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

.no-tasks {
  text-align: center;
  padding: 40px 0;
}

.personal-stats-container {
  height: 100%;
}

.personal-stats-tabs {
  height: 100%;
}

.personal-stats-tabs .el-tabs__content {
  height: calc(100% - 40px);
}

.personal-stats-tabs .el-tab-pane {
  height: 100%;
}

/* 调整卡片头部的标签页样式 */
.card-header .el-tabs {
  margin: 0;
  width: 100%;
  position: relative;
}

.card-header .el-tabs__header {
  margin: 0;
  padding: 0;
  display: flex;
  justify-content: flex-end;
  position: absolute;
  right: 0;
  top: 0;
  width: auto;
}

.card-header .el-tabs__nav-wrap {
  padding: 0;
  display: flex;
  justify-content: flex-end;
  position: absolute;
  right: 0;
  top: 0;
  width: auto;
}

.card-header .el-tabs__nav {
  border: none;
  margin-left: auto;
  transform: translateX(-200px);
  position: absolute;
  right: 20px;
  top: 0;
}

.card-header .el-tabs__item {
  padding: 0 80px;
  height: 40px;
  line-height: 40px;
  font-size: 16px;
  font-weight: 500;
  margin-right: 20px;
}

.card-header .el-tabs__active-bar {
  background-color: #409EFF;
  height: 3px;
}

/* 近6个月工时统计样式 */
.monthly-man-days-container {
  height: 100%;
}

.monthly-tabs {
  margin-bottom: 20px;
}

.monthly-tabs .el-tabs__header {
  margin-bottom: 0;
}

.monthly-tabs .el-tabs__nav-wrap {
  padding: 0;
}

.monthly-tabs .el-tabs__item {
  padding: 0 20px;
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  font-weight: 500;
}

.monthly-tabs .el-tabs__active-bar {
  background-color: #409EFF;
  height: 3px;
}

/* 未活跃用户统计样式 */
.inactive-users-row {
  margin-bottom: 20px;
}

.inactive-users-card {
  min-height: 300px;
}

.inactive-users-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.date-range-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.custom-date-range {
  margin-left: 10px;
}

.inactive-users-content {
  padding: 10px 0;
}

.inactive-users-summary {
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f6f9fc 0%, #e9f3ff 100%);
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.summary-item {
  text-align: center;
  padding: 10px;
}

.summary-number {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  line-height: 1;
  margin-bottom: 8px;
}

.summary-number.inactive-count {
  color: #f56c6c;
}

.summary-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.inactive-users-list {
  margin-bottom: 15px;
}

.inactive-users-list .el-table {
  border-radius: 8px;
  overflow: hidden;
}

.inactive-users-list .el-table th {
  background-color: #f8f9fa;
  color: #333;
  font-weight: 600;
}

.no-inactive-users {
  padding: 40px 0;
  text-align: center;
}

.time-range-info {
  text-align: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  margin-top: 15px;
}

.time-range-text {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .inactive-users-card .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .date-range-selector {
    width: 100%;
    justify-content: flex-start;
  }
  
  .custom-date-range {
    margin-left: 0;
    margin-top: 10px;
  }
  
  .inactive-users-summary .el-col {
    margin-bottom: 10px;
  }
}
</style> 