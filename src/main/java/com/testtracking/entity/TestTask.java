package com.testtracking.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.AssertTrue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
} 