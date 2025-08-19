package com.testtracking.repository;

import com.testtracking.entity.ScheduledTask;
import com.testtracking.entity.ScheduledTaskExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledTaskExecutionRepository extends JpaRepository<ScheduledTaskExecution, Long> {

    List<ScheduledTaskExecution> findByTaskOrderByExecutionTimeDesc(ScheduledTask task);
    
    Page<ScheduledTaskExecution> findByTaskOrderByExecutionTimeDesc(ScheduledTask task, Pageable pageable);
    
    List<ScheduledTaskExecution> findByTaskAndExecutionTimeBetweenOrderByExecutionTimeDesc(
        ScheduledTask task, LocalDateTime startTime, LocalDateTime endTime);
    
    List<ScheduledTaskExecution> findByStatus(ScheduledTaskExecution.ExecutionStatus status);
}
