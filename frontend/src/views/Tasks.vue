<template>
  <div class="tasks-container">
         <div class="page-header">
       <h1>测试任务管理</h1>
               <div class="header-actions">
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
      <div v-loading="loading" class="table-container">
        <!-- 表头 -->
        <div class="custom-table-header">
          <div class="header-cell" style="min-width: 200px; max-width: 200px;">任务名称</div>
          <div class="header-cell" style="min-width: 200px; max-width: 200px;">任务描述</div>
          <div class="header-cell" style="min-width: 120px; max-width: 120px;">部门</div>
          <div class="header-cell" style="min-width: 120px; max-width: 120px;">负责人</div>
          <div class="header-cell" style="min-width: 100px; max-width: 100px;">状态</div>
          <div class="header-cell" style="min-width: 80px; max-width: 80px;">优先级</div>
          <div class="header-cell" style="min-width: 120px; max-width: 120px;">开始日期</div>
          <div class="header-cell" style="min-width: 120px; max-width: 120px;">预计结束</div>
          <div class="header-cell" style="min-width: 100px; max-width: 100px;">投入人数</div>
          <div class="header-cell" style="min-width: 120px; max-width: 120px;">工时(人/天)</div>
          <div class="header-cell" style="min-width: 120px; max-width: 120px;">进度</div>
          <div class="header-cell" style="min-width: 100px; max-width: 100px;">超时状态</div>
          <div class="header-cell" style="min-width: 120px; max-width: 120px;">实际结束</div>
          <div class="header-cell" style="min-width: 150px; max-width: 150px;">更新时间</div>
          <div class="header-cell sticky-action-header" style="min-width: 200px; max-width: 200px;">操作</div>
        </div>

        <!-- 任务行和展开内容 -->
        <template v-for="task in filteredTasks" :key="task.id">
          <!-- 任务行 -->
          <div class="custom-table-row" :class="getRowClassName(task)">
            <!-- 任务名称列 -->
            <div class="table-cell task-name-cell" style="min-width: 200px; max-width: 200px;">
              <div class="task-name-cell">
                <div class="task-name-content">
                  <span class="task-name">{{ task.taskName }}</span>
                  <el-tag v-if="task.taskType === 'VERSION'" type="info" size="small" style="margin-left: 8px; flex-shrink: 0;">
                    版本
                  </el-tag>
                  <el-tag v-else-if="task.taskType === 'REQUIREMENT'" type="success" size="small" style="margin-left: 8px; flex-shrink: 0;">
                    需求
                  </el-tag>
                </div>
                <!-- 展开/折叠按钮 -->
                <el-button
                  v-if="task.taskType === 'VERSION'"
                  type="text"
                  size="small"
                  @click="toggleVersionExpansion(task)"
                  style="padding: 2px; margin: 0; min-width: 20px; cursor: pointer; flex-shrink: 0; margin-left: 8px;"
                >
                  <el-icon v-if="expandedVersions.includes(task.id)" style="color: #409eff;">
                    <ArrowDown />
                  </el-icon>
                  <el-icon v-else style="color: #909399;">
                    <ArrowRight />
                  </el-icon>
                </el-button>
              </div>
            </div>

            <!-- 任务描述列 -->
            <div class="table-cell task-description-cell" style="min-width: 200px; max-width: 200px;" :title="task.taskDescription">
              {{ task.taskDescription }}
            </div>

            <!-- 部门列 -->
            <div class="table-cell" style="min-width: 120px; max-width: 120px;">{{ task.department }}</div>

            <!-- 负责人列 -->
            <div class="table-cell" style="min-width: 120px; max-width: 120px;">{{ task.assignedToName }}</div>

            <!-- 状态列 -->
            <div class="table-cell" style="min-width: 100px; max-width: 100px;">
              <el-tag :type="getStatusType(task.status)">{{ getStatusText(task.status) }}</el-tag>
            </div>

            <!-- 优先级列 -->
            <div class="table-cell" style="min-width: 80px; max-width: 80px;">
              <el-tag :type="getPriorityType(task.priority)" size="small">{{ getPriorityText(task.priority) }}</el-tag>
            </div>

            <!-- 开始日期列 -->
            <div class="table-cell" style="min-width: 120px; max-width: 120px;">{{ formatDate(task.startDate) }}</div>

            <!-- 预计结束日期列 -->
            <div class="table-cell" style="min-width: 120px; max-width: 120px;">{{ formatDate(task.expectedEndDate) }}</div>

            <!-- 投入人数列 -->
            <div class="table-cell" style="min-width: 100px; max-width: 100px;">{{ task.participantCount }}</div>

            <!-- 工时列 -->
            <div class="table-cell" style="min-width: 120px; max-width: 120px;">
              <div>
                <div>预计: {{ task.manDays ? task.manDays.toFixed(1) : '-' }}</div>
                <div>实际: {{ task.actualManDays ? task.actualManDays.toFixed(1) : '-' }}</div>
              </div>
            </div>

            <!-- 进度列 -->
            <div class="table-cell" style="min-width: 120px; max-width: 120px;">
              <el-progress :percentage="task.progressPercentage || 0" />
            </div>

            <!-- 超时状态列 -->
            <div class="table-cell" style="min-width: 100px; max-width: 100px;">
              <el-tag v-if="task.isOverdue" type="danger" size="small">超时{{ task.overdueDays }}天</el-tag>
              <el-tag v-else type="success" size="small">正常</el-tag>
            </div>

            <!-- 实际结束日期列 -->
            <div class="table-cell" style="min-width: 120px; max-width: 120px;">{{ formatDate(task.actualEndDate) }}</div>

            <!-- 更新时间列 -->
            <div class="table-cell" style="min-width: 150px; max-width: 150px;">{{ formatDateTime(task.updatedTime) }}</div>

            <!-- 操作列 -->
            <div class="table-cell sticky-action-cell" style="min-width: 200px; max-width: 200px;">
              <div class="action-buttons">
                <el-button v-if="canEditTask(task)" size="small" @click="editTask(task)">编辑</el-button>
                <el-button size="small" type="info" @click="viewDetails(task)">详情</el-button>
                <el-button size="small" type="warning" @click="viewProgress(task)">进度更新</el-button>
                <el-button v-if="canDeleteTask(task)" size="small" type="danger" @click="deleteTask(task)">删除</el-button>
              </div>
            </div>
          </div>

          <!-- 版本任务的展开内容 - 直接跟在对应行后面 -->
          <div v-if="task.taskType === 'VERSION' && expandedVersions.includes(task.id)" class="custom-expand-content">
            <div class="requirements-expand">
              <div class="requirements-header">
                <h4>需求列表 ({{ task.taskName }})</h4>
                <el-button size="small" type="success" @click="createRequirementForVersion(task)">
                  <el-icon><Plus /></el-icon>
                  添加需求
                </el-button>
              </div>
              
              <div class="requirements-table-container">
                <div v-if="!task.requirements || task.requirements.length === 0" style="text-align: center; padding: 20px; color: #909399;">
                  暂无需求数据
                </div>
                
                <div v-else>
                  <!-- 需求列表表头 -->
                  <div class="requirements-table-header">
                    <div class="req-header-cell" style="min-width: 200px; max-width: 200px;">任务名称</div>
                    <div class="req-header-cell" style="min-width: 120px; max-width: 120px;">负责人</div>
                    <div class="req-header-cell" style="min-width: 100px; max-width: 100px;">状态</div>
                    <div class="req-header-cell" style="min-width: 80px; max-width: 80px;">优先级</div>
                    <div class="req-header-cell" style="min-width: 120px; max-width: 120px;">开始日期</div>
                    <div class="req-header-cell" style="min-width: 120px; max-width: 120px;">预计结束</div>
                    <div class="req-header-cell" style="min-width: 100px; max-width: 100px;">投入人数</div>
                    <div class="req-header-cell" style="min-width: 120px; max-width: 120px;">工时(人/天)</div>
                    <div class="req-header-cell" style="min-width: 120px; max-width: 120px;">进度</div>
                    <div class="req-header-cell" style="min-width: 100px; max-width: 100px;">超时状态</div>
                    <div class="req-header-cell" style="min-width: 120px; max-width: 120px;">实际结束</div>
                    <div class="req-header-cell" style="min-width: 150px; max-width: 150px;">更新时间</div>
                    <div class="req-header-cell sticky-action-header" style="min-width: 200px; max-width: 200px;">操作</div>
                  </div>
                  
                  <!-- 需求列表行 -->
                  <div v-for="(req, index) in task.requirements" :key="`req-${req.id}-${index}`" class="requirements-table-row">
                    <!-- 任务名称列 -->
                    <div class="req-table-cell req-task-name-cell" style="min-width: 200px; max-width: 200px;">
                      <div class="task-name-content">
                        <span class="task-name">{{ req.taskName || '未知任务' }}</span>
                        <el-tag type="success" size="small" style="margin-left: 0; flex-shrink: 0;">需求</el-tag>
                      </div>
                    </div>
                    
                    <!-- 负责人列 -->
                    <div class="req-table-cell" style="min-width: 120px; max-width: 120px;">{{ req.assignedToName }}</div>
                    
                    <!-- 状态列 -->
                    <div class="req-table-cell" style="min-width: 100px; max-width: 100px;">
                      <el-tag :type="getStatusType(req.status)">{{ getStatusText(req.status) }}</el-tag>
                    </div>
                    
                    <!-- 优先级列 -->
                    <div class="req-table-cell" style="min-width: 80px; max-width: 80px;">
                      <el-tag :type="getPriorityType(req.priority)" size="small">{{ getPriorityText(req.priority) }}</el-tag>
                    </div>
                    
                    <!-- 开始日期列 -->
                    <div class="req-table-cell" style="min-width: 120px; max-width: 120px;">{{ formatDate(req.startDate) }}</div>
                    
                    <!-- 预计结束日期列 -->
                    <div class="req-table-cell" style="min-width: 120px; max-width: 120px;">{{ formatDate(req.expectedEndDate) }}</div>
                    
                    <!-- 投入人数列 -->
                    <div class="req-table-cell" style="min-width: 100px; max-width: 100px;">{{ req.participantCount }}</div>
                    
                    <!-- 工时列 -->
                    <div class="req-table-cell" style="min-width: 120px; max-width: 120px;">
                      <div>
                        <div>预计: {{ req.manDays ? req.manDays.toFixed(1) : '-' }}</div>
                        <div>实际: {{ req.actualManDays ? req.actualManDays.toFixed(1) : '-' }}</div>
                      </div>
                    </div>
                    
                    <!-- 进度列 -->
                    <div class="req-table-cell" style="min-width: 120px; max-width: 120px;">
                      <el-progress :percentage="req.progressPercentage || 0" />
                    </div>
                    
                    <!-- 超时状态列 -->
                    <div class="req-table-cell" style="min-width: 100px; max-width: 100px;">
                      <el-tag v-if="req.isOverdue" type="danger" size="small">超时{{ req.overdueDays }}天</el-tag>
                      <el-tag v-else type="success" size="small">正常</el-tag>
                    </div>
                    
                    <!-- 实际结束日期列 -->
                    <div class="req-table-cell" style="min-width: 120px; max-width: 120px;">{{ formatDate(req.actualEndDate) }}</div>
                    
                    <!-- 更新时间列 -->
                    <div class="req-table-cell" style="min-width: 150px; max-width: 150px;">{{ formatDateTime(req.updatedTime) }}</div>
                    
                    <!-- 操作列 -->
                    <div class="req-table-cell sticky-action-cell" style="min-width: 200px; max-width: 200px;">
                      <div class="action-buttons">
                        <el-button v-if="canEditTask(req)" size="small" @click="editTask(req)">编辑</el-button>
                        <el-button size="small" type="info" @click="viewDetails(req)">详情</el-button>
                        <el-button size="small" type="warning" @click="viewProgress(req)">进度更新</el-button>
                        <el-button v-if="canDeleteTask(req)" size="small" type="danger" @click="deleteTask(req)">删除</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
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
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="taskForm.taskType" placeholder="请选择任务类型" style="width: 100%" @change="handleTaskTypeChange">
            <el-option label="版本测试" value="VERSION" />
            <el-option label="需求测试" value="REQUIREMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属版本" prop="parentTaskId" v-if="taskForm.taskType === 'REQUIREMENT'">
          <el-select 
            v-model="taskForm.parentTaskId" 
            placeholder="请选择所属版本（可选）" 
            style="width: 100%"
            :disabled="!!editingTask && taskForm.parentTaskId"
            clearable
          >
            <el-option label="独立需求（不选择版本）" value="" />
            <el-option
              v-for="version in versionTasks"
              :key="version.id"
              :label="version.taskName"
              :value="version.id"
            />
          </el-select>
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
          />
        </el-form-item>
        <el-form-item label="工时(人/天)" prop="manDays">
          <el-input-number
            v-model="taskForm.manDays"
            :min="0"
            :precision="1"
            placeholder="自动计算"
            style="width: 100%"
          />
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
             
                           <!-- 版本下的需求进度显示 -->
              <div v-if="selectedTask.taskType === 'VERSION'" class="requirements-progress-section" style="margin-top: 30px;">
                <h4>版本下需求进度</h4>
                <div v-if="versionRequirements.length === 0" class="no-requirements">
                  <el-empty description="暂无需求任务" />
                </div>
                <div v-else class="requirements-progress-list">
                  <div v-for="requirement in versionRequirements" :key="requirement.id" class="requirement-progress-item">
                    <div class="requirement-header">
                      <h5>{{ requirement.taskName }}</h5>
                      <el-tag :type="getStatusType(requirement.status)" size="small">
                        {{ getStatusText(requirement.status) }}
                      </el-tag>
                    </div>
                    <div class="requirement-info">
                      <span>负责人: {{ requirement.assignedToName }}</span>
                      <span>进度: {{ requirement.progressPercentage || 0 }}%</span>
                    </div>
                    <el-progress :percentage="requirement.progressPercentage || 0" :stroke-width="8" />
                  </div>
                </div>
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
               
               <!-- 版本任务的进度历史 -->
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
               
               <!-- 版本下需求的进度历史 -->
               <div v-if="selectedTask.taskType === 'VERSION' && versionRequirements.length > 0" class="requirements-progress-section" style="margin-top: 30px;">
                 <h4>版本下需求进度历史</h4>
                 <div class="requirements-progress-timeline">
                   <div v-for="requirement in versionRequirements" :key="requirement.id" class="requirement-progress-timeline">
                     <div class="requirement-progress-header">
                       <h5>{{ requirement.taskName }}</h5>
                       <el-tag :type="getStatusType(requirement.status)" size="small">
                         {{ getStatusText(requirement.status) }}
                       </el-tag>
                     </div>
                     
                     <div v-if="requirement.progressHistory && requirement.progressHistory.length > 0" class="requirement-progress-list">
                       <div v-for="progress in requirement.progressHistory" :key="progress.id" class="requirement-progress-item">
                         <div class="requirement-progress-info">
                           <span class="requirement-progress-percentage">{{ progress.progressPercentage }}%</span>
                           <span class="requirement-progress-time">{{ formatDateTime(progress.updateTime) }}</span>
                           <span class="requirement-progress-user">更新人: {{ progress.updatedByUserName }}</span>
                         </div>
                         
                         <div v-if="progress.progressNotes" class="requirement-progress-notes">
                           <strong>进度描述:</strong>
                           <div style="white-space: pre-wrap; margin-top: 5px;">{{ progress.progressNotes }}</div>
                         </div>
                       </div>
                     </div>
                     
                     <div v-else class="no-requirement-progress">
                       <el-empty description="暂无进度记录" :image-size="60" />
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
         
         <el-form-item label="实际结束时间" prop="actualEndDate" v-if="progressForm.progressPercentage === 100">
           <el-date-picker
             v-model="progressForm.actualEndDate"
             type="date"
             placeholder="选择实际结束时间（必填）"
             value-format="YYYY-MM-DD"
             style="width: 100%"
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
           />
         </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAddProgressDialog = false">取消</el-button>
          <el-button type="primary" @click="addProgress">确定</el-button>
        </div>
      </template>
    </el-dialog>
    

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, QuestionFilled, ArrowRight, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getTasks, createTask, updateTask, deleteTask as deleteTaskApi, getTaskProgress, addTaskProgress } from '@/api/tasks'
import { getVersionTasks, getRequirementTasksByVersion, calculateVersionProgress } from '@/api/tasks'
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

