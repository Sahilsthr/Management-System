
package com.furniture.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.ClientAccess;

public interface ClientAccessRepository extends JpaRepository<ClientAccess, Long> {

    Optional<ClientAccess> findByTokenHash(String tokenHash);

    List<ClientAccess> findByProjectId(Long projectId);

    boolean existsByTokenHash(String tokenHash);

}
