package com.testtracking.controller;

import com.testtracking.dto.TaskProgressDto;
import com.testtracking.service.TaskProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/progress")
@RequiredArgsConstructor
@Slf4j
public class TaskProgressController {

    private final TaskProgressService taskProgressService;

    /**
     * 测试端点 - 验证路由是否工作
     */
    @GetMapping("/test")
    public ResponseEntity<String> test(@PathVariable Long taskId) {
        log.info("测试端点被调用: taskId={}", taskId);
        return ResponseEntity.ok("TaskProgressController is working! TaskId: " + taskId);
    }

    /**
     * 获取任务进度列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getTaskProgress(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("收到获取任务进度请求: taskId={}, page={}, size={}", taskId, page, size);
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<TaskProgressDto> progressList = taskProgressService.getTaskProgressByTaskId(taskId, pageable);
            log.info("成功获取任务进度: taskId={}, 进度数量={}", taskId, progressList.getTotalElements());
            return ResponseEntity.ok(progressList);
        } catch (Exception e) {
            log.error("获取任务进度列表失败: taskId={}, error={}", taskId, e.getMessage());
            return ResponseEntity.badRequest().body("获取任务进度列表失败");
        }
    }

    /**
     * 添加任务进度
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> addTaskProgress(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskProgressDto progressDto) {
        log.info("收到添加任务进度请求: taskId={}, progressDto={}", taskId, progressDto);
        try {
            progressDto.setTaskId(taskId);
            TaskProgressDto savedProgress = taskProgressService.addTaskProgress(progressDto);
            log.info("成功添加任务进度: taskId={}, progressId={}", taskId, savedProgress.getId());
            return ResponseEntity.ok(savedProgress);
        } catch (Exception e) {
            log.error("添加任务进度失败: taskId={}, error={}", taskId, e.getMessage());
            return ResponseEntity.badRequest().body("添加任务进度失败");
        }
    }

    /**
     * 更新任务进度
     */
    @PutMapping("/{progressId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> updateTaskProgress(
            @PathVariable Long taskId,
            @PathVariable Long progressId,
            @Valid @RequestBody TaskProgressDto progressDto) {
        try {
            progressDto.setId(progressId);
            progressDto.setTaskId(taskId);
            TaskProgressDto updatedProgress = taskProgressService.updateTaskProgress(progressDto);
            return ResponseEntity.ok(updatedProgress);
        } catch (Exception e) {
            log.error("更新任务进度失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("更新任务进度失败");
        }
    }

    /**
     * 删除任务进度
     */
    @DeleteMapping("/{progressId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteTaskProgress(
            @PathVariable Long taskId,
            @PathVariable Long progressId) {
        try {
            taskProgressService.deleteTaskProgress(progressId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("删除任务进度失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("删除任务进度失败");
        }
    }

    /**
     * 获取任务最新进度
     */
    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getLatestTaskProgress(@PathVariable Long taskId) {
        try {
            TaskProgressDto latestProgress = taskProgressService.getLatestTaskProgress(taskId);
            return ResponseEntity.ok(latestProgress);
        } catch (Exception e) {
            log.error("获取最新任务进度失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取最新任务进度失败");
        }
    }
}
