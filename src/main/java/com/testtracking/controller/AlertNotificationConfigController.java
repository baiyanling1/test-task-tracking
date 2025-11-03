package com.testtracking.controller;

import com.testtracking.entity.AlertNotificationConfig;
import com.testtracking.service.AlertNotificationConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/alert-notification-config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AlertNotificationConfigController {

    private final AlertNotificationConfigService alertNotificationConfigService;

    /**
     * 获取所有告警通知配置
     */
    @GetMapping
    public ResponseEntity<List<AlertNotificationConfig>> getAllConfigs() {
        try {
            List<AlertNotificationConfig> configs = alertNotificationConfigService.getAllConfigs();
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            log.error("获取告警通知配置失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新告警通知配置
     */
    @PutMapping("/{alertType}")
    public ResponseEntity<?> updateConfig(
            @PathVariable String alertType,
            @RequestBody Map<String, Boolean> request) {
        try {
            Boolean dingtalkEnabled = request.get("dingtalkEnabled");
            Boolean feishuEnabled = request.get("feishuEnabled");
            
            if (dingtalkEnabled == null && feishuEnabled == null) {
                return ResponseEntity.badRequest().body("至少需要提供dingtalkEnabled或feishuEnabled参数");
            }
            
            // 如果只提供了其中一个，则使用当前值
            AlertNotificationConfig currentConfig = alertNotificationConfigService.getConfigByType(alertType);
            if (currentConfig == null) {
                return ResponseEntity.badRequest().body("告警类型不存在: " + alertType);
            }
            
            boolean finalDingtalkEnabled = dingtalkEnabled != null ? dingtalkEnabled : currentConfig.getDingtalkEnabled();
            boolean finalFeishuEnabled = feishuEnabled != null ? feishuEnabled : 
                (currentConfig.getFeishuEnabled() != null ? currentConfig.getFeishuEnabled() : false);
            
            alertNotificationConfigService.updateConfig(alertType, finalDingtalkEnabled, finalFeishuEnabled);
            return ResponseEntity.ok("配置更新成功");
        } catch (Exception e) {
            log.error("更新告警通知配置失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("配置更新失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新告警通知配置
     */
    @PutMapping("/batch")
    public ResponseEntity<?> updateConfigs(@RequestBody List<AlertNotificationConfig> configs) {
        try {
            alertNotificationConfigService.updateConfigs(configs);
            return ResponseEntity.ok("批量配置更新成功");
        } catch (Exception e) {
            log.error("批量更新告警通知配置失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("批量配置更新失败: " + e.getMessage());
        }
    }
}
