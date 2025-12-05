-- ============================================
-- 任务层级结构支持（版本-需求）
-- ============================================

-- 1. 添加父任务ID（实现层级关系）
ALTER TABLE test_tasks ADD COLUMN parent_id BIGINT COMMENT '父任务ID（版本任务）';

-- 2. 添加任务类型（区分版本任务和普通任务）
ALTER TABLE test_tasks ADD COLUMN task_type VARCHAR(20) DEFAULT 'NORMAL' 
    COMMENT '任务类型: VERSION(版本)/REQUIREMENT(需求)/NORMAL(独立任务)';

-- 3. 添加版本号字段（用于版本任务）
ALTER TABLE test_tasks ADD COLUMN version_code VARCHAR(50) COMMENT '版本号，如 V3.1.0';

-- 4. 添加索引
ALTER TABLE test_tasks ADD INDEX idx_parent_id (parent_id);
ALTER TABLE test_tasks ADD INDEX idx_task_type (task_type);

-- 5. 添加外键约束（可选，自引用）
-- ALTER TABLE test_tasks ADD CONSTRAINT fk_parent_task 
--     FOREIGN KEY (parent_id) REFERENCES test_tasks(id) ON DELETE CASCADE;

-- 6. 更新现有数据为普通任务（如果需要，可手动执行）
-- 注意：由于 task_type 已设置 DEFAULT 'NORMAL'，新数据会自动为 NORMAL
-- 如需更新旧数据，请在上面的 ALTER 语句执行完成后再执行：
-- UPDATE test_tasks SET task_type = 'NORMAL' WHERE task_type IS NULL;

