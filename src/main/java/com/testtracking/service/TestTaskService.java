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
        
        // 设置层级字段
        task.setParentId(taskDto.getParentId());
        task.setTaskType(taskDto.getTaskType() != null ? taskDto.getTaskType() : TestTask.TaskType.NORMAL);
        task.setVersionCode(taskDto.getVersionCode());
        
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
        
        // 如果是需求任务，更新父任务（版本）的进度
        if (savedTask.getParentId() != null) {
            updateVersionProgress(savedTask.getParentId());
        }
        
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
        
        // 如果是需求任务，更新父任务（版本）的进度
        if (savedTask.getParentId() != null) {
            updateVersionProgress(savedTask.getParentId());
        }
        
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
        
        // 如果是需求任务，更新父任务（版本）的进度
        if (savedTask.getParentId() != null) {
            updateVersionProgress(savedTask.getParentId());
        }
        
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
                                                List<TestTask.TaskStatus> statuses, 
                                                TestTask.TaskPriority priority, String projectName, 
                                                TestTask.TestType testType, TestTask.TaskType taskType,
                                                LocalDate startDateFrom, LocalDate startDateTo,
                                                Boolean isOverdue, Boolean isExpectedCompletionReached, 
                                                String search, Pageable pageable) {
        Page<TestTask> tasks = testTaskRepository.findByFilters(assignedTo, assignedToName, department, 
                                                               statuses, priority, projectName, testType, taskType,
                                                               startDateFrom, startDateTo, isOverdue, 
                                                               isExpectedCompletionReached, search, pageable);
        
        // 构建树形结构：为版本任务加载子任务
        return tasks.map(task -> {
            TestTaskDto dto = TestTaskDto.fromEntity(task);
            if (task.getTaskType() == TestTask.TaskType.VERSION) {
                List<TestTask> children = testTaskRepository.findByParentIdOrderByIdAsc(task.getId());
                List<TestTaskDto> childDtos = children.stream()
                        .map(TestTaskDto::fromEntity)
                        .collect(Collectors.toList());
                dto.setChildren(childDtos);
                dto.setHasChildren(!children.isEmpty());
                dto.setChildCount(children.size());
                dto.setCompletedChildCount((int) children.stream()
                        .filter(c -> c.getStatus() == TestTask.TaskStatus.COMPLETED)
                        .count());
            }
            return dto;
        });
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
        
        // 保存父任务ID（删除前）
        Long parentId = task.getParentId();
        
        // 如果是版本任务，先删除所有子需求
        if (task.getTaskType() == TestTask.TaskType.VERSION) {
            List<TestTask> children = testTaskRepository.findByParentIdOrderByIdAsc(taskId);
            if (!children.isEmpty()) {
                log.info("删除版本任务 {} 的 {} 个子需求", taskId, children.size());
                testTaskRepository.deleteAll(children);
            }
        }
        
        testTaskRepository.delete(task);
        
        // 如果是需求任务，删除后更新父任务（版本）的进度和投入人数
        if (parentId != null) {
            updateVersionProgress(parentId);
        }
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
                case CANCELLED:
                    userStat.put("cancelled", count);
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
     * 定时检查超时任务（已由动态调度器管理，此注解已禁用）
     */
    // @Scheduled(cron = "0 0 1 * * ?") // 已禁用：由DynamicScheduledTaskService动态管理
    // @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "scheduler.enabled", havingValue = "false", matchIfMissing = false)
    public void checkOverdueTasks() {
        log.info("开始检查超时任务...");
        
        // 使用Asia/Shanghai时区的当前时间
        LocalDateTime executeTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        
        try {
            List<TestTask> allTasks = testTaskRepository.findAll();
            int updatedCount = 0;
            
            // 收集所有超时且未完成的任务（用于每日合并通知）
            List<TestTask> allOverdueTasks = new ArrayList<>();
            
            for (TestTask task : allTasks) {
                boolean wasOverdue = task.getIsOverdue();
                task.checkOverdue();
                
                // 如果超时状态发生变化，保存更新
                if (wasOverdue != task.getIsOverdue()) {
                    testTaskRepository.save(task);
                    updatedCount++;
                }
                
                // 收集所有超时且状态为"计划中"或"进行中"的任务（超时7天及以上才通知）
                // 排除：已完成、暂停、取消的任务
                // 由于任务每周五更新，超时7天（超过一个完整更新周期）才需要通知
                if (task.getIsOverdue() != null && task.getIsOverdue() &&
                    task.getOverdueDays() != null && task.getOverdueDays() >= 7 &&
                    (task.getStatus() == TestTask.TaskStatus.PLANNED || 
                     task.getStatus() == TestTask.TaskStatus.IN_PROGRESS)) {
                    allOverdueTasks.add(task);
                }
            }
            
            // 发送合并通知（只通知超时7天及以上的未完成任务）
            // 由于任务每周五更新，超时7天（超过一个完整更新周期）才需要通知
            if (!allOverdueTasks.isEmpty()) {
                try {
                    notificationService.sendBatchOverdueNotification(allOverdueTasks);
                    log.info("已发送合并超时通知，包含 {} 个超时7天及以上的未完成任务", allOverdueTasks.size());
                } catch (Exception e) {
                    log.error("发送合并超时通知失败: {}", e.getMessage());
                }
            } else {
                log.info("没有超时7天及以上的未完成任务需要通知");
            }
            
            log.info("超时任务检查完成，更新了 {} 个任务，当前超时7天及以上未完成 {} 个", updatedCount, allOverdueTasks.size());
            
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

    // ========== 版本任务层级管理 ==========

    /**
     * 更新版本任务的进度（根据子任务自动计算）
     */
    public void updateVersionProgress(Long versionTaskId) {
        log.info("更新版本任务进度: versionTaskId={}", versionTaskId);
        
        TestTask versionTask = testTaskRepository.findById(versionTaskId)
                .orElse(null);
        
        if (versionTask == null) {
            log.warn("版本任务不存在: {}", versionTaskId);
            return;
        }
        
        // 获取所有子任务
        List<TestTask> childTasks = testTaskRepository.findByParentIdOrderByIdAsc(versionTaskId);
        
        if (childTasks.isEmpty()) {
            versionTask.setProgressPercentage(0);
            testTaskRepository.save(versionTask);
            return;
        }
        
        // 按工时加权计算进度
        double totalWeight = 0;
        double completedWeight = 0;
        double totalManDays = 0;
        double actualManDays = 0;
        int completedCount = 0;
        java.util.Set<Long> assigneeIds = new java.util.HashSet<>();  // 责任人ID集合（去重）
        
        for (TestTask child : childTasks) {
            double manDays = child.getManDays() != null ? child.getManDays() : 1;
            int progress = child.getProgressPercentage() != null ? child.getProgressPercentage() : 0;
            
            totalWeight += manDays;
            completedWeight += manDays * progress / 100.0;
            totalManDays += manDays;
            
            // 只有已完成的任务才计算实际工时
            if (child.getStatus() == TestTask.TaskStatus.COMPLETED) {
                completedCount++;
                actualManDays += child.getActualManDays() != null ? child.getActualManDays() : 0;
            }
            
            // 按责任人去重计算投入人数
            if (child.getAssignedTo() != null) {
                assigneeIds.add(child.getAssignedTo().getId());
            }
        }
        
        int totalParticipants = assigneeIds.size();  // 投入人数 = 不同责任人的数量
        
        // 更新版本任务进度
        int versionProgress = totalWeight > 0 ? (int) Math.round(completedWeight / totalWeight * 100) : 0;
        versionTask.setProgressPercentage(versionProgress);
        versionTask.setManDays(totalManDays);
        // 只有当所有子任务都完成时才显示实际工时
        boolean allCompleted = completedCount == childTasks.size() && childTasks.size() > 0;
        versionTask.setActualManDays(allCompleted ? actualManDays : null);
        // 更新投入人数（所有子任务的总和）
        versionTask.setParticipantCount(totalParticipants > 0 ? totalParticipants : 1);
        
        // 自动更新版本状态
        if (completedCount == childTasks.size() && childTasks.size() > 0) {
            versionTask.setStatus(TestTask.TaskStatus.COMPLETED);
            if (versionTask.getActualEndDate() == null) {
                versionTask.setActualEndDate(LocalDate.now());
            }
        } else if (completedWeight > 0) {
            versionTask.setStatus(TestTask.TaskStatus.IN_PROGRESS);
        }
        
        // 检查超时
        versionTask.checkOverdue();
        
        testTaskRepository.save(versionTask);
        log.info("版本任务进度已更新: versionTaskId={}, progress={}%, completed={}/{}", 
                versionTaskId, versionProgress, completedCount, childTasks.size());
    }

    /**
     * 获取任务树形结构（包含子任务）
     */
    @Transactional(readOnly = true)
    public List<TestTaskDto> getTasksWithChildren(List<TestTask> tasks) {
        List<TestTaskDto> result = new ArrayList<>();
        
        for (TestTask task : tasks) {
            TestTaskDto dto = TestTaskDto.fromEntity(task);
            
            // 如果是版本任务，加载子任务
            if (task.getTaskType() == TestTask.TaskType.VERSION) {
                List<TestTask> children = testTaskRepository.findByParentIdOrderByIdAsc(task.getId());
                List<TestTaskDto> childDtos = children.stream()
                        .map(TestTaskDto::fromEntity)
                        .collect(Collectors.toList());
                dto.setChildren(childDtos);
                dto.setHasChildren(!children.isEmpty());
                dto.setChildCount(children.size());
                dto.setCompletedChildCount((int) children.stream()
                        .filter(c -> c.getStatus() == TestTask.TaskStatus.COMPLETED)
                        .count());
            }
            
            result.add(dto);
        }
        
        return result;
    }

    /**
     * 获取版本任务的子任务列表
     */
    @Transactional(readOnly = true)
    public List<TestTaskDto> getChildTasks(Long parentId) {
        List<TestTask> children = testTaskRepository.findByParentIdOrderByIdAsc(parentId);
        return children.stream()
                .map(TestTaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 添加需求任务到版本
     */
    public TestTaskDto addRequirementToVersion(Long versionId, TestTaskDto requirementDto, String currentUsername) {
        log.info("添加需求任务到版本: versionId={}, requirementName={}", versionId, requirementDto.getTaskName());
        
        // 验证版本任务存在
        TestTask versionTask = testTaskRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("版本任务不存在: " + versionId));
        
        if (versionTask.getTaskType() != TestTask.TaskType.VERSION) {
            throw new RuntimeException("只能向版本任务添加需求");
        }
        
        // 设置需求任务属性
        requirementDto.setParentId(versionId);
        requirementDto.setTaskType(TestTask.TaskType.REQUIREMENT);
        requirementDto.setProjectName(versionTask.getProjectName());  // 继承项目名
        requirementDto.setDepartment(versionTask.getDepartment());    // 继承部门
        
        // 创建任务
        return createTask(requirementDto, currentUsername);
    }
} 