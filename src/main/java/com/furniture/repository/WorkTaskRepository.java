package com.furniture.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.TaskStatus;
import com.furniture.entity.WorkTask;

public interface WorkTaskRepository extends JpaRepository<WorkTask, Long>{
    
    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
    List<WorkTask> findByAssignedEmployeeId(Long employeeId);
    Optional<WorkTask> findByIdAndAssignedEmployeeId(Long id, Long employeeId);
    List<WorkTask> findByAssignedEmployeeIdAndProjectIdIn(
    Long employeeId,
    List<Long> projectIds
);
}
