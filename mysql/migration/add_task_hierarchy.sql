-- 使用指定的数据库
USE `test_tracking`;

-- 添加任务层级关系字段
-- 如果字段不存在则添加
ALTER TABLE `test_tasks`
ADD COLUMN IF NOT EXISTS `parent_task_id` bigint DEFAULT NULL COMMENT '父任务ID，null表示顶级任务（版本）' AFTER `id`;

ALTER TABLE `test_tasks`
ADD COLUMN IF NOT EXISTS `task_type` varchar(20) DEFAULT 'REQUIREMENT' COMMENT '任务类型：VERSION（版本）或 REQUIREMENT（需求）' AFTER `parent_task_id`;

-- 添加索引以提高查询性能
ALTER TABLE `test_tasks`
ADD INDEX IF NOT EXISTS `idx_parent_task_id` (`parent_task_id`);

ALTER TABLE `test_tasks`
ADD INDEX IF NOT EXISTS `idx_task_type` (`task_type`);

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
    AND TABLE_NAME = 'test_tasks'
    AND COLUMN_NAME IN ('parent_task_id', 'task_type');

-- 显示索引信息
SHOW INDEX FROM `test_tasks` WHERE Key_name IN ('idx_parent_task_id', 'idx_task_type');
