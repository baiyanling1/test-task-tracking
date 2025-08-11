package com.testtracking.service;

import com.testtracking.dto.LoginHistoryDto;
import com.testtracking.entity.LoginHistory;
import com.testtracking.entity.User;
import com.testtracking.repository.LoginHistoryRepository;
import com.testtracking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    /**
     * 记录登录历史
     */
    @Transactional
    public void recordLoginHistory(String username, String ipAddress, String userAgent, LoginHistory.LoginStatus status) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                LoginHistory loginHistory = new LoginHistory();
                loginHistory.setUser(user);
                loginHistory.setLoginTime(LocalDateTime.now());
                loginHistory.setIpAddress(ipAddress);
                loginHistory.setUserAgent(userAgent);
                loginHistory.setStatus(status);
                
                loginHistoryRepository.save(loginHistory);
                log.info("记录登录历史: 用户={}, IP={}, 状态={}", username, ipAddress, status);
            }
        } catch (Exception e) {
            log.error("记录登录历史失败: {}", e.getMessage());
        }
    }

    /**
     * 记录成功登录
     */
    @Transactional
    public void recordSuccessfulLogin(String username, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        recordLoginHistory(username, ipAddress, userAgent, LoginHistory.LoginStatus.SUCCESS);
    }

    /**
     * 记录失败登录
     */
    @Transactional
    public void recordFailedLogin(String username, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        recordLoginHistory(username, ipAddress, userAgent, LoginHistory.LoginStatus.FAILED);
    }

    /**
     * 获取用户登录历史（最近一个月）
     */
    @Transactional(readOnly = true)
    public List<LoginHistoryDto> getUserLoginHistory(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }
        
        // 只查询最近一个月的登录记录
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<LoginHistory> loginHistories = loginHistoryRepository.findByUserAndLoginTimeAfterOrderByLoginTimeDesc(user, oneMonthAgo);
        return loginHistories.stream()
                .map(LoginHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 分页获取用户登录历史（最近一个月）
     */
    @Transactional(readOnly = true)
    public Page<LoginHistoryDto> getUserLoginHistoryPage(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Page.empty(pageable);
        }
        
        // 只查询最近一个月的登录记录
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        Page<LoginHistory> loginHistories = loginHistoryRepository.findByUserAndLoginTimeAfterOrderByLoginTimeDesc(user, oneMonthAgo, pageable);
        return loginHistories.map(LoginHistoryDto::fromEntity);
    }

    /**
     * 获取用户最近登录记录（最近一个月）
     */
    @Transactional(readOnly = true)
    public List<LoginHistoryDto> getUserRecentLoginHistory(Long userId, int limit) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }
        
        // 只查询最近一个月的登录记录
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<LoginHistory> loginHistories = loginHistoryRepository.findByUserAndLoginTimeAfterOrderByLoginTimeDesc(user, oneMonthAgo);
        return loginHistories.stream()
                .limit(limit)
                .map(LoginHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户最后一次成功登录（最近一个月）
     */
    @Transactional(readOnly = true)
    public LoginHistoryDto getLastSuccessfulLogin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        
        // 只查询最近一个月的成功登录记录
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<LoginHistory> loginHistories = loginHistoryRepository.findByUserAndStatusAndLoginTimeAfterOrderByLoginTimeDesc(user, LoginHistory.LoginStatus.SUCCESS, oneMonthAgo);
        if (loginHistories.isEmpty()) {
            return null;
        }
        
        return LoginHistoryDto.fromEntity(loginHistories.get(0));
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 清理旧的登录历史记录（清理一个月前的记录）
     */
    @Transactional
    public void cleanOldLoginHistory() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<LoginHistory> oldRecords = loginHistoryRepository.findByLoginTimeBefore(oneMonthAgo);
        
        if (!oldRecords.isEmpty()) {
            loginHistoryRepository.deleteAll(oldRecords);
            log.info("清理了 {} 条一个月前的登录历史记录", oldRecords.size());
        }
    }
}
