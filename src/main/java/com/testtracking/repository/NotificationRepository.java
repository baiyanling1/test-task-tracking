package com.testtracking.repository;

import com.testtracking.entity.Notification;
import com.testtracking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 基础查询
    List<Notification> findByRecipient(User recipient);
    
    List<Notification> findByRecipientOrderByCreatedTimeDesc(User recipient);
    
    List<Notification> findByRecipientAndIsRead(User recipient, Boolean isRead);
    
    List<Notification> findByType(Notification.NotificationType type);
    
    List<Notification> findByPriority(Notification.NotificationPriority priority);

    // 分页查询
    Page<Notification> findByRecipient(User recipient, Pageable pageable);
    
    Page<Notification> findByRecipientAndIsRead(User recipient, Boolean isRead, Pageable pageable);
    
    Page<Notification> findByType(Notification.NotificationType type, Pageable pageable);

    // 统计查询
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false")
    Long countUnreadByRecipient(@Param("recipient") User recipient);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient")
    Long countByRecipient(@Param("recipient") User recipient);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.type = :type AND n.isRead = false")
    Long countUnreadByType(@Param("type") Notification.NotificationType type);

    // 时间范围查询
    List<Notification> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    List<Notification> findByRecipientAndCreatedTimeBetween(User recipient, LocalDateTime startTime, LocalDateTime endTime);

    // 过期通知查询
    @Query("SELECT n FROM Notification n WHERE n.expireTime IS NOT NULL AND n.expireTime < :now")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);

    // 相关任务查询
    List<Notification> findByRelatedTaskId(Long taskId);
    
    List<Notification> findByRelatedTaskIdOrderByCreatedTimeDesc(Long taskId);

    // 批量标记已读
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readTime = :readTime WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsReadByRecipient(@Param("recipient") User recipient, @Param("readTime") LocalDateTime readTime);

    // 删除过期通知
    @Query("DELETE FROM Notification n WHERE n.expireTime IS NOT NULL AND n.expireTime < :now")
    void deleteExpiredNotifications(@Param("now") LocalDateTime now);

    // 根据优先级查询
    Page<Notification> findByRecipientAndPriority(User recipient, Notification.NotificationPriority priority, Pageable pageable);

    // 搜索查询
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%)")
    Page<Notification> findByRecipientAndTitleContainingOrContentContaining(
            @Param("recipient") User recipient, 
            @Param("keyword") String keyword, 
            @Param("keyword") String keyword2, 
            Pageable pageable);

    // 根据类型查询
    Page<Notification> findByRecipientAndType(User recipient, Notification.NotificationType type, Pageable pageable);
} 