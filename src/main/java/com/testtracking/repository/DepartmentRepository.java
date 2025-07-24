package com.testtracking.repository;

import com.testtracking.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    boolean existsByName(String name);

    List<Department> findByIsActiveTrueOrderBySortOrderAsc();

    @Query("SELECT d FROM Department d WHERE d.isActive = true ORDER BY d.sortOrder ASC, d.name ASC")
    List<Department> findAllActiveOrdered();
} 