package com.testtracking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testtracking.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeiShuNotificationService {

    private final RestTemplate restTemplate;
    private final FeiShuConfigService configService;
    private final AlertNotificationConfigService alertNotificationConfigService;
    private final ObjectMapper objectMapper;

    // 动态读取配置
    private boolean feishuEnabled;
    private String webhookUrl;
    private String secret;

    /**
     * 刷新配置
     */
    private void refreshConfig() {
        try {
            FeiShuConfigService.FeiShuConfig config = configService.loadConfig();
            this.feishuEnabled = config.isEnabled();
            this.webhookUrl = config.getWebhookUrl();
            this.secret = config.getSecret();
            log.debug("飞书配置已刷新: enabled={}, webhookUrl={}", feishuEnabled, webhookUrl);
        } catch (Exception e) {
            log.error("刷新飞书配置失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送通知到飞书
     */
    public void sendNotificationToFeiShu(Notification notification) {
        // 每次发送前刷新配置
        refreshConfig();
        
        log.info("开始发送飞书通知，配置状态: enabled={}, webhookUrl={}", feishuEnabled, webhookUrl);
        
        if (!feishuEnabled || webhookUrl.isEmpty()) {
            log.warn("飞书通知未启用或未配置webhook地址，跳过发送");
            return;
        }

        // 检查该告警类型是否启用飞书通知
        String alertType = getAlertTypeFromNotification(notification);
        if (!alertNotificationConfigService.isFeiShuEnabled(alertType)) {
            log.info("告警类型 {} 未启用飞书通知，跳过发送", alertType);
            return;
        }

        try {
            log.info("构建飞书消息内容，通知标题: {}", notification.getTitle());
            
            // 判断是工作流webhook还是机器人webhook
            boolean isWorkflowWebhook = webhookUrl.contains("/flow/api/trigger-webhook/");
            Map<String, Object> message;
            
            if (isWorkflowWebhook) {
                log.info("检测到工作流webhook，使用富文本post格式");
                message = buildWorkflowMessage(notification);
            } else {
                log.info("检测到机器人webhook，使用交互式卡片格式");
                message = buildFeiShuMessage(notification);
            }
            
            log.info("飞书消息内容构建完成");
            
            sendMessage(message);
            log.info("成功发送通知到飞书: {}", notification.getTitle());
        } catch (Exception e) {
            log.error("发送通知到飞书失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据通知内容判断告警类型
     */
    private String getAlertTypeFromNotification(Notification notification) {
        String title = notification.getTitle();
        
        // 根据通知标题判断告警类型
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
     * 构建飞书工作流消息（富文本格式）
     * 工作流webhook通常直接接收纯文本或简单的键值对
     */
    private Map<String, Object> buildWorkflowMessage(Notification notification) {
        Map<String, Object> message = new HashMap<>();
        
        // 构建纯文本内容
        StringBuilder textBuilder = new StringBuilder();
        
        // 添加标题
        textBuilder.append("📢 ").append(notification.getTitle()).append("\n\n");
        
        // 清理Markdown格式但保留换行
        String cleanContent = cleanMarkdownButKeepNewlines(notification.getContent());
        textBuilder.append(cleanContent);
        
        // 添加时间信息
        if (notification.getCreatedTime() != null) {
            textBuilder.append("\n\n⏰ 时间：")
                .append(notification.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        String fullText = textBuilder.toString();
        
        // 工作流webhook支持多种格式，我们提供3种常见格式
        // 格式1: 直接使用text字段（最简单）
        message.put("text", fullText);
        
        // 格式2: 使用title和content分开（推荐）
        message.put("title", notification.getTitle());
        message.put("content", cleanContent);
        
        // 格式3: 兼容msg_type格式
        message.put("msg_type", "text");
        Map<String, Object> contentObj = new HashMap<>();
        contentObj.put("text", fullText);
        message.put("content", contentObj);
        
        // 添加额外的元数据字段
        message.put("notification_type", notification.getType() != null ? notification.getType().name() : "SYSTEM_ALERT");
        message.put("priority", notification.getPriority() != null ? notification.getPriority().name() : "NORMAL");
        message.put("time", notification.getCreatedTime() != null ? 
            notification.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        
        // 打印完整的JSON格式（美化输出）
        try {
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
            log.info("构建飞书工作流消息（兼容多种格式）:\n{}", prettyJson);
        } catch (Exception e) {
            log.warn("JSON格式化失败: {}", e.getMessage());
            log.debug("构建工作流消息: {}", message);
        }
        
        return message;
    }

    /**
     * 清理Markdown格式但保留换行符（用于飞书文本消息）
     */
    private String cleanMarkdownButKeepNewlines(String content) {
        if (content == null) {
            return "";
        }
        
        // 移除Markdown标记，保留换行符
        String cleaned = content
            // 移除标题标记（## 、### 等）
            .replaceAll("#+\\s+", "")
            // 移除加粗标记 **text**
            .replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
            // 移除斜体标记 *text*
            .replaceAll("\\*([^*]+)\\*", "$1")
            // 移除分割线
            .replaceAll("---+\\n?", "")
            // 移除多余的空行（3个以上换行符压缩为2个）
            .replaceAll("\\n{3,}", "\n\n")
            // 移除行首行尾空白
            .trim();
        
        return cleaned;
    }

    /**
     * 构建飞书消息（富文本格式）
     */
    private Map<String, Object> buildFeiShuMessage(Notification notification) {
        Map<String, Object> message = new HashMap<>();
        message.put("msg_type", "interactive");
        
        // 构建卡片内容
        Map<String, Object> card = new HashMap<>();
        
        // 设置标题
        Map<String, Object> header = new HashMap<>();
        Map<String, Object> title = new HashMap<>();
        title.put("tag", "plain_text");
        title.put("content", "🚨 " + notification.getTitle());
        header.put("title", title);
        header.put("template", getPriorityColor(notification.getPriority()));
        card.put("header", header);
        
        // 设置内容元素
        java.util.List<Map<String, Object>> elements = new java.util.ArrayList<>();
        
        // 如果是任务跟踪提醒或其他包含长文本的通知，使用Markdown显示完整内容
        if (notification.getContent() != null && notification.getContent().length() > 100) {
            // 添加Markdown格式的内容块
            Map<String, Object> contentElement = new HashMap<>();
            contentElement.put("tag", "markdown");
            contentElement.put("content", notification.getContent());
            elements.add(contentElement);
        } else {
            // 短消息使用字段形式显示
            // 添加通知标题
            elements.add(createFieldElement("通知标题", notification.getTitle()));
            
            // 添加通知内容
            elements.add(createFieldElement("通知内容", notification.getContent()));
            
            // 添加通知类型
            elements.add(createFieldElement("通知类型", 
                getTypeEmoji(notification.getType()) + notification.getType().getDescription()));
            
            // 添加优先级
            elements.add(createFieldElement("优先级", 
                getPriorityEmoji(notification.getPriority()) + notification.getPriority().getDescription()));
            
            // 添加任务信息
            if (notification.getRelatedTask() != null) {
                elements.add(createFieldElement("任务名称", notification.getRelatedTask().getTaskName()));
                elements.add(createFieldElement("负责人", 
                    notification.getRelatedTask().getAssignedTo() != null ? 
                        notification.getRelatedTask().getAssignedTo().getRealName() : "未分配"));
            }
            
            // 添加通知时间
            elements.add(createFieldElement("通知时间", 
                notification.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        
        // 添加分割线
        Map<String, Object> divider = new HashMap<>();
        divider.put("tag", "hr");
        elements.add(divider);
        
        // 添加按钮（如果有相关任务，显示查看任务按钮）
        Map<String, Object> actions = new HashMap<>();
        actions.put("tag", "action");
        
        java.util.List<Map<String, Object>> actionButtons = new java.util.ArrayList<>();
        
        // 添加"查看任务跟踪平台"按钮
        Map<String, Object> viewButton = new HashMap<>();
        viewButton.put("tag", "button");
        viewButton.put("text", createButtonText("查看任务跟踪平台"));
        viewButton.put("type", "primary");
        viewButton.put("url", getSystemUrl() + "/tasks"); // 跳转到任务列表页面
        actionButtons.add(viewButton);
        
        // 如果有关联任务，添加"查看任务详情"按钮
        if (notification.getRelatedTask() != null) {
            Map<String, Object> taskButton = new HashMap<>();
            taskButton.put("tag", "button");
            taskButton.put("text", createButtonText("查看任务详情"));
            taskButton.put("type", "default");
            taskButton.put("url", getSystemUrl() + "/tasks?id=" + notification.getRelatedTask().getId());
            actionButtons.add(taskButton);
        }
        
        actions.put("actions", actionButtons);
        elements.add(actions);
        
        // 添加备注
        Map<String, Object> note = new HashMap<>();
        note.put("tag", "note");
        Map<String, Object> noteContent = new HashMap<>();
        noteContent.put("tag", "plain_text");
        noteContent.put("content", "此消息由测试任务跟踪系统自动发送");
        note.put("elements", java.util.Arrays.asList(noteContent));
        elements.add(note);
        
        card.put("elements", elements);
        message.put("card", card);
        
        // 打印完整的JSON格式（美化输出）
        try {
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
            log.info("构建飞书机器人消息（交互式卡片格式）:\n{}", prettyJson);
        } catch (Exception e) {
            log.warn("JSON格式化失败: {}", e.getMessage());
            log.debug("构建机器人消息: {}", message);
        }
        
        return message;
    }

    /**
     * 创建按钮文本
     */
    private Map<String, Object> createButtonText(String text) {
        Map<String, Object> buttonText = new HashMap<>();
        buttonText.put("tag", "plain_text");
        buttonText.put("content", text);
        return buttonText;
    }

    /**
     * 获取系统访问地址
     */
    private String getSystemUrl() {
        // TODO: 从配置文件读取系统地址，这里先使用默认值
        // 您可以在 application.yml 中添加配置项：system.base-url
        return "http://10.18.50.48:3000"; // 替换为您的实际系统地址
    }

    /**
     * 创建字段元素
     */
    private Map<String, Object> createFieldElement(String name, String value) {
        Map<String, Object> element = new HashMap<>();
        element.put("tag", "div");
        
        Map<String, Object> field = new HashMap<>();
        field.put("is_short", true);
        
        Map<String, Object> text = new HashMap<>();
        text.put("tag", "lark_md");
        text.put("content", "**" + name + "：**\n" + value);
        field.put("text", text);
        
        element.put("fields", java.util.Arrays.asList(field));
        
        return element;
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
     * 获取优先级对应的卡片颜色
     */
    private String getPriorityColor(Notification.NotificationPriority priority) {
        switch (priority) {
            case HIGH:
                return "red";
            case NORMAL:
                return "yellow";
            case LOW:
                return "green";
            default:
                return "blue";
        }
    }

    /**
     * 发送消息到飞书
     */
    private void sendMessage(Map<String, Object> message) {
        try {
            log.info("准备发送飞书消息到webhook地址: {}", webhookUrl);
            
            // 工作流webhook不需要签名，只有机器人webhook需要
            boolean isWorkflowWebhook = webhookUrl.contains("/flow/api/trigger-webhook/");
            
            if (!isWorkflowWebhook && secret != null && !secret.trim().isEmpty()) {
                log.info("机器人webhook，添加签名");
                addSignature(message);
            } else if (isWorkflowWebhook) {
                log.info("工作流webhook，跳过签名");
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            log.info("设置HTTP请求头: Content-Type={}", MediaType.APPLICATION_JSON);

            log.debug("飞书请求体详细内容: {}", message);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(message, headers);
            log.info("开始发送HTTP POST请求到飞书webhook...");
            
            var response = restTemplate.postForEntity(webhookUrl, request, String.class);
            String responseBody = response.getBody();
            log.info("飞书webhook响应: HTTP状态码={}, 响应体长度={}", 
                response.getStatusCode(), responseBody != null ? responseBody.length() : 0);
            log.debug("飞书响应体详细内容: {}", responseBody);
            
            // 解析飞书响应，检查 code 字段（0 表示成功，非 0 表示失败）
            if (responseBody != null && !responseBody.isBlank()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> json = objectMapper.readValue(responseBody, Map.class);
                    Object codeObj = json.get("code");
                    int code = codeObj instanceof Number ? ((Number) codeObj).intValue() : -1;
                    String msg = json.containsKey("msg") ? String.valueOf(json.get("msg")) : "";
                    if (code != 0) {
                        String errorDesc = (code == 11232)
                            ? "飞书接口频率限制(frequency limited)，请减少通知发送频率或稍后重试"
                            : "code=" + code + (msg.isEmpty() ? "" : ", msg=" + msg);
                        log.error("飞书webhook返回错误: {}", errorDesc);
                        throw new RuntimeException("飞书发送失败: " + errorDesc);
                    }
                    log.info("飞书消息发送成功，code=0");
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    log.warn("解析飞书响应体失败，按原逻辑判断: {}", e.getMessage());
                    if (!responseBody.contains("\"code\":0")) {
                        log.error("飞书可能返回了错误信息: {}", responseBody);
                        throw new RuntimeException("飞书发送失败，响应: " + responseBody);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("发送飞书消息失败，webhook地址: {}, 错误信息: {}", webhookUrl, e.getMessage(), e);
            throw new RuntimeException("发送飞书消息失败: " + e.getMessage());
        }
    }

    /**
     * 添加签名
     */
    private void addSignature(Map<String, Object> message) {
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String stringToSign = timestamp + "\n" + secret;
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] signData = mac.doFinal(new byte[]{});
            String sign = Base64.getEncoder().encodeToString(signData);
            
            message.put("timestamp", String.valueOf(timestamp));
            message.put("sign", sign);
            
            log.info("构建飞书签名: timestamp={}, sign={}", timestamp, sign);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("构建飞书签名失败: {}", e.getMessage(), e);
            log.warn("不添加签名直接发送消息");
        }
    }

    /**
     * 测试飞书连接
     */
    public boolean testConnection() {
        if (!feishuEnabled || webhookUrl.isEmpty()) {
            return false;
        }

        try {
            Map<String, Object> testMessage = buildTestMessage();
            sendMessage(testMessage);
            return true;
        } catch (Exception e) {
            log.error("飞书连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送测试通知
     */
    public String sendTestNotification() {
        // 刷新配置
        refreshConfig();
        
        log.info("开始发送飞书测试通知，配置状态: enabled={}, webhookUrl={}", feishuEnabled, webhookUrl);
        
        if (!feishuEnabled) {
            throw new RuntimeException("飞书通知未启用，请在前端配置页面启用飞书通知");
        }
        
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            throw new RuntimeException("飞书webhook地址未正确配置，请在前端配置页面设置正确的webhook地址");
        }

        try {
            Map<String, Object> testMessage = buildTestMessage();
            
            log.info("准备发送测试消息");
            sendMessage(testMessage);
            log.info("飞书测试消息发送成功");
            return "测试消息发送成功";
        } catch (Exception e) {
            log.error("发送测试通知失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送测试通知失败: " + e.getMessage());
        }
    }

    /**
     * 构建测试消息
     */
    private Map<String, Object> buildTestMessage() {
        Map<String, Object> message = new HashMap<>();
        message.put("msg_type", "interactive");
        
        // 构建卡片内容
        Map<String, Object> card = new HashMap<>();
        
        // 设置标题
        Map<String, Object> header = new HashMap<>();
        Map<String, Object> title = new HashMap<>();
        title.put("tag", "plain_text");
        title.put("content", "🧪 飞书配置测试");
        header.put("title", title);
        header.put("template", "blue");
        card.put("header", header);
        
        // 设置内容元素
        java.util.List<Map<String, Object>> elements = new java.util.ArrayList<>();
        
        // 添加测试时间
        elements.add(createFieldElement("测试时间", 
            java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        
        // 添加配置状态
        elements.add(createFieldElement("配置状态", "✅ 配置正确"));
        
        // 添加Webhook地址（部分显示）
        String maskedUrl = webhookUrl.length() > 50 ? 
            webhookUrl.substring(0, 50) + "..." : webhookUrl;
        elements.add(createFieldElement("Webhook地址", maskedUrl));
        
        // 添加分割线
        Map<String, Object> divider = new HashMap<>();
        divider.put("tag", "hr");
        elements.add(divider);
        
        // 添加备注
        Map<String, Object> note = new HashMap<>();
        note.put("tag", "note");
        Map<String, Object> noteContent = new HashMap<>();
        noteContent.put("tag", "plain_text");
        noteContent.put("content", "此消息用于测试飞书通知配置");
        note.put("elements", java.util.Arrays.asList(noteContent));
        elements.add(note);
        
        card.put("elements", elements);
        message.put("card", card);
        
        return message;
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
        return feishuEnabled && !webhookUrl.isEmpty();
    }
}

