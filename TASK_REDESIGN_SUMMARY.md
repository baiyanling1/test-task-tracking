# 任务系统重新设计总结

## 需求回顾

根据用户的具体需求，对任务系统进行了以下调整：

1. **界面简化**：移除树形/平铺视图切换，采用单一表格视图，主任务可展开显示子任务
2. **任务类型分类**：
   - **需求测试**：独立任务，不能添加子任务
   - **版本测试**：可以拆分为多个子任务
3. **进度计算规则**：只有主任务创建者(A)更新进度时，主任务进度才变化
4. **时间约束**：子任务的时间范围必须在主任务时间范围内
5. **主任务详情**：显示所有子任务的进度

## 实现的功能

### 1. 数据库层面

#### 新增迁移脚本：`15-add-task-type-and-constraints.sql`
- 添加 `task_type` 字段：`ENUM('REQUIREMENT', 'VERSION')`
- 为现有数据设置默认值
- 添加相应索引

#### 实体类更新：`TestTask.java`
- 添加 `TaskType` 枚举
- 添加 `taskType` 字段
- 新增方法：
  - `isValidSubTaskTimeRange()` - 验证子任务时间约束
  - `updateMainTaskProgress()` - 只允许创建者更新主任务进度
  - 修改 `addSubTask()` - 添加类型和时间验证

#### DTO更新：`TestTaskDto.java`
- 添加 `taskType` 字段
- 更新 `fromEntity()` 方法

### 2. 服务层面

#### TestTaskService更新
- 修改 `createSubTask()` 方法：
  - 只有版本测试类型的主任务才能创建子任务
  - 添加时间约束验证
- 其他相关业务逻辑调整

### 3. 前端界面

#### Tasks.vue重大调整

**界面简化**：
- 移除视图模式切换组件
- 删除平铺视图表格，只保留支持展开的单一表格
- 简化操作逻辑

**新增功能**：
- 任务类型筛选下拉框
- 任务名称列显示任务类型标签：
  - 绿色标签：需求测试
  - 蓝色标签：版本测试
- 只有版本测试主任务显示"添加子任务"按钮
- 任务创建对话框添加任务类型选择

**样式优化**：
- 添加 `.task-type-tag` 样式
- 保持原有的层级显示效果

## 关键业务规则

### 1. 任务类型限制
```javascript
// 只有版本测试主任务能添加子任务
if (row.taskType === 'VERSION') {
  // 显示"添加子任务"按钮
}
```

### 2. 时间约束验证
```java
// 子任务时间必须在主任务时间范围内
private boolean isValidSubTaskTimeRange(TestTask subTask) {
    return !subTask.getStartDate().isBefore(this.startDate) && 
           !subTask.getExpectedEndDate().isAfter(this.expectedEndDate);
}
```

### 3. 进度更新权限
```java
// 只有主任务创建者能更新主任务进度
public void updateMainTaskProgress(Integer progressPercentage, User currentUser) {
    if (this.createdByUser.getId().equals(currentUser.getId())) {
        // 更新进度
    }
}
```

### 4. 界面显示逻辑
```vue
<!-- 任务类型标签 -->
<el-tag :type="row.taskType === 'VERSION' ? 'primary' : 'success'">
  {{ row.taskType === 'VERSION' ? '版本测试' : '需求测试' }}
</el-tag>

<!-- 子任务数量（仅版本测试主任务显示） -->
<el-tag v-if="row.taskLevel === 'MAIN' && row.taskType === 'VERSION' && row.hasSubTasks">
  {{ row.subTasks.length }}个子任务
</el-tag>
```

## 数据兼容性

- 现有数据自动标记为"需求测试"类型
- 有子任务的主任务自动标记为"版本测试"类型
- 所有现有功能保持兼容

## 用户体验改进

1. **界面更简洁**：单一表格视图，减少用户困惑
2. **类型清晰**：通过颜色区分需求测试和版本测试
3. **操作直观**：只在合适的任务上显示相应操作按钮
4. **约束明确**：时间和类型约束防止错误操作

## 后续可优化项

1. **主任务详情页**：专门展示所有子任务进度的详情页面
2. **批量操作**：批量更新子任务状态和进度
3. **进度可视化**：更直观的进度展示图表
4. **时间冲突检测**：更智能的时间约束提示

## 部署说明

1. 执行数据库迁移：`15-add-task-type-and-constraints.sql`
2. 重启后端服务
3. 清理前端缓存
4. 验证功能正常工作

用户现在可以：
- 创建需求测试（独立任务）
- 创建版本测试（可拆分子任务）
- 在单一界面中管理所有任务
- 展开版本测试查看子任务
- 按类型筛选任务
