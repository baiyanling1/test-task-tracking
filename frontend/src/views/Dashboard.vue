<template>
  <div class="dashboard">
    <!-- TAB页导航 -->
    <el-tabs v-model="activeTab" class="dashboard-tabs">
      <!-- 主仪表板TAB -->
      <el-tab-pane label="仪表板概览" name="overview">
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
                  <el-icon><WarningFilled /></el-icon>
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
                <div class="stats-icon total-days">
                  <el-icon><Calendar /></el-icon>
            </div>
            <div class="stats-info">
                  <div class="stats-number">{{ formatNumber(stats.totalManDays) }}</div>
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
      </el-tab-pane>

      <!-- 管理员和项目经理统计TAB页 -->
      <el-tab-pane v-if="canViewInactiveUsers()" label="管理统计" name="admin">
        <!-- 未活跃用户统计区域 -->
        <el-row :gutter="20" class="inactive-users-row">
      <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
        <el-card class="inactive-users-card">
          <template #header>
            <div class="card-header">
              <span>未填写任务统计</span>
                  <div class="time-range-selector">
                    <el-select v-model="inactiveUsersTimeRange" @change="loadInactiveUsersStats" size="small">
                      <el-option label="最近一周" value="lastWeek" />
                      <el-option label="自定义" value="custom" />
                    </el-select>
                  <el-date-picker
                      v-if="inactiveUsersTimeRange === 'custom'"
                    v-model="customDateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                      @change="loadInactiveUsersStats"
                    size="small"
                    style="margin-left: 10px;"
                  />
                    <el-text v-if="inactiveUsersTimeRange === 'lastWeek'" type="info" size="small" style="margin-left: 10px;">
                      默认显示上周数据
                    </el-text>
              </div>
            </div>
          </template>
          
              <div class="inactive-users-simple">
                <el-row :gutter="20" class="inactive-users-summary">
                  <el-col :span="8">
                  <div class="summary-item">
                      <div class="summary-number">{{ inactiveUsers.length }}</div>
                    <div class="summary-label">未活跃用户</div>
                  </div>
                </el-col>
                  <el-col :span="8">
                  <div class="summary-item">
                      <div class="summary-number">{{ stats.totalUsers || 0 }}</div>
                      <div class="summary-label">总用户数</div>
                  </div>
                </el-col>
                  <el-col :span="8">
                  <div class="summary-item">
                      <div class="summary-number">{{ Math.round((inactiveUsers.length / (stats.totalUsers || 1)) * 100) }}%</div>
                      <div class="summary-label">未活跃比例</div>
                  </div>
                </el-col>
              </el-row>
                
                <div v-if="inactiveUsers.length > 0" class="inactive-users-names">
                  <div class="names-label">未活跃用户列表：</div>
                  <div class="user-tags">
                    <el-tag 
                      v-for="user in inactiveUsers" 
                      :key="user.userId"
                      type="warning"
                      class="user-tag"
                    >
                      {{ user.realName || user.userName }} ({{ user.department || '未分配' }})
                    </el-tag>
                  </div>
            </div>

            <div v-else class="no-inactive-users">
                  <el-empty description="所有用户都很活跃！" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

        <!-- 人员任务统计区域 -->
        <el-row :gutter="20" class="user-task-stats-row">
          <el-col :span="24">
            <el-card class="task-stats-card">
              <template #header>
                <div class="card-header">
                  <span>测试人员任务统计</span>
                  <div class="time-range-selector">
                    <el-date-picker
                      v-model="taskStatsDateRange"
                      type="daterange"
                      range-separator="至"
                      start-placeholder="开始日期"
                      end-placeholder="结束日期"
                      @change="loadUserTaskStats"
                      size="small"
                      value-format="YYYY-MM-DD"
                    />
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="loadUserTaskStats"
                      :loading="loadingTaskStats"
                      style="margin-left: 10px;"
                    >
                      查询
                    </el-button>
                    <el-button 
                      size="small" 
                      @click="resetTaskStatsDateRange"
                      style="margin-left: 5px;"
                    >
                      重置
                    </el-button>
                  </div>
                </div>
              </template>
              
              <!-- 人员任务数柱状图 -->
              <div class="chart-section">
                <h4 class="section-subtitle">各测试人员任务数统计</h4>
                
                <!-- 加载中 -->
                <div v-if="loadingTaskStats" style="height: 400px; display: flex; align-items: center; justify-content: center;">
                  <el-icon class="is-loading" :size="40"><Loading /></el-icon>
                  <span style="margin-left: 10px; color: #409EFF;">加载中...</span>
                </div>
                
                <!-- 无数据 -->
                <div v-else-if="!userTaskStats || userTaskStats.length === 0" class="no-data">
                  <el-empty description="暂无数据" />
                </div>
                
                <!-- 有数据时显示图表 -->
                <div v-else style="width: 100%; height: 450px;">
                  <v-chart 
                    :option="userTaskCountChartOption" 
                    style="width: 100%; height: 100%;" 
                    autoresize
                  />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 人员工时统计区域 -->
        <el-row :gutter="20" class="user-work-hours-row" style="margin-top: 20px;">
          <el-col :span="24">
            <el-card class="task-stats-card">
              <template #header>
                <div class="card-header">
                  <span>测试人员工时统计</span>
                  <div class="time-range-selector">
                    <el-date-picker
                      v-model="workHoursDateRange"
                      type="daterange"
                      range-separator="至"
                      start-placeholder="开始日期"
                      end-placeholder="结束日期"
                      @change="loadUserWorkHoursStats"
                      size="small"
                      value-format="YYYY-MM-DD"
                    />
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="loadUserWorkHoursStats"
                      :loading="loadingWorkHours"
                      style="margin-left: 10px;"
                    >
                      查询
                    </el-button>
                    <el-button 
                      size="small" 
                      @click="resetWorkHoursDateRange"
                      style="margin-left: 5px;"
                    >
                      重置
                    </el-button>
                  </div>
                </div>
              </template>
              
              <!-- 人员实际工时统计柱状图 -->
              <div class="chart-section">
                <h4 class="section-subtitle">各测试人员实际工时统计</h4>
                
                <!-- 加载中 -->
                <div v-if="loadingWorkHours" style="height: 400px; display: flex; align-items: center; justify-content: center;">
                  <el-icon class="is-loading" :size="40"><Loading /></el-icon>
                  <span style="margin-left: 10px; color: #409EFF;">加载中...</span>
                </div>
                
                <!-- 无数据 -->
                <div v-else-if="!userWorkHoursStats || userWorkHoursStats.length === 0" class="no-data">
                  <el-empty description="暂无数据" />
                </div>
                
                <!-- 有数据时显示图表 -->
                <div v-else style="width: 100%; height: 450px;">
                  <v-chart 
                    :option="userWorkHoursChartOption" 
                    style="width: 100%; height: 100%;" 
                    autoresize
                  />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 人员工作统计区域 - 已屏蔽 -->
        <!-- <div class="admin-stats-section">
          <div class="admin-stats-container">
            <el-row :gutter="20" class="section-header">
              <el-col :span="24">
                <h3 class="section-title">
                  <el-icon><User /></el-icon>
                  人员工作统计
                </h3>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" class="workload-dashboard">
              <el-col :span="24">
                <el-card class="stats-card">
           <template #header>
             <div class="card-header">
                      <span>工作饱和度仪表板</span>
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="loadUsersWorkStats"
                        :loading="loadingUserStats"
                      >
                        刷新数据
                      </el-button>
             </div>
           </template>
                  
                  <div class="workload-grid">
                    <div v-if="usersWorkStats.length === 0" style="text-align: center; padding: 40px; color: #999;">
                      <p>暂无用户工作统计数据</p>
                      <el-button type="primary" @click="loadUsersWorkStats" :loading="loadingUserStats">
                        重新加载数据
                      </el-button>
             </div>
                    <div 
                      v-for="(user, index) in usersWorkStats" 
                      :key="user.userId || index"
                      class="user-workload-card"
                      :class="getWorkloadCardClass(user.workloadStatus)"
                    >
                      <div class="user-info">
                        <div class="user-name">{{ user.realName || user.userName }}</div>
                        <div class="user-dept">{{ user.department || '未分配部门' }}</div>
                      </div>
                      
                      <div class="workload-indicator">
                        <div class="workload-circle" :style="{ borderColor: user.workloadStatusColor }">
                          <span class="workload-percentage">{{ user.workloadUtilization }}%</span>
                        </div>
                        <div class="workload-status" :style="{ color: user.workloadStatusColor }">
                          {{ user.workloadStatusText }}
                        </div>
                      </div>
                      
                      <div class="workload-details">
                        <div class="detail-item">
                          <span class="label">进行中任务:</span>
                          <span class="value">{{ user.currentActiveTasks }}个</span>
                        </div>
                        <div class="detail-item">
                          <span class="label">按时完成率:</span>
                          <span class="value">{{ user.onTimeCompletionRate }}%</span>
                        </div>
                        <div class="detail-item">
                          <span class="label">工时:</span>
                          <span class="value">{{ user.totalManDays }}/{{ user.standardWorkDays }}天</span>
                        </div>
                      </div>
             </div>
           </div>
         </el-card>
       </el-col>
     </el-row>

            <el-row :gutter="20" class="detailed-stats">
              <el-col :span="24">
                <el-card class="stats-card">
          <template #header>
            <div class="card-header">
                      <span>详细工作统计 (数据条数: {{ usersWorkStats.length }})</span>
            </div>
          </template>
                  
                  <div v-if="usersWorkStats.length === 0" style="padding: 20px; text-align: center; color: #999;">
                    <p>暂无统计数据</p>
                    <el-button type="primary" @click="loadUsersWorkStats" :loading="loadingUserStats">
                      重新加载数据
                    </el-button>
            </div>
                  
                  <el-table 
                    :data="usersWorkStats" 
                    stripe 
                    class="stats-table"
                    :default-sort="{ prop: 'workloadUtilization', order: 'descending' }"
                    style="width: 100%"
                    :header-cell-style="{ 
                      textAlign: 'center', 
                      verticalAlign: 'middle', 
                      height: '50px',
                      lineHeight: '50px',
                      padding: '0',
                      backgroundColor: '#f8f9fa',
                      borderBottom: '1px solid #ebeef5'
                    }"
                    :cell-style="{ 
                      textAlign: 'center', 
                      verticalAlign: 'middle',
                      height: '60px',
                      padding: '12px 8px'
                    }"
                  >
                    <el-table-column prop="realName" label="姓名" width="80" align="center" />
                    <el-table-column prop="department" label="部门" width="120" align="center" />
                    <el-table-column prop="workloadUtilization" label="工时利用率" width="100" sortable align="center">
                      <template #default="{ row }">
                        <span :style="{ color: row.workloadStatusColor, fontWeight: 'bold' }">
                          {{ row.workloadUtilization }}%
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="workloadStatusText" label="工作状态" width="90" align="center">
                      <template #default="{ row }">
                        <el-tag 
                          :style="{ backgroundColor: row.workloadStatusColor, color: '#fff', border: 'none' }"
                          effect="dark"
                          size="small"
                        >
                          {{ row.workloadStatusText }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="currentActiveTasks" label="进行中任务" width="90" align="center" />
                    <el-table-column prop="onTimeCompletionRate" label="按时完成率" width="100" sortable align="center">
                      <template #default="{ row }">
                        {{ row.onTimeCompletionRate }}%
                      </template>
                    </el-table-column>
                    <el-table-column prop="avgDelayDays" label="平均延期天数" width="110" sortable align="center">
                      <template #default="{ row }">
                        {{ row.avgDelayDays }}天
                      </template>
                    </el-table-column>
                    <el-table-column prop="estimationAccuracy" label="预估准确度" width="100" sortable align="center">
                      <template #default="{ row }">
                        {{ row.estimationAccuracy }}%
                      </template>
                    </el-table-column>
                    <el-table-column label="任务分布" min-width="260" align="center">
                      <template #default="{ row }">
                        <div class="task-distribution-inline">
                          <el-tag size="small" type="info">计划: {{ row.plannedTasks }}</el-tag>
                          <el-tag size="small" type="warning">进行: {{ row.currentActiveTasks }}</el-tag>
                          <el-tag size="small" type="success">完成: {{ row.completedTasks }}</el-tag>
                          <el-tag size="small" type="danger">暂停: {{ row.onHoldTasks }}</el-tag>
          </div>
                      </template>
                    </el-table-column>
                  </el-table>
        </el-card>
      </el-col>
    </el-row>
          </div>
        </div> -->
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'
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
import { List, Loading, Check, Clock, WarningFilled, Calendar, User } from '@element-plus/icons-vue'
import request from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { ElMessage } from 'element-plus'
// import { getUsersWorkStats } from '@/api/userStats' // 已屏蔽

const authStore = useAuthStore()
const themeStore = useThemeStore()

// 获取当前主题的图表配色
const getChartColors = computed(() => {
  const theme = themeStore.getThemeConfig()
  return theme.colors.chartColors || ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
})

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

// 响应式数据
const stats = ref({
  totalTasks: 0,
  inProgressTasks: 0,
  completedTasks: 0,
  onHoldTasks: 0,
  overdueTasks: 0,
  totalManDays: 0,
  departmentStats: [],
  weeklyTrend: [],
  totalUsers: 0
})

const inactiveUsers = ref([])
const inactiveUsersStats = ref({})
const inactiveUsersTimeRange = ref('lastWeek')
const customDateRange = ref([])

// 用户任务统计相关数据
const userTaskStats = ref([])
const taskStatsDateRange = ref([])
const loadingTaskStats = ref(false)

// 用户工时统计相关数据
const userWorkHoursStats = ref([])
const workHoursDateRange = ref([])
const loadingWorkHours = ref(false)

// 用户工作统计相关数据 - 已屏蔽
// const usersWorkStats = ref([])
// const loadingUserStats = ref(false)

// TAB页控制
const activeTab = ref('overview')

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

// 本周任务趋势图配置
const lineChartOption = computed(() => {
  const weeklyData = stats.value.weeklyTrend || []
  
  // 生成本周的日期标签
  const today = new Date()
  const monday = new Date(today)
  monday.setDate(today.getDate() - today.getDay() + 1) // 获取本周一
  
  const dates = []
  const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  
  for (let i = 0; i < 7; i++) {
    dates.push(dayNames[i])
  }
  
  // 如果后端返回的是数字数组，直接使用
  const taskCounts = Array.isArray(weeklyData) && typeof weeklyData[0] === 'number' 
    ? weeklyData 
    : weeklyData.map(item => item.created || 0)

  return {
    title: {
      text: ''
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        let result = params[0].name + '<br/>'
        params.forEach(param => {
          result += param.marker + param.seriesName + ': ' + param.value + '个<br/>'
        })
        return result
      }
    },
    legend: {
      data: ['任务数量']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: { 
      type: 'value',
      name: '任务数'
    },
    series: [
      {
        name: '任务数量',
        type: 'line',
        data: taskCounts,
        smooth: true,
        itemStyle: { 
          color: '#409EFF'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(64, 158, 255, 0.6)'
            }, {
              offset: 1, color: 'rgba(64, 158, 255, 0.1)'
            }]
          }
        }
      }
    ]
  }
})

