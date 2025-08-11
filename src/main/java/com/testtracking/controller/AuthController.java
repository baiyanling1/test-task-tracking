package com.testtracking.controller;

import com.testtracking.dto.UserDto;
import com.testtracking.entity.User;
import com.testtracking.security.JwtTokenUtil;
import com.testtracking.service.LoginHistoryService;
import com.testtracking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final LoginHistoryService loginHistoryService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenUtil.generateToken(userDetails);

            // 获取完整的用户信息
            UserDto user = userService.getUserByUsername(loginRequest.getUsername())
                    .map(UserDto::fromEntityWithoutPassword)
                    .orElse(null);

            // 更新最后登录时间
            userService.updateLastLoginTime(loginRequest.getUsername());

            // 记录登录历史
            loginHistoryService.recordSuccessfulLogin(loginRequest.getUsername(), request);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            // 记录失败登录
            loginHistoryService.recordFailedLogin(loginRequest.getUsername(), request);
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto userDto) {
        try {
            UserDto createdUser = userService.createUser(userDto);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 验证令牌
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody TokenRequest tokenRequest) {
        try {
            boolean isValid = jwtTokenUtil.validateToken(tokenRequest.getToken());
            if (isValid) {
                String username = jwtTokenUtil.extractUsername(tokenRequest.getToken());
                UserDto user = userService.getUserByUsername(username).map(UserDto::fromEntityWithoutPassword).orElse(null);
                
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("user", user);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.ok(Map.of("valid", false));
            }
        } catch (Exception e) {
            log.error("令牌验证失败: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String username = jwtTokenUtil.extractUsername(jwt);
            UserDto user = userService.getUserByUsername(username).map(UserDto::fromEntityWithoutPassword).orElse(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取用户信息失败");
        }
    }

    /**
     * 获取用户资料
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String username = jwtTokenUtil.extractUsername(jwt);
            UserDto user = userService.getUserByUsername(username).map(UserDto::fromEntityWithoutPassword).orElse(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("获取用户资料失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取用户资料失败");
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token, @RequestBody ChangePasswordRequest request) {
        try {
            String jwt = token.substring(7);
            String username = jwtTokenUtil.extractUsername(jwt);
            
            // 验证当前密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getCurrentPassword())
            );
            
            // 更新密码
            userService.changePassword(username, request.getNewPassword());
            
            return ResponseEntity.ok("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("当前密码错误或修改失败");
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            // 在实际应用中，可以将token加入黑名单
            // 这里简单返回成功
            return ResponseEntity.ok("登出成功");
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("登出失败");
        }
    }

    // 内部类
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class TokenRequest {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
} 