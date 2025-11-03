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
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;
import java.util.HashMap;
import com.testtracking.service.DingTalkNotificationService;
import com.testtracking.service.DingTalkConfigService;
import com.testtracking.service.FeiShuNotificationService;
import com.testtracking.service.FeiShuConfigService;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private final DingTalkNotificationService dingTalkNotificationService;
    private final DingTalkConfigService dingTalkConfigService;
    private final FeiShuNotificationService feiShuNotificationService;
    private final FeiShuConfigService feiShuConfigService;

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
     * 测试钉钉通知配置
     */
    @PostMapping("/test-dingtalk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testDingTalkNotification() {
        try {
            String result = dingTalkNotificationService.sendTestNotification();
            return ResponseEntity.ok("钉钉通知测试成功: " + result);
        } catch (Exception e) {
            log.error("钉钉通知测试失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("钉钉通知测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取钉钉配置信息
     */
    @GetMapping("/dingtalk-config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDingTalkConfig() {
        try {
            DingTalkConfigService.DingTalkConfig config = dingTalkConfigService.loadConfig();
            Map<String, Object> response = new HashMap<>();
            response.put("enabled", config.isEnabled());
            response.put("webhookUrl", config.getWebhookUrl());
            response.put("secret", config.getSecret());
            response.put("isConfigured", config.isEnabled() && !config.getWebhookUrl().isEmpty());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取钉钉配置失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("获取钉钉配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存钉钉配置
     */
    @PostMapping("/dingtalk-config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> saveDingTalkConfig(@RequestBody DingTalkConfigRequest request) {
        try {
            log.info("保存钉钉配置: enabled={}, webhookUrl={}", request.isEnabled(), request.getWebhookUrl());
            
            // 验证webhook地址
            if (request.isEnabled() && (request.getWebhookUrl() == null || request.getWebhookUrl().trim().isEmpty())) {
                return ResponseEntity.badRequest().body("启用钉钉通知时必须提供webhook地址");
            }
            
            // 保存配置
            dingTalkConfigService.saveConfig(request.isEnabled(), request.getWebhookUrl(), request.getSecret());
            
            return ResponseEntity.ok("钉钉配置保存成功");
        } catch (Exception e) {
            log.error("保存钉钉配置失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("保存钉钉配置失败: " + e.getMessage());
        }
    }

    /**
     * 测试飞书通知配置
     */
    @PostMapping("/test-feishu")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testFeiShuNotification() {
        try {
            String result = feiShuNotificationService.sendTestNotification();
            return ResponseEntity.ok("飞书通知测试成功: " + result);
        } catch (Exception e) {
            log.error("飞书通知测试失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("飞书通知测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取飞书配置信息
     */
    @GetMapping("/feishu-config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getFeiShuConfig() {
        try {
            FeiShuConfigService.FeiShuConfig config = feiShuConfigService.loadConfig();
            Map<String, Object> response = new HashMap<>();
            response.put("enabled", config.isEnabled());
            response.put("webhookUrl", config.getWebhookUrl());
            response.put("secret", config.getSecret());
            response.put("isConfigured", config.isEnabled() && !config.getWebhookUrl().isEmpty());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取飞书配置失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("获取飞书配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存飞书配置
     */
    @PostMapping("/feishu-config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> saveFeiShuConfig(@RequestBody FeiShuConfigRequest request) {
        try {
            log.info("保存飞书配置: enabled={}, webhookUrl={}", request.isEnabled(), request.getWebhookUrl());
            
            // 验证webhook地址
            if (request.isEnabled() && (request.getWebhookUrl() == null || request.getWebhookUrl().trim().isEmpty())) {
                return ResponseEntity.badRequest().body("启用飞书通知时必须提供webhook地址");
            }
            
            // 保存配置
            feiShuConfigService.saveConfig(request.isEnabled(), request.getWebhookUrl(), request.getSecret());
            
            return ResponseEntity.ok("飞书配置保存成功");
        } catch (Exception e) {
            log.error("保存飞书配置失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("保存飞书配置失败: " + e.getMessage());
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

    public static class FeiShuConfigRequest {
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