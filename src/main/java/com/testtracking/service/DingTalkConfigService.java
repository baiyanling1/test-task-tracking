package com.testtracking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class DingTalkConfigService {

    private static final String CONFIG_FILE = "dingtalk-config.properties"; // 兼容旧实现，不再使用
    private static final String ENABLED_KEY = "dingtalk.enabled";
    private static final String WEBHOOK_KEY = "dingtalk.webhook"; // 与DB初始化脚本保持一致
    private static final String SECRET_KEY = "dingtalk.secret";

    @Value("${dingtalk.enabled:false}")
    private boolean defaultEnabled;

    @Value("${dingtalk.webhook.url:https://oapi.dingtalk.com/robot/send?access_token=98b6b18dfda2b07323b01b7050fce8a34ffce394815428c85eef1cff4ee41726}")
    private String defaultWebhookUrl;

    @Value("${dingtalk.secret:}")
    private String defaultSecret;

    private final SystemConfigService systemConfigService;

    /**
     * 保存钉钉配置
     */
    public void saveConfig(boolean enabled, String webhookUrl, String secret) {
        try {
            systemConfigService.setValue(ENABLED_KEY, String.valueOf(enabled), "钉钉通知开关");
            systemConfigService.setValue(WEBHOOK_KEY, webhookUrl != null ? webhookUrl : "", "钉钉Webhook地址");
            systemConfigService.setValue(SECRET_KEY, secret != null ? secret : "", "钉钉签名密钥");
            log.info("钉钉配置已保存到数据库: enabled={}, webhookUrl={}, secret={}", enabled, webhookUrl, secret);
        } catch (Exception e) {
            log.error("保存钉钉配置失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存钉钉配置失败: " + e.getMessage());
        }
    }

    /**
     * 读取钉钉配置
     */
    public DingTalkConfig loadConfig() {
        try {
            boolean enabled = systemConfigService.getValue(ENABLED_KEY)
                    .filter(v -> v != null && !v.trim().isEmpty())
                    .map(Boolean::parseBoolean)
                    .orElse(defaultEnabled);

            String webhookUrl = systemConfigService.getValue(WEBHOOK_KEY)
                    .filter(v -> v != null && !v.trim().isEmpty())
                    .orElse(defaultWebhookUrl);

            String secret = systemConfigService.getValue(SECRET_KEY)
                    .filter(v -> v != null && !v.trim().isEmpty())
                    .orElse(defaultSecret);

            log.info("从数据库加载钉钉配置: enabled={}, webhookUrl={}", enabled, webhookUrl);
            return new DingTalkConfig(enabled, webhookUrl, secret);
        } catch (Exception e) {
            log.error("读取钉钉配置失败: {}", e.getMessage(), e);
            log.warn("使用默认配置");
            return new DingTalkConfig(defaultEnabled, defaultWebhookUrl, defaultSecret);
        }
    }

    /**
     * 获取配置文件路径
     */
    private Path getConfigFilePath() {
        String userDir = System.getProperty("user.dir");
        return Paths.get(userDir, CONFIG_FILE);
    }

    /**
     * 钉钉配置类
     */
    public static class DingTalkConfig {
        private final boolean enabled;
        private final String webhookUrl;
        private final String secret;

        public DingTalkConfig(boolean enabled, String webhookUrl, String secret) {
            this.enabled = enabled;
            this.webhookUrl = webhookUrl;
            this.secret = secret;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public String getSecret() {
            return secret;
        }
    }
}