// 用户任务数柱状图配置
const userTaskCountChartOption = computed(() => {
  const userData = userTaskStats.value || []
  
  return {
    title: {
      text: ''
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        let result = params[0].name + '<br/>'
        params.forEach(param => {
          result += param.marker + param.seriesName + ': ' + param.value + '个<br/>'
        })
        return result
      }
    },
    legend: {
      data: ['任务总数', '已完成', '进行中', '计划中']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: userData.map(user => user.realName || user.userName),
      axisLabel: {
        interval: 0,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '任务数'
    },
    series: [
      {
        name: '任务总数',
        type: 'bar',
        data: userData.map(user => user.totalTasks || 0),
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '已完成',
        type: 'bar',
        data: userData.map(user => user.completedTasks || 0),
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '进行中',
        type: 'bar',
        data: userData.map(user => user.inProgressTasks || 0),
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '计划中',
        type: 'bar',
        data: userData.map(user => user.plannedTasks || 0),
        itemStyle: { color: '#909399' }
      }
    ]
  }
})

// 用户工时统计柱状图配置
const userWorkHoursChartOption = computed(() => {
  const userData = userWorkHoursStats.value || []
  
  return {
    title: {
      text: ''
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        let result = params[0].name + '<br/>'
        params.forEach(param => {
          result += param.marker + param.seriesName + ': ' + param.value + '人天<br/>'
        })
        return result
      }
    },
    legend: {
      data: ['实际工时', '预计工时']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: userData.map(user => user.realName || user.userName),
      axisLabel: {
        interval: 0,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '工时(人天)'
    },
    series: [
      {
        name: '实际工时',
        type: 'bar',
        data: userData.map(user => user.totalActualManDays ? user.totalActualManDays.toFixed(1) : 0),
        itemStyle: { color: '#409EFF' },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}'
        }
      },
      {
        name: '预计工时',
        type: 'bar',
        data: userData.map(user => user.totalPlannedManDays ? user.totalPlannedManDays.toFixed(1) : 0),
        itemStyle: { color: '#E6A23C' },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}'
        }
      }
    ]
  }
})

