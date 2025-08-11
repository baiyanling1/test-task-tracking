package com.testtracking.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
@EqualsAndHashCode(callSuper = false)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "通知标题不能为空")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "通知类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_time")
    private LocalDateTime readTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_task_id")
    private TestTask relatedTask;

    @Column(name = "related_entity_type")
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    public enum NotificationType {
        TASK_OVERDUE("任务超时"),
        TASK_COMPLETED("任务完成"),
        TASK_ASSIGNED("任务分配"),
        TASK_PROGRESS_UPDATE("进度更新"),
        RISK_ALERT("风险告警"),
        SYSTEM_ALERT("系统告警"),
        DINGTALK_NOTIFICATION("钉钉通知");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum NotificationPriority {
        LOW("低"),
        NORMAL("普通"),
        HIGH("高"),
        URGENT("紧急");

        private final String description;

        NotificationPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 标记为已读
    public void markAsRead() {
        this.isRead = true;
        this.readTime = LocalDateTime.now();
    }

    // 检查是否过期
    public boolean isExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }
} 