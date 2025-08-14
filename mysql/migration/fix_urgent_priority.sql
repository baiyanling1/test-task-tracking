-- 使用指定的数据库
USE `test_tracking`;

-- 修复URGENT优先级问题
-- 将数据库中的URGENT优先级更新为HIGH
UPDATE `test_tasks` SET `priority` = 'HIGH' WHERE `priority` = 'URGENT';

-- 更新通知表中的URGENT优先级为HIGH
UPDATE `notifications` SET `priority` = 'HIGH' WHERE `priority` = 'URGENT';

-- 修改test_tasks表的priority字段定义，移除URGENT选项
ALTER TABLE `test_tasks` MODIFY COLUMN `priority` enum('LOW','MEDIUM','HIGH') DEFAULT 'MEDIUM' COMMENT '优先级';

-- 修改notifications表的priority字段定义，移除URGENT选项
ALTER TABLE `notifications` MODIFY COLUMN `priority` enum('LOW','NORMAL','HIGH') DEFAULT 'NORMAL' COMMENT '优先级';

-- 验证更新结果
SELECT 'URGENT priority has been updated to HIGH successfully!' as message;

-- 显示更新后的优先级分布
SELECT 'test_tasks priority distribution:' as info;
SELECT `priority`, COUNT(*) as count FROM `test_tasks` GROUP BY `priority`;

SELECT 'notifications priority distribution:' as info;
SELECT `priority`, COUNT(*) as count FROM `notifications` GROUP BY `priority`;
