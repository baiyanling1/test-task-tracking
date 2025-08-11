package com.testtracking.dto;

import com.testtracking.entity.Notification;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class NotificationDto {

    private Long id;

    @NotBlank(message = "通知标题不能为空")
    private String title;

    private String content;

    @NotNull(message = "通知类型不能为空")
    private Notification.NotificationType type;

    private Notification.NotificationPriority priority;

    private Boolean isRead;

    private LocalDateTime readTime;

    private Long recipientId;
    private String recipientName;

    private Long relatedTaskId;
    private String relatedTaskName;

    private String relatedEntityType;

    private Long relatedEntityId;

    private String actionUrl;

    private LocalDateTime expireTime;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    // 从实体转换为DTO
    public static NotificationDto fromEntity(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setType(notification.getType());
        dto.setPriority(notification.getPriority());
        dto.setIsRead(notification.getIsRead());
        dto.setReadTime(notification.getReadTime());
        
        if (notification.getRecipient() != null) {
            dto.setRecipientId(notification.getRecipient().getId());
            dto.setRecipientName(notification.getRecipient().getRealName());
        }
        
        if (notification.getRelatedTask() != null) {
            dto.setRelatedTaskId(notification.getRelatedTask().getId());
            dto.setRelatedTaskName(notification.getRelatedTask().getTaskName());
        }
        
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setActionUrl(notification.getActionUrl());
        dto.setExpireTime(notification.getExpireTime());
        dto.setCreatedTime(notification.getCreatedTime());
        dto.setUpdatedTime(notification.getUpdatedTime());
        
        return dto;
    }

    // 从DTO转换为实体
    public Notification toEntity() {
        Notification notification = new Notification();
        notification.setId(this.id);
        notification.setTitle(this.title);
        notification.setContent(this.content);
        notification.setType(this.type);
        notification.setPriority(this.priority);
        notification.setIsRead(this.isRead);
        notification.setReadTime(this.readTime);
        notification.setRelatedEntityType(this.relatedEntityType);
        notification.setRelatedEntityId(this.relatedEntityId);
        notification.setActionUrl(this.actionUrl);
        notification.setExpireTime(this.expireTime);
        notification.setCreatedTime(this.createdTime);
        notification.setUpdatedTime(this.updatedTime);
        
        return notification;
    }
} 