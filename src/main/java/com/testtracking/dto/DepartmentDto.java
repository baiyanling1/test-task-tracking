package com.testtracking.dto;

import com.testtracking.entity.Department;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class DepartmentDto {

    private Long id;

    @NotBlank(message = "部门名称不能为空")
    @Size(min = 2, max = 100, message = "部门名称长度必须在2-100个字符之间")
    private String name;

    private String description;

    private Boolean isActive = true;

    private Integer sortOrder = 0;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    public static DepartmentDto fromEntity(Department department) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setIsActive(department.getIsActive());
        dto.setSortOrder(department.getSortOrder());
        dto.setCreatedTime(department.getCreatedTime());
        dto.setUpdatedTime(department.getUpdatedTime());
        return dto;
    }
} 