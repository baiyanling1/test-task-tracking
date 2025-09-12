package com.testtracking.controller;

import com.testtracking.service.DashboardService;
import com.testtracking.service.UserService;
import com.testtracking.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    /**
     * 获取综合Dashboard数据
     */
    @GetMapping
    public ResponseEntity<?> getDashboardData() {
        try {
            Map<String, Object> dashboardData = dashboardService.getDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            log.error("获取Dashboard数据失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取Dashboard数据失败");
        }
    }

    /**
     * 获取本周统计信息
     */
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyStatistics() {
        try {
            Map<String, Object> weeklyStats = dashboardService.getWeeklyStatistics();
            return ResponseEntity.ok(weeklyStats);
        } catch (Exception e) {
            log.error("获取本周统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取本周统计失败");
        }
    }

    /**
     * 获取本月统计信息
     */
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyStatistics() {
        try {
            Map<String, Object> monthlyStats = dashboardService.getMonthlyStatistics();
            return ResponseEntity.ok(monthlyStats);
        } catch (Exception e) {
            log.error("获取本月统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取本月统计失败");
        }
    }

    /**
     * 获取本年统计信息
     */
    @GetMapping("/yearly")
    public ResponseEntity<?> getYearlyStatistics() {
        try {
            Map<String, Object> yearlyStats = dashboardService.getYearlyStatistics();
            return ResponseEntity.ok(yearlyStats);
        } catch (Exception e) {
            log.error("获取本年统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取本年统计失败");
        }
    }

    /**
     * 获取任务状态统计
     */
    @GetMapping("/task-status")
    public ResponseEntity<?> getTaskStatusStatistics() {
        try {
            Map<String, Object> statusStats = dashboardService.getTaskStatusStatistics();
            return ResponseEntity.ok(statusStats);
        } catch (Exception e) {
            log.error("获取任务状态统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取任务状态统计失败");
        }
    }

    /**
     * 获取风险等级统计
     */
    @GetMapping("/risk-level")
    public ResponseEntity<?> getRiskLevelStatistics() {
        try {
            Map<String, Object> riskStats = dashboardService.getRiskLevelStatistics();
            return ResponseEntity.ok(riskStats);
        } catch (Exception e) {
            log.error("获取风险等级统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取风险等级统计失败");
        }
    }

    /**
     * 获取近6个月工时统计
     */
    @GetMapping("/monthly-man-days")
    public ResponseEntity<?> getMonthlyManDaysStatistics() {
        try {
            Map<String, Object> monthlyManDaysStats = dashboardService.getMonthlyManDaysStatistics();
            return ResponseEntity.ok(monthlyManDaysStats);
        } catch (Exception e) {
            log.error("获取近6个月工时统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取近6个月工时统计失败");
        }
    }

    /**
     * 获取超时任务统计
     */
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueTaskStatistics() {
        try {
            Map<String, Object> overdueStats = dashboardService.getOverdueTaskStatistics();
            return ResponseEntity.ok(overdueStats);
        } catch (Exception e) {
            log.error("获取超时任务统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取超时任务统计失败");
        }
    }

    /**
     * 获取项目统计
     */
    @GetMapping("/projects")
    public ResponseEntity<?> getProjectStatistics() {
        try {
            Map<String, Object> projectStats = dashboardService.getProjectStatistics();
            return ResponseEntity.ok(projectStats);
        } catch (Exception e) {
            log.error("获取项目统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取项目统计失败");
        }
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUserStatistics() {
        try {
            Map<String, Object> userStats = dashboardService.getUserStatistics();
            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            log.error("获取用户统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取用户统计失败");
        }
    }

    /**
     * 获取人天统计
     */
    @GetMapping("/man-days")
    public ResponseEntity<?> getManDaysStatistics() {
        try {
            Map<String, Object> statistics = dashboardService.getManDaysStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取人天统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取人天统计失败");
        }
    }

    /**
     * 获取部门统计
     */
    @GetMapping("/department")
    public ResponseEntity<?> getDepartmentStatistics() {
        try {
            Map<String, Object> statistics = dashboardService.getDepartmentStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取部门统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取部门统计失败");
        }
    }

    /**
     * 获取上周没有填写任务的用户（默认）
     */
    @GetMapping("/inactive-users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getLastWeekInactiveUsers() {
        try {
            Map<String, Object> statistics = dashboardService.getLastWeekInactiveUsers();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取上周未活跃用户统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取上周未活跃用户统计失败");
        }
    }

    /**
     * 获取指定时间范围内没有填写任务的用户
     */
    @GetMapping("/inactive-users/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getInactiveUsersByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            
            if (start.isAfter(end)) {
                return ResponseEntity.badRequest().body("开始日期不能晚于结束日期");
            }
            
            Map<String, Object> statistics = dashboardService.getInactiveUsers(start, end);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取指定时间范围内未活跃用户统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取指定时间范围内未活跃用户统计失败: " + e.getMessage());
        }
    }

    // ========================================
    // 子任务统计API
    // ========================================

    /**
     * 获取子任务统计概览
     */
    @GetMapping("/subtask-statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getSubTaskStatistics() {
        try {
            Map<String, Object> statistics = dashboardService.getSubTaskStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取子任务统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取子任务统计失败");
        }
    }

    /**
     * 获取本月子任务按用户统计
     */
    @GetMapping("/monthly-subtask-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getMonthlySubTaskStatsByUser() {
        try {
            Map<String, Object> statistics = dashboardService.getMonthlySubTaskStatsByUser();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取本月子任务用户统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取本月子任务用户统计失败");
        }
    }

    /**
     * 获取指定用户的子任务详细统计
     */
    @GetMapping("/user-subtask-details/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getUserSubTaskDetails(@PathVariable String username, 
                                                   Authentication authentication) {
        try {
            String currentUsername = authentication.getName();
            
            // 检查权限：普通用户只能查看自己的统计
            if (!currentUsername.equals(username)) {
                User currentUser = userService.getUserByUsername(currentUsername)
                        .orElseThrow(() -> new RuntimeException("当前用户不存在"));
                if (currentUser.getRole() != User.UserRole.ADMIN && currentUser.getRole() != User.UserRole.MANAGER) {
                    return ResponseEntity.status(403).body("没有权限查看其他用户的子任务统计");
                }
            }
            
            Map<String, Object> details = dashboardService.getUserSubTaskDetails(username);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("获取用户子任务详细统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取用户子任务详细统计失败");
        }
    }

    /**
     * 获取主任务进度汇总
     */
    @GetMapping("/main-task-progress-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getMainTaskProgressSummary() {
        try {
            Map<String, Object> summary = dashboardService.getMainTaskProgressSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("获取主任务进度汇总失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取主任务进度汇总失败");
        }
    }
} 