-- ========================================
-- 移除任务进度表中的未使用字段
-- ========================================

-- 移除风险等级字段
ALTER TABLE `task_progress` DROP COLUMN IF EXISTS `risk_level`;

-- 移除风险描述字段
ALTER TABLE `task_progress` DROP COLUMN IF EXISTS `risk_description`;

-- 移除阻碍因素字段
ALTER TABLE `task_progress` DROP COLUMN IF EXISTS `blockers`;

-- 移除下一步计划字段
ALTER TABLE `task_progress` DROP COLUMN IF EXISTS `next_steps`;
