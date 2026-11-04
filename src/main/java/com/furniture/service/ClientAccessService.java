
package com.furniture.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.dto.ClientAccessCreateResponse;
import com.furniture.dto.ClientAccessResponse;
import com.furniture.entity.ClientAccess;
import com.furniture.entity.Project;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.ClientAccessRepository;
import com.furniture.repository.ProjectRepository;

@Service
public class ClientAccessService {

    private final ClientAccessRepository clientAccessRepository;
    private final ProjectRepository projectRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public ClientAccessService(
            ClientAccessRepository clientAccessRepository,
            ProjectRepository projectRepository) {
        this.clientAccessRepository = clientAccessRepository;
        this.projectRepository = projectRepository;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }

    @Transactional
    public ClientAccessCreateResponse createAccess(Long projectId, int validDays) {

        if (validDays < 1 || validDays > 365) {
            throw new BadRequestException(
                    "validDays must be between 1 and 365");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId));

        String rawToken;
        String tokenHash;

        do {
            rawToken = generateToken();
            tokenHash = hashToken(rawToken);
        } while (clientAccessRepository.existsByTokenHash(tokenHash));

        ClientAccess clientAccess = new ClientAccess();

        clientAccess.setProject(project);
        clientAccess.setTokenHash(tokenHash);
        clientAccess.setExpiresAt(
                LocalDateTime.now().plusDays(validDays));

        ClientAccess saved = clientAccessRepository.save(clientAccess);

        return new ClientAccessCreateResponse(
                saved.getId(),
                project.getId(),
                rawToken,
                saved.getExpiresAt(),
                saved.getActive(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ClientAccessResponse> getAccessByProject(Long projectId) {

        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException(
                    "Project not found with id " + projectId);
        }

        return clientAccessRepository.findByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ClientAccess> validateToken(String token) {

        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String tokenHash = hashToken(token);

        ClientAccess access = clientAccessRepository
                .findByTokenHash(tokenHash)
                .orElse(null);

        if (access == null) {
            return Optional.empty();
        }

        if (!Boolean.TRUE.equals(access.getActive())) {
            return Optional.empty();
        }

        if (access.getExpiresAt() == null
                || !LocalDateTime.now().isBefore(access.getExpiresAt())) {
            return Optional.empty();
        }

        return Optional.of(access);
    }

    @Transactional
    public ClientAccessResponse revokeToken(Long id) {

        ClientAccess access = clientAccessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client access not found with id " + id));

        access.setActive(false);

        ClientAccess saved = clientAccessRepository.save(access);

        return toResponse(saved);
    }

    private ClientAccessResponse toResponse(ClientAccess access) {

        return new ClientAccessResponse(
                access.getId(),
                access.getProject().getId(),
                access.getExpiresAt(),
                access.getActive(),
                access.getCreatedAt()
        );
    }
}
