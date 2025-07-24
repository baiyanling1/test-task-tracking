-- 测试任务跟踪系统数据库初始化脚本
-- 创建时间: 2024年
-- 描述: 初始化数据库表结构和基础数据

-- 使用数据库
USE test_tracking;

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 用户表
-- ========================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `real_name` varchar(100) NOT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone_number` varchar(20) DEFAULT NULL COMMENT '手机号',
  `role` enum('ADMIN','MANAGER','TESTER') NOT NULL DEFAULT 'TESTER' COMMENT '角色',
  `is_active` boolean NOT NULL DEFAULT true COMMENT '是否激活',
  `department` varchar(100) DEFAULT NULL COMMENT '部门',
  `position` varchar(100) DEFAULT NULL COMMENT '职位',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 部门表
-- ========================================
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  `description` text COMMENT '部门描述',
  `is_active` boolean NOT NULL DEFAULT true COMMENT '是否活跃',
  `sort_order` int DEFAULT 0 COMMENT '排序值',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ========================================
-- 测试任务表
-- ========================================
DROP TABLE IF EXISTS `test_tasks`;
CREATE TABLE `test_tasks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(200) NOT NULL COMMENT '任务名称',
  `task_description` text COMMENT '任务描述',
  `start_date` date NOT NULL COMMENT '开始日期',
  `expected_end_date` date NOT NULL COMMENT '预期结束日期',
  `actual_end_date` date DEFAULT NULL COMMENT '实际结束日期',
  `participant_count` int NOT NULL COMMENT '参与人数',
  `man_days` double DEFAULT NULL COMMENT '人天',
  `task_status` enum('PLANNED','IN_PROGRESS','ON_HOLD','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PLANNED' COMMENT '任务状态',
  `priority` enum('LOW','MEDIUM','HIGH','URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',
  `progress_percentage` int DEFAULT 0 COMMENT '进度百分比',
  `risk_level` enum('LOW','MEDIUM','HIGH','CRITICAL') DEFAULT 'LOW' COMMENT '风险等级',
  `risk_description` text COMMENT '风险描述',
  `assigned_to` bigint(20) DEFAULT NULL COMMENT '负责人ID',
  `created_by_user` bigint(20) NOT NULL COMMENT '创建人ID',
  `project_name` varchar(100) DEFAULT NULL COMMENT '项目名称',
  `module_name` varchar(100) DEFAULT NULL COMMENT '模块名称',
  `test_type` enum('FUNCTIONAL','PERFORMANCE','SECURITY','USABILITY','COMPATIBILITY','INTEGRATION','SYSTEM','REGRESSION') DEFAULT NULL COMMENT '测试类型',
  `is_overdue` boolean DEFAULT false COMMENT '是否超时',
  `overdue_days` int DEFAULT 0 COMMENT '超时天数',
  `last_progress_update` datetime DEFAULT NULL COMMENT '最后进度更新时间',
  `department` varchar(100) DEFAULT NULL COMMENT '部门',
  `delay_reason` text COMMENT '延期原因',
  `is_delayed_completion` boolean DEFAULT false COMMENT '是否延期完成',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_assigned_to` (`assigned_to`),
  KEY `idx_created_by_user` (`created_by_user`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_expected_end_date` (`expected_end_date`),
  KEY `idx_is_overdue` (`is_overdue`),
  CONSTRAINT `fk_task_assigned_to` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_task_created_by_user` FOREIGN KEY (`created_by_user`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试任务表';

-- ========================================
-- 任务进度表
-- ========================================
DROP TABLE IF EXISTS `task_progress`;
CREATE TABLE `task_progress` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `updated_by_user` bigint(20) NOT NULL COMMENT '更新用户ID',
  `progress_percentage` int NOT NULL COMMENT '进度百分比',
  `progress_notes` text COMMENT '进度说明',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `task_status` enum('PLANNED','IN_PROGRESS','ON_HOLD','COMPLETED','CANCELLED') DEFAULT NULL COMMENT '任务状态',
  `risk_level` enum('LOW','MEDIUM','HIGH','CRITICAL') DEFAULT NULL COMMENT '风险等级',
  `risk_description` text COMMENT '风险描述',
  `blockers` text COMMENT '阻碍因素',
  `next_steps` text COMMENT '下一步计划',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_updated_by_user` (`updated_by_user`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_progress_task` FOREIGN KEY (`task_id`) REFERENCES `test_tasks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_progress_user` FOREIGN KEY (`updated_by_user`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务进度表';

-- ========================================
-- 通知表
-- ========================================
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `notification_type` enum('TASK_ASSIGNED','TASK_COMPLETED','TASK_OVERDUE','TASK_PROGRESS_UPDATE','RISK_ALERT','SYSTEM_ALERT','DINGTALK_NOTIFICATION') NOT NULL COMMENT '通知类型',
  `priority` enum('LOW','NORMAL','HIGH','URGENT') DEFAULT 'NORMAL' COMMENT '优先级',
  `is_read` boolean DEFAULT false COMMENT '是否已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `recipient_id` bigint(20) DEFAULT NULL COMMENT '接收者ID',
  `related_task_id` bigint(20) DEFAULT NULL COMMENT '相关任务ID',
  `related_entity_type` varchar(50) DEFAULT NULL COMMENT '相关实体类型',
  `related_entity_id` bigint(20) DEFAULT NULL COMMENT '相关实体ID',
  `action_url` varchar(500) DEFAULT NULL COMMENT '操作链接',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_recipient_id` (`recipient_id`),
  KEY `idx_related_task_id` (`related_task_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_time` (`created_time`),
  CONSTRAINT `fk_notification_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_notification_task` FOREIGN KEY (`related_task_id`) REFERENCES `test_tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ========================================
-- 登录历史表
-- ========================================
DROP TABLE IF EXISTS `login_history`;
CREATE TABLE `login_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text COMMENT '用户代理',
  `status` enum('SUCCESS','FAILED','LOCKED') NOT NULL DEFAULT 'SUCCESS' COMMENT '登录状态',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_login_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录历史表';

-- ========================================
-- 系统配置表
-- ========================================
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `description` varchar(255) DEFAULT NULL COMMENT '配置描述',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ========================================
-- 插入初始数据
-- ========================================

-- 插入默认部门
INSERT INTO `departments` (`name`, `description`, `is_active`, `sort_order`) VALUES
('运营商', '运营商业务部门', true, 1),
('创新业务', '创新业务部门', true, 2),
('RedteaReady', 'RedteaReady产品部门', true, 3),
('xSIM', 'xSIM产品部门', true, 4),
('车联网', '车联网业务部门', true, 5);

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO `users` (`username`, `password`, `real_name`, `email`, `phone_number`, `role`, `is_active`, `department`, `position`) VALUES
('admin', '$2a$10$f5cNgmkKUs7Oj9Ze/cqMGO1WIrO2HBjy9W0PfVcoVUzVK8NJ8jXAi', '系统管理员', 'admin@example.com', '13800138000', 'ADMIN', true, '运营商', '系统管理员'),
('manager', '$2a$10$f5cNgmkKUs7Oj9Ze/cqMGO1WIrO2HBjy9W0PfVcoVUzVK8NJ8jXAi', '项目经理', 'manager@example.com', '13800138001', 'MANAGER', true, '创新业务', '项目经理'),
('tester1', '$2a$10$f5cNgmkKUs7Oj9Ze/cqMGO1WIrO2HBjy9W0PfVcoVUzVK8NJ8jXAi', '测试工程师1', 'tester1@example.com', '13800138002', 'TESTER', true, 'RedteaReady', '测试工程师'),
('tester2', '$2a$10$f5cNgmkKUs7Oj9Ze/cqMGO1WIrO2HBjy9W0PfVcoVUzVK8NJ8jXAi', '测试工程师2', 'tester2@example.com', '13800138003', 'TESTER', true, 'xSIM', '测试工程师');

-- 插入示例测试任务
INSERT INTO `test_tasks` (`task_name`, `task_description`, `start_date`, `expected_end_date`, `participant_count`, `man_days`, `task_status`, `priority`, `progress_percentage`, `risk_level`, `project_name`, `module_name`, `test_type`, `assigned_to`, `created_by_user`, `department`) VALUES
('用户登录功能测试', '测试用户登录、注册、密码重置等功能', '2024-01-01', '2024-01-05', 2, 10.0, 'IN_PROGRESS', 'HIGH', 60, 'MEDIUM', '用户系统', '认证模块', 'FUNCTIONAL', 3, 1, '运营商'),
('订单系统性能测试', '测试订单创建、查询、支付等功能的性能', '2024-01-02', '2024-01-08', 3, 18.0, 'PLANNED', 'MEDIUM', 0, 'LOW', '电商系统', '订单模块', 'PERFORMANCE', 4, 2, '创新业务'),
('支付接口安全测试', '测试支付接口的安全性，包括SQL注入、XSS等', '2023-12-25', '2023-12-30', 2, 10.0, 'COMPLETED', 'URGENT', 100, 'HIGH', '支付系统', '支付模块', 'SECURITY', 3, 1, 'RedteaReady'),
('移动端兼容性测试', '测试在不同移动设备上的兼容性', '2023-12-20', '2023-12-28', 2, 16.0, 'ON_HOLD', 'MEDIUM', 30, 'MEDIUM', '移动应用', 'UI模块', 'COMPATIBILITY', 4, 2, 'xSIM');

-- 插入任务进度记录
INSERT INTO `task_progress` (`task_id`, `updated_by_user`, `progress_percentage`, `progress_notes`, `update_time`, `task_status`, `risk_level`) VALUES
(1, 3, 30, '完成登录功能测试用例编写', '2024-01-02 10:00:00', 'IN_PROGRESS', 'LOW'),
(1, 3, 60, '完成注册功能测试，发现2个bug', '2024-01-03 15:30:00', 'IN_PROGRESS', 'MEDIUM'),
(3, 3, 100, '安全测试完成，发现1个高危漏洞已修复', '2023-12-30 16:00:00', 'COMPLETED', 'LOW'),
(4, 4, 30, '完成iOS设备测试，Android设备测试暂停', '2023-12-25 14:20:00', 'ON_HOLD', 'MEDIUM');

-- 插入示例通知
INSERT INTO `notifications` (`title`, `content`, `notification_type`, `priority`, `is_read`, `recipient_id`, `related_task_id`, `related_entity_type`, `related_entity_id`, `action_url`) VALUES
('任务超时提醒', '任务「移动端兼容性测试」已超时5天，请及时处理', 'TASK_OVERDUE', 'HIGH', false, 4, 4, 'TestTask', 4, '/tasks/4'),
('任务完成通知', '任务「支付接口安全测试」已完成', 'TASK_COMPLETED', 'NORMAL', false, 3, 3, 'TestTask', 3, '/tasks/3'),
('进度更新通知', '任务「用户登录功能测试」进度已更新为60%', 'TASK_PROGRESS_UPDATE', 'LOW', false, 3, 1, 'TestTask', 1, '/tasks/1');

-- 插入登录历史
INSERT INTO `login_history` (`user_id`, `login_time`, `ip_address`, `user_agent`, `status`) VALUES
(1, NOW(), '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS'),
(2, DATE_SUB(NOW(), INTERVAL 1 DAY), '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'SUCCESS'),
(3, DATE_SUB(NOW(), INTERVAL 2 DAY), '192.168.1.102', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS'),
(1, DATE_SUB(NOW(), INTERVAL 3 DAY), '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 'SUCCESS');

-- 插入系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('system.name', '测试任务跟踪系统', '系统名称'),
('system.version', '1.0.0', '系统版本'),
('system.maintenance', 'false', '系统维护模式'),
('email.enabled', 'false', '邮件通知开关'),
('dingtalk.enabled', 'false', '钉钉通知开关'),
('dingtalk.webhook', '', '钉钉Webhook地址'),
('dingtalk.secret', '', '钉钉签名密钥'),
('jwt.expiration', '86400000', 'JWT过期时间(毫秒)'),
('task.auto_assign', 'true', '任务自动分配开关'),
('alert.retention_days', '30', '告警保留天数');

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 显示初始化完成信息
SELECT 'Database initialization completed successfully!' as message; 