// 工具函数
const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  return parseFloat(num.toFixed(1)).toString()
}

const isAdmin = () => {
  return authStore.user?.role === 'ADMIN'
}

const canViewInactiveUsers = () => {
  const userRole = authStore.user?.role
  return userRole === 'ADMIN' || userRole === 'MANAGER'
}

// 工时完成率标签类型
const getWorkHoursRateType = (rate) => {
  if (!rate) return 'info'
  if (rate >= 90) return 'success'
  if (rate >= 70) return 'warning'
  return 'danger'
}

// 数据加载函数
const loadInactiveUsersStats = async () => {
  if (!canViewInactiveUsers()) return
  
  try {
    let response;
    
    // 如果是自定义时间范围
    if (inactiveUsersTimeRange.value === 'custom' && customDateRange.value && customDateRange.value.length === 2) {
      // 格式化日期为 YYYY-MM-DD 格式
      const startDate = customDateRange.value[0].toISOString().split('T')[0];
      const endDate = customDateRange.value[1].toISOString().split('T')[0];
      response = await request.get('/dashboard/inactive-users/range', { 
        params: { startDate, endDate } 
      });
    } 
    // 其他时间范围使用默认接口（只支持上周）
    else {
      response = await request.get('/dashboard/inactive-users');
    }
    
    // 处理返回数据格式并保存totalUsers
    if (response.inactiveUsers) {
      // 直接返回的格式
      inactiveUsers.value = response.inactiveUsers || [];
      inactiveUsersStats.value = response || {};
    } else if (response.users) {
      // 包装后的格式
      inactiveUsers.value = response.users || [];
      inactiveUsersStats.value = response.stats || {};
    } else {
      inactiveUsers.value = [];
      inactiveUsersStats.value = {};
    }
    
    // 如果返回了totalUsers，更新到stats中
    if (response.totalUsers) {
      stats.value.totalUsers = response.totalUsers;
      }
    } catch (error) {
    console.error('加载未活跃用户统计失败:', error)
    inactiveUsers.value = []
    inactiveUsersStats.value = {}
  }
}

