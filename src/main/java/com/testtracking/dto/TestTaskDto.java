package com.testtracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.testtracking.entity.TestTask;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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
    private Double actualManDays;
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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastProgressUpdate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedTime;
    
    private String department;
    private String delayReason;
    private Boolean isDelayedCompletion;
    
    // ========== 层级结构字段 ==========
    private Long parentId;                      // 父任务ID
    private TestTask.TaskType taskType;         // 任务类型
    private String versionCode;                 // 版本号
    private List<TestTaskDto> children;         // 子任务列表（用于树形展示）
    private Integer childCount;                 // 子任务数量
    private Integer completedChildCount;        // 已完成子任务数量
    private Boolean hasChildren;                // 是否有子任务

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
        dto.setActualManDays(task.getActualManDays());
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
        
        // 层级结构字段
        dto.setParentId(task.getParentId());
        dto.setTaskType(task.getTaskType());
        dto.setVersionCode(task.getVersionCode());
        dto.setChildren(new ArrayList<>());  // 初始化空列表
        dto.setHasChildren(false);  // 默认没有子任务
        
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