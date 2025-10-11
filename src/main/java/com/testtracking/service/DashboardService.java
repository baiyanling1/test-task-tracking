package com.testtracking.service;

import com.testtracking.dto.TestTaskDto;
import com.testtracking.dto.UserWorkStatsDto;
import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import com.testtracking.repository.TestTaskRepository;
import com.testtracking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final TestTaskRepository testTaskRepository;
    private final UserRepository userRepository;

    /**
     * 获取本周统计信息
     */
    public Map<String, Object> getWeeklyStatistics() {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = now.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        Map<String, Object> statistics = new HashMap<>();
        
        // 本周开始的任务数
        long tasksStartedThisWeek = testTaskRepository.countTasksThisWeek(weekStart, weekEnd);
        statistics.put("tasksStartedThisWeek", tasksStartedThisWeek);
        
        // 本周结束的任务数
        long tasksEndingThisWeek = testTaskRepository.countTasksEndingThisWeek(weekStart, weekEnd);
        statistics.put("tasksEndingThisWeek", tasksEndingThisWeek);
        
        // 本周完成的任务数
        long tasksCompletedThisWeek = testTaskRepository.countCompletedTasksThisMonth(weekStart, weekEnd);
        statistics.put("tasksCompletedThisWeek", tasksCompletedThisWeek);
        
        // 本周人天统计
        Double manDaysThisWeek = testTaskRepository.sumManDaysByDateRange(weekStart, weekEnd);
        statistics.put("manDaysThisWeek", manDaysThisWeek != null ? manDaysThisWeek : 0.0);
        
        return statistics;
    }

    /**
     * 获取本月统计信息
     */
    public Map<String, Object> getMonthlyStatistics() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.with(TemporalAdjusters.lastDayOfMonth());

        Map<String, Object> statistics = new HashMap<>();
        
        // 本月开始的任务数
        long tasksStartedThisMonth = testTaskRepository.countTasksThisMonth(monthStart, monthEnd);
        statistics.put("tasksStartedThisMonth", tasksStartedThisMonth);
        
        // 本月完成的任务数
        long tasksCompletedThisMonth = testTaskRepository.countCompletedTasksThisMonth(monthStart, monthEnd);
        statistics.put("tasksCompletedThisMonth", tasksCompletedThisMonth);
        
        // 本月人天统计
        Double manDaysThisMonth = testTaskRepository.sumManDaysByDateRange(monthStart, monthEnd);
        statistics.put("manDaysThisMonth", manDaysThisMonth != null ? manDaysThisMonth : 0.0);
        
        return statistics;
    }

    /**
     * 获取本年统计信息
     */
    public Map<String, Object> getYearlyStatistics() {
        LocalDate now = LocalDate.now();
        LocalDate yearStart = now.withDayOfYear(1);
        LocalDate yearEnd = now.with(TemporalAdjusters.lastDayOfYear());

        Map<String, Object> statistics = new HashMap<>();
        
        // 本年开始的任务数
        long tasksStartedThisYear = testTaskRepository.countTasksThisMonth(yearStart, yearEnd);
        statistics.put("tasksStartedThisYear", tasksStartedThisYear);
        
        // 本年完成的任务数
        long tasksCompletedThisYear = testTaskRepository.countCompletedTasksThisMonth(yearStart, yearEnd);
        statistics.put("tasksCompletedThisYear", tasksCompletedThisYear);
        
        // 本年人天统计
        Double manDaysThisYear = testTaskRepository.sumManDaysByDateRange(yearStart, yearEnd);
        statistics.put("manDaysThisYear", manDaysThisYear != null ? manDaysThisYear : 0.0);
        
        return statistics;
    }

    /**
     * 获取任务状态统计
     */
    public Map<String, Object> getTaskStatusStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 各状态任务数量
        long plannedTasks = testTaskRepository.countByStatus(TestTask.TaskStatus.PLANNED);
        long inProgressTasks = testTaskRepository.countByStatus(TestTask.TaskStatus.IN_PROGRESS);
        long onHoldTasks = testTaskRepository.countByStatus(TestTask.TaskStatus.ON_HOLD);
        long completedTasks = testTaskRepository.countByStatus(TestTask.TaskStatus.COMPLETED);
        long cancelledTasks = testTaskRepository.countByStatus(TestTask.TaskStatus.CANCELLED);
        
        statistics.put("plannedTasks", plannedTasks);
        statistics.put("inProgressTasks", inProgressTasks);
        statistics.put("onHoldTasks", onHoldTasks);
        statistics.put("completedTasks", completedTasks);
        statistics.put("cancelledTasks", cancelledTasks);
        
        // 总任务数
        long totalTasks = plannedTasks + inProgressTasks + onHoldTasks + completedTasks + cancelledTasks;
        statistics.put("totalTasks", totalTasks);
        
        // 完成率
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        statistics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        
        return statistics;
    }

    /**
     * 获取风险等级统计
     */
    public Map<String, Object> getRiskLevelStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        long lowRiskTasks = testTaskRepository.countByRiskLevel(TestTask.RiskLevel.LOW);
        long mediumRiskTasks = testTaskRepository.countByRiskLevel(TestTask.RiskLevel.MEDIUM);
        long highRiskTasks = testTaskRepository.countByRiskLevel(TestTask.RiskLevel.HIGH);
        long criticalRiskTasks = testTaskRepository.countByRiskLevel(TestTask.RiskLevel.CRITICAL);
        
        statistics.put("lowRiskTasks", lowRiskTasks);
        statistics.put("mediumRiskTasks", mediumRiskTasks);
        statistics.put("highRiskTasks", highRiskTasks);
        statistics.put("criticalRiskTasks", criticalRiskTasks);
        
        return statistics;
    }

    /**
     * 获取超时任务统计
     */
    public Map<String, Object> getOverdueTaskStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        long overdueTasks = testTaskRepository.countOverdueTasks();
        statistics.put("overdueTasks", overdueTasks);
        
        // 获取超时任务列表并转换为DTO，避免懒加载问题
        List<TestTask> overdueTaskList = testTaskRepository.findByIsOverdueTrue();
        List<TestTaskDto> overdueTaskDtoList = overdueTaskList.stream()
                .map(TestTaskDto::fromEntity)
                .collect(Collectors.toList());
        statistics.put("overdueTaskList", overdueTaskDtoList);
        
        return statistics;
    }

    /**
     * 获取项目统计
     */
    public Map<String, Object> getProjectStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        List<Object[]> projectStats = testTaskRepository.countByProject();
        statistics.put("projectStatistics", projectStats);
        
        return statistics;
    }

    /**
     * 获取用户统计
     */
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 用户角色统计
        long adminCount = userRepository.countByRole(User.UserRole.ADMIN);
        long managerCount = userRepository.countByRole(User.UserRole.MANAGER);
        long testerCount = userRepository.countByRole(User.UserRole.TESTER);
        
        statistics.put("adminCount", adminCount);
        statistics.put("managerCount", managerCount);
        statistics.put("testerCount", testerCount);
        
        // 部门统计
        List<Object[]> departmentStats = userRepository.countByDepartment();
        statistics.put("departmentStatistics", departmentStats);
        
        return statistics;
    }

    /**
     * 获取人天统计信息
     */
    public Map<String, Object> getManDaysStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 各状态的人天统计
        Double plannedManDays = testTaskRepository.sumManDaysByStatus(TestTask.TaskStatus.PLANNED);
        Double inProgressManDays = testTaskRepository.sumManDaysByStatus(TestTask.TaskStatus.IN_PROGRESS);
        Double completedManDays = testTaskRepository.sumManDaysByStatus(TestTask.TaskStatus.COMPLETED);
        Double onHoldManDays = testTaskRepository.sumManDaysByStatus(TestTask.TaskStatus.ON_HOLD);
        
        statistics.put("plannedManDays", plannedManDays != null ? Math.round(plannedManDays * 10.0) / 10.0 : 0.0);
        statistics.put("inProgressManDays", inProgressManDays != null ? Math.round(inProgressManDays * 10.0) / 10.0 : 0.0);
        statistics.put("completedManDays", completedManDays != null ? Math.round(completedManDays * 10.0) / 10.0 : 0.0);
        statistics.put("onHoldManDays", onHoldManDays != null ? Math.round(onHoldManDays * 10.0) / 10.0 : 0.0);
        
        // 总人天 - 使用精确计算避免浮点数精度问题
        double totalManDays = (plannedManDays != null ? plannedManDays : 0.0) +
                             (inProgressManDays != null ? inProgressManDays : 0.0) +
                             (completedManDays != null ? completedManDays : 0.0) +
                             (onHoldManDays != null ? onHoldManDays : 0.0);
        // 保留1位小数，避免浮点数精度问题
        totalManDays = Math.round(totalManDays * 10.0) / 10.0;
        statistics.put("totalManDays", totalManDays);
        
        return statistics;
    }

    /**
     * 获取部门统计信息
     */
    public Map<String, Object> getDepartmentStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 各部门任务数量统计
        List<Object[]> departmentTaskCounts = testTaskRepository.countByDepartment();
        Map<String, Long> departmentTaskMap = new HashMap<>();
        for (Object[] result : departmentTaskCounts) {
            String department = (String) result[0];
            Long count = (Long) result[1];
            departmentTaskMap.put(department, count);
        }
        statistics.put("departmentTaskCounts", departmentTaskMap);
        
        // 各部门超时任务统计
        List<TestTask> overdueTasks = testTaskRepository.findByIsOverdueTrue();
        Map<String, Long> departmentOverdueMap = overdueTasks.stream()
                .filter(task -> task.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        TestTask::getDepartment,
                        Collectors.counting()
                ));
        statistics.put("departmentOverdueCounts", departmentOverdueMap);
        
        // 各部门人天统计
        Map<String, Double> departmentManDaysMap = overdueTasks.stream()
                .filter(task -> task.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        TestTask::getDepartment,
                        Collectors.summingDouble(task -> task.getManDays() != null ? task.getManDays() : 0.0)
                ));
        statistics.put("departmentManDays", departmentManDaysMap);
        
        return statistics;
    }

    /**
     * 获取综合Dashboard数据
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        dashboard.put("weeklyStats", getWeeklyStatistics());
        dashboard.put("monthlyStats", getMonthlyStatistics());
        dashboard.put("yearlyStats", getYearlyStatistics());
        dashboard.put("taskStatusStats", getTaskStatusStatistics());
        dashboard.put("riskLevelStats", getRiskLevelStatistics());
        dashboard.put("overdueTaskStats", getOverdueTaskStatistics());
        dashboard.put("projectStats", getProjectStatistics());
        dashboard.put("userStats", getUserStatistics());
        dashboard.put("manDaysStats", getManDaysStatistics());
        
        return dashboard;
    }

    /**
     * 获取近6个月工时统计
     */
    public Map<String, Object> getMonthlyManDaysStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        
        // 获取所有用户
        List<User> users = userRepository.findAll();
        log.info("获取到用户数量: {}", users.size());
        
        // 获取近6个月的数据
        for (int i = 5; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
            monthData.put("monthName", yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月")));
            
            List<Map<String, Object>> userData = new ArrayList<>();
            
            for (User user : users) {
                Map<String, Object> userMonthData = new HashMap<>();
                userMonthData.put("userId", user.getId());
                String userName = user.getRealName() != null ? user.getRealName() : user.getUsername();
                userMonthData.put("userName", userName);
                
                // 获取该用户在该月的工时统计
                Double userManDays = testTaskRepository.sumActualManDaysByUserAndDateRange(
                    user.getId(), monthStart, monthEnd);
                
                userMonthData.put("manDays", userManDays != null ? userManDays : 0.0);
                userData.add(userMonthData);
                
                log.debug("用户 {} 在 {} 月的工时: {}", userName, yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")), userManDays);
            }
            
            monthData.put("users", userData);
            monthlyData.add(monthData);
        }
        
        statistics.put("monthlyData", monthlyData);
        log.info("返回近6个月工时统计数据，包含 {} 个月", monthlyData.size());
        return statistics;
    }

    /**
     * 获取指定时间范围内没有填写任务或更新进度的用户
     */
    public Map<String, Object> getInactiveUsers(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> inactiveUsersList = new ArrayList<>();
        
        // 获取所有活跃用户
        List<User> activeUsers = userRepository.findByIsActiveTrue();
        
        // 过滤掉系统管理员，统计实际参与检查的用户数
        long actualUserCount = activeUsers.stream()
            .filter(user -> !"admin".equals(user.getUsername()))
            .count();
        
        log.info("检查 {} 到 {} 期间的用户活动，共 {} 个活跃用户（排除管理员后 {} 个）", 
            startDate, endDate, activeUsers.size(), actualUserCount);
        
        for (User user : activeUsers) {
            // 排除系统管理员用户（用户名为admin的用户）
            if ("admin".equals(user.getUsername())) {
                log.debug("跳过系统管理员用户: {}", user.getUsername());
                continue;
            }
            
            // 检查用户在指定时间范围内是否有活动记录
            boolean hasActivity = hasUserActivityInDateRange(user, startDate, endDate);
            
            if (!hasActivity) {
                Map<String, Object> inactiveUser = new HashMap<>();
                inactiveUser.put("userId", user.getId());
                inactiveUser.put("username", user.getUsername());
                inactiveUser.put("realName", user.getRealName());
                inactiveUser.put("department", user.getDepartment());
                inactiveUser.put("email", user.getEmail());
                
                // 获取用户分配的任务数量（仅计算未完成的任务）
                List<TestTask.TaskStatus> activeStatuses = List.of(
                    TestTask.TaskStatus.PLANNED,
                    TestTask.TaskStatus.IN_PROGRESS,
                    TestTask.TaskStatus.ON_HOLD
                );
                long assignedTaskCount = testTaskRepository.countByAssignedToAndStatusIn(user, activeStatuses);
                inactiveUser.put("assignedTaskCount", assignedTaskCount);
                
                inactiveUsersList.add(inactiveUser);
                log.info("用户 {} ({}) 在 {} 到 {} 期间无活动记录，分配任务数: {}", 
                    user.getRealName(), user.getUsername(), startDate, endDate, assignedTaskCount);
            }
        }
        
        result.put("totalUsers", actualUserCount);
        result.put("inactiveUsers", inactiveUsersList);
        result.put("inactiveCount", inactiveUsersList.size());
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        
        log.info("检查完成，共 {} 个用户无活动记录（已排除系统管理员）", inactiveUsersList.size());
        return result;
    }

    /**
     * 检查用户在指定时间范围内是否有活动记录
     */
    private boolean hasUserActivityInDateRange(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 检查是否有新建任务
        long createdTaskCount = testTaskRepository.countByCreatedByUserAndCreatedTimeBetween(
            user, startDateTime, endDateTime);
        if (createdTaskCount > 0) {
            log.debug("用户 {} 在期间内创建了 {} 个任务", user.getRealName(), createdTaskCount);
            return true;
        }
        
        // 检查是否有任务进度更新记录
        long progressUpdateCount = testTaskRepository.countTaskProgressUpdatesByUserAndDateRange(
            user.getId(), startDateTime, endDateTime);
        if (progressUpdateCount > 0) {
            log.debug("用户 {} 在期间内更新了 {} 次任务进度", user.getRealName(), progressUpdateCount);
            return true;
        }
        
        // 检查是否有任务状态更新
        long taskUpdateCount = testTaskRepository.countByUpdatedByAndUpdatedTimeBetween(
            user.getUsername(), startDateTime, endDateTime);
        if (taskUpdateCount > 0) {
            log.debug("用户 {} 在期间内更新了 {} 个任务", user.getRealName(), taskUpdateCount);
            return true;
        }
        
        return false;
    }

    /**
     * 获取上周没有活动的用户（默认情况）
     */
    public Map<String, Object> getLastWeekInactiveUsers() {
        LocalDate now = LocalDate.now();
        LocalDate lastWeekStart = now.minusWeeks(1).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
        
        return getInactiveUsers(lastWeekStart, lastWeekEnd);
    }

    /**
     * 获取所有用户的工作统计数据（仅管理员可访问）
     */
    public List<UserWorkStatsDto> getAllUsersWorkStats() {
        List<User> allUsers = userRepository.findAll();
        List<UserWorkStatsDto> userStatsList = new ArrayList<>();
        
        // 计算本月的工作日数（标准工时）
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        int workDaysInMonth = calculateWorkDays(monthStart, monthEnd);
        double standardWorkDays = workDaysInMonth; // 假设每天8小时，这里按天计算
        
        for (User user : allUsers) {
            UserWorkStatsDto stats = calculateUserWorkStats(user, monthStart, monthEnd, standardWorkDays);
            userStatsList.add(stats);
        }
        
        // 按工时利用率降序排序
        userStatsList.sort((a, b) -> Double.compare(
            b.getWorkloadUtilization() != null ? b.getWorkloadUtilization() : 0.0,
            a.getWorkloadUtilization() != null ? a.getWorkloadUtilization() : 0.0
        ));
        
        return userStatsList;
    }
    
    /**
     * 计算单个用户的工作统计
     */
    private UserWorkStatsDto calculateUserWorkStats(User user, LocalDate startDate, LocalDate endDate, double standardWorkDays) {
        UserWorkStatsDto stats = new UserWorkStatsDto();
        
        // 基本信息
        stats.setUserId(user.getId());
        stats.setUserName(user.getUsername());
        stats.setRealName(user.getRealName());
        stats.setDepartment(user.getDepartment());
        stats.setStandardWorkDays(standardWorkDays);
        
        // 获取用户在指定时间范围内的任务
        List<TestTask> userTasks = testTaskRepository.findByAssignedToAndDateRange(user, startDate, endDate);
        
        // 计算总工时
        double totalManDays = userTasks.stream()
            .filter(task -> task.getActualManDays() != null)
            .mapToDouble(TestTask::getActualManDays)
            .sum();
        
        // 如果没有实际工时，使用预估工时
        if (totalManDays == 0) {
            totalManDays = userTasks.stream()
                .filter(task -> task.getManDays() != null)
                .mapToDouble(TestTask::getManDays)
                .sum();
        }
        
        stats.setTotalManDays(Math.round(totalManDays * 10.0) / 10.0);
        
        // 计算工时利用率
        double workloadUtilization = standardWorkDays > 0 ? (totalManDays / standardWorkDays) * 100 : 0;
        stats.setWorkloadUtilization(Math.round(workloadUtilization * 10.0) / 10.0);
        
        // 确定工作状态
        String workloadStatus;
        if (workloadUtilization > 110) {
            workloadStatus = "OVERLOADED";
        } else if (workloadUtilization >= 90) {
            workloadStatus = "SATURATED";
        } else if (workloadUtilization >= 60) {
            workloadStatus = "NORMAL";
        } else {
            workloadStatus = "IDLE";
        }
        stats.setWorkloadStatus(workloadStatus);
        
        // 任务完成统计
        int totalTasks = userTasks.size();
        long completedTasks = userTasks.stream()
            .filter(task -> task.getStatus() == TestTask.TaskStatus.COMPLETED)
            .count();
        
        // 计算按时完成的任务数
        long onTimeCompletedTasks = userTasks.stream()
            .filter(task -> task.getStatus() == TestTask.TaskStatus.COMPLETED)
            .filter(task -> task.getActualEndDate() != null && task.getExpectedEndDate() != null)
            .filter(task -> !task.getActualEndDate().isAfter(task.getExpectedEndDate()))
            .count();
        
        stats.setTotalTasks(totalTasks);
        stats.setCompletedTasks((int) completedTasks);
        stats.setOnTimeCompletedTasks((int) onTimeCompletedTasks);
        
        // 计算按时完成率
        double onTimeCompletionRate = completedTasks > 0 ? (double) onTimeCompletedTasks / completedTasks * 100 : 0;
        stats.setOnTimeCompletionRate(Math.round(onTimeCompletionRate * 10.0) / 10.0);
        
        // 计算平均延期天数
        double avgDelayDays = userTasks.stream()
            .filter(task -> task.getStatus() == TestTask.TaskStatus.COMPLETED)
            .filter(task -> task.getActualEndDate() != null && task.getExpectedEndDate() != null)
            .filter(task -> task.getActualEndDate().isAfter(task.getExpectedEndDate()))
            .mapToLong(task -> java.time.temporal.ChronoUnit.DAYS.between(task.getExpectedEndDate(), task.getActualEndDate()))
            .average()
            .orElse(0.0);
        stats.setAvgDelayDays(Math.round(avgDelayDays * 10.0) / 10.0);
        
        // 当前任务负载
        List<TestTask> currentTasks = testTaskRepository.findByAssignedToAndStatusIn(user, 
            Arrays.asList(TestTask.TaskStatus.PLANNED, TestTask.TaskStatus.IN_PROGRESS, TestTask.TaskStatus.ON_HOLD));
        
        long currentActiveTasks = currentTasks.stream()
            .filter(task -> task.getStatus() == TestTask.TaskStatus.IN_PROGRESS)
            .count();
        
        long plannedTasks = currentTasks.stream()
            .filter(task -> task.getStatus() == TestTask.TaskStatus.PLANNED)
            .count();
        
        long onHoldTasks = currentTasks.stream()
            .filter(task -> task.getStatus() == TestTask.TaskStatus.ON_HOLD)
            .count();
        
        stats.setCurrentActiveTasks((int) currentActiveTasks);
        stats.setPlannedTasks((int) plannedTasks);
        stats.setOnHoldTasks((int) onHoldTasks);
        
        // 计算当前工作负载（预估工时）
        double currentWorkload = currentTasks.stream()
            .filter(task -> task.getManDays() != null)
            .mapToDouble(TestTask::getManDays)
            .sum();
        stats.setCurrentWorkload(Math.round(currentWorkload * 10.0) / 10.0);
        
        // 计算工时预估准确度
        double estimationAccuracy = calculateEstimationAccuracy(userTasks);
        stats.setEstimationAccuracy(Math.round(estimationAccuracy * 10.0) / 10.0);
        
        // 计算显示属性
        stats.calculateDisplayProperties();
        
        return stats;
    }
    
    /**
     * 计算工时预估准确度
     */
    private double calculateEstimationAccuracy(List<TestTask> tasks) {
        List<Double> accuracyRates = tasks.stream()
            .filter(task -> task.getStatus() == TestTask.TaskStatus.COMPLETED)
            .filter(task -> task.getManDays() != null && task.getActualManDays() != null)
            .filter(task -> task.getManDays() > 0 && task.getActualManDays() > 0)
            .map(task -> {
                double estimated = task.getManDays();
                double actual = task.getActualManDays();
                double accuracy = 100 - Math.abs(estimated - actual) / estimated * 100;
                return Math.max(0, accuracy); // 确保不为负数
            })
            .collect(Collectors.toList());
        
        return accuracyRates.isEmpty() ? 0.0 : 
            accuracyRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    /**
     * 计算工作日数（排除周末，暂不考虑节假日）
     */
    private int calculateWorkDays(LocalDate startDate, LocalDate endDate) {
        int workDays = 0;
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek() != java.time.DayOfWeek.SATURDAY && 
                current.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
                workDays++;
            }
            current = current.plusDays(1);
        }
        
        return workDays;
    }
} 