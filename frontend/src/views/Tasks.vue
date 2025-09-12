<template>
  <div class="tasks-container">
         <div class="page-header">
       <h1>测试任务管理</h1>
      <div class="header-actions">
        
        <el-button 
          type="info" 
          size="small"
          @click="showHelpDialog = true"
          style="margin-right: 10px;"
        >
          <el-icon><QuestionFilled /></el-icon>
          填写帮助
        </el-button>
        <el-button 
          v-if="canCreateTask()" 
          type="primary" 
          @click="createNewTask"
        >
         <el-icon><Plus /></el-icon>
         新建任务
       </el-button>
      </div>
     </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <!-- 第一行筛选条件 -->
      <el-row :gutter="20" style="margin-bottom: 15px;">
        <el-col :span="6">
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
          <el-select v-model="taskTypeFilter" placeholder="任务类型" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="需求测试" value="REQUIREMENT" />
            <el-option label="版本测试" value="VERSION" />
          </el-select>
        </el-col>
        <el-col :span="4">
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
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="计划中" value="PLANNED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已暂停" value="ON_HOLD" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="priorityFilter" placeholder="优先级筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-col>
      </el-row>
      
      <!-- 第二行筛选条件 -->
      <el-row :gutter="20">
        <el-col :span="4">
          <el-select v-model="overdueFilter" placeholder="超时状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="超预期/延期" value="overdue" />
            <el-option label="正常" value="normal" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="startDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始时间（起）"
            end-placeholder="开始时间（止）"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="3">
          <el-button type="success" @click="loadAllData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-col>
        <el-col :span="11">
          <div style="color: #909399; font-size: 14px; line-height: 32px;">
            <el-icon><InfoFilled /></el-icon>
            筛选提示：支持任务名称、描述搜索，可按部门、类型、负责人、状态等条件筛选
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 任务列表 -->
    <div class="tasks-table">
      <!-- 任务表格 - 支持展开子任务 -->
      <el-table
        ref="taskTableRef"
        :data="taskTreeData"
        v-loading="loading"
        stripe
        row-key="id"
        :tree-props="{children: 'subTasks', hasChildren: 'hasSubTasks'}"
        :default-expand-all="false"
        :indent="20"
        style="width: 100%"
      >
        <!-- 任务名称列 - 显示层级 -->
        <el-table-column prop="taskName" label="任务名称" min-width="280">
          <template #default="{ row }">
            <div class="task-name-cell">
              <!-- 任务图标 -->
              <el-icon v-if="row.taskLevel === 'MAIN' && row.taskType === 'VERSION'" class="main-task-icon">
                <Folder />
              </el-icon>
              <el-icon v-else-if="row.taskLevel === 'SUB'" class="sub-task-icon">
                <Document />
              </el-icon>
              
              <!-- 任务名称 -->
              <span :class="{'main-task-name': row.taskLevel === 'MAIN', 'sub-task-name': row.taskLevel === 'SUB'}">
                {{ row.taskName }}
              </span>
              
              <!-- 任务类型标签（仅主任务显示） -->
              <el-tag v-if="row.taskLevel === 'MAIN'" 
                      :type="getTaskTypeTagType(row.taskType || 'REQUIREMENT')" 
                      size="small" 
                      class="task-type-tag">
                {{ getTaskTypeText(row.taskType || 'REQUIREMENT') }}
              </el-tag>
              
              <!-- 子任务数量标签 -->
              <el-tag v-if="row.taskLevel === 'MAIN' && (row.taskType === 'VERSION' || (!row.taskType && row.hasSubTasks)) && row.subTasks && row.subTasks.length > 0" 
                      size="small" 
                      type="info" 
                      class="subtask-count">
                {{ row.subTasks.length }}个子任务
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <!-- 描述列 -->
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
        
        <!-- 状态列 -->
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        
        <!-- 优先级列 -->
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 进度列 - 主任务显示自动计算标识 -->
        <el-table-column prop="progressPercentage" label="进度" width="180">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress :percentage="row.progressPercentage || 0">
                <template #default="{ percentage }">
                  <span>{{ percentage }}%</span>
                  <el-tag v-if="row.taskLevel === 'MAIN' && row.autoProgressCalculation" 
                          size="small" 
                          type="info" 
                          class="auto-progress-tag">
                    <el-icon><Cpu /></el-icon>
                    自动
                  </el-tag>
                </template>
              </el-progress>
            </div>
          </template>
        </el-table-column>
        
        <!-- 投入人数列 -->
        <el-table-column prop="participantCount" label="投入人数" width="100" align="center">
          <template #default="{ row }">
            {{ row.participantCount || 0 }}人
          </template>
        </el-table-column>
        
        <!-- 工时列 -->
        <el-table-column prop="manDays" label="工时(人/天)" width="120" align="center">
          <template #default="{ row }">
            <div>
              <div>{{ (row.manDays || 0).toFixed(1) }}</div>
              <div v-if="row.actualManDays" style="color: #909399; font-size: 12px;">
                实际: {{ row.actualManDays.toFixed(1) }}
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="startDate" label="开始时间" width="100">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="expectedEndDate" label="预计结束日期" width="160">
          <template #default="{ row }">
            {{ formatDate(row.expectedEndDate) }}
          </template>
        </el-table-column>
        
        <!-- 超时状态列 -->
        <el-table-column label="超时状态" width="100" align="center">
          <template #default="{ row }">
            <div v-if="row.isOverdue" style="color: #f56c6c;">
              <el-icon><WarningFilled /></el-icon>
              超时{{ row.overdueDays }}天
            </div>
            <div v-else-if="row.isExpectedCompletionReached" style="color: #e6a23c;">
              已到期
            </div>
            <div v-else style="color: #67c23a;">
              正常
            </div>
          </template>
        </el-table-column>
        
        <!-- 实际结束时间列 -->
        <el-table-column prop="actualEndDate" label="实际结束时间" width="160">
          <template #default="{ row }">
            <span v-if="row.actualEndDate" :style="{ color: row.actualEndDate > row.expectedEndDate ? '#f56c6c' : '#67c23a' }">
              {{ formatDate(row.actualEndDate) }}
            </span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        
        <!-- 更新时间列 -->
        <el-table-column prop="updatedTime" label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedTime) }}
          </template>
        </el-table-column>
        
        <!-- 操作列 - 根据任务类型显示不同操作 -->
        <el-table-column label="操作" width="320" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <!-- 主任务操作 -->
              <template v-if="row.taskLevel === 'MAIN'">
                <!-- 只有版本测试主任务才能添加子任务 -->
                <el-button v-if="row.taskType === 'VERSION'" size="small" @click="createSubTaskForParent(row)">
                  <el-icon><Plus /></el-icon>
                  添加子任务
                </el-button>
                <el-button size="small" type="info" @click="viewDetails(row)">
                  详情
                </el-button>
                <el-button v-if="canEditTask(row)" size="small" @click="editTask(row)">
                  编辑
                </el-button>
                <!-- 需求测试主任务：负责人或创建者可以更新进度 -->
                <el-button 
                  v-if="row.taskType === 'REQUIREMENT' && canEditTask(row)" 
                  size="small" 
                  type="warning" 
                  @click="updateRequirementTaskProgressDialog(row)"
                >
                  进度更新
                </el-button>
                <!-- 版本测试主任务：只有创建者可以手动更新进度 -->
                <el-button 
                  v-if="row.taskType === 'VERSION' && isMainTaskCreator(row)" 
                  size="small" 
                  type="warning" 
                  @click="updateMainTaskProgressDialog(row)"
                >
                  进度更新
                </el-button>
                <!-- 历史任务兼容：没有taskType的主任务按需求测试处理 -->
                <el-button 
                  v-if="!row.taskType && canEditTask(row)" 
                  size="small" 
                  type="warning" 
                  @click="updateRequirementTaskProgressDialog(row)"
                >
                  进度更新
                </el-button>
              </template>
              
              <!-- 子任务操作 -->
              <template v-else>
                <el-button size="small" type="info" @click="viewDetails(row)">
                  详情
                </el-button>
                <el-button v-if="canEditTask(row)" size="small" @click="editTask(row)">
                  编辑
                </el-button>
                <el-button v-if="canEditTask(row)" size="small" type="warning" @click="updateSubTaskProgressDialog(row)">
                  进度更新
                </el-button>
              </template>
              
              <!-- 通用操作 -->
              <el-button v-if="canDeleteTask(row)" size="small" type="danger" @click="deleteTask(row)">
                删除
              </el-button>
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
        <!-- 任务类型选择（仅新建主任务时显示） -->
        <el-form-item label="任务类型" v-if="!editingTask && taskForm.taskLevel === 'MAIN'">
          <el-radio-group v-model="taskForm.taskType">
            <el-radio label="REQUIREMENT">
              <span class="task-type-option">
                <el-icon><Document /></el-icon>
                需求测试（独立任务）
              </span>
            </el-radio>
            <el-radio label="VERSION">
              <span class="task-type-option">
                <el-icon><Folder /></el-icon>
                版本测试（可拆分子任务）
              </span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 父任务选择（创建子任务时） -->
        <el-form-item label="归属主任务" v-if="taskForm.taskLevel === 'SUB'" prop="parentTaskId">
          <el-select v-model="taskForm.parentTaskId" placeholder="选择主任务" style="width: 100%">
            <el-option 
              v-for="mainTask in mainTaskOptions" 
              :key="mainTask.id"
              :label="mainTask.taskName" 
              :value="mainTask.id"
            />
          </el-select>
        </el-form-item>

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
                 <el-form-item label="开始时间" prop="startDate">
           <el-date-picker
             v-model="taskForm.startDate"
             type="date"
             placeholder="选择开始时间"
             value-format="YYYY-MM-DD"
             style="width: 100%"
             :disabled="editingTask && !isAdmin()"
           />
         </el-form-item>
        <el-form-item label="预计结束时间" prop="expectedEndDate">
          <el-date-picker
            v-model="taskForm.expectedEndDate"
            type="date"
            placeholder="选择预计结束时间"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled="editingTask && !isAdmin()"
          />
        </el-form-item>
        <el-form-item label="预计工时(人/天)" prop="manDays">
          <div class="man-days-input-group">
            <el-input-number
              v-model="taskForm.manDays"
              :min="0.1"
              :precision="1"
              placeholder="请输入或点击计算"
              style="width: calc(100% - 80px)"
              :disabled="editingTask && !isAdmin()"
            />
            <el-button
              type="primary"
              size="small"
              :disabled="!taskForm.startDate || !taskForm.expectedEndDate || !taskForm.participantCount || (editingTask && !isAdmin())"
              @click="calculateManDaysManually"
              style="margin-left: 8px; width: 72px;"
            >
              计算
            </el-button>
          </div>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            * 点击"计算"按钮根据时间区间和参与人数自动计算，也可手动输入
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
             @change="onProgressChange"
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
          <div class="man-days-input-group">
            <el-input-number
              v-model="taskForm.actualManDays"
              :min="0.1"
              :precision="1"
              :step="0.5"
              placeholder="请输入或点击计算"
              style="width: calc(100% - 80px)"
              @change="onActualManDaysChange"
            />
            <el-button
              type="primary"
              size="small"
              :disabled="!taskForm.startDate || !taskForm.actualEndDate || !taskForm.participantCount"
              @click="calculateActualManDaysManually"
              style="margin-left: 8px; width: 72px;"
            >
              计算
            </el-button>
          </div>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            * 点击"计算"按钮根据开始时间和实际结束时间自动计算，也可手动输入
          </div>
        </el-form-item>
                 <el-form-item label="状态" prop="status">
           <el-select v-model="taskForm.status" placeholder="选择状态" style="width: 100%" @change="onStatusChange">
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
               <el-descriptions-item label="开始时间">
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
               <el-descriptions-item label="超时状态" v-if="selectedTask.isOverdue || selectedTask.isExpectedCompletionReached">
                 <el-tag v-if="selectedTask.isOverdue && selectedTask.actualEndDate" type="danger">
                   延期完成{{ selectedTask.overdueDays }}天
                 </el-tag>
                 <el-tag v-else-if="selectedTask.isOverdue && !selectedTask.actualEndDate" type="warning">
                   超预期{{ selectedTask.overdueDays }}天
                 </el-tag>
                 <el-tag v-else-if="selectedTask.isExpectedCompletionReached && !selectedTask.actualEndDate" type="info">
                   已到预期时间
                 </el-tag>
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

    <!-- 帮助文档对话框 -->
    <el-dialog
      v-model="showHelpDialog"
      title="任务填写帮助文档"
      width="800px"
      class="help-dialog"
    >
      <div class="help-content">
        <el-collapse v-model="activeHelpItems" accordion>
          
          <!-- 日期字段填写规则 -->
          <el-collapse-item title="📅 日期字段填写规则" name="dates">
            <div class="help-section">
              <h4>开始时间 & 预计结束时间</h4>
              <ul>
                <li><strong>开始时间</strong>：任务实际开始执行的日期</li>
                <li><strong>预计结束时间</strong>：根据任务复杂度和工作量预估的完成日期</li>
                <li><strong>填写要求</strong>：
                  <ul>
                    <li>开始时间不能晚于预计结束时间（可以相等，表示当天完成）</li>
                    <li>当开始时间等于预计结束时间时，自动计算按1天工时</li>
                    <li>建议预留一定的缓冲时间，避免过于紧张的排期</li>
                    <li>考虑节假日和周末（系统会自动排除）</li>
                  </ul>
                </li>
              </ul>
              
              <h4>实际结束时间</h4>
              <ul>
                <li><strong>填写时机</strong>：任务完成时填写，进度达到100%时可选填</li>
                <li><strong>作用</strong>：用于计算实际工时，分析任务执行效率</li>
                <li><strong>延期处理</strong>：
                  <ul>
                    <li>如果实际结束时间晚于预计结束时间，系统会标记为"延期完成"</li>
                    <li>需要在"延期备注"中说明延期原因</li>
                  </ul>
                </li>
              </ul>
              
              <div class="tip-box">
                <el-icon><InfoFilled /></el-icon>
                <span><strong>权限说明：</strong>编辑任务时，只有管理员可以修改开始时间和预计结束时间</span>
              </div>
            </div>
          </el-collapse-item>

          <!-- 工时填写方法 -->
          <el-collapse-item title="⏱️ 工时填写方法与注意事项" name="workdays">
            <div class="help-section">
              <h4>预计工时（人/天）</h4>
              <ul>
                <li><strong>计算方法</strong>：
                  <ul>
                    <li>手动输入：根据经验直接填写预估工时</li>
                    <li>自动计算：点击"计算"按钮，系统根据时间区间和投入人数计算</li>
                    <li>计算公式：工作日数 × 投入人数 = 预计工时</li>
                  </ul>
                </li>
                <li><strong>工作日计算</strong>：
                  <ul>
                    <li>自动排除周末（周六、周日）</li>
                    <li>排除国家法定节假日</li>
                    <li>只计算有效工作日</li>
                  </ul>
                </li>
                <li><strong>填写建议</strong>：
                  <ul>
                    <li>手动填写时，支持0.1天起的精确工时（如0.5天表示半天）</li>
                    <li>同一天开始和结束的任务，自动计算按1天计算</li>
                    <li>考虑任务复杂度，适当增加缓冲时间</li>
                    <li>参考历史类似任务的实际工时</li>
                    <li>考虑团队成员的经验水平</li>
                  </ul>
                </li>
              </ul>

              <h4>实际工时（人/天）</h4>
              <ul>
                <li><strong>填写时机</strong>：任务完成后填写</li>
                <li><strong>作用</strong>：用于工时统计和项目管理分析</li>
                <li><strong>计算方式</strong>：
                  <ul>
                    <li>自动计算：基于开始时间和实际结束时间</li>
                    <li>手动调整：可根据实际情况手动修改</li>
                  </ul>
                </li>
              </ul>

              <div class="example-box">
                <h5>💡 计算示例</h5>
                <p><strong>自动计算示例1：</strong>开始时间 2024-01-15（周一），预计结束时间 2024-01-19（周五），投入人数 2人</p>
                <p><strong>计算：</strong>5个工作日 × 2人 = 10.0人天</p>
                <p><strong>自动计算示例2：</strong>开始时间 2024-01-15（周一），预计结束时间 2024-01-15（周一），投入人数 1人</p>
                <p><strong>计算：</strong>1个工作日 × 1人 = 1.0人天（同一天按1天计算）</p>
                <p><strong>手动填写示例：</strong>简单任务可以填写 0.5人天（半天），复杂任务可以填写 2.5人天等</p>
              </div>
            </div>
          </el-collapse-item>

          <!-- 进度更新方法 -->
          <el-collapse-item title="📈 进度更新方法" name="progress">
            <div class="help-section">
              <h4>进度更新方式</h4>
              <ul>
                <li><strong>方式一：任务列表中直接更新</strong>
                  <ul>
                    <li>点击任务行的"编辑"按钮</li>
                    <li>在表单中调整"当前进度"滑块</li>
                    <li>填写"进度描述"说明本次更新内容</li>
                  </ul>
                </li>
                <li><strong>方式二：任务详情中添加进度记录</strong>
                  <ul>
                    <li>点击"查看详情"进入任务详情页</li>
                    <li>切换到"进度历史"标签</li>
                    <li>点击"添加进度更新"按钮</li>
                  </ul>
                </li>
              </ul>

              <h4>进度填写规范</h4>
              <ul>
                <li><strong>进度百分比</strong>：
                  <ul>
                    <li>0%：任务尚未开始</li>
                    <li>1-99%：任务进行中</li>
                    <li>100%：任务已完成</li>
                  </ul>
                </li>
                <li><strong>进度描述要求</strong>：
                  <ul>
                    <li>详细说明本阶段完成的工作内容</li>
                    <li>列出遇到的问题和解决方案</li>
                    <li>说明下一阶段的工作计划</li>
                  </ul>
                </li>
                <li><strong>更新频率建议</strong>：
                  <ul>
                    <li>每周至少更新一次进度</li>
                    <li>重要节点及时更新</li>
                    <li>遇到阻塞问题时立即更新</li>
                  </ul>
                </li>
              </ul>

              <h4>状态自动联动</h4>
              <ul>
                <li>进度设置为100%时，状态自动变为"已完成"</li>
                <li>进度从0%变为其他值时，状态自动变为"进行中"</li>
                <li>状态设置为"已完成"时，进度自动变为100%</li>
              </ul>
            </div>
          </el-collapse-item>

          <!-- 其他注意事项 -->
          <el-collapse-item title="⚠️ 其他注意事项" name="notes">
            <div class="help-section">
              <h4>权限说明</h4>
              <ul>
                <li><strong>管理员（ADMIN）</strong>：可以编辑所有任务的所有字段</li>
                <li><strong>经理（MANAGER）</strong>：可以编辑所有任务，但编辑时部分关键字段受限</li>
                <li><strong>测试员（TESTER）</strong>：只能编辑分配给自己的任务或自己创建的任务</li>
              </ul>

              <h4>数据保存</h4>
              <ul>
                <li>所有修改都会记录操作时间和操作人</li>
                <li>进度更新会保留完整的历史记录</li>
                <li>系统会自动计算任务的超时状态</li>
              </ul>

              <h4>最佳实践</h4>
              <ul>
                <li><strong>及时更新</strong>：定期更新任务进度，保持信息准确性</li>
                <li><strong>详细描述</strong>：进度描述要详细，便于团队协作</li>
                <li><strong>合理估时</strong>：预计工时要结合实际情况，避免过于乐观</li>
                <li><strong>问题反馈</strong>：遇到问题及时在进度描述中说明</li>
              </ul>

              <div class="warning-box">
                <el-icon><WarningFilled /></el-icon>
                <span><strong>重要提醒：</strong>请确保任务信息的准确性，这些数据将用于项目管理和工作量统计</span>
              </div>
            </div>
          </el-collapse-item>

        </el-collapse>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="showHelpDialog = false">知道了</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, QuestionFilled, InfoFilled, WarningFilled, Grid, List, Folder, Document, Cpu, Refresh } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { 
  getTasks, createTask, updateTask, deleteTask as deleteTaskApi, getTaskProgress, addTaskProgress,
  createSubTask, getSubTasks, updateSubTaskProgress, deleteSubTask, convertToMainTask,
  updateMainTaskProgressManually
} from '@/api/tasks'
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
const overdueFilter = ref('') // 超时状态筛选（包含超预期、延期完成）
const startDateRange = ref([]) // 新增：开始时间范围
const currentPage = ref(1)
const pageSize = ref(20)
const totalTasks = ref(0)

