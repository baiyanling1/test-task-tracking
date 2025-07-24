package com.testtracking.repository;

import com.testtracking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByRealName(String realName);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(User.UserRole role);

    List<User> findByDepartment(String department);

    List<User> findByIsActiveTrue();

    @Query("SELECT u FROM User u WHERE u.realName LIKE %:keyword% OR u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") User.UserRole role);

    @Query("SELECT u.department, COUNT(u) FROM User u WHERE u.isActive = true GROUP BY u.department")
    List<Object[]> countByDepartment();

    // 分页查询方法
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    
    // 统计方法
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countByIsActiveTrue();
} 