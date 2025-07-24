package com.testtracking.service;

import com.testtracking.dto.UserDto;
import com.testtracking.entity.User;
import com.testtracking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.testtracking.dto.UserUpdateDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     */
    public UserDto createUser(UserDto userDto) {
        log.info("创建用户: {}", userDto.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("用户名已存在: " + userDto.getUsername());
        }
        
        // 检查邮箱是否已存在
        if (userDto.getEmail() != null && userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + userDto.getEmail());
        }
        
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRealName(userDto.getRealName());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setRole(userDto.getRole() != null ? userDto.getRole() : User.UserRole.TESTER);
        user.setIsActive(userDto.getIsActive() != null ? userDto.getIsActive() : true);
        user.setDepartment(userDto.getDepartment());
        user.setPosition(userDto.getPosition());
        
        User savedUser = userRepository.save(user);
        return UserDto.fromEntityWithoutPassword(savedUser);
    }

    /**
     * 更新用户
     */
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("更新用户: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        // 检查用户名是否被其他用户使用
        if (!user.getUsername().equals(userDto.getUsername()) && 
            userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("用户名已存在: " + userDto.getUsername());
        }
        
        // 检查邮箱是否被其他用户使用
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + userDto.getEmail());
        }
        
        user.setUsername(userDto.getUsername());
        // 只有当密码不为空且长度符合要求时才更新密码
        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty() && userDto.getPassword().length() >= 6) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        user.setRealName(userDto.getRealName());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        // 验证角色是否有效
        if (userDto.getRole() != null) {
            try {
                User.UserRole.valueOf(userDto.getRole().name());
                user.setRole(userDto.getRole());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的用户角色: " + userDto.getRole());
            }
        }
        user.setIsActive(userDto.getIsActive());
        user.setDepartment(userDto.getDepartment());
        user.setPosition(userDto.getPosition());
        
        User savedUser = userRepository.save(user);
        return UserDto.fromEntityWithoutPassword(savedUser);
    }

    /**
     * 根据UserUpdateDto更新用户
     */
    public UserDto updateUserFromDto(Long userId, UserUpdateDto userUpdateDto) {
        log.info("更新用户: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        // 检查用户名是否被其他用户使用
        if (!user.getUsername().equals(userUpdateDto.getUsername()) && 
            userRepository.existsByUsername(userUpdateDto.getUsername())) {
            throw new RuntimeException("用户名已存在: " + userUpdateDto.getUsername());
        }
        
        // 检查邮箱是否被其他用户使用
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmail(userUpdateDto.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + userUpdateDto.getEmail());
        }
        
        user.setUsername(userUpdateDto.getUsername());
        // 只有当密码不为空且长度符合要求时才更新密码
        if (userUpdateDto.getPassword() != null && !userUpdateDto.getPassword().trim().isEmpty() && userUpdateDto.getPassword().length() >= 6) {
            user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }
        user.setRealName(userUpdateDto.getRealName());
        user.setEmail(userUpdateDto.getEmail());
        user.setPhoneNumber(userUpdateDto.getPhoneNumber());
        // 验证角色是否有效
        if (userUpdateDto.getRole() != null) {
            try {
                User.UserRole.valueOf(userUpdateDto.getRole().name());
                user.setRole(userUpdateDto.getRole());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的用户角色: " + userUpdateDto.getRole());
            }
        }
        user.setIsActive(userUpdateDto.getIsActive());
        user.setDepartment(userUpdateDto.getDepartment());
        user.setPosition(userUpdateDto.getPosition());
        
        User savedUser = userRepository.save(user);
        return UserDto.fromEntityWithoutPassword(savedUser);
    }

    /**
     * 根据ID获取用户
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        return UserDto.fromEntityWithoutPassword(user);
    }

    /**
     * 根据用户名获取用户
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 获取所有用户
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findByIsActiveTrue().stream()
                .map(UserDto::fromEntityWithoutPassword)
                .collect(Collectors.toList());
    }

    /**
     * 根据角色获取用户
     */
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(UserDto::fromEntityWithoutPassword)
                .collect(Collectors.toList());
    }

    /**
     * 根据部门获取用户
     */
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department).stream()
                .map(UserDto::fromEntityWithoutPassword)
                .collect(Collectors.toList());
    }

    /**
     * 搜索用户
     */
    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String keyword) {
        return userRepository.findByKeyword(keyword).stream()
                .map(UserDto::fromEntityWithoutPassword)
                .collect(Collectors.toList());
    }

    /**
     * 删除用户（硬删除）
     */
    public void deleteUser(Long userId) {
        log.info("删除用户: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        // 不能删除自己
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName().equals(user.getUsername())) {
            throw new RuntimeException("不能删除当前登录用户");
        }
        
        // 硬删除用户
        userRepository.delete(user);
    }

    /**
     * 禁用用户
     */
    public void disableUser(Long userId) {
        log.info("禁用用户: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        // 不能禁用自己
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName().equals(user.getUsername())) {
            throw new RuntimeException("不能禁用当前登录用户");
        }
        
        user.setIsActive(false);
        userRepository.save(user);
    }

    /**
     * 启用用户
     */
    public void enableUser(Long userId) {
        log.info("启用用户: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        user.setIsActive(true);
        userRepository.save(user);
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    /**
     * 获取用户统计信息
     */
    @Transactional(readOnly = true)
    public long countUsersByRole(User.UserRole role) {
        return userRepository.countByRole(role);
    }

    /**
     * 获取部门用户统计
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDepartmentUserCount() {
        return userRepository.countByDepartment();
    }

    /**
     * 分页查询用户（带筛选）
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersWithFilters(String username, String role, Boolean isActive, Pageable pageable) {
        Page<User> users;
        
        if (username != null && !username.isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        } else if (role != null && !role.isEmpty()) {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            users = userRepository.findByRole(userRole, pageable);
        } else if (isActive != null) {
            users = userRepository.findByIsActive(isActive, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        return users.map(UserDto::fromEntityWithoutPassword);
    }

    /**
     * 重置用户密码
     */
    public String resetUserPassword(Long userId) {
        log.info("重置用户密码: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        // 生成随机密码
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return newPassword;
    }

    /**
     * 获取用户统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long adminCount = userRepository.countByRole(User.UserRole.ADMIN);
        long managerCount = userRepository.countByRole(User.UserRole.MANAGER);
        long testerCount = userRepository.countByRole(User.UserRole.TESTER);
        long activeUsers = userRepository.countByIsActiveTrue();
        
        stats.put("totalUsers", totalUsers);
        stats.put("adminCount", adminCount);
        stats.put("managerCount", managerCount);
        stats.put("testerCount", testerCount);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);
        
        // 部门统计
        List<Object[]> departmentStats = userRepository.countByDepartment();
        stats.put("departmentStats", departmentStats);
        
        return stats;
    }

    /**
     * 更新用户最后登录时间（通过用户ID）
     */
    public void updateLastLoginTime(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }

    /**
     * 修改用户密码
     */
    public void changePassword(String username, String newPassword) {
        log.info("修改用户密码: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
} 