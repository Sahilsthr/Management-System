package com.furniture.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Service;
import com.furniture.entity.ClientAccess;
import com.furniture.entity.Project;
import com.furniture.repository.ClientAccessRepository;
import com.furniture.repository.ProjectRepository;

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
        Project project = projectRepository.findById(projectId)
                .orElse(null);

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
    return clientAccessRepository.findByProjectId(projectId);
}

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
    public ClientAccess revokeToken(Long id){
        ClientAccess access = clientAccessRepository.findById(id).orElse(null);
        access.setActive(false);

        return clientAccessRepository.save(access);
    }

}
