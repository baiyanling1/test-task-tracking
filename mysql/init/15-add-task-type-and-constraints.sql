-- 添加任务类型和约束优化 - 数据库迁移脚本
-- 创建时间: 2024年
-- 描述: 优化子任务功能，添加任务类型和时间约束

USE test_tracking;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 为任务表添加任务类型字段
ALTER TABLE test_tasks 
ADD COLUMN task_type ENUM('REQUIREMENT', 'VERSION') DEFAULT 'REQUIREMENT' COMMENT '任务类型：需求测试/版本测试';

-- 更新现有数据：将有子任务的主任务标记为版本测试
UPDATE test_tasks 
SET task_type = 'VERSION' 
WHERE task_level = 'MAIN' 
  AND EXISTS (SELECT 1 FROM test_tasks sub WHERE sub.parent_task_id = test_tasks.id);

-- 更新没有子任务的主任务为需求测试
UPDATE test_tasks 
SET task_type = 'REQUIREMENT' 
WHERE task_level = 'MAIN' 
  AND NOT EXISTS (SELECT 1 FROM test_tasks sub WHERE sub.parent_task_id = test_tasks.id);

-- 子任务继承父任务的类型
UPDATE test_tasks sub
SET task_type = (
    SELECT parent.task_type 
    FROM test_tasks parent 
    WHERE parent.id = sub.parent_task_id
)
WHERE sub.task_level = 'SUB' AND sub.parent_task_id IS NOT NULL;

-- 添加索引优化查询性能
CREATE INDEX idx_task_type ON test_tasks(task_type);

-- 添加约束：只有版本测试类型的主任务才能有子任务
-- 这个约束通过应用层控制，数据库层面不强制

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 显示迁移完成信息
SELECT 'Task type and constraints migration completed successfully!' as message;
SELECT 
    task_type,
    task_level,
    COUNT(*) as count
FROM test_tasks 
GROUP BY task_type, task_level
ORDER BY task_type, task_level;
