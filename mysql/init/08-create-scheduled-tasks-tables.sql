-- 创建定时任务表
CREATE TABLE IF NOT EXISTS scheduled_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(100) NOT NULL UNIQUE COMMENT '任务名称',
    task_description TEXT COMMENT '任务描述',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
    bean_name VARCHAR(100) NOT NULL COMMENT 'Spring Bean名称',
    method_name VARCHAR(100) NOT NULL COMMENT '方法名称',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    last_execute_time DATETIME COMMENT '最后执行时间',
    last_execute_result VARCHAR(20) COMMENT '最后执行结果',
    next_execute_time DATETIME COMMENT '下次执行时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '任务状态',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(100) COMMENT '创建人',
    updated_by VARCHAR(100) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- 创建定时任务执行记录表
CREATE TABLE IF NOT EXISTS scheduled_task_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    execution_time DATETIME NOT NULL COMMENT '执行时间',
    execution_result VARCHAR(20) COMMENT '执行结果',
    error_message TEXT COMMENT '错误信息',
    execution_duration BIGINT COMMENT '执行时长(毫秒)',
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '执行状态',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(100) COMMENT '创建人',
    updated_by VARCHAR(100) COMMENT '更新人',
    FOREIGN KEY (task_id) REFERENCES scheduled_tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行记录表';

-- 插入默认的定时任务数据
INSERT INTO scheduled_tasks (task_name, task_description, cron_expression, bean_name, method_name, enabled, status, created_time, updated_time) VALUES
('checkOverdueTasks', '超时任务检查', '0 0 1 * * ?', 'testTaskService', 'checkOverdueTasks', true, 'ENABLED', NOW(), NOW()),
('cleanOldLoginHistory', '登录历史清理', '0 0 2 * * ?', 'scheduledTaskService', 'cleanOldLoginHistory', true, 'ENABLED', NOW(), NOW()),
('scheduledBackup', '数据库备份', '0 0 2 * * MON', 'databaseBackupService', 'scheduledBackup', true, 'ENABLED', NOW(), NOW()),
('deleteExpiredNotifications', '过期通知清理', '0 0 2 * * ?', 'notificationService', 'deleteExpiredNotifications', true, 'ENABLED', NOW(), NOW()),
('checkFridayTaskTracking', '任务跟踪提醒', '0 30 9 * * MON', 'taskTrackingReminderService', 'checkFridayTaskTracking', true, 'ENABLED', NOW(), NOW())
ON DUPLICATE KEY UPDATE
task_description = VALUES(task_description),
cron_expression = VALUES(cron_expression),
bean_name = VALUES(bean_name),
method_name = VALUES(method_name),
updated_time = NOW();
