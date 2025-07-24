-- 修复现有任务的人天计算
USE test_tracking;

-- 更新所有任务的人天计算
UPDATE test_tasks 
SET man_days = (
    CASE 
        WHEN start_date IS NOT NULL AND expected_end_date IS NOT NULL AND participant_count IS NOT NULL 
        THEN (DATEDIFF(expected_end_date, start_date) + 1) * participant_count
        ELSE NULL
    END
)
WHERE man_days IS NULL OR man_days = 0;

-- 显示修复结果
SELECT 
    'Man days calculation fixed!' as message,
    COUNT(*) as total_tasks,
    COUNT(CASE WHEN man_days IS NOT NULL AND man_days > 0 THEN 1 END) as tasks_with_man_days,
    SUM(man_days) as total_man_days
FROM test_tasks;

-- 显示各状态的人天统计
SELECT 
    status,
    COUNT(*) as task_count,
    SUM(man_days) as total_man_days,
    AVG(man_days) as avg_man_days
FROM test_tasks 
WHERE man_days IS NOT NULL AND man_days > 0
GROUP BY status;

-- 显示修复前后的对比
SELECT 
    'Before fix' as period,
    COUNT(*) as total_tasks,
    COUNT(CASE WHEN man_days IS NOT NULL AND man_days > 0 THEN 1 END) as tasks_with_man_days,
    SUM(man_days) as total_man_days
FROM test_tasks; 