const loadDashboardStats = async () => {
  try {
    const response = await request.get('/tasks/stats')
    if (response) {
      stats.value = {
        totalTasks: response.totalTasks || 0,
        inProgressTasks: response.inProgressTasks || 0,
        completedTasks: response.completedTasks || 0,
        onHoldTasks: response.onHoldTasks || 0,
        overdueTasks: response.overdueTasks || 0,
        totalManDays: response.totalManDays || 0,
        departmentStats: response.departmentStats || [],
        weeklyTrend: response.weeklyTrend || [],
        totalUsers: response.totalUsers || 0
      }
    }
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
  }
}

// 加载用户任务统计数据
const loadUserTaskStats = async () => {
  if (!canViewInactiveUsers()) return
  
  loadingTaskStats.value = true
  try {
    let params = {}
    
    // 如果选择了时间范围
    if (taskStatsDateRange.value && taskStatsDateRange.value.length === 2) {
      params.startDate = taskStatsDateRange.value[0]
      params.endDate = taskStatsDateRange.value[1]
    }
    
    const response = await request.get('/dashboard/user-task-stats', { params })
    
    if (response && Array.isArray(response)) {
      // 处理数据，计算工时完成率
      userTaskStats.value = response.map(user => ({
        ...user,
        workHoursRate: user.totalPlannedManDays > 0 
          ? Math.round((user.totalActualManDays / user.totalPlannedManDays) * 100)
          : 0
      }))
      
      // 等待DOM更新
      await nextTick()
    } else {
      userTaskStats.value = []
    }
  } catch (error) {
    console.error('加载用户任务统计失败:', error)
    ElMessage.error('加载用户任务统计失败')
    userTaskStats.value = []
  } finally {
    loadingTaskStats.value = false
  }
}

