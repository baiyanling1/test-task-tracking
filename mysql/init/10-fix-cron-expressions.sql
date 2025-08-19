-- 修复定时任务的cron表达式格式
-- 确保cron表达式符合Spring CronTrigger的要求

UPDATE scheduled_tasks 
SET cron_expression = '0 0 1 * * ?' 
WHERE task_name = 'checkOverdueTasks' 
AND cron_expression != '0 0 1 * * ?';

UPDATE scheduled_tasks 
SET cron_expression = '0 0 2 * * ?' 
WHERE task_name = 'cleanOldLoginHistory' 
AND cron_expression != '0 0 2 * * ?';

UPDATE scheduled_tasks 
SET cron_expression = '0 0 2 * * MON' 
WHERE task_name = 'scheduledBackup' 
AND cron_expression != '0 0 2 * * MON';

UPDATE scheduled_tasks 
SET cron_expression = '0 0 2 * * ?' 
WHERE task_name = 'deleteExpiredNotifications' 
AND cron_expression != '0 0 2 * * ?';

UPDATE scheduled_tasks 
SET cron_expression = '0 30 9 * * MON' 
WHERE task_name = 'checkFridayTaskTracking' 
AND cron_expression != '0 30 9 * * MON';

-- 显示修复后的结果
SELECT task_name, cron_expression, task_description 
FROM scheduled_tasks 
ORDER BY task_name;
