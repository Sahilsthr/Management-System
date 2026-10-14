package com.furniture.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.entity.Employee;
import com.furniture.entity.Project;
import com.furniture.entity.ProjectEmployee;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.ProjectEmployeeRepository;
import com.furniture.repository.ProjectRepository;
import com.furniture.dto.AssignEmployeeRequest;

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

    public ProjectEmployee assignEmployee(AssignEmployeeRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + request.getProjectId()));
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id " + request.getEmployeeId()));

        if (projectEmployeeRepository.existsByProjectIdAndEmployeeId(
                request.getProjectId(), request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee already assigned to this project");
        }

        ProjectEmployee pe = new ProjectEmployee();
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