// 重置任务统计时间范围
const resetTaskStatsDateRange = () => {
  taskStatsDateRange.value = []
  loadUserTaskStats()
}

// 加载用户工时统计数据
const loadUserWorkHoursStats = async () => {
  if (!canViewInactiveUsers()) return
  
  loadingWorkHours.value = true
  try {
    let params = {}
    
    // 如果选择了时间范围
    if (workHoursDateRange.value && workHoursDateRange.value.length === 2) {
      params.startDate = workHoursDateRange.value[0]
      params.endDate = workHoursDateRange.value[1]
    }
    
    const response = await request.get('/dashboard/user-task-stats', { params })
    
    if (response && Array.isArray(response)) {
      // 处理数据，计算工时完成率
      userWorkHoursStats.value = response.map(user => ({
        ...user,
        workHoursRate: user.totalPlannedManDays > 0 
          ? Math.round((user.totalActualManDays / user.totalPlannedManDays) * 100)
          : 0
      }))
      
      // 等待DOM更新
      await nextTick()
    } else {
      userWorkHoursStats.value = []
    }
  } catch (error) {
    console.error('加载用户工时统计失败:', error)
    ElMessage.error('加载用户工时统计失败')
    userWorkHoursStats.value = []
  } finally {
    loadingWorkHours.value = false
  }
}

