package com.testtracking.service;

import com.testtracking.dto.TestTaskDto;
import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import com.testtracking.repository.TestTaskRepository;
import com.testtracking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        
        statistics.put("plannedManDays", plannedManDays != null ? plannedManDays : 0.0);
        statistics.put("inProgressManDays", inProgressManDays != null ? inProgressManDays : 0.0);
        statistics.put("completedManDays", completedManDays != null ? completedManDays : 0.0);
        statistics.put("onHoldManDays", onHoldManDays != null ? onHoldManDays : 0.0);
        
        // 总人天
        double totalManDays = (plannedManDays != null ? plannedManDays : 0.0) +
                             (inProgressManDays != null ? inProgressManDays : 0.0) +
                             (completedManDays != null ? completedManDays : 0.0) +
                             (onHoldManDays != null ? onHoldManDays : 0.0);
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
} 