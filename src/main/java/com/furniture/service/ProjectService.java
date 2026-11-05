package com.furniture.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.dto.CreateProjectRequest;
import com.furniture.dto.ProjectSummaryResponse;
import com.furniture.entity.Client;
import com.furniture.entity.Employee;
import com.furniture.entity.Project;
import com.furniture.entity.ProjectEmployee;
import com.furniture.entity.ProjectStatus;
import com.furniture.entity.User;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.ClientRepository;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.ProjectEmployeeRepository;
import com.furniture.repository.ProjectRepository;
import com.furniture.repository.UserRepository;

@Service
public class ProjectService {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ClientRepository clientRepository,
            EmployeeRepository employeeRepository,
            ProjectEmployeeRepository projectEmployeeRepository,
            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.projectEmployeeRepository = projectEmployeeRepository;
        this.userRepository = userRepository;
    }

    public Project createProject(CreateProjectRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id " + request.getClientId()));

        if (request.getExpectedEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("expectedEndDate cannot be before startDate");
        }

        ProjectStatus status = ProjectStatus.PLANNED;
        if (request.getProjectStatus() != null && !request.getProjectStatus().isBlank()) {
            try {
                status = ProjectStatus.valueOf(
                        request.getProjectStatus().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid project status");
            }
        }

        Project project = new Project();
        project.setProjectName(request.getProjectName().trim());
        project.setDescription(request.getDescription().trim());
        project.setStartDate(request.getStartDate());
        project.setExpectedEndDate(request.getExpectedEndDate());
        project.setProjectStatus(status);
        project.setTotalAmount(request.getTotalAmount());
        project.setClient(client);

        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProject() {
        return projectRepository.findAll();
    }

    private Employee getEmployeeByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return employeeRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> getMyProjects(String email) {
        Employee employee = getEmployeeByEmail(email);

        List<ProjectEmployee> assignments =
                projectEmployeeRepository.findByEmployeeId(employee.getId());

        return assignments.stream()
                .map(ProjectEmployee::getProject)
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + id));
    }

    private ProjectSummaryResponse toSummaryResponse(Project project) {
        return new ProjectSummaryResponse(
                project.getId(),
                project.getProjectName(),
                project.getDescription(),
                project.getStartDate(),
                project.getExpectedEndDate(),
                project.getProjectStatus());
    }
}