// 重置工时统计时间范围
const resetWorkHoursDateRange = () => {
  workHoursDateRange.value = []
  loadUserWorkHoursStats()
}

// 加载用户工作统计数据 - 已屏蔽
/*
const loadUsersWorkStats = async () => {
  if (!isAdmin()) return
  
  loadingUserStats.value = true
  try {
    const response = await getUsersWorkStats()
    
    // 处理数据，确保每个用户都有必要的显示属性
    const processedData = (response || []).map(user => ({
      ...user,
      workloadStatusText: user.workloadStatusText || getWorkloadStatusText(user.workloadStatus),
      workloadStatusColor: user.workloadStatusColor || getWorkloadStatusColor(user.workloadStatus)
    }))
    
    usersWorkStats.value = processedData
    } catch (error) {
    console.error('加载用户工作统计失败:', error)
    ElMessage.error('加载用户工作统计失败')
  } finally {
    loadingUserStats.value = false
  }
}

// 获取工作负载状态文本
const getWorkloadStatusText = (status) => {
  const statusMap = {
    'OVERLOADED': '过载',
    'SATURATED': '饱和', 
    'NORMAL': '正常',
    'IDLE': '空闲'
  }
  return statusMap[status] || '未知'
}

// 获取工作负载状态颜色
const getWorkloadStatusColor = (status) => {
  const colorMap = {
    'OVERLOADED': '#f56565', // 红色
    'SATURATED': '#ed8936',  // 橙色
    'NORMAL': '#48bb78',     // 绿色
    'IDLE': '#4299e1'        // 蓝色
  }
  return colorMap[status] || '#a0aec0'
}

// 获取工作负载卡片样式类
const getWorkloadCardClass = (status) => {
  return {
    'workload-overloaded': status === 'OVERLOADED',
    'workload-saturated': status === 'SATURATED', 
    'workload-normal': status === 'NORMAL',
    'workload-idle': status === 'IDLE'
  }
}
*/