// 任务数据
const taskTreeData = ref([])
const showCreateSubTaskDialog = ref(false)
const parentTaskForSub = ref(null)
const mainTaskOptions = ref([])
const showSubTaskProgress = ref(false)
const selectedSubTask = ref(null)

// 筛选条件
const taskTypeFilter = ref('')



// 对话框状态
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showProgressDialog = ref(false)
const showAddProgressDialog = ref(false)
const showHelpDialog = ref(false)
const editingTask = ref(null)
const selectedTask = ref(null)
const activeTab = ref('basic')
const progressHistory = ref([])
const activeHelpItems = ref(['dates']) // 默认展开第一个帮助项

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

// 表格和表单引用
const taskTableRef = ref()
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
  progressNotes: '',
  // 子任务相关字段
  taskLevel: 'MAIN',
  parentTaskId: null,
  subtaskWeight: 1.00,
  taskType: 'VERSION' // 默认为版本测试
})

// 标记实际工时是否为手动输入
const isActualManDaysManual = ref(false)

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
    { required: true, message: '请选择开始时间', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (value && taskForm.expectedEndDate && value > taskForm.expectedEndDate) {
          callback(new Error('开始时间不能晚于预计结束时间'))
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
          callback(new Error('预计结束时间不能早于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  actualEndDate: [
    { 
      required: false, 
      message: '请选择实际结束时间', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if ((taskForm.progressPercentage === 100 || taskForm.status === 'COMPLETED') && !value) {
          callback(new Error('进度为100%或状态为已完成时，实际结束时间是必填的'))
        } else {
          callback()
        }
      }
    }
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
    { type: 'number', min: 0.1, message: '工时必须大于0', trigger: 'blur' }
  ],
  actualManDays: [
    { 
      required: false, 
      message: '请输入实际工时', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if ((taskForm.progressPercentage === 100 || taskForm.status === 'COMPLETED') && (value === null || value === undefined || value < 0.1)) {
          callback(new Error('进度为100%或状态为已完成时，实际工时是必填的且必须大于0'))
        } else if (value !== null && value !== undefined && value < 0.1) {
          callback(new Error('实际工时必须大于0'))
        } else {
          callback()
        }
      }
    }
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

// 统一的数据加载方法 - 单接口调用
const loadAllData = async () => {
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
      taskType: taskTypeFilter.value || undefined,
      isOverdue: overdueFilter.value === 'overdue' ? true : overdueFilter.value === 'normal' ? false : undefined,
      startDateFrom: startDateRange.value && startDateRange.value.length === 2 ? startDateRange.value[0] : undefined,
      startDateTo: startDateRange.value && startDateRange.value.length === 2 ? startDateRange.value[1] : undefined
    }
    
    // 单接口调用，获取所有任务数据
    const response = await getTasks(params)
    
    if (response && response.content) {
      const allTasks = response.content.map(task => ({
        ...task,
        taskName: task.taskName || '',
        taskDescription: task.taskDescription || '',
        assignedToName: task.assignedToName || '',
        department: task.department || '',
        status: task.status || 'PLANNED',
        priority: task.priority || 'MEDIUM'
      }))
      
      // 智能分离数据：根据taskLevel和taskType分类
      tasks.value = allTasks // 所有任务的扁平列表
      
      // 构建树形数据：只包含主任务，子任务作为children
      taskTreeData.value = allTasks
        .filter(task => task.taskLevel === 'MAIN') // 只取主任务
        .map(mainTask => ({
          ...mainTask,
          // 如果有子任务数据，保持原有的subTasks结构
          subTasks: mainTask.subTasks || [],
          hasSubTasks: mainTask.hasSubTasks || false
        }))
        
    } else {
      tasks.value = []
      taskTreeData.value = []
    }
    
    totalTasks.value = response?.totalElements || 0
    
  } catch (error) {
    ElMessage.error('加载任务数据失败')
    console.error('Load data error:', error)
    tasks.value = []
    taskTreeData.value = []
    totalTasks.value = 0
  } finally {
    loading.value = false
  }
}

