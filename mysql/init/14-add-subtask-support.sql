-- 添加子任务支持 - 数据库迁移脚本
-- 创建时间: 2024年
-- 描述: 为现有任务系统添加主任务-子任务层级支持

USE test_tracking;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 为现有任务表添加层级支持字段
ALTER TABLE test_tasks 
ADD COLUMN parent_task_id BIGINT DEFAULT NULL COMMENT '父任务ID（主任务）',
ADD COLUMN task_level ENUM('MAIN', 'SUB') DEFAULT 'MAIN' COMMENT '任务层级：主任务/子任务',
ADD COLUMN sub_task_order INT DEFAULT 0 COMMENT '子任务排序',
ADD COLUMN auto_progress_calculation BOOLEAN DEFAULT FALSE COMMENT '是否自动计算进度（主任务用）',
ADD COLUMN subtask_weight DECIMAL(5,2) DEFAULT 1.00 COMMENT '子任务权重（用于进度计算）';

-- 添加外键约束
ALTER TABLE test_tasks 
ADD CONSTRAINT fk_parent_task 
FOREIGN KEY (parent_task_id) REFERENCES test_tasks(id) ON DELETE CASCADE;

-- 添加索引优化查询性能
CREATE INDEX idx_parent_task_id ON test_tasks(parent_task_id);
CREATE INDEX idx_task_level ON test_tasks(task_level);
CREATE INDEX idx_sub_task_order ON test_tasks(sub_task_order);

-- 将现有任务标记为主任务
UPDATE test_tasks 
SET task_level = 'MAIN', 
    auto_progress_calculation = FALSE,
    subtask_weight = 1.00,
    sub_task_order = 0
WHERE task_level IS NULL OR task_level = '';

-- 添加预期结束时间到达字段（如果不存在）
ALTER TABLE test_tasks 
ADD COLUMN IF NOT EXISTS is_expected_completion_reached BOOLEAN DEFAULT FALSE COMMENT '是否已到预期完成时间';

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 显示迁移完成信息
SELECT 'Subtask support migration completed successfully!' as message;
SELECT COUNT(*) as total_main_tasks FROM test_tasks WHERE task_level = 'MAIN';
