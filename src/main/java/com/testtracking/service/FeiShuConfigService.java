package com.testtracking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeiShuConfigService {

    private static final String ENABLED_KEY = "feishu.enabled";
    private static final String WEBHOOK_KEY = "feishu.webhook";
    private static final String SECRET_KEY = "feishu.secret";

    @Value("${feishu.enabled:false}")
    private boolean defaultEnabled;

    @Value("${feishu.webhook.url:}")
    private String defaultWebhookUrl;

    @Value("${feishu.secret:}")
    private String defaultSecret;

    private final SystemConfigService systemConfigService;

    /**
     * 保存飞书配置
     */
    public void saveConfig(boolean enabled, String webhookUrl, String secret) {
        try {
            systemConfigService.setValue(ENABLED_KEY, String.valueOf(enabled), "飞书通知开关");
            systemConfigService.setValue(WEBHOOK_KEY, webhookUrl != null ? webhookUrl : "", "飞书Webhook地址");
            systemConfigService.setValue(SECRET_KEY, secret != null ? secret : "", "飞书签名密钥");
            log.info("飞书配置已保存到数据库: enabled={}, webhookUrl={}, secret={}", enabled, webhookUrl, secret);
        } catch (Exception e) {
            log.error("保存飞书配置失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存飞书配置失败: " + e.getMessage());
        }
    }

    /**
     * 读取飞书配置
     */
    public FeiShuConfig loadConfig() {
        try {
            boolean enabled = Boolean.parseBoolean(
                systemConfigService.getValue(ENABLED_KEY)
                    .orElse(String.valueOf(defaultEnabled))
            );
            
            String webhookUrl = systemConfigService.getValue(WEBHOOK_KEY)
                    .filter(url -> !url.isEmpty())
                    .orElse(defaultWebhookUrl);
            
            String secret = systemConfigService.getValue(SECRET_KEY)
                    .orElse(defaultSecret);
            
            log.info("从数据库加载飞书配置: enabled={}, webhookUrl={}", enabled, webhookUrl);
            return new FeiShuConfig(enabled, webhookUrl, secret);
        } catch (Exception e) {
            log.error("读取飞书配置失败: {}", e.getMessage(), e);
            log.info("使用默认飞书配置");
            return new FeiShuConfig(defaultEnabled, defaultWebhookUrl, defaultSecret);
        }
    }

    /**
     * 检查飞书是否已配置
     */
    public boolean isConfigured() {
        FeiShuConfig config = loadConfig();
        return config.isEnabled() && config.getWebhookUrl() != null && !config.getWebhookUrl().isEmpty();
    }

    /**
     * 飞书配置类
     */
    public static class FeiShuConfig {
        private final boolean enabled;
        private final String webhookUrl;
        private final String secret;

        public FeiShuConfig(boolean enabled, String webhookUrl, String secret) {
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

