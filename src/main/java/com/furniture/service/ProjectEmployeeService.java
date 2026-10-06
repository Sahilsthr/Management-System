package com.furniture.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.Employee;
import com.furniture.entity.Project;
import com.furniture.entity.ProjectEmployee;
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
        Project project = projectRepository.findById(pe.getProject().getId()).orElse(null);
        Employee employee = employeeRepository.findById(pe.getEmployee().getId()).orElse(null);
        pe.setProject(project);
        pe.setEmployee(employee);
        return projectEmployeeRepository.save(pe);
    }

    public List<ProjectEmployee> getAllAssignment() {
        return projectEmployeeRepository.findAll();
    }

    public Optional<ProjectEmployee> getAssignmentById(Long id) {
        return projectEmployeeRepository.findById(id);
    }

    public List<ProjectEmployee> getAssignmentByProject(Long projectId) {
        return projectEmployeeRepository.findByProjectId(projectId);
    }

    public List<ProjectEmployee> getAssignmentByEmployee(Long employeeId) {
        return projectEmployeeRepository.findByEmployeeId(employeeId);
    }
}