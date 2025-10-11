package com.testtracking.dto;

import lombok.Data;

@Data
public class UserWorkStatsDto {
    private Long userId;
    private String userName;
    private String realName;
    private String department;
    
    // 工作饱和度相关
    private Double workloadUtilization; // 工时利用率 (%)
    private String workloadStatus; // 工作状态: OVERLOADED, SATURATED, NORMAL, IDLE
    private Double totalManDays; // 总工时
    private Double standardWorkDays; // 标准工作日
    
    // 任务完成效率相关
    private Integer totalTasks; // 总任务数
    private Integer completedTasks; // 已完成任务数
    private Integer onTimeCompletedTasks; // 按时完成任务数
    private Double onTimeCompletionRate; // 按时完成率 (%)
    private Double avgDelayDays; // 平均延期天数
    
    // 当前任务负载
    private Integer currentActiveTasks; // 当前进行中任务数
    private Integer plannedTasks; // 计划中任务数
    private Integer onHoldTasks; // 暂停任务数
    private Double currentWorkload; // 当前工作负载 (人天)
    
    // 工时预估准确度
    private Double estimationAccuracy; // 工时预估准确度 (%)
    
    // 前端显示用的计算属性
    private String workloadStatusText; // 工作状态文本
    private String workloadStatusColor; // 工作状态颜色
    
    // 辅助方法 - 用于在服务层设置计算属性
    public void calculateDisplayProperties() {
        this.workloadStatusText = getWorkloadStatusTextByStatus(this.workloadStatus);
        this.workloadStatusColor = getWorkloadStatusColorByStatus(this.workloadStatus);
    }
    
    private String getWorkloadStatusTextByStatus(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "OVERLOADED": return "过载";
            case "SATURATED": return "饱和";
            case "NORMAL": return "正常";
            case "IDLE": return "空闲";
            default: return "未知";
        }
    }
    
    private String getWorkloadStatusColorByStatus(String status) {
        if (status == null) return "#a0aec0";
        switch (status) {
            case "OVERLOADED": return "#f56565"; // 红色
            case "SATURATED": return "#ed8936"; // 橙色
            case "NORMAL": return "#48bb78"; // 绿色
            case "IDLE": return "#4299e1"; // 蓝色
            default: return "#a0aec0"; // 灰色
        }
    }
}
