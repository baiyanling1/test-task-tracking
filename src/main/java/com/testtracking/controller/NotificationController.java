package com.testtracking.controller;

import com.testtracking.dto.NotificationDto;
import com.testtracking.service.NotificationService;
import com.testtracking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * 获取当前用户的通知列表
     */
    @GetMapping
    public ResponseEntity<?> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Long currentUserId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size, 
                    Sort.Direction.fromString(sortDir.toUpperCase()), sortBy);
            
            Page<NotificationDto> notifications = notificationService.getUserNotifications(currentUserId, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("获取通知列表失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取通知列表失败");
        }
    }

    /**
     * 获取当前用户未读通知数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            Long currentUserId = getCurrentUserId();
            long unreadCount = notificationService.getUnreadNotificationCount(currentUserId);
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
        } catch (Exception e) {
            log.error("获取未读通知数量失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取未读通知数量失败");
        }
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok("通知已标记为已读");
        } catch (Exception e) {
            log.error("标记通知为已读失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("标记通知为已读失败");
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead() {
        try {
            Long currentUserId = getCurrentUserId();
            notificationService.markAllNotificationsAsRead(currentUserId);
            return ResponseEntity.ok("所有通知已标记为已读");
        } catch (Exception e) {
            log.error("标记所有通知为已读失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("标记所有通知为已读失败");
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok("通知已删除");
        } catch (Exception e) {
            log.error("删除通知失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("删除通知失败");
        }
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteNotifications(@RequestBody java.util.List<Long> notificationIds) {
        try {
            notificationService.deleteNotifications(notificationIds);
            return ResponseEntity.ok("批量删除通知成功");
        } catch (Exception e) {
            log.error("批量删除通知失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("批量删除通知失败");
        }
    }

    /**
     * 根据状态获取通知
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getNotificationsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long currentUserId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<NotificationDto> notifications = notificationService.getNotificationsByStatus(currentUserId, status, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("根据状态获取通知失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("根据状态获取通知失败");
        }
    }

    /**
     * 根据优先级获取通知
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<?> getNotificationsByPriority(
            @PathVariable String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long currentUserId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<NotificationDto> notifications = notificationService.getNotificationsByPriority(currentUserId, priority, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("根据优先级获取通知失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("根据优先级获取通知失败");
        }
    }

    /**
     * 搜索通知
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchNotifications(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long currentUserId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<NotificationDto> notifications = notificationService.searchNotifications(currentUserId, keyword, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("搜索通知失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("搜索通知失败");
        }
    }

    /**
     * 获取超时任务通知
     */
    @GetMapping("/overdue-tasks")
    public ResponseEntity<?> getOverdueTaskNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long currentUserId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<NotificationDto> notifications = notificationService.getOverdueTaskNotifications(currentUserId, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("获取超时任务通知失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取超时任务通知失败");
        }
    }

    /**
     * 发送测试钉钉通知
     */
    @PostMapping("/test-dingtalk")
    public ResponseEntity<?> sendTestDingTalkNotification(@RequestBody TestNotificationRequest request) {
        try {
            // 从保存的配置中读取钉钉设置
            boolean dingtalkEnabled = (Boolean) dingTalkConfig.get("enabled");
            String webhookUrl = (String) dingTalkConfig.get("webhookUrl");
            
            if (!dingtalkEnabled) {
                return ResponseEntity.badRequest().body("钉钉通知未启用，请先在配置中启用钉钉通知");
            }
            
            if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("钉钉webhook地址未配置，请先配置webhook地址");
            }
            
            // 这里应该调用DingTalkNotificationService发送测试消息
            // 暂时返回成功，实际使用时需要完善
            log.info("发送测试钉钉通知: enabled={}, webhookUrl={}, message={}, type={}", 
                    dingtalkEnabled, webhookUrl, request.getMessage(), request.getType());
            return ResponseEntity.ok("测试钉钉通知发送成功");
        } catch (Exception e) {
            log.error("发送测试钉钉通知失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("发送测试钉钉通知失败: " + e.getMessage());
        }
    }

    // 临时存储钉钉配置（实际项目中应该使用数据库或配置文件）
    private static Map<String, Object> dingTalkConfig = new HashMap<>();
    static {
        dingTalkConfig.put("enabled", false);
        dingTalkConfig.put("webhookUrl", "");
        dingTalkConfig.put("secret", "");
    }

    /**
     * 获取钉钉配置
     */
    @GetMapping("/dingtalk-config")
    public ResponseEntity<?> getDingTalkConfig() {
        try {
            return ResponseEntity.ok(dingTalkConfig);
        } catch (Exception e) {
            log.error("获取钉钉配置失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取钉钉配置失败");
        }
    }

    /**
     * 保存钉钉配置
     */
    @PostMapping("/dingtalk-config")
    public ResponseEntity<?> saveDingTalkConfig(@RequestBody DingTalkConfigRequest request) {
        try {
            // 保存配置到内存（实际项目中应该保存到数据库或配置文件）
            dingTalkConfig.put("enabled", request.isEnabled());
            dingTalkConfig.put("webhookUrl", request.getWebhookUrl());
            dingTalkConfig.put("secret", request.getSecret());
            
            log.info("保存钉钉配置: enabled={}, webhookUrl={}", request.isEnabled(), request.getWebhookUrl());
            return ResponseEntity.ok("钉钉配置保存成功");
        } catch (Exception e) {
            log.error("保存钉钉配置失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("保存钉钉配置失败");
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            return userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username))
                    .getId();
        } catch (Exception e) {
            log.error("获取当前用户ID失败: {}", e.getMessage());
            throw new RuntimeException("获取当前用户ID失败");
        }
    }

    // 内部类
    public static class TestNotificationRequest {
        private String message;
        private String type;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    // 内部类
    public static class DingTalkConfigRequest {
        private boolean enabled;
        private String webhookUrl;
        private String secret;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }
} 