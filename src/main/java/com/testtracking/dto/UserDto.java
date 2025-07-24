package com.testtracking.dto;

import com.testtracking.entity.User;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class UserDto {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phoneNumber;

    private User.UserRole role;

    private Boolean isActive;

    private LocalDateTime lastLoginTime;

    private String department;

    private String position;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    // 用于创建用户
    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setLastLoginTime(user.getLastLoginTime());
        dto.setDepartment(user.getDepartment());
        dto.setPosition(user.getPosition());
        dto.setCreatedTime(user.getCreatedTime());
        dto.setUpdatedTime(user.getUpdatedTime());
        return dto;
    }

    // 用于更新用户（不包含密码）
    public static UserDto fromEntityWithoutPassword(User user) {
        UserDto dto = fromEntity(user);
        dto.setPassword(null);
        return dto;
    }
} 