// 为了兼容性，保留原方法名
const loadTasks = loadAllData

const handleSearch = () => {
  currentPage.value = 1
  loadAllData()
}

const handleDepartmentChange = () => {
  // 当部门变化时，清空负责人筛选
  assignedToFilter.value = ''
  currentPage.value = 1
  loadAllData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadAllData()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadAllData()
}

const createNewTask = () => {
  editingTask.value = null
  parentTaskForSub.value = null
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
    manDays: null,
    actualManDays: null,
    progressNotes: '',
    // 子任务相关字段
    taskLevel: 'MAIN',
    parentTaskId: null,
    subtaskWeight: 1.00,
    taskType: 'VERSION' // 默认为版本测试，便于创建子任务
  })
  // 重置手动输入标记
  isActualManDaysManual.value = false
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
    manDays: null,
    actualManDays: null,
    progressNotes: '',
    taskLevel: 'MAIN',
    parentTaskId: null,
    subtaskWeight: 1.00,
    taskType: 'VERSION'
  })
  // 重置手动输入标记
  isActualManDaysManual.value = false
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
    progressNotes: task.progressNotes || '',
    // 子任务相关字段
    taskLevel: task.taskLevel || 'MAIN',
    parentTaskId: task.parentTaskId || null,
    subtaskWeight: task.subtaskWeight || 1.00,
    taskType: task.taskType || 'VERSION'
  })
  // 编辑时重置手动输入标记，允许自动计算
  isActualManDaysManual.value = false
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
      // 新建任务或子任务
      let response
      if (taskForm.taskLevel === 'SUB' && taskForm.parentTaskId) {
        // 创建子任务
        response = await createSubTask(taskForm.parentTaskId, taskForm)
        ElMessage.success('子任务创建成功')
      } else {
        // 创建主任务
        response = await createTask(taskForm)
        ElMessage.success('任务创建成功')
      }
      
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
      manDays: null,
      actualManDays: null,
      progressNotes: ''
    })
    // 重置手动输入标记
    isActualManDaysManual.value = false
    
    // 重新加载任务数据
    loadAllData()
  } catch (error) {
    // 详细错误处理
    let errorMessage = editingTask.value ? '更新任务失败' : '创建任务失败'
    
    if (error.response?.data) {
      if (typeof error.response.data === 'string') {
        // 如果是权限错误，显示详细信息
        if (error.response.data.includes('权限不足')) {
          errorMessage = error.response.data
        } else if (error.response.data.includes('时间范围')) {
          errorMessage = error.response.data
        } else if (error.response.data.includes('不能添加子任务')) {
          errorMessage = error.response.data
        } else {
          errorMessage = error.response.data
        }
      } else if (error.response.data.message) {
        errorMessage = error.response.data.message
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
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
    loadAllData()
  } catch (error) {
    if (error !== 'cancel') {
      // 详细错误处理
      let errorMessage = '删除任务失败'
      
      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          if (error.response.data.includes('权限')) {
            errorMessage = error.response.data
          } else {
            errorMessage = error.response.data
          }
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message
        }
      } else if (error.message) {
        errorMessage = error.message
      }
      
      ElMessage.error(errorMessage)
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
    
    // 判断是否为子任务进度更新
    if (selectedTask.value?.taskLevel === 'SUB') {
      // 子任务进度更新
      await updateSubTaskProgress(selectedTask.value.id, progressForm.value.progressPercentage)
      ElMessage.success('子任务进度更新成功')
    } else {
      // 主任务/需求测试任务进度更新
      await addTaskProgress(selectedTask.value.id, progressData)
      ElMessage.success('任务进度更新成功')
    }
    
    showAddProgressDialog.value = false
    loadProgressHistory(selectedTask.value.id)
    
    // 重新加载所有任务数据
    loadAllData()
    
    // 重置表单
    progressForm.value = {
      progressPercentage: 0,
      progressNotes: '',
      actualEndDate: '',
      actualManDays: null
    }
  } catch (error) {
    // 详细错误处理
    let errorMessage = '进度更新失败'
    
    if (error.response?.data) {
      if (typeof error.response.data === 'string') {
        if (error.response.data.includes('权限')) {
          errorMessage = error.response.data
        } else {
          errorMessage = error.response.data
        }
      } else if (error.response.data.message) {
        errorMessage = error.response.data.message
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
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
    // 检查是否为负责人（使用多种匹配方式）
    const isAssignee = task.assignedToName === currentUser?.realName || 
                      task.assignedToName === currentUser?.username ||
                      task.assignedToId === currentUser?.id
    
    // 检查是否为创建者（使用多种匹配方式）
    const isCreator = task.createdByUserName === currentUser?.realName || 
                     task.createdByUserName === currentUser?.username ||
                     task.createdByUserId === currentUser?.id
    
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

// 检查是否为管理员
const isAdmin = () => {
  return authStore.user?.role === 'ADMIN'
}

// 检查是否为主任务创建者
const isMainTaskCreator = (task) => {
  const currentUser = authStore.user
  return task.taskLevel === 'MAIN' && 
         (task.createdByUserName === currentUser?.realName || 
          task.createdByUserName === currentUser?.username ||
          task.createdByUserId === currentUser?.id)
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
    manDays: null,
    actualManDays: null,
    progressNotes: '',
    taskLevel: 'MAIN',
    parentTaskId: null,
    subtaskWeight: 1.00,
    taskType: 'VERSION'
  })
  // 重置手动输入标记
  isActualManDaysManual.value = false
  if (taskFormRef.value) {
    taskFormRef.value.resetFields()
  }
}

const getStatusType = (status) => {
  const types = {
    'PLANNED': 'info',
    'IN_PROGRESS': 'primary',
    'ON_HOLD': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PLANNED': '计划中',
    'IN_PROGRESS': '进行中',
    'ON_HOLD': '已暂停',
    'COMPLETED': '已完成',
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
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 原来的自动计算函数，现在改为不自动执行
const calculateManDays = () => {
  // 不再自动计算，只有手动点击才计算
  return;
};

// 手动计算工时的函数
const calculateManDaysManually = () => {
  if (!taskForm.startDate || !taskForm.expectedEndDate || !taskForm.participantCount) {
    ElMessage.warning('请先选择开始时间、预计结束时间和投入人数');
    return;
  }

  const start = new Date(taskForm.startDate);
  const end = new Date(taskForm.expectedEndDate);
  
  // 验证日期的合理性
  if (start > end) {
    ElMessage.error('开始时间不能晚于预计结束时间');
    return;
  }

  // 计算工作日数，如果开始时间等于结束时间，按1天计算
  let workDays;
  if (start.getTime() === end.getTime()) {
    workDays = 1; // 同一天按1个工作日计算
  } else {
    workDays = calculateWorkDays(start, end);
  }
  
  // 工时 = 工作日 × 参与人数
  const calculatedManDays = parseFloat((workDays * taskForm.participantCount).toFixed(1));
  taskForm.manDays = calculatedManDays;
  
  ElMessage.success(`已计算工时：${calculatedManDays} 人天（${workDays} 工作日 × ${taskForm.participantCount} 人）`);
};

// 计算实际工时（自动触发）
const calculateActualManDays = () => {
  // 如果是手动输入，不进行自动计算
  if (isActualManDaysManual.value) {
    return;
  }
  
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

// 手动计算实际工时的函数
const calculateActualManDaysManually = () => {
  if (!taskForm.startDate || !taskForm.actualEndDate || !taskForm.participantCount) {
    ElMessage.warning('请先选择开始时间、实际结束时间和投入人数');
    return;
  }

  const start = new Date(taskForm.startDate);
  const end = new Date(taskForm.actualEndDate);

  // 验证日期的合理性
  if (start > end) {
    ElMessage.error('开始时间不能晚于实际结束时间');
    return;
  }

  // 计算工作日数，如果开始时间等于结束时间，按1天计算
  let workDays;
  if (start.getTime() === end.getTime()) {
    workDays = 1; // 同一天按1个工作日计算
  } else {
    workDays = calculateWorkDays(start, end);
  }
  
  // 工时 = 工作日 × 参与人数
  const calculatedActualManDays = parseFloat((workDays * taskForm.participantCount).toFixed(1));
  taskForm.actualManDays = calculatedActualManDays;
  
  ElMessage.success(`已计算实际工时：${calculatedActualManDays} 人天（${workDays} 工作日 × ${taskForm.participantCount} 人）`);
};

// 处理实际工时手动输入
const onActualManDaysChange = (value) => {
  // 标记为手动输入
  isActualManDaysManual.value = true;
  taskForm.actualManDays = value;
};

// 处理进度变化
const onProgressChange = (value) => {
  // 如果进度设置为100%，自动将状态设置为已完成
  if (value === 100) {
    taskForm.status = 'COMPLETED';
  }
  // 如果进度从100%降低，且状态是已完成，则根据进度调整状态
  else if (value < 100 && taskForm.status === 'COMPLETED') {
    if (value === 0) {
      taskForm.status = 'PLANNED';
    } else {
      taskForm.status = 'IN_PROGRESS';
    }
  }
};

// 处理状态变化
const onStatusChange = (value) => {
  // 如果状态设置为已完成，自动将进度设置为100%
  if (value === 'COMPLETED') {
    taskForm.progressPercentage = 100;
  }
  // 如果状态从已完成改为其他状态，且进度是100%，则根据状态调整进度
  else if (value !== 'COMPLETED' && taskForm.progressPercentage === 100) {
    if (value === 'PLANNED') {
      taskForm.progressPercentage = 0;
    } else if (value === 'IN_PROGRESS') {
      taskForm.progressPercentage = 50; // 默认设置为50%
    } else {
      taskForm.progressPercentage = 0; // 其他状态设置为0
    }
  }
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

// ========================================
// 子任务相关方法
// ========================================

// 默认加载任务树（包含展开功能）

// 智能判断任务类型和显示逻辑
const getTaskTypeText = (taskType) => {
  if (taskType === 'VERSION') return '版本测试'
  if (taskType === 'REQUIREMENT') return '需求测试'
  return '未知类型'
}

const getTaskTypeTagType = (taskType) => {
  if (taskType === 'VERSION') return 'primary'
  if (taskType === 'REQUIREMENT') return 'success'
  return 'info'
}

// 判断任务是否可以添加子任务
const canAddSubTask = (task) => {
  return task.taskLevel === 'MAIN' && task.taskType === 'VERSION'
}


// 为主任务创建子任务
const createSubTaskForParent = (parentTask) => {
  parentTaskForSub.value = parentTask
  taskForm.taskLevel = 'SUB'
  taskForm.parentTaskId = parentTask.id
  // 继承父任务的部分属性
  taskForm.department = parentTask.department
  taskForm.projectName = parentTask.projectName
  taskForm.taskType = parentTask.taskType || 'VERSION' // 继承父任务类型
  taskForm.assignedToName = authStore.user?.realName || ''
  showCreateDialog.value = true
}

// 加载主任务选项
const loadMainTaskOptions = async () => {
  try {
    const response = await getTasks({ 
      size: 1000,
      taskLevel: 'MAIN'
    })
    mainTaskOptions.value = response.content || []
  } catch (error) {
    console.error('加载主任务选项失败:', error)
  }
}

// 更新子任务进度对话框
const updateSubTaskProgressDialog = (subTask) => {
  selectedSubTask.value = subTask
  selectedTask.value = subTask // 确保selectedTask也设置为子任务
  progressForm.value.progressPercentage = subTask.progressPercentage || 0
  progressForm.value.progressNotes = ''
  progressForm.value.actualEndDate = subTask.actualEndDate || ''
  progressForm.value.actualManDays = subTask.actualManDays || null
  showAddProgressDialog.value = true
}

// 需求测试任务进度更新对话框
const updateRequirementTaskProgressDialog = (task) => {
  selectedTask.value = task
  progressForm.value.progressPercentage = task.progressPercentage || 0
  progressForm.value.progressNotes = ''
  progressForm.value.actualEndDate = task.actualEndDate || ''
  progressForm.value.actualManDays = task.actualManDays || null
  showAddProgressDialog.value = true
}

// 版本测试主任务进度更新对话框（手动更新）
const updateMainTaskProgressDialog = (mainTask) => {
  ElMessageBox.prompt('请输入新的进度百分比', '更新版本测试主任务进度', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^(100|[1-9]?\d)$/,
    inputErrorMessage: '请输入0-100之间的整数',
    inputValue: mainTask.progressPercentage?.toString() || '0'
  }).then(async ({ value }) => {
    try {
      const progressPercentage = parseInt(value)
      await updateMainTaskProgressManually(mainTask.id, progressPercentage)
      ElMessage.success('版本测试主任务进度更新成功')
      loadAllData() // 重新加载所有数据
    } catch (error) {
      ElMessage.error(error.response?.data || '更新主任务进度失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 创建子任务
const createSubTaskForMain = async (parentTaskId, subTaskData) => {
  try {
    await createSubTask(parentTaskId, subTaskData)
    ElMessage.success('子任务创建成功')
    loadAllData()
  } catch (error) {
    ElMessage.error('创建子任务失败')
    console.error('Create subtask error:', error)
  }
}

// 更新子任务进度
const updateSubTaskProgressValue = async (subTaskId, progressPercentage) => {
  try {
    await updateSubTaskProgress(subTaskId, progressPercentage)
    ElMessage.success('子任务进度更新成功')
    loadAllData()
  } catch (error) {
    ElMessage.error('更新子任务进度失败')
    console.error('Update subtask progress error:', error)
  }
}

// 删除子任务
const deleteSubTaskById = async (subTaskId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个子任务吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteSubTask(subTaskId)
    ElMessage.success('子任务删除成功')
    loadAllData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除子任务失败')
      console.error('Delete subtask error:', error)
    }
  }
}

// 生命周期
onMounted(() => {
  loadUsers()
  loadDepartments()
  loadMainTaskOptions()
  // 非管理员默认显示自己的任务
  if (authStore.user?.role !== 'ADMIN') {
    assignedToFilter.value = authStore.user?.realName || authStore.user?.username || ''
  }
  // 加载所有任务数据（统一接口）
  loadAllData()
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

/* 工时输入组样式 */
.man-days-input-group {
  display: flex;
  align-items: center;
  width: 100%;
}

.man-days-input-group .el-input-number {
  flex: 1;
}

.man-days-input-group .el-button {
  flex-shrink: 0;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 帮助文档对话框样式 */
.help-dialog .el-dialog__body {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.help-content {
  font-size: 14px;
  line-height: 1.6;
}

.help-section {
  padding: 15px 0;
}

.help-section h4 {
  color: #303133;
  margin: 15px 0 10px 0;
  font-size: 16px;
  font-weight: 600;
}

.help-section h5 {
  color: #409EFF;
  margin: 10px 0 5px 0;
  font-size: 14px;
  font-weight: 600;
}

.help-section ul {
  margin: 10px 0;
  padding-left: 20px;
}

.help-section li {
  margin: 8px 0;
  line-height: 1.6;
}

.help-section ul ul {
  margin: 5px 0;
  padding-left: 20px;
}

.help-section ul ul li {
  margin: 5px 0;
  color: #606266;
}

.tip-box {
  background-color: #e1f3d8;
  border: 1px solid #b3d8a4;
  border-radius: 6px;
  padding: 12px;
  margin: 15px 0;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.tip-box .el-icon {
  color: #67c23a;
  margin-top: 2px;
  flex-shrink: 0;
}

.example-box {
  background-color: #f0f9ff;
  border: 1px solid #b3e5fc;
  border-radius: 6px;
  padding: 12px;
  margin: 15px 0;
}

.example-box h5 {
  margin: 0 0 8px 0;
  color: #0277bd;
}

.example-box p {
  margin: 5px 0;
  color: #37474f;
}

.warning-box {
  background-color: #fef0e6;
  border: 1px solid #f5c6cb;
  border-radius: 6px;
  padding: 12px;
  margin: 15px 0;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.warning-box .el-icon {
  color: #e6a23c;
  margin-top: 2px;
  flex-shrink: 0;
}

.help-section strong {
  color: #303133;
  font-weight: 600;
}

/* 折叠面板自定义样式 */
.help-content .el-collapse-item__header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-left: 10px;
}

.help-content .el-collapse-item__content {
  padding: 0 10px 20px 10px;
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

/* ========================================
 * 子任务相关样式
 * ======================================== */

/* 任务名称单元格 */
.task-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.main-task-icon {
  color: #409EFF;
}

.sub-task-icon {
  color: #67C23A;
}

.main-task-name {
  font-weight: 600;
  color: #303133;
}

.sub-task-name {
  color: #606266;
}

.subtask-count {
  margin-left: 8px;
}

.task-type-tag {
  margin-left: 8px;
  margin-right: 4px;
}

/* 进度单元格 */
.progress-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.auto-progress-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.auto-progress-hint .el-icon {
  font-size: 14px;
}

/* 任务类型选择 */
.task-type-option {
  display: flex;
  align-items: center;
  gap: 6px;
}

.task-type-option .el-icon {
  font-size: 16px;
}

/* 视图切换按钮组 */
.header-actions .el-radio-group {
  border-radius: 6px;
  overflow: hidden;
}

.header-actions .el-radio-button__inner {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
}

/* 自动进度标签样式 */
.auto-progress-tag {
  margin-left: 8px;
  vertical-align: middle;
}

.auto-progress-tag .el-icon {
  margin-right: 2px;
}


/* 树形表格样式增强 */
.el-table .el-table__row--level-1 .main-task-name {
  font-weight: 700;
  font-size: 15px;
}

.el-table .el-table__row--level-2 .sub-task-name {
  font-size: 14px;
  color: #666;
}

/* 子任务行样式 */
.el-table .el-table__row .el-table__cell {
  border-bottom: 1px solid #f0f2f5;
}

.el-table .el-table__row--level-2 {
  background-color: #fafbfc;
}

.el-table .el-table__row--level-2:hover {
  background-color: #f0f9ff;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-buttons .el-button {
  margin: 0;
  flex-shrink: 0;
}

.action-buttons .el-button + .el-button {
  margin-left: 0;
}
</style> 