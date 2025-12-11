<template>
  <div class="alerts-container">
    <div class="page-header">
      <h1>告警管理</h1>
      <div class="header-actions">
        <el-button type="warning" @click="markAllAsRead" :disabled="unreadCount === 0">
          全部标记为已读
        </el-button>
      </div>
    </div>



    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索告警标题、内容或任务名称"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="告警状态" clearable @change="handleSearch">
            <el-option label="未读" value="UNREAD" />
            <el-option label="已读" value="READ" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.priority" placeholder="告警级别" clearable @change="handleSearch">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="NORMAL" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 告警统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card urgent">
          <div class="stats-content">
            <div class="stats-number">{{ urgentCount }}</div>
            <div class="stats-label">紧急告警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card high">
          <div class="stats-content">
            <div class="stats-number">{{ highCount }}</div>
            <div class="stats-label">高级告警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card normal">
          <div class="stats-content">
            <div class="stats-number">{{ normalCount }}</div>
            <div class="stats-label">普通告警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card unread">
          <div class="stats-content">
            <div class="stats-number">{{ unreadCount }}</div>
            <div class="stats-label">未读告警</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 告警列表 -->
    <el-card class="alerts-card">
      <template #header>
        <div class="card-header">
          <span>告警列表 (共 {{ total }} 条)</span>
          <div class="header-info">
            <el-tag type="warning">未读: {{ unreadCount }}</el-tag>
          </div>
        </div>
      </template>

      <el-table
        :data="alerts"
        stripe
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="title" label="告警标题" min-width="200">
          <template #default="{ row }">
            <div class="alert-title">
              <span :class="{ 'unread': !row.isRead }">{{ row.title }}</span>
              <el-tag v-if="!row.isRead" type="danger" size="small">未读</el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="content" label="告警内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.content" class="alert-content">{{ row.content }}</span>
            <span v-else class="text-muted">无内容</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="priority" label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.priority)" size="small">
              {{ getLevelText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="isRead" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'success' : 'danger'" size="small">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="relatedTaskName" label="来源" min-width="150">
          <template #default="{ row }">
            <span v-if="row.relatedTaskName">{{ row.relatedTaskName }}</span>
            <span v-else-if="row.relatedEntityId" class="text-muted">任务ID: {{ row.relatedEntityId }}</span>
            <span v-else class="text-muted">系统通知</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="readTime" label="阅读时间" width="180">
          <template #default="{ row }">
            {{ row.readTime ? formatDateTime(row.readTime) : '-' }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" @click="viewDetails(row)">详情</el-button>
              <el-button 
                v-if="!row.isRead" 
                size="small" 
                type="success" 
                @click="markAsRead(row)"
              >
                标记已读
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteAlert(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  getAlerts,
  getAlertsByStatus,
  getAlertsByPriority,
  searchAlerts,
  markAlertAsRead,
  markAllAlertsAsRead,
  deleteAlert,
  deleteAlerts,
  getUnreadAlertCount
} from '@/api/alerts'

// 响应式数据
const loading = ref(false)
const alerts = ref([])
const selectedAlerts = ref([])
const total = ref(0)
const unreadCount = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  priority: ''
})

// 分页
const pagination = reactive({
  page: 0,
  size: 10
})

// 计算统计数量
const urgentCount = computed(() => {
  const urgentAlerts = alerts.value.filter(alert => alert.priority === 'URGENT')
  const count = urgentAlerts.length
  console.log('紧急告警数量:', count, urgentAlerts)
  return count
})

const highCount = computed(() => {
  const highAlerts = alerts.value.filter(alert => alert.priority === 'HIGH')
  const count = highAlerts.length
  console.log('高级告警数量:', count, highAlerts)
  return count
})

const normalCount = computed(() => {
  const normalAlerts = alerts.value.filter(alert => alert.priority === 'NORMAL' || alert.priority === 'LOW')
  const count = normalAlerts.length
  console.log('普通告警数量:', count, normalAlerts)
  return count
})

// 获取告警列表
const loadAlerts = async () => {
  try {
    loading.value = true
    
    let response
    if (searchForm.keyword) {
      response = await searchAlerts(searchForm.keyword, {
        page: pagination.page,
        size: pagination.size
      })
    } else if (searchForm.status) {
      response = await getAlertsByStatus(searchForm.status, {
        page: pagination.page,
        size: pagination.size
      })
    } else if (searchForm.priority) {
      response = await getAlertsByPriority(searchForm.priority, {
        page: pagination.page,
        size: pagination.size
      })
    } else {
      response = await getAlerts({
        page: pagination.page,
        size: pagination.size
      })
    }
    
    console.log('API响应:', response) // 调试信息
    alerts.value = response.content || []
    total.value = response.totalElements || 0
    
    console.log('告警数据:', alerts.value) // 调试信息
    
    // 加载未读数量
    await loadUnreadCount()
  } catch (error) {
    console.error('加载告警列表失败:', error)
    ElMessage.error('加载告警列表失败')
  } finally {
    loading.value = false
  }
}

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const response = await getUnreadAlertCount()
    unreadCount.value = response.unreadCount || 0
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 0
  loadAlerts()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.priority = ''
  pagination.page = 0
  loadAlerts()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 0
  loadAlerts()
}

const handleCurrentChange = (page) => {
  pagination.page = page - 1 // 后端从0开始
  loadAlerts()
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedAlerts.value = selection
}

// 获取告警级别的颜色
const getPriorityColor = (priority) => {
  const colors = {
    'LOW': '#67c23a',
    'NORMAL': '#409eff',
    'HIGH': '#e6a23c',
    'URGENT': '#f56c6c'
  }
  return colors[priority] || '#909399'
}

// 获取告警级别的图标
const getPriorityIcon = (priority) => {
  const icons = {
    'LOW': '✓',
    'NORMAL': 'ℹ',
    'HIGH': '⚠',
    'URGENT': '⚡'
  }
  return icons[priority] || 'ℹ'
}

// 查看详情
const viewDetails = async (alert) => {
  try {
    const priorityColor = getPriorityColor(alert.priority)
    const priorityIcon = getPriorityIcon(alert.priority)
    const statusColor = alert.isRead ? '#67c23a' : '#f56c6c'
    
    // 显示完整的告警详情
    ElMessageBox.alert(
      `<div class="alert-detail-content">
        <!-- 头部区域 -->
        <div class="alert-header" style="border-left: 4px solid ${priorityColor}; background: linear-gradient(135deg, ${priorityColor}15 0%, ${priorityColor}05 100%);">
          <div class="alert-title-row">
            <span class="alert-icon" style="background: ${priorityColor};">${priorityIcon}</span>
            <h3 style="margin: 0; flex: 1; color: #303133; font-size: 16px;">${alert.title}</h3>
          </div>
          <div class="alert-meta">
            <span class="meta-tag" style="background: ${priorityColor}20; color: ${priorityColor};">
              ${getLevelText(alert.priority)}
            </span>
            <span class="meta-tag" style="background: ${statusColor}20; color: ${statusColor};">
              ${alert.isRead ? '已读' : '未读'}
            </span>
          </div>
        </div>

        <!-- 内容区域 -->
        <div class="alert-body">
          <div class="info-section">
            <div class="info-label">
              <svg class="icon" viewBox="0 0 1024 1024" width="14" height="14">
                <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z" fill="currentColor"/>
                <path d="M464 336a48 48 0 1 0 96 0 48 48 0 1 0-96 0z m72 112h-48c-4.4 0-8 3.6-8 8v272c0 4.4 3.6 8 8 8h48c4.4 0 8-3.6 8-8V456c0-4.4-3.6-8-8-8z" fill="currentColor"/>
              </svg>
              告警内容
            </div>
            <div class="info-content">${alert.content || '无内容'}</div>
          </div>

          ${alert.relatedTaskName ? `
          <div class="info-section">
            <div class="info-label">
              <svg class="icon" viewBox="0 0 1024 1024" width="14" height="14">
                <path d="M854.6 288.6L639.4 73.4c-6-6-14.1-9.4-22.6-9.4H192c-17.7 0-32 14.3-32 32v832c0 17.7 14.3 32 32 32h640c17.7 0 32-14.3 32-32V311.3c0-8.5-3.4-16.7-9.4-22.7zM790.2 326H602V137.8L790.2 326z m1.8 562H232V136h302v216a42 42 0 0 0 42 42h216v494z" fill="currentColor"/>
              </svg>
              关联任务
            </div>
            <div class="info-value">${alert.relatedTaskName}</div>
          </div>
          ` : ''}

          <div class="info-section">
            <div class="info-label">
              <svg class="icon" viewBox="0 0 1024 1024" width="14" height="14">
                <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z" fill="currentColor"/>
                <path d="M686.7 638.6L544.1 535.5V288c0-4.4-3.6-8-8-8h-48c-4.4 0-8 3.6-8 8v275.4c0 2.6 1.2 5 3.3 6.5l165.4 120.6c3.6 2.6 8.6 1.8 11.2-1.7l28.6-39c2.6-3.7 1.8-8.7-1.9-11.2z" fill="currentColor"/>
              </svg>
              创建时间
            </div>
            <div class="info-value">${formatDateTime(alert.createdTime)}</div>
          </div>

          ${alert.readTime ? `
          <div class="info-section">
            <div class="info-label">
              <svg class="icon" viewBox="0 0 1024 1024" width="14" height="14">
                <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m193.5 301.7l-210.6 292c-12.7 17.7-39 17.7-51.7 0L318.5 484.9c-3.8-5.3 0-12.7 6.5-12.7h46.9c10.2 0 19.9 4.9 25.9 13.3l71.2 98.8 157.2-218c6-8.3 15.6-13.3 25.9-13.3H699c6.5 0 10.3 7.4 6.5 12.7z" fill="currentColor"/>
              </svg>
              阅读时间
            </div>
            <div class="info-value">${formatDateTime(alert.readTime)}</div>
          </div>
          ` : ''}
        </div>
      </div>`,
      '告警详情',
      {
        confirmButtonText: '确定',
        dangerouslyUseHTMLString: true,
        customClass: 'alert-detail-dialog'
      }
    )
    
    // 如果是未读状态，标记为已读
    if (!alert.isRead) {
      await markAlertAsRead(alert.id)
      loadAlerts() // 刷新列表
    }
  } catch (error) {
    console.error('查看告警详情失败:', error)
    ElMessage.error('查看告警详情失败')
  }
}

