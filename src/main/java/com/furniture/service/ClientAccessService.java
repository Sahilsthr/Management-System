package com.furniture.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.ClientAccess;
import com.furniture.entity.Project;
import com.furniture.repository.ClientAccessRepository;
import com.furniture.repository.ProjectRepository;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.exception.BadRequestException;

@Service
public class ClientAccessService {
    private final ClientAccessRepository clientAccessRepository;
    private final ProjectRepository projectRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public ClientAccessService(ClientAccessRepository clientAccessRepository, ProjectRepository projectRepository) {
        this.clientAccessRepository = clientAccessRepository;
        this.projectRepository = projectRepository;
    }

    public ClientAccess createAccess(Long projectId, int validDays) {
        if (validDays < 1 || validDays > 365) {
            throw new BadRequestException("validDays must be between 1 and 365");
        }
        Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));

        String token;
        do {
            token = generateToken();
        } while (clientAccessRepository.existsByToken(token));

        ClientAccess clientAccess = new ClientAccess();
        clientAccess.setProject(project);
        clientAccess.setToken(token);
        clientAccess.setExpiresAt(LocalDateTime.now().plusDays(validDays));
        return clientAccessRepository.save(clientAccess);
    }
    public List<ClientAccess> getAccessByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id " + projectId);
        }
        return clientAccessRepository.findByProjectId(projectId);
    }

    // Token galat, revoke ya expire: teeno me ek jaisa empty (bahar kuch pata na chale)
    public Optional<ClientAccess> validateToken(String token) {
        ClientAccess access = clientAccessRepository.findByToken(token).orElse(null);

        if (access == null) {
            return Optional.empty();
        }
        if (!Boolean.TRUE.equals(access.getActive())) {
            return Optional.empty();
        }
        if (LocalDateTime.now().isAfter(access.getExpiresAt())) {
            return Optional.empty();
        }
        return Optional.of(access);
    }
    public ClientAccess revokeToken(Long id) {
        ClientAccess access = clientAccessRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Client access not found with id " + id));
        access.setActive(false);
        return clientAccessRepository.save(access);
    }

}
