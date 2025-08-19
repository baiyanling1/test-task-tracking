-- 创建告警通知配置表
CREATE TABLE IF NOT EXISTS alert_notification_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_type VARCHAR(100) NOT NULL UNIQUE COMMENT '告警类型',
    alert_name VARCHAR(200) NOT NULL COMMENT '告警类型名称',
    dingtalk_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用钉钉通知',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认的告警类型配置
INSERT INTO alert_notification_config (alert_type, alert_name, dingtalk_enabled) VALUES
('TASK_TRACKING_REMINDER', '任务跟踪表填写提醒', TRUE),
('TASK_ASSIGNMENT', '任务分配通知', FALSE),
('TASK_OVERDUE', '任务超时提醒', FALSE),
('TASK_COMPLETION', '任务完成通知', FALSE),
('SYSTEM_MAINTENANCE', '系统维护通知', FALSE)
ON DUPLICATE KEY UPDATE 
    alert_name = VALUES(alert_name),
    updated_time = CURRENT_TIMESTAMP;
