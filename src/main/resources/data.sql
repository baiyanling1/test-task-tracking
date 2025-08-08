-- 初始化管理员用户
-- 密码: admin123 (BCrypt加密后的值)
INSERT INTO users (username, password, real_name, email, role, is_active, department, position, created_time, updated_time) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '系统管理员', 'admin@example.com', 'ADMIN', true, '技术部', '系统管理员', NOW(), NOW());

-- 初始化项目经理用户
-- 密码: manager123
INSERT INTO users (username, password, real_name, email, role, is_active, department, position, created_time, updated_time) 
VALUES ('manager', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOe6g7fKjYqGxHxKqKqKqKqKqKqKqKqKq', '项目经理', 'manager@example.com', 'MANAGER', true, '测试部', '项目经理', NOW(), NOW());

-- 初始化测试人员用户
-- 密码: tester123
INSERT INTO users (username, password, real_name, email, role, is_active, department, position, created_time, updated_time) 
VALUES ('tester1', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOe6g7fKjYqGxHxKqKqKqKqKqKqKqKqKq', '测试工程师1', 'tester1@example.com', 'TESTER', true, '测试部', '测试工程师', NOW(), NOW());

INSERT INTO users (username, password, real_name, email, role, is_active, department, position, created_time, updated_time) 
VALUES ('tester2', '$2a$10$8K1p/a0dL1LXMIgoEDFrwOe6g7fKjYqGxHxKqKqKqKqKqKqKqKqKq', '测试工程师2', 'tester2@example.com', 'TESTER', true, '测试部', '测试工程师', NOW(), NOW());

-- 初始化示例测试任务
INSERT INTO test_tasks (task_name, task_description, start_date, expected_end_date, participant_count, man_days, task_status, priority, progress_percentage, risk_level, project_name, module_name, test_type, assigned_to, created_by_user, is_overdue, overdue_days, department, created_time, updated_time) 
VALUES ('用户登录功能测试', '测试用户登录、注册、密码重置等功能', '2024-01-01', '2024-01-05', 2, 10.0, 'IN_PROGRESS', 'HIGH', 60, 'MEDIUM', '用户系统', '认证模块', 'FUNCTIONAL', 3, 1, false, 0, '运营商', NOW(), NOW());

INSERT INTO test_tasks (task_name, task_description, start_date, expected_end_date, participant_count, man_days, task_status, priority, progress_percentage, risk_level, project_name, module_name, test_type, assigned_to, created_by_user, is_overdue, overdue_days, department, created_time, updated_time) 
VALUES ('订单系统性能测试', '测试订单创建、查询、支付等功能的性能', '2024-01-02', '2024-01-08', 3, 18.0, 'PLANNED', 'MEDIUM', 0, 'LOW', '电商系统', '订单模块', 'PERFORMANCE', 4, 2, false, 0, '创新业务', NOW(), NOW());

INSERT INTO test_tasks (task_name, task_description, start_date, expected_end_date, participant_count, man_days, task_status, priority, progress_percentage, risk_level, project_name, module_name, test_type, assigned_to, created_by_user, is_overdue, overdue_days, department, created_time, updated_time) 
VALUES ('支付接口安全测试', '测试支付接口的安全性，包括SQL注入、XSS等', '2023-12-25', '2023-12-30', 2, 10.0, 'COMPLETED', 'HIGH', 100, 'HIGH', '支付系统', '支付模块', 'SECURITY', 3, 1, false, 0, 'RedteaReady', NOW(), NOW());

INSERT INTO test_tasks (task_name, task_description, start_date, expected_end_date, participant_count, man_days, task_status, priority, progress_percentage, risk_level, project_name, module_name, test_type, assigned_to, created_by_user, is_overdue, overdue_days, department, created_time, updated_time) 
VALUES ('移动端兼容性测试', '测试在不同移动设备上的兼容性', '2023-12-20', '2023-12-28', 2, 16.0, 'ON_HOLD', 'MEDIUM', 30, 'MEDIUM', '移动应用', 'UI模块', 'COMPATIBILITY', 4, 2, true, 5, 'xSIM', NOW(), NOW());

-- 初始化任务进度记录
INSERT INTO task_progress (task_id, updated_by_user, progress_percentage, progress_notes, update_time, task_status, created_time) 
VALUES (1, 3, 30, '完成登录功能测试用例编写', '2024-01-02 10:00:00', 'IN_PROGRESS', NOW());

INSERT INTO task_progress (task_id, updated_by_user, progress_percentage, progress_notes, update_time, task_status, created_time) 
VALUES (1, 3, 60, '完成注册功能测试，发现2个bug', '2024-01-03 15:30:00', 'IN_PROGRESS', NOW());

INSERT INTO task_progress (task_id, updated_by_user, progress_percentage, progress_notes, update_time, task_status, created_time) 
VALUES (3, 3, 100, '安全测试完成，发现1个高危漏洞已修复', '2023-12-30 16:00:00', 'COMPLETED', NOW());

INSERT INTO task_progress (task_id, updated_by_user, progress_percentage, progress_notes, update_time, task_status, created_time) 
VALUES (4, 4, 30, '完成iOS设备测试，Android设备测试暂停', '2023-12-25 14:20:00', 'ON_HOLD', NOW());

-- 初始化示例通知
INSERT INTO notifications (title, content, notification_type, priority, is_read, recipient_id, related_task_id, related_entity_type, related_entity_id, action_url, created_time, updated_time) 
VALUES ('任务超时提醒', '任务「移动端兼容性测试」已超时5天，请及时处理', 'TASK_OVERDUE', 'HIGH', false, 4, 4, 'TestTask', 4, '/tasks/4', NOW(), NOW());

INSERT INTO notifications (title, content, notification_type, priority, is_read, recipient_id, related_task_id, related_entity_type, related_entity_id, action_url, created_time, updated_time) 
VALUES ('任务完成通知', '任务「支付接口安全测试」已完成', 'TASK_COMPLETED', 'NORMAL', false, 3, 3, 'TestTask', 3, '/tasks/3', NOW(), NOW());

INSERT INTO notifications (title, content, notification_type, priority, is_read, recipient_id, related_task_id, related_entity_type, related_entity_id, action_url, created_time, updated_time) 
VALUES ('进度更新通知', '任务「用户登录功能测试」进度已更新为60%', 'TASK_PROGRESS_UPDATE', 'LOW', false, 3, 1, 'TestTask', 1, '/tasks/1', NOW(), NOW()); 