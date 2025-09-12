# 子任务功能迁移验证指南

## 概述

本文档用于验证 `14-add-subtask-support.sql` 迁移脚本在现有数据库上的可行性。

## 验证步骤

### 1️⃣ 准备测试环境

```bash
# 1. 使用子任务功能之前的完整初始化脚本创建测试数据库
mysql -u root -p < mysql/init/pre-subtask-complete-init.sql

# 2. 验证基础数据
mysql -u root -p test_tracking -e "SELECT COUNT(*) as task_count FROM test_tasks;"
mysql -u root -p test_tracking -e "DESCRIBE test_tasks;" | grep -E "(parent_task_id|task_level|sub_task_order)"
```

**预期结果**: 
- 任务表应该有3条示例数据
- 不应该存在子任务相关字段

### 2️⃣ 执行迁移前检查

```bash
# 运行检查脚本
mysql -u root -p test_tracking < test-subtask-migration.sql
```

**检查项目**:
- ✅ 表结构完整性
- ✅ 外键约束状态  
- ✅ 索引冲突检查
- ✅ 数据依赖关系

### 3️⃣ 执行迁移脚本

```bash
# 备份现有数据
mysqldump -u root -p test_tracking > backup_before_subtask_migration.sql

# 执行子任务支持迁移
mysql -u root -p test_tracking < mysql/init/14-add-subtask-support.sql
```

### 4️⃣ 迁移后验证

```sql
-- 检查新增字段
DESCRIBE test_tasks;

-- 验证现有数据迁移
SELECT 
    id,
    task_name,
    task_level,
    parent_task_id,
    auto_progress_calculation,
    subtask_weight
FROM test_tasks;

-- 检查外键约束
SELECT 
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'test_tasks' 
  AND CONSTRAINT_NAME = 'fk_parent_task';

-- 检查索引
SHOW INDEX FROM test_tasks WHERE Key_name IN ('idx_parent_task_id', 'idx_task_level', 'idx_sub_task_order');
```

**预期结果**:
- ✅ 新增5个子任务支持字段
- ✅ 现有任务标记为 `task_level = 'MAIN'`
- ✅ 外键约束 `fk_parent_task` 创建成功
- ✅ 3个新索引创建成功

## 关键验证点

### 🔍 字段验证

| 字段名 | 类型 | 默认值 | 注释 |
|--------|------|--------|------|
| `parent_task_id` | BIGINT | NULL | 父任务ID |
| `task_level` | ENUM('MAIN', 'SUB') | 'MAIN' | 任务层级 |
| `sub_task_order` | INT | 0 | 子任务排序 |
| `auto_progress_calculation` | BOOLEAN | FALSE | 自动计算进度 |
| `subtask_weight` | DECIMAL(5,2) | 1.00 | 子任务权重 |

### 🔗 约束验证

```sql
-- 验证自引用外键
INSERT INTO test_tasks (task_name, start_date, expected_end_date, participant_count, 
                       created_by_user, task_level, parent_task_id) 
VALUES ('测试子任务', '2024-01-01', '2024-01-05', 1, 1, 'SUB', 1);

-- 检查插入是否成功
SELECT * FROM test_tasks WHERE task_level = 'SUB';

-- 清理测试数据
DELETE FROM test_tasks WHERE task_level = 'SUB' AND task_name = '测试子任务';
```

### 📊 数据完整性验证

```sql
-- 验证现有数据完整性
SELECT 
    COUNT(*) as total_tasks,
    COUNT(CASE WHEN task_level = 'MAIN' THEN 1 END) as main_tasks,
    COUNT(CASE WHEN task_level = 'SUB' THEN 1 END) as sub_tasks,
    COUNT(CASE WHEN parent_task_id IS NOT NULL THEN 1 END) as with_parent
FROM test_tasks;

-- 预期：total_tasks = main_tasks, sub_tasks = 0, with_parent = 0
```

## 回滚方案

如果迁移出现问题，可以使用以下回滚步骤：

```sql
-- 1. 删除外键约束
ALTER TABLE test_tasks DROP FOREIGN KEY fk_parent_task;

-- 2. 删除索引
DROP INDEX idx_parent_task_id ON test_tasks;
DROP INDEX idx_task_level ON test_tasks;
DROP INDEX idx_sub_task_order ON test_tasks;

-- 3. 删除新增字段
ALTER TABLE test_tasks 
DROP COLUMN parent_task_id,
DROP COLUMN task_level,
DROP COLUMN sub_task_order,
DROP COLUMN auto_progress_calculation,
DROP COLUMN subtask_weight;

-- 4. 或者直接恢复备份
-- mysql -u root -p test_tracking < backup_before_subtask_migration.sql
```

## 成功标准

✅ **迁移成功的标志**:
1. 所有新字段正确添加
2. 现有数据保持完整
3. 外键约束正常工作
4. 应用程序可以正常启动
5. 能够创建和查询子任务

❌ **迁移失败的情况**:
1. 字段添加失败
2. 外键约束创建失败  
3. 现有数据丢失或损坏
4. 应用程序启动报错

## 测试用例

### 测试用例1: 创建主任务
```sql
INSERT INTO test_tasks (task_name, start_date, expected_end_date, participant_count, 
                       created_by_user, task_level, auto_progress_calculation) 
VALUES ('测试版本V3.0', '2024-01-10', '2024-01-20', 3, 1, 'MAIN', true);
```

### 测试用例2: 创建子任务
```sql
INSERT INTO test_tasks (task_name, start_date, expected_end_date, participant_count, 
                       created_by_user, task_level, parent_task_id, sub_task_order, subtask_weight) 
VALUES ('功能模块A测试', '2024-01-10', '2024-01-15', 1, 1, 'SUB', 
        (SELECT id FROM test_tasks WHERE task_name = '测试版本V3.0'), 1, 1.5);
```

### 测试用例3: 验证层级关系
```sql
SELECT 
    p.task_name as parent_task,
    s.task_name as sub_task,
    s.sub_task_order,
    s.subtask_weight
FROM test_tasks p
JOIN test_tasks s ON p.id = s.parent_task_id
WHERE p.task_level = 'MAIN' AND s.task_level = 'SUB';
```

## 注意事项

⚠️ **重要提醒**:
1. 务必在生产环境执行前进行完整测试
2. 确保备份最新的生产数据
3. 建议在维护窗口期间执行迁移
4. 迁移后需要重启应用程序服务
5. 验证前端功能是否正常工作

💡 **最佳实践**:
1. 先在开发环境完整测试迁移过程
2. 准备回滚计划和脚本
3. 通知相关开发和测试人员
4. 迁移后进行功能回归测试
