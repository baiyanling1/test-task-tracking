-- 检查任务层级字段是否正确添加
USE `test_tracking`;

-- 检查字段是否存在
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'test_tasks'
    AND COLUMN_NAME IN ('parent_task_id', 'task_type');

-- 检查索引是否存在
SHOW INDEX FROM `test_tasks` WHERE Key_name IN ('idx_parent_task_id', 'idx_task_type');

-- 查看当前任务数据
SELECT 
    id,
    task_name,
    parent_task_id,
    task_type,
    status,
    priority
FROM test_tasks 
ORDER BY created_time DESC 
LIMIT 10;

-- 统计任务类型分布
SELECT 
    task_type,
    COUNT(*) as count
FROM test_tasks 
GROUP BY task_type;

-- 统计有父任务的任务数量
SELECT 
    COUNT(*) as tasks_with_parent
FROM test_tasks 
WHERE parent_task_id IS NOT NULL;
