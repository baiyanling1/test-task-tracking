package com.testtracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.testtracking.entity.ScheduledTask;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduledTaskDto {

    private Long id;
    private String taskName;
    private String taskDescription;
    private String cronExpression;
    private String beanName;
    private String methodName;
    private boolean enabled;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastExecuteTime;
    
    private String lastExecuteResult; // 执行结果：SUCCESS, FAILED
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime nextExecuteTime;
    
    private ScheduledTask.TaskStatus status;

    // 从实体转换为DTO
    public static ScheduledTaskDto fromEntity(ScheduledTask task) {
        ScheduledTaskDto dto = new ScheduledTaskDto();
        dto.setId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setTaskDescription(task.getTaskDescription());
        dto.setCronExpression(task.getCronExpression());
        dto.setBeanName(task.getBeanName());
        dto.setMethodName(task.getMethodName());
        dto.setEnabled(task.isEnabled());
        dto.setLastExecuteTime(task.getLastExecuteTime());
        dto.setLastExecuteResult(task.getLastExecuteResult());
        dto.setNextExecuteTime(task.getNextExecuteTime());
        dto.setStatus(task.getStatus());
        return dto;
    }
}
