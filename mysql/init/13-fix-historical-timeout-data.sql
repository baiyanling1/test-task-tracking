-- 修复历史数据的超时状态和超时时间计算
-- 新的业务逻辑：
-- 1. 有实际结束时间：根据实际结束时间与预期结束时间比较判断超时
-- 2. 没有实际结束时间但已到预期结束日期：标记为超预期（超时）
-- 3. 没有实际结束时间且未到预期结束日期：不超时

-- 首先，重置所有任务的超时相关字段为默认值
UPDATE test_tasks SET 
    is_overdue = FALSE,
    overdue_days = 0,
    is_delayed_completion = FALSE,
    is_expected_completion_reached = FALSE;

-- 1. 对有实际结束时间的任务，根据实际结束时间与预期结束时间比较
UPDATE test_tasks 
SET 
    is_overdue = CASE 
        WHEN actual_end_date > expected_end_date THEN TRUE 
        ELSE FALSE 
    END,
    overdue_days = CASE 
        WHEN actual_end_date > expected_end_date THEN DATEDIFF(actual_end_date, expected_end_date)
        ELSE 0 
    END,
    is_delayed_completion = CASE 
        WHEN actual_end_date > expected_end_date THEN TRUE 
        ELSE FALSE 
    END,
    is_expected_completion_reached = TRUE
WHERE actual_end_date IS NOT NULL 
    AND expected_end_date IS NOT NULL;

-- 2. 对没有实际结束时间但已超过预期结束日期的任务，标记为超预期
UPDATE test_tasks 
SET 
    is_overdue = TRUE,
    overdue_days = DATEDIFF(CURDATE(), expected_end_date),
    is_delayed_completion = FALSE,
    is_expected_completion_reached = TRUE
WHERE actual_end_date IS NULL 
    AND expected_end_date IS NOT NULL 
    AND expected_end_date < CURDATE();

-- 3. 对没有实际结束时间且今天是预期结束日期的任务
UPDATE test_tasks 
SET 
    is_overdue = FALSE,
    overdue_days = 0,
    is_delayed_completion = FALSE,
    is_expected_completion_reached = TRUE
WHERE actual_end_date IS NULL 
    AND expected_end_date IS NOT NULL 
    AND expected_end_date = CURDATE();

-- 4. 对没有实际结束时间且还未到预期结束日期的任务
UPDATE test_tasks 
SET 
    is_overdue = FALSE,
    overdue_days = 0,
    is_delayed_completion = FALSE,
    is_expected_completion_reached = FALSE
WHERE actual_end_date IS NULL 
    AND expected_end_date IS NOT NULL 
    AND expected_end_date > CURDATE();

-- 5. 处理没有预期结束时间的异常数据
UPDATE test_tasks 
SET 
    is_overdue = FALSE,
    overdue_days = 0,
    is_delayed_completion = FALSE,
    is_expected_completion_reached = FALSE
WHERE expected_end_date IS NULL;

-- 5. 更新所有任务的更新时间
UPDATE test_tasks 
SET updated_time = CURRENT_TIMESTAMP 
WHERE 1=1;

-- 输出修复结果统计
SELECT 
    '修复完成统计' AS info,
    COUNT(*) AS total_tasks,
    SUM(CASE WHEN is_overdue = TRUE THEN 1 ELSE 0 END) AS overdue_tasks,
    SUM(CASE WHEN is_delayed_completion = TRUE THEN 1 ELSE 0 END) AS delayed_completion_tasks,
    SUM(CASE WHEN is_expected_completion_reached = TRUE THEN 1 ELSE 0 END) AS expected_completion_reached_tasks
FROM test_tasks;

-- 按状态分组显示修复结果
SELECT 
    task_status AS task_status,
    COUNT(*) AS task_count,
    SUM(CASE WHEN is_overdue = TRUE THEN 1 ELSE 0 END) AS overdue_count,
    SUM(CASE WHEN is_delayed_completion = TRUE THEN 1 ELSE 0 END) AS delayed_completion_count,
    SUM(CASE WHEN is_expected_completion_reached = TRUE THEN 1 ELSE 0 END) AS expected_completion_reached_count
FROM test_tasks 
GROUP BY task_status 
ORDER BY task_status;
