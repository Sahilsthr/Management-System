package com.furniture.service;
import com.furniture.repository.ClientRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.Client;
import com.furniture.entity.Project;
import com.furniture.entity.ProjectStatus;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.ProjectRepository;

@Service
public class ProjectService {
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository, ClientRepository clientRepository) {
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
    }

    public Project createProject(Project project) {
        if (project.getProjectName() == null) {
            throw new BadRequestException("Project name is required");
        }
        if (project.getStartDate() == null) {
            throw new BadRequestException("StartDate is Required");
        }
        if (project.getExpectedEndDate() == null) {
            throw new BadRequestException("Expected End Date is Required");
        }
        if (project.getExpectedEndDate().isBefore(project.getStartDate())) {
            throw new BadRequestException("End date not to be before Start date");
        }
        if (project.getProjectStatus() == null) {
            project.setProjectStatus(ProjectStatus.PLANNED);
        }
        if(project.getTotalAmount() != null && project.getTotalAmount().compareTo(BigDecimal.ZERO) < 0){
            throw new BadRequestException("Total Amount cannot be negative");
        }
        if(project.getClient() == null || project.getClient().getId() == null){
            throw new BadRequestException("Client Id is Required");
        }
        Long clientId = project.getClient().getId();
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id " + clientId));

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
