package com.testtracking.service;

import com.testtracking.dto.DepartmentDto;
import com.testtracking.entity.Department;
import com.testtracking.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    /**
     * 创建部门
     */
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        log.info("创建部门: {}", departmentDto.getName());
        
        // 检查部门名称是否已存在
        if (departmentRepository.existsByName(departmentDto.getName())) {
            throw new RuntimeException("部门名称已存在: " + departmentDto.getName());
        }
        
        Department department = new Department();
        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());
        department.setIsActive(departmentDto.getIsActive() != null ? departmentDto.getIsActive() : true);
        department.setSortOrder(departmentDto.getSortOrder() != null ? departmentDto.getSortOrder() : 0);
        
        Department savedDepartment = departmentRepository.save(department);
        return DepartmentDto.fromEntity(savedDepartment);
    }

    /**
     * 更新部门
     */
    public DepartmentDto updateDepartment(Long departmentId, DepartmentDto departmentDto) {
        log.info("更新部门: {}", departmentId);
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("部门不存在: " + departmentId));
        
        // 检查部门名称是否被其他部门使用
        if (!department.getName().equals(departmentDto.getName()) && 
            departmentRepository.existsByName(departmentDto.getName())) {
            throw new RuntimeException("部门名称已存在: " + departmentDto.getName());
        }
        
        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());
        department.setIsActive(departmentDto.getIsActive());
        department.setSortOrder(departmentDto.getSortOrder());
        
        Department savedDepartment = departmentRepository.save(department);
        return DepartmentDto.fromEntity(savedDepartment);
    }

    /**
     * 根据ID获取部门
     */
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("部门不存在: " + departmentId));
        return DepartmentDto.fromEntity(department);
    }

    /**
     * 获取所有活跃部门
     */
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllActiveDepartments() {
        return departmentRepository.findAllActiveOrdered().stream()
                .map(DepartmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有部门
     */
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 删除部门（硬删除）
     */
    public void deleteDepartment(Long departmentId) {
        log.info("删除部门: {}", departmentId);
        
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("部门不存在: " + departmentId));
        
        // 检查是否有用户关联到此部门
        // 这里可以添加检查逻辑，如果有用户关联到该部门，可以选择抛出异常或警告
        
        // 硬删除部门
        departmentRepository.delete(department);
    }

    /**
     * 根据名称获取部门
     */
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentByName(String name) {
        Department department = departmentRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("部门不存在: " + name));
        return DepartmentDto.fromEntity(department);
    }
} 