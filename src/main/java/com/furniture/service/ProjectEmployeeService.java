package com.furniture.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.entity.Employee;
import com.furniture.entity.Project;
import com.furniture.entity.ProjectEmployee;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.ProjectEmployeeRepository;
import com.furniture.repository.ProjectRepository;

@Service
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    public ProjectEmployeeService(ProjectEmployeeRepository projectEmployeeRepository,
                                    ProjectRepository projectRepository,
                                    EmployeeRepository employeeRepository) {
        this.projectEmployeeRepository = projectEmployeeRepository;
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
    }

    public ProjectEmployee assignEmployee(ProjectEmployee pe) {
        if (pe.getProject() == null || pe.getProject().getId() == null) {
            throw new BadRequestException("project.id is required");
        }
        if (pe.getEmployee() == null || pe.getEmployee().getId() == null) {
            throw new BadRequestException("employee.id is required");
        }

        Long projectId = pe.getProject().getId();
        Long employeeId = pe.getEmployee().getId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + employeeId));

        if (projectEmployeeRepository.existsByProjectIdAndEmployeeId(projectId, employeeId)) {
            throw new DuplicateResourceException("Employee already assigned to this project");
        }
        
        pe.setProject(project);
        pe.setEmployee(employee);
        return projectEmployeeRepository.save(pe);
    }

    public List<ProjectEmployee> getAllAssignment() {
        return projectEmployeeRepository.findAll();
    }

    public ProjectEmployee getAssignmentById(Long id) {
        return projectEmployeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id " + id));
    }

    public List<ProjectEmployee> getAssignmentByProject(Long projectId) {
        return projectEmployeeRepository.findByProjectId(projectId);
    }

    public List<ProjectEmployee> getAssignmentByEmployee(Long employeeId) {
        return projectEmployeeRepository.findByEmployeeId(employeeId);
    }
}