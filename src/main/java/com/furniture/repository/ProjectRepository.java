package com.furniture.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.furniture.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    
}
    
