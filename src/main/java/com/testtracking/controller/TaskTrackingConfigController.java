package com.testtracking.controller;

import com.testtracking.dto.UserDto;
import com.testtracking.service.TaskTrackingConfigService;
import com.testtracking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/task-tracking-config")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskTrackingConfigController {

    private final TaskTrackingConfigService taskTrackingConfigService;
    private final UserService userService;

    /**
     * 获取白名单配置
     */
    @GetMapping("/whitelist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getWhitelist() {
        try {
            List<String> whitelist = taskTrackingConfigService.loadWhitelist();
            return ResponseEntity.ok(Map.of("whitelist", whitelist));
        } catch (Exception e) {
            log.error("获取白名单配置失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("获取白名单配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存白名单配置
     */
    @PostMapping("/whitelist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> saveWhitelist(@RequestBody WhitelistRequest request) {
        try {
            log.info("保存任务跟踪白名单: {}", request.getUsernames());
            taskTrackingConfigService.saveWhitelist(request.getUsernames());
            return ResponseEntity.ok("白名单配置保存成功");
        } catch (Exception e) {
            log.error("保存白名单配置失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("保存白名单配置失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否在白名单中
     */
    @GetMapping("/whitelist/check/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkUserInWhitelist(@PathVariable String username) {
        try {
            boolean inWhitelist = taskTrackingConfigService.isUserInWhitelist(username);
            return ResponseEntity.ok(Map.of("username", username, "inWhitelist", inWhitelist));
        } catch (Exception e) {
            log.error("检查用户白名单状态失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("检查用户白名单状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有活跃用户列表（用于白名单选择）
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllActiveUsers() {
        try {
            List<UserDto> users = userService.getAllUsers();
            return ResponseEntity.ok(Map.of("users", users));
        } catch (Exception e) {
            log.error("获取用户列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("获取用户列表失败: " + e.getMessage());
        }
    }

    // 内部类
    public static class WhitelistRequest {
        private List<String> usernames;

        public List<String> getUsernames() {
            return usernames;
        }

        public void setUsernames(List<String> usernames) {
            this.usernames = usernames;
        }
    }
}
