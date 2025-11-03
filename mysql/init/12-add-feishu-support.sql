-- 添加飞书通知配置字段
ALTER TABLE alert_notification_config 
ADD COLUMN feishu_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用飞书通知' AFTER dingtalk_enabled;

-- 更新系统配置，添加飞书相关配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('feishu.enabled', 'false', '飞书通知开关'),
('feishu.webhook', '', '飞书Webhook地址'),
('feishu.secret', '', '飞书签名密钥')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    description = VALUES(description);

