package com.testtracking.repository;

import com.testtracking.entity.LoginHistory;
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
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // 基础查询
    List<LoginHistory> findByUser(User user);
    
    List<LoginHistory> findByUserOrderByLoginTimeDesc(User user);
    
    List<LoginHistory> findByStatus(LoginHistory.LoginStatus status);
    
    List<LoginHistory> findByUserAndStatus(User user, LoginHistory.LoginStatus status);
    
    // 时间范围查询
    List<LoginHistory> findByUserAndLoginTimeAfterOrderByLoginTimeDesc(User user, LocalDateTime after);
    
    List<LoginHistory> findByUserAndStatusAndLoginTimeAfterOrderByLoginTimeDesc(User user, LoginHistory.LoginStatus status, LocalDateTime after);

    // 分页查询
    Page<LoginHistory> findByUser(User user, Pageable pageable);
    
    Page<LoginHistory> findByUserOrderByLoginTimeDesc(User user, Pageable pageable);
    
    Page<LoginHistory> findByStatus(LoginHistory.LoginStatus status, Pageable pageable);
    
    // 分页时间范围查询
    Page<LoginHistory> findByUserAndLoginTimeAfterOrderByLoginTimeDesc(User user, LocalDateTime after, Pageable pageable);

    // 时间范围查询
    List<LoginHistory> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    List<LoginHistory> findByUserAndLoginTimeBetween(User user, LocalDateTime startTime, LocalDateTime endTime);
    
    List<LoginHistory> findByLoginTimeBefore(LocalDateTime before);

    // 统计查询
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.user = :user")
    Long countByUser(@Param("user") User user);

    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.user = :user AND lh.status = :status")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") LoginHistory.LoginStatus status);

    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.loginTime >= :startTime AND lh.loginTime <= :endTime")
    Long countByLoginTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // 最新登录记录
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user = :user ORDER BY lh.loginTime DESC")
    List<LoginHistory> findLatestByUser(@Param("user") User user);

    // 获取用户最后一次成功登录
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user = :user AND lh.status = 'SUCCESS' ORDER BY lh.loginTime DESC")
    List<LoginHistory> findLatestSuccessByUser(@Param("user") User user);
}
