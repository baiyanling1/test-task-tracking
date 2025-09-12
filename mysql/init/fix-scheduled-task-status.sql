-- 修复 scheduled_tasks 表中的空状态值
-- 这个问题导致应用启动时枚举转换失败

USE test_tracking;

-- 查看当前 scheduled_tasks 表中的状态值
SELECT 'Current status values in scheduled_tasks:' as info;
SELECT 
    id, 
    task_name, 
    status,
    CASE 
        WHEN status = '' THEN 'EMPTY STRING'
        WHEN status IS NULL THEN 'NULL'
        ELSE status
    END as status_description
FROM scheduled_tasks;

-- 修复空字符串状态为 ENABLED
UPDATE scheduled_tasks 
SET status = 'ENABLED' 
WHERE status = '' OR status IS NULL;

-- 确保所有状态值都是有效的枚举值
UPDATE scheduled_tasks 
SET status = 'ENABLED' 
WHERE status NOT IN ('ENABLED', 'DISABLED', 'RUNNING', 'ERROR');

-- 验证修复结果
SELECT 'Fixed status values:' as info;
SELECT 
    id, 
    task_name, 
    status
FROM scheduled_tasks;

-- 显示修复完成信息
SELECT 'Scheduled task status values have been fixed!' as message;
