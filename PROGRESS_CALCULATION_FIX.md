# 进度计算逻辑修正说明

## 问题描述

之前的实现有误解，用户指出了一个重要的Bug：

**错误理解**：只有主任务创建者更新进度时，主任务进度才变化
**正确逻辑**：所有子任务更新时，主任务就需要根据整体子任务进度自动更新自身进度

## 修正后的逻辑

### 1. 自动进度计算（核心功能）
- **触发条件**：任何子任务进度更新
- **计算方式**：基于所有子任务的加权平均进度
- **更新对象**：对应的主任务进度自动更新

### 2. 手动进度覆盖（可选功能）
- **权限限制**：只有主任务创建者才能手动更新主任务进度
- **覆盖效果**：手动设置的进度会覆盖自动计算的进度
- **使用场景**：当创建者认为自动计算不准确时可以手动调整

## 实现的修改

### 后端修改

#### 1. 实体类 (`TestTask.java`)

**新增方法**：
```java
/**
 * 主任务创建者手动更新进度（会覆盖自动计算的进度）
 */
public void updateMainTaskProgressManually(Integer progressPercentage, User currentUser)

/**
 * 子任务进度更新时，触发主任务进度重新计算
 */
public void onSubTaskProgressUpdated()
```

**保留原有方法**：
```java
/**
 * 计算主任务进度（基于子任务加权平均）
 */
public void calculateMainTaskProgress()
```

#### 2. 服务层 (`TestTaskService.java`)

**新增方法**：
```java
/**
 * 主任务创建者手动更新主任务进度（覆盖自动计算的进度）
 */
public TestTaskDto updateMainTaskProgressManually(Long mainTaskId, Integer progressPercentage, String currentUsername)
```

**现有方法保持不变**：
```java
/**
 * 更新子任务进度并自动计算主任务进度
 */
public TestTaskDto updateSubTaskProgress(Long subTaskId, Integer progressPercentage, String currentUsername) {
    // ... 更新子任务进度
    
    // 自动计算主任务进度 ✅ 这个逻辑已经正确实现
    if (subTask.getParentTask() != null) {
        calculateMainTaskProgress(subTask.getParentTask().getId());
    }
}
```

#### 3. 控制器 (`TestTaskController.java`)

**新增API端点**：
```java
@PutMapping("/{mainTaskId}/progress/manual")
public ResponseEntity<?> updateMainTaskProgressManually(@PathVariable Long mainTaskId, @RequestBody Map<String, Integer> request)
```

### 前端修改

#### 1. API调用 (`tasks.js`)
```javascript
// 主任务创建者手动更新主任务进度
export const updateMainTaskProgressManually = (mainTaskId, progressPercentage) => {
  return request({
    url: `/tasks/${mainTaskId}/progress/manual`,
    method: 'put',
    data: { progressPercentage }
  })
}
```

#### 2. 界面组件 (`Tasks.vue`)

**新增功能**：
- 主任务创建者可以看到"进度更新"按钮
- 点击按钮弹出进度输入对话框
- 手动更新会覆盖自动计算的进度

**权限检查**：
```javascript
// 检查是否为主任务创建者
const isMainTaskCreator = (task) => {
  const currentUser = authStore.user
  return task.taskLevel === 'MAIN' && 
         (task.createdByUserName === currentUser?.realName || 
          task.createdByUserName === currentUser?.username ||
          task.createdByUserId === currentUser?.id)
}
```

## 工作流程

### 场景1：子任务进度更新（自动）
1. 用户更新任何子任务的进度
2. 系统自动触发主任务进度重新计算
3. 基于所有子任务的加权平均计算新的主任务进度
4. 主任务进度自动更新

### 场景2：主任务创建者手动更新（覆盖）
1. 主任务创建者点击"进度更新"按钮
2. 输入新的进度百分比
3. 手动设置的进度覆盖自动计算的进度
4. 主任务进度更新为手动输入的值

### 场景3：混合使用
1. 子任务进度变化时，主任务进度自动计算更新
2. 如果创建者觉得自动计算不准确，可以手动调整
3. 下次子任务更新时，又会重新自动计算（可选择是否保留手动优先级）

## 用户体验

- **普通用户**：更新子任务进度后，可以立即看到主任务进度的自动更新
- **主任务创建者**：除了自动计算外，还可以根据实际情况手动调整主任务进度
- **团队协作**：多人分别负责不同子任务，主任务进度能反映整体进展

## 技术特点

1. **实时性**：子任务更新立即触发主任务重新计算
2. **权限控制**：只有创建者能手动覆盖进度
3. **灵活性**：自动计算+手动调整相结合
4. **透明性**：进度变化对所有相关人员可见

这样修正后，主任务的进度能真实反映所有子任务的整体完成情况，同时保留了创建者的主控权。
