package com.testtracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.testtracking.entity.TaskProgress;
import com.testtracking.entity.TestTask;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class TaskProgressDto {

    private Long id;

    private Long taskId;
    private String taskName;

    private Long updatedByUserId;
    private String updatedByUserName;

    @NotNull(message = "进度百分比不能为空")
    @Min(value = 0, message = "进度百分比不能小于0")
    @Max(value = 100, message = "进度百分比不能大于100")
    private Integer progressPercentage;

    private String progressNotes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;

    private TestTask.TaskStatus taskStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdTime;
    
    // 实际结束时间（可选）
    private String actualEndDate;
    
    // 实际工时（可选）
    private Double actualManDays;
    
    // 新增：本周投入时间记录（可选）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime workStartTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime workEndTime;
    
    private Double workHours;

    // 从实体转换为DTO
    public static TaskProgressDto fromEntity(TaskProgress progress) {
        TaskProgressDto dto = new TaskProgressDto();
        dto.setId(progress.getId());
        dto.setTaskId(progress.getTask().getId());
        dto.setTaskName(progress.getTask().getTaskName());
        dto.setUpdatedByUserId(progress.getUpdatedByUser().getId());
        dto.setUpdatedByUserName(progress.getUpdatedByUser().getRealName());
        dto.setProgressPercentage(progress.getProgressPercentage());
        dto.setProgressNotes(progress.getProgressNotes());
        dto.setUpdateTime(progress.getUpdateTime());
        dto.setTaskStatus(progress.getTaskStatus());
        dto.setCreatedTime(progress.getCreatedTime());
        dto.setActualEndDate(progress.getActualEndDate());
        dto.setActualManDays(progress.getActualManDays());
        dto.setWorkStartTime(progress.getWorkStartTime());
        dto.setWorkEndTime(progress.getWorkEndTime());
        dto.setWorkHours(progress.getWorkHours());
        return dto;
    }
} 