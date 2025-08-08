package com.testtracking.repository;

import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TestTaskRepository extends JpaRepository<TestTask, Long> {

    // 基础查询
    List<TestTask> findByAssignedTo(User assignedTo);
    
    List<TestTask> findByCreatedByUser(User createdByUser);
    
    List<TestTask> findByStatus(TestTask.TaskStatus status);
    
    List<TestTask> findByPriority(TestTask.TaskPriority priority);
    
    List<TestTask> findByRiskLevel(TestTask.RiskLevel riskLevel);
    
    List<TestTask> findByProjectName(String projectName);
    
    List<TestTask> findByTestType(TestTask.TestType testType);

    // 分页查询
    Page<TestTask> findByAssignedTo(User assignedTo, Pageable pageable);
    
    Page<TestTask> findByStatus(TestTask.TaskStatus status, Pageable pageable);
    
    Page<TestTask> findByProjectName(String projectName, Pageable pageable);

    // 时间范围查询
    List<TestTask> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<TestTask> findByExpectedEndDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<TestTask> findByActualEndDateBetween(LocalDate startDate, LocalDate endDate);

    // 超时任务查询
    List<TestTask> findByIsOverdueTrue();
    
    List<TestTask> findByIsOverdueTrueAndStatusNot(TestTask.TaskStatus status);
    
    List<TestTask> findByExpectedEndDateBeforeAndStatusNot(LocalDate date, TestTask.TaskStatus status);

    // 复杂查询
    @Query("SELECT t FROM TestTask t WHERE " +
           "(:assignedTo IS NULL OR t.assignedTo = :assignedTo) AND " +
           "(:assignedToName IS NULL OR t.assignedTo.realName = :assignedToName) AND " +
           "(:department IS NULL OR t.department = :department) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:projectName IS NULL OR t.projectName = :projectName) AND " +
           "(:testType IS NULL OR t.testType = :testType) AND " +
           "(:startDateFrom IS NULL OR t.startDate >= :startDateFrom) AND " +
           "(:startDateTo IS NULL OR t.startDate <= :startDateTo)")
    Page<TestTask> findByFilters(@Param("assignedTo") User assignedTo,
                                 @Param("assignedToName") String assignedToName,
                                 @Param("department") String department,
                                 @Param("status") TestTask.TaskStatus status,
                                 @Param("priority") TestTask.TaskPriority priority,
                                 @Param("projectName") String projectName,
                                 @Param("testType") TestTask.TestType testType,
                                 @Param("startDateFrom") LocalDate startDateFrom,
                                 @Param("startDateTo") LocalDate startDateTo,
                                 Pageable pageable);

    // 统计查询
    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.status = :status")
    Long countByStatus(@Param("status") TestTask.TaskStatus status);

    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.isOverdue = true")
    Long countOverdueTasks();

    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.riskLevel = :riskLevel")
    Long countByRiskLevel(@Param("riskLevel") TestTask.RiskLevel riskLevel);

    @Query("SELECT t.projectName, COUNT(t) FROM TestTask t GROUP BY t.projectName")
    List<Object[]> countByProject();

    @Query("SELECT t.status, COUNT(t) FROM TestTask t GROUP BY t.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT t.priority, COUNT(t) FROM TestTask t GROUP BY t.priority")
    List<Object[]> countByPriorityGroup();

    // 部门统计
    @Query("SELECT t.department, COUNT(t) FROM TestTask t WHERE t.department IS NOT NULL GROUP BY t.department")
    List<Object[]> countByDepartment();

    // 本周任务统计
    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.startDate >= :weekStart AND t.startDate <= :weekEnd")
    Long countTasksThisWeek(@Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.expectedEndDate >= :weekStart AND t.expectedEndDate <= :weekEnd")
    Long countTasksEndingThisWeek(@Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    // 本月任务统计
    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.startDate >= :monthStart AND t.startDate <= :monthEnd")
    Long countTasksThisMonth(@Param("monthStart") LocalDate monthStart, @Param("monthEnd") LocalDate monthEnd);

    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.actualEndDate >= :monthStart AND t.actualEndDate <= :monthEnd")
    Long countCompletedTasksThisMonth(@Param("monthStart") LocalDate monthStart, @Param("monthEnd") LocalDate monthEnd);

    // 人天统计
    @Query("SELECT SUM(t.manDays) FROM TestTask t WHERE t.status = :status")
    Double sumManDaysByStatus(@Param("status") TestTask.TaskStatus status);

    @Query("SELECT SUM(t.manDays) FROM TestTask t WHERE t.startDate >= :startDate AND t.startDate <= :endDate")
    Double sumManDaysByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 搜索功能
    @Query("SELECT t FROM TestTask t WHERE " +
           "t.taskName LIKE %:keyword% OR " +
           "t.taskDescription LIKE %:keyword% OR " +
           "t.projectName LIKE %:keyword% OR " +
           "t.moduleName LIKE %:keyword%")
    List<TestTask> findByKeyword(@Param("keyword") String keyword);

    // 按开始日期统计任务数量
    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.startDate = :date")
    Long countByStartDate(@Param("date") LocalDate date);

    // 个人任务统计
    @Query("SELECT t.assignedTo.realName, COUNT(t) FROM TestTask t " +
           "WHERE t.assignedTo IS NOT NULL " +
           "AND t.startDate >= :monthStart AND t.startDate <= :monthEnd " +
           "GROUP BY t.assignedTo.realName " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> countByUserThisMonth(@Param("monthStart") LocalDate monthStart, @Param("monthEnd") LocalDate monthEnd);
} 