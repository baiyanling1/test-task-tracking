package com.testtracking.controller;

import com.testtracking.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

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
} 