// 父子任务关系数据
const versionTasks = ref([])
const expandedVersions = ref([]) // 记录展开的版本行

// 确保expandedVersions是响应式的
console.log('expandedVersions初始化:', expandedVersions.value)



// 对话框状态
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showProgressDialog = ref(false)
const showAddProgressDialog = ref(false)
const editingTask = ref(null)
const selectedTask = ref(null)
const activeTab = ref('basic')
const progressHistory = ref([])
const versionRequirements = ref([]) // 版本下的需求列表

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
  parentTaskId: null,  // 父任务ID
  taskType: 'REQUIREMENT',  // 任务类型：VERSION 或 REQUIREMENT
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
  progressNotes: ''
})

// 表单验证规则
const taskRules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  taskType: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ],
  parentTaskId: [
    { 
      validator: (rule, value, callback) => {
        // 需求测试可以选择版本，但不是必须的
        callback()
      },
      trigger: 'change'
    }
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
  progressNotes: [
    { required: false, message: '请输入进度描述', trigger: 'blur' }
  ]
}

// 计算属性
const filteredTasks = computed(() => {
  let filtered = tasks.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(task => {
      // 检查主任务
      const mainTaskMatch = 
        (task.taskName && task.taskName.toLowerCase().includes(query)) ||
        (task.taskDescription && task.taskDescription.toLowerCase().includes(query)) ||
        (task.assignedToName && task.assignedToName.toLowerCase().includes(query)) ||
        (task.department && task.department.toLowerCase().includes(query))
      
      if (mainTaskMatch) return true
      
      // 检查子需求
      if (task.requirements && task.requirements.length > 0) {
        return task.requirements.some(req => 
          (req.taskName && req.taskName.toLowerCase().includes(query)) ||
          (req.taskDescription && req.taskDescription.toLowerCase().includes(query)) ||
          (req.assignedToName && req.assignedToName.toLowerCase().includes(query)) ||
          (req.department && req.department.toLowerCase().includes(query))
        )
      }
      
      return false
    })
  }

  if (assignedToFilter.value) {
    filtered = filtered.filter(task => {
      // 检查主任务
      if (task.assignedToName === assignedToFilter.value) return true
      
      // 检查子需求
      if (task.requirements && task.requirements.length > 0) {
        return task.requirements.some(req => req.assignedToName === assignedToFilter.value)
      }
      
      return false
    })
  }

  if (departmentFilter.value) {
    filtered = filtered.filter(task => {
      // 检查主任务
      if (task.department === departmentFilter.value) return true
      
      // 检查子需求
      if (task.requirements && task.requirements.length > 0) {
        return task.requirements.some(req => req.department === departmentFilter.value)
      }
      
      return false
    })
  }

  if (statusFilter.value) {
    filtered = filtered.filter(task => {
      // 检查主任务
      if (task.status === statusFilter.value) return true
      
      // 检查子需求
      if (task.requirements && task.requirements.length > 0) {
        return task.requirements.some(req => req.status === statusFilter.value)
      }
      
      return false
    })
  }

  if (priorityFilter.value) {
    filtered = filtered.filter(task => {
      // 检查主任务
      if (task.priority === priorityFilter.value) return true
      
      // 检查子需求
      if (task.requirements && task.requirements.length > 0) {
        return task.requirements.some(req => req.priority === priorityFilter.value)
      }
      
      return false
    })
  }

  if (startDateRange.value && startDateRange.value.length === 2) {
    const [startDate, endDate] = startDateRange.value
    filtered = filtered.filter(task => {
      // 检查主任务
      const taskStartDate = new Date(task.startDate).getTime()
      const start = new Date(startDate).getTime()
      const end = new Date(endDate).getTime()
      const mainTaskMatch = taskStartDate >= start && taskStartDate <= end
      
      if (mainTaskMatch) return true
      
      // 检查子需求
      if (task.requirements && task.requirements.length > 0) {
        return task.requirements.some(req => {
          const reqStartDate = new Date(req.startDate).getTime()
          return reqStartDate >= start && reqStartDate <= end
        })
      }
      
      return false
    })
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

// 加载版本任务
const loadVersionTasks = async () => {
  try {
    const response = await getVersionTasks()
    versionTasks.value = response || []
  } catch (error) {
    console.error('加载版本任务失败:', error)
  }
}



// 表格引用







// 获取行类名
const getRowClassName = (task) => {
  if (task.taskType === 'VERSION') {
    return 'version-row'
  }
  return ''
}

// 切换版本展开状态
const toggleVersionExpansion = async (row) => {
  console.log('toggleVersionExpansion 被调用', row)
  
  const isExpanded = expandedVersions.value.includes(row.id)
  console.log('当前展开状态:', isExpanded, '版本ID:', row.id)
  
  if (isExpanded) {
    // 收起版本
    console.log('收起版本')
    expandedVersions.value = expandedVersions.value.filter(id => id !== row.id)
    row.requirements = []
  } else {
    // 展开版本
    console.log('展开版本')
    expandedVersions.value.push(row.id)
    
    try {
      console.log('开始加载需求任务，版本ID:', row.id)
      const requirements = await getRequirementTasksByVersion(row.id)
      console.log('加载到的需求任务:', requirements)
      
      // 设置需求数据
      row.requirements = requirements || []
      console.log('展开完成，需求数量:', row.requirements.length)
    } catch (error) {
      console.error('加载需求任务失败:', error)
      row.requirements = []
      expandedVersions.value = expandedVersions.value.filter(id => id !== row.id)
      ElMessage.error('加载需求任务失败: ' + (error.response?.data || error.message))
    }
  }
}







// 为指定版本创建需求任务
const createRequirementForVersion = (version) => {
  Object.assign(taskForm, {
    taskName: '',
    taskDescription: '',
    startDate: version.startDate || '', // 默认使用版本的开始时间
    expectedEndDate: version.expectedEndDate || '', // 默认使用版本的结束时间
    actualEndDate: '',
    participantCount: 1,
    manDays: 0,
    actualManDays: null,
    status: 'PLANNED',
    priority: 'MEDIUM',
    riskLevel: 'LOW',
    riskDescription: '',
    assignedToName: authStore.user?.realName || authStore.user?.username || '',
    projectName: '',
    moduleName: '',
    testType: null, // 设置为null而不是空字符串
    department: authStore.user?.department || '',
    delayReason: '',
    isDelayedCompletion: false,
    parentTaskId: version.id, // 设置为当前版本ID
    taskType: 'REQUIREMENT' // 设置为需求类型
  })
  editingTask.value = null
  showCreateDialog.value = true
}

// 处理任务类型变化
const handleTaskTypeChange = () => {
  if (taskForm.value.taskType === 'VERSION') {
    // 如果是版本测试，清空父任务ID
    taskForm.value.parentTaskId = null
  } else if (taskForm.value.taskType === 'REQUIREMENT') {
    // 如果是需求测试，需要选择父任务
    taskForm.value.parentTaskId = ''
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
      priority: priorityFilter.value || undefined,
      startDateFrom: startDateRange.value && startDateRange.value.length === 2 ? startDateRange.value[0] : undefined,
      startDateTo: startDateRange.value && startDateRange.value.length === 2 ? startDateRange.value[1] : undefined
    }
    
    const response = await getTasks(params)
    // 确保tasks数组正确初始化，并添加数据验证
    if (response && response.content) {
      // 过滤任务：只显示版本任务和独立的（没有父任务的）需求任务
      const filteredContent = response.content.filter(task => {
        // 如果是版本任务，直接显示
        if (task.taskType === 'VERSION') {
          return true
        }
        // 如果是需求任务，只有没有父任务的才显示在主列表中
        if (task.taskType === 'REQUIREMENT') {
          return !task.parentTaskId
        }
        // 其他情况不显示
        return false
      })
      
      tasks.value = filteredContent.map(task => {
        const taskObj = {
          ...task,
          taskName: task.taskName || '',
          taskDescription: task.taskDescription || '',
          assignedToName: task.assignedToName || '',
          department: task.department || '',
          status: task.status || 'PLANNED',
          priority: task.priority || 'MEDIUM',
          taskType: task.taskType || 'REQUIREMENT', // 确保taskType字段存在
          requirements: task.taskType === 'VERSION' ? [] : undefined, // 只有版本任务才初始化需求数组
          hasRequirements: task.taskType === 'VERSION' // 版本任务可能有需求
        }
        return taskObj
      })
      
      // 检查版本任务数据
      const versionTasks = tasks.value.filter(t => t.taskType === 'VERSION')
      console.log('版本任务数量:', versionTasks.length)
      
      // 清空展开状态，避免数据不一致
      expandedVersions.value = []
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
  // 清空展开状态，避免搜索结果不一致
  expandedVersions.value = []
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  // 清空展开状态，避免分页不一致
  expandedVersions.value = []
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  // 清空展开状态，避免分页不一致
  expandedVersions.value = []
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
    progressNotes: '',
    taskType: 'VERSION', // 默认为版本测试
    parentTaskId: null // 版本测试没有父任务
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
    progressNotes: '',
    taskType: 'REQUIREMENT', // 默认需求测试
    parentTaskId: null // 默认没有父任务
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
    progressNotes: task.progressNotes || '',
    taskType: task.taskType || 'REQUIREMENT', // 确保taskType正确设置
    parentTaskId: task.parentTaskId || null // 确保parentTaskId正确设置
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

    // 准备发送的数据，确保枚举字段不为空字符串
    const taskData = { ...taskForm }
    if (taskData.testType === '') {
      taskData.testType = null
    }
    
    // 确保 taskType 字段正确设置
    if (!taskData.taskType) {
      taskData.taskType = 'REQUIREMENT'
    }
    
    // 如果是需求测试但没有父任务ID，设置为null
    if (taskData.taskType === 'REQUIREMENT' && !taskData.parentTaskId) {
      taskData.parentTaskId = null
    }
    
    if (editingTask.value) {
      await updateTask(editingTask.value.id, taskData)
      ElMessage.success('任务更新成功')
    } else {
      // 新建任务
      const response = await createTask(taskData)
      ElMessage.success('任务创建成功')
      
      // 如果新建的是需求任务，重新计算父版本的进度
      if (taskForm.taskType === 'REQUIREMENT' && taskForm.parentTaskId) {
        try {
          await calculateVersionProgress(taskForm.parentTaskId)
          
          // 如果当前有展开的版本，重新加载该版本的需求列表
          if (expandedVersions.value.includes(taskForm.parentTaskId)) {
            const versionTask = tasks.value.find(t => t.id === taskForm.parentTaskId)
            if (versionTask) {
              const requirements = await getRequirementTasksByVersion(taskForm.parentTaskId)
              versionTask.requirements = requirements || []
              console.log('重新加载版本需求列表:', requirements)
            }
          }
          
          // 强制刷新展开的需求列表显示
          await loadTasks()
        } catch (progressError) {
          console.error('计算版本进度失败:', progressError)
        }
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
      manDays: 0,
      progressNotes: '',
      taskType: 'VERSION', // 默认为版本测试
      parentTaskId: null // 版本测试没有父任务
    })
    
    // 重新加载任务列表
    await loadTasks()
    
    // 如果创建的是需求任务且有父版本，重新加载该版本的展开状态
    if (taskForm.taskType === 'REQUIREMENT' && taskForm.parentTaskId && expandedVersions.value.includes(taskForm.parentTaskId)) {
      const versionTask = tasks.value.find(t => t.id === taskForm.parentTaskId)
      if (versionTask) {
        try {
          const requirements = await getRequirementTasksByVersion(taskForm.parentTaskId)
          versionTask.requirements = requirements || []
          console.log('重新加载版本需求列表:', requirements)
          // 强制更新视图
          await nextTick()
        } catch (error) {
          console.error('重新加载版本需求列表失败:', error)
        }
      }
    }
  } catch (error) {
    // 检查是否是权限不足的错误
    if (error.response?.data && typeof error.response.data === 'string' && error.response.data.includes('权限不足')) {
      ElMessage.error('权限不足，无法执行此操作')
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
    
    // 如果删除的是需求任务，重新计算父版本的进度
    if (task.taskType === 'REQUIREMENT' && task.parentTaskId) {
      try {
        await calculateVersionProgress(task.parentTaskId)
        
        // 如果当前有展开的版本，重新加载该版本的需求列表
        if (expandedVersions.value.includes(task.parentTaskId)) {
          const versionTask = tasks.value.find(t => t.id === task.parentTaskId)
          if (versionTask) {
            const requirements = await getRequirementTasksByVersion(task.parentTaskId)
            versionTask.requirements = requirements || []
          }
        }
      } catch (progressError) {
        console.error('计算版本进度失败:', progressError)
      }
    }
    
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
  
  // 如果是版本任务，加载其下的需求（包括进度历史）
  if (task.taskType === 'VERSION') {
    loadVersionRequirements(task.id)
  } else {
    versionRequirements.value = []
  }
}

// 加载版本下的需求
const loadVersionRequirements = async (versionId) => {
  try {
    const requirements = await getRequirementTasksByVersion(versionId)
    
    // 为每个需求加载进度历史
    const requirementsWithProgress = await Promise.all(
      (requirements || []).map(async (requirement) => {
        try {
          const progressResponse = await getTaskProgress(requirement.id, { page: 0, size: 100 })
          return {
            ...requirement,
            progressHistory: progressResponse?.content || []
          }
        } catch (progressError) {
          console.error(`加载需求 ${requirement.id} 的进度历史失败:`, progressError)
          return {
            ...requirement,
            progressHistory: []
          }
        }
      })
    )
    
    versionRequirements.value = requirementsWithProgress
  } catch (error) {
    console.error('加载版本需求失败:', error)
    versionRequirements.value = []
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
    
    // 如果更新的是版本任务的进度为100%，自动更新所有子需求进度为100%
    if (selectedTask.value.taskType === 'VERSION' && progressForm.value.progressPercentage === 100) {
      try {
        // 获取该版本下的所有需求任务
        const requirements = await getRequirementTasksByVersion(selectedTask.value.id)
        if (requirements && requirements.length > 0) {
          // 批量更新所有需求的进度为100%
          for (const requirement of requirements) {
            if (requirement.progressPercentage < 100) {
              const requirementProgressData = {
                progressPercentage: 100,
                progressNotes: `版本进度更新为100%，自动更新需求进度`,
                actualEndDate: progressForm.value.actualEndDate || '',
                actualManDays: requirement.actualManDays || requirement.manDays || 0,
                updatedByUserId: authStore.user.id
              }
              await addTaskProgress(requirement.id, requirementProgressData)
            }
          }
          ElMessage.success('已自动更新所有子需求进度为100%')
        }
      } catch (autoUpdateError) {
        console.error('自动更新需求进度失败:', autoUpdateError)
      }
    }
    
    // 如果更新的是需求任务的进度，重新计算父版本的进度
    if (selectedTask.value.taskType === 'REQUIREMENT' && selectedTask.value.parentTaskId) {
      try {
        await calculateVersionProgress(selectedTask.value.parentTaskId)
        
        // 如果当前有展开的版本，重新加载该版本的需求列表
        if (expandedVersions.value.includes(selectedTask.value.parentTaskId)) {
          const versionTask = tasks.value.find(t => t.id === selectedTask.value.parentTaskId)
          if (versionTask) {
            const requirements = await getRequirementTasksByVersion(selectedTask.value.parentTaskId)
            versionTask.requirements = requirements || []
            console.log('重新加载版本需求列表:', requirements)
            // 强制更新视图
            await nextTick()
          }
        }
      } catch (progressError) {
        console.error('计算版本进度失败:', progressError)
      }
    }
    
    // 重新加载任务列表以更新主任务数据
    await loadTasks()
    
    // 恢复展开状态
    const currentExpandedVersions = [...expandedVersions.value]
    expandedVersions.value = []
    await nextTick()
    expandedVersions.value = currentExpandedVersions
    
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

// 自定义tooltip功能





// 权限检查方法
const canEditTask = (task) => {
  const userRole = authStore.user?.role
  // 只有管理员和测试经理可以编辑任务
  return userRole === 'ADMIN' || userRole === 'MANAGER'
}

const canDeleteTask = (task) => {
  const userRole = authStore.user?.role
  // 管理员可以删除所有任务，测试经理可以删除需求任务
  if (userRole === 'ADMIN') {
    return true
  }
  if (userRole === 'MANAGER') {
    // 测试经理可以删除需求任务（包括版本下的需求）
    return task.taskType === 'REQUIREMENT'
  }
  return false
}

const canCreateTask = () => {
  const userRole = authStore.user?.role
  // 所有角色都可以创建任务
  return true
}

// 检查是否可以创建版本下的需求
const canCreateRequirementForVersion = () => {
  const userRole = authStore.user?.role
  // 所有角色都可以创建版本下的需求
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
    progressNotes: '',
    taskType: 'REQUIREMENT', // 默认需求测试
    parentTaskId: null // 默认没有父任务
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









// 展开所有版本任务




// 生命周期
onMounted(() => {
  console.log('Tasks.vue onMounted 开始')
  console.log('初始展开状态:', expandedVersions.value)
  
  // 确保展开状态正确初始化
  expandedVersions.value = []
  
  loadUsers()
  loadDepartments()
  loadTasks()
  loadVersionTasks() // 加载版本任务
  console.log('Tasks.vue onMounted 完成')
  console.log('加载后展开状态:', expandedVersions.value)
  
  // 添加调试信息
  console.log('版本任务数量:', versionTasks.value.length)
  console.log('任务列表数量:', tasks.value.length)
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

.header-actions {
  display: flex;
  gap: 10px;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.tasks-table {
  margin-top: 20px;
  width: 100%;
}

.tasks-table .el-table {
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
}

/* 确保操作列固定且有阴影效果 */
.tasks-table .el-table__fixed-right {
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
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

/* 进度条样式优化 */
.table-cell .el-progress {
  width: 100%;
  min-width: 80px;
}

.table-cell .el-progress .el-progress-bar {
  height: 8px;
  border-radius: 4px;
  background: #f0f0f0;
}

.table-cell .el-progress .el-progress-bar__outer {
  height: 8px;
  border-radius: 4px;
  background: #f0f0f0;
}

.table-cell .el-progress .el-progress-bar__inner {
  height: 8px;
  border-radius: 4px;
  background: linear-gradient(90deg, #67c23a 0%, #409eff 100%);
  transition: all 0.3s ease;
}

.table-cell .el-progress .el-progress__text {
  font-size: 12px;
  color: #606266;
  margin-left: 8px;
}

/* 需求表格中的进度条样式 */
.req-table-cell .el-progress {
  width: 100%;
  min-width: 80px;
}

.req-table-cell .el-progress .el-progress-bar {
  height: 6px;
  border-radius: 3px;
  background: #f0f0f0;
}

.req-table-cell .el-progress .el-progress-bar__outer {
  height: 6px;
  border-radius: 3px;
  background: #f0f0f0;
}

.req-table-cell .el-progress .el-progress-bar__inner {
  height: 6px;
  border-radius: 3px;
  background: linear-gradient(90deg, #67c23a 0%, #409eff 100%);
  transition: all 0.3s ease;
}

.req-table-cell .el-progress .el-progress__text {
  font-size: 11px;
  color: #606266;
  margin-left: 6px;
}

/* 表格容器样式 - 启用水平滚动 */
.table-container {
  overflow-x: auto;
  overflow-y: hidden;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  background: #fff;
  max-width: 100%;
}

/* 限制横向滚动范围 */
.table-container::-webkit-scrollbar {
  height: 8px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.table-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 自定义表格样式 */
.custom-table-header {
  display: flex;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  color: #495057;
  border-bottom: 2px solid #dee2e6;
  font-weight: 600;
  font-size: 14px;
  min-width: 2000px; /* 确保表格有最小宽度 */
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-cell {
  padding: 16px 12px;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0 !important;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.header-cell:last-child {
  border-right: none;
}

.custom-table-row {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  transition: all 0.3s ease;
  min-width: 2000px; /* 确保表格行有最小宽度 */
}

.custom-table-row:hover {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.custom-table-row:nth-child(even) {
  background: #fafbfc;
}

.custom-table-row:nth-child(even):hover {
  background: linear-gradient(135deg, #f0f2ff 0%, #e8ebff 100%);
}

.table-cell {
  padding: 16px 12px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: #606266;
  flex-shrink: 0 !important;
  transition: all 0.3s ease;
}

/* 任务名称列支持换行 */
.table-cell:first-child {
  white-space: normal !important;
  word-wrap: break-word !important;
  word-break: break-all !important;
  align-items: flex-start !important;
  min-height: 60px !important;
  max-width: 200px !important;
  width: 200px !important;
  flex-shrink: 0 !important;
}

/* 任务描述列使用省略号 */
.table-cell:nth-child(2) {
  white-space: nowrap !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  max-width: 200px !important;
}

.table-cell:last-child {
  border-right: none;
}

/* 自定义展开内容样式 */
.custom-expand-content {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-top: none;
  margin-bottom: 0;
  border-radius: 0;
  overflow-x: auto; /* 为展开内容也添加水平滚动 */
}

.requirements-expand {
  padding: 20px;
}

.requirements-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
}

.requirements-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.requirements-table-container {
  background: white;
  border-radius: 6px;
  padding: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow-x: auto; /* 为需求表格容器添加水平滚动 */
}

.requirements-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.requirement-item {
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 15px;
  background: #fafbfc;
}

.requirement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.requirement-header h5 {
  margin: 0;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
}

.requirement-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 12px;
  color: #606266;
}

.requirement-content {
  margin-bottom: 15px;
}

.requirement-content p {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 13px;
}

.requirement-details {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}

.requirement-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
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
  gap: 6px;
  flex-wrap: nowrap;
  align-items: center;
}

.action-buttons .el-button {
  margin: 0;
  flex-shrink: 0;
  font-size: 12px;
  padding: 6px 12px;
  height: 28px;
  border-radius: 4px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
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



/* 展开行样式 */
.requirements-expand {
  padding: 15px;
  background: #f8f9fa;
  margin: 0 20px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.requirements-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.requirements-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.requirements-table {
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
}

.requirements-table .el-table {
  font-size: 12px;
}

.requirements-table .el-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
  padding: 8px 0;
}

.requirements-table .el-table td {
  padding: 6px 0;
}

.requirements-table .el-button {
  padding: 4px 8px;
  font-size: 11px;
  margin: 0 2px;
}

.no-requirements {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

/* 版本需求进度样式 */
.requirements-progress-section {
  margin-top: 30px;
}

.requirements-progress-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.requirement-progress-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  background-color: #fafafa;
}

.requirement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.requirement-header h5 {
  margin: 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.requirement-info {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
  font-size: 12px;
  color: #606266;
}

 .requirement-info span {
   display: flex;
   align-items: center;
 }
 
 /* 需求进度历史样式 */
 .requirements-progress-timeline {
   display: flex;
   flex-direction: column;
   gap: 20px;
 }
 
 .requirement-progress-timeline {
   border: 1px solid #e4e7ed;
   border-radius: 8px;
   padding: 15px;
   background-color: #fafafa;
 }
 
 .requirement-progress-header {
   display: flex;
   justify-content: space-between;
   align-items: center;
   margin-bottom: 15px;
   padding-bottom: 10px;
   border-bottom: 1px solid #e4e7ed;
 }
 
 .requirement-progress-header h5 {
   margin: 0;
   color: #303133;
   font-size: 14px;
   font-weight: 600;
 }
 
 .requirement-progress-list {
   display: flex;
   flex-direction: column;
   gap: 10px;
 }
 
 .requirement-progress-item {
   border: 1px solid #e4e7ed;
   border-radius: 6px;
   padding: 12px;
   background-color: #fff;
   margin-left: 10px;
 }
 
 .requirement-progress-info {
   display: flex;
   align-items: center;
   gap: 15px;
   margin-bottom: 8px;
 }
 
 .requirement-progress-percentage {
   font-size: 16px;
   font-weight: bold;
   color: #409eff;
 }
 
 .requirement-progress-time {
   color: #909399;
   font-size: 12px;
 }
 
 .requirement-progress-user {
   color: #606266;
   font-size: 12px;
 }
 
 .requirement-progress-notes {
   margin-top: 8px;
   padding-top: 8px;
   border-top: 1px solid #f0f0f0;
 }
 
 .requirement-progress-notes strong {
   color: #303133;
   font-size: 12px;
 }
 
 .no-requirement-progress {
   text-align: center;
   padding: 20px;
   color: #909399;
 }

/* 需求展开区域样式 */
.requirements-expand {
  padding: 20px;
  background-color: #fafafa;
}

.requirements-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.requirements-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.no-requirements {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

/* 需求表格容器样式 */
.requirements-table-container {
  overflow-x: auto;
  max-width: 100%;
  margin: 10px 0;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background-color: #fff;
}

.requirements-table {
  min-width: 100%; /* 使子需求列表适应容器宽度 */
  width: 100%;
}

.requirements-table .el-table__body-wrapper {
  overflow-x: auto;
}

/* 需求表格样式优化 - 与主表格保持一致 */
.requirements-table .el-table {
  font-size: 13px; /* 稍微小一点的字体 */
}

.requirements-table .el-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
  padding: 8px 0;
  white-space: nowrap;
}

.requirements-table .el-table td {
  padding: 8px 0;
  font-size: 13px;
}

/* 需求表格中的按钮样式优化 */
.requirements-table .el-button {
  margin: 1px;
  padding: 3px 6px;
  font-size: 11px;
  height: 22px;
  line-height: 1;
}

/* 需求表格中的进度条样式 */
.requirements-table .el-progress {
  margin: 0;
}

.requirements-table .el-progress__text {
  font-size: 11px;
}

/* 需求表格中的标签样式 */
.requirements-table .el-tag {
  font-size: 11px;
  height: 20px;
  line-height: 18px;
}

/* 需求表格中的操作列固定 */
.requirements-table .el-table__fixed-right {
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
}

/* 需求操作按钮样式 */
.requirement-action-buttons {
  display: flex;
  gap: 2px;
  flex-wrap: wrap;
}

.requirement-action-buttons .el-button {
  font-size: 11px;
  padding: 3px 6px;
  height: 22px;
  line-height: 1;
  margin: 0;
}



/* 版本行样式 */
.version-row {
  background-color: #f8f9fa;
}

/* 展开按钮样式 */
.el-button--text {
  color: #409eff;
}

.el-button--text:hover {
  color: #66b1ff;
}

/* 确保展开内容可见 */
.requirements-expand {
  background-color: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin: 10px 20px;
  padding: 15px;
}



/* 优化操作按钮区域 */
.action-buttons {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.action-buttons .el-button {
  font-size: 12px;
  padding: 4px 8px;
  height: 24px;
  line-height: 1;
  margin: 0;
}

/* 任务名称单元格样式 */
.task-name-cell {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 4px;
}

.task-name {
  font-weight: 600;
  color: #303133;
  margin-left: 8px;
  word-wrap: break-word !important;
  word-break: break-all !important;
  flex: 1;
  max-width: 100%;
  overflow-wrap: break-word;
  line-height: 1.4;
}

/* 标签样式优化 */
.el-tag {
  border-radius: 4px;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  font-size: 11px;
  padding: 2px 6px;
  height: 20px;
  line-height: 16px;
}

.el-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

/* 版本行样式 */
.version-row {
  background-color: #f8f9fa;
}

/* 新的任务列表布局样式 */
.task-item {
  margin-bottom: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
}

.task-main-row {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.task-main-row.version-task {
  background: #f8f9fa;
  border-left: 4px solid #409eff;
}

.task-info {
  display: flex;
  align-items: center;
  min-width: 300px;
  flex-shrink: 0;
}

.task-name {
  font-weight: 600;
  color: #303133;
  margin-left: 8px;
}

.task-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0 20px;
}

.task-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.4;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #909399;
}

.task-meta span {
  white-space: nowrap;
}

.task-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.requirements-expand {
  background: #fafafa;
  padding: 20px;
  border-top: 1px solid #e4e7ed;
}

.requirements-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.requirements-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.requirements-table-container {
  background: #fff;
  border-radius: 6px;
  padding: 15px;
  border: 1px solid #e4e7ed;
}

.requirements-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.requirement-item {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.requirement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.requirement-header h5 {
  margin: 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.requirement-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.requirement-content {
  margin-bottom: 15px;
}

.requirement-content p {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.4;
}

.requirement-details {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #909399;
}

.requirement-details span {
  white-space: nowrap;
}

.requirement-actions {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
}

.requirement-actions .el-button {
  font-size: 11px;
  padding: 3px 6px;
  height: 22px;
  line-height: 1;
}

/* 需求列表表格样式 */
.requirements-table-header {
  display: flex;
  background: linear-gradient(135deg, #ecf0f1 0%, #d5dbdb 100%);
  border-bottom: 2px solid #e4e7ed;
  font-weight: 600;
  color: #2c3e50;
  font-size: 13px;
  margin-bottom: 0;
  min-width: 2000px; /* 确保需求表格有最小宽度 */
  border-radius: 6px 6px 0 0;
}

.req-header-cell {
  padding: 12px 8px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0 !important;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.req-header-cell:last-child {
  border-right: none;
}

.requirements-table-row {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
  transition: all 0.3s ease;
  min-width: 2000px; /* 确保需求表格行有最小宽度 */
}

.requirements-table-row:hover {
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.requirements-table-row:nth-child(even) {
  background: #fafafa;
}

.requirements-table-row:nth-child(even):hover {
  background: linear-gradient(135deg, #f0f2f5 0%, #e8ebff 100%);
}

.req-table-cell {
  padding: 12px 8px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: #606266;
  flex-shrink: 0 !important;
  transition: all 0.3s ease;
}

/* 需求表格中的任务名称列支持换行 */
.req-table-cell:first-child {
  white-space: normal !important;
  word-wrap: break-word !important;
  word-break: break-all !important;
  align-items: flex-start !important;
  min-height: 50px !important;
  max-width: 200px !important;
  width: 200px !important;
  flex-shrink: 0 !important;
}

/* 需求表格中的任务描述列使用省略号 */
.req-table-cell:nth-child(2) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.req-table-cell:last-child {
  border-right: none;
}

/* 固定操作列样式 */
.sticky-action-header {
  position: sticky !important;
  right: 0 !important;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%) !important;
  z-index: 20 !important;
  box-shadow: -4px 0 8px rgba(0, 0, 0, 0.15) !important;
  backdrop-filter: blur(10px);
  color: #fff !important;
}

/* 主表格样式优化 */
.custom-table-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 20px;
}



.custom-table-body {
  overflow-x: auto;
  max-height: 600px;
  overflow-y: auto;
}

.custom-table-row {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  transition: all 0.3s ease;
  min-width: 2000px; /* 确保表格行有最小宽度 */
}

.custom-table-row:hover {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.custom-table-row:nth-child(even) {
  background: #fafbfc;
}

.custom-table-row:nth-child(even):hover {
  background: linear-gradient(135deg, #f0f2ff 0%, #e8ebff 100%);
}

.table-cell {
  padding: 16px 12px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: #606266;
  flex-shrink: 0 !important;
  transition: all 0.3s ease;
}

/* 任务名称列支持换行 */
.table-cell:first-child {
  white-space: normal !important;
  word-wrap: break-word !important;
  word-break: break-all !important;
  align-items: flex-start !important;
  min-height: 60px !important;
  max-width: 200px !important;
  width: 200px !important;
  flex-shrink: 0 !important;
}

/* 任务描述列使用省略号 */
.table-cell:nth-child(2) {
  white-space: nowrap !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  max-width: 200px !important;
  width: 200px !important;
  flex-shrink: 0 !important;
}

/* 任务名称单元格特定样式 */
.task-name-cell {
  white-space: normal !important;
  word-wrap: break-word !important;
  word-break: break-all !important;
  align-items: flex-start !important;
  min-height: 60px !important;
  max-width: 200px !important;
  width: 200px !important;
  flex-shrink: 0 !important;
  display: flex !important;
  flex-direction: row !important;
  align-items: flex-start !important;
  gap: 8px !important;
  justify-content: space-between !important;
}

.task-name-content {
  display: flex !important;
  flex-direction: column !important;
  flex: 1 !important;
  min-width: 0 !important;
  gap: 4px !important;
  max-width: calc(100% - 30px) !important;
}

/* 进度条样式优化 */
.table-cell .el-progress {
  width: 100%;
  min-width: 80px;
}

.table-cell .el-progress .el-progress-bar {
  height: 8px;
  border-radius: 4px;
  background: #f0f0f0;
}

.table-cell .el-progress .el-progress-bar__outer {
  height: 8px;
  border-radius: 4px;
  background: #f0f0f0;
}

.table-cell .el-progress .el-progress-bar__inner {
  height: 8px;
  border-radius: 4px;
  background: linear-gradient(90deg, #67c23a 0%, #409eff 100%);
  transition: all 0.3s ease;
}

.table-cell .el-progress .el-progress__text {
  font-size: 12px;
  color: #606266;
  margin-left: 8px;
}

.table-cell:last-child {
  border-right: none;
}

/* 操作按钮样式优化 */
.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
  align-items: center;
  width: 100%;
  justify-content: flex-start;
}

.action-buttons .el-button {
  margin: 0;
  flex-shrink: 0;
  font-size: 11px;
  padding: 4px 8px;
  height: 26px;
  border-radius: 4px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  min-width: auto;
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 固定操作列样式 */
.sticky-action-header {
  position: sticky !important;
  right: 0 !important;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%) !important;
  color: #495057 !important;
  z-index: 20 !important;
  box-shadow: -4px 0 8px rgba(0, 0, 0, 0.15) !important;
  backdrop-filter: blur(10px);
}

.sticky-action-cell {
  position: sticky !important;
  right: 0 !important;
  background: #fff !important;
  z-index: 20 !important;
  box-shadow: -4px 0 8px rgba(0, 0, 0, 0.15) !important;
  backdrop-filter: blur(10px);
  border-left: 2px solid #f0f0f0 !important;
}

.custom-table-row:hover .sticky-action-cell {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%) !important;
}

.custom-table-row:nth-child(even) .sticky-action-cell {
  background: #fafbfc !important;
}

.custom-table-row:nth-child(even):hover .sticky-action-cell {
  background: linear-gradient(135deg, #f0f2ff 0%, #e8ebff 100%) !important;
}

/* 自定义展开内容样式 */
.custom-expand-content {
  background: linear-gradient(135deg, #f8f9fa 0%, #f1f3f4 100%);
  border: 1px solid #e9ecef;
  border-top: none;
  margin-bottom: 0;
  border-radius: 0;
  overflow-x: auto;
  box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.05);
}

.requirements-expand {
  background: #fafafa;
  padding: 20px;
  border-top: 1px solid #e4e7ed;
}

.requirements-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e4e7ed;
}

.requirements-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.requirements-table-container {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
  max-width: 100%;
}

/* 子需求表格滚动条样式 */
.requirements-table-container::-webkit-scrollbar {
  height: 6px;
}

.requirements-table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.requirements-table-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.requirements-table-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 需求列表表格样式 */
.requirements-table-header {
  display: flex;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 2px solid #dee2e6;
  font-weight: 600;
  color: #495057;
  font-size: 13px;
  margin-bottom: 0;
  min-width: 2000px;
  border-radius: 6px 6px 0 0;
}

.req-header-cell {
  padding: 12px 8px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0 !important;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.req-header-cell:last-child {
  border-right: none;
}

.requirements-table-row {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
  transition: all 0.3s ease;
  min-width: 2000px;
}

.requirements-table-row:hover {
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.requirements-table-row:nth-child(even) {
  background: #fafafa;
}

.requirements-table-row:nth-child(even):hover {
  background: linear-gradient(135deg, #f0f2f5 0%, #e8ebff 100%);
}

.req-table-cell {
  padding: 12px 8px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: #606266;
  flex-shrink: 0 !important;
  transition: all 0.3s ease;
}

/* 需求表格中的任务名称列支持换行 */
.req-table-cell:first-child {
  white-space: normal !important;
  word-wrap: break-word !important;
  word-break: break-all !important;
  align-items: flex-start !important;
  min-height: 50px !important;
  max-width: 200px !important;
  width: 200px !important;
  flex-shrink: 0 !important;
}

/* 需求表格中的任务描述列使用省略号 */
.req-table-cell:nth-child(2) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 需求表格中的任务名称单元格特定样式 */
.req-task-name-cell {
  white-space: normal !important;
  word-wrap: break-word !important;
  word-break: break-all !important;
  align-items: flex-start !important;
  min-height: 50px !important;
  max-width: 200px !important;
  width: 200px !important;
  flex-shrink: 0 !important;
  display: flex !important;
  flex-direction: row !important;
  align-items: flex-start !important;
  gap: 8px !important;
  justify-content: space-between !important;
}

/* 需求表格中的进度条样式 */
.req-table-cell .el-progress {
  width: 100%;
  min-width: 80px;
}

.req-table-cell .el-progress .el-progress-bar {
  height: 6px;
  border-radius: 3px;
  background: #f0f0f0;
}

.req-table-cell .el-progress .el-progress-bar__outer {
  height: 6px;
  border-radius: 3px;
  background: #f0f0f0;
}

.req-table-cell .el-progress .el-progress-bar__inner {
  height: 6px;
  border-radius: 3px;
  background: linear-gradient(90deg, #67c23a 0%, #409eff 100%);
  transition: all 0.3s ease;
}

.req-table-cell .el-progress .el-progress__text {
  font-size: 11px;
  color: #606266;
  margin-left: 6px;
}

.req-table-cell:last-child {
  border-right: none;
}

/* 需求表格中的操作按钮样式 */
.requirements-table-row .action-buttons {
  display: flex;
  gap: 3px;
  flex-wrap: nowrap;
  align-items: center;
  width: 100%;
  justify-content: flex-start;
}

.requirements-table-row .action-buttons .el-button {
  font-size: 10px;
  padding: 3px 6px;
  height: 22px;
  line-height: 1;
  margin: 0;
  flex-shrink: 0;
  border-radius: 3px;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  min-width: auto;
}

.requirements-table-row .action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

/* 需求表格固定操作列样式 */
.requirements-table-row .sticky-action-cell {
  position: sticky !important;
  right: 0 !important;
  background: #fff !important;
  z-index: 30 !important;
  box-shadow: -4px 0 8px rgba(0, 0, 0, 0.15) !important;
  backdrop-filter: blur(10px);
  width: 200px !important;
  max-width: 200px !important;
  min-width: 200px !important;
  border-left: 2px solid #e4e7ed !important;
}

.requirements-table-row .sticky-action-header {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%) !important;
  color: #495057 !important;
}

.requirements-table-row:hover .sticky-action-cell {
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%) !important;
}

.requirements-table-row:nth-child(even) .sticky-action-cell {
  background: #fafafa !important;
}

.requirements-table-row:nth-child(even):hover .sticky-action-cell {
  background: linear-gradient(135deg, #f0f2f5 0%, #e8ebff 100%) !important;
}

/* 任务名称样式优化 */
.task-name {
  font-weight: 600;
  color: #303133;
  margin-left: 0;
  word-wrap: break-word !important;
  word-break: break-all !important;
  max-width: 100%;
  overflow-wrap: break-word;
  line-height: 1.4;
  flex: 1;
  min-width: 0;
}

/* 分页样式优化 */
.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 响应式优化 */
@media (max-width: 1200px) {
  .custom-table-header,
  .custom-table-row,
  .requirements-table-header,
  .requirements-table-row {
    min-width: 1800px;
  }
}

@media (max-width: 768px) {
  .custom-table-header,
  .custom-table-row,
  .requirements-table-header,
  .requirements-table-row {
    min-width: 1600px;
  }
  
  .header-cell,
  .table-cell,
  .req-header-cell,
  .req-table-cell {
    padding: 12px 8px;
    font-size: 12px;
  }
}

</style> 