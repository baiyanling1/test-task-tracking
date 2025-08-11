package com.testtracking.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "task_progress")
@EqualsAndHashCode(callSuper = false)
public class TaskProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private TestTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user", nullable = false)
    private User updatedByUser;

    @NotNull(message = "进度百分比不能为空")
    @Min(value = 0, message = "进度百分比不能小于0")
    @Max(value = 100, message = "进度百分比不能大于100")
    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage;

    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status")
    private TestTask.TaskStatus taskStatus;
    
    @Column(name = "actual_end_date")
    private String actualEndDate;
    
    @Column(name = "actual_man_days")
    private Double actualManDays;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        updateTime = LocalDateTime.now();
    }
} 