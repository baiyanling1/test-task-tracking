package com.testtracking.service;

import com.testtracking.entity.TestTask;
import com.testtracking.repository.TestTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TaskStatusUpdateService {

    @Autowired
    private TestTaskRepository testTaskRepository;

    /**
     * 每天凌晨2点自动检查和更新任务状态
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void updateTaskStatuses() {
        log.info("开始自动更新任务状态...");
        
        try {
            // 获取所有未完成的任务
            List<TestTask> activeTasks = testTaskRepository.findByStatusNotIn(
                List.of(TestTask.TaskStatus.COMPLETED, TestTask.TaskStatus.CANCELLED)
            );
            
            int updatedCount = 0;
            for (TestTask task : activeTasks) {
                TestTask.TaskStatus oldStatus = task.getStatus();
                task.checkTaskStatusAndOverdue();
                
                if (oldStatus != task.getStatus()) {
                    log.info("任务 {} 状态从 {} 更新为 {}", 
                        task.getTaskName(), oldStatus.getDescription(), task.getStatus().getDescription());
                    updatedCount++;
                }
            }
            
            // 批量保存更新
            if (!activeTasks.isEmpty()) {
                testTaskRepository.saveAll(activeTasks);
            }
            
            log.info("任务状态更新完成，共更新 {} 个任务", updatedCount);
            
        } catch (Exception e) {
            log.error("自动更新任务状态时发生错误", e);
        }
    }

    /**
     * 每小时检查一次预期完成时间到达的任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void checkExpectedCompletionReached() {
        log.info("检查预期完成时间到达的任务...");
        
        try {
            // 获取今天预期完成的任务
            List<TestTask> tasksReachingExpectedDate = testTaskRepository.findByExpectedEndDateAndStatus(
                java.time.LocalDate.now(), 
                TestTask.TaskStatus.IN_PROGRESS
            );
            
            int updatedCount = 0;
            for (TestTask task : tasksReachingExpectedDate) {
                task.checkTaskStatusAndOverdue();
                log.info("任务 {} 已达到预期完成时间", task.getTaskName());
                updatedCount++;
            }
            
            // 批量保存更新
            if (!tasksReachingExpectedDate.isEmpty()) {
                testTaskRepository.saveAll(tasksReachingExpectedDate);
            }
            
            log.info("预期完成时间检查完成，共更新 {} 个任务", updatedCount);
            
        } catch (Exception e) {
            log.error("检查预期完成时间到达的任务时发生错误", e);
        }
    }
}
