package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.TaskStatus;
import com.furniture.entity.WorkTask;

public interface WorkTaskRepository extends JpaRepository<WorkTask, Long>{
    
    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
    
}
