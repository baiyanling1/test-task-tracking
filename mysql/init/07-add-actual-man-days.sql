-- 添加实际工时字段的数据库迁移脚本
-- 创建时间: 2024年
-- 描述: 为test_tasks和task_progress表添加actual_man_days字段

USE test_tracking;

-- 为test_tasks表添加actual_man_days字段
ALTER TABLE `test_tasks` 
ADD COLUMN `actual_man_days` double DEFAULT NULL COMMENT '实际人天' 
AFTER `man_days`;

-- 为task_progress表添加actual_man_days字段
ALTER TABLE `task_progress` 
ADD COLUMN `actual_man_days` double DEFAULT NULL COMMENT '实际人天' 
AFTER `actual_end_date`;

-- 更新现有记录的注释
ALTER TABLE `test_tasks` 
MODIFY COLUMN `man_days` double DEFAULT NULL COMMENT '预计人天';

-- 显示更新结果
SELECT 'Database migration completed successfully!' as message;