// 页面挂载时加载数据
onMounted(async () => {
  await loadDashboardStats()
  
  // 管理员和项目经理可以加载统计功能
  if (canViewInactiveUsers()) {
    loadInactiveUsersStats()
    loadUserTaskStats() // 加载用户任务统计
    loadUserWorkHoursStats() // 加载用户工时统计
    // loadUsersWorkStats() // 已屏蔽
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
  position: relative;
  min-height: 100vh;
}

/* TAB页样式 */
.dashboard-tabs {
  margin-bottom: 20px;
}

.dashboard-tabs .el-tabs__header {
  margin: 0 0 20px 0;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 10px 20px 0;
}

.dashboard-tabs .el-tabs__nav-wrap {
  padding: 0;
}

.dashboard-tabs .el-tabs__item {
  font-size: 16px;
  font-weight: 500;
  padding: 0 30px;
  height: 50px;
  line-height: 50px;
}

.dashboard-tabs .el-tabs__item.is-active {
  color: #409EFF;
  font-weight: 600;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  height: 120px;
  position: relative;
  z-index: 1;
}

/* 任务统计卡片 - 不限制高度 */
.task-stats-card {
  position: relative;
  z-index: 1;
  margin-bottom: 20px;
}

.stats-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 10px;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
  font-size: 24px;
}

.stats-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.in-progress {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.completed {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.pending {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stats-icon.overdue {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stats-icon.total-days {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.stats-info {
  flex: 1;
}

.stats-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 5px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.time-range-selector {
  display: flex;
  align-items: center;
}

/* 未活跃用户统计样式 */
.inactive-users-row {
  margin-bottom: 30px;
}

.inactive-users-card {
  min-height: 200px;
}

/* 用户任务统计样式 */
.user-task-stats-row {
  margin-bottom: 30px;
}

.chart-section {
  margin-bottom: 40px;
}

.table-section {
  margin-top: 40px;
}

.section-subtitle {
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
}

.no-data {
  padding: 40px 0;
  text-align: center;
}

.work-hours-table {
  margin-top: 20px;
}

/* 未活跃用户统计样式 - 继续 */

.inactive-users-simple {
  padding: 20px 0;
}

.inactive-users-summary {
  margin-bottom: 20px;
  text-align: center;
}

.summary-item {
  padding: 20px;
  border-radius: 8px;
  background: #f8f9fa;
}

.summary-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.summary-label {
  font-size: 14px;
  color: #606266;
}

.inactive-users-names {
  margin-top: 20px;
}

.names-label {
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.user-tag {
  margin: 2px;
}

.no-inactive-users {
  padding: 40px 0;
  text-align: center;
}

/* Admin专用：用户工作统计样式 */
.admin-stats-section {
  margin-top: 30px;
  position: relative;
  z-index: 1;
  clear: both;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

.admin-stats-container {
  width: 100%;
  background: transparent;
  position: relative;
  z-index: 2;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title .el-icon {
  color: #409EFF;
}

/* 工作饱和度仪表板样式 */
.workload-dashboard {
  margin-bottom: 20px;
  position: relative;
  z-index: 2;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  max-height: none !important;
  overflow: visible !important;
  height: auto !important;
}

.workload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  padding: 16px 0;
  position: relative;
  z-index: 3;
  max-height: none !important;
  overflow: visible !important;
  height: auto !important;
}

.user-workload-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 4;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

.user-workload-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.user-workload-card.workload-overloaded {
  border-left: 4px solid #f56565;
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
}

.user-workload-card.workload-saturated {
  border-left: 4px solid #ed8936;
  background: linear-gradient(135deg, #fffaf0 0%, #ffffff 100%);
}

.user-workload-card.workload-normal {
  border-left: 4px solid #48bb78;
  background: linear-gradient(135deg, #f0fff4 0%, #ffffff 100%);
}

.user-workload-card.workload-idle {
  border-left: 4px solid #4299e1;
  background: linear-gradient(135deg, #ebf8ff 0%, #ffffff 100%);
}

.user-info {
  margin-bottom: 12px;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.user-dept {
  font-size: 12px;
  color: #909399;
}

.workload-indicator {
  text-align: center;
  margin-bottom: 16px;
}

.workload-circle {
  width: 80px;
  height: 80px;
  border: 4px solid #e4e7ed;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.workload-percentage {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.workload-status {
  font-size: 14px;
  font-weight: 600;
}

.workload-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item .label {
  font-size: 12px;
  color: #909399;
}

.detail-item .value {
  color: #303133;
  font-weight: 500;
}

/* 详细统计表格样式 */
.detailed-stats {
  margin-bottom: 20px;
  position: relative;
  z-index: 2;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  max-height: none !important;
  overflow: visible !important;
  height: auto !important;
}

.stats-table {
  font-size: 13px;
  position: relative;
  z-index: 3;
  visibility: visible !important;
  opacity: 1 !important;
  width: 100%;
}

.stats-table .el-table__header-wrapper {
  overflow: visible;
}

/* 强制表头对齐 */
.stats-table .el-table__header {
  table-layout: fixed;
}

.stats-table .el-table__header tr {
  height: 50px !important;
}

.stats-table .el-table__header th {
  background-color: #f8f9fa;
  color: #333;
  font-weight: 600;
  text-align: center;
  vertical-align: middle;
  height: 50px !important;
  line-height: 50px;
  padding: 0 !important;
  border-bottom: 1px solid #ebeef5;
  position: relative;
}

/* 表头单元格基础样式 */
.stats-table .el-table__header th .cell {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 50px !important;
  line-height: normal !important;
  padding: 0 8px !important;
  white-space: nowrap;
  position: relative;
}

/* 完全重写排序样式 */
.stats-table .el-table__header th[class*="sortable"] .cell {
  flex-direction: row !important;
  gap: 4px !important;
}

/* 隐藏默认排序图标 */
.stats-table .caret-wrapper,
.stats-table .sort-caret {
  display: none !important;
  visibility: hidden !important;
}

/* 自定义排序图标 */
.stats-table .el-table__header th[class*="sortable"] .cell::after {
  content: "⇅";
  font-size: 10px;
  color: #909399;
  font-weight: normal;
  line-height: 1;
  margin-left: 2px;
}

.stats-table .el-table__header th.ascending .cell::after {
  content: "▲";
  color: #409EFF;
  font-size: 8px;
}

.stats-table .el-table__header th.descending .cell::after {
  content: "▼";
  color: #409EFF;
  font-size: 8px;
}

/* 强制覆盖Element Plus的默认样式 */
.stats-table .el-table__header-wrapper .el-table__header th .cell * {
  display: inline !important;
}

.stats-table .el-table__header-wrapper .el-table__header th .cell .caret-wrapper {
  display: none !important;
}

.stats-table .el-table__body td {
  text-align: center;
  vertical-align: middle;
  padding: 12px 8px;
  height: 60px;
}

.stats-table .el-table__body td .cell {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Element Plus卡片样式 */
.admin-stats-section .el-card {
  min-height: 300px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  max-height: none !important;
  height: auto !important;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  overflow: visible !important;
}

.admin-stats-section .el-card__body {
  min-height: 250px;
  padding: 20px;
  max-height: none !important;
  height: auto !important;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  overflow: visible !important;
}

.admin-stats-section .el-table {
  min-height: 200px;
  background: #ffffff;
  max-height: none !important;
  height: auto !important;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  overflow: visible !important;
}

/* 任务分布样式 */
.task-distribution {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.task-distribution-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.task-distribution-inline .el-tag {
  margin: 0;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 3px;
  flex-shrink: 0;
}

.task-item {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  background-color: #f0f2f5;
  color: #606266;
}

.task-item.planned {
  background-color: #e1f3d8;
  color: #67c23a;
}

.task-item.progress {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.task-item.completed {
  background-color: #e8f4fd;
  color: #409eff;
}

.task-item.hold {
  background-color: #fef0f0;
  color: #f56c6c;
}
</style> 