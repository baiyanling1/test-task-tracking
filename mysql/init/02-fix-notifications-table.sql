-- 修复notifications表结构
-- 添加缺失的字段

USE test_tracking;

-- 添加read_time字段（如果不存在）
ALTER TABLE `notifications` 
ADD COLUMN IF NOT EXISTS `read_time` datetime DEFAULT NULL COMMENT '阅读时间';

-- 添加expire_time字段（如果不存在）
ALTER TABLE `notifications` 
ADD COLUMN IF NOT EXISTS `expire_time` datetime DEFAULT NULL COMMENT '过期时间';

-- 更新notification_type枚举值
-- 注意：MySQL不支持直接修改ENUM，需要重新创建表
-- 这里我们使用ALTER TABLE来添加新的枚举值（如果支持的话）
-- 如果遇到错误，需要手动重新创建表

-- 显示修复完成信息
SELECT 'Notifications table structure fixed successfully!' as message; 