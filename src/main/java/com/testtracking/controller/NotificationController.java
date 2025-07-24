package com.testtracking.controller;

import com.testtracking.dto.NotificationDto;
import com.testtracking.service.NotificationService;
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

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

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
            // 这里需要添加删除通知的逻辑
            // 暂时返回成功，实际使用时需要完善
            return ResponseEntity.ok("通知删除成功");
        } catch (Exception e) {
            log.error("删除通知失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("删除通知失败");
        }
    }

    /**
     * 发送测试钉钉通知
     */
    @PostMapping("/test-dingtalk")
    public ResponseEntity<?> sendTestDingTalkNotification(@RequestBody TestNotificationRequest request) {
        try {
            // 这里需要添加发送测试钉钉通知的逻辑
            // 暂时返回成功，实际使用时需要完善
            return ResponseEntity.ok("测试钉钉通知发送成功");
        } catch (Exception e) {
            log.error("发送测试钉钉通知失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("发送测试钉钉通知失败");
        }
    }

    // 获取当前用户ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        // 这里需要根据用户名获取用户ID
        // 暂时返回1，实际使用时需要完善
        return 1L;
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
} 