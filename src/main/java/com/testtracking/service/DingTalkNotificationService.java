package com.testtracking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DingTalkNotificationService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${dingtalk.webhook.url:}")
    private String webhookUrl;

    @Value("${dingtalk.secret:}")
    private String secret;

    @Value("${dingtalk.enabled:false}")
    private boolean enabled;

    /**
     * 发送文本消息
     */
    public void sendTextMessage(String content, List<String> atMobiles, boolean atAll) {
        if (!enabled || webhookUrl.isEmpty()) {
            log.warn("钉钉通知未启用或webhook地址未配置");
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("msgtype", "text");

            Map<String, String> text = new HashMap<>();
            text.put("content", content);
            message.put("text", text);

            Map<String, Object> at = new HashMap<>();
            at.put("atMobiles", atMobiles);
            at.put("isAtAll", atAll);
            message.put("at", at);

            sendMessage(message);
        } catch (Exception e) {
            log.error("发送钉钉文本消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送markdown消息
     */
    public void sendMarkdownMessage(String title, String content, List<String> atMobiles, boolean atAll) {
        if (!enabled || webhookUrl.isEmpty()) {
            log.warn("钉钉通知未启用或webhook地址未配置");
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("msgtype", "markdown");

            Map<String, String> markdown = new HashMap<>();
            markdown.put("title", title);
            markdown.put("text", content);
            message.put("markdown", markdown);

            Map<String, Object> at = new HashMap<>();
            at.put("atMobiles", atMobiles);
            at.put("isAtAll", atAll);
            message.put("at", at);

            sendMessage(message);
        } catch (Exception e) {
            log.error("发送钉钉markdown消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送任务超时通知
     */
    public void sendTaskOverdueNotification(String taskName, String assignedTo, int overdueDays, String projectName) {
        String title = "🚨 任务超时提醒";
        String content = String.format(
                "### %s\n" +
                "**任务名称**: %s\n" +
                "**负责人**: %s\n" +
                "**项目**: %s\n" +
                "**超时天数**: %d天\n" +
                "**时间**: %s\n" +
                "\n请及时处理！",
                title, taskName, assignedTo, projectName, overdueDays,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        sendMarkdownMessage(title, content, null, false);
    }

    /**
     * 发送任务完成通知
     */
    public void sendTaskCompletedNotification(String taskName, String completedBy, String projectName) {
        String title = "✅ 任务完成通知";
        String content = String.format(
                "### %s\n" +
                "**任务名称**: %s\n" +
                "**完成人**: %s\n" +
                "**项目**: %s\n" +
                "**完成时间**: %s\n" +
                "\n恭喜完成任务！",
                title, taskName, completedBy, projectName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        sendMarkdownMessage(title, content, null, false);
    }

    /**
     * 发送风险告警通知
     */
    public void sendRiskAlertNotification(String taskName, String riskLevel, String riskDescription, String assignedTo) {
        String title = "⚠️ 风险告警";
        String content = String.format(
                "### %s\n" +
                "**任务名称**: %s\n" +
                "**风险等级**: %s\n" +
                "**负责人**: %s\n" +
                "**风险描述**: %s\n" +
                "**告警时间**: %s\n" +
                "\n请及时评估和处理风险！",
                title, taskName, riskLevel, assignedTo, riskDescription,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        sendMarkdownMessage(title, content, null, false);
    }

    /**
     * 发送系统告警通知
     */
    public void sendSystemAlertNotification(String alertType, String message, String details) {
        String title = "🔔 系统告警";
        String content = String.format(
                "### %s\n" +
                "**告警类型**: %s\n" +
                "**告警信息**: %s\n" +
                "**详细信息**: %s\n" +
                "**告警时间**: %s\n" +
                "\n请系统管理员及时处理！",
                title, alertType, message, details,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        sendMarkdownMessage(title, content, null, true);
    }

    /**
     * 发送任务分配通知
     */
    public void sendTaskAssignedNotification(String taskName, String assignedTo, String assignedBy, String projectName) {
        String title = "📋 任务分配通知";
        String content = String.format(
                "### %s\n" +
                "**任务名称**: %s\n" +
                "**负责人**: %s\n" +
                "**分配人**: %s\n" +
                "**项目**: %s\n" +
                "**分配时间**: %s\n" +
                "\n请及时查看和处理任务！",
                title, taskName, assignedTo, assignedBy, projectName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        sendMarkdownMessage(title, content, null, false);
    }

    /**
     * 发送进度更新通知
     */
    public void sendProgressUpdateNotification(String taskName, int progressPercentage, String updatedBy, String projectName) {
        String title = "📈 进度更新通知";
        String content = String.format(
                "### %s\n" +
                "**任务名称**: %s\n" +
                "**当前进度**: %d%%\n" +
                "**更新人**: %s\n" +
                "**项目**: %s\n" +
                "**更新时间**: %s\n" +
                "\n任务进度已更新！",
                title, taskName, progressPercentage, updatedBy, projectName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        sendMarkdownMessage(title, content, null, false);
    }

    /**
     * 发送消息到钉钉
     */
    private void sendMessage(Map<String, Object> message) {
        String url = buildSignedUrl();
        
        webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(message)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> log.info("钉钉消息发送成功: {}", response),
                        error -> log.error("钉钉消息发送失败: {}", error.getMessage())
                );
    }

    /**
     * 构建带签名的URL
     */
    private String buildSignedUrl() {
        if (secret.isEmpty()) {
            return webhookUrl;
        }

        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(stringToSign.getBytes());
            String signature = java.util.Base64.getEncoder().encodeToString(hash);
            
            return webhookUrl + "&timestamp=" + timestamp + "&sign=" + signature;
        } catch (Exception e) {
            log.error("构建钉钉签名URL失败: {}", e.getMessage());
            return webhookUrl;
        }
    }
} 