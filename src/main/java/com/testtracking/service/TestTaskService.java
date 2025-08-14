package com.testtracking.service;

import com.testtracking.dto.TestTaskDto;
import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import com.testtracking.repository.TestTaskRepository;
import com.testtracking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TestTaskService {

    private final TestTaskRepository testTaskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 创建测试任务
     */
    public TestTaskDto createTask(TestTaskDto taskDto, String currentUsername) {
        log.info("创建测试任务: {}", taskDto.getTaskName());
        log.info("任务类型: {}, 父任务ID: {}, 测试类型: {}", 
                taskDto.getTaskType(), taskDto.getParentTaskId(), taskDto.getTestType());
        
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        TestTask task = new TestTask();
        task.setTaskName(taskDto.getTaskName());
        task.setTaskDescription(taskDto.getTaskDescription());
        task.setParentTaskId(taskDto.getParentTaskId());
        // 处理任务类型，确保不为空
        TestTask.TaskType taskType = taskDto.getTaskType();
        if (taskType == null) {
            // 如果有父任务ID，说明是需求测试
            if (taskDto.getParentTaskId() != null) {
                taskType = TestTask.TaskType.REQUIREMENT;
            } else {
                taskType = TestTask.TaskType.VERSION;
            }
        }
        task.setTaskType(taskType);
        task.setStartDate(taskDto.getStartDate());
        task.setExpectedEndDate(taskDto.getExpectedEndDate());
        task.setParticipantCount(taskDto.getParticipantCount());
        task.setPriority(taskDto.getPriority() != null ? taskDto.getPriority() : TestTask.TaskPriority.MEDIUM);
        task.setRiskLevel(taskDto.getRiskLevel() != null ? taskDto.getRiskLevel() : TestTask.RiskLevel.LOW);
        task.setRiskDescription(taskDto.getRiskDescription());
        task.setProjectName(taskDto.getProjectName());
        task.setModuleName(taskDto.getModuleName());
        task.setTestType(taskDto.getTestType());
        task.setDepartment(taskDto.getDepartment());
        task.setProgressPercentage(taskDto.getProgressPercentage());
        task.setStatus(taskDto.getStatus() != null ? taskDto.getStatus() : TestTask.TaskStatus.PLANNED);
        task.setDelayReason(taskDto.getDelayReason());
        task.setIsDelayedCompletion(false);
        task.setCreatedByUser(currentUser);
        
        // 设置负责人
        if (taskDto.getAssignedToName() != null) {
            User assignedTo = userRepository.findByRealName(taskDto.getAssignedToName())
                    .orElseThrow(() -> new RuntimeException("指定的负责人不存在: " + taskDto.getAssignedToName()));
            task.setAssignedTo(assignedTo);
        }
        
        // 设置工时 - 如果前端提供了工时，使用前端值；否则自动计算
        if (taskDto.getManDays() != null && taskDto.getManDays() > 0) {
            task.setManDays(taskDto.getManDays());
        } else {
            // 只有在工时为空或0时才自动计算
            task.calculateManDays();
        }
        
        // 检查是否超时
        task.checkOverdue();
        
        TestTask savedTask = testTaskRepository.save(task);
        
        // 发送任务分配通知
        if (savedTask.getAssignedTo() != null) {
            notificationService.sendTaskAssignedNotification(savedTask, currentUser.getRealName());
        }
        
        return TestTaskDto.fromEntity(savedTask);
    }

    /**
     * 更新任务
     */
    public TestTaskDto updateTask(Long taskId, TestTaskDto taskDto, String currentUsername) {
        log.info("更新任务: {} -> {}", taskId, taskDto.getTaskName());
        
        TestTask task = testTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("测试任务不存在: " + taskId));
        
        // 检查权限：只有负责人或管理员可以修改任务
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        if (!canModifyTask(task, currentUser)) {
            throw new RuntimeException(getModifyTaskErrorMessage(task, currentUser));
        }
        
        // 更新基本信息
        task.setTaskName(taskDto.getTaskName());
        task.setTaskDescription(taskDto.getTaskDescription());
        task.setParentTaskId(taskDto.getParentTaskId());
        // 处理任务类型，确保不为空
        TestTask.TaskType taskType = taskDto.getTaskType();
        if (taskType == null) {
            // 如果有父任务ID，说明是需求测试
            if (taskDto.getParentTaskId() != null) {
                taskType = TestTask.TaskType.REQUIREMENT;
            } else {
                taskType = task.getTaskType(); // 保持原有类型
            }
        }
        task.setTaskType(taskType);
        task.setPriority(taskDto.getPriority());
        task.setStatus(taskDto.getStatus());
        task.setStartDate(taskDto.getStartDate());
        task.setExpectedEndDate(taskDto.getExpectedEndDate());
        task.setActualEndDate(taskDto.getActualEndDate());
        task.setParticipantCount(taskDto.getParticipantCount());
        task.setProjectName(taskDto.getProjectName());
        task.setModuleName(taskDto.getModuleName());
        task.setTestType(taskDto.getTestType());
        task.setDepartment(taskDto.getDepartment());
        task.setProgressPercentage(taskDto.getProgressPercentage());
        task.setDelayReason(taskDto.getDelayReason());
        
        // 检查是否延期完成
        if (taskDto.getActualEndDate() != null && taskDto.getExpectedEndDate() != null && 
            taskDto.getActualEndDate().isAfter(taskDto.getExpectedEndDate())) {
            task.setIsDelayedCompletion(true);
        } else {
            task.setIsDelayedCompletion(false);
        }
        
        // 更新负责人
        if (taskDto.getAssignedToName() != null) {
            User assignedTo = userRepository.findByRealName(taskDto.getAssignedToName())
                    .orElseThrow(() -> new RuntimeException("指定的负责人不存在: " + taskDto.getAssignedToName()));
            task.setAssignedTo(assignedTo);
        }
        
        // 更新工时 - 如果前端提供了工时，使用前端值；否则保持原值
        if (taskDto.getManDays() != null && taskDto.getManDays() >= 0) {
            task.setManDays(taskDto.getManDays());
        }
        // 如果没有提供工时，保持原有的工时值不变
        
        // 检查是否超时
        task.checkOverdue();
        
        TestTask savedTask = testTaskRepository.save(task);
        return TestTaskDto.fromEntity(savedTask);
    }

    /**
     * 更新任务进度
     */
    public TestTaskDto updateTaskProgress(Long taskId, Integer progressPercentage, String currentUsername) {
        log.info("更新任务进度: {} -> {}%", taskId, progressPercentage);
        
        TestTask task = testTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("测试任务不存在: " + taskId));
        
        // 检查权限：只有负责人或管理员可以更新进度
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        if (!canUpdateProgress(task, currentUser)) {
            throw new RuntimeException("没有权限更新此任务进度");
        }
        
        task.updateProgress(progressPercentage);
        task.checkOverdue();
        
        TestTask savedTask = testTaskRepository.save(task);
        
        // 发送进度更新通知
        notificationService.sendProgressUpdateNotification(savedTask, progressPercentage, currentUser.getRealName());
        
        // 如果任务完成，发送完成通知
        if (savedTask.getStatus() == TestTask.TaskStatus.COMPLETED) {
            notificationService.sendTaskCompletedNotification(savedTask, currentUser.getRealName());
        }
        
        return TestTaskDto.fromEntity(savedTask);
    }

    /**
     * 根据ID获取任务
     */
    @Transactional(readOnly = true)
    public TestTaskDto getTaskById(Long taskId) {
        TestTask task = testTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("测试任务不存在: " + taskId));
        return TestTaskDto.fromEntity(task);
    }

    /**
     * 获取用户的任务
     */
    @Transactional(readOnly = true)
    public List<TestTaskDto> getTasksByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        return testTaskRepository.findByAssignedTo(user).stream()
                .map(TestTaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询任务
     */
    @Transactional(readOnly = true)
    public Page<TestTaskDto> getTasksWithFilters(User assignedTo, String assignedToName, String department,
                                                TestTask.TaskStatus status, 
                                                TestTask.TaskPriority priority, String projectName, 
                                                TestTask.TestType testType, 
                                                LocalDate startDateFrom, LocalDate startDateTo,
                                                Long parentTaskId, TestTask.TaskType taskType,
                                                Pageable pageable) {
        Page<TestTask> tasks = testTaskRepository.findByFilters(assignedTo, assignedToName, department, 
                                                               status, priority, projectName, testType, 
                                                               startDateFrom, startDateTo, parentTaskId, taskType, pageable);
        return tasks.map(TestTaskDto::fromEntity);
    }

    /**
     * 获取超时任务
     */
    @Transactional(readOnly = true)
    public List<TestTaskDto> getOverdueTasks() {
        return testTaskRepository.findByIsOverdueTrue().stream()
                .map(TestTaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有版本任务（顶级任务）
     */
    @Transactional(readOnly = true)
    public List<TestTaskDto> getVersionTasks() {
        return testTaskRepository.findByParentTaskIdIsNullAndTaskType(TestTask.TaskType.VERSION).stream()
                .map(TestTaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定版本下的需求任务
     */
    @Transactional(readOnly = true)
    public List<TestTaskDto> getRequirementTasksByVersion(Long versionTaskId) {
        return testTaskRepository.findByParentTaskIdAndTaskType(versionTaskId, TestTask.TaskType.REQUIREMENT).stream()
                .map(TestTaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 计算版本任务的总体进度
     */
    @Transactional
    public void calculateVersionProgress(Long versionTaskId) {
        List<TestTask> requirements = testTaskRepository.findByParentTaskIdAndTaskType(versionTaskId, TestTask.TaskType.REQUIREMENT);
        if (!requirements.isEmpty()) {
            double avgProgress = requirements.stream()
                .mapToInt(TestTask::getProgressPercentage)
                .average()
                .orElse(0.0);
            
            TestTask versionTask = testTaskRepository.findById(versionTaskId).orElse(null);
            if (versionTask != null) {
                versionTask.setProgressPercentage((int) avgProgress);
                testTaskRepository.save(versionTask);
            }
        }
    }

    /**
     * 获取任务的层级结构
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTaskHierarchy() {
        Map<String, Object> hierarchy = new HashMap<>();
        
        // 获取所有版本任务
        List<TestTask> versionTasks = testTaskRepository.findByParentTaskIdIsNullAndTaskType(TestTask.TaskType.VERSION);
        
        List<Map<String, Object>> versions = new ArrayList<>();
        for (TestTask versionTask : versionTasks) {
            Map<String, Object> version = new HashMap<>();
            version.put("version", TestTaskDto.fromEntity(versionTask));
            
            // 获取该版本下的需求任务
            List<TestTask> requirementTasks = testTaskRepository.findByParentTaskIdAndTaskType(versionTask.getId(), TestTask.TaskType.REQUIREMENT);
            List<TestTaskDto> requirements = requirementTasks.stream()
                .map(TestTaskDto::fromEntity)
                .collect(Collectors.toList());
            version.put("requirements", requirements);
            
            versions.add(version);
        }
        
        hierarchy.put("versions", versions);
        return hierarchy;
    }

    /**
     * 搜索任务
     */
    @Transactional(readOnly = true)
    public List<TestTaskDto> searchTasks(String keyword) {
        return testTaskRepository.findByKeyword(keyword).stream()
                .map(TestTaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 删除任务
     */
    public void deleteTask(Long taskId, String currentUsername) {
        log.info("删除测试任务: {}", taskId);
        
        TestTask task = testTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("测试任务不存在: " + taskId));
        
        // 检查权限：只有创建者或管理员可以删除
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        if (!canDeleteTask(task, currentUser)) {
            throw new RuntimeException("没有权限删除此任务");
        }
        
        testTaskRepository.delete(task);
    }

    /**
     * 获取任务统计信息
     */
    @Transactional(readOnly = true)
    public long countTasksByStatus(TestTask.TaskStatus status) {
        return testTaskRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countOverdueTasks() {
        return testTaskRepository.countOverdueTasks();
    }

    @Transactional(readOnly = true)
    public long countTasksByRiskLevel(TestTask.RiskLevel riskLevel) {
        return testTaskRepository.countByRiskLevel(riskLevel);
    }

    /**
     * 获取本周任务统计
     */
    @Transactional(readOnly = true)
    public long countTasksThisWeek(LocalDate weekStart, LocalDate weekEnd) {
        return testTaskRepository.countTasksThisWeek(weekStart, weekEnd);
    }

    @Transactional(readOnly = true)
    public long countTasksEndingThisWeek(LocalDate weekStart, LocalDate weekEnd) {
        return testTaskRepository.countTasksEndingThisWeek(weekStart, weekEnd);
    }

    /**
     * 获取本月任务统计
     */
    @Transactional(readOnly = true)
    public long countTasksThisMonth(LocalDate monthStart, LocalDate monthEnd) {
        return testTaskRepository.countTasksThisMonth(monthStart, monthEnd);
    }

    @Transactional(readOnly = true)
    public long countCompletedTasksThisMonth(LocalDate monthStart, LocalDate monthEnd) {
        return testTaskRepository.countCompletedTasksThisMonth(monthStart, monthEnd);
    }

    /**
     * 获取人天统计
     */
    @Transactional(readOnly = true)
    public Double sumManDaysByStatus(TestTask.TaskStatus status) {
        return testTaskRepository.sumManDaysByStatus(status);
    }

    @Transactional(readOnly = true)
    public Double sumManDaysByDateRange(LocalDate startDate, LocalDate endDate) {
        return testTaskRepository.sumManDaysByDateRange(startDate, endDate);
    }

    /**
     * 获取项目统计
     */
    @Transactional(readOnly = true)
    public List<Object[]> getProjectStatistics() {
        return testTaskRepository.countByProject();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getStatusStatistics() {
        return testTaskRepository.countByStatusGroup();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getPriorityStatistics() {
        return testTaskRepository.countByPriorityGroup();
    }

    /**
     * 获取本月个人任务统计
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserTaskStatistics() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        
        List<Object[]> results = testTaskRepository.countByUserThisMonth(monthStart, monthEnd);
        List<Map<String, Object>> userStats = new ArrayList<>();
        
        for (Object[] result : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("name", result[0]);
            stat.put("value", result[1]);
            userStats.add(stat);
        }
        
        return userStats;
    }

    /**
     * 检查用户是否为任务负责人（用于权限控制）
     */
    public boolean isTaskAssignee(Long taskId, String username) {
        try {
            TestTask task = testTaskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            return task.getAssignedTo() != null && task.getAssignedTo().getId().equals(user.getId());
        } catch (Exception e) {
            log.error("检查任务负责人权限失败: {}", e.getMessage());
            return false;
        }
    }

    // 权限检查方法
    private boolean canModifyTask(TestTask task, User currentUser) {
        // ADMIN可以修改所有任务
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            return true;
        }
        
        // MANAGER可以修改所有任务
        if (currentUser.getRole() == User.UserRole.MANAGER) {
            return true;
        }
        
        // TESTER只能修改分配给自己的任务或自己创建的任务
        if (currentUser.getRole() == User.UserRole.TESTER) {
            boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId());
            boolean isCreator = task.getCreatedByUser() != null && task.getCreatedByUser().getId().equals(currentUser.getId());
            return isAssignee || isCreator;
        }
        
        return false;
    }
    
    // 获取权限检查错误信息
    private String getModifyTaskErrorMessage(TestTask task, User currentUser) {
        if (currentUser.getRole() == User.UserRole.TESTER) {
            boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId());
            boolean isCreator = task.getCreatedByUser() != null && task.getCreatedByUser().getId().equals(currentUser.getId());
            
            if (!isAssignee && !isCreator) {
                String assigneeName = task.getAssignedTo() != null ? task.getAssignedTo().getRealName() : "未分配";
                String creatorName = task.getCreatedByUser() != null ? task.getCreatedByUser().getRealName() : "未知";
                return String.format("权限不足：您只能修改分配给您的任务或您创建的任务。当前任务负责人：%s，创建者：%s", assigneeName, creatorName);
            }
        }
        return "权限不足：您没有权限修改此任务";
    }

    private boolean canUpdateProgress(TestTask task, User currentUser) {
        return currentUser.getRole() == User.UserRole.ADMIN ||
               (task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId()));
    }

    private boolean canDeleteTask(TestTask task, User currentUser) {
        return currentUser.getRole() == User.UserRole.ADMIN ||
               task.getCreatedByUser().getId().equals(currentUser.getId());
    }

    /**
     * 定时检查超时任务
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void checkOverdueTasks() {
        log.info("开始检查超时任务...");
        
        List<TestTask> allTasks = testTaskRepository.findAll();
        int updatedCount = 0;
        int alertCount = 0;
        
        for (TestTask task : allTasks) {
            boolean wasOverdue = task.getIsOverdue();
            task.checkOverdue();
            
            if (wasOverdue != task.getIsOverdue()) {
                testTaskRepository.save(task);
                updatedCount++;
                
                // 如果任务变为超时状态，发送通知
                if (task.getIsOverdue()) {
                    try {
                        notificationService.sendTaskOverdueNotification(task);
                        alertCount++;
                    } catch (Exception e) {
                        log.error("为超时任务发送通知失败: taskId={}, error={}", task.getId(), e.getMessage());
                    }
                }
            }
        }
        
        log.info("超时任务检查完成，更新了 {} 个任务，创建了 {} 个告警", updatedCount, alertCount);
    }

    /**
     * 手动检查并更新超时任务状态
     */
    public void updateOverdueStatus() {
        log.info("手动检查超时任务状态...");
        
        List<TestTask> allTasks = testTaskRepository.findAll();
        int updatedCount = 0;
        
        for (TestTask task : allTasks) {
            boolean wasOverdue = task.getIsOverdue();
            task.checkOverdue();
            
            if (wasOverdue != task.getIsOverdue()) {
                testTaskRepository.save(task);
                updatedCount++;
            }
        }
        
        log.info("超时任务状态更新完成，更新了 {} 个任务", updatedCount);
    }
} 