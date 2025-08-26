-- 添加预期完成时间到达字段
ALTER TABLE test_tasks ADD COLUMN is_expected_completion_reached BOOLEAN DEFAULT FALSE;

-- 更新现有数据
UPDATE test_tasks 
SET is_expected_completion_reached = TRUE 
WHERE expected_end_date <= CURDATE() 
  AND status NOT IN ('COMPLETED', 'CANCELLED');

-- 更新超时状态
UPDATE test_tasks 
SET is_overdue = TRUE, 
    overdue_days = DATEDIFF(CURDATE(), expected_end_date)
WHERE expected_end_date < CURDATE() 
  AND status NOT IN ('COMPLETED', 'CANCELLED');

-- 更新已完成任务的超时状态（基于实际结束时间）
UPDATE test_tasks 
SET is_overdue = CASE 
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
  END
WHERE status = 'COMPLETED' 
  AND actual_end_date IS NOT NULL;
