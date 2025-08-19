package com.testtracking.service;

import com.testtracking.dto.ScheduledTaskDto;
import com.testtracking.entity.ScheduledTask;
import com.testtracking.repository.ScheduledTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskManagementService {

    private final ScheduledTaskRepository scheduledTaskRepository;
    private final ApplicationContext applicationContext;
    private final TestTaskService testTaskService;
    private final ScheduledTaskService scheduledTaskService;
    private final DatabaseBackupService databaseBackupService;
    private final NotificationService notificationService;
    private final TaskTrackingReminderService taskTrackingReminderService;
    private final TaskScheduler taskScheduler;
    private final CronExpressionTestService cronExpressionTestService;

    /**
     * 获取所有定时任务列表
     */
    @Transactional(readOnly = true)
    public List<ScheduledTaskDto> getAllScheduledTasks() {
        List<ScheduledTaskDto> tasks = new ArrayList<>();
        
        // 从数据库获取任务列表
        List<ScheduledTask> dbTasks = scheduledTaskRepository.findAll();
        
        for (ScheduledTask task : dbTasks) {
            ScheduledTaskDto dto = ScheduledTaskDto.fromEntity(task);
            
            // 如果数据库中的下次执行时间为空，则重新计算
            if (task.getNextExecuteTime() == null) {
                LocalDateTime nextTime = calculateNextExecuteTime(task.getCronExpression());
                dto.setNextExecuteTime(nextTime);
                
                // 更新数据库中的下次执行时间
                task.setNextExecuteTime(nextTime);
                scheduledTaskRepository.save(task);
                log.info("任务 {} 下次执行时间已重新计算: {}", task.getTaskName(), nextTime);
            } else {
                dto.setNextExecuteTime(task.getNextExecuteTime());
            }
            
            tasks.add(dto);
        }
        
        return tasks;
    }

    /**
     * 手动触发定时任务
     */
    @Transactional
    public void triggerTask(String taskName) {
        log.info("手动触发定时任务: {}", taskName);
        
        // 使用Asia/Shanghai时区的当前时间
        LocalDateTime executeTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        
        try {
            switch (taskName) {
                case "checkOverdueTasks":
                    testTaskService.checkOverdueTasks();
                    break;
                case "cleanOldLoginHistory":
                    scheduledTaskService.cleanOldLoginHistory();
                    break;
                case "scheduledBackup":
                    databaseBackupService.scheduledBackup();
                    break;
                case "deleteExpiredNotifications":
                    notificationService.deleteExpiredNotifications();
                    break;
                case "checkFridayTaskTracking":
                    taskTrackingReminderService.manualCheck();
                    break;
                default:
                    throw new RuntimeException("未知的定时任务: " + taskName);
            }
            
            // 更新任务的最后执行时间和结果（手动执行不更新下次执行时间）
            updateTaskExecutionResult(taskName, executeTime, "SUCCESS", null, false);
            log.info("定时任务 {} 手动执行成功", taskName);
            
        } catch (Exception e) {
            log.error("定时任务 {} 手动执行失败: {}", taskName, e.getMessage(), e);
            // 更新任务的最后执行时间和结果（手动执行不更新下次执行时间）
            updateTaskExecutionResult(taskName, executeTime, "FAILED", e.getMessage(), false);
            throw e;
        }
    }

    /**
     * 切换任务状态
     */
    @Transactional
    public void toggleTaskStatus(String taskName, boolean enabled) {
        log.info("切换任务 {} 状态为: {}", taskName, enabled ? "启用" : "禁用");
        
        ScheduledTask task = scheduledTaskRepository.findByTaskName(taskName)
            .orElseThrow(() -> new RuntimeException("任务不存在: " + taskName));
        
        task.setEnabled(enabled);
        task.setStatus(enabled ? ScheduledTask.TaskStatus.ENABLED : ScheduledTask.TaskStatus.DISABLED);
        scheduledTaskRepository.save(task);
        
        log.info("任务 {} 状态已更新为: {}", taskName, enabled ? "启用" : "禁用");
    }

    /**
     * 更新任务执行计划
     */
    @Transactional
    public void updateTaskSchedule(String taskName, String cronExpression) {
        log.info("更新任务 {} 执行计划为: {}", taskName, cronExpression);
        
        // 验证cron表达式
        try {
            new CronTrigger(cronExpression);
        } catch (Exception e) {
            throw new RuntimeException("无效的cron表达式: " + e.getMessage());
        }
        
        ScheduledTask task = scheduledTaskRepository.findByTaskName(taskName)
            .orElseThrow(() -> new RuntimeException("任务不存在: " + taskName));
        
        task.setCronExpression(cronExpression);
        
        // 计算并更新下次执行时间
        LocalDateTime nextExecuteTime = calculateNextExecuteTime(cronExpression);
        task.setNextExecuteTime(nextExecuteTime);
        
        scheduledTaskRepository.save(task);
        
        log.info("任务 {} 执行计划已更新为: {}, 下次执行时间: {}", taskName, cronExpression, nextExecuteTime);
    }

    /**
     * 应用启动时初始化所有任务的下次执行时间
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeNextExecuteTimes() {
        log.info("开始初始化所有定时任务的下次执行时间...");
        
        // 先测试cron表达式解析
        cronExpressionTestService.testCronExpressions();
        
        List<ScheduledTask> tasks = scheduledTaskRepository.findAll();
        for (ScheduledTask task : tasks) {
            try {
                LocalDateTime nextExecuteTime = calculateNextExecuteTime(task.getCronExpression());
                task.setNextExecuteTime(nextExecuteTime);
                scheduledTaskRepository.save(task);
                log.info("任务 {} 下次执行时间已初始化: {}", task.getTaskName(), nextExecuteTime);
            } catch (Exception e) {
                log.error("初始化任务 {} 下次执行时间失败: {}", task.getTaskName(), e.getMessage());
            }
        }
        
        log.info("所有定时任务的下次执行时间初始化完成");
    }

    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextExecuteTime(String cronExpression) {
        try {
            log.debug("计算下次执行时间，cron表达式: {}", cronExpression);
            
            // 验证cron表达式格式
            if (cronExpression == null || cronExpression.trim().isEmpty()) {
                log.error("cron表达式为空");
                return null;
            }
            
            // 创建CronTrigger，明确指定Asia/Shanghai时区
            CronTrigger trigger = new CronTrigger(cronExpression.trim(), ZoneId.of("Asia/Shanghai"));
            
            // 计算下次执行时间，使用Asia/Shanghai时区的当前时间
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
            Date nowDate = Date.from(now.atZone(ZoneId.of("Asia/Shanghai")).toInstant());
            Date nextExecution = trigger.nextExecutionTime(new SimpleTriggerContext(nowDate, nowDate, nowDate));
            if (nextExecution != null) {
                // 转换为Asia/Shanghai时区
                LocalDateTime nextTime = LocalDateTime.ofInstant(nextExecution.toInstant(), ZoneId.of("Asia/Shanghai"));
                log.debug("计算得到下次执行时间: {}", nextTime);
                return nextTime;
            } else {
                log.warn("无法计算下次执行时间，cron表达式: {}", cronExpression);
            }
        } catch (IllegalArgumentException e) {
            log.error("cron表达式格式错误: {}, error: {}", cronExpression, e.getMessage());
        } catch (Exception e) {
            log.error("解析cron表达式失败: {}, error: {}", cronExpression, e.getMessage(), e);
        }
        return null;
    }

    /**
     * 更新任务执行结果
     */
    private void updateTaskExecutionResult(String taskName, LocalDateTime executeTime, String result, String errorMessage) {
        updateTaskExecutionResult(taskName, executeTime, result, errorMessage, true);
    }

    /**
     * 更新任务执行结果
     * @param updateNextExecuteTime 是否更新下次执行时间（手动执行时不更新）
     */
    private void updateTaskExecutionResult(String taskName, LocalDateTime executeTime, String result, String errorMessage, boolean updateNextExecuteTime) {
        try {
            ScheduledTask task = scheduledTaskRepository.findByTaskName(taskName)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskName));
            
            task.setLastExecuteTime(executeTime);
            task.setLastExecuteResult(result);
            
            // 只有在自动执行时才更新下次执行时间
            if (updateNextExecuteTime) {
                LocalDateTime nextExecuteTime = calculateNextExecuteTime(task.getCronExpression());
                task.setNextExecuteTime(nextExecuteTime);
                log.info("任务 {} 执行结果已更新: 时间={}, 结果={}, 下次执行时间={}", taskName, executeTime, result, nextExecuteTime);
            } else {
                log.info("任务 {} 手动执行结果已更新: 时间={}, 结果={}", taskName, executeTime, result);
            }
            
            scheduledTaskRepository.save(task);
        } catch (Exception e) {
            log.error("更新任务执行结果失败: taskName={}, error={}", taskName, e.getMessage());
        }
    }
}
