package com.testtracking.repository;

import com.testtracking.entity.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    
    Optional<ScheduledTask> findByTaskName(String taskName);
}
