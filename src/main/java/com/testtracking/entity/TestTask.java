package com.testtracking.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
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

    // 计算人天
    public void calculateManDays() {
        if (startDate != null && expectedEndDate != null && participantCount != null) {
            long days = ChronoUnit.DAYS.between(startDate, expectedEndDate) + 1;
            this.manDays = (double) days * participantCount;
        }
    }

    // 检查是否超时
    public void checkOverdue() {
        if (status != TaskStatus.COMPLETED && expectedEndDate != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(expectedEndDate)) {
                this.isOverdue = true;
                this.overdueDays = (int) ChronoUnit.DAYS.between(expectedEndDate, today);
            } else {
                this.isOverdue = false;
                this.overdueDays = 0;
            }
        }
    }

    // 更新进度
    public void updateProgress(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
        this.lastProgressUpdate = LocalDateTime.now();
        
        if (progressPercentage >= 100) {
            this.status = TaskStatus.COMPLETED;
            this.actualEndDate = LocalDate.now();
        }
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