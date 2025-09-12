package com.testtracking.controller;

import com.testtracking.dto.TestTaskDto;
import com.testtracking.dto.UserDto;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
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
     * 统一的任务查询接口 - 支持平铺和树形两种模式
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) String assignedToName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String testType,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String startDateFrom,
            @RequestParam(required = false) String startDateTo,
            @RequestParam(required = false) Boolean isOverdue,
            @RequestParam(required = false) Boolean isExpectedCompletionReached,
            @RequestParam(defaultValue = "flat") String viewMode) {
        
        try {
            String currentUsername = getCurrentUsername();
            Sort sort = Sort.by(sortDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 构建过滤条件
            Map<String, Object> filters = new HashMap<>();
            if (search != null && !search.trim().isEmpty()) filters.put("search", search);
            if (assignedToName != null && !assignedToName.trim().isEmpty()) filters.put("assignedToName", assignedToName);
            if (department != null && !department.trim().isEmpty()) filters.put("department", department);
            if (status != null && !status.trim().isEmpty()) filters.put("status", status);
            if (priority != null && !priority.trim().isEmpty()) filters.put("priority", priority);
            if (taskType != null && !taskType.trim().isEmpty()) filters.put("taskType", taskType);
            if (isOverdue != null) filters.put("isOverdue", isOverdue);
            if (startDateFrom != null && !startDateFrom.trim().isEmpty()) filters.put("startDateFrom", startDateFrom);
            if (startDateTo != null && !startDateTo.trim().isEmpty()) filters.put("startDateTo", startDateTo);
            
            // 统一返回所有任务数据，前端根据字段智能渲染
            Page<TestTaskDto> tasks = testTaskService.getAllTasksUnified(currentUsername, filters, pageable);
            
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

    /**
     * 获取本月或上月的个人任务统计
     */
    @GetMapping("/statistics/user-tasks-by-month")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getUserTaskStatisticsByMonth(@RequestParam String month) {
        try {
            List<Map<String, Object>> userStats = testTaskService.getUserTaskStatisticsByMonth(month);
            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            log.error("获取个人任务统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取个人任务统计失败");
        }
    }

    // 获取当前用户名
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // ========================================
    // 子任务管理API
    // ========================================

    /**
     * 为指定主任务创建子任务
     */
    @PostMapping("/{parentTaskId}/subtasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> createSubTask(
            @PathVariable Long parentTaskId,
            @Valid @RequestBody TestTaskDto subTaskDto) {
        try {
            String currentUsername = getCurrentUsername();
            TestTaskDto createdSubTask = testTaskService.createSubTask(parentTaskId, subTaskDto, currentUsername);
            log.info("子任务创建成功: {} (主任务ID: {})", createdSubTask.getTaskName(), parentTaskId);
            return ResponseEntity.ok(createdSubTask);
        } catch (Exception e) {
            log.error("创建子任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取任务树结构（主任务及其子任务）
     * @deprecated 请使用 GET /api/tasks?viewMode=tree 替代
     */
    @Deprecated
    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getTaskTree(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String assignedToName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Boolean isOverdue,
            @RequestParam(required = false) String startDateFrom,
            @RequestParam(required = false) String startDateTo) {
        try {
            String currentUsername = getCurrentUsername();
            Sort sort = Sort.by(sortDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 构建过滤条件
            Map<String, Object> filters = new HashMap<>();
            if (search != null && !search.trim().isEmpty()) filters.put("search", search);
            if (assignedToName != null && !assignedToName.trim().isEmpty()) filters.put("assignedToName", assignedToName);
            if (department != null && !department.trim().isEmpty()) filters.put("department", department);
            if (status != null && !status.trim().isEmpty()) filters.put("status", status);
            if (priority != null && !priority.trim().isEmpty()) filters.put("priority", priority);
            if (taskType != null && !taskType.trim().isEmpty()) filters.put("taskType", taskType);
            if (isOverdue != null) filters.put("isOverdue", isOverdue);
            if (startDateFrom != null && !startDateFrom.trim().isEmpty()) filters.put("startDateFrom", startDateFrom);
            if (startDateTo != null && !startDateTo.trim().isEmpty()) filters.put("startDateTo", startDateTo);
            
            Page<TestTaskDto> taskTree = testTaskService.getTaskTreeForUserWithFilters(currentUsername, filters, pageable);
            return ResponseEntity.ok(taskTree);
        } catch (Exception e) {
            log.error("获取任务树失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取指定主任务的子任务列表
     */
    @GetMapping("/{parentTaskId}/subtasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getSubTasks(@PathVariable Long parentTaskId) {
        try {
            String currentUsername = getCurrentUsername();
            List<TestTaskDto> subTasks = testTaskService.getSubTasks(parentTaskId, currentUsername);
            return ResponseEntity.ok(subTasks);
        } catch (Exception e) {
            log.error("获取子任务列表失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新子任务进度
     */
    @PutMapping("/subtasks/{subTaskId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> updateSubTaskProgress(
            @PathVariable Long subTaskId,
            @RequestBody Map<String, Integer> request) {
        try {
            String currentUsername = getCurrentUsername();
            Integer progressPercentage = request.get("progressPercentage");
            
            if (progressPercentage == null || progressPercentage < 0 || progressPercentage > 100) {
                return ResponseEntity.badRequest().body("进度百分比必须在0-100之间");
            }
            
            TestTaskDto updatedSubTask = testTaskService.updateSubTaskProgress(subTaskId, progressPercentage, currentUsername);
            return ResponseEntity.ok(updatedSubTask);
        } catch (Exception e) {
            log.error("更新子任务进度失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 主任务创建者手动更新主任务进度
     */
    @PutMapping("/{mainTaskId}/progress/manual")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> updateMainTaskProgressManually(
            @PathVariable Long mainTaskId,
            @RequestBody Map<String, Integer> request) {
        try {
            String currentUsername = getCurrentUsername();
            Integer progressPercentage = request.get("progressPercentage");
            
            if (progressPercentage == null || progressPercentage < 0 || progressPercentage > 100) {
                return ResponseEntity.badRequest().body("进度百分比必须在0-100之间");
            }
            
            TestTaskDto updatedTask = testTaskService.updateMainTaskProgressManually(mainTaskId, progressPercentage, currentUsername);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            log.error("手动更新主任务进度失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 重新计算主任务进度
     */
    @PostMapping("/{mainTaskId}/recalculate-progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> recalculateMainTaskProgress(@PathVariable Long mainTaskId) {
        try {
            testTaskService.calculateMainTaskProgress(mainTaskId);
            return ResponseEntity.ok("主任务进度重新计算成功");
        } catch (Exception e) {
            log.error("重新计算主任务进度失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 将任务转换为主任务
     */
    @PutMapping("/{taskId}/convert-to-main")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> convertToMainTask(@PathVariable Long taskId) {
        try {
            String currentUsername = getCurrentUsername();
            TestTaskDto convertedTask = testTaskService.convertToMainTask(taskId, currentUsername);
            return ResponseEntity.ok(convertedTask);
        } catch (Exception e) {
            log.error("转换为主任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除子任务
     */
    @DeleteMapping("/subtasks/{subTaskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteSubTask(@PathVariable Long subTaskId) {
        try {
            String currentUsername = getCurrentUsername();
            testTaskService.deleteSubTask(subTaskId, currentUsername);
            return ResponseEntity.ok("子任务删除成功");
        } catch (Exception e) {
            log.error("删除子任务失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取用户的子任务统计
     * 特别强调：子任务的责任人可以看到自己的子任务，并归到该责任人下统计
     */
    @GetMapping("/statistics/user-subtasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getUserSubTaskStatistics() {
        try {
            String currentUsername = getCurrentUsername();
            Map<String, Object> statistics = testTaskService.getUserSubTaskStatistics(currentUsername);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取用户子任务统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 管理员获取所有用户的子任务统计
     */
    @GetMapping("/statistics/all-user-subtasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getAllUserSubTaskStatistics() {
        try {
            // 获取所有用户的子任务统计
            List<UserDto> users = userService.getAllUsers();
            List<Map<String, Object>> allUserStats = new ArrayList<>();
            
            for (UserDto user : users) {
                try {
                    Map<String, Object> userStats = testTaskService.getUserSubTaskStatistics(user.getUsername());
                    userStats.put("username", user.getUsername());
                    userStats.put("realName", user.getRealName());
                    userStats.put("department", user.getDepartment());
                    allUserStats.add(userStats);
                } catch (Exception e) {
                    log.warn("获取用户 {} 的子任务统计失败: {}", user.getUsername(), e.getMessage());
                }
            }
            
            return ResponseEntity.ok(allUserStats);
        } catch (Exception e) {
            log.error("获取所有用户子任务统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取子任务责任人分配统计
     */
    @GetMapping("/statistics/subtask-assignees")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getSubTaskAssigneeStatistics() {
        try {
            // 这里可以调用Repository的统计方法
            List<Object[]> assigneeStats = testTaskRepository.countSubTasksByAssignee();
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object[] stat : assigneeStats) {
                Map<String, Object> item = new HashMap<>();
                item.put("assigneeName", stat[0]);
                item.put("subTaskCount", stat[1]);
                result.add(item);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取子任务责任人统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 批量更新子任务状态
     */
    @PutMapping("/subtasks/batch-update-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> batchUpdateSubTaskStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> subTaskIds = (List<Long>) request.get("subTaskIds");
            String status = (String) request.get("status");
            
            if (subTaskIds == null || subTaskIds.isEmpty()) {
                return ResponseEntity.badRequest().body("子任务ID列表不能为空");
            }
            
            String currentUsername = getCurrentUsername();
            List<TestTaskDto> updatedTasks = new ArrayList<>();
            
            for (Long subTaskId : subTaskIds) {
                try {
                    TestTaskDto task = testTaskService.getTaskById(subTaskId);
                    task.setStatus(TestTask.TaskStatus.valueOf(status));
                    TestTaskDto updatedTask = testTaskService.updateTask(subTaskId, task, currentUsername);
                    updatedTasks.add(updatedTask);
                } catch (Exception e) {
                    log.warn("更新子任务 {} 状态失败: {}", subTaskId, e.getMessage());
                }
            }
            
            return ResponseEntity.ok(updatedTasks);
        } catch (Exception e) {
            log.error("批量更新子任务状态失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


} 