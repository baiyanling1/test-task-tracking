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
      <el-row :gutter="12">
        <el-col :span="3">
          <el-input
            v-model="searchQuery"
            placeholder="搜索任务"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="2">
          <el-select v-model="departmentFilter" placeholder="部门" clearable @change="handleDepartmentChange">
            <el-option label="全部" value="" />
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.name"
            />
          </el-select>
        </el-col>
        <el-col :span="2">
          <el-select v-model="assignedToFilter" placeholder="负责人" clearable @change="handleSearch">
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
          <el-select v-model="statusFilter" placeholder="状态" multiple clearable @change="handleSearch">
            <el-option label="计划中" value="PLANNED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已暂停" value="ON_HOLD" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-col>
        <el-col :span="2">
          <el-select v-model="priorityFilter" placeholder="优先级" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-col>
        <el-col :span="2">
          <el-select v-model="taskTypeFilter" placeholder="任务类型" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="版本" value="VERSION" />
            <el-option label="需求" value="REQUIREMENT" />
            <el-option label="独立" value="NORMAL" />
          </el-select>
        </el-col>
        <el-col :span="2">
          <el-select v-model="overdueFilter" placeholder="超时状态" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="超期" value="overdue" />
            <el-option label="正常" value="normal" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-date-picker
            v-model="startDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="1">
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
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :cell-style="{ verticalAlign: 'middle' }"
        @header-dragend="handleColumnResize"
      >
        <el-table-column 
          prop="taskName" 
          label="任务名称" 
          :width="columnWidths.taskName || undefined"
          min-width="350" 
          show-overflow-tooltip
          resizable
        >
          <template #default="{ row }">
            <div class="task-name-cell" :style="{ paddingLeft: row.parentId ? '24px' : '0', display: 'flex', alignItems: 'center', gap: '6px' }">
              <!-- 任务类型标签 -->
              <el-tag 
                v-if="row.taskType === 'VERSION'" 
                type="primary" 
                size="small"
              >版本</el-tag>
              <el-tag 
                v-else-if="row.taskType === 'REQUIREMENT'" 
                type="success" 
                size="small"
              >需求</el-tag>
              
              <!-- 任务名称和版本号（用括号更紧凑） -->
              <span style="flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                {{ row.taskName }}
                <span v-if="row.versionCode" style="color: #909399; font-size: 12px;">
                  ({{ row.versionCode }})
                </span>
              </span>
              
              <!-- 需求个数标签 -->
              <el-tag 
                v-if="row.taskType === 'VERSION' && row.childCount" 
                type="info" 
                size="small"
                style="flex-shrink: 0;"
              >{{ row.completedChildCount || 0 }}/{{ row.childCount }}需求</el-tag>
            </div>
          </template>
        </el-table-column>
          <!-- <el-table-column prop="taskDescription" label="描述" min-width="200">
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
        <el-table-column prop="department" label="部门" width="120" /> -->
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
        <el-table-column prop="participantCount" label="投入人数" width="100" />
        <el-table-column prop="manDays" label="工时(人/天)" width="160">
          <template #default="{ row }">
            <div>预计：{{ row.manDays ? row.manDays.toFixed(1) : '-' }}</div>
            <div>实际：{{ row.actualManDays ? row.actualManDays.toFixed(1) : '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="progressPercentage" label="进度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.progressPercentage || 0" />
          </template>
        </el-table-column>
        <el-table-column label="超时状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.isOverdue && row.actualEndDate" type="danger" size="small">
              延期完成{{ row.overdueDays }}天
            </el-tag>
            <el-tag v-else-if="row.isOverdue && !row.actualEndDate" type="warning" size="small">
              超预期{{ row.overdueDays }}天
            </el-tag>
            <el-tag v-else-if="row.isExpectedCompletionReached && !row.actualEndDate" type="info" size="small">
              已到预期时间
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
                 <el-table-column label="操作" width="350" fixed="right" align="center">
           <template #default="{ row }">
             <div class="action-buttons">
               <el-button 
                 v-if="row.taskType === 'VERSION'" 
                 size="small" 
                 type="success"
                 @click="addRequirement(row)"
               >添加需求</el-button>
               <el-button 
                 v-if="canEditTask(row)" 
                 size="small" 
                 @click="editTask(row)"
               >编辑</el-button>
              <el-button size="small" type="info" @click="viewDetails(row)">详情</el-button>
              <el-button 
                v-if="row.taskType !== 'VERSION' || (row.taskType === 'VERSION' && !row.childCount)"
                size="small" 
                type="warning" 
                @click="viewProgress(row)"
              >进度更新</el-button>
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
      :title="editingTask ? '编辑任务' : (parentVersionTask ? `添加需求 - ${parentVersionTask.taskName}` : '新建任务')"
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
        <!-- 添加需求时显示所属版本，否则显示任务类型选择 -->
        <el-form-item v-if="parentVersionTask" label="所属版本">
          <el-tag type="primary" size="large">{{ parentVersionTask.taskName }} {{ parentVersionTask.versionCode }}</el-tag>
        </el-form-item>
        <el-form-item v-else label="任务类型" prop="taskType">
          <el-radio-group v-model="taskForm.taskType" @change="handleTaskTypeChange">
            <el-radio-button value="NORMAL">独立任务</el-radio-button>
            <el-radio-button value="VERSION">版本</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="taskForm.taskType === 'VERSION' && !parentVersionTask" label="版本号">
          <el-input 
            v-model="taskForm.versionCode" 
            placeholder="请输入版本号，如 V3.1.0"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-select 
            v-model="taskForm.department" 
            placeholder="选择部门" 
            style="width: 100%"
            @change="handleFormDepartmentChange"
          >
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
          <el-select 
            v-model="taskForm.assignedToName" 
            placeholder="选择负责人" 
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="user in formFilteredUsers"
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
          <div class="man-days-input-group" data-field="manDays">
            <el-input-number
              v-model="taskForm.manDays"
              :min="0.1"
              :precision="1"
              placeholder="请输入或点击计算"
              style="width: calc(100% - 80px)"
              :disabled="editingTask && !isAdmin()"
              :class="{ 'calculated-value': isManDaysCalculated }"
              @change="isManDaysCalculated = false"
            />
            <el-button
              type="primary"
              size="small"
              :disabled="!taskForm.startDate || !taskForm.expectedEndDate || !taskForm.participantCount || (editingTask && !isAdmin())"
              @click="showManDaysCalculationDialog"
              style="margin-left: 8px; width: 72px;"
            >
              计算
            </el-button>
          </div>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            <el-icon><InfoFilled /></el-icon>
            点击"计算"按钮根据时间区间和参与人数自动计算，<span style="color: #e6a23c; font-weight: bold;">请仔细核对后再使用</span>
          </div>
          <div v-if="isManDaysCalculated" style="font-size: 12px; color: #67c23a; margin-top: 5px;">
            <el-icon><CircleCheck /></el-icon>
            已使用自动计算值，如不准确请手动调整
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
            @change="handleActualEndDateChange"
          />
        </el-form-item>
        <el-form-item label="实际工时(人/天)" prop="actualManDays" v-if="taskForm.status === 'COMPLETED' || taskForm.actualEndDate">
          <div class="man-days-input-group" data-field="actualManDays">
            <el-input-number
              v-model="taskForm.actualManDays"
              :min="0.1"
              :precision="1"
              :step="0.5"
              placeholder="请输入或点击计算"
              style="width: calc(100% - 80px)"
              @change="onActualManDaysChange"
              :class="{ 'calculated-value': isActualManDaysCalculated }"
            />
            <el-button
              type="primary"
              size="small"
              :disabled="!taskForm.startDate || !taskForm.actualEndDate || !taskForm.participantCount"
              @click="showActualManDaysCalculationDialog"
              style="margin-left: 8px; width: 72px;"
            >
              计算
            </el-button>
          </div>
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">
            <el-icon><InfoFilled /></el-icon>
            点击"计算"按钮根据开始时间和实际结束时间自动计算，<span style="color: #e6a23c; font-weight: bold;">请仔细核对后再使用</span>
          </div>
          <div v-if="isActualManDaysCalculated" style="font-size: 12px; color: #67c23a; margin-top: 5px;">
            <el-icon><CircleCheck /></el-icon>
            已使用自动计算值，如不准确请手动调整
          </div>
          <div v-if="taskForm.manDays && taskForm.actualManDays && Math.abs(taskForm.actualManDays - taskForm.manDays) > taskForm.manDays * 0.3" 
               style="font-size: 12px; color: #f56c6c; margin-top: 5px;">
            <el-icon><WarningFilled /></el-icon>
            实际工时与预计工时偏差超过30%（{{ ((Math.abs(taskForm.actualManDays - taskForm.manDays) / taskForm.manDays * 100).toFixed(0)) }}%），请检查是否正确
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
            
            <!-- 进度历史显示（普通任务 或 没有子需求的版本任务） -->
            <div v-if="selectedTask?.taskType !== 'VERSION' || (selectedTask?.taskType === 'VERSION' && !selectedTask?.childCount)" class="progress-section" style="margin-top: 30px;">
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
                  
                  <!-- 显示本周投入时间 -->
                  <div v-if="progress.workStartTime && progress.workEndTime" class="work-time-info" style="margin-top: 10px; padding: 8px 12px; background: #f0f9ff; border-left: 3px solid #409eff; border-radius: 4px;">
                    <div style="display: flex; align-items: center; gap: 8px; color: #409eff; font-weight: 600; margin-bottom: 5px;">
                      <el-icon><Clock /></el-icon>
                      <span>本周投入时间</span>
                    </div>
                    <div style="font-size: 13px; color: #606266; line-height: 1.8;">
                      <div>开始：{{ formatDateTime(progress.workStartTime) }}</div>
                      <div>结束：{{ formatDateTime(progress.workEndTime) }}</div>
                      <div style="font-weight: 600; color: #67c23a;">时长：{{ progress.workHours }} 小时</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 版本任务有子需求时：显示子需求的进度历史 -->
            <div v-if="selectedTask?.taskType === 'VERSION' && selectedTask?.childCount > 0 && childrenProgressHistory.length > 0" class="progress-section" style="margin-top: 30px;">
              <h4>子需求更新历史</h4>
               <div class="progress-timeline">
                 <div v-for="progress in childrenProgressHistory" :key="`child-${progress.id}`" class="progress-item" style="border-left: 3px solid #67c23a; padding-left: 12px;">
                   <div class="progress-header">
                     <div class="progress-info" style="display: flex; align-items: center; flex-wrap: wrap; gap: 8px;">
                       <span style="font-weight: 600; color: #409eff;">【{{ progress.childTaskName }}】</span>
                       <span class="progress-percentage" style="color: #67c23a; font-weight: bold;">{{ progress.progressPercentage }}%</span>
                       <span class="progress-time" style="color: #909399;">{{ formatDateTime(progress.updateTime) }}</span>
                     </div>
                     <div class="progress-user" style="color: #606266; font-size: 13px;">
                       更新人: {{ progress.updatedByUserName }}
                     </div>
                   </div>
                   <div v-if="progress.progressNotes" class="progress-notes" style="margin-top: 8px; padding: 8px; background: #f5f7fa; border-radius: 4px;">
                     <strong>进度描述:</strong>
                     <div style="white-space: pre-wrap; margin-top: 5px;">{{ progress.progressNotes }}</div>
                   </div>
                   
                   <!-- 显示本周投入时间 -->
                   <div v-if="progress.workStartTime && progress.workEndTime" class="work-time-info" style="margin-top: 10px; padding: 8px 12px; background: #f0f9ff; border-left: 3px solid #409eff; border-radius: 4px;">
                     <div style="display: flex; align-items: center; gap: 8px; color: #409eff; font-weight: 600; margin-bottom: 5px;">
                       <el-icon><Clock /></el-icon>
                       <span>本周投入时间</span>
                     </div>
                     <div style="font-size: 13px; color: #606266; line-height: 1.8;">
                       <div>开始：{{ formatDateTime(progress.workStartTime) }}</div>
                       <div>结束：{{ formatDateTime(progress.workEndTime) }}</div>
                       <div style="font-weight: 600; color: #67c23a;">时长：{{ progress.workHours }} 小时</div>
                     </div>
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
                  
                  <!-- 显示本周投入时间 -->
                  <div v-if="progress.workStartTime && progress.workEndTime" class="work-time-info" style="margin-top: 10px; padding: 8px 12px; background: #f0f9ff; border-left: 3px solid #409eff; border-radius: 4px;">
                    <div style="display: flex; align-items: center; gap: 8px; color: #409eff; font-weight: 600; margin-bottom: 5px;">
                      <el-icon><Clock /></el-icon>
                      <span>本周投入时间</span>
                    </div>
                    <div style="font-size: 13px; color: #606266; line-height: 1.8;">
                      <div>开始：{{ formatDateTime(progress.workStartTime) }}</div>
                      <div>结束：{{ formatDateTime(progress.workEndTime) }}</div>
                      <div style="font-weight: 600; color: #67c23a;">时长：{{ progress.workHours }} 小时</div>
                    </div>
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
         
         <!-- 新增：本周投入时间记录（可选） -->
         <el-divider>本周投入时间（可选）</el-divider>
         
         <el-form-item label="投入开始时间">
           <el-date-picker
             v-model="progressForm.workStartTime"
             type="datetime"
             placeholder="选择投入开始时间"
             value-format="YYYY-MM-DD HH:mm:ss"
             style="width: 100%"
             @change="calculateWorkHours"
           />
         </el-form-item>
         
         <el-form-item label="投入结束时间">
           <el-date-picker
             v-model="progressForm.workEndTime"
             type="datetime"
             placeholder="选择投入结束时间"
             value-format="YYYY-MM-DD HH:mm:ss"
             style="width: 100%"
             @change="calculateWorkHours"
           />
         </el-form-item>
         
         <el-form-item label="投入时长" v-if="progressForm.workHours !== null && progressForm.workHours !== undefined">
           <el-tag size="large" type="success">
             <el-icon><Clock /></el-icon>
             {{ progressForm.workHours }} 小时
           </el-tag>
         </el-form-item>
         
         <el-divider />
         
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
           <div class="man-days-input-group" data-field="progressActualManDays">
             <el-input-number
               v-model="progressForm.actualManDays"
               :min="0"
               :precision="1"
               :step="0.5"
               placeholder="请输入实际工时（必填）"
               style="flex: 1;"
               :class="{ 'calculated-value': isProgressManDaysCalculated }"
               @change="isProgressManDaysCalculated = false"
             />
             <el-button 
               type="primary" 
               size="small"
               @click="showProgressActualManDaysDialog"
               style="margin-left: 8px;"
             >
               计算
             </el-button>
           </div>
           <div style="font-size: 12px; color: #909399; margin-top: 5px;">
             <el-icon><InfoFilled /></el-icon>
             点击"计算"按钮根据开始时间和实际结束时间自动计算，<span style="color: #e6a23c; font-weight: bold;">请仔细核对后再使用</span>
           </div>
           <div v-if="isProgressManDaysCalculated" style="font-size: 12px; color: #67c23a; margin-top: 5px;">
             <el-icon><CircleCheck /></el-icon>
             已使用自动计算值，如不准确请手动调整
           </div>
           <div v-if="selectedTask?.manDays && progressForm.actualManDays && Math.abs(progressForm.actualManDays - selectedTask.manDays) > selectedTask.manDays * 0.3" 
                style="font-size: 12px; color: #f56c6c; margin-top: 5px;">
             <el-icon><WarningFilled /></el-icon>
             实际工时与预计工时偏差超过30%（{{ ((Math.abs(progressForm.actualManDays - selectedTask.manDays) / selectedTask.manDays * 100).toFixed(0)) }}%），请检查是否正确
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

    <!-- 预计工时计算确认对话框 -->
    <el-dialog
      v-model="showManDaysConfirmDialog"
      title="预计工时自动计算"
      width="550px"
      :close-on-click-modal="false"
    >
      <div class="calculation-dialog">
        <el-alert
          title="请仔细检查计算结果"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div><strong>自动计算仅供参考，请根据实际情况调整</strong></div>
            <div style="margin-top: 8px; font-size: 13px; line-height: 1.6;">
              计算公式：<code style="background: #f5f7fa; padding: 2px 6px; border-radius: 3px;">工作日数 × 参与人数 = 工时</code><br/>
              <span style="color: #e6a23c;">⚠️ 未考虑：任务复杂度、人员投入比例、会议培训等因素</span>
            </div>
          </template>
        </el-alert>
        
        <el-descriptions :column="1" border size="default">
          <el-descriptions-item label="开始时间" label-class-name="desc-label">
            <strong>{{ formatDate(taskForm.startDate) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="预计结束时间" label-class-name="desc-label">
            <strong>{{ formatDate(taskForm.expectedEndDate) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="时间跨度" label-class-name="desc-label">
            {{ calculationDetails.totalDays }} 天
            <el-tag size="small" type="info" style="margin-left: 8px;">已排除 {{ calculationDetails.weekends }} 个周末</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="工作日数" label-class-name="desc-label">
            <el-tag type="primary" size="large" style="font-size: 15px;">{{ calculationDetails.workDays }} 天</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="参与人数" label-class-name="desc-label">
            <strong>{{ taskForm.participantCount }}</strong> 人
          </el-descriptions-item>
          <el-descriptions-item label="计算结果" label-class-name="desc-label">
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-tag type="success" size="large" class="result-tag">
                <span style="font-size: 18px; font-weight: bold;">{{ calculationDetails.calculatedManDays }} 人天</span>
              </el-tag>
              <span style="color: #909399; font-size: 13px;">
                ({{ calculationDetails.workDays }} × {{ taskForm.participantCount }})
              </span>
            </div>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 智能警告提示 -->
        <div v-if="calculationDetails.warnings.length > 0" style="margin-top: 15px;">
          <el-alert
            v-for="(warning, index) in calculationDetails.warnings"
            :key="index"
            :title="warning.title"
            :type="warning.type"
            :closable="false"
            show-icon
            style="margin-bottom: 10px;"
          >
            <template #default>
              <div style="font-size: 13px;">{{ warning.message }}</div>
            </template>
          </el-alert>
        </div>
        
        <!-- 手动调整区域 -->
        <div style="margin-top: 20px; padding: 16px; background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%); border-radius: 8px; border: 1px solid #dcdfe6;">
          <div style="margin-bottom: 12px; color: #606266; font-weight: 600; display: flex; align-items: center; gap: 6px;">
            <el-icon style="color: #409eff;"><Edit /></el-icon>
            如计算结果不准确，可手动调整：
          </div>
          <el-input-number
            v-model="adjustedManDays"
            :min="0.1"
            :precision="1"
            :step="0.5"
            controls-position="right"
            style="width: 100%;"
          />
          <div style="margin-top: 10px; font-size: 12px; color: #909399; line-height: 1.5;">
            💡 <strong>建议考虑因素：</strong><br/>
            • 任务实际复杂度（简单/中等/复杂）<br/>
            • 人员投入比例（是否全职投入）<br/>
            • 会议、培训、突发事项等
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showManDaysConfirmDialog = false" size="default">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button type="primary" @click="confirmManDaysCalculation" size="default">
            <el-icon><Check /></el-icon>
            确认使用
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 实际工时计算确认对话框 -->
    <el-dialog
      v-model="showActualManDaysConfirmDialog"
      title="实际工时自动计算"
      width="550px"
      :close-on-click-modal="false"
    >
      <div class="calculation-dialog">
        <el-alert
          title="请仔细检查计算结果"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div><strong>自动计算仅供参考，请根据实际工作时长调整</strong></div>
            <div style="margin-top: 8px; font-size: 13px; line-height: 1.6;">
              计算公式：<code style="background: #f5f7fa; padding: 2px 6px; border-radius: 3px;">实际工作日数 × 参与人数 = 实际工时</code><br/>
              <span style="color: #e6a23c;">⚠️ 未考虑：加班时间、请假天数、实际投入比例等因素</span>
            </div>
          </template>
        </el-alert>
        
        <el-descriptions :column="1" border size="default">
          <el-descriptions-item label="开始时间" label-class-name="desc-label">
            <strong>{{ formatDate(taskForm.startDate) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="实际结束时间" label-class-name="desc-label">
            <strong>{{ formatDate(taskForm.actualEndDate) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="时间跨度" label-class-name="desc-label">
            {{ actualCalculationDetails.totalDays }} 天
            <el-tag size="small" type="info" style="margin-left: 8px;">已排除 {{ actualCalculationDetails.weekends }} 个周末</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="实际工作日数" label-class-name="desc-label">
            <el-tag type="primary" size="large" style="font-size: 15px;">{{ actualCalculationDetails.workDays }} 天</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="参与人数" label-class-name="desc-label">
            <strong>{{ taskForm.participantCount }}</strong> 人
          </el-descriptions-item>
          <el-descriptions-item label="预计工时" label-class-name="desc-label">
            {{ taskForm.manDays ? taskForm.manDays + ' 人天' : '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="计算结果" label-class-name="desc-label">
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-tag type="success" size="large" class="result-tag">
                <span style="font-size: 18px; font-weight: bold;">{{ actualCalculationDetails.calculatedManDays }} 人天</span>
              </el-tag>
              <span style="color: #909399; font-size: 13px;">
                ({{ actualCalculationDetails.workDays }} × {{ taskForm.participantCount }})
              </span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="与预计对比" label-class-name="desc-label" v-if="taskForm.manDays">
            <el-tag 
              :type="Math.abs(actualCalculationDetails.calculatedManDays - taskForm.manDays) <= taskForm.manDays * 0.1 ? 'success' : 
                     Math.abs(actualCalculationDetails.calculatedManDays - taskForm.manDays) <= taskForm.manDays * 0.3 ? 'warning' : 'danger'"
              size="large"
            >
              {{ actualCalculationDetails.calculatedManDays > taskForm.manDays ? '超出' : '节省' }}
              {{ Math.abs(actualCalculationDetails.calculatedManDays - taskForm.manDays).toFixed(1) }} 人天
              ({{ ((Math.abs(actualCalculationDetails.calculatedManDays - taskForm.manDays) / taskForm.manDays * 100).toFixed(0)) }}%)
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 智能警告提示 -->
        <div v-if="actualCalculationDetails.warnings.length > 0" style="margin-top: 15px;">
          <el-alert
            v-for="(warning, index) in actualCalculationDetails.warnings"
            :key="index"
            :title="warning.title"
            :type="warning.type"
            :closable="false"
            show-icon
            style="margin-bottom: 10px;"
          >
            <template #default>
              <div style="font-size: 13px;">{{ warning.message }}</div>
            </template>
          </el-alert>
        </div>
        
        <!-- 手动调整区域 -->
        <div style="margin-top: 20px; padding: 16px; background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%); border-radius: 8px; border: 1px solid #dcdfe6;">
          <div style="margin-bottom: 12px; color: #606266; font-weight: 600; display: flex; align-items: center; gap: 6px;">
            <el-icon style="color: #409eff;"><Edit /></el-icon>
            如计算结果不准确，可手动调整：
          </div>
          <el-input-number
            v-model="adjustedActualManDays"
            :min="0.1"
            :precision="1"
            :step="0.5"
            controls-position="right"
            style="width: 100%;"
          />
          <div style="margin-top: 10px; font-size: 12px; color: #909399; line-height: 1.5;">
            💡 <strong>建议考虑因素：</strong><br/>
            • 实际加班情况<br/>
            • 请假、培训等占用时间<br/>
            • 实际投入比例和工作效率
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showActualManDaysConfirmDialog = false" size="default">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button type="primary" @click="confirmActualManDaysCalculation" size="default">
            <el-icon><Check /></el-icon>
            确认使用
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 进度更新-实际工时计算确认对话框 -->
    <el-dialog
      v-model="showProgressManDaysConfirmDialog"
      title="实际工时自动计算（进度更新）"
      width="550px"
      :close-on-click-modal="false"
    >
      <div class="calculation-dialog">
        <el-alert
          title="请仔细检查计算结果"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div><strong>自动计算仅供参考，请根据实际工作时长调整</strong></div>
            <div style="margin-top: 8px; font-size: 13px; line-height: 1.6;">
              计算公式：<code style="background: #f5f7fa; padding: 2px 6px; border-radius: 3px;">实际工作日数 × 参与人数 = 实际工时</code><br/>
              <span style="color: #e6a23c;">⚠️ 未考虑：加班时间、请假天数、实际投入比例等因素</span>
            </div>
          </template>
        </el-alert>
        
        <el-descriptions :column="1" border size="default">
          <el-descriptions-item label="任务名称" label-class-name="desc-label">
            <strong>{{ selectedTask?.taskName }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间" label-class-name="desc-label">
            <strong>{{ formatDate(selectedTask?.startDate) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="实际结束时间" label-class-name="desc-label">
            <strong>{{ formatDate(progressForm.actualEndDate) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="时间跨度" label-class-name="desc-label">
            {{ progressCalculationDetails.totalDays }} 天
            <el-tag size="small" type="info" style="margin-left: 8px;">已排除 {{ progressCalculationDetails.weekends }} 个周末</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="实际工作日数" label-class-name="desc-label">
            <el-tag type="primary" size="large" style="font-size: 15px;">{{ progressCalculationDetails.workDays }} 天</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="参与人数" label-class-name="desc-label">
            <strong>{{ selectedTask?.participantCount || 1 }}</strong> 人
          </el-descriptions-item>
          <el-descriptions-item label="预计工时" label-class-name="desc-label">
            {{ selectedTask?.manDays ? selectedTask.manDays + ' 人天' : '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="计算结果" label-class-name="desc-label">
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-tag type="success" size="large" class="result-tag">
                <span style="font-size: 18px; font-weight: bold;">{{ progressCalculationDetails.calculatedManDays }} 人天</span>
              </el-tag>
              <span style="color: #909399; font-size: 13px;">
                ({{ progressCalculationDetails.workDays }} × {{ selectedTask?.participantCount || 1 }})
              </span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="与预计对比" label-class-name="desc-label" v-if="selectedTask?.manDays">
            <el-tag 
              :type="Math.abs(progressCalculationDetails.calculatedManDays - selectedTask.manDays) <= selectedTask.manDays * 0.1 ? 'success' : 
                     Math.abs(progressCalculationDetails.calculatedManDays - selectedTask.manDays) <= selectedTask.manDays * 0.3 ? 'warning' : 'danger'"
              size="large"
            >
              {{ progressCalculationDetails.calculatedManDays > selectedTask.manDays ? '超出' : '节省' }}
              {{ Math.abs(progressCalculationDetails.calculatedManDays - selectedTask.manDays).toFixed(1) }} 人天
              ({{ ((Math.abs(progressCalculationDetails.calculatedManDays - selectedTask.manDays) / selectedTask.manDays * 100).toFixed(0)) }}%)
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 智能警告提示 -->
        <div v-if="progressCalculationDetails.warnings.length > 0" style="margin-top: 15px;">
          <el-alert
            v-for="(warning, index) in progressCalculationDetails.warnings"
            :key="index"
            :title="warning.title"
            :type="warning.type"
            :closable="false"
            show-icon
            style="margin-bottom: 10px;"
          >
            <template #default>
              <div style="font-size: 13px;">{{ warning.message }}</div>
            </template>
          </el-alert>
        </div>
        
        <!-- 手动调整区域 -->
        <div style="margin-top: 20px; padding: 16px; background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%); border-radius: 8px; border: 1px solid #dcdfe6;">
          <div style="margin-bottom: 12px; color: #606266; font-weight: 600; display: flex; align-items: center; gap: 6px;">
            <el-icon style="color: #409eff;"><Edit /></el-icon>
            如计算结果不准确，可手动调整：
          </div>
          <el-input-number
            v-model="adjustedProgressManDays"
            :min="0.1"
            :precision="1"
            :step="0.5"
            controls-position="right"
            style="width: 100%;"
          />
          <div style="margin-top: 10px; font-size: 12px; color: #909399; line-height: 1.5;">
            💡 <strong>建议考虑因素：</strong><br/>
            • 实际加班情况<br/>
            • 请假、培训等占用时间<br/>
            • 实际投入比例和工作效率
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showProgressManDaysConfirmDialog = false" size="default">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button type="primary" @click="confirmProgressManDaysCalculation" size="default">
            <el-icon><Check /></el-icon>
            确认使用
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, QuestionFilled, InfoFilled, WarningFilled, Clock, CircleCheck, Edit, Check, Close } from '@element-plus/icons-vue'
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
const statusFilter = ref([]) // 改为数组支持多选
const priorityFilter = ref('')
const taskTypeFilter = ref('') // 任务类型筛选
const overdueFilter = ref('') // 超时状态筛选（包含超预期、延期完成）
const startDateRange = ref([]) // 新增：开始时间范围
const currentPage = ref(1)
const pageSize = ref(20)
const totalTasks = ref(0)

// 列宽管理 - 从 localStorage 加载或使用默认值
const COLUMN_WIDTHS_KEY = 'task_table_column_widths'
const columnWidths = ref({
  taskName: parseInt(localStorage.getItem(`${COLUMN_WIDTHS_KEY}_taskName`)) || null
})

// 处理列宽调整并保存到 localStorage
const handleColumnResize = (newWidth, oldWidth, column) => {
  const prop = column.property
  if (prop && newWidth) {
    columnWidths.value[prop] = newWidth
    localStorage.setItem(`${COLUMN_WIDTHS_KEY}_${prop}`, newWidth)
  }
}

// 对话框状态
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showProgressDialog = ref(false)
const showAddProgressDialog = ref(false)
const showHelpDialog = ref(false)
const editingTask = ref(null)
const selectedTask = ref(null)
const parentVersionTask = ref(null) // 添加需求时保存父版本任务信息
const activeTab = ref('basic')
const progressHistory = ref([])
const childrenProgressHistory = ref([]) // 子需求的进度历史
const activeHelpItems = ref(['dates']) // 默认展开第一个帮助项

// 工时计算改进 - 新增状态
const showManDaysConfirmDialog = ref(false)
const showActualManDaysConfirmDialog = ref(false)
const showProgressManDaysConfirmDialog = ref(false) // 进度更新的工时计算对话框
const isManDaysCalculated = ref(false)
const isActualManDaysCalculated = ref(false)
const isProgressManDaysCalculated = ref(false) // 进度更新的工时是否已计算
const adjustedManDays = ref(0)
const adjustedActualManDays = ref(0)
const adjustedProgressManDays = ref(0) // 进度更新的调整工时
const calculationDetails = ref({
  totalDays: 0,
  weekends: 0,
  workDays: 0,
  calculatedManDays: 0,
  warnings: []
})
const actualCalculationDetails = ref({
  totalDays: 0,
  weekends: 0,
  workDays: 0,
  calculatedManDays: 0,
  warnings: []
})
const progressCalculationDetails = ref({ // 进度更新的计算详情
  totalDays: 0,
  weekends: 0,
  workDays: 0,
  calculatedManDays: 0,
  warnings: []
})

// 进度表单
const progressForm = ref({
  progressPercentage: 0,
  progressNotes: '',
  actualEndDate: '',
  actualManDays: null,
  workStartTime: null,
  workEndTime: null,
  workHours: null
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
  progressNotes: '',
  // 层级字段
  taskType: 'NORMAL',
  parentId: null,
  versionCode: ''
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

// 表单中的负责人列表：根据表单中选择的部门过滤
const formFilteredUsers = computed(() => {
  if (!taskForm.department) {
    return users.value
  }
  return users.value.filter(user => user.department === taskForm.department)
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

// 构建任务树形结构
const buildTaskTree = (flatTasks) => {
  const taskMap = new Map()
  const rootTasks = []
  
  // 首先创建任务映射
  flatTasks.forEach(task => {
    taskMap.set(task.id, { ...task, children: [] })
  })
  
  // 构建父子关系
  flatTasks.forEach(task => {
    const current = taskMap.get(task.id)
    if (task.parentId && taskMap.has(task.parentId)) {
      // 有父任务且父任务在列表中
      const parent = taskMap.get(task.parentId)
      parent.children.push(current)
      parent.hasChildren = true
    } else if (!task.parentId) {
      // 没有父任务，是顶层任务
      rootTasks.push(current)
    }
    // 注释掉：不再把找不到父任务的子任务显示为独立任务
    // else {
    //   // 有父任务但父任务不在列表中（可能是筛选导致），作为独立任务显示
    //   rootTasks.push(current)
    // }
  })
  
  return rootTasks
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
      // 多状态筛选：将数组中的每个状态值作为独立的status参数传递
      status: statusFilter.value && statusFilter.value.length > 0 ? statusFilter.value : undefined,
      priority: priorityFilter.value || undefined,
      taskType: taskTypeFilter.value || undefined,
      isOverdue: overdueFilter.value === 'overdue' ? true : overdueFilter.value === 'normal' ? false : undefined,
      startDateFrom: startDateRange.value && startDateRange.value.length === 2 ? startDateRange.value[0] : undefined,
      startDateTo: startDateRange.value && startDateRange.value.length === 2 ? startDateRange.value[1] : undefined
    }
    
    const response = await getTasks(params)
    // 确保tasks数组正确初始化，并添加数据验证
    if (response && response.content) {
      const rawTasks = response.content.map(task => ({
        ...task,
        taskName: task.taskName || '',
        taskDescription: task.taskDescription || '',
        assignedToName: task.assignedToName || '',
        department: task.department || '',
        status: task.status || 'PLANNED',
        priority: task.priority || 'MEDIUM',
        taskType: task.taskType || 'NORMAL',
        children: task.children || [],
        hasChildren: task.hasChildren || false
      }))
      // 构建树形结构
      tasks.value = buildTaskTree(rawTasks)
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
  parentVersionTask.value = null // 清空父版本任务
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
    taskType: 'NORMAL',
    parentId: null,
    versionCode: ''
  })
  // 重置手动输入标记
  isActualManDaysManual.value = false
  showCreateDialog.value = true
}

// 添加需求任务到版本
const addRequirement = (versionTask) => {
  editingTask.value = null
  parentVersionTask.value = versionTask // 保存父版本任务信息
  Object.assign(taskForm, {
    taskName: '',
    taskDescription: '',
    department: versionTask.department || authStore.user?.department || '',
    assignedToName: '', // 需求任务需要单独指定负责人
    participantCount: 1,
    priority: 'MEDIUM',
    startDate: versionTask.startDate || '',
    expectedEndDate: versionTask.expectedEndDate || '',
    actualEndDate: '',
    progressPercentage: 0,
    status: 'PLANNED',
    delayReason: '',
    manDays: null,
    actualManDays: null,
    progressNotes: '',
    taskType: 'REQUIREMENT',
    parentId: versionTask.id,
    versionCode: ''
  })
  isActualManDaysManual.value = false
  showCreateDialog.value = true
}

// 任务类型变化处理
const handleTaskTypeChange = (newType) => {
  if (newType === 'VERSION') {
    // 版本任务默认不需要指定负责人
    taskForm.assignedToName = ''
  } else if (newType === 'NORMAL') {
    taskForm.versionCode = ''
    taskForm.parentId = null
  }
}

// 部门变化处理：清空负责人（因为负责人列表已过滤）
const handleFormDepartmentChange = () => {
  // 检查当前选择的负责人是否在新部门中
  const currentUser = users.value.find(u => u.realName === taskForm.assignedToName)
  if (!currentUser || currentUser.department !== taskForm.department) {
    taskForm.assignedToName = ''
  }
}

const handleDialogClose = () => {
  editingTask.value = null
  parentVersionTask.value = null // 清空父版本任务
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
    taskType: 'NORMAL',
    parentId: null,
    versionCode: ''
  })
  // 重置手动输入标记
  isActualManDaysManual.value = false
  // 重置工时计算标记
  isManDaysCalculated.value = false
  isActualManDaysCalculated.value = false
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
    taskType: task.taskType || 'NORMAL',
    parentId: task.parentId || null,
    versionCode: task.versionCode || ''
  })
  // 如果任务已经有实际工时值，标记为手动输入，避免被自动覆盖
  isActualManDaysManual.value = task.actualManDays && task.actualManDays > 0
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
      // 编辑任务
      await updateTask(editingTask.value.id, taskForm)
      
      // 如果进度发生变化，创建进度历史记录
      const progressChanged = taskForm.progressPercentage !== editingTask.value.progressPercentage
      if (progressChanged) {
        try {
          const progressData = {
            progressPercentage: taskForm.progressPercentage || 0,
            progressNotes: taskForm.progressNotes && taskForm.progressNotes.trim() 
              ? taskForm.progressNotes 
              : `进度更新至 ${taskForm.progressPercentage}%`,
            actualEndDate: taskForm.actualEndDate || '',
            updatedByUserId: authStore.user.id,
            actualManDays: taskForm.actualManDays || null
          }
          
          await addTaskProgress(editingTask.value.id, progressData)
          ElMessage.success('任务和进度历史更新成功')
        } catch (progressError) {
          console.error('创建进度历史失败:', progressError)
          ElMessage.warning('任务更新成功，但进度历史创建失败：' + (progressError.response?.data || progressError.message || '未知错误'))
        }
      } else {
        ElMessage.success('任务更新成功')
      }
    } else {
      // 新建任务
      const response = await createTask(taskForm)
      ElMessage.success('任务创建成功')
      
      // 如果新建任务时进度大于0，自动创建进度历史记录
      if (taskForm.progressPercentage > 0) {
        try {
          const progressData = {
            progressPercentage: taskForm.progressPercentage || 0,
            progressNotes: taskForm.progressNotes && taskForm.progressNotes.trim() 
              ? taskForm.progressNotes 
              : `初始进度 ${taskForm.progressPercentage}%`,
            actualEndDate: taskForm.actualEndDate || '',
            updatedByUserId: authStore.user.id,
            actualManDays: taskForm.actualManDays || null
          }
          
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
    
    // 重新加载任务列表
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

const viewDetails = async (task) => {
  selectedTask.value = task
  activeTab.value = 'basic'
  showProgressDialog.value = true
  // 详情页面也加载进度历史
  loadProgressHistory(task.id)
  // 如果是版本任务，加载子需求的进度历史
  if (task.taskType === 'VERSION') {
    // 使用 API 获取子任务（因为表格中的 children 可能为空）
    try {
      const { getChildTasks } = await import('@/api/tasks')
      const children = await getChildTasks(task.id)
      if (children && children.length > 0) {
        loadChildrenProgressHistory(children)
      } else {
        childrenProgressHistory.value = []
      }
    } catch (error) {
      console.error('获取子任务失败:', error)
      childrenProgressHistory.value = []
    }
  } else {
    childrenProgressHistory.value = []
  }
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

// 加载子需求的进度历史
const loadChildrenProgressHistory = async (children) => {
  try {
    const allProgress = []
    for (const child of children) {
      const response = await getTaskProgress(child.id, { page: 0, size: 50 })
      const childProgress = (response?.content || []).map(p => ({
        ...p,
        childTaskName: child.taskName,
        childTaskId: child.id
      }))
      allProgress.push(...childProgress)
    }
    // 按更新时间倒序排列
    childrenProgressHistory.value = allProgress.sort((a, b) => 
      new Date(b.updateTime) - new Date(a.updateTime)
    )
  } catch (error) {
    console.error('加载子需求进度历史失败:', error)
    childrenProgressHistory.value = []
  }
}

// 计算本周投入时长
const calculateWorkHours = () => {
  if (progressForm.value.workStartTime && progressForm.value.workEndTime) {
    const start = new Date(progressForm.value.workStartTime)
    const end = new Date(progressForm.value.workEndTime)
    
    // 验证结束时间必须大于开始时间
    if (end <= start) {
      ElMessage.error('结束时间必须大于开始时间')
      progressForm.value.workEndTime = null
      progressForm.value.workHours = null
      return
    }
    
    // 计算时长（小时）
    const diffMs = end - start
    const hours = diffMs / (1000 * 60 * 60)
    progressForm.value.workHours = Math.round(hours * 100) / 100 // 保留两位小数
    
    ElMessage.success(`已自动计算投入时长：${progressForm.value.workHours} 小时`)
  } else {
    progressForm.value.workHours = null
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
    loadTasks()
    
    // 重置表单
    progressForm.value = {
      progressPercentage: 0,
      progressNotes: '',
      actualEndDate: '',
      actualManDays: null,
      workStartTime: null,
      workEndTime: null,
      workHours: null
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
  // 清空投入时间字段
  progressForm.value.workStartTime = null
  progressForm.value.workEndTime = null
  progressForm.value.workHours = null
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

// 显示进度更新的实际工时计算对话框
const showProgressActualManDaysDialog = () => {
  if (!selectedTask.value || !selectedTask.value.startDate || !progressForm.value.actualEndDate) {
    ElMessage.warning('请先选择实际结束时间')
    return
  }

  const start = new Date(selectedTask.value.startDate)
  const end = new Date(progressForm.value.actualEndDate)
  
  if (start > end) {
    ElMessage.error('开始时间不能晚于实际结束时间')
    return
  }

  // 计算详细信息
  let workDays
  if (start.getTime() === end.getTime()) {
    workDays = 1
  } else {
    workDays = calculateWorkDays(start, end)
  }
  
  const totalDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
  const weekends = totalDays - workDays
  const participantCount = selectedTask.value.participantCount || 1
  const calculatedManDays = parseFloat((workDays * participantCount).toFixed(1))
  
  // 智能检测异常值并生成警告
  const warnings = []
  
  // 1. 与预计工时对比
  if (selectedTask.value.manDays) {
    const deviation = Math.abs(calculatedManDays - selectedTask.value.manDays)
    const deviationPercent = (deviation / selectedTask.value.manDays * 100).toFixed(0)
    
    if (deviation > selectedTask.value.manDays * 0.5) {
      warnings.push({
        title: '偏差过大',
        type: 'error',
        message: `实际工时与预计工时偏差 ${deviationPercent}%（${calculatedManDays > selectedTask.value.manDays ? '超出' : '节省'} ${deviation.toFixed(1)} 人天），请仔细检查是否正确。`
      })
    } else if (deviation > selectedTask.value.manDays * 0.3) {
      warnings.push({
        title: '偏差较大',
        type: 'warning',
        message: `实际工时与预计工时偏差 ${deviationPercent}%，建议确认数据是否准确。`
      })
    } else if (deviation <= selectedTask.value.manDays * 0.1) {
      warnings.push({
        title: '估算准确',
        type: 'success',
        message: `实际工时与预计工时偏差仅 ${deviationPercent}%，估算非常准确！`
      })
    }
  }
  
  // 2. 检查是否过大
  if (calculatedManDays > 50) {
    warnings.push({
      title: '实际工时过大',
      type: 'warning',
      message: `实际工时为 ${calculatedManDays} 人天，请确认时间范围和参与人数是否正确。`
    })
  }
  // 3. 检查是否过小
  else if (calculatedManDays < 0.5) {
    warnings.push({
      title: '实际工时较小',
      type: 'info',
      message: `实际工时为 ${calculatedManDays} 人天，建议最少设置 0.5 人天。`
    })
  }
  
  // 4. 检查是否为整数
  if (calculatedManDays % 1 === 0 && calculatedManDays > 3) {
    warnings.push({
      title: '建议精确填写',
      type: 'info',
      message: `实际工时为整数 ${calculatedManDays} 人天，建议根据实际投入情况填写更精确的值（如 ${calculatedManDays - 0.5} 或 ${calculatedManDays + 0.5}）。`
    })
  }
  
  progressCalculationDetails.value = {
    totalDays,
    weekends,
    workDays,
    calculatedManDays,
    warnings
  }
  
  // 初始化调整值为计算值
  adjustedProgressManDays.value = calculatedManDays
  
  showProgressManDaysConfirmDialog.value = true
}

// 确认使用进度更新的实际工时计算结果
const confirmProgressManDaysCalculation = () => {
  progressForm.value.actualManDays = adjustedProgressManDays.value
  isProgressManDaysCalculated.value = true
  showProgressManDaysConfirmDialog.value = false
  
  // 添加高亮动画效果
  highlightField('progressActualManDays')
  
  // 偏差提示
  if (selectedTask.value?.manDays && Math.abs(adjustedProgressManDays.value - selectedTask.value.manDays) > selectedTask.value.manDays * 0.3) {
    ElMessage.warning({
      message: `实际工时与预计工时偏差较大（${((Math.abs(adjustedProgressManDays.value - selectedTask.value.manDays) / selectedTask.value.manDays * 100).toFixed(0))}%），已确认使用`,
      duration: 5000
    })
  } else {
    ElMessage.success({
      message: `已设置实际工时：${adjustedProgressManDays.value} 人天`,
      duration: 3000
    })
  }
}

// 手动计算进度表单的实际工时（保留原有功能，改为调用新对话框）
const calculateProgressActualManDaysManually = () => {
  showProgressActualManDaysDialog()
};

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
    progressNotes: ''
  })
  // 重置手动输入标记
  isActualManDaysManual.value = false
  // 重置工时计算标记
  isManDaysCalculated.value = false
  isActualManDaysCalculated.value = false
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

// 显示预计工时计算对话框（融合版本：强制确认 + 智能检测）
const showManDaysCalculationDialog = () => {
  if (!taskForm.startDate || !taskForm.expectedEndDate || !taskForm.participantCount) {
    ElMessage.warning('请先选择开始时间、预计结束时间和投入人数')
    return
  }

  const start = new Date(taskForm.startDate)
  const end = new Date(taskForm.expectedEndDate)
  
  if (start > end) {
    ElMessage.error('开始时间不能晚于预计结束时间')
    return
  }

  // 计算详细信息
  let workDays
  if (start.getTime() === end.getTime()) {
    workDays = 1
  } else {
    workDays = calculateWorkDays(start, end)
  }
  
  const totalDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
  const weekends = totalDays - workDays
  const calculatedManDays = parseFloat((workDays * taskForm.participantCount).toFixed(1))
  
  // 智能检测异常值并生成警告
  const warnings = []
  
  // 1. 检查是否过大（超过30天）
  if (calculatedManDays > 30) {
    warnings.push({
      title: '工时较大',
      type: 'warning',
      message: `计算结果为 ${calculatedManDays} 人天，建议检查时间范围和参与人数是否正确，或考虑将任务拆分。`
    })
  }
  // 2. 检查是否过小（小于0.5天）
  else if (calculatedManDays < 0.5) {
    warnings.push({
      title: '工时较小',
      type: 'info',
      message: `计算结果为 ${calculatedManDays} 人天，建议最少设置 0.5 人天。`
    })
  }
  
  // 3. 检查是否为整数倍（可能没考虑实际情况）
  if (calculatedManDays % 1 === 0 && calculatedManDays > 5) {
    warnings.push({
      title: '建议调整',
      type: 'info',
      message: `计算结果为整数 ${calculatedManDays} 人天，实际工时通常会有小数，建议根据任务复杂度进行微调。`
    })
  }
  
  // 4. 检查人数是否合理
  if (taskForm.participantCount > 5) {
    warnings.push({
      title: '参与人数较多',
      type: 'warning',
      message: `参与人数为 ${taskForm.participantCount} 人，请确认是否所有人都全职投入此任务。`
    })
  }
  
  // 5. 检查时间跨度
  if (workDays > 20) {
    warnings.push({
      title: '时间跨度较长',
      type: 'info',
      message: `任务时间跨度 ${workDays} 个工作日，建议考虑将任务拆分为多个阶段，便于跟踪进度。`
    })
  }
  
  calculationDetails.value = {
    totalDays,
    weekends,
    workDays,
    calculatedManDays,
    warnings
  }
  
  // 初始化调整值为计算值
  adjustedManDays.value = calculatedManDays
  
  showManDaysConfirmDialog.value = true
}

// 确认使用预计工时计算结果
const confirmManDaysCalculation = () => {
  taskForm.manDays = adjustedManDays.value
  isManDaysCalculated.value = true
  showManDaysConfirmDialog.value = false
  
  // 添加高亮动画效果
  highlightField('manDays')
  
  ElMessage.success({
    message: `已设置预计工时：${adjustedManDays.value} 人天`,
    duration: 3000
  })
}

// 显示实际工时计算对话框（融合版本：强制确认 + 智能检测）
const showActualManDaysCalculationDialog = () => {
  if (!taskForm.startDate || !taskForm.actualEndDate || !taskForm.participantCount) {
    ElMessage.warning('请先选择开始时间、实际结束时间和投入人数')
    return
  }

  const start = new Date(taskForm.startDate)
  const end = new Date(taskForm.actualEndDate)
  
  if (start > end) {
    ElMessage.error('开始时间不能晚于实际结束时间')
    return
  }

  // 计算详细信息
  let workDays
  if (start.getTime() === end.getTime()) {
    workDays = 1
  } else {
    workDays = calculateWorkDays(start, end)
  }
  
  const totalDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
  const weekends = totalDays - workDays
  const calculatedManDays = parseFloat((workDays * taskForm.participantCount).toFixed(1))
  
  // 智能检测异常值并生成警告
  const warnings = []
  
  // 1. 与预计工时对比
  if (taskForm.manDays) {
    const deviation = Math.abs(calculatedManDays - taskForm.manDays)
    const deviationPercent = (deviation / taskForm.manDays * 100).toFixed(0)
    
    if (deviation > taskForm.manDays * 0.5) {
      warnings.push({
        title: '偏差过大',
        type: 'error',
        message: `实际工时与预计工时偏差 ${deviationPercent}%（${calculatedManDays > taskForm.manDays ? '超出' : '节省'} ${deviation.toFixed(1)} 人天），请仔细检查是否正确。`
      })
    } else if (deviation > taskForm.manDays * 0.3) {
      warnings.push({
        title: '偏差较大',
        type: 'warning',
        message: `实际工时与预计工时偏差 ${deviationPercent}%，建议确认数据是否准确。`
      })
    } else if (deviation <= taskForm.manDays * 0.1) {
      warnings.push({
        title: '估算准确',
        type: 'success',
        message: `实际工时与预计工时偏差仅 ${deviationPercent}%，估算非常准确！`
      })
    }
  }
  
  // 2. 检查是否过大
  if (calculatedManDays > 50) {
    warnings.push({
      title: '实际工时过大',
      type: 'warning',
      message: `实际工时为 ${calculatedManDays} 人天，请确认时间范围和参与人数是否正确。`
    })
  }
  // 3. 检查是否过小
  else if (calculatedManDays < 0.5) {
    warnings.push({
      title: '实际工时较小',
      type: 'info',
      message: `实际工时为 ${calculatedManDays} 人天，建议最少设置 0.5 人天。`
    })
  }
  
  // 4. 检查是否为整数
  if (calculatedManDays % 1 === 0 && calculatedManDays > 3) {
    warnings.push({
      title: '建议精确填写',
      type: 'info',
      message: `实际工时为整数 ${calculatedManDays} 人天，建议根据实际投入情况填写更精确的值（如 ${calculatedManDays - 0.5} 或 ${calculatedManDays + 0.5}）。`
    })
  }
  
  actualCalculationDetails.value = {
    totalDays,
    weekends,
    workDays,
    calculatedManDays,
    warnings
  }
  
  // 初始化调整值为计算值
  adjustedActualManDays.value = calculatedManDays
  
  showActualManDaysConfirmDialog.value = true
}

// 确认使用实际工时计算结果
const confirmActualManDaysCalculation = () => {
  taskForm.actualManDays = adjustedActualManDays.value
  isActualManDaysCalculated.value = true
  isActualManDaysManual.value = true
  showActualManDaysConfirmDialog.value = false
  
  // 添加高亮动画效果
  highlightField('actualManDays')
  
  // 偏差提示
  if (taskForm.manDays && Math.abs(adjustedActualManDays.value - taskForm.manDays) > taskForm.manDays * 0.3) {
    ElMessage.warning({
      message: `实际工时与预计工时偏差较大（${((Math.abs(adjustedActualManDays.value - taskForm.manDays) / taskForm.manDays * 100).toFixed(0))}%），已确认使用`,
      duration: 5000
    })
  } else {
    ElMessage.success({
      message: `已设置实际工时：${adjustedActualManDays.value} 人天`,
      duration: 3000
    })
  }
}

// 高亮字段动画
const highlightField = (fieldName) => {
  nextTick(() => {
    const element = document.querySelector(`[data-field="${fieldName}"]`)
    if (element) {
      element.classList.add('field-highlight-animation')
      setTimeout(() => {
        element.classList.remove('field-highlight-animation')
      }, 2400) // 3次动画 × 0.8秒
    }
  })
}

// 手动计算工时的函数（保留原有功能，但改为调用新的对话框）
const calculateManDaysManually = () => {
  showManDaysCalculationDialog()
};

// 手动计算实际工时的函数（保留原有功能，但改为调用新的对话框）
const calculateActualManDaysManually = () => {
  showActualManDaysCalculationDialog()
};

// 计算实际工时（自动触发）
const calculateActualManDays = () => {
  // 如果没有实际结束时间，清空实际工时
  if (!taskForm.actualEndDate) {
    taskForm.actualManDays = null;
    return;
  }
  
  // 如果是手动输入，不进行自动计算
  if (isActualManDaysManual.value) {
    return;
  }
  
  // 如果实际工时已经有值且不为0，说明可能是手动输入的，不自动覆盖
  if (taskForm.actualManDays && taskForm.actualManDays > 0) {
    return;
  }
  
  if (!taskForm.startDate || !taskForm.participantCount) {
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

// 处理实际工时手动输入
const onActualManDaysChange = (value) => {
  // 标记为手动输入
  isActualManDaysManual.value = true;
  isActualManDaysCalculated.value = false; // 清除计算标记
  taskForm.actualManDays = value;
};

// 处理进度变化
const onProgressChange = (value) => {
  // 如果进度设置为100%，自动将状态设置为已完成
  if (value === 100) {
    taskForm.status = 'COMPLETED';
  }
  // 如果进度从0变为>0，自动设置状态为进行中
  else if (value > 0 && taskForm.status === 'PLANNED') {
    taskForm.status = 'IN_PROGRESS';
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

// 处理实际结束日期变化
const onActualEndDateChange = (value) => {
  if (value && taskForm.progressPercentage < 100) {
    ElMessageBox.confirm(
      '您已填写实际结束日期，是否将任务进度更新为100%？',
      '提示',
      {
        confirmButtonText: '是，更新为100%',
        cancelButtonText: '否，保持当前进度',
        type: 'warning'
      }
    ).then(() => {
      taskForm.progressPercentage = 100;
      taskForm.status = 'COMPLETED';
    }).catch(() => {
      // 用户选择保持当前进度
    });
  }
};

// 合并处理实际结束日期变化的事件
const handleActualEndDateChange = (value) => {
  onActualEndDateChange(value);
  calculateActualManDays();
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

/* 任务名称列样式 - 不换行，超长省略 */
.task-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-name-cell span {
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 树形表格展开按钮对齐 */
:deep(.el-table__expand-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
  margin-right: 4px;
}

/* 树形表格行内容垂直居中 */
:deep(.el-table__row .el-table__cell:first-child .cell) {
  display: inline-flex !important;
  align-items: center !important;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: var(--theme-backgroundCard, #ffffff);
  border-radius: 8px;
  box-shadow: var(--theme-cardShadow, 0 2px 8px rgba(0,0,0,0.08));
}

.page-header h1 {
  margin: 0;
  color: var(--theme-text, #2c3e50);
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

/* ========== 工时计算改进样式 ========== */

/* 计算后的值高亮显示 */
.calculated-value :deep(.el-input-number__decrease),
.calculated-value :deep(.el-input-number__increase) {
  background-color: #e1f3d8 !important;
}

.calculated-value :deep(.el-input__inner) {
  background-color: #f0f9ff !important;
  border-color: #67c23a !important;
  font-weight: 600;
}

/* 字段高亮动画 */
@keyframes field-highlight {
  0%, 100% {
    background-color: transparent;
    transform: scale(1);
  }
  50% {
    background-color: #e1f3d8;
    transform: scale(1.02);
  }
}

.field-highlight-animation {
  animation: field-highlight 0.8s ease-in-out 3;
  border-radius: 4px;
}

.field-highlight-animation :deep(.el-input-number__decrease),
.field-highlight-animation :deep(.el-input-number__increase) {
  animation: field-highlight 0.8s ease-in-out 3;
}

.field-highlight-animation :deep(.el-input__inner) {
  animation: field-highlight 0.8s ease-in-out 3;
  font-weight: 600;
  border-width: 2px;
}

/* 计算对话框样式 */
.calculation-dialog {
  font-size: 14px;
}

.calculation-dialog .el-descriptions :deep(.el-descriptions__label) {
  width: 130px;
  font-weight: 600;
  background-color: #fafafa;
}

.calculation-dialog .el-descriptions :deep(.el-descriptions__content) {
  font-weight: 500;
}

.calculation-dialog .el-input-number {
  width: 100%;
}

.calculation-dialog .result-tag {
  padding: 8px 16px;
  border-radius: 6px;
}

.calculation-dialog code {
  font-family: 'Courier New', monospace;
  font-weight: 600;
}

/* 对话框footer按钮样式 */
.calculation-dialog + .el-dialog__footer .dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.calculation-dialog + .el-dialog__footer .dialog-footer .el-button {
  min-width: 100px;
  font-weight: 500;
}
</style> 