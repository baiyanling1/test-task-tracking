<template>
  <div class="tasks-container">
         <div class="page-header">
       <h1>测试任务管理</h1>
               <el-button 
          v-if="canCreateTask()" 
          type="primary" 
          @click="createNewTask"
        >
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
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="3">
          <el-select v-model="departmentFilter" placeholder="部门筛选" clearable @change="handleDepartmentChange">
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
          <el-select v-model="assignedToFilter" placeholder="负责人筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option
              v-for="user in filteredUsers"
              :key="user.id"
              :label="user.realName"
              :value="user.realName"
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
                 <el-col :span="4">
           <el-date-picker
             v-model="startDateRange"
             type="daterange"
             range-separator="至"
             start-placeholder="开始日期（起）"
             end-placeholder="开始日期（止）"
             value-format="YYYY-MM-DD"
             @change="handleSearch"
             style="width: 100%"
           />
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
                         <el-table-column prop="taskDescription" label="描述" min-width="200">
          <template #default="{ row }">
            <div 
              style="white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: pointer;"
              @mouseenter="showTooltip($event, row.taskDescription)"
              @mouseleave="hideTooltip"
            >
              {{ row.taskDescription }}
            </div>
          </template>
        </el-table-column>
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
        <el-table-column prop="expectedEndDate" label="预计结束日期" width="160">
          <template #default="{ row }">
            {{ formatDate(row.expectedEndDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="participantCount" label="投入人数" width="100" />
        <el-table-column prop="manDays" label="工时(人/天)" width="160">
          <template #default="{ row }">
            <div>
              <div>预计: {{ row.manDays ? row.manDays.toFixed(1) : '-' }}</div>
              <div>实际: {{ row.actualManDays ? row.actualManDays.toFixed(1) : '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="progressPercentage" label="进度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.progressPercentage || 0" />
          </template>
        </el-table-column>
        <el-table-column label="超时状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isOverdue" type="danger" size="small">
              超时{{ row.overdueDays }}天
            </el-tag>
            <el-tag v-else type="success" size="small">
              正常
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="actualEndDate" label="实际结束时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.actualEndDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedTime" label="修改时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedTime) }}
          </template>
        </el-table-column>
                 <el-table-column label="操作" width="280" fixed="right" align="center">
           <template #default="{ row }">
             <div class="action-buttons">
               <el-button 
                 v-if="canEditTask(row)" 
                 size="small" 
                 @click="editTask(row)"
               >编辑</el-button>
               <el-button size="small" type="info" @click="viewDetails(row)">详情</el-button>
               <el-button size="small" type="warning" @click="viewProgress(row)">进度更新</el-button>
               <el-button 
                 v-if="canDeleteTask(row)" 
                 size="small" 
                 type="danger" 
                 @click="deleteTask(row)"
               >删除</el-button>
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
      @close="handleDialogClose"
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
            @change="calculateManDays"
          />
        </el-form-item>
                 <el-form-item label="开始日期" prop="startDate">
           <el-date-picker
             v-model="taskForm.startDate"
             type="date"
             placeholder="选择开始日期"
             value-format="YYYY-MM-DD"
             style="width: 100%"
             @change="calculateManDays"
             :disabled="!!editingTask"
           />
         </el-form-item>
        <el-form-item label="预计结束时间" prop="expectedEndDate">
          <el-date-picker
            v-model="taskForm.expectedEndDate"
            type="date"
            placeholder="选择预计结束时间"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="calculateManDays"
            :disabled="!!editingTask"
          />
        </el-form-item>
        <el-form-item label="预计工时(人/天)" prop="manDays">
          <el-input-number
            v-model="taskForm.manDays"
            :min="0"
            :precision="1"
            placeholder="自动计算"
            style="width: 100%"
            :disabled="!!editingTask"
          />
              <div style="font-size: 12px; color: #909399; margin-top: 5px;">
                * 根据时间区间和参与人数自动计算，可根据实际情况手动调整
              </div>
        </el-form-item>
        
        <!-- 任务进度模块 -->
        <el-form-item label="当前进度" prop="progressPercentage">
          <el-slider
            v-model="taskForm.progressPercentage"
            :min="0"
            :max="100"
            :step="5"
            show-input
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="进度描述" prop="progressNotes">
          <el-input
            v-model="taskForm.progressNotes"
            type="textarea"
            :rows="4"
            placeholder="请输入本次进度更新的详细描述"
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
            @change="calculateActualManDays"
          />
        </el-form-item>
        <el-form-item label="实际工时(人/天)" prop="actualManDays" v-if="taskForm.status === 'COMPLETED' || taskForm.actualEndDate">
          <el-input-number
            v-model="taskForm.actualManDays"
            :min="0"
            :precision="1"
            :step="0.5"
            placeholder="自动计算"
            style="width: 100%"
            @change="calculateActualManDays"
          />
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            * 根据实际开始和结束时间自动计算，可根据实际情况手动调整
          </div>
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
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="saveTask" :loading="saving">
            {{ editingTask ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showProgressDialog"
      :title="activeTab === 'basic' ? '任务详情' : '任务进度'"
      width="1000px"
    >
      <div v-if="selectedTask">
        <el-tabs v-model="activeTab">
                     <el-tab-pane label="基本信息" name="basic" v-if="activeTab === 'basic'">
             <h3>{{ selectedTask.taskName }}</h3>
             <el-descriptions :column="2" border>
                               <el-descriptions-item label="任务描述">
                  <div style="white-space: pre-wrap;">{{ selectedTask.taskDescription }}</div>
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
                 <div>
                   <div>预计: {{ selectedTask.manDays ? selectedTask.manDays.toFixed(1) : '-' }}</div>
                   <div>实际: {{ selectedTask.actualManDays ? selectedTask.actualManDays.toFixed(1) : '-' }}</div>
                 </div>
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
             
             <!-- 进度历史显示 -->
             <div class="progress-section" style="margin-top: 30px;">
               <h4>进度更新历史</h4>
               
               <div v-if="progressHistory.length === 0" class="no-progress">
                 <el-empty description="暂无进度记录" />
               </div>
               
               <div v-else class="progress-timeline">
                 <div v-for="progress in progressHistory" :key="progress.id" class="progress-item">
                   <div class="progress-header">
                     <div class="progress-info">
                       <span class="progress-percentage">{{ progress.progressPercentage }}%</span>
                       <span class="progress-time">{{ formatDateTime(progress.updateTime) }}</span>
                     </div>
                     <div class="progress-user">
                       更新人: {{ progress.updatedByUserName }}
                     </div>
                   </div>
                   
                   <div v-if="progress.progressNotes" class="progress-notes">
                     <strong>进度描述:</strong>
                     <div style="white-space: pre-wrap; margin-top: 5px;">{{ progress.progressNotes }}</div>
                   </div>
                 </div>
               </div>
             </div>
           </el-tab-pane>
          
          <el-tab-pane label="进度历史" name="progress" v-if="activeTab === 'progress'">
            <div class="progress-section">
              <div class="progress-header">
                <h4>进度更新历史</h4>
                <el-button type="primary" size="small" @click="showProgressUpdateDialog">
                  添加进度更新
                </el-button>
              </div>
              
              <div v-if="progressHistory.length === 0" class="no-progress">
                <el-empty description="暂无进度记录" />
              </div>
              
              <div v-else class="progress-timeline">
                <div v-for="progress in progressHistory" :key="progress.id" class="progress-item">
                  <div class="progress-header">
                    <div class="progress-info">
                      <span class="progress-percentage">{{ progress.progressPercentage }}%</span>
                      <span class="progress-time">{{ formatDateTime(progress.updateTime) }}</span>
                    </div>
                    <div class="progress-user">
                      更新人: {{ progress.updatedByUserName }}
                    </div>
                  </div>
                  
                  <div v-if="progress.progressNotes" class="progress-notes">
                    <strong>进度描述:</strong>
                    <div style="white-space: pre-wrap; margin-top: 5px;">{{ progress.progressNotes }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
    
    <!-- 添加进度对话框 -->
    <el-dialog
      v-model="showAddProgressDialog"
      title="添加进度更新"
      width="600px"
    >
      <el-form
        ref="progressFormRef"
        :model="progressForm"
        :rules="progressFormRules"
        label-width="120px"
      >
        <el-form-item label="进度百分比" prop="progressPercentage">
          <el-slider
            v-model="progressForm.progressPercentage"
            :min="0"
            :max="100"
            :step="5"
            show-input
            style="width: 100%"
          />
        </el-form-item>
        
                 <el-form-item label="进度描述" prop="progressNotes">
           <el-input
             v-model="progressForm.progressNotes"
             type="textarea"
             :rows="4"
             placeholder="请输入本次进度更新的详细描述"
             style="width: 100%"
           />
         </el-form-item>
         
         <el-form-item label="实际结束时间" prop="actualEndDate" v-if="progressForm.progressPercentage === 100">
           <el-date-picker
             v-model="progressForm.actualEndDate"
             type="date"
             placeholder="选择实际结束时间（必填）"
             value-format="YYYY-MM-DD"
             style="width: 100%"
             @change="calculateProgressActualManDays"
           />
         </el-form-item>
         
         <el-form-item label="实际工时(人天)" prop="actualManDays" v-if="progressForm.progressPercentage === 100">
           <el-input-number
             v-model="progressForm.actualManDays"
             :min="0"
             :precision="1"
             :step="0.5"
             placeholder="请输入实际工时（必填）"
             style="width: 100%"
             @change="calculateProgressActualManDays"
           />
                             <div style="font-size: 12px; color: #909399; margin-top: 5px;">
                    * 请根据实际工时自己修改
                  </div>
         </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddProgressDialog = false">取消</el-button>
          <el-button type="primary" @click="addProgress">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 自定义tooltip -->
    <div 
      v-if="tooltipVisible" 
      class="custom-tooltip"
      :style="{
        left: tooltipPosition.x + 'px',
        top: tooltipPosition.y + 'px'
      }"
    >
      <div class="tooltip-content" style="white-space: pre-wrap;">{{ tooltipContent }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, QuestionFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getTasks, createTask, updateTask, deleteTask as deleteTaskApi, getTaskProgress, addTaskProgress } from '@/api/tasks'
import { getUsers } from '@/api/users'
import { getDepartments } from '@/api/departments'
import dayjs from 'dayjs'

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
const startDateRange = ref([]) // 新增：开始日期范围
const currentPage = ref(1)
const pageSize = ref(20)
const totalTasks = ref(0)



// 对话框状态
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showProgressDialog = ref(false)
const showAddProgressDialog = ref(false)
const editingTask = ref(null)
const selectedTask = ref(null)
const activeTab = ref('basic')
const progressHistory = ref([])

// 进度表单
const progressForm = ref({
  progressPercentage: 0,
  progressNotes: '',
  actualEndDate: '',
  actualManDays: null
})

// 进度表单验证规则
const progressFormRules = {
  progressPercentage: [
    { required: true, message: '请输入进度百分比', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '进度百分比必须在0-100之间', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        const currentProgress = selectedTask.value?.progressPercentage || 0
        if (value < currentProgress) {
          callback(new Error(`进度不能小于当前进度 ${currentProgress}%`))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  progressNotes: [
    { required: true, message: '请输入进度描述', trigger: 'blur' }
  ],
  actualEndDate: [
    { 
      required: true, 
      message: '进度为100%时，结束时间是必填的', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (progressForm.value.progressPercentage === 100 && !value) {
          callback(new Error('进度为100%时，结束时间是必填的'))
        } else {
          callback()
        }
      }
    }
  ],
  actualManDays: [
    { 
      required: true, 
      message: '进度为100%时，实际工时是必填的', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (progressForm.value.progressPercentage === 100 && (value === null || value === undefined || value <= 0)) {
          callback(new Error('进度为100%时，实际工时是必填的且必须大于0'))
        } else {
          callback()
        }
      }
    }
  ]
}

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
  delayReason: '',
  manDays: 0,
  actualManDays: null,
  progressNotes: ''
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
    { required: true, message: '请选择开始日期', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (value && taskForm.expectedEndDate && value > taskForm.expectedEndDate) {
          callback(new Error('开始时间不能大于预计结束时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  expectedEndDate: [
    { required: true, message: '请选择预计结束时间', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (value && taskForm.startDate && value < taskForm.startDate) {
          callback(new Error('预计结束时间不能小于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
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
  ],
  manDays: [
    { required: true, message: '请输入工时', trigger: 'blur' },
    { type: 'number', min: 0, message: '工时必须大于等于0', trigger: 'blur' }
  ],
  actualManDays: [
    { required: false, message: '请输入实际工时', trigger: 'blur' },
    { type: 'number', min: 0, message: '实际工时必须大于等于0', trigger: 'blur' }
  ],
  progressNotes: [
    { required: false, message: '请输入进度描述', trigger: 'blur' }
  ]
}

// 计算属性 - 现在所有筛选都在后端处理，前端只显示结果
const filteredTasks = computed(() => {
  return tasks.value
})

// 根据部门筛选用户
const filteredUsers = computed(() => {
  if (!departmentFilter.value) {
    return users.value
  }
  return users.value.filter(user => user.department === departmentFilter.value)
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
      search: searchQuery.value || undefined,
      assignedToName: assignedToFilter.value || undefined,
      department: departmentFilter.value || undefined,
      status: statusFilter.value || undefined,
      priority: priorityFilter.value || undefined,
      startDateFrom: startDateRange.value && startDateRange.value.length === 2 ? startDateRange.value[0] : undefined,
      startDateTo: startDateRange.value && startDateRange.value.length === 2 ? startDateRange.value[1] : undefined
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
  loadTasks()
}

const handleDepartmentChange = () => {
  // 当部门变化时，清空负责人筛选
  assignedToFilter.value = ''
  currentPage.value = 1
  loadTasks()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadTasks()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadTasks()
}

const createNewTask = () => {
  editingTask.value = null
  // 确保表单完全重置
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
    delayReason: '',
    manDays: 0,
    actualManDays: null,
    progressNotes: ''
  })
  showCreateDialog.value = true
}

const handleDialogClose = () => {
  editingTask.value = null
  // 确保表单完全重置
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
    delayReason: '',
    manDays: 0,
    actualManDays: null,
    progressNotes: ''
  })
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
    delayReason: task.delayReason || '',
    manDays: task.manDays || 0,
    actualManDays: task.actualManDays || null,
    progressNotes: task.progressNotes || ''
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
    
    // 检查进度为100%时是否填写了实际结束时间
    if (taskForm.progressPercentage === 100 && !taskForm.actualEndDate) {
      ElMessage.error('进度为100%时，实际结束时间是必填的')
      return
    }

    if (editingTask.value) {
      await updateTask(editingTask.value.id, taskForm)
      ElMessage.success('任务更新成功')
    } else {
      // 新建任务
      const response = await createTask(taskForm)
      ElMessage.success('任务创建成功')
      
      // 如果新建任务时有进度描述，自动创建进度历史记录
      if (taskForm.progressNotes && taskForm.progressNotes.trim()) {
        try {
          const progressData = {
            progressPercentage: taskForm.progressPercentage || 0,
            progressNotes: taskForm.progressNotes,
            actualEndDate: taskForm.actualEndDate || '',
            updatedByUserId: authStore.user.id
          }
          
          // 获取新创建的任务ID
          const newTaskId = response?.id
          if (newTaskId) {
            await addTaskProgress(newTaskId, progressData)
          }
        } catch (progressError) {
          console.error('创建进度历史失败:', progressError)
          // 不显示错误消息，因为任务已经创建成功
        }
      }
    }

    showCreateDialog.value = false
         // 确保表单完全重置
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
       delayReason: '',
       manDays: 0,
       actualManDays: null,
       progressNotes: ''
     })
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
  activeTab.value = 'progress'
  showProgressDialog.value = true
  loadProgressHistory(task.id)
}

const viewDetails = (task) => {
  selectedTask.value = task
  activeTab.value = 'basic'
  showProgressDialog.value = true
  // 详情页面也加载进度历史
  loadProgressHistory(task.id)
}

const loadProgressHistory = async (taskId) => {
  try {
    const response = await getTaskProgress(taskId, { page: 0, size: 100 })
    progressHistory.value = response?.content || []
  } catch (error) {
    console.error('加载进度历史失败:', error)
    progressHistory.value = []
  }
}

const addProgress = async () => {
  try {
    // 验证进度不能小于当前进度
    const currentProgress = selectedTask.value?.progressPercentage || 0
    if (progressForm.value.progressPercentage < currentProgress) {
      ElMessage.error(`进度不能小于当前进度 ${currentProgress}%`)
      return
    }
    
    // 验证进度为100%时结束时间是否填写
    if (progressForm.value.progressPercentage === 100 && !progressForm.value.actualEndDate) {
      ElMessage.error('进度为100%时，结束时间是必填的')
      return
    }
    
    // 验证进度为100%时实际工时是否填写
    if (progressForm.value.progressPercentage === 100 && (progressForm.value.actualManDays === null || progressForm.value.actualManDays === undefined || progressForm.value.actualManDays <= 0)) {
      ElMessage.error('进度为100%时，实际工时是必填的且必须大于0')
      return
    }
    
    const progressData = {
      ...progressForm.value,
      updatedByUserId: authStore.user.id
    }
    
    await addTaskProgress(selectedTask.value.id, progressData)
    ElMessage.success('进度更新成功')
    showAddProgressDialog.value = false
    loadProgressHistory(selectedTask.value.id)
    
    // 重新加载任务列表
    await loadTasks()
    
    // 重置表单
    progressForm.value = {
      progressPercentage: 0,
      progressNotes: '',
      actualEndDate: '',
      actualManDays: null
    }
  } catch (error) {
    ElMessage.error('进度更新失败')
    console.error('Add progress error:', error)
  }
}

// 显示进度更新对话框时，设置当前进度
const showProgressUpdateDialog = () => {
  // 设置当前任务进度作为默认值
  progressForm.value.progressPercentage = selectedTask.value.progressPercentage || 0
  showAddProgressDialog.value = true
}

// 计算进度表单的实际工时（排除节假日）
const calculateProgressActualManDays = () => {
  if (progressForm.value.progressPercentage === 100 && selectedTask.value) {
    const startDate = selectedTask.value.startDate
    const actualEndDate = progressForm.value.actualEndDate
    
    if (startDate && actualEndDate) {
      const workDays = calculateWorkDays(new Date(startDate), new Date(actualEndDate))
      const participantCount = selectedTask.value.participantCount || 1
      progressForm.value.actualManDays = parseFloat((workDays * participantCount).toFixed(1))
    }
  }
}

// 自定义tooltip功能
const tooltipVisible = ref(false)
const tooltipContent = ref('')
const tooltipPosition = ref({ x: 0, y: 0 })

const showTooltip = (event, content) => {
  if (!content || content.trim() === '') return
  
  const rect = event.target.getBoundingClientRect()
  const isOverflow = event.target.scrollWidth > event.target.clientWidth
  
  if (isOverflow) {
    tooltipContent.value = content
    tooltipPosition.value = {
      x: rect.left + rect.width / 2,
      y: rect.top - 10
    }
    tooltipVisible.value = true
  }
}

const hideTooltip = () => {
  tooltipVisible.value = false
}



// 权限检查方法
const canEditTask = (task) => {
  const userRole = authStore.user?.role
  const currentUser = authStore.user
  
  // 管理员和测试经理可以编辑所有任务
  if (userRole === 'ADMIN' || userRole === 'MANAGER') {
    return true
  }
  
  // 测试人员可以编辑分配给自己的任务或自己创建的任务
  if (userRole === 'TESTER') {
    const isAssignee = task.assignedToName === currentUser?.realName || task.assignedToName === currentUser?.username
    const isCreator = task.createdByUserName === currentUser?.realName || task.createdByUserName === currentUser?.username
    return isAssignee || isCreator
  }
  
  return false
}

const canDeleteTask = (task) => {
  const userRole = authStore.user?.role
  // 只有管理员可以删除
  return userRole === 'ADMIN'
}

const canCreateTask = () => {
  const userRole = authStore.user?.role
  // 所有角色都可以创建任务
  return true
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
    delayReason: '',
    manDays: 0,
    actualManDays: null,
    progressNotes: ''
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
  return dayjs.utc(date).tz('Asia/Shanghai').format('YYYY-MM-DD')
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs.utc(date).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm')
}

const calculateManDays = () => {
  if (!taskForm.startDate || !taskForm.expectedEndDate || !taskForm.participantCount) {
    taskForm.manDays = 0;
    return;
  }

  const start = new Date(taskForm.startDate);
  const end = new Date(taskForm.expectedEndDate);
  const diffTime = Math.abs(end.getTime() - start.getTime());
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1; // 计算总天数，加1是因为包含开始日期

  // 排除节假日的工作日计算
  const workDays = calculateWorkDays(start, end);
  
  // 工时 = 工作日 × 参与人数
  taskForm.manDays = parseFloat((workDays * taskForm.participantCount).toFixed(1));
};

// 计算实际工时
const calculateActualManDays = () => {
  if (!taskForm.startDate || !taskForm.actualEndDate || !taskForm.participantCount) {
    taskForm.actualManDays = null;
    return;
  }

  const start = new Date(taskForm.startDate);
  const end = new Date(taskForm.actualEndDate);
  
  // 排除节假日的工作日计算
  const workDays = calculateWorkDays(start, end);
  
  // 实际工时 = 工作日 × 参与人数
  taskForm.actualManDays = parseFloat((workDays * taskForm.participantCount).toFixed(1));
};

// 计算工作日（排除周末和节假日）
const calculateWorkDays = (startDate, endDate) => {
  let workDays = 0;
  const current = new Date(startDate);
  
  while (current <= endDate) {
    const dayOfWeek = current.getDay();
    // 排除周末（周六=6，周日=0）
    if (dayOfWeek !== 0 && dayOfWeek !== 6) {
      workDays++;
    }
    current.setDate(current.getDate() + 1);
  }
  
  return workDays;
};

// 生命周期
onMounted(() => {
  loadUsers()
  loadDepartments()
  // 非管理员默认显示自己的任务
  if (authStore.user?.role !== 'ADMIN') {
    assignedToFilter.value = authStore.user?.realName || authStore.user?.username || ''
  }
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
  background-color: #f8f9fa;
  border-radius: 6px;
}

.delay-reason h4 {
  margin: 0 0 10px 0;
  color: #e6a23c;
}

.progress-section {
  margin-top: 20px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.progress-header h4 {
  margin: 0;
  color: #303133;
}

.progress-timeline {
  max-height: 400px;
  overflow-y: auto;
}

.progress-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 15px;
  background-color: #fafafa;
}

.progress-item .progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.progress-percentage {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.progress-time {
  color: #909399;
  font-size: 14px;
}

.progress-user {
  color: #606266;
  font-size: 14px;
}

.progress-notes,
.progress-risk,
.progress-risk-desc,
.progress-blockers,
.progress-next {
  margin-bottom: 10px;
}

.progress-notes strong,
.progress-risk strong,
.progress-risk-desc strong,
.progress-blockers strong,
.progress-next strong {
  color: #303133;
  margin-right: 8px;
}

.progress-notes p,
.progress-risk-desc p,
.progress-blockers p,
.progress-next p {
  margin: 5px 0 0 0;
  color: #606266;
  line-height: 1.5;
}

.no-progress {
  text-align: center;
  padding: 40px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px 0 0 0;
  border-top: 1px solid #e4e7ed;
  margin-top: 20px;
}

.dialog-footer .el-button {
  min-width: 80px;
}

/* 自定义tooltip样式 */
.custom-tooltip {
  position: fixed;
  z-index: 9999;
  background-color: #303133;
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.4;
  max-width: 300px;
  word-wrap: break-word;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  pointer-events: none;
  transform: translateX(-50%);
}

.tooltip-content {
  white-space: pre-wrap;
  word-break: break-word;
}
</style> 