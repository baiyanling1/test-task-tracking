package com.testtracking.controller;

import com.testtracking.dto.TestTaskDto;
import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import com.testtracking.service.TestTaskService;
import com.testtracking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import com.testtracking.repository.TestTaskRepository;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestTaskController {

    private final TestTaskService testTaskService;
    private final UserService userService;
    private final TestTaskRepository testTaskRepository;

    /**
     * 创建测试任务
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createTask(@Valid @RequestBody TestTaskDto taskDto) {
        try {
            String currentUsername = getCurrentUsername();
            TestTaskDto createdTask = testTaskService.createTask(taskDto, currentUsername);
            return ResponseEntity.ok(createdTask);
        } catch (Exception e) {
            log.error("创建任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新测试任务
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> updateTask(@PathVariable Long taskId, @Valid @RequestBody TestTaskDto taskDto) {
        try {
            String currentUsername = getCurrentUsername();
            TestTaskDto updatedTask = testTaskService.updateTask(taskId, taskDto, currentUsername);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            log.error("更新任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新任务进度
     */
    @PutMapping("/{taskId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @testTaskService.isTaskAssignee(#taskId, authentication.name)")
    public ResponseEntity<?> updateTaskProgress(@PathVariable Long taskId, @RequestBody ProgressUpdateRequest request) {
        try {
            String currentUsername = getCurrentUsername();
            TestTaskDto updatedTask = testTaskService.updateTaskProgress(taskId, request.getProgressPercentage(), currentUsername);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            log.error("更新任务进度失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 根据ID获取任务
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getTaskById(@PathVariable Long taskId) {
        try {
            TestTaskDto task = testTaskService.getTaskById(taskId);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            log.error("获取任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取当前用户的任务
     */
    @GetMapping("/my-tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getMyTasks() {
        try {
            String currentUsername = getCurrentUsername();
            List<TestTaskDto> tasks = testTaskService.getTasksByUser(currentUsername);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("获取我的任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 分页查询任务
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) String assignedToName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String testType) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, 
                    Sort.Direction.fromString(sortDir.toUpperCase()), sortBy);
            
            User assignedTo = null;
            if (assignedToId != null) {
                assignedTo = userService.getUserById(assignedToId).getId() != null ? 
                        userService.getUserByUsername("").orElse(null) : null;
            }
            
            TestTask.TaskStatus taskStatus = status != null ? TestTask.TaskStatus.valueOf(status.toUpperCase()) : null;
            TestTask.TaskPriority taskPriority = priority != null ? TestTask.TaskPriority.valueOf(priority.toUpperCase()) : null;
            TestTask.TestType taskTestType = testType != null ? TestTask.TestType.valueOf(testType.toUpperCase()) : null;
            
            Page<TestTaskDto> tasks = testTaskService.getTasksWithFilters(
                    assignedTo, assignedToName, department, taskStatus, taskPriority, projectName, taskTestType, pageable);
            
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("获取任务列表失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取任务列表失败");
        }
    }

    /**
     * 获取超时任务
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getOverdueTasks() {
        try {
            List<TestTaskDto> overdueTasks = testTaskService.getOverdueTasks();
            return ResponseEntity.ok(overdueTasks);
        } catch (Exception e) {
            log.error("获取超时任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 搜索任务
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> searchTasks(@RequestParam String keyword) {
        try {
            List<TestTaskDto> tasks = testTaskService.searchTasks(keyword);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("搜索任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        try {
            String currentUsername = getCurrentUsername();
            testTaskService.deleteTask(taskId, currentUsername);
            return ResponseEntity.ok("任务删除成功");
        } catch (Exception e) {
            log.error("删除任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getTaskStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 基础统计
            long totalTasks = testTaskRepository.count();
            long plannedTasks = testTaskRepository.countByStatus(TestTask.TaskStatus.PLANNED);
            long inProgressTasks = testTaskRepository.countByStatus(TestTask.TaskStatus.IN_PROGRESS);
            long completedTasks = testTaskRepository.countByStatus(TestTask.TaskStatus.COMPLETED);
            long onHoldTasks = testTaskRepository.countByStatus(TestTask.TaskStatus.ON_HOLD);
            long cancelledTasks = testTaskRepository.countByStatus(TestTask.TaskStatus.CANCELLED);
            long overdueTasks = testTaskRepository.countOverdueTasks();
            
            stats.put("totalTasks", totalTasks);
            stats.put("plannedTasks", plannedTasks);
            stats.put("inProgressTasks", inProgressTasks);
            stats.put("completedTasks", completedTasks);
            stats.put("onHoldTasks", onHoldTasks);
            stats.put("cancelledTasks", cancelledTasks);
            stats.put("overdueTasks", overdueTasks);
            
            // 人天统计
            Double plannedManDays = testTaskRepository.sumManDaysByStatus(TestTask.TaskStatus.PLANNED);
            Double inProgressManDays = testTaskRepository.sumManDaysByStatus(TestTask.TaskStatus.IN_PROGRESS);
            Double completedManDays = testTaskRepository.sumManDaysByStatus(TestTask.TaskStatus.COMPLETED);
            Double onHoldManDays = testTaskRepository.sumManDaysByStatus(TestTask.TaskStatus.ON_HOLD);
            Double cancelledManDays = testTaskRepository.sumManDaysByStatus(TestTask.TaskStatus.CANCELLED);
            
            // 计算总人天（所有状态的人天总和）
            double totalManDays = (plannedManDays != null ? plannedManDays : 0.0) +
                                 (inProgressManDays != null ? inProgressManDays : 0.0) +
                                 (completedManDays != null ? completedManDays : 0.0) +
                                 (onHoldManDays != null ? onHoldManDays : 0.0) +
                                 (cancelledManDays != null ? cancelledManDays : 0.0);
            
            stats.put("totalManDays", totalManDays);
            
            // 部门统计
            List<Object[]> departmentStats = testTaskRepository.countByDepartment();
            List<Map<String, Object>> departmentData = new ArrayList<>();
            for (Object[] result : departmentStats) {
                Map<String, Object> dept = new HashMap<>();
                dept.put("name", result[0]);
                dept.put("value", result[1]);
                departmentData.add(dept);
            }
            stats.put("departmentStats", departmentData);
            
            // 本周趋势（真实数据）
            LocalDate now = LocalDate.now();
            LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate weekEnd = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            
            List<Integer> weeklyTrend = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate date = weekStart.plusDays(i);
                long count = testTaskRepository.countByStartDate(date);
                weeklyTrend.add((int) count);
            }
            stats.put("weeklyTrend", weeklyTrend);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取任务统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取任务统计失败");
        }
    }

    /**
     * 获取任务状态统计
     */
    @GetMapping("/statistics/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getTaskStatusStatistics() {
        try {
            long plannedTasks = testTaskService.countTasksByStatus(TestTask.TaskStatus.PLANNED);
            long inProgressTasks = testTaskService.countTasksByStatus(TestTask.TaskStatus.IN_PROGRESS);
            long completedTasks = testTaskService.countTasksByStatus(TestTask.TaskStatus.COMPLETED);
            long onHoldTasks = testTaskService.countTasksByStatus(TestTask.TaskStatus.ON_HOLD);
            long cancelledTasks = testTaskService.countTasksByStatus(TestTask.TaskStatus.CANCELLED);
            
            return ResponseEntity.ok(Map.of(
                    "plannedTasks", plannedTasks,
                    "inProgressTasks", inProgressTasks,
                    "completedTasks", completedTasks,
                    "onHoldTasks", onHoldTasks,
                    "cancelledTasks", cancelledTasks
            ));
        } catch (Exception e) {
            log.error("获取任务状态统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取风险等级统计
     */
    @GetMapping("/statistics/risk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getRiskLevelStatistics() {
        try {
            long lowRiskTasks = testTaskService.countTasksByRiskLevel(TestTask.RiskLevel.LOW);
            long mediumRiskTasks = testTaskService.countTasksByRiskLevel(TestTask.RiskLevel.MEDIUM);
            long highRiskTasks = testTaskService.countTasksByRiskLevel(TestTask.RiskLevel.HIGH);
            long criticalRiskTasks = testTaskService.countTasksByRiskLevel(TestTask.RiskLevel.CRITICAL);
            
            return ResponseEntity.ok(Map.of(
                    "lowRiskTasks", lowRiskTasks,
                    "mediumRiskTasks", mediumRiskTasks,
                    "highRiskTasks", highRiskTasks,
                    "criticalRiskTasks", criticalRiskTasks
            ));
        } catch (Exception e) {
            log.error("获取风险统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 手动更新超时任务状态
     */
    @PostMapping("/update-overdue-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> updateOverdueStatus() {
        try {
            testTaskService.updateOverdueStatus();
            return ResponseEntity.ok("超时任务状态更新成功");
        } catch (Exception e) {
            log.error("更新超时任务状态失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取个人任务统计
     */
    @GetMapping("/statistics/user-tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getUserTaskStatistics() {
        try {
            List<Map<String, Object>> userStats = testTaskService.getUserTaskStatistics();
            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            log.error("获取个人任务统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 获取当前用户名
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // 内部类
    public static class ProgressUpdateRequest {
        private Integer progressPercentage;

        public Integer getProgressPercentage() {
            return progressPercentage;
        }

        public void setProgressPercentage(Integer progressPercentage) {
            this.progressPercentage = progressPercentage;
        }
    }
} 