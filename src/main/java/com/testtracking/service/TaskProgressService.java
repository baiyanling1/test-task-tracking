package com.testtracking.service;

import com.testtracking.dto.TaskProgressDto;
import com.testtracking.entity.TaskProgress;
import com.testtracking.entity.TestTask;
import com.testtracking.entity.User;
import com.testtracking.repository.TaskProgressRepository;
import com.testtracking.repository.TestTaskRepository;
import com.testtracking.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskProgressService {

    private final TaskProgressRepository taskProgressRepository;
    private final TestTaskRepository testTaskRepository;
    private final UserRepository userRepository;
    private final TestTaskService testTaskService;

    public TaskProgressService(TaskProgressRepository taskProgressRepository,
                               TestTaskRepository testTaskRepository,
                               UserRepository userRepository,
                               @Lazy TestTaskService testTaskService) {
        this.taskProgressRepository = taskProgressRepository;
        this.testTaskRepository = testTaskRepository;
        this.userRepository = userRepository;
        this.testTaskService = testTaskService;
    }

    /**
     * 根据任务ID获取进度列表
     */
    @Transactional(readOnly = true)
    public Page<TaskProgressDto> getTaskProgressByTaskId(Long taskId, Pageable pageable) {
        TestTask task = testTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        
        Page<TaskProgress> progressPage = taskProgressRepository.findByTask(task, pageable);
        return progressPage.map(TaskProgressDto::fromEntity);
    }

    /**
     * 添加任务进度
     */
    @Transactional
    public TaskProgressDto addTaskProgress(TaskProgressDto progressDto) {
        log.info("开始添加任务进度 - taskId: {}, updatedByUserId: {}, progressPercentage: {}, progressNotes: {}", 
                progressDto.getTaskId(), progressDto.getUpdatedByUserId(), 
                progressDto.getProgressPercentage(), progressDto.getProgressNotes());
        
        TestTask task = testTaskRepository.findById(progressDto.getTaskId())
                .orElseThrow(() -> new RuntimeException("任务不存在: " + progressDto.getTaskId()));
        
        log.info("找到任务 - taskName: {}, assignedTo: {}, createdByUser: {}", 
                task.getTaskName(), 
                task.getAssignedTo() != null ? task.getAssignedTo().getRealName() : "未分配",
                task.getCreatedByUser() != null ? task.getCreatedByUser().getRealName() : "未知");
        
        User updatedByUser = userRepository.findById(progressDto.getUpdatedByUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在: " + progressDto.getUpdatedByUserId()));
        
        log.info("操作用户 - username: {}, realName: {}, role: {}", 
                updatedByUser.getUsername(), updatedByUser.getRealName(), updatedByUser.getRole());

        // 权限检查：只有管理员、测试经理或任务负责人可以更新进度
        boolean canUpdate = canUpdateProgress(task, updatedByUser);
        log.info("权限检查结果: {}", canUpdate);
        
        if (!canUpdate) {
            String assigneeName = task.getAssignedTo() != null ? task.getAssignedTo().getRealName() : "未分配";
            String creatorName = task.getCreatedByUser() != null ? task.getCreatedByUser().getRealName() : "未知";
            String errorMsg = String.format("权限不足：您只能更新分配给您的任务或您创建的任务。当前任务负责人：%s，创建者：%s", assigneeName, creatorName);
            log.error("权限检查失败: {}", errorMsg);
            throw new RuntimeException(errorMsg);
        }

        TaskProgress progress = new TaskProgress();
        progress.setTask(task);
        progress.setUpdatedByUser(updatedByUser);
        progress.setProgressPercentage(progressDto.getProgressPercentage());
        progress.setProgressNotes(progressDto.getProgressNotes());
        progress.setTaskStatus(progressDto.getTaskStatus());
        progress.setActualEndDate(progressDto.getActualEndDate());
        progress.setActualManDays(progressDto.getActualManDays());
        progress.setUpdateTime(LocalDateTime.now());

        TaskProgress savedProgress = taskProgressRepository.save(progress);
        log.info("进度历史保存成功 - progressId: {}", savedProgress.getId());
        
        // 更新任务的进度百分比和状态
        task.updateProgress(progressDto.getProgressPercentage());
        
        // 根据进度自动更新任务状态
        if (progressDto.getProgressPercentage() == 100) {
            task.setStatus(TestTask.TaskStatus.COMPLETED);
            // 当进度为100%时，更新任务的实际结束时间
            if (progressDto.getActualEndDate() != null && !progressDto.getActualEndDate().isEmpty()) {
                try {
                    LocalDate actualEndDate = LocalDate.parse(progressDto.getActualEndDate());
                    task.setActualEndDate(actualEndDate);
                } catch (Exception e) {
                    log.warn("无法解析实际结束时间: {}", progressDto.getActualEndDate());
                }
            }
            // 当进度为100%时，更新任务的实际工时
            if (progressDto.getActualManDays() != null && progressDto.getActualManDays() > 0) {
                task.setActualManDays(progressDto.getActualManDays());
            }
        } else if (progressDto.getProgressPercentage() > 0) {
            task.setStatus(TestTask.TaskStatus.IN_PROGRESS);
        }
        
        // 重新计算超时状态（确保基于正确的实际结束时间）
        task.checkOverdue();
        
        testTaskRepository.save(task);
        log.info("任务进度已保存 - taskId: {}, progress: {}%, parentId: {}", task.getId(), task.getProgressPercentage(), task.getParentId());
        
        // 如果是需求任务，更新父任务（版本）的进度
        if (task.getParentId() != null) {
            log.info("检测到子任务，开始更新父版本进度 - parentId: {}", task.getParentId());
            testTaskService.updateVersionProgress(task.getParentId());
            log.info("父版本进度更新完成 - parentId: {}", task.getParentId());
        }
        
        return TaskProgressDto.fromEntity(savedProgress);
    }

    /**
     * 更新任务进度
     */
    @Transactional
    public TaskProgressDto updateTaskProgress(TaskProgressDto progressDto) {
        TaskProgress progress = taskProgressRepository.findById(progressDto.getId())
                .orElseThrow(() -> new RuntimeException("进度记录不存在: " + progressDto.getId()));
        
        progress.setProgressPercentage(progressDto.getProgressPercentage());
        progress.setProgressNotes(progressDto.getProgressNotes());
        progress.setTaskStatus(progressDto.getTaskStatus());
        progress.setUpdateTime(LocalDateTime.now());

        TaskProgress updatedProgress = taskProgressRepository.save(progress);
        
        // 更新任务的进度百分比
        TestTask task = progress.getTask();
        task.updateProgress(progressDto.getProgressPercentage());
        testTaskRepository.save(task);
        
        return TaskProgressDto.fromEntity(updatedProgress);
    }

    /**
     * 删除任务进度
     */
    @Transactional
    public void deleteTaskProgress(Long progressId) {
        TaskProgress progress = taskProgressRepository.findById(progressId)
                .orElseThrow(() -> new RuntimeException("进度记录不存在: " + progressId));
        
        taskProgressRepository.delete(progress);
    }

    /**
     * 获取任务最新进度
     */
    @Transactional(readOnly = true)
    public TaskProgressDto getLatestTaskProgress(Long taskId) {
        TestTask task = testTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        
        List<TaskProgress> latestProgressList = taskProgressRepository.findLatestProgressByTaskLimit1(task);
        
        if (latestProgressList.isEmpty()) {
            return null;
        }
        
        return TaskProgressDto.fromEntity(latestProgressList.get(0));
    }

    /**
     * 获取任务所有进度
     */
    @Transactional(readOnly = true)
    public List<TaskProgressDto> getAllTaskProgress(Long taskId) {
        TestTask task = testTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        
        return taskProgressRepository.findByTaskOrderByUpdateTimeDesc(task)
                .stream()
                .map(TaskProgressDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 权限检查：是否可以更新任务进度
     */
    private boolean canUpdateProgress(TestTask task, User currentUser) {
        // ADMIN可以更新所有任务进度
        if (currentUser.getRole() == User.UserRole.ADMIN) {
            return true;
        }
        
        // MANAGER可以更新所有任务进度
        if (currentUser.getRole() == User.UserRole.MANAGER) {
            return true;
        }
        
        // TESTER只能更新分配给自己的任务或自己创建的任务
        if (currentUser.getRole() == User.UserRole.TESTER) {
            boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId());
            boolean isCreator = task.getCreatedByUser() != null && task.getCreatedByUser().getId().equals(currentUser.getId());
            return isAssignee || isCreator;
        }
        
        return false;
    }
}
