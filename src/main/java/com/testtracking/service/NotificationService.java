package com.testtracking.service;

import com.testtracking.dto.NotificationDto;
import com.testtracking.entity.Notification;
import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import com.testtracking.repository.NotificationRepository;
import com.testtracking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DingTalkNotificationService dingTalkNotificationService;

    /**
     * 创建系统内部通知
     */
    public NotificationDto createNotification(NotificationDto notificationDto) {
        log.info("创建通知: {}", notificationDto.getTitle());
        
        Notification notification = new Notification();
        notification.setTitle(notificationDto.getTitle());
        notification.setContent(notificationDto.getContent());
        notification.setType(notificationDto.getType());
        notification.setPriority(notificationDto.getPriority());
        
        if (notificationDto.getRecipientId() != null) {
            User recipient = userRepository.findById(notificationDto.getRecipientId())
                    .orElseThrow(() -> new RuntimeException("接收用户不存在"));
            notification.setRecipient(recipient);
        }
        
        if (notificationDto.getRelatedTaskId() != null) {
            // 这里需要注入TestTaskRepository来获取任务
            // 暂时跳过，实际使用时需要完善
        }
        
        notification.setRelatedEntityType(notificationDto.getRelatedEntityType());
        notification.setRelatedEntityId(notificationDto.getRelatedEntityId());
        notification.setActionUrl(notificationDto.getActionUrl());
        notification.setExpireTime(notificationDto.getExpireTime());
        
        Notification savedNotification = notificationRepository.save(notification);
        return NotificationDto.fromEntity(savedNotification);
    }

    /**
     * 发送任务超时通知
     */
    public void sendTaskOverdueNotification(TestTask task) {
        if (task.getAssignedTo() == null) {
            return;
        }
        
        // 创建系统内部通知
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("任务超时提醒");
        notificationDto.setContent(String.format("任务「%s」已超时%d天，请及时处理", 
                task.getTaskName(), task.getOverdueDays()));
        notificationDto.setType(Notification.NotificationType.TASK_OVERDUE);
        notificationDto.setPriority(Notification.NotificationPriority.HIGH);
        notificationDto.setRecipientId(task.getAssignedTo().getId());
        notificationDto.setRelatedTaskId(task.getId());
        notificationDto.setRelatedEntityType("TestTask");
        notificationDto.setRelatedEntityId(task.getId());
        notificationDto.setActionUrl("/tasks/" + task.getId());
        
        createNotification(notificationDto);
        
        // 发送钉钉通知
        try {
            Notification savedNotification = notificationRepository.save(notificationDto.toEntity());
            dingTalkNotificationService.sendNotificationToDingTalk(savedNotification);
        } catch (Exception e) {
            log.error("发送钉钉通知失败: {}", e.getMessage());
        }
    }

    /**
     * 发送任务完成通知
     */
    public void sendTaskCompletedNotification(TestTask task, String completedBy) {
        // 创建系统内部通知
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("任务完成通知");
        notificationDto.setContent(String.format("任务「%s」已完成", task.getTaskName()));
        notificationDto.setType(Notification.NotificationType.TASK_COMPLETED);
        notificationDto.setPriority(Notification.NotificationPriority.NORMAL);
        notificationDto.setRecipientId(task.getAssignedTo().getId());
        notificationDto.setRelatedTaskId(task.getId());
        notificationDto.setRelatedEntityType("TestTask");
        notificationDto.setRelatedEntityId(task.getId());
        notificationDto.setActionUrl("/tasks/" + task.getId());
        
        createNotification(notificationDto);
        
        // 发送钉钉通知
        try {
            Notification savedNotification = notificationRepository.save(notificationDto.toEntity());
            dingTalkNotificationService.sendNotificationToDingTalk(savedNotification);
        } catch (Exception e) {
            log.error("发送钉钉通知失败: {}", e.getMessage());
        }
    }

    /**
     * 发送任务分配通知
     */
    public void sendTaskAssignedNotification(TestTask task, String assignedBy) {
        if (task.getAssignedTo() == null) {
            return;
        }
        
        // 创建系统内部通知
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("任务分配通知");
        notificationDto.setContent(String.format("您被分配了新任务「%s」", task.getTaskName()));
        notificationDto.setType(Notification.NotificationType.TASK_ASSIGNED);
        notificationDto.setPriority(Notification.NotificationPriority.NORMAL);
        notificationDto.setRecipientId(task.getAssignedTo().getId());
        notificationDto.setRelatedTaskId(task.getId());
        notificationDto.setRelatedEntityType("TestTask");
        notificationDto.setRelatedEntityId(task.getId());
        notificationDto.setActionUrl("/tasks/" + task.getId());
        
        createNotification(notificationDto);
        
        // 发送钉钉通知
        try {
            Notification savedNotification = notificationRepository.save(notificationDto.toEntity());
            dingTalkNotificationService.sendNotificationToDingTalk(savedNotification);
        } catch (Exception e) {
            log.error("发送钉钉通知失败: {}", e.getMessage());
        }
    }

    /**
     * 发送进度更新通知
     */
    public void sendProgressUpdateNotification(TestTask task, int progressPercentage, String updatedBy) {
        if (task.getAssignedTo() == null) {
            return;
        }
        
        // 创建系统内部通知
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("进度更新通知");
        notificationDto.setContent(String.format("任务「%s」进度已更新为%d%%", 
                task.getTaskName(), progressPercentage));
        notificationDto.setType(Notification.NotificationType.TASK_PROGRESS_UPDATE);
        notificationDto.setPriority(Notification.NotificationPriority.LOW);
        notificationDto.setRecipientId(task.getAssignedTo().getId());
        notificationDto.setRelatedTaskId(task.getId());
        notificationDto.setRelatedEntityType("TestTask");
        notificationDto.setRelatedEntityId(task.getId());
        notificationDto.setActionUrl("/tasks/" + task.getId());
        
        createNotification(notificationDto);
        
        // 发送钉钉通知
        try {
            Notification savedNotification = notificationRepository.save(notificationDto.toEntity());
            dingTalkNotificationService.sendNotificationToDingTalk(savedNotification);
        } catch (Exception e) {
            log.error("发送钉钉通知失败: {}", e.getMessage());
        }
    }

    /**
     * 发送风险告警通知
     */
    public void sendRiskAlertNotification(TestTask task) {
        if (task.getAssignedTo() == null) {
            return;
        }
        
        // 创建系统内部通知
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("风险告警");
        notificationDto.setContent(String.format("任务「%s」存在%s风险", 
                task.getTaskName(), task.getRiskLevel().getDescription()));
        notificationDto.setType(Notification.NotificationType.RISK_ALERT);
        notificationDto.setPriority(Notification.NotificationPriority.HIGH);
        notificationDto.setRecipientId(task.getAssignedTo().getId());
        notificationDto.setRelatedTaskId(task.getId());
        notificationDto.setRelatedEntityType("TestTask");
        notificationDto.setRelatedEntityId(task.getId());
        notificationDto.setActionUrl("/tasks/" + task.getId());
        
        createNotification(notificationDto);
        
        // 发送钉钉通知
        try {
            Notification savedNotification = notificationRepository.save(notificationDto.toEntity());
            dingTalkNotificationService.sendNotificationToDingTalk(savedNotification);
        } catch (Exception e) {
            log.error("发送钉钉通知失败: {}", e.getMessage());
        }
    }

    /**
     * 发送系统告警通知
     */
    public void sendSystemAlertNotification(String alertType, String message, String details) {
        // 创建系统内部通知
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setTitle("系统告警");
        notificationDto.setContent(String.format("告警类型: %s\n告警信息: %s\n详细信息: %s", alertType, message, details));
        notificationDto.setType(Notification.NotificationType.SYSTEM_ALERT);
        notificationDto.setPriority(Notification.NotificationPriority.HIGH);
        notificationDto.setRelatedEntityType("System");
        notificationDto.setActionUrl("/alerts");
        
        createNotification(notificationDto);
        
        // 发送钉钉通知
        try {
            Notification savedNotification = notificationRepository.save(notificationDto.toEntity());
            dingTalkNotificationService.sendNotificationToDingTalk(savedNotification);
        } catch (Exception e) {
            log.error("发送钉钉通知失败: {}", e.getMessage());
        }
    }

    /**
     * 获取用户的通知
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Page<Notification> notifications = notificationRepository.findByRecipient(user, pageable);
        return notifications.map(NotificationDto::fromEntity);
    }

    /**
     * 获取用户未读通知数量
     */
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return notificationRepository.countUnreadByRecipient(user);
    }

    /**
     * 标记通知为已读
     */
    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * 标记用户所有通知为已读
     */
    public void markAllNotificationsAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        notificationRepository.markAllAsReadByRecipient(user, LocalDateTime.now());
    }

    /**
     * 删除过期通知（定时任务）
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void deleteExpiredNotifications() {
        log.info("开始清理过期通知");
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications(LocalDateTime.now());
        
        if (!expiredNotifications.isEmpty()) {
            notificationRepository.deleteAll(expiredNotifications);
            log.info("清理了{}条过期通知", expiredNotifications.size());
        }
    }

    /**
     * 检查超时任务并发送通知（定时任务）
     */
    @Scheduled(cron = "0 0 9 * * ?") // 每天上午9点执行
    public void checkOverdueTasksAndNotify() {
        log.info("开始检查超时任务");
        // 这里需要注入TestTaskService来获取超时任务
        // 实际使用时需要完善
    }

    /**
     * 删除通知
     */
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        
        notificationRepository.delete(notification);
        log.info("删除通知: notificationId={}", notificationId);
    }

    /**
     * 批量删除通知
     */
    public void deleteNotifications(List<Long> notificationIds) {
        notificationRepository.deleteAllById(notificationIds);
        log.info("批量删除通知: notificationIds={}", notificationIds);
    }

    /**
     * 根据状态获取通知
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByStatus(Long userId, String status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        boolean isRead = "READ".equalsIgnoreCase(status);
        Page<Notification> notifications = notificationRepository.findByRecipientAndIsRead(user, isRead, pageable);
        return notifications.map(NotificationDto::fromEntity);
    }

    /**
     * 根据优先级获取通知
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByPriority(Long userId, String priority, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Notification.NotificationPriority notificationPriority = Notification.NotificationPriority.valueOf(priority.toUpperCase());
        Page<Notification> notifications = notificationRepository.findByRecipientAndPriority(user, notificationPriority, pageable);
        return notifications.map(NotificationDto::fromEntity);
    }

    /**
     * 搜索通知
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> searchNotifications(Long userId, String keyword, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Page<Notification> notifications = notificationRepository.findByRecipientAndTitleContainingOrContentContaining(user, keyword, keyword, pageable);
        return notifications.map(NotificationDto::fromEntity);
    }

    /**
     * 获取超时任务通知
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getOverdueTaskNotifications(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Page<Notification> notifications = notificationRepository.findByRecipientAndType(user, Notification.NotificationType.TASK_OVERDUE, pageable);
        return notifications.map(NotificationDto::fromEntity);
    }
} 