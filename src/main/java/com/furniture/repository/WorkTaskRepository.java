package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.WorkTask;

public interface WorkTaskRepository extends JpaRepository<WorkTask, Long>{
    
}
