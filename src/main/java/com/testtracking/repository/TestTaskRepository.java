package com.testtracking.repository;

import com.testtracking.entity.TestTask;
import com.testtracking.entity.TaskProgress;
import com.testtracking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
           "(:startDateTo IS NULL OR t.startDate <= :startDateTo) AND " +
           "(:isOverdue IS NULL OR t.isOverdue = :isOverdue) AND " +
           "(:isExpectedCompletionReached IS NULL OR t.isExpectedCompletionReached = :isExpectedCompletionReached) AND " +
           "(:search IS NULL OR t.taskName LIKE %:search% OR t.taskDescription LIKE %:search%)")
    Page<TestTask> findByFilters(@Param("assignedTo") User assignedTo,
                                 @Param("assignedToName") String assignedToName,
                                 @Param("department") String department,
                                 @Param("status") TestTask.TaskStatus status,
                                 @Param("priority") TestTask.TaskPriority priority,
                                 @Param("projectName") String projectName,
                                 @Param("testType") TestTask.TestType testType,
                                 @Param("startDateFrom") LocalDate startDateFrom,
                                 @Param("startDateTo") LocalDate startDateTo,
                                 @Param("isOverdue") Boolean isOverdue,
                                 @Param("isExpectedCompletionReached") Boolean isExpectedCompletionReached,
                                 @Param("search") String search,
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

    // 个人任务统计（按状态分类）
    @Query("SELECT t.assignedTo.realName, t.status, COUNT(t) FROM TestTask t " +
           "WHERE t.assignedTo IS NOT NULL " +
           "AND t.startDate >= :monthStart AND t.startDate <= :monthEnd " +
           "GROUP BY t.assignedTo.realName, t.status " +
           "ORDER BY t.assignedTo.realName, t.status")
    List<Object[]> countByUserAndStatusThisMonth(@Param("monthStart") LocalDate monthStart, @Param("monthEnd") LocalDate monthEnd);

    // 任务跟踪提醒相关查询
    @Query("SELECT t FROM TestTask t WHERE t.assignedTo = :assignedTo AND t.status IN :statuses")
    List<TestTask> findByAssignedToAndStatusIn(@Param("assignedTo") User assignedTo, @Param("statuses") List<TestTask.TaskStatus> statuses);

    @Query("SELECT t FROM TestTask t WHERE t.createdByUser = :createdByUser AND t.createdTime BETWEEN :startTime AND :endTime")
    List<TestTask> findByCreatedByUserAndCreatedTimeBetween(@Param("createdByUser") User createdByUser, 
                                                           @Param("startTime") java.time.LocalDateTime startTime, 
                                                           @Param("endTime") java.time.LocalDateTime endTime);

    @Query("SELECT t FROM TestTask t WHERE t.assignedTo = :assignedTo AND t.updatedTime BETWEEN :startTime AND :endTime")
    List<TestTask> findByAssignedToAndUpdatedTimeBetween(@Param("assignedTo") User assignedTo, 
                                                        @Param("startTime") java.time.LocalDateTime startTime, 
                                                        @Param("endTime") java.time.LocalDateTime endTime);

    // 任务状态更新相关查询
    @Query("SELECT t FROM TestTask t WHERE t.status NOT IN :statuses")
    List<TestTask> findByStatusNotIn(@Param("statuses") List<TestTask.TaskStatus> statuses);

    @Query("SELECT t FROM TestTask t WHERE t.expectedEndDate = :expectedEndDate AND t.status = :status")
    List<TestTask> findByExpectedEndDateAndStatus(@Param("expectedEndDate") java.time.LocalDate expectedEndDate, 
                                                 @Param("status") TestTask.TaskStatus status);

    // 按用户和日期范围统计实际工时
    @Query("SELECT SUM(t.actualManDays) FROM TestTask t WHERE t.assignedTo.id = :userId " +
           "AND ((t.actualEndDate >= :startDate AND t.actualEndDate <= :endDate) " +
           "OR (t.actualEndDate IS NULL AND t.expectedEndDate >= :startDate AND t.expectedEndDate <= :endDate)) " +
           "AND t.actualManDays IS NOT NULL")
    Double sumActualManDaysByUserAndDateRange(@Param("userId") Long userId, 
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);

    // 统计用户在指定时间范围内创建的任务数量
    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.createdByUser = :user " +
           "AND t.createdTime BETWEEN :startTime AND :endTime")
    Long countByCreatedByUserAndCreatedTimeBetween(@Param("user") User user, 
                                                  @Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime);

    // 统计用户在指定时间范围内的任务进度更新次数
    @Query("SELECT COUNT(tp) FROM TaskProgress tp WHERE tp.updatedByUser.id = :userId " +
           "AND tp.updateTime BETWEEN :startTime AND :endTime")
    Long countTaskProgressUpdatesByUserAndDateRange(@Param("userId") Long userId, 
                                                   @Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);

    // 统计用户在指定时间范围内修改的任务数量
    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.updatedBy = :username " +
           "AND t.updatedTime BETWEEN :startTime AND :endTime")
    Long countByUpdatedByAndUpdatedTimeBetween(@Param("username") String username, 
                                              @Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);

    // 统计用户分配的指定状态任务数量
    @Query("SELECT COUNT(t) FROM TestTask t WHERE t.assignedTo = :user AND t.status IN :statuses")
    Long countByAssignedToAndStatusIn(@Param("user") User user, @Param("statuses") List<TestTask.TaskStatus> statuses);

    // 获取用户在指定日期范围内的任务
    @Query("SELECT t FROM TestTask t WHERE t.assignedTo = :user " +
           "AND ((t.startDate >= :startDate AND t.startDate <= :endDate) " +
           "OR (t.actualEndDate >= :startDate AND t.actualEndDate <= :endDate) " +
           "OR (t.expectedEndDate >= :startDate AND t.expectedEndDate <= :endDate))")
    List<TestTask> findByAssignedToAndDateRange(@Param("user") User user, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
} 