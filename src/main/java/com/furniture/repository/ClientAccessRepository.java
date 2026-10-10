package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.ClientAccess;

import java.util.List;
import java.util.Optional;


public interface ClientAccessRepository extends JpaRepository<ClientAccess, Long> {

    Optional<ClientAccess> findByToken(String token);

    List<ClientAccess> findByProjectId(Long projectId);

    boolean existsByToken(String token);
    
}
