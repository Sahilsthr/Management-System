package com.furniture.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.furniture.entity.ProjectEmployee;

public interface ProjectEmployeeRepository extends JpaRepository<ProjectEmployee, Long>{
    
    List<ProjectEmployee> findByProjectId(Long projectId);
    List<ProjectEmployee> findByEmployeeId(Long employeeId);
    boolean existsByProjectIdAndEmployeeId(Long projectId, Long employeeId);

}
