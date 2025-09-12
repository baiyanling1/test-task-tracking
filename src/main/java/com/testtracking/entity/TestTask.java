package com.testtracking.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.AssertTrue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "test_tasks")
@EqualsAndHashCode(callSuper = false)
public class TestTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "任务名称不能为空")
    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @NotNull(message = "开始时间不能为空")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "预期结束时间不能为空")
    @Column(name = "expected_end_date", nullable = false)
    private LocalDate expectedEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @NotNull(message = "参与人数不能为空")
    @Positive(message = "参与人数必须大于0")
    @Column(name = "participant_count", nullable = false)
    private Integer participantCount;

    @Column(name = "man_days")
    private Double manDays;

    @Column(name = "actual_man_days")
    private Double actualManDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false)
    private TaskStatus status = TaskStatus.PLANNED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Column(name = "risk_level")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(name = "risk_description")
    private String riskDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user")
    private User createdByUser;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "test_type")
    @Enumerated(EnumType.STRING)
    private TestType testType;

    @Column(name = "is_overdue")
    private Boolean isOverdue = false;

    @Column(name = "overdue_days")
    private Integer overdueDays = 0;

    @Column(name = "last_progress_update")
    private LocalDateTime lastProgressUpdate;

    @Column(name = "department")
    private String department;

    @Column(name = "delay_reason", columnDefinition = "TEXT")
    private String delayReason;

    @Column(name = "is_delayed_completion")
    private Boolean isDelayedCompletion = false;

    @Column(name = "is_expected_completion_reached")
    private Boolean isExpectedCompletionReached = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type")
    private TaskType taskType = TaskType.REQUIREMENT;

    // ========================================
    // 子任务支持字段
    // ========================================
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private TestTask parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("subTaskOrder ASC")
    private List<TestTask> subTasks = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "task_level")
    private TaskLevel taskLevel = TaskLevel.MAIN;

    @Column(name = "sub_task_order")
    private Integer subTaskOrder = 0;

    @Column(name = "auto_progress_calculation")
    private Boolean autoProgressCalculation = false;

    @Column(name = "subtask_weight", precision = 5, scale = 2)
    private BigDecimal subtaskWeight = new BigDecimal("1.00");

    // 计算人天
    public void calculateManDays() {
        if (startDate != null && expectedEndDate != null && participantCount != null) {
            long days = ChronoUnit.DAYS.between(startDate, expectedEndDate) + 1;
            this.manDays = (double) days * participantCount;
        }
    }

    // 检查任务状态和超时情况
    public void checkTaskStatusAndOverdue() {
        if (expectedEndDate == null) {
            // 没有预期结束时间，重置所有状态
            this.isOverdue = false;
            this.overdueDays = 0;
            this.isDelayedCompletion = false;
            this.isExpectedCompletionReached = false;
            return;
        }

        LocalDate today = LocalDate.now();
        
        if (actualEndDate != null) {
            // 有实际结束时间，根据实际结束时间与预期结束时间比较
            if (actualEndDate.isAfter(expectedEndDate)) {
                this.isOverdue = true;
                this.overdueDays = (int) ChronoUnit.DAYS.between(expectedEndDate, actualEndDate);
                this.isDelayedCompletion = true;
            } else {
                this.isOverdue = false;
                this.overdueDays = 0;
                this.isDelayedCompletion = false;
            }
            this.isExpectedCompletionReached = true;
        } else {
            // 没有实际结束时间，检查是否已到预期结束日期
            if (today.isAfter(expectedEndDate)) {
                // 已超过预期结束时间，标记为超预期
                this.isOverdue = true;
                this.overdueDays = (int) ChronoUnit.DAYS.between(expectedEndDate, today);
                this.isExpectedCompletionReached = true;
                this.isDelayedCompletion = false; // 还未完成，不是延期完成
            } else if (today.isEqual(expectedEndDate)) {
                // 今天是预期结束时间
                this.isOverdue = false;
                this.overdueDays = 0;
                this.isExpectedCompletionReached = true;
                this.isDelayedCompletion = false;
            } else {
                // 还未到预期结束时间
                this.isOverdue = false;
                this.overdueDays = 0;
                this.isExpectedCompletionReached = false;
                this.isDelayedCompletion = false;
            }
        }
    }

    // 检查是否超时（保持向后兼容）
    public void checkOverdue() {
        checkTaskStatusAndOverdue();
    }

    // 更新进度
    public void updateProgress(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
        this.lastProgressUpdate = LocalDateTime.now();
        
        if (progressPercentage >= 100) {
            this.status = TaskStatus.COMPLETED;
            this.actualEndDate = LocalDate.now();
            // 重新检查超时状态（基于实际结束时间）
            this.checkTaskStatusAndOverdue();
        } else if (progressPercentage > 0) {
            // 如果进度大于0，状态应该是进行中
            if (this.status == TaskStatus.PLANNED) {
                this.status = TaskStatus.IN_PROGRESS;
            }
        }
    }

    // 验证开始日期和结束日期的关系
    @AssertTrue(message = "预计结束时间不能早于开始时间")
    public boolean isEndDateValid() {
        if (startDate == null || expectedEndDate == null) {
            return true; // 如果任一日期为空，让@NotNull注解处理
        }
        return !expectedEndDate.isBefore(startDate); // 允许相等，但不允许结束日期早于开始日期
    }

    public enum TaskStatus {
        PLANNED("计划中"),
        IN_PROGRESS("进行中"),
        ON_HOLD("暂停"),
        COMPLETED("已完成"),
        CANCELLED("已取消");

        private final String description;

        TaskStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TaskPriority {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高");

        private final String description;

        TaskPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中风险"),
        HIGH("高风险"),
        CRITICAL("严重风险");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TaskType {
        REQUIREMENT("需求测试"),
        VERSION("版本测试");

        private final String description;

        TaskType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TestType {
        FUNCTIONAL("功能测试"),
        PERFORMANCE("性能测试"),
        SECURITY("安全测试"),
        USABILITY("可用性测试"),
        COMPATIBILITY("兼容性测试"),
        INTEGRATION("集成测试"),
        SYSTEM("系统测试"),
        REGRESSION("回归测试");

        private final String description;

        TestType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TaskLevel {
        MAIN("主任务"),
        SUB("子任务");

        private final String description;

        TaskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ========================================
    // 子任务相关业务方法
    // ========================================

    /**
     * 判断是否为主任务
     */
    public boolean isMainTask() {
        return taskLevel == TaskLevel.MAIN;
    }

    /**
     * 判断是否为子任务
     */
    public boolean isSubTask() {
        return taskLevel == TaskLevel.SUB;
    }

    /**
     * 获取主任务（如果当前是子任务）
     */
    public TestTask getMainTask() {
        if (isSubTask()) {
            return parentTask;
        }
        return this;
    }

    /**
     * 添加子任务（只有版本测试类型的主任务才能添加子任务）
     */
    public void addSubTask(TestTask subTask) {
        if (this.isMainTask() && this.taskType == TaskType.VERSION) {
            // 验证时间约束：子任务时间必须在主任务时间范围内
            if (!isValidSubTaskTimeRange(subTask)) {
                String errorMessage = String.format(
                    "子任务时间范围必须在主任务时间范围内。主任务时间范围：%s 至 %s，子任务时间范围：%s 至 %s",
                    this.startDate, this.expectedEndDate,
                    subTask.getStartDate(), subTask.getExpectedEndDate()
                );
                throw new IllegalArgumentException(errorMessage);
            }
            
            subTask.setParentTask(this);
            subTask.setTaskLevel(TaskLevel.SUB);
            subTask.setTaskType(this.taskType); // 子任务继承父任务类型
            subTask.setAutoProgressCalculation(false);
            
            // 设置子任务排序
            int maxOrder = subTasks.stream()
                    .mapToInt(task -> task.getSubTaskOrder() != null ? task.getSubTaskOrder() : 0)
                    .max()
                    .orElse(0);
            subTask.setSubTaskOrder(maxOrder + 1);
            
            this.subTasks.add(subTask);
            
            // 启用自动进度计算
            if (!this.autoProgressCalculation) {
                this.autoProgressCalculation = true;
            }
        } else if (this.taskType == TaskType.REQUIREMENT) {
            throw new IllegalArgumentException("需求测试类型的任务不能添加子任务");
        }
    }

    /**
     * 移除子任务
     */
    public void removeSubTask(TestTask subTask) {
        if (this.isMainTask() && this.subTasks.contains(subTask)) {
            this.subTasks.remove(subTask);
            subTask.setParentTask(null);
            
            // 如果没有子任务了，关闭自动进度计算
            if (this.subTasks.isEmpty()) {
                this.autoProgressCalculation = false;
            }
        }
    }

    /**
     * 验证子任务时间范围是否有效
     */
    private boolean isValidSubTaskTimeRange(TestTask subTask) {
        if (subTask.getStartDate() == null || subTask.getExpectedEndDate() == null) {
            return false;
        }
        
        // 子任务开始时间不能早于主任务开始时间
        if (subTask.getStartDate().isBefore(this.startDate)) {
            return false;
        }
        
        // 子任务结束时间不能晚于主任务结束时间
        if (subTask.getExpectedEndDate().isAfter(this.expectedEndDate)) {
            return false;
        }
        
        return true;
    }

    /**
     * 主任务创建者手动更新进度（会覆盖自动计算的进度）
     */
    public void updateMainTaskProgressManually(Integer progressPercentage, User currentUser) {
        if (this.isMainTask() && this.createdByUser != null && 
            this.createdByUser.getId().equals(currentUser.getId())) {
            this.progressPercentage = progressPercentage;
            this.lastProgressUpdate = LocalDateTime.now();
            
            // 手动更新时，暂时禁用自动计算（可以考虑添加一个标志）
            // this.autoProgressCalculation = false;
            
            if (progressPercentage >= 100) {
                this.status = TaskStatus.COMPLETED;
                this.actualEndDate = LocalDate.now();
            } else if (progressPercentage > 0 && this.status == TaskStatus.PLANNED) {
                this.status = TaskStatus.IN_PROGRESS;
            }
        }
    }

    /**
     * 子任务进度更新时，触发主任务进度重新计算
     */
    public void onSubTaskProgressUpdated() {
        if (this.parentTask != null && this.parentTask.isMainTask()) {
            this.parentTask.calculateMainTaskProgress();
        }
    }

    /**
     * 计算主任务进度（基于子任务加权平均）
     */
    public void calculateMainTaskProgress() {
        if (!this.isMainTask() || !this.autoProgressCalculation || this.subTasks.isEmpty()) {
            return;
        }

        // 计算加权平均进度
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedProgress = BigDecimal.ZERO;
        
        for (TestTask subTask : this.subTasks) {
            BigDecimal weight = subTask.getSubtaskWeight() != null ? 
                    subTask.getSubtaskWeight() : BigDecimal.ONE;
            BigDecimal progress = BigDecimal.valueOf(
                    subTask.getProgressPercentage() != null ? subTask.getProgressPercentage() : 0
            );
            
            totalWeight = totalWeight.add(weight);
            weightedProgress = weightedProgress.add(weight.multiply(progress));
        }
        
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal avgProgress = weightedProgress.divide(totalWeight, 2, java.math.RoundingMode.HALF_UP);
            this.progressPercentage = avgProgress.intValue();
        }

        // 检查主任务状态
        updateMainTaskStatus();
    }

    /**
     * 更新主任务状态
     */
    private void updateMainTaskStatus() {
        if (!this.isMainTask() || this.subTasks.isEmpty()) {
            return;
        }

        // 检查是否所有子任务都完成
        boolean allCompleted = this.subTasks.stream()
                .allMatch(task -> task.getStatus() == TaskStatus.COMPLETED);
        
        if (allCompleted) {
            this.status = TaskStatus.COMPLETED;
            if (this.actualEndDate == null) {
                this.actualEndDate = LocalDate.now();
            }
        } else if (this.progressPercentage > 0 && this.status == TaskStatus.PLANNED) {
            this.status = TaskStatus.IN_PROGRESS;
        }
        
        // 处理取消状态：如果所有子任务都被取消，主任务也设置为取消
        boolean allCancelled = this.subTasks.stream()
                .allMatch(task -> task.getStatus() == TaskStatus.CANCELLED);
        if (allCancelled && !this.subTasks.isEmpty()) {
            this.status = TaskStatus.CANCELLED;
        }

        // 重新检查超时状态
        this.checkTaskStatusAndOverdue();
    }

    /**
     * 获取任务完整路径（用于显示）
     */
    public String getTaskPath() {
        if (this.isSubTask() && this.parentTask != null) {
            return this.parentTask.getTaskName() + " > " + this.taskName;
        }
        return this.taskName;
    }

    /**
     * 检查用户是否有权限访问此任务
     * 特别强调：子任务的责任人可以看到自己的子任务
     */
    public boolean hasUserAccess(User user) {
        if (user == null) {
            return false;
        }

        // 检查是否为任务负责人
        if (this.assignedTo != null && this.assignedTo.getId().equals(user.getId())) {
            return true;
        }

        // 检查是否为任务创建者
        if (this.createdByUser != null && this.createdByUser.getId().equals(user.getId())) {
            return true;
        }

        // 如果是子任务，还要检查主任务的权限
        if (this.isSubTask() && this.parentTask != null) {
            return this.parentTask.hasUserAccess(user);
        }

        // 管理员和经理角色
        return user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.MANAGER;
    }
} 