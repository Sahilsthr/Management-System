package com.furniture.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.dto.CreateProjectRequest;
import com.furniture.entity.Client;
import com.furniture.entity.Project;
import com.furniture.entity.ProjectStatus;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.ClientRepository;
import com.furniture.repository.ProjectRepository;

@Service
public class ProjectService {
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository, ClientRepository clientRepository) {
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
    }

    public Project createProject(CreateProjectRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id " + request.getClientId()));

        if (request.getExpectedEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("expectedEndDate cannot be before startDate");
        }

        Project project = new Project();
        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setExpectedEndDate(request.getExpectedEndDate());
        String projectStatus = request.getProjectStatus();
        project.setProjectStatus(projectStatus != null && !projectStatus.isBlank()
                ? ProjectStatus.valueOf(projectStatus.trim().toUpperCase())
                : ProjectStatus.PLANNED);
        project.setTotalAmount(request.getTotalAmount());
        project.setClient(client);
        return projectRepository.save(project);
    }

    public List<Project> getAllProject() {
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
    return projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));
    }
}
