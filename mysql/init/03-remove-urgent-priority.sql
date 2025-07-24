-- 删除紧急优先级，将现有的紧急优先级更新为高优先级
USE test_tracking;

-- 更新任务表中的URGENT优先级为HIGH
UPDATE test_tasks SET priority = 'HIGH' WHERE priority = 'URGENT';

-- 更新通知表中的URGENT优先级为HIGH
UPDATE notifications SET priority = 'HIGH' WHERE priority = 'URGENT';

-- 验证更新结果
SELECT 'Tasks table priority distribution:' as info;
SELECT priority, COUNT(*) as count FROM test_tasks GROUP BY priority;

SELECT 'Notifications table priority distribution:' as info;
SELECT priority, COUNT(*) as count FROM notifications GROUP BY priority;

SELECT 'URGENT priority has been updated to HIGH successfully!' as message; 