package com.testtracking.repository;

import com.testtracking.entity.TaskProgress;
import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskProgressRepository extends JpaRepository<TaskProgress, Long> {

    // 基础查询
    List<TaskProgress> findByTask(TestTask task);
    
    List<TaskProgress> findByTaskOrderByUpdateTimeDesc(TestTask task);
    
    List<TaskProgress> findByUpdatedByUser(User updatedByUser);
    
    List<TaskProgress> findByTaskAndUpdatedByUser(TestTask task, User updatedByUser);

    // 分页查询
    Page<TaskProgress> findByTask(TestTask task, Pageable pageable);
    
    Page<TaskProgress> findByUpdatedByUser(User updatedByUser, Pageable pageable);

    // 时间范围查询
    List<TaskProgress> findByUpdateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    List<TaskProgress> findByTaskAndUpdateTimeBetween(TestTask task, LocalDateTime startTime, LocalDateTime endTime);

    // 最新进度查询
    @Query("SELECT tp FROM TaskProgress tp WHERE tp.task = :task ORDER BY tp.updateTime DESC")
    List<TaskProgress> findLatestProgressByTask(@Param("task") TestTask task);

    @Query("SELECT tp FROM TaskProgress tp WHERE tp.task = :task ORDER BY tp.updateTime DESC")
    List<TaskProgress> findLatestProgressByTaskLimit1(@Param("task") TestTask task);

    // 分页查询最新进度
    @Query("SELECT tp FROM TaskProgress tp WHERE tp.task = :task ORDER BY tp.updateTime DESC")
    Page<TaskProgress> findByTaskOrderByUpdateTimeDesc(@Param("task") TestTask task, Pageable pageable);

    // 进度变化查询
    @Query("SELECT tp FROM TaskProgress tp WHERE tp.task = :task AND tp.progressPercentage > :minProgress ORDER BY tp.updateTime DESC")
    List<TaskProgress> findByTaskAndProgressGreaterThan(@Param("task") TestTask task, @Param("minProgress") Integer minProgress);

    // 统计查询
    @Query("SELECT COUNT(tp) FROM TaskProgress tp WHERE tp.task = :task")
    Long countByTask(@Param("task") TestTask task);

    @Query("SELECT COUNT(tp) FROM TaskProgress tp WHERE tp.updatedByUser = :user")
    Long countByUpdatedByUser(@Param("user") User user);

    @Query("SELECT COUNT(tp) FROM TaskProgress tp WHERE tp.updateTime >= :startTime AND tp.updateTime <= :endTime")
    Long countByUpdateTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // 平均进度查询
    @Query("SELECT AVG(tp.progressPercentage) FROM TaskProgress tp WHERE tp.task = :task")
    Double getAverageProgressByTask(@Param("task") TestTask task);

    // 进度趋势查询
    @Query("SELECT tp.updateTime, tp.progressPercentage FROM TaskProgress tp WHERE tp.task = :task ORDER BY tp.updateTime")
    List<Object[]> getProgressTrendByTask(@Param("task") TestTask task);
} 