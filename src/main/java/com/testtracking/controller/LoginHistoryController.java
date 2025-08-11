package com.testtracking.controller;

import com.testtracking.dto.LoginHistoryDto;
import com.testtracking.service.LoginHistoryService;
import com.testtracking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/login-history")
@RequiredArgsConstructor
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;
    private final UserService userService;

    /**
     * 获取当前用户的登录历史
     */
    @GetMapping("/my")
    public ResponseEntity<List<LoginHistoryDto>> getMyLoginHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 这里需要根据用户名获取用户ID，简化处理
            // 实际项目中应该从用户服务获取当前用户ID
            Long userId = getCurrentUserId();
            
            List<LoginHistoryDto> loginHistory = loginHistoryService.getUserLoginHistory(userId);
            return ResponseEntity.ok(loginHistory);
        } catch (Exception e) {
            log.error("获取登录历史失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 分页获取当前用户的登录历史
     */
    @GetMapping("/my/page")
    public ResponseEntity<Page<LoginHistoryDto>> getMyLoginHistoryPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            Page<LoginHistoryDto> loginHistory = loginHistoryService.getUserLoginHistoryPage(userId, pageable);
            return ResponseEntity.ok(loginHistory);
        } catch (Exception e) {
            log.error("获取登录历史失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取当前用户最近登录记录
     */
    @GetMapping("/my/recent")
    public ResponseEntity<List<LoginHistoryDto>> getMyRecentLoginHistory(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            Long userId = getCurrentUserId();
            List<LoginHistoryDto> loginHistory = loginHistoryService.getUserRecentLoginHistory(userId, limit);
            return ResponseEntity.ok(loginHistory);
        } catch (Exception e) {
            log.error("获取最近登录历史失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取当前用户最后一次成功登录
     */
    @GetMapping("/my/last-success")
    public ResponseEntity<LoginHistoryDto> getMyLastSuccessfulLogin() {
        try {
            Long userId = getCurrentUserId();
            LoginHistoryDto lastLogin = loginHistoryService.getLastSuccessfulLogin(userId);
            return ResponseEntity.ok(lastLogin);
        } catch (Exception e) {
            log.error("获取最后一次成功登录失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 管理员获取指定用户的登录历史
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoginHistoryDto>> getUserLoginHistory(@PathVariable Long userId) {
        try {
            List<LoginHistoryDto> loginHistory = loginHistoryService.getUserLoginHistory(userId);
            return ResponseEntity.ok(loginHistory);
        } catch (Exception e) {
            log.error("获取用户登录历史失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 管理员分页获取指定用户的登录历史
     */
    @GetMapping("/user/{userId}/page")
    public ResponseEntity<Page<LoginHistoryDto>> getUserLoginHistoryPage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LoginHistoryDto> loginHistory = loginHistoryService.getUserLoginHistoryPage(userId, pageable);
            return ResponseEntity.ok(loginHistory);
        } catch (Exception e) {
            log.error("获取用户登录历史失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 管理员手动清理旧的登录历史记录
     */
    @DeleteMapping("/clean-old")
    public ResponseEntity<?> cleanOldLoginHistory() {
        try {
            loginHistoryService.cleanOldLoginHistory();
            return ResponseEntity.ok("成功清理一个月前的登录历史记录");
        } catch (Exception e) {
            log.error("清理登录历史失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("清理失败");
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
}
