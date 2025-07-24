package com.testtracking.controller;

import com.testtracking.dto.UserDto;
import com.testtracking.dto.UserUpdateDto;
import com.testtracking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, 
                    Sort.Direction.fromString(sortDir.toUpperCase()), sortBy);
            
            Page<UserDto> users = userService.getUsersWithFilters(username, role, isActive, pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("获取用户列表失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取用户列表失败");
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #userId == authentication.principal.id")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            UserDto user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("获取用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取用户失败");
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto) {
        try {
            UserDto createdUser = userService.createUser(userDto);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #userId == authentication.principal.id")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        try {
            // 如果不是ADMIN或MANAGER，只允许修改密码
            if (!hasRole("ADMIN") && !hasRole("MANAGER")) {
                // 只允许修改密码，其他字段保持不变
                UserDto currentUser = userService.getUserById(userId);
                userUpdateDto.setUsername(currentUser.getUsername());
                userUpdateDto.setEmail(currentUser.getEmail());
                userUpdateDto.setRealName(currentUser.getRealName());
                userUpdateDto.setRole(currentUser.getRole());
                userUpdateDto.setIsActive(currentUser.getIsActive());
            }
            
            // 直接使用UserUpdateDto更新用户
            UserDto updatedUser = userService.updateUserFromDto(userId, userUpdateDto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("更新用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("用户删除成功");
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 禁用用户
     */
    @PostMapping("/{userId}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> disableUser(@PathVariable Long userId) {
        try {
            userService.disableUser(userId);
            return ResponseEntity.ok("用户已禁用");
        } catch (Exception e) {
            log.error("禁用用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 启用用户
     */
    @PostMapping("/{userId}/enable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> enableUser(@PathVariable Long userId) {
        try {
            userService.enableUser(userId);
            return ResponseEntity.ok("用户已启用");
        } catch (Exception e) {
            log.error("启用用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long userId) {
        try {
            String newPassword = userService.resetUserPassword(userId);
            return ResponseEntity.ok(Map.of("newPassword", newPassword));
        } catch (Exception e) {
            log.error("重置用户密码失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getUserStats() {
        try {
            Map<String, Object> stats = userService.getUserStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取用户统计失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取用户统计失败");
        }
    }

    /**
     * 更新用户最后登录时间
     */
    @PostMapping("/{userId}/update-last-login")
    public ResponseEntity<?> updateLastLoginTime(@PathVariable Long userId) {
        try {
            userService.updateLastLoginTime(userId);
            return ResponseEntity.ok("登录时间更新成功");
        } catch (Exception e) {
            log.error("更新登录时间失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("更新登录时间失败");
        }
    }
    
    /**
     * 检查当前用户是否具有指定角色
     */
    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
} 