package com.testtracking.service;

import com.testtracking.dto.TestTaskDto;
import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import com.testtracking.entity.ScheduledTask;
import com.testtracking.repository.TestTaskRepository;
import com.testtracking.repository.UserRepository;
import com.testtracking.repository.ScheduledTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
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
    private final ScheduledTaskRepository scheduledTaskRepository;

    /**
     * 创建测试任务
     */
    public TestTaskDto createTask(TestTaskDto taskDto, String currentUsername) {
        log.info("创建测试任务: {}", taskDto.getTaskName());
        
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        TestTask task = new TestTask();
        task.setTaskName(taskDto.getTaskName());
        task.setTaskDescription(taskDto.getTaskDescription());
        task.setStartDate(taskDto.getStartDate());
        task.setExpectedEndDate(taskDto.getExpectedEndDate());
        task.setActualEndDate(taskDto.getActualEndDate());
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
        
        // 设置任务类型和层级
        if (taskDto.getTaskType() != null) {
            TestTask.TaskType taskType = TestTask.TaskType.valueOf(taskDto.getTaskType());
            task.setTaskType(taskType);
        } else {
            task.setTaskType(TestTask.TaskType.REQUIREMENT); // 默认为需求测试
        }
        
        if (taskDto.getTaskLevel() != null) {
            task.setTaskLevel(taskDto.getTaskLevel());
        } else {
            task.setTaskLevel(TestTask.TaskLevel.MAIN); // 默认为主任务
        }
        
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
        
        // 设置实际工时
        if (taskDto.getActualManDays() != null && taskDto.getActualManDays() >= 0) {
            task.setActualManDays(taskDto.getActualManDays());
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
        
        // 更新任务类型和层级（如果提供了）
        if (taskDto.getTaskType() != null) {
            TestTask.TaskType taskType = TestTask.TaskType.valueOf(taskDto.getTaskType());
            task.setTaskType(taskType);
        }
        
        if (taskDto.getTaskLevel() != null) {
            task.setTaskLevel(taskDto.getTaskLevel());
        }
        
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
        
        // 更新实际工时
        if (taskDto.getActualManDays() != null && taskDto.getActualManDays() >= 0) {
            task.setActualManDays(taskDto.getActualManDays());
        }
        
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
                                                TestTask.TaskType taskType,
                                                LocalDate startDateFrom, LocalDate startDateTo,
                                                Boolean isOverdue, Boolean isExpectedCompletionReached, 
                                                String search, Pageable pageable) {
        Page<TestTask> tasks = testTaskRepository.findByFiltersWithTaskType(assignedTo, assignedToName, department, 
                                                               status, priority, projectName, testType, taskType,
                                                               startDateFrom, startDateTo, isOverdue, 
                                                               isExpectedCompletionReached, search, pageable);
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
     * 获取本月或上月的个人任务统计（按状态分类）
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserTaskStatisticsByMonth(String month) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart, monthEnd;
        
        if ("last".equals(month)) {
            // 上月
            monthStart = now.minusMonths(1).withDayOfMonth(1);
            monthEnd = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
        } else {
            // 本月
            monthStart = now.withDayOfMonth(1);
            monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        }
        
        List<Object[]> results = testTaskRepository.countByUserAndStatusThisMonth(monthStart, monthEnd);
        Map<String, Map<String, Object>> userStatsMap = new HashMap<>();
        
        for (Object[] result : results) {
            String userName = (String) result[0];
            TestTask.TaskStatus status = (TestTask.TaskStatus) result[1];
            Long count = (Long) result[2];
            
            if (!userStatsMap.containsKey(userName)) {
                Map<String, Object> userStat = new HashMap<>();
                userStat.put("name", userName);
                userStat.put("completed", 0L);
                userStat.put("inProgress", 0L);
                userStat.put("onHold", 0L);
                userStat.put("planned", 0L);
                userStatsMap.put(userName, userStat);
            }
            
            Map<String, Object> userStat = userStatsMap.get(userName);
            switch (status) {
                case COMPLETED:
                    userStat.put("completed", count);
                    break;
                case IN_PROGRESS:
                    userStat.put("inProgress", count);
                    break;
                case ON_HOLD:
                    userStat.put("onHold", count);
                    break;
                case PLANNED:
                    userStat.put("planned", count);
                    break;
            }
        }
        
        return new ArrayList<>(userStatsMap.values());
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
        log.info("权限检查 - 任务ID: {}, 当前用户: {} (ID: {}), 角色: {}", 
                task.getId(), currentUser.getUsername(), currentUser.getId(), currentUser.getRole());
        
        // ADMIN可以修改所有任务
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            log.info("用户是管理员，允许修改");
            return true;
        }
        
        // MANAGER可以修改所有任务
        if (currentUser.getRole() == User.UserRole.MANAGER) {
            log.info("用户是经理，允许修改");
            return true;
        }
        
        // TESTER只能修改分配给自己的任务或自己创建的任务
        if (currentUser.getRole() == User.UserRole.TESTER) {
            boolean isAssignee = false;
            boolean isCreator = false;
            
            if (task.getAssignedTo() != null) {
                isAssignee = task.getAssignedTo().getId().equals(currentUser.getId());
                log.info("任务负责人: {} (ID: {}), 当前用户ID: {}, 是否为负责人: {}", 
                        task.getAssignedTo().getUsername(), task.getAssignedTo().getId(), 
                        currentUser.getId(), isAssignee);
            } else {
                log.info("任务没有分配负责人");
            }
            
            if (task.getCreatedByUser() != null) {
                isCreator = task.getCreatedByUser().getId().equals(currentUser.getId());
                log.info("任务创建者: {} (ID: {}), 当前用户ID: {}, 是否为创建者: {}", 
                        task.getCreatedByUser().getUsername(), task.getCreatedByUser().getId(), 
                        currentUser.getId(), isCreator);
            } else {
                log.info("任务没有创建者信息");
            }
            
            boolean canModify = isAssignee || isCreator;
            log.info("TESTER权限检查结果: {}", canModify);
            return canModify;
        }
        
        log.info("用户角色不匹配，拒绝修改");
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
        
        // 使用Asia/Shanghai时区的当前时间
        LocalDateTime executeTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        
        try {
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
            
            // 更新定时任务执行记录
            updateScheduledTaskExecution("checkOverdueTasks", executeTime, "SUCCESS", null);
            
        } catch (Exception e) {
            log.error("超时任务检查失败: {}", e.getMessage(), e);
            updateScheduledTaskExecution("checkOverdueTasks", executeTime, "FAILED", e.getMessage());
            throw e;
        }
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

    /**
     * 更新定时任务执行记录
     */
    private void updateScheduledTaskExecution(String taskName, LocalDateTime executeTime, String result, String errorMessage) {
        try {
            ScheduledTask task = scheduledTaskRepository.findByTaskName(taskName)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskName));
            
            task.setLastExecuteTime(executeTime);
            task.setLastExecuteResult(result);
            
            // 自动执行时更新下次执行时间
            LocalDateTime nextExecuteTime = calculateNextExecuteTime(task.getCronExpression());
            task.setNextExecuteTime(nextExecuteTime);
            
            scheduledTaskRepository.save(task);
            
            log.info("定时任务 {} 执行记录已更新: 时间={}, 结果={}, 下次执行时间={}", taskName, executeTime, result, nextExecuteTime);
        } catch (Exception e) {
            log.error("更新定时任务执行记录失败: taskName={}, error={}", taskName, e.getMessage());
        }
    }

    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextExecuteTime(String cronExpression) {
        try {
            CronTrigger trigger = new CronTrigger(cronExpression);
            Date now = new Date();
            Date nextExecution = trigger.nextExecutionTime(new SimpleTriggerContext(now, now, now));
            if (nextExecution != null) {
                return LocalDateTime.ofInstant(nextExecution.toInstant(), ZoneId.of("Asia/Shanghai"));
            }
        } catch (Exception e) {
            log.error("解析cron表达式失败: {}, error: {}", cronExpression, e.getMessage());
        }
        return null;
    }

    // ========================================
    // 子任务管理功能
    // ========================================

    /**
     * 创建子任务
     */
    public TestTaskDto createSubTask(Long parentTaskId, TestTaskDto subTaskDto, String currentUsername) {
        log.info("为主任务 {} 创建子任务: {}", parentTaskId, subTaskDto.getTaskName());
        
        // 获取当前用户
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        // 获取父任务
        TestTask parentTask = testTaskRepository.findById(parentTaskId)
                .orElseThrow(() -> new RuntimeException("主任务不存在: " + parentTaskId));
        
        // 验证权限
        if (!canModifyTask(parentTask, currentUser)) {
            throw new RuntimeException("没有权限为此主任务创建子任务");
        }
        
        // 验证父任务是主任务
        if (parentTask.getTaskLevel() != TestTask.TaskLevel.MAIN) {
            throw new RuntimeException("只能为主任务创建子任务");
        }
        
        // 创建子任务
        TestTask subTask = new TestTask();
        subTask.setTaskName(subTaskDto.getTaskName());
        subTask.setTaskDescription(subTaskDto.getTaskDescription());
        subTask.setStartDate(subTaskDto.getStartDate());
        subTask.setExpectedEndDate(subTaskDto.getExpectedEndDate());
        subTask.setParticipantCount(subTaskDto.getParticipantCount());
        subTask.setPriority(subTaskDto.getPriority());
        subTask.setProjectName(parentTask.getProjectName()); // 继承主任务的项目名
        subTask.setModuleName(subTaskDto.getModuleName());
        subTask.setTestType(subTaskDto.getTestType());
        subTask.setDepartment(parentTask.getDepartment()); // 继承主任务的部门
        subTask.setCreatedByUser(currentUser);
        subTask.setSubtaskWeight(subTaskDto.getSubtaskWeight());
        
        // 设置子任务属性
        subTask.setTaskLevel(TestTask.TaskLevel.SUB);
        subTask.setParentTask(parentTask);
        subTask.setAutoProgressCalculation(false);
        
        // 子任务继承父任务的类型
        subTask.setTaskType(parentTask.getTaskType());
        
        // 设置负责人
        if (subTaskDto.getAssignedToName() != null && !subTaskDto.getAssignedToName().isEmpty()) {
            User assignedTo = userRepository.findByRealName(subTaskDto.getAssignedToName())
                    .orElseThrow(() -> new RuntimeException("指定的负责人不存在: " + subTaskDto.getAssignedToName()));
            subTask.setAssignedTo(assignedTo);
        }
        
        // 计算工时
        subTask.calculateManDays();
        
        // 添加到主任务中（会自动设置排序和启用自动进度计算）
        parentTask.addSubTask(subTask);
        
        // 保存
        TestTask savedSubTask = testTaskRepository.save(subTask);
        testTaskRepository.save(parentTask); // 保存主任务的变更
        
        // 重新计算主任务进度
        calculateMainTaskProgress(parentTaskId);
        
        log.info("子任务创建成功: {} (ID: {})", savedSubTask.getTaskName(), savedSubTask.getId());
        
        // 发送通知
        if (savedSubTask.getAssignedTo() != null) {
            notificationService.sendTaskAssignedNotification(savedSubTask, currentUser.getRealName());
        }
        
        return TestTaskDto.fromEntity(savedSubTask);
    }

    /**
     * 获取任务树（主任务及其子任务）
     */
    @Transactional(readOnly = true)
    public Page<TestTaskDto> getTaskTree(Map<String, Object> params, Pageable pageable) {
        return getTaskTreeWithFilters(params, pageable);
    }

    /**
     * 获取任务树（支持过滤条件）
     */
    @Transactional(readOnly = true)
    public Page<TestTaskDto> getTaskTreeWithFilters(Map<String, Object> filters, Pageable pageable) {
        log.info("获取任务树，过滤条件: {}", filters);
        
        // 解析过滤条件
        String assignedToName = (String) filters.get("assignedToName");
        String department = (String) filters.get("department");
        String statusStr = (String) filters.get("status");
        String priorityStr = (String) filters.get("priority");
        String taskTypeStr = (String) filters.get("taskType");
        Boolean isOverdue = (Boolean) filters.get("isOverdue");
        String search = (String) filters.get("search");
        
        // 转换枚举
        TestTask.TaskStatus status = statusStr != null ? TestTask.TaskStatus.valueOf(statusStr) : null;
        TestTask.TaskPriority priority = priorityStr != null ? TestTask.TaskPriority.valueOf(priorityStr) : null;
        TestTask.TaskType taskType = taskTypeStr != null ? TestTask.TaskType.valueOf(taskTypeStr) : null;
        
        // 只查询主任务，并预加载子任务
        Page<TestTask> mainTasks = testTaskRepository.findMainTasksWithFilters(
            assignedToName, department, status, priority, taskType, isOverdue, search, pageable
        );
        
        return mainTasks.map(TestTaskDto::fromEntityWithSubTasks);
    }

    /**
     * 根据用户权限获取可见的任务树
     */
    @Transactional(readOnly = true)
    public Page<TestTaskDto> getTaskTreeForUser(String username, Pageable pageable) {
        return getTaskTreeForUserWithFilters(username, new HashMap<>(), pageable);
    }

    /**
     * 根据用户权限和过滤条件获取可见的任务树
     */
    @Transactional(readOnly = true)
    public Page<TestTaskDto> getTaskTreeForUserWithFilters(String username, Map<String, Object> filters, Pageable pageable) {
        log.info("获取用户 {} 的可见任务树，过滤条件: {}", username, filters);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 管理员和经理可以看到所有任务
        if (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.MANAGER) {
            return getTaskTreeWithFilters(filters, pageable);
        }
        
        // 解析过滤条件（和管理员相同的逻辑）
        String assignedToName = (String) filters.get("assignedToName");
        String department = (String) filters.get("department");
        String statusStr = (String) filters.get("status");
        String priorityStr = (String) filters.get("priority");
        String taskTypeStr = (String) filters.get("taskType");
        Boolean isOverdue = (Boolean) filters.get("isOverdue");
        String search = (String) filters.get("search");
        
        // 转换枚举
        TestTask.TaskStatus status = statusStr != null ? TestTask.TaskStatus.valueOf(statusStr) : null;
        TestTask.TaskPriority priority = priorityStr != null ? TestTask.TaskPriority.valueOf(priorityStr) : null;
        TestTask.TaskType taskType = taskTypeStr != null ? TestTask.TaskType.valueOf(taskTypeStr) : null;
        
        // 普通用户只能看到与自己相关的任务
        Page<TestTask> visibleTasks = testTaskRepository.findMainTasksVisibleToUserWithFilters(
            user, assignedToName, department, status, priority, taskType, isOverdue, search, pageable
        );
        return visibleTasks.map(TestTaskDto::fromEntityWithSubTasks);
    }

    /**
     * 统一的任务查询方法 - 返回所有任务数据（主任务+子任务），前端智能渲染
     */
    @Transactional(readOnly = true)
    public Page<TestTaskDto> getAllTasksUnified(String username, Map<String, Object> filters, Pageable pageable) {
        log.info("获取用户 {} 的所有可见任务，过滤条件: {}", username, filters);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 解析过滤条件
        String assignedToName = (String) filters.get("assignedToName");
        String department = (String) filters.get("department");
        String statusStr = (String) filters.get("status");
        String priorityStr = (String) filters.get("priority");
        String taskTypeStr = (String) filters.get("taskType");
        Boolean isOverdue = (Boolean) filters.get("isOverdue");
        String search = (String) filters.get("search");
        
        // 转换枚举
        TestTask.TaskStatus status = statusStr != null ? TestTask.TaskStatus.valueOf(statusStr) : null;
        TestTask.TaskPriority priority = priorityStr != null ? TestTask.TaskPriority.valueOf(priorityStr) : null;
        TestTask.TaskType taskType = taskTypeStr != null ? TestTask.TaskType.valueOf(taskTypeStr) : null;
        
        Page<TestTask> tasks;
        
        // 管理员和经理可以看到所有任务
        if (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.MANAGER) {
            tasks = testTaskRepository.findAllTasksWithFilters(
                assignedToName, department, status, priority, taskType, isOverdue, search, pageable
            );
        } else {
            // 普通用户只能看到与自己相关的任务（包括主任务和子任务）
            tasks = testTaskRepository.findAllTasksVisibleToUserWithFilters(
                user, assignedToName, department, status, priority, taskType, isOverdue, search, pageable
            );
        }
        
        // 转换为DTO，确保包含完整的层级和类型信息
        return tasks.map(task -> {
            TestTaskDto dto = TestTaskDto.fromEntity(task);
            
            // 如果是主任务且有子任务，加载子任务信息
            if (task.getTaskLevel() == TestTask.TaskLevel.MAIN && !task.getSubTasks().isEmpty()) {
                List<TestTaskDto> subTaskDtos = task.getSubTasks().stream()
                    .map(TestTaskDto::fromEntity)
                    .collect(Collectors.toList());
                dto.setSubTasks(subTaskDtos);
                dto.setHasSubTasks(true);
            }
            
            return dto;
        });
    }

    /**
     * 获取指定主任务的子任务列表
     */
    @Transactional(readOnly = true)
    public List<TestTaskDto> getSubTasks(Long parentTaskId, String currentUsername) {
        log.info("获取主任务 {} 的子任务列表", parentTaskId);
        
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        TestTask parentTask = testTaskRepository.findById(parentTaskId)
                .orElseThrow(() -> new RuntimeException("主任务不存在"));
        
        // 检查权限
        if (!parentTask.hasUserAccess(currentUser) && 
            currentUser.getRole() != User.UserRole.ADMIN && 
            currentUser.getRole() != User.UserRole.MANAGER) {
            throw new RuntimeException("没有权限查看此主任务的子任务");
        }
        
        List<TestTask> subTasks = testTaskRepository.findSubTasksByParentId(parentTaskId);
        return subTasks.stream()
                .map(TestTaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 更新子任务进度并自动计算主任务进度
     */
    public TestTaskDto updateSubTaskProgress(Long subTaskId, Integer progressPercentage, String currentUsername) {
        log.info("更新子任务 {} 进度为: {}%", subTaskId, progressPercentage);
        
        TestTask subTask = testTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new RuntimeException("子任务不存在"));
        
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        // 检查权限
        if (!canUpdateProgress(subTask, currentUser)) {
            throw new RuntimeException("没有权限更新此子任务进度");
        }
        
        // 更新子任务进度
        subTask.updateProgress(progressPercentage);
        TestTask savedSubTask = testTaskRepository.save(subTask);
        
        // 自动计算主任务进度
        if (subTask.getParentTask() != null) {
            calculateMainTaskProgress(subTask.getParentTask().getId());
        }
        
        // 发送进度更新通知
        notificationService.sendProgressUpdateNotification(savedSubTask, progressPercentage, currentUser.getRealName());
        
        return TestTaskDto.fromEntity(savedSubTask);
    }

    /**
     * 主任务创建者手动更新主任务进度（覆盖自动计算的进度）
     */
    public TestTaskDto updateMainTaskProgressManually(Long mainTaskId, Integer progressPercentage, String currentUsername) {
        log.info("主任务创建者手动更新任务 {} 进度为: {}%", mainTaskId, progressPercentage);
        
        TestTask mainTask = testTaskRepository.findById(mainTaskId)
                .orElseThrow(() -> new RuntimeException("主任务不存在"));
        
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        // 检查是否为主任务创建者
        if (!mainTask.isMainTask() || !mainTask.getCreatedByUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("只有主任务创建者才能手动更新主任务进度");
        }
        
        // 手动更新进度
        mainTask.updateMainTaskProgressManually(progressPercentage, currentUser);
        TestTask savedTask = testTaskRepository.save(mainTask);
        
        // 发送进度更新通知
        notificationService.sendProgressUpdateNotification(savedTask, progressPercentage, currentUser.getRealName());
        
        // 如果任务完成，发送完成通知
        if (savedTask.getStatus() == TestTask.TaskStatus.COMPLETED) {
            notificationService.sendTaskCompletedNotification(savedTask, currentUser.getRealName());
        }
        
        log.info("主任务 {} 进度已手动更新为: {}%", mainTaskId, progressPercentage);
        return TestTaskDto.fromEntity(savedTask);
    }

    /**
     * 计算主任务进度
     */
    public void calculateMainTaskProgress(Long mainTaskId) {
        log.info("计算主任务 {} 的进度", mainTaskId);
        
        TestTask mainTask = testTaskRepository.findById(mainTaskId)
                .orElseThrow(() -> new RuntimeException("主任务不存在"));
        
        if (mainTask.getTaskLevel() != TestTask.TaskLevel.MAIN) {
            log.warn("任务 {} 不是主任务，跳过进度计算", mainTaskId);
            return;
        }
        
        // 执行进度计算
        mainTask.calculateMainTaskProgress();
        testTaskRepository.save(mainTask);
        
        log.info("主任务 {} 进度已更新为: {}%", mainTaskId, mainTask.getProgressPercentage());
    }

    /**
     * 将普通任务转换为主任务
     */
    public TestTaskDto convertToMainTask(Long taskId, String currentUsername) {
        log.info("将任务 {} 转换为主任务", taskId);
        
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        TestTask task = testTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 检查权限
        if (!canModifyTask(task, currentUser)) {
            throw new RuntimeException("没有权限修改此任务");
        }
        
        // 如果已经是主任务，直接返回
        if (task.getTaskLevel() == TestTask.TaskLevel.MAIN) {
            return TestTaskDto.fromEntity(task);
        }
        
        // 如果是子任务，需要先从主任务中移除
        if (task.getTaskLevel() == TestTask.TaskLevel.SUB && task.getParentTask() != null) {
            TestTask parentTask = task.getParentTask();
            parentTask.removeSubTask(task);
            testTaskRepository.save(parentTask);
            
            // 重新计算原主任务进度
            calculateMainTaskProgress(parentTask.getId());
        }
        
        // 转换为主任务
        task.setTaskLevel(TestTask.TaskLevel.MAIN);
        task.setParentTask(null);
        task.setSubTaskOrder(0);
        task.setAutoProgressCalculation(false);
        
        TestTask savedTask = testTaskRepository.save(task);
        
        log.info("任务 {} 已转换为主任务", taskId);
        
        return TestTaskDto.fromEntity(savedTask);
    }

    /**
     * 删除子任务
     */
    public void deleteSubTask(Long subTaskId, String currentUsername) {
        log.info("删除子任务: {}", subTaskId);
        
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
        
        TestTask subTask = testTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new RuntimeException("子任务不存在"));
        
        // 检查权限
        if (!canDeleteTask(subTask, currentUser)) {
            throw new RuntimeException("没有权限删除此子任务");
        }
        
        // 获取主任务ID用于重新计算进度
        Long parentTaskId = null;
        if (subTask.getParentTask() != null) {
            parentTaskId = subTask.getParentTask().getId();
        }
        
        // 删除子任务
        testTaskRepository.deleteById(subTaskId);
        
        // 重新计算主任务进度
        if (parentTaskId != null) {
            calculateMainTaskProgress(parentTaskId);
        }
        
        log.info("子任务 {} 删除成功", subTaskId);
    }

    /**
     * 获取用户的子任务统计
     * 特别强调：子任务的责任人可以看到自己的子任务，并归到该责任人下统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserSubTaskStatistics(String username) {
        log.info("获取用户 {} 的子任务统计", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取用户的子任务
        List<TestTask> userSubTasks = testTaskRepository.findTasksAccessibleByUser(user, TestTask.TaskLevel.SUB);
        
        // 按状态统计
        Map<TestTask.TaskStatus, Long> statusCount = userSubTasks.stream()
                .collect(Collectors.groupingBy(TestTask::getStatus, Collectors.counting()));
        
        // 总工时统计
        double totalManDays = userSubTasks.stream()
                .mapToDouble(task -> task.getManDays() != null ? task.getManDays() : 0.0)
                .sum();
        
        double actualManDays = userSubTasks.stream()
                .mapToDouble(task -> task.getActualManDays() != null ? task.getActualManDays() : 0.0)
                .sum();
        
        // 进度统计
        double avgProgress = userSubTasks.stream()
                .mapToInt(task -> task.getProgressPercentage() != null ? task.getProgressPercentage() : 0)
                .average()
                .orElse(0.0);
        
        statistics.put("totalSubTasks", userSubTasks.size());
        statistics.put("statusCount", statusCount);
        statistics.put("totalManDays", totalManDays);
        statistics.put("actualManDays", actualManDays);
        statistics.put("averageProgress", Math.round(avgProgress * 100.0) / 100.0);
        statistics.put("subTasks", userSubTasks.stream()
                .map(TestTaskDto::fromEntity)
                .collect(Collectors.toList()));
        
        return statistics;
    }

    /**
     * 重新计算所有启用自动进度计算的主任务
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void recalculateAllMainTaskProgress() {
        log.debug("开始重新计算所有主任务进度");
        
        try {
            List<TestTask> mainTasks = testTaskRepository.findMainTasksWithAutoProgress();
            
            for (TestTask mainTask : mainTasks) {
                try {
                    mainTask.calculateMainTaskProgress();
                    testTaskRepository.save(mainTask);
                } catch (Exception e) {
                    log.error("计算主任务 {} 进度失败: {}", mainTask.getId(), e.getMessage());
                }
            }
            
            log.debug("主任务进度重新计算完成，处理了 {} 个主任务", mainTasks.size());
        } catch (Exception e) {
            log.error("重新计算主任务进度时发生错误", e);
        }
    }
} 