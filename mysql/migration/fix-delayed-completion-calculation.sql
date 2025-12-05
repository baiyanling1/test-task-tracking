-- 修复延期完成状态计算错误的数据
-- 问题：实际结束时间 = 预计结束时间时，被错误标记为延期完成

-- 1. 修复：当实际结束时间 <= 预计结束时间时，不应该是延期完成
UPDATE test_task 
SET 
    is_overdue = FALSE,
    overdue_days = 0,
    is_delayed_completion = FALSE
WHERE 
    actual_end_date IS NOT NULL 
    AND expected_end_date IS NOT NULL
    AND actual_end_date <= expected_end_date
    AND (is_overdue = TRUE OR is_delayed_completion = TRUE OR overdue_days > 0);

-- 2. 修复：当实际结束时间 > 预计结束时间时，正确计算延期天数
UPDATE test_task 
SET 
    is_overdue = TRUE,
    overdue_days = DATEDIFF(actual_end_date, expected_end_date),
    is_delayed_completion = TRUE
WHERE 
    actual_end_date IS NOT NULL 
    AND expected_end_date IS NOT NULL
    AND actual_end_date > expected_end_date;

-- 3. 查看修复结果
SELECT 
    id,
    task_name,
    expected_end_date,
    actual_end_date,
    is_overdue,
    overdue_days,
    is_delayed_completion,
    CASE 
        WHEN actual_end_date > expected_end_date THEN '延期完成'
        WHEN actual_end_date = expected_end_date THEN '按时完成'
        WHEN actual_end_date < expected_end_date THEN '提前完成'
        ELSE '未完成'
    END AS calculated_status
FROM test_task 
WHERE actual_end_date IS NOT NULL
ORDER BY id DESC
LIMIT 20;

