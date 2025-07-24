package com.testtracking.dto;

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

    private LocalDateTime updateTime;

    private TestTask.TaskStatus taskStatus;

    private TestTask.RiskLevel riskLevel;

    private String riskDescription;

    private String blockers;

    private String nextSteps;

    private LocalDateTime createdTime;

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
        dto.setRiskLevel(progress.getRiskLevel());
        dto.setRiskDescription(progress.getRiskDescription());
        dto.setBlockers(progress.getBlockers());
        dto.setNextSteps(progress.getNextSteps());
        dto.setCreatedTime(progress.getCreatedTime());
        return dto;
    }
} 