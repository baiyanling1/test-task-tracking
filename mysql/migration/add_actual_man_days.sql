-- 添加实际工时字段到测试任务表
-- 如果字段不存在则添加
ALTER TABLE `test_tasks` 
ADD COLUMN IF NOT EXISTS `actual_man_days` double DEFAULT NULL COMMENT '实际人天' AFTER `man_days`;

-- 添加实际工时字段到任务进度表
-- 如果字段不存在则添加
ALTER TABLE `task_progress` 
ADD COLUMN IF NOT EXISTS `actual_man_days` double DEFAULT NULL COMMENT '实际人天' AFTER `actual_end_date`;

-- 更新现有任务的注释，将原来的man_days字段注释改为"预计人天"
ALTER TABLE `test_tasks` 
MODIFY COLUMN `man_days` double DEFAULT NULL COMMENT '预计人天';

-- 验证字段是否添加成功
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME IN ('test_tasks', 'task_progress')
    AND COLUMN_NAME = 'actual_man_days';
