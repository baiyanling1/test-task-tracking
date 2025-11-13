package com.testtracking.service;

import com.testtracking.entity.ScheduledTask;
import com.testtracking.repository.ScheduledTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态定时任务调度服务
 * 负责在运行时动态地添加、修改、删除定时任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicScheduledTaskService {

    private final TaskScheduler taskScheduler;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final TestTaskService testTaskService;
    private final ScheduledTaskService scheduledTaskService;
    private final DatabaseBackupService databaseBackupService;
    private final NotificationService notificationService;
    private final TaskTrackingReminderService taskTrackingReminderService;

    // 存储所有已调度的任务
    private final Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();

    /**
     * 应用启动时初始化所有定时任务
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeScheduledTasks() {
        log.info("开始初始化动态定时任务...");
        
        List<ScheduledTask> tasks = scheduledTaskRepository.findAll();
        for (ScheduledTask task : tasks) {
            if (task.isEnabled()) {
                scheduleTask(task);
            }
        }
        
        log.info("动态定时任务初始化完成，已调度 {} 个任务", scheduledFutures.size());
    }

    /**
     * 调度一个任务
     */
    public void scheduleTask(ScheduledTask task) {
        try {
            // 如果任务已存在，先取消
            cancelTask(task.getTaskName());
            
            // 创建Runnable任务
            Runnable taskRunnable = createTaskRunnable(task.getTaskName());
            if (taskRunnable == null) {
                log.error("无法为任务 {} 创建可执行任务", task.getTaskName());
                return;
            }
            
            // 创建CronTrigger，指定Asia/Shanghai时区
            CronTrigger cronTrigger = new CronTrigger(task.getCronExpression(), ZoneId.of("Asia/Shanghai"));
            
            // 调度任务
            ScheduledFuture<?> future = taskScheduler.schedule(taskRunnable, cronTrigger);
            scheduledFutures.put(task.getTaskName(), future);
            
            log.info("任务 {} 已调度，cron表达式: {}", task.getTaskName(), task.getCronExpression());
            
        } catch (Exception e) {
            log.error("调度任务 {} 失败: {}", task.getTaskName(), e.getMessage(), e);
        }
    }

    /**
     * 取消一个任务
     */
    public void cancelTask(String taskName) {
        ScheduledFuture<?> future = scheduledFutures.remove(taskName);
        if (future != null) {
            future.cancel(false);
            log.info("任务 {} 已取消", taskName);
        }
    }

    /**
     * 重新调度任务（更新cron表达式后调用）
     */
    public void rescheduleTask(String taskName) {
        scheduledTaskRepository.findByTaskName(taskName).ifPresent(task -> {
            if (task.isEnabled()) {
                scheduleTask(task);
            } else {
                cancelTask(taskName);
            }
        });
    }

    /**
     * 启用任务
     */
    public void enableTask(String taskName) {
        scheduledTaskRepository.findByTaskName(taskName).ifPresent(task -> {
            if (task.isEnabled()) {
                scheduleTask(task);
            }
        });
    }

    /**
     * 禁用任务
     */
    public void disableTask(String taskName) {
        cancelTask(taskName);
    }

    /**
     * 创建任务的Runnable
     */
    private Runnable createTaskRunnable(String taskName) {
        switch (taskName) {
            case "checkOverdueTasks":
                return () -> testTaskService.checkOverdueTasks();
            case "cleanOldLoginHistory":
                return () -> scheduledTaskService.cleanOldLoginHistory();
            case "scheduledBackup":
                return () -> databaseBackupService.scheduledBackup();
            case "deleteExpiredNotifications":
                return () -> notificationService.deleteExpiredNotifications();
            case "checkFridayTaskTracking":
                return () -> taskTrackingReminderService.checkFridayTaskTracking();
            default:
                log.error("未知的任务名称: {}", taskName);
                return null;
        }
    }

    /**
     * 获取所有已调度的任务
     */
    public Map<String, ScheduledFuture<?>> getScheduledTasks() {
        return scheduledFutures;
    }
}

