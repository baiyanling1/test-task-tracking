-- 子任务功能迁移测试脚本
-- 用于验证 14-add-subtask-support.sql 迁移脚本的可行性

USE test_tracking;

-- ========================================
-- 1. 检查迁移前的表结构
-- ========================================
SELECT '=== 迁移前的表结构检查 ===' as info;

-- 检查 test_tasks 表是否存在子任务相关字段
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'test_tracking' 
  AND TABLE_NAME = 'test_tasks'
  AND COLUMN_NAME IN ('parent_task_id', 'task_level', 'sub_task_order', 'auto_progress_calculation', 'subtask_weight');

-- 检查现有任务数据
SELECT '=== 迁移前的任务数据 ===' as info;
SELECT 
    id, 
    task_name, 
    task_status, 
    assigned_to, 
    created_by_user
FROM test_tasks 
ORDER BY id 
LIMIT 5;

-- ========================================
-- 2. 模拟执行迁移脚本（仅检查语法）
-- ========================================
SELECT '=== 准备执行迁移脚本 ===' as info;

-- 检查外键约束状态
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'test_tracking' 
  AND TABLE_NAME = 'test_tasks'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ========================================
-- 3. 验证迁移脚本的安全性
-- ========================================
SELECT '=== 验证迁移脚本安全性 ===' as info;

-- 检查是否存在会冲突的外键约束
SELECT 
    CONSTRAINT_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = 'test_tracking' 
  AND TABLE_NAME = 'test_tasks'
  AND CONSTRAINT_TYPE = 'FOREIGN KEY'
  AND CONSTRAINT_NAME = 'fk_parent_task';

-- 检查是否存在会冲突的索引
SELECT 
    INDEX_NAME
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'test_tracking' 
  AND TABLE_NAME = 'test_tasks'
  AND INDEX_NAME IN ('idx_parent_task_id', 'idx_task_level', 'idx_sub_task_order');

-- ========================================
-- 4. 预测迁移结果
-- ========================================
SELECT '=== 预测迁移结果 ===' as info;

-- 统计当前任务数量
SELECT 
    '当前任务总数' as metric,
    COUNT(*) as count
FROM test_tasks
UNION ALL
SELECT 
    '将被标记为主任务的数量' as metric,
    COUNT(*) as count
FROM test_tasks;

-- ========================================
-- 5. 检查依赖关系
-- ========================================
SELECT '=== 检查表依赖关系 ===' as info;

-- 检查引用 test_tasks 的外键
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'test_tracking' 
  AND REFERENCED_TABLE_NAME = 'test_tasks'
  AND REFERENCED_COLUMN_NAME = 'id';

-- ========================================
-- 6. 生成迁移建议
-- ========================================
SELECT '=== 迁移建议 ===' as info;

SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = 'test_tracking' 
              AND TABLE_NAME = 'test_tasks'
              AND COLUMN_NAME = 'parent_task_id'
        ) THEN '⚠️  parent_task_id 字段已存在，请检查是否需要更新'
        ELSE '✅ parent_task_id 字段不存在，可以安全添加'
    END as parent_task_id_status
UNION ALL
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = 'test_tracking' 
              AND TABLE_NAME = 'test_tasks'
              AND COLUMN_NAME = 'task_level'
        ) THEN '⚠️  task_level 字段已存在，请检查是否需要更新'
        ELSE '✅ task_level 字段不存在，可以安全添加'
    END as task_level_status
UNION ALL
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
            WHERE TABLE_SCHEMA = 'test_tracking' 
              AND TABLE_NAME = 'test_tasks'
              AND CONSTRAINT_NAME = 'fk_parent_task'
        ) THEN '⚠️  fk_parent_task 约束已存在，可能会冲突'
        ELSE '✅ fk_parent_task 约束不存在，可以安全添加'
    END as foreign_key_status;

-- ========================================
-- 7. 备份建议
-- ========================================
SELECT '=== 备份建议 ===' as info;

SELECT 
    CONCAT(
        '建议在执行迁移前备份以下数据：\n',
        '1. test_tasks 表: ', (SELECT COUNT(*) FROM test_tasks), ' 条记录\n',
        '2. task_progress 表: ', (SELECT COUNT(*) FROM task_progress), ' 条记录\n',
        '3. notifications 表: ', (SELECT COUNT(*) FROM notifications), ' 条记录\n',
        '执行命令：mysqldump -u root -p test_tracking > backup_before_subtask_migration.sql'
    ) as backup_recommendation;
