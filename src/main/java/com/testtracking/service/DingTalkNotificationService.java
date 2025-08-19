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
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DingTalkNotificationService {

    private final RestTemplate restTemplate;
    private final DingTalkConfigService configService;
    private final AlertNotificationConfigService alertNotificationConfigService;

    // 移除@Value注解，改为动态读取配置
    private boolean dingtalkEnabled;
    private String webhookUrl;
    private String secret;

    /**
     * 刷新配置
     */
    private void refreshConfig() {
        try {
            DingTalkConfigService.DingTalkConfig config = configService.loadConfig();
            this.dingtalkEnabled = config.isEnabled();
            this.webhookUrl = config.getWebhookUrl();
            this.secret = config.getSecret();
            log.debug("钉钉配置已刷新: enabled={}, webhookUrl={}", dingtalkEnabled, webhookUrl);
        } catch (Exception e) {
            log.error("刷新钉钉配置失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送通知到钉钉
     */
    public void sendNotificationToDingTalk(Notification notification) {
        // 每次发送前刷新配置
        refreshConfig();
        
        log.info("开始发送钉钉通知，配置状态: enabled={}, webhookUrl={}", dingtalkEnabled, webhookUrl);
        
        if (!dingtalkEnabled || webhookUrl.isEmpty()) {
            log.warn("钉钉通知未启用或未配置webhook地址，跳过发送");
            return;
        }

        // 检查该告警类型是否启用钉钉通知
        String alertType = getAlertTypeFromNotification(notification);
        if (!alertNotificationConfigService.isDingTalkEnabled(alertType)) {
            log.info("告警类型 {} 未启用钉钉通知，跳过发送", alertType);
            return;
        }

        try {
            log.info("构建钉钉消息内容，通知标题: {}", notification.getTitle());
            String message = buildDingTalkMessage(notification);
            log.info("钉钉消息内容构建完成，长度: {} 字符", message.length());
            
            sendMessage(message);
            log.info("成功发送通知到钉钉: {}", notification.getTitle());
        } catch (Exception e) {
            log.error("发送通知到钉钉失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据通知内容判断告警类型
     */
    private String getAlertTypeFromNotification(Notification notification) {
        String title = notification.getTitle();
        String content = notification.getContent();
        
        // 根据通知标题和内容判断告警类型
        if (title != null && title.contains("任务跟踪表填写提醒")) {
            return "TASK_TRACKING_REMINDER";
        } else if (title != null && title.contains("任务分配")) {
            return "TASK_ASSIGNMENT";
        } else if (title != null && (title.contains("超时") || title.contains("逾期"))) {
            return "TASK_OVERDUE";
        } else if (title != null && title.contains("完成")) {
            return "TASK_COMPLETION";
        } else if (title != null && title.contains("系统维护")) {
            return "SYSTEM_MAINTENANCE";
        } else {
            // 默认返回任务分配类型
            return "TASK_ASSIGNMENT";
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
        try {
            log.info("准备发送钉钉消息到webhook地址: {}", webhookUrl);
            
            // 构建带签名的URL
            String signedUrl = buildSignedUrl();
            log.info("使用签名URL发送消息: {}", signedUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            log.info("设置HTTP请求头: Content-Type={}", MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("msgtype", "markdown");

            Map<String, String> markdown = new HashMap<>();
            markdown.put("title", "任务通知");
            markdown.put("text", message);
            requestBody.put("markdown", markdown);

            log.info("构建钉钉请求体: msgtype={}, title={}, text长度={}", 
                requestBody.get("msgtype"), markdown.get("title"), markdown.get("text").length());
            log.debug("钉钉请求体详细内容: {}", requestBody);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            log.info("开始发送HTTP POST请求到钉钉webhook...");
            
            var response = restTemplate.postForEntity(signedUrl, request, String.class);
            log.info("钉钉webhook响应: HTTP状态码={}, 响应体长度={}", 
                response.getStatusCode(), response.getBody() != null ? response.getBody().length() : 0);
            log.debug("钉钉响应体详细内容: {}", response.getBody());
            
            // 检查钉钉返回的错误信息
            if (response.getBody() != null && response.getBody().contains("\"errcode\":0")) {
                log.info("钉钉消息发送成功，errcode=0");
            } else {
                log.warn("钉钉可能返回了错误信息，请检查响应体: {}", response.getBody());
            }
            
        } catch (Exception e) {
            log.error("发送钉钉消息失败，webhook地址: {}, 错误信息: {}", webhookUrl, e.getMessage(), e);
            throw new RuntimeException("发送钉钉消息失败: " + e.getMessage());
        }
    }

    /**
     * 构建带签名的URL
     */
    private String buildSignedUrl() {
        if (secret == null || secret.trim().isEmpty()) {
            log.info("未配置钉钉签名密钥，使用原始webhook地址");
            return webhookUrl;
        }
        
        try {
            long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + secret;
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(java.util.Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);
            
            String signedUrl = webhookUrl + "&timestamp=" + timestamp + "&sign=" + sign;
            log.info("构建钉钉签名URL: timestamp={}, sign={}", timestamp, sign);
            
            return signedUrl;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("构建钉钉签名URL失败: {}", e.getMessage(), e);
            log.warn("使用原始webhook地址发送消息");
            return webhookUrl;
        }
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

    /**
     * 发送测试通知
     */
    public String sendTestNotification() {
        // 刷新配置
        refreshConfig();
        
        log.info("开始发送钉钉测试通知，配置状态: enabled={}, webhookUrl={}", dingtalkEnabled, webhookUrl);
        
        if (!dingtalkEnabled) {
            throw new RuntimeException("钉钉通知未启用，请在前端配置页面启用钉钉通知");
        }
        
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            throw new RuntimeException("钉钉webhook地址未正确配置，请在前端配置页面设置正确的webhook地址");
        }

        try {
            String testMessage = "## 🧪 钉钉配置测试\n\n" +
                "**测试时间:** " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n" +
                "**配置状态:** ✅ 配置正确\n\n" +
                "**Webhook地址:** " + webhookUrl + "\n\n" +
                "---\n" +
                "*此消息用于测试钉钉通知配置*";
            
            log.info("准备发送测试消息: {}", testMessage);
            sendMessage(testMessage);
            log.info("钉钉测试消息发送成功");
            return "测试消息发送成功";
        } catch (Exception e) {
            log.error("发送测试通知失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送测试通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取webhook地址
     */
    public String getWebhookUrl() {
        refreshConfig();
        return webhookUrl;
    }

    /**
     * 获取密钥
     */
    public String getSecret() {
        refreshConfig();
        return secret;
    }

    /**
     * 检查是否已配置
     */
    public boolean isConfigured() {
        refreshConfig();
        return dingtalkEnabled && !webhookUrl.isEmpty();
    }
} 