package com.testtracking.controller;

import com.testtracking.dto.ScheduledTaskDto;
import com.testtracking.service.ScheduledTaskManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/scheduled-tasks")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class ScheduledTaskManagementController {

    private final ScheduledTaskManagementService scheduledTaskManagementService;

    /**
     * 获取定时任务列表
     */
    @GetMapping
    public ResponseEntity<List<ScheduledTaskDto>> getScheduledTasks() {
        try {
            List<ScheduledTaskDto> tasks = scheduledTaskManagementService.getAllScheduledTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("获取定时任务列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 手动触发任务
     */
    @PostMapping("/{taskName}/trigger")
    public ResponseEntity<?> triggerTask(@PathVariable String taskName) {
        try {
            scheduledTaskManagementService.triggerTask(taskName);
            return ResponseEntity.ok("任务执行成功");
        } catch (Exception e) {
            log.error("手动触发任务失败: taskName={}, error={}", taskName, e.getMessage(), e);
            return ResponseEntity.badRequest().body("任务执行失败: " + e.getMessage());
        }
    }



    /**
     * 切换任务状态
     */
    @PutMapping("/{taskName}/toggle")
    public ResponseEntity<?> toggleTask(@PathVariable String taskName, @RequestParam boolean enabled) {
        try {
            scheduledTaskManagementService.toggleTaskStatus(taskName, enabled);
            return ResponseEntity.ok("任务状态更新成功");
        } catch (Exception e) {
            log.error("切换任务状态失败: taskName={}, enabled={}, error={}", taskName, enabled, e.getMessage(), e);
            return ResponseEntity.badRequest().body("任务状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新任务执行计划
     */
    @PostMapping("/{taskName}/schedule")
    public ResponseEntity<?> updateTaskSchedule(@PathVariable String taskName, @RequestBody java.util.Map<String, String> request) {
        try {
            String cronExpression = request.get("cronExpression");
            if (cronExpression == null || cronExpression.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("cron表达式不能为空");
            }
            
            scheduledTaskManagementService.updateTaskSchedule(taskName, cronExpression.trim());
            return ResponseEntity.ok("任务执行计划更新成功");
        } catch (Exception e) {
            log.error("更新任务执行计划失败: taskName={}, error={}", taskName, e.getMessage());
            return ResponseEntity.badRequest().body("更新任务执行计划失败: " + e.getMessage());
        }
    }
}
