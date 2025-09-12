package com.testtracking.dto;

import com.testtracking.entity.TestTask;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private LocalDateTime lastProgressUpdate;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String department;
    private String delayReason;
    private Boolean isDelayedCompletion;
    
    // ========================================
    // 子任务支持字段
    // ========================================
    private Long parentTaskId;
    private String parentTaskName;
    private TestTask.TaskLevel taskLevel;
    private Integer subTaskOrder;
    private Boolean autoProgressCalculation;
    private BigDecimal subtaskWeight;
    
    // 子任务列表（用于树形结构）
    private List<TestTaskDto> subTasks = new ArrayList<>();
    private Boolean hasSubTasks = false;
    private String taskType;

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
        
        // 子任务相关字段
        dto.setTaskLevel(task.getTaskLevel() != null ? task.getTaskLevel() : TestTask.TaskLevel.MAIN);
        dto.setSubTaskOrder(task.getSubTaskOrder());
        dto.setAutoProgressCalculation(task.getAutoProgressCalculation());
        dto.setSubtaskWeight(task.getSubtaskWeight());
        
        if (task.getParentTask() != null) {
            dto.setParentTaskId(task.getParentTask().getId());
            dto.setParentTaskName(task.getParentTask().getTaskName());
        }
        
        // 设置是否有子任务
        dto.setHasSubTasks(task.getSubTasks() != null && !task.getSubTasks().isEmpty());
        
        // 设置任务类型
        dto.setTaskType(task.getTaskType() != null ? task.getTaskType().name() : null);
        
        // 安全地获取关联用户信息，避免懒加载异常
        if (task.getAssignedTo() != null) {
            dto.setAssignedToName(task.getAssignedTo().getRealName());
        }
        if (task.getCreatedByUser() != null) {
            dto.setCreatedByUserName(task.getCreatedByUser().getRealName());
        }
        
        return dto;
    }
    
    /**
     * 转换为实体类（带子任务）
     */
    public static TestTaskDto fromEntityWithSubTasks(TestTask task) {
        TestTaskDto dto = fromEntity(task);
        
        // 递归转换子任务
        if (task.getSubTasks() != null && !task.getSubTasks().isEmpty()) {
            List<TestTaskDto> subTaskDtos = new ArrayList<>();
            for (TestTask subTask : task.getSubTasks()) {
                subTaskDtos.add(fromEntity(subTask));
            }
            dto.setSubTasks(subTaskDtos);
        }
        
        return dto;
    }
} 