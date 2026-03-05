package com.testtracking.service;

import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import com.testtracking.repository.TestTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TaskStatusUpdateService {

    @Autowired
    private TestTaskRepository testTaskRepository;
    
    @Autowired
    private TaskTrackingConfigService taskTrackingConfigService;

    /**
     * 每天凌晨2点自动检查和更新任务状态（已由动态调度器管理，此注解已禁用）
     */
    // @Scheduled(cron = "0 0 2 * * ?") // 已禁用：由DynamicScheduledTaskService动态管理
    // @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "scheduler.enabled", havingValue = "false", matchIfMissing = false)
    @Transactional
    public void updateTaskStatuses() {
        log.info("开始自动更新任务状态...");
        
        try {
            // 获取所有未完成的任务
            List<TestTask> activeTasks = testTaskRepository.findByStatusNotIn(
                List.of(TestTask.TaskStatus.COMPLETED, TestTask.TaskStatus.CANCELLED)
            );
            
            int updatedCount = 0;
            int skippedCount = 0;
            
            for (TestTask task : activeTasks) {
                // 检查任务负责人是否应该被跟踪
                if (!shouldTrackUser(task.getAssignedTo())) {
                    skippedCount++;
                    log.debug("任务 {} 的负责人已禁用或在白名单中，跳过状态更新", task.getTaskName());
                    continue;
                }
                
                TestTask.TaskStatus oldStatus = task.getStatus();
                task.checkTaskStatusAndOverdue();
                
                if (oldStatus != task.getStatus()) {
                    log.info("任务 {} 状态从 {} 更新为 {}", 
                        task.getTaskName(), oldStatus.getDescription(), task.getStatus().getDescription());
                    updatedCount++;
                }
            }
            
            // 批量保存更新
            if (!activeTasks.isEmpty()) {
                testTaskRepository.saveAll(activeTasks);
            }
            
            log.info("任务状态更新完成，共检查 {} 个任务，更新 {} 个，跳过 {} 个（用户已禁用或在白名单）", 
                     activeTasks.size(), updatedCount, skippedCount);
            
        } catch (Exception e) {
            log.error("自动更新任务状态时发生错误", e);
        }
    }

    /**
     * 每小时检查一次预期完成时间到达的任务（已由动态调度器管理，此注解已禁用）
     */
    // @Scheduled(cron = "0 0 * * * ?") // 已禁用：由DynamicScheduledTaskService动态管理
    // @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "scheduler.enabled", havingValue = "false", matchIfMissing = false)
    @Transactional
    public void checkExpectedCompletionReached() {
        log.info("检查预期完成时间到达的任务...");
        
        try {
            // 获取今天预期完成的任务
            List<TestTask> tasksReachingExpectedDate = testTaskRepository.findByExpectedEndDateAndStatus(
                java.time.LocalDate.now(), 
                TestTask.TaskStatus.IN_PROGRESS
            );
            
            int updatedCount = 0;
            int skippedCount = 0;
            
            for (TestTask task : tasksReachingExpectedDate) {
                // 检查任务负责人是否应该被跟踪
                if (!shouldTrackUser(task.getAssignedTo())) {
                    skippedCount++;
                    log.debug("任务 {} 的负责人已禁用或在白名单中，跳过检查", task.getTaskName());
                    continue;
                }
                
                task.checkTaskStatusAndOverdue();
                log.info("任务 {} 已达到预期完成时间", task.getTaskName());
                updatedCount++;
            }
            
            // 批量保存更新
            if (!tasksReachingExpectedDate.isEmpty()) {
                testTaskRepository.saveAll(tasksReachingExpectedDate);
            }
            
            log.info("预期完成时间检查完成，共检查 {} 个任务，更新 {} 个，跳过 {} 个（用户已禁用或在白名单）", 
                     tasksReachingExpectedDate.size(), updatedCount, skippedCount);
            
        } catch (Exception e) {
            log.error("检查预期完成时间到达的任务时发生错误", e);
        }
    }
    
    /**
     * 检查用户是否应该被跟踪（排除已禁用用户和白名单用户）
     */
    private boolean shouldTrackUser(User user) {
        if (user == null) {
            return false;
        }
        
        // 检查用户是否被禁用
        if (user.getIsActive() == null || !user.getIsActive()) {
            log.debug("用户 {} 已被禁用", user.getRealName());
            return false;
        }
        
        // 检查用户是否在白名单中
        if (taskTrackingConfigService.isUserInWhitelist(user.getUsername())) {
            log.debug("用户 {} 在白名单中", user.getRealName());
            return false;
        }
        
        return true;
    }
}
