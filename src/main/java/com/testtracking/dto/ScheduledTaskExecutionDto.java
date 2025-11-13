package com.testtracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.testtracking.entity.ScheduledTaskExecution;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduledTaskExecutionDto {

    private Long id;
    private Long taskId;
    private String taskName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime executionTime;
    
    private ScheduledTaskExecution.ExecutionStatus executionResult;
    private String errorMessage;
    private Long executionDuration;
    private ScheduledTaskExecution.ExecutionStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdTime;

    // 从实体转换为DTO
    public static ScheduledTaskExecutionDto fromEntity(ScheduledTaskExecution execution) {
        ScheduledTaskExecutionDto dto = new ScheduledTaskExecutionDto();
        dto.setId(execution.getId());
        dto.setTaskId(execution.getTask().getId());
        dto.setTaskName(execution.getTask().getTaskName());
        dto.setExecutionTime(execution.getExecutionTime());
        dto.setExecutionResult(execution.getExecutionResult());
        dto.setErrorMessage(execution.getErrorMessage());
        dto.setExecutionDuration(execution.getExecutionDuration());
        dto.setStatus(execution.getStatus());
        dto.setCreatedTime(execution.getCreatedTime());
        return dto;
    }
}
