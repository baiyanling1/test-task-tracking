<template>
  <div class="tasks-container">
    <div class="page-header">
      <h1>测试任务管理</h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建任务
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-input
            v-model="searchQuery"
            placeholder="搜索任务名称或描述"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="3">
          <el-select v-model="assignedToFilter" placeholder="负责人筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.realName"
              :value="user.realName"
            />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="departmentFilter" placeholder="部门筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.name"
            />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="计划中" value="PLANNED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已暂停" value="ON_HOLD" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="priorityFilter" placeholder="优先级筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-col>
        <el-col :span="2">
          <el-button type="primary" @click="loadTasks">刷新</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 任务列表 -->
    <div class="tasks-table">
      <el-table
        :data="filteredTasks"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="taskName" label="任务名称" min-width="200" />
        <el-table-column prop="taskDescription" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="assignedToName" label="负责人" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="100">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="expectedEndDate" label="预计结束" width="100">
          <template #default="{ row }">
            {{ formatDate(row.expectedEndDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="participantCount" label="投入人数" width="100" />
        <el-table-column prop="manDays" label="工时(人/天)" width="120">
          <template #default="{ row }">
            {{ row.manDays ? row.manDays.toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="progressPercentage" label="进度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.progressPercentage || 0" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isOverdue" type="danger" size="small">
              超时{{ row.overdueDays }}天
            </el-tag>
            <el-tag v-else type="success" size="small">
              正常
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedTime" label="修改时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="editTask(row)">编辑</el-button>
              <el-button size="small" type="info" @click="viewDetails(row)">详情</el-button>
              <el-button size="small" type="danger" @click="deleteTask(row)">删除</el-button>
            </div>
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
        :total="totalTasks"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建/编辑任务对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingTask ? '编辑任务' : '新建任务'"
      width="600px"
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskRules"
        label-width="120px"
      >
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="taskForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务描述" prop="taskDescription">
          <el-input
            v-model="taskForm.taskDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-select v-model="taskForm.department" placeholder="选择部门" style="width: 100%">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="taskForm.priority" placeholder="请选择优先级">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="assignedToName">
          <el-select v-model="taskForm.assignedToName" placeholder="选择负责人" style="width: 100%">
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.realName"
              :value="user.realName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="投入人数" prop="participantCount">
          <el-input-number
            v-model="taskForm.participantCount"
            :min="1"
            :max="100"
            placeholder="请输入投入人数"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="taskForm.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预计结束时间" prop="expectedEndDate">
          <el-date-picker
            v-model="taskForm.expectedEndDate"
            type="date"
            placeholder="选择预计结束时间"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="实际结束时间" prop="actualEndDate">
          <el-date-picker
            v-model="taskForm.actualEndDate"
            type="date"
            placeholder="选择实际结束时间（可选）"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="进度" prop="progressPercentage">
          <el-input-number
            v-model="taskForm.progressPercentage"
            :min="0"
            :max="100"
            placeholder="请输入进度百分比"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="taskForm.status" placeholder="选择状态" style="width: 100%">
            <el-option label="计划中" value="PLANNED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已暂停" value="ON_HOLD" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="延期备注" prop="delayReason" v-if="taskForm.actualEndDate && taskForm.expectedEndDate && taskForm.actualEndDate > taskForm.expectedEndDate">
          <el-input
            v-model="taskForm.delayReason"
            type="textarea"
            :rows="3"
            placeholder="请输入延期原因和说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="saveTask" :loading="saving">
            {{ editingTask ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showProgressDialog"
      title="任务详情"
      width="800px"
    >
      <div v-if="selectedTask">
        <h3>{{ selectedTask.taskName }}</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务描述">
            {{ selectedTask.taskDescription }}
          </el-descriptions-item>
          <el-descriptions-item label="部门">
            {{ selectedTask.department }}
          </el-descriptions-item>
          <el-descriptions-item label="负责人">
            {{ selectedTask.assignedToName }}
          </el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusType(selectedTask.status)">
              {{ getStatusText(selectedTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(selectedTask.priority)">
              {{ getPriorityText(selectedTask.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前进度">
            <el-progress :percentage="selectedTask.progressPercentage || 0" />
          </el-descriptions-item>
          <el-descriptions-item label="投入人数">
            {{ selectedTask.participantCount }}人
          </el-descriptions-item>
          <el-descriptions-item label="工时(人/天)">
            {{ selectedTask.manDays ? selectedTask.manDays.toFixed(1) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="开始日期">
            {{ formatDate(selectedTask.startDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="预计结束日期">
            {{ formatDate(selectedTask.expectedEndDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="实际结束日期">
            {{ formatDate(selectedTask.actualEndDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(selectedTask.createdTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(selectedTask.updatedTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="超时状态" v-if="selectedTask.isOverdue">
            <el-tag type="danger">超时{{ selectedTask.overdueDays }}天</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="延期完成" v-if="selectedTask.isDelayedCompletion">
            <el-tag type="warning">延期完成</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="selectedTask.delayReason" class="delay-reason">
          <h4>延期原因</h4>
          <p>{{ selectedTask.delayReason }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getTasks, createTask, updateTask, deleteTask as deleteTaskApi } from '@/api/tasks'
import { getUsers } from '@/api/users'
import { getDepartments } from '@/api/departments'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const tasks = ref([])
const users = ref([])
const departments = ref([])
const searchQuery = ref('')
const assignedToFilter = ref('')
const departmentFilter = ref('')
const statusFilter = ref('')
const priorityFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalTasks = ref(0)

// 对话框状态
const showCreateDialog = ref(false)
const showProgressDialog = ref(false)
const editingTask = ref(null)
const selectedTask = ref(null)
const progressUpdates = ref([])

// 表单数据
const taskFormRef = ref()
const taskForm = reactive({
  taskName: '',
  taskDescription: '',
  department: '',
  assignedToName: '',
  participantCount: 1,
  priority: 'MEDIUM',
  startDate: '',
  expectedEndDate: '',
  actualEndDate: '',
  progressPercentage: 0,
  status: 'PLANNED',
  delayReason: ''
})

// 表单验证规则
const taskRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  taskDescription: [
    { required: true, message: '请输入任务描述', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  assignedToName: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ],
  participantCount: [
    { required: true, message: '请输入投入人数', trigger: 'blur' },
    { type: 'number', min: 1, message: '投入人数必须大于0', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  expectedEndDate: [
    { required: true, message: '请选择预计结束时间', trigger: 'change' }
  ],
  actualEndDate: [
    { required: false, message: '请选择实际结束时间', trigger: 'change' }
  ],
  progressPercentage: [
    { required: true, message: '请输入进度百分比', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '进度百分比必须在0-100之间', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  delayReason: [
    { required: false, message: '请输入延期原因和说明', trigger: 'blur' }
  ]
}

// 计算属性
const filteredTasks = computed(() => {
  let filtered = tasks.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(task => 
      (task.taskName && task.taskName.toLowerCase().includes(query)) ||
      (task.taskDescription && task.taskDescription.toLowerCase().includes(query))
    )
  }

  if (assignedToFilter.value) {
    filtered = filtered.filter(task => task.assignedToName === assignedToFilter.value)
  }

  if (departmentFilter.value) {
    filtered = filtered.filter(task => task.department === departmentFilter.value)
  }

  if (statusFilter.value) {
    filtered = filtered.filter(task => task.status === statusFilter.value)
  }

  if (priorityFilter.value) {
    filtered = filtered.filter(task => task.priority === priorityFilter.value)
  }

  return filtered
})

// 方法
const loadUsers = async () => {
  try {
    const response = await getUsers({ size: 1000 })
    users.value = response.content || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

const loadDepartments = async () => {
  try {
    const response = await getDepartments()
    departments.value = response || []
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
}

const loadTasks = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: 'createdTime',
      sortDir: 'desc',
      assignedToName: assignedToFilter.value || undefined,
      department: departmentFilter.value || undefined,
      status: statusFilter.value || undefined,
      priority: priorityFilter.value || undefined
    }
    
    const response = await getTasks(params)
    // 确保tasks数组正确初始化，并添加数据验证
    if (response && response.content) {
      tasks.value = response.content.map(task => ({
        ...task,
        taskName: task.taskName || '',
        taskDescription: task.taskDescription || '',
        assignedToName: task.assignedToName || '',
        department: task.department || '',
        status: task.status || 'PLANNED',
        priority: task.priority || 'MEDIUM'
      }))
    } else {
      tasks.value = []
    }
    totalTasks.value = response?.totalElements || 0
  } catch (error) {
    ElMessage.error('加载任务列表失败')
    console.error('Load tasks error:', error)
    tasks.value = []
    totalTasks.value = 0
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

const editTask = (task) => {
  editingTask.value = task
  Object.assign(taskForm, {
    taskName: task.taskName,
    taskDescription: task.taskDescription,
    department: task.department,
    assignedToName: task.assignedToName,
    participantCount: task.participantCount,
    priority: task.priority,
    startDate: task.startDate,
    expectedEndDate: task.expectedEndDate,
    actualEndDate: task.actualEndDate,
    progressPercentage: task.progressPercentage || 0,
    status: task.status,
    delayReason: task.delayReason || ''
  })
  showCreateDialog.value = true
}

const saveTask = async () => {
  if (!taskFormRef.value) return
  
  try {
    await taskFormRef.value.validate()
    saving.value = true

    // 如果没有设置负责人，默认使用当前用户
    if (!taskForm.assignedToName) {
      taskForm.assignedToName = authStore.user?.realName || authStore.user?.username
    }

    // 检查是否延期完成
    if (taskForm.actualEndDate && taskForm.expectedEndDate && taskForm.actualEndDate > taskForm.expectedEndDate) {
      // 如果实际结束时间大于预计结束时间，自动标记为延期完成
      taskForm.status = 'COMPLETED'
    }

    if (editingTask.value) {
      await updateTask(editingTask.value.id, taskForm)
      ElMessage.success('任务更新成功')
    } else {
      await createTask(taskForm)
      ElMessage.success('任务创建成功')
    }

    showCreateDialog.value = false
    resetForm()
    loadTasks()
  } catch (error) {
    // 检查是否是权限不足的错误
    if (error.response?.data && typeof error.response.data === 'string' && error.response.data.includes('权限不足')) {
      ElMessage.error('非本人任务无法修改')
    } else {
      ElMessage.error(editingTask.value ? '更新任务失败' : '创建任务失败')
    }
    console.error('Save task error:', error)
  } finally {
    saving.value = false
  }
}

const deleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除任务 "${task.taskName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteTaskApi(task.id)
    ElMessage.success('任务删除成功')
    loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除任务失败')
      console.error('Delete task error:', error)
    }
  }
}

const viewProgress = (task) => {
  selectedTask.value = task
  // TODO: 从后端API获取进度更新数据
  progressUpdates.value = []
  showProgressDialog.value = true
}

const viewDetails = (task) => {
  selectedTask.value = task
  showProgressDialog.value = true
}

const resetForm = () => {
  editingTask.value = null
  Object.assign(taskForm, {
    taskName: '',
    taskDescription: '',
    department: authStore.user?.department || '',
    assignedToName: authStore.user?.realName || authStore.user?.username || '',
    participantCount: 1,
    priority: 'MEDIUM',
    startDate: '',
    expectedEndDate: '',
    actualEndDate: '',
    progressPercentage: 0,
    status: 'PLANNED',
    delayReason: ''
  })
  if (taskFormRef.value) {
    taskFormRef.value.resetFields()
  }
}

const getStatusType = (status) => {
  const types = {
    'PLANNED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'ON_HOLD': 'danger',
    'CANCELLED': 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PLANNED': '计划中',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'ON_HOLD': '已暂停',
    'CANCELLED': '已取消'
  }
  return texts[status] || status
}

const getPriorityType = (priority) => {
  const types = {
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'info'
  }
  return types[priority] || 'info'
}

const getPriorityText = (priority) => {
  const texts = {
    'HIGH': '高',
    'MEDIUM': '中',
    'LOW': '低'
  }
  return texts[priority] || priority
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  loadUsers()
  loadDepartments()
  loadTasks()
})
</script>

<style scoped>
.tasks-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e9ecef;
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.tasks-table {
  margin-top: 20px;
}

.tasks-table .el-table {
  border-radius: 8px;
  overflow: hidden;
}

.tasks-table .el-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.tasks-table .el-table td {
  padding: 12px 0;
}

.tasks-table .el-progress {
  margin: 0;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.progress-updates {
  margin-top: 20px;
}

.progress-updates h4 {
  margin-bottom: 15px;
  color: #303133;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  margin: 0;
  flex-shrink: 0;
}

.delay-reason {
  margin-top: 20px;
  padding: 15px;
  background-color: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 4px;
}

.delay-reason h4 {
  margin-bottom: 10px;
  color: #e6a23c;
  font-size: 16px;
}

.delay-reason p {
  color: #f56c6c;
  font-size: 14px;
  line-height: 1.6;
}
</style> 