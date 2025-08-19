-- 添加默认的任务跟踪白名单配置
INSERT INTO system_config (config_key, config_value, description, created_time, updated_time) 
VALUES ('task.tracking.whitelist', 'admin', '任务跟踪白名单配置', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    description = VALUES(description),
    updated_time = NOW();
