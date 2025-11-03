package com.testtracking.service;

import com.testtracking.dto.NotificationDto;
import com.testtracking.entity.Notification;
import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import com.testtracking.entity.ScheduledTask;
import com.testtracking.repository.TestTaskRepository;
import com.testtracking.repository.UserRepository;
import com.testtracking.repository.ScheduledTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskTrackingReminderService {

    private final UserRepository userRepository;
    private final TestTaskRepository testTaskRepository;
    private final NotificationService notificationService;
    private final DingTalkNotificationService dingTalkNotificationService;
    private final FeiShuNotificationService feiShuNotificationService;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final TaskTrackingConfigService taskTrackingConfigService;

    /**
     * 每周一早上9点半检查上周五任务跟踪表填写情况
     */
    @Scheduled(cron = "0 30 9 * * MON")
    public void checkFridayTaskTracking() {
        log.info("开始检查上周五任务跟踪表填写情况...");
        
        // 使用Asia/Shanghai时区的当前时间
        LocalDateTime executeTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        
        try {
            LocalDate lastFriday = getLastFriday();
            List<User> inactiveUsers = findInactiveUsers(lastFriday);
            
            if (!inactiveUsers.isEmpty()) {
                sendReminderNotifications(inactiveUsers, lastFriday);
                log.info("发送任务跟踪提醒通知完成，涉及 {} 名用户", inactiveUsers.size());
            } else {
                log.info("所有用户都已按时填写任务跟踪表");
            }
            
            // 更新定时任务执行记录
            updateScheduledTaskExecution("checkFridayTaskTracking", executeTime, "SUCCESS", null);
            
        } catch (Exception e) {
            log.error("检查任务跟踪表填写情况失败: {}", e.getMessage(), e);
            updateScheduledTaskExecution("checkFridayTaskTracking", executeTime, "FAILED", e.getMessage());
            throw e;
        }
    }

    /**
     * 获取上周五日期
     */
    private LocalDate getLastFriday() {
        LocalDate today = LocalDate.now();
        int daysToSubtract = today.getDayOfWeek().getValue() + 2; // 计算到上周五的天数
        return today.minusDays(daysToSubtract);
    }

    /**
     * 查找上周五未填写任务的用户
     */
    @Transactional(readOnly = true)
    private List<User> findInactiveUsers(LocalDate checkDate) {
        log.info("检查日期: {}", checkDate);
        
        // 获取所有活跃用户
        List<User> activeUsers = userRepository.findByIsActiveTrue();
        List<User> inactiveUsers = new ArrayList<>();
        
        for (User user : activeUsers) {
            // 检查用户是否在白名单中
            if (taskTrackingConfigService.isUserInWhitelist(user.getUsername())) {
                log.info("用户 {} 在白名单中，跳过检查", user.getRealName());
                continue;
            }
            
            // 检查用户在上周五是否有活动记录
            boolean hasActivity = hasUserActivityOnDate(user, checkDate);
            if (!hasActivity) {
                inactiveUsers.add(user);
                log.info("用户 {} 在上周五未填写任务跟踪表", user.getRealName());
            }
        }
        
        return inactiveUsers;
    }

    /**
     * 检查用户指定日期是否有活动
     */
    private boolean hasUserActivityOnDate(User user, LocalDate checkDate) {
        // 检查是否有任务创建或更新记录
        LocalDateTime startOfDay = checkDate.atStartOfDay();
        LocalDateTime endOfDay = checkDate.atTime(23, 59, 59);
        
        // 检查任务创建
        List<TestTask> createdTasks = testTaskRepository.findByCreatedByUserAndCreatedTimeBetween(
            user, startOfDay, endOfDay
        );
        
        if (!createdTasks.isEmpty()) {
            return true;
        }
        
        // 检查任务更新（通过最后更新时间）
        List<TestTask> updatedTasks = testTaskRepository.findByAssignedToAndUpdatedTimeBetween(
            user, startOfDay, endOfDay
        );
        
        return !updatedTasks.isEmpty();
    }

    /**
     * 发送提醒通知
     */
    private void sendReminderNotifications(List<User> users, LocalDate checkDate) {
        String checkDateStr = checkDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // 构建钉钉通知内容
        String dingTalkMessage = buildDingTalkMessage(users, checkDateStr);
        
        // 发送钉钉通知
        try {
            sendDingTalkNotification(dingTalkMessage);
        } catch (Exception e) {
            log.error("发送钉钉通知失败: {}", e.getMessage(), e);
        }
        
        // 发送系统内部通知
        for (User user : users) {
            try {
                sendSystemNotification(user, checkDateStr);
            } catch (Exception e) {
                log.error("为用户 {} 发送系统通知失败: {}", user.getRealName(), e.getMessage());
            }
        }
    }

    /**
     * 构建钉钉通知消息
     */
    private String buildDingTalkMessage(List<User> users, String checkDate) {
        StringBuilder message = new StringBuilder();
        message.append("## 📢 任务跟踪表填写提醒\n\n");
        message.append("**检查时间**：上周五（").append(checkDate).append("）\n\n");
        message.append("**检查结果**：发现 ").append(users.size()).append(" 名人员未填写或更新任务跟踪表\n\n");
        
        message.append("### 未填写人员列表：\n");
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            List<TestTask> assignedTasks = testTaskRepository.findByAssignedToAndStatusIn(
                user, 
                List.of(TestTask.TaskStatus.PLANNED, TestTask.TaskStatus.IN_PROGRESS)
            );
            
            message.append(i + 1).append(". **").append(user.getRealName())
                   .append("** - ").append(user.getDepartment())
                   .append(" - 负责任务：").append(assignedTasks.size()).append("个");
            
            // 如果没有分配任务，添加说明
            if (assignedTasks.isEmpty()) {
                message.append(" (无分配任务)");
            }
            message.append("\n");
        }
        
        message.append("\n### 提醒事项：\n");
        message.append("- 请及时登录系统填写任务进度\n");
        message.append("- 更新任务状态和工时信息\n");
        message.append("- 如有特殊情况请及时说明\n");
        message.append("- 即使没有分配任务，也请登录系统确认状态\n\n");
        
        message.append("---\n");
        message.append("*此消息由测试任务跟踪系统自动发送*");
        
        return message.toString();
    }

    /**
     * 发送钉钉和飞书通知
     */
    private void sendDingTalkNotification(String message) {
        // 创建临时通知对象用于钉钉和飞书发送
        Notification notification = new Notification();
        notification.setTitle("任务跟踪表填写提醒");
        notification.setContent(message);
        notification.setType(Notification.NotificationType.SYSTEM_ALERT);
        notification.setPriority(Notification.NotificationPriority.HIGH);
        notification.setCreatedTime(LocalDateTime.now());
        
        // 发送到钉钉和飞书
        dingTalkNotificationService.sendNotificationToDingTalk(notification);
        feiShuNotificationService.sendNotificationToFeiShu(notification);
    }

    /**
     * 发送系统内部通知
     */
    private void sendSystemNotification(User user, String checkDate) {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("任务跟踪表填写提醒");
        notificationDto.setContent(String.format("您在上周五（%s）未填写或更新任务跟踪表，请及时登录系统更新任务进度。", checkDate));
        notificationDto.setType(Notification.NotificationType.SYSTEM_ALERT);
        notificationDto.setPriority(Notification.NotificationPriority.HIGH);
        notificationDto.setRecipientId(user.getId());
        notificationDto.setActionUrl("/tasks");
        
        notificationService.createNotification(notificationDto);
    }

    /**
     * 手动触发检查（供管理界面调用）
     */
    public void manualCheck() {
        log.info("手动触发任务跟踪表填写检查...");
        checkFridayTaskTracking();
    }

    /**
     * 更新定时任务执行记录
     */
    private void updateScheduledTaskExecution(String taskName, LocalDateTime executeTime, String result, String errorMessage) {
        try {
            ScheduledTask task = scheduledTaskRepository.findByTaskName(taskName)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskName));
            
            task.setLastExecuteTime(executeTime);
            task.setLastExecuteResult(result);
            
            // 自动执行时更新下次执行时间
            LocalDateTime nextExecuteTime = calculateNextExecuteTime(task.getCronExpression());
            task.setNextExecuteTime(nextExecuteTime);
            
            scheduledTaskRepository.save(task);
            
            log.info("定时任务 {} 执行记录已更新: 时间={}, 结果={}, 下次执行时间={}", taskName, executeTime, result, nextExecuteTime);
        } catch (Exception e) {
            log.error("更新定时任务执行记录失败: taskName={}, error={}", taskName, e.getMessage());
        }
    }

    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextExecuteTime(String cronExpression) {
        try {
            CronTrigger trigger = new CronTrigger(cronExpression);
            Date now = new Date();
            Date nextExecution = trigger.nextExecutionTime(new SimpleTriggerContext(now, now, now));
            if (nextExecution != null) {
                return LocalDateTime.ofInstant(nextExecution.toInstant(), ZoneId.of("Asia/Shanghai"));
            }
        } catch (Exception e) {
            log.error("解析cron表达式失败: {}, error: {}", cronExpression, e.getMessage());
        }
        return null;
    }
}
