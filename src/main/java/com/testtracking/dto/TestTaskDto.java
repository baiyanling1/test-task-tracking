package com.testtracking.dto;

import com.testtracking.entity.TestTask;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TestTaskDto {
    private Long id;
    private String taskName;
    private String taskDescription;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate actualEndDate;
    private Integer participantCount;
    private Double manDays;
    private TestTask.TaskStatus status;
    private TestTask.TaskPriority priority;
    private Integer progressPercentage;
    private TestTask.RiskLevel riskLevel;
    private String riskDescription;
    private String assignedToName; // 只包含用户名，避免懒加载
    private String createdByUserName; // 只包含用户名，避免懒加载
    private String projectName;
    private String moduleName;
    private TestTask.TestType testType;
    private Boolean isOverdue;
    private Integer overdueDays;
    private LocalDateTime lastProgressUpdate;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String department;
    private String delayReason;
    private Boolean isDelayedCompletion;

    public static TestTaskDto fromEntity(TestTask task) {
        TestTaskDto dto = new TestTaskDto();
        dto.setId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setTaskDescription(task.getTaskDescription());
        dto.setStartDate(task.getStartDate());
        dto.setExpectedEndDate(task.getExpectedEndDate());
        dto.setActualEndDate(task.getActualEndDate());
        dto.setParticipantCount(task.getParticipantCount());
        dto.setManDays(task.getManDays());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setProgressPercentage(task.getProgressPercentage());
        dto.setRiskLevel(task.getRiskLevel());
        dto.setRiskDescription(task.getRiskDescription());
        dto.setProjectName(task.getProjectName());
        dto.setModuleName(task.getModuleName());
        dto.setTestType(task.getTestType());
        dto.setIsOverdue(task.getIsOverdue());
        dto.setOverdueDays(task.getOverdueDays());
        dto.setLastProgressUpdate(task.getLastProgressUpdate());
        dto.setCreatedTime(task.getCreatedTime());
        dto.setUpdatedTime(task.getUpdatedTime());
        dto.setDepartment(task.getDepartment());
        dto.setDelayReason(task.getDelayReason());
        dto.setIsDelayedCompletion(task.getIsDelayedCompletion());
        
        // 安全地获取关联用户信息，避免懒加载异常
        if (task.getAssignedTo() != null) {
            dto.setAssignedToName(task.getAssignedTo().getRealName());
        }
        if (task.getCreatedByUser() != null) {
            dto.setCreatedByUserName(task.getCreatedByUser().getRealName());
        }
        
        return dto;
    }
} 