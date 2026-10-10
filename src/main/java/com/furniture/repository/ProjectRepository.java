package com.furniture.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.furniture.entity.Project;
import com.furniture.entity.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Long countByProjectStatus(ProjectStatus projectStatus);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Project p")
    BigDecimal sumAllProjectAmounts();
    
}
    
