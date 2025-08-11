package com.testtracking.dto;

import com.testtracking.entity.LoginHistory;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginHistoryDto {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    private LoginHistory.LoginStatus status;

    public static LoginHistoryDto fromEntity(LoginHistory loginHistory) {
        LoginHistoryDto dto = new LoginHistoryDto();
        dto.setId(loginHistory.getId());
        dto.setUserId(loginHistory.getUser().getId());
        dto.setUsername(loginHistory.getUser().getUsername());
        dto.setRealName(loginHistory.getUser().getRealName());
        dto.setLoginTime(loginHistory.getLoginTime());
        dto.setIpAddress(loginHistory.getIpAddress());
        dto.setUserAgent(loginHistory.getUserAgent());
        dto.setStatus(loginHistory.getStatus());
        return dto;
    }
}
