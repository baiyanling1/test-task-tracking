package com.testtracking.controller;

import com.testtracking.dto.DepartmentDto;
import com.testtracking.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取所有活跃部门
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TESTER')")
    public ResponseEntity<?> getAllActiveDepartments() {
        try {
            List<DepartmentDto> departments = departmentService.getAllActiveDepartments();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            log.error("获取部门列表失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取部门列表失败");
        }
    }

    /**
     * 获取所有部门（包括非活跃）
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getAllDepartments() {
        try {
            List<DepartmentDto> departments = departmentService.getAllDepartments();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            log.error("获取所有部门失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取所有部门失败");
        }
    }

    /**
     * 根据ID获取部门
     */
    @GetMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getDepartmentById(@PathVariable Long departmentId) {
        try {
            DepartmentDto department = departmentService.getDepartmentById(departmentId);
            return ResponseEntity.ok(department);
        } catch (Exception e) {
            log.error("获取部门失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取部门失败");
        }
    }

    /**
     * 创建部门
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createDepartment(@Valid @RequestBody DepartmentDto departmentDto) {
        try {
            DepartmentDto createdDepartment = departmentService.createDepartment(departmentDto);
            return ResponseEntity.ok(createdDepartment);
        } catch (Exception e) {
            log.error("创建部门失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新部门
     */
    @PutMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateDepartment(@PathVariable Long departmentId, @Valid @RequestBody DepartmentDto departmentDto) {
        try {
            DepartmentDto updatedDepartment = departmentService.updateDepartment(departmentId, departmentDto);
            return ResponseEntity.ok(updatedDepartment);
        } catch (Exception e) {
            log.error("更新部门失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long departmentId) {
        try {
            departmentService.deleteDepartment(departmentId);
            return ResponseEntity.ok("部门删除成功");
        } catch (Exception e) {
            log.error("删除部门失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 