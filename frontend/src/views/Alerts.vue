<template>
  <div class="alerts-container">
    <div class="page-header">
      <h1>告警管理</h1>
      <div class="header-actions">
        <el-button type="warning" @click="markAllAsRead">
          <el-icon><Check /></el-icon>
          全部标记为已读
        </el-button>
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新建告警
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchQuery"
            placeholder="搜索告警内容"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="levelFilter" placeholder="级别筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="严重" value="CRITICAL" />
            <el-option label="警告" value="WARNING" />
            <el-option label="信息" value="INFO" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="未读" value="UNREAD" />
            <el-option label="已读" value="READ" />
            <el-option label="已处理" value="RESOLVED" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadAlerts">刷新</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 告警统计 -->
    <div class="alerts-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card critical">
            <div class="stat-content">
              <div class="stat-number">{{ stats.critical }}</div>
              <div class="stat-label">严重告警</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card warning">
            <div class="stat-content">
              <div class="stat-number">{{ stats.warning }}</div>
              <div class="stat-label">警告告警</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card info">
            <div class="stat-content">
              <div class="stat-number">{{ stats.info }}</div>
              <div class="stat-label">信息告警</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card unread">
            <div class="stat-content">
              <div class="stat-number">{{ stats.unread }}</div>
              <div class="stat-label">未读告警</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 告警列表 -->
    <div class="alerts-table">
      <el-table
        :data="filteredAlerts"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="告警标题" min-width="200" />
        <el-table-column prop="message" label="告警内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="level" label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="readTime" label="阅读时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.readTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewAlert(row)">查看</el-button>
            <el-button 
              v-if="row.status === 'UNREAD'"
              size="small" 
              type="success" 
              @click="markAsRead(row)"
            >
              标记已读
            </el-button>
            <el-button 
              v-if="row.status === 'READ'"
              size="small" 
              type="warning" 
              @click="resolveAlert(row)"
            >
              标记已处理
            </el-button>
            <el-button size="small" type="danger" @click="deleteAlert(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalAlerts"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建告警对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="新建告警"
      width="500px"
    >
      <el-form
        ref="alertFormRef"
        :model="alertForm"
        :rules="alertRules"
        label-width="100px"
      >
        <el-form-item label="告警标题" prop="title">
          <el-input v-model="alertForm.title" placeholder="请输入告警标题" />
        </el-form-item>
        <el-form-item label="告警内容" prop="message">
          <el-input
            v-model="alertForm.message"
            type="textarea"
            :rows="4"
            placeholder="请输入告警内容"
          />
        </el-form-item>
        <el-form-item label="告警级别" prop="level">
          <el-select v-model="alertForm.level" placeholder="请选择告警级别">
            <el-option label="严重" value="CRITICAL" />
            <el-option label="警告" value="WARNING" />
            <el-option label="信息" value="INFO" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警来源" prop="source">
          <el-input v-model="alertForm.source" placeholder="请输入告警来源" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="saveAlert" :loading="saving">
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 告警详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="告警详情"
      width="600px"
    >
      <div v-if="selectedAlert">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="告警标题">{{ selectedAlert.title }}</el-descriptions-item>
          <el-descriptions-item label="告警内容">{{ selectedAlert.message }}</el-descriptions-item>
          <el-descriptions-item label="告警级别">
            <el-tag :type="getLevelType(selectedAlert.level)">
              {{ getLevelText(selectedAlert.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警状态">
            <el-tag :type="getStatusType(selectedAlert.status)">
              {{ getStatusText(selectedAlert.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警来源">{{ selectedAlert.source }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(selectedAlert.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="阅读时间">{{ formatDateTime(selectedAlert.readTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Check } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getAlerts, createAlert, updateAlert, deleteAlert as deleteAlertApi, markAlertAsRead, markAlertsAsRead } from '@/api/alerts'
import dayjs from 'dayjs'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const alerts = ref([])
const searchQuery = ref('')
const levelFilter = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalAlerts = ref(0)

// 对话框状态
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const selectedAlert = ref(null)

// 表单数据
const alertFormRef = ref()
const alertForm = reactive({
  title: '',
  message: '',
  level: 'WARNING',
  source: ''
})

// 表单验证规则
const alertRules = {
  title: [
    { required: true, message: '请输入告警标题', trigger: 'blur' }
  ],
  message: [
    { required: true, message: '请输入告警内容', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择告警级别', trigger: 'change' }
  ],
  source: [
    { required: true, message: '请输入告警来源', trigger: 'blur' }
  ]
}

// 统计信息
const stats = computed(() => {
  const critical = alerts.value.filter(a => a.level === 'CRITICAL').length
  const warning = alerts.value.filter(a => a.level === 'WARNING').length
  const info = alerts.value.filter(a => a.level === 'INFO').length
  const unread = alerts.value.filter(a => a.status === 'UNREAD').length
  
  return { critical, warning, info, unread }
})

// 计算属性
const filteredAlerts = computed(() => {
  let filtered = alerts.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(alert => 
      alert.title.toLowerCase().includes(query) ||
      alert.message.toLowerCase().includes(query)
    )
  }

  if (levelFilter.value) {
    filtered = filtered.filter(alert => alert.level === levelFilter.value)
  }

  if (statusFilter.value) {
    filtered = filtered.filter(alert => alert.status === statusFilter.value)
  }

  return filtered
})

// 方法
const loadAlerts = async () => {
  loading.value = true
  try {
    const response = await getAlerts({
      page: currentPage.value - 1,
      size: pageSize.value
    })
    alerts.value = response.content || []
    totalAlerts.value = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载告警列表失败')
    console.error('Load alerts error:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

const saveAlert = async () => {
  if (!alertFormRef.value) return
  
  try {
    await alertFormRef.value.validate()
    saving.value = true

    await createAlert(alertForm)
    ElMessage.success('告警创建成功')

    showCreateDialog.value = false
    resetForm()
    loadAlerts()
  } catch (error) {
    ElMessage.error('创建告警失败')
    console.error('Save alert error:', error)
  } finally {
    saving.value = false
  }
}

const viewAlert = (alert) => {
  selectedAlert.value = alert
  showDetailDialog.value = true
  
  // 如果未读，标记为已读
  if (alert.status === 'UNREAD') {
    markAsRead(alert)
  }
}

const markAsRead = async (alert) => {
  try {
    await markAlertAsRead(alert.id)
    alert.status = 'READ'
    alert.readTime = new Date()
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Mark as read error:', error)
  }
}

const resolveAlert = async (alert) => {
  try {
    alert.status = 'RESOLVED'
    ElMessage.success('已标记为已处理')
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Resolve alert error:', error)
  }
}

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

    await markAlertsAsRead()
    ElMessage.success('所有告警已标记为已读')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error('Mark all as read error:', error)
    }
  }
}

const deleteAlert = async (alert) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除告警 "${alert.title}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteAlertApi(alert.id)
    ElMessage.success('告警删除成功')
    loadAlerts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除告警失败')
      console.error('Delete alert error:', error)
    }
  }
}

const resetForm = () => {
  Object.assign(alertForm, {
    title: '',
    message: '',
    level: 'WARNING',
    source: ''
  })
  if (alertFormRef.value) {
    alertFormRef.value.resetFields()
  }
}

const getLevelType = (level) => {
  const types = {
    'CRITICAL': 'danger',
    'WARNING': 'warning',
    'INFO': 'info'
  }
  return types[level] || 'info'
}

const getLevelText = (level) => {
  const texts = {
    'CRITICAL': '严重',
    'WARNING': '警告',
    'INFO': '信息'
  }
  return texts[level] || level
}

const getStatusType = (status) => {
  const types = {
    'UNREAD': 'danger',
    'READ': 'warning',
    'RESOLVED': 'success'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'UNREAD': '未读',
    'READ': '已读',
    'RESOLVED': '已处理'
  }
  return texts[status] || status
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs.utc(date).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm')
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

.search-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.alerts-stats {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-card.critical {
  border-left: 4px solid #f56c6c;
}

.stat-card.warning {
  border-left: 4px solid #e6a23c;
}

.stat-card.info {
  border-left: 4px solid #409eff;
}

.stat-card.unread {
  border-left: 4px solid #909399;
}

.stat-content {
  padding: 10px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.alerts-table {
  margin-bottom: 20px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 