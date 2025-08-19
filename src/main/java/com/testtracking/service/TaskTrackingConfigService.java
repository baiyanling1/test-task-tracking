package com.testtracking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskTrackingConfigService {

    private static final String WHITELIST_KEY = "task.tracking.whitelist";
    
    private final SystemConfigService systemConfigService;

    @Value("${task.tracking.whitelist:admin}")
    private String defaultWhitelist;

    /**
     * 保存白名单配置
     */
    @Transactional
    public void saveWhitelist(List<String> usernames) {
        try {
            String whitelistStr = String.join(",", usernames);
            systemConfigService.setValue(WHITELIST_KEY, whitelistStr, "任务跟踪白名单配置");
            log.info("任务跟踪白名单已保存到数据库: {}", whitelistStr);
        } catch (Exception e) {
            log.error("保存任务跟踪白名单失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存任务跟踪白名单失败: " + e.getMessage());
        }
    }

    /**
     * 读取白名单配置
     */
    @Transactional(readOnly = true)
    public List<String> loadWhitelist() {
        try {
            Optional<String> whitelistOpt = systemConfigService.getValue(WHITELIST_KEY);
            if (whitelistOpt.isPresent() && whitelistOpt.get() != null && !whitelistOpt.get().trim().isEmpty()) {
                String whitelistStr = whitelistOpt.get();
                List<String> whitelist = Arrays.asList(whitelistStr.split(","));
                log.info("从数据库加载任务跟踪白名单: {}", whitelist);
                return whitelist;
            } else {
                log.info("数据库中没有白名单配置，使用默认白名单");
                return Arrays.asList(defaultWhitelist.split(","));
            }
        } catch (Exception e) {
            log.error("读取任务跟踪白名单失败: {}", e.getMessage(), e);
            log.warn("使用默认白名单");
            return Arrays.asList(defaultWhitelist.split(","));
        }
    }

    /**
     * 检查用户是否在白名单中
     */
    public boolean isUserInWhitelist(String username) {
        List<String> whitelist = loadWhitelist();
        return whitelist.contains(username);
    }
}