// 标记已读
const markAsRead = async (alert) => {
  try {
    await markAlertAsRead(alert.id)
    ElMessage.success('已标记为已读')
    loadAlerts() // 重新加载列表
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败')
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要将所有未读告警标记为已读吗？',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await markAllAlertsAsRead()
    ElMessage.success('全部标记为已读成功')
    loadAlerts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量标记已读失败:', error)
      ElMessage.error('批量标记已读失败')
    }
  }
}

// 删除告警
const handleDeleteAlert = async (alert) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除告警"${alert.title}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteAlert(alert.id)
    ElMessage.success('删除成功')
    loadAlerts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除告警失败:', error)
      ElMessage.error('删除告警失败')
    }
  }
}

// 批量删除
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedAlerts.value.length} 个告警吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const alertIds = selectedAlerts.value.map(alert => alert.id)
    await deleteAlerts(alertIds)
    ElMessage.success('批量删除成功')
    loadAlerts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}



// 工具方法
const getLevelType = (priority) => {
  console.log('获取级别类型:', priority)
  const types = {
    'LOW': 'success',
    'NORMAL': 'warning',
    'HIGH': 'danger',
    'URGENT': 'danger'
  }
  return types[priority] || 'info'
}

const getLevelText = (priority) => {
  console.log('获取级别文本:', priority)
  const texts = {
    'LOW': '低',
    'NORMAL': '中',
    'HIGH': '高',
    'URGENT': '紧急'
  }
  return texts[priority] || priority
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 生命周期
onMounted(() => {
  loadAlerts()
})
</script>

<style scoped>
.alerts-container {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  text-align: center;
  border-left: 4px solid #e4e7ed;
}

.stats-card.urgent {
  border-left-color: #f56c6c;
}

.stats-card.high {
  border-left-color: #e6a23c;
}

.stats-card.normal {
  border-left-color: #409eff;
}

.stats-card.unread {
  border-left-color: #909399;
}

.stats-content {
  padding: 10px;
}

.stats-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.alerts-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  gap: 10px;
}

.alert-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.alert-title .unread {
  font-weight: bold;
  color: #f56c6c;
}

.text-muted {
  color: #909399;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  margin: 0;
  padding: 4px 8px;
  font-size: 12px;
}

/* 告警内容样式 - 限制显示行数 */
.alert-content {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
  max-height: 3em;
}

/* 详情对话框样式 */
:deep(.alert-detail-dialog) {
  width: 650px;
  max-width: 90%;
}

:deep(.alert-detail-dialog .el-message-box__content) {
  padding: 0;
  max-height: 600px;
  overflow-y: auto;
}

:deep(.alert-detail-dialog .el-message-box__message) {
  padding: 0;
}

/* 告警详情内容样式 */
.alert-detail-content {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.alert-header {
  padding: 20px;
  border-radius: 4px 4px 0 0;
  margin-bottom: 20px;
}

.alert-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.alert-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: bold;
  flex-shrink: 0;
}

.alert-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.meta-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.alert-body {
  padding: 0 20px 20px;
}

.info-section {
  margin-bottom: 20px;
}

.info-section:last-child {
  margin-bottom: 0;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.info-label .icon {
  color: #909399;
}

.info-content {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  white-space: pre-wrap;
  word-wrap: break-word;
  line-height: 1.8;
  color: #303133;
  font-size: 14px;
  max-height: 300px;
  overflow-y: auto;
}

.info-content::-webkit-scrollbar {
  width: 6px;
}

.info-content::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.info-content::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.info-value {
  background: #fafafa;
  border-left: 3px solid #409eff;
  padding: 10px 12px;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
}
</style> 