# 数据库问题排查和解决方案

## 问题1: 外键约束失败

### 错误信息
```
SQL错误 [1452] [23000]: Cannot add or update a child row: a foreign key constraint fails (`test_tracking`.`test_tasks`, CONSTRAINT `fk_task_assigned_to` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`) ON DELETE SET NULL)
```

### 原因
插入的任务数据中的 `assigned_to` 和 `created_by_user` 字段引用了不存在的用户ID。生产数据中包含的用户ID（如3, 7, 14, 21等）在测试脚本中只创建了4个用户（ID: 1, 2, 3, 4）。

### 解决方案
有两种解决方法：

#### 方法1: 创建缺失的用户数据
在插入任务数据前，先创建所有需要的用户：

```sql
-- 创建缺失的用户（根据实际需要的ID创建）
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `email`, `role`, `is_active`, `department`) VALUES
(5, 'user5', '$2a$10$f5cNgmkKUs7Oj9Ze/cqMGO1WIrO2HBjy9W0PfVcoVUzVK8NJ8jXAi', '用户5', 'user5@example.com', 'TESTER', true, '运营商'),
(7, 'user7', '$2a$10$f5cNgmkKUs7Oj9Ze/cqMGO1WIrO2HBjy9W0PfVcoVUzVK8NJ8jXAi', '用户7', 'user7@example.com', 'TESTER', true, '运营商'),
-- ... 继续添加其他需要的用户ID
;
```

#### 方法2: 暂时禁用外键检查
```sql
SET FOREIGN_KEY_CHECKS = 0;
-- 插入数据
-- ...
SET FOREIGN_KEY_CHECKS = 1;
```

## 问题2: ScheduledTask枚举错误

### 错误信息
```
No enum constant com.testtracking.entity.ScheduledTask.TaskStatus.
```

### 原因
数据库中的 `scheduled_tasks` 表的 `status` 字段包含空字符串值，而Java枚举 `TaskStatus` 无法处理空字符串。

### 根本原因
`pre-subtask-complete-init.sql` 中的 `scheduled_tasks` 表结构与Java实体类不匹配：
- 缺少 `status` 字段（枚举类型）
- 缺少 `bean_name` 和 `method_name` 字段
- 字段名不一致（`is_enabled` vs `enabled`）

### 解决方案

#### 步骤1: 修复现有数据库
运行修复脚本：
```bash
mysql -u root -p test_tracking < mysql/init/fix-scheduled-task-status.sql
```

#### 步骤2: 使用更新后的初始化脚本
使用修复后的 `pre-subtask-complete-init.sql`，它包含了正确的表结构：

```sql
CREATE TABLE `scheduled_tasks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_description` varchar(255) DEFAULT NULL COMMENT '任务描述',
  `cron_expression` varchar(50) NOT NULL COMMENT 'Cron表达式',
  `bean_name` varchar(100) NOT NULL DEFAULT 'scheduledTaskService' COMMENT 'Spring Bean名称',
  `method_name` varchar(100) NOT NULL DEFAULT 'executeTask' COMMENT '方法名称',
  `enabled` boolean NOT NULL DEFAULT true COMMENT '是否启用',
  `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `last_execute_result` varchar(20) DEFAULT NULL COMMENT '上次执行结果',
  `status` enum('ENABLED','DISABLED','RUNNING','ERROR') NOT NULL DEFAULT 'ENABLED' COMMENT '任务状态',
  `execute_count` bigint(20) DEFAULT 0 COMMENT '执行次数',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_name` (`task_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';
```

## 问题3: 数据库结构与实体类不匹配

### 检查方法
1. 比较数据库表结构与Java实体类
2. 确保字段名、类型、约束完全一致
3. 特别注意枚举字段的值范围

### 预防措施
1. 使用数据库迁移脚本管理结构变更
2. 在实体类变更后及时更新初始化脚本
3. 定期验证开发、测试、生产环境的结构一致性

## 验证步骤

### 1. 验证表结构
```sql
DESCRIBE scheduled_tasks;
DESCRIBE test_tasks;
DESCRIBE users;
```

### 2. 验证数据完整性
```sql
-- 检查用户数据
SELECT id, username, real_name FROM users ORDER BY id;

-- 检查定时任务状态
SELECT task_name, status, enabled FROM scheduled_tasks;

-- 检查外键引用
SELECT COUNT(*) as orphaned_tasks FROM test_tasks t 
LEFT JOIN users u ON t.assigned_to = u.id 
WHERE t.assigned_to IS NOT NULL AND u.id IS NULL;
```

### 3. 验证应用启动
```bash
# 启动后端服务
java -jar target/test-task-tracking.jar

# 检查启动日志，确保没有数据库相关错误
```

## 最佳实践

1. **环境一致性**: 确保开发、测试、生产环境使用相同的数据库结构
2. **版本控制**: 所有数据库变更都应该有对应的迁移脚本
3. **测试验证**: 在应用代码变更前先验证数据库兼容性
4. **备份策略**: 执行结构变更前必须备份数据
5. **渐进式迁移**: 大型结构变更应该分步骤执行，确保每步都可以回滚
