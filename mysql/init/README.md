# 数据库初始化说明

## 新环境部署使用

对于新环境部署，请直接使用 `00-complete-init.sql` 文件，它包含了所有功能的完整初始化：

```bash
mysql -u root -p < mysql/init/00-complete-init.sql
```

## 完整功能清单

### 核心表结构
- ✅ `users` - 用户表（包含角色权限）
- ✅ `departments` - 部门表
- ✅ `test_tasks` - 测试任务表（**包含子任务支持**）
- ✅ `task_progress` - 任务进度表
- ✅ `notifications` - 通知表
- ✅ `login_history` - 登录历史表
- ✅ `system_config` - 系统配置表
- ✅ `scheduled_tasks` - 定时任务表
- ✅ `scheduled_task_executions` - 定时任务执行历史表
- ✅ `alert_notification_config` - 告警通知配置表

### 子任务功能字段
在 `test_tasks` 表中包含以下子任务支持字段：
- `parent_task_id` - 父任务ID
- `task_level` - 任务层级（MAIN/SUB）
- `sub_task_order` - 子任务排序
- `auto_progress_calculation` - 是否自动计算进度
- `subtask_weight` - 子任务权重

### 重要字段更新
- ✅ 移除了URGENT优先级（只保留LOW/MEDIUM/HIGH）
- ✅ 添加了 `is_expected_completion_reached` 字段
- ✅ 添加了 `actual_man_days` 字段
- ✅ 所有表都包含标准的时间戳字段
- ✅ 正确的外键约束和索引

### 默认数据
- 5个部门（运营商、创新业务、RedteaReady、xSIM、车联网）
- 4个用户（admin、manager、tester1、tester2）密码均为: `admin123`
- 示例主任务和子任务数据
- 系统配置
- 告警配置
- 定时任务配置

## 现有环境升级

如果是现有环境需要升级，请按顺序执行迁移文件：
1. `02-fix-notifications-table.sql`
2. `03-remove-urgent-priority.sql`
3. `04-fix-man-days-calculation.sql`
4. `05-remove-task-progress-fields.sql`
5. `07-add-actual-man-days.sql`
6. `08-create-scheduled-tasks-tables.sql`
7. `10-add-default-whitelist.sql`
8. `10-fix-cron-expressions.sql`
9. `11-add-alert-notification-config.sql`
10. `12-add-expected-completion-reached.sql`
11. `12-update-task-statuses.sql`
12. `13-fix-historical-timeout-data.sql`
13. `14-add-subtask-support.sql` （**最新的子任务功能**）

## 验证部署成功

部署完成后，可以执行以下SQL验证：

```sql
-- 检查表结构
SHOW TABLES;

-- 检查子任务支持字段
DESCRIBE test_tasks;

-- 检查数据
SELECT 
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM test_tasks WHERE task_level = 'MAIN') as main_tasks,
    (SELECT COUNT(*) FROM test_tasks WHERE task_level = 'SUB') as sub_tasks;
```

## 注意事项

1. **字符集**: 所有表都使用 utf8mb4 字符集
2. **时区**: 建议设置为 Asia/Shanghai
3. **权限**: 确保应用用户有足够的数据库权限
4. **备份**: 部署前请备份现有数据
5. **子任务功能**: 新的层级任务管理系统完全向后兼容
