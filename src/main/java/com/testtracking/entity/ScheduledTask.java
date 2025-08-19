package com.testtracking.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scheduled_tasks")
@EqualsAndHashCode(callSuper = false)
public class ScheduledTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false, unique = true)
    private String taskName;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;

    @Column(name = "bean_name", nullable = false)
    private String beanName;

    @Column(name = "method_name", nullable = false)
    private String methodName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "last_execute_time")
    private LocalDateTime lastExecuteTime;

    @Column(name = "last_execute_result")
    private String lastExecuteResult; // SUCCESS, FAILED

    @Column(name = "next_execute_time")
    private LocalDateTime nextExecuteTime;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.ENABLED;

    public enum TaskStatus {
        ENABLED("启用"),
        DISABLED("禁用"),
        RUNNING("运行中"),
        ERROR("错误");

        private final String description;

        TaskStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
