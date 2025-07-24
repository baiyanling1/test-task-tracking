package com.testtracking.dto;

import com.testtracking.entity.User;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class UserUpdateDto {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    // 密码字段可选，用于更新
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

    // 从UserUpdateDto转换为UserDto
    public UserDto toUserDto() {
        UserDto dto = new UserDto();
        dto.setId(this.id);
        dto.setUsername(this.username);
        dto.setPassword(this.password);
        dto.setRealName(this.realName);
        dto.setEmail(this.email);
        dto.setPhoneNumber(this.phoneNumber);
        dto.setRole(this.role);
        dto.setIsActive(this.isActive);
        dto.setLastLoginTime(this.lastLoginTime);
        dto.setDepartment(this.department);
        dto.setPosition(this.position);
        dto.setCreatedTime(this.createdTime);
        dto.setUpdatedTime(this.updatedTime);
        return dto;
    }

    // 从User实体创建UserUpdateDto
    public static UserUpdateDto fromEntity(User user) {
        UserUpdateDto dto = new UserUpdateDto();
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
} 