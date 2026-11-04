
package com.furniture.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.furniture.entity.Project;
import com.furniture.entity.ProjectStatus;

import jakarta.persistence.LockModeType;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Long countByProjectStatus(ProjectStatus projectStatus);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Project p")
    BigDecimal sumAllProjectAmounts();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Project p WHERE p.id = :projectId")
    Optional<Project> findByIdForUpdate(@Param("projectId") Long projectId);
}
