package com.testtracking.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scheduled_task_executions")
@EqualsAndHashCode(callSuper = false)
public class ScheduledTaskExecution extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private ScheduledTask task;

    @Column(name = "execution_time", nullable = false)
    private LocalDateTime executionTime;

    @Column(name = "execution_result")
    @Enumerated(EnumType.STRING)
    private ExecutionStatus executionResult;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_duration")
    private Long executionDuration; // 执行时长(毫秒)

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status = ExecutionStatus.SUCCESS;

    public enum ExecutionStatus {
        SUCCESS("成功"),
        FAILED("失败"),
        RUNNING("运行中");

        private final String description;

        ExecutionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
