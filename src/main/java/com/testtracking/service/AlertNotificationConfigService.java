package com.testtracking.service;

import com.testtracking.entity.AlertNotificationConfig;
import com.testtracking.repository.AlertNotificationConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotificationConfigService {

    private final AlertNotificationConfigRepository alertNotificationConfigRepository;

    /**
     * 获取所有告警通知配置
     */
    @Transactional(readOnly = true)
    public List<AlertNotificationConfig> getAllConfigs() {
        return alertNotificationConfigRepository.findAll();
    }

    /**
     * 根据告警类型获取配置
     */
    @Transactional(readOnly = true)
    public AlertNotificationConfig getConfigByType(String alertType) {
        return alertNotificationConfigRepository.findByAlertType(alertType)
                .orElse(null);
    }

    /**
     * 检查指定告警类型是否启用钉钉通知
     */
    @Transactional(readOnly = true)
    public boolean isDingTalkEnabled(String alertType) {
        AlertNotificationConfig config = getConfigByType(alertType);
        return config != null && Boolean.TRUE.equals(config.getDingtalkEnabled());
    }

    /**
     * 检查指定告警类型是否启用飞书通知
     */
    @Transactional(readOnly = true)
    public boolean isFeiShuEnabled(String alertType) {
        AlertNotificationConfig config = getConfigByType(alertType);
        return config != null && Boolean.TRUE.equals(config.getFeishuEnabled());
    }

    /**
     * 更新告警通知配置
     */
    @Transactional
    public void updateConfig(String alertType, boolean dingtalkEnabled, boolean feishuEnabled) {
        AlertNotificationConfig config = getConfigByType(alertType);
        if (config != null) {
            config.setDingtalkEnabled(dingtalkEnabled);
            config.setFeishuEnabled(feishuEnabled);
            alertNotificationConfigRepository.save(config);
            log.info("更新告警通知配置: {} -> 钉钉通知: {}, 飞书通知: {}", alertType, dingtalkEnabled, feishuEnabled);
        } else {
            log.warn("告警类型配置不存在: {}", alertType);
        }
    }

    /**
     * 更新告警通知配置（兼容旧版本）
     */
    @Transactional
    public void updateConfig(String alertType, boolean dingtalkEnabled) {
        AlertNotificationConfig config = getConfigByType(alertType);
        if (config != null) {
            config.setDingtalkEnabled(dingtalkEnabled);
            alertNotificationConfigRepository.save(config);
            log.info("更新告警通知配置: {} -> 钉钉通知: {}", alertType, dingtalkEnabled);
        } else {
            log.warn("告警类型配置不存在: {}", alertType);
        }
    }

    /**
     * 批量更新告警通知配置
     */
    @Transactional
    public void updateConfigs(List<AlertNotificationConfig> configs) {
        for (AlertNotificationConfig config : configs) {
            updateConfig(config.getAlertType(), config.getDingtalkEnabled(), 
                        config.getFeishuEnabled() != null ? config.getFeishuEnabled() : false);
        }
        log.info("批量更新告警通知配置完成，共更新 {} 个配置", configs.size());
    }
}
