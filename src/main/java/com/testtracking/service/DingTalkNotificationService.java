package com.testtracking.service;

import com.testtracking.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DingTalkNotificationService {

    private final RestTemplate restTemplate;

    @Value("${dingtalk.enabled:false}")
    private boolean dingtalkEnabled;

    @Value("${dingtalk.webhook.url:}")
    private String webhookUrl;

    @Value("${dingtalk.secret:}")
    private String secret;

    /**
     * 发送通知到钉钉
     */
    public void sendNotificationToDingTalk(Notification notification) {
        if (!dingtalkEnabled || webhookUrl.isEmpty()) {
            log.info("钉钉通知未启用或未配置webhook地址");
            return;
        }

        try {
            String message = buildDingTalkMessage(notification);
            sendMessage(message);
            log.info("成功发送通知到钉钉: {}", notification.getTitle());
        } catch (Exception e) {
            log.error("发送通知到钉钉失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 构建钉钉消息
     */
    private String buildDingTalkMessage(Notification notification) {
        StringBuilder message = new StringBuilder();
        message.append("## 🚨 任务通知\n\n");
        message.append("**通知标题:** ").append(notification.getTitle()).append("\n\n");
        message.append("**通知内容:** ").append(notification.getContent()).append("\n\n");
        message.append("**通知类型:** ").append(getTypeEmoji(notification.getType())).append(notification.getType().getDescription()).append("\n\n");
        message.append("**优先级:** ").append(getPriorityEmoji(notification.getPriority())).append(notification.getPriority().getDescription()).append("\n\n");
        
        if (notification.getRelatedTask() != null) {
            message.append("**任务名称:** ").append(notification.getRelatedTask().getTaskName()).append("\n\n");
            message.append("**负责人:** ").append(notification.getRelatedTask().getAssignedTo() != null ? 
                notification.getRelatedTask().getAssignedTo().getRealName() : "未分配").append("\n\n");
        }
        
        message.append("**通知时间:** ").append(notification.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        message.append("---\n");
        message.append("*此消息由测试任务跟踪系统自动发送*");
        
        return message.toString();
    }

    /**
     * 获取类型对应的emoji
     */
    private String getTypeEmoji(Notification.NotificationType type) {
        switch (type) {
            case TASK_OVERDUE:
                return "🚨 ";
            case TASK_COMPLETED:
                return "✅ ";
            case TASK_ASSIGNED:
                return "📋 ";
            case TASK_PROGRESS_UPDATE:
                return "📈 ";
            case RISK_ALERT:
                return "⚠️ ";
            case SYSTEM_ALERT:
                return "🔔 ";
            default:
                return "📢 ";
        }
    }

    /**
     * 获取优先级对应的emoji
     */
    private String getPriorityEmoji(Notification.NotificationPriority priority) {
        switch (priority) {
            case HIGH:
                return "🔴 ";
            case NORMAL:
                return "🟡 ";
            case LOW:
                return "🟢 ";
            default:
                return "⚪ ";
        }
    }

    /**
     * 发送消息到钉钉
     */
    private void sendMessage(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("msgtype", "markdown");

        Map<String, String> markdown = new HashMap<>();
        markdown.put("title", "任务通知");
        markdown.put("text", message);
        requestBody.put("markdown", markdown);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        restTemplate.postForEntity(webhookUrl, request, String.class);
    }

    /**
     * 测试钉钉连接
     */
    public boolean testConnection() {
        if (!dingtalkEnabled || webhookUrl.isEmpty()) {
            return false;
        }

        try {
            String testMessage = "## 测试消息\n\n这是一条测试消息，用于验证钉钉webhook配置是否正确。\n\n发送时间: " + 
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            sendMessage(testMessage);
            return true;
        } catch (Exception e) {
            log.error("钉钉连接测试失败: {}", e.getMessage());
            return false;
        }
    }
} 