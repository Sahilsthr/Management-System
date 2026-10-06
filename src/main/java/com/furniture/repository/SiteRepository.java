package com.furniture.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.Site;

public interface SiteRepository extends JpaRepository<Site,Long> {
    
    Optional<Site> findByProjectId(Long projectId);
    boolean existsByProjectId(Long projectId);
}
