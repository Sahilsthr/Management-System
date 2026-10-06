package com.furniture.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.entity.ProjectEmployee;
import com.furniture.service.ProjectEmployeeService;

@RestController
public class ProjectEmployeeController {
    private final ProjectEmployeeService proS;

    public ProjectEmployeeController(ProjectEmployeeService proS) {
        this.proS = proS;
    }

    @PostMapping("/api/assignments")
    public ProjectEmployee assignEmployee(@RequestBody ProjectEmployee pes) {
        return proS.assignEmployee(pes);
    }

    @GetMapping("/api/assignments")
    public List<ProjectEmployee> getAllAssignment() {
        return proS.getAllAssignment();
    }

    @GetMapping("/api/assignments/{id}")
    public Optional<ProjectEmployee> getAssignmentById(@PathVariable Long id) {
        return proS.getAssignmentById(id);
    }
    
    @GetMapping("/api/projects/{projectId}/employees")
    public List<ProjectEmployee> getAssignmentByProject(@PathVariable Long projectId){
        return proS.getAssignmentByProject(projectId);
    }

    @GetMapping("/api/employees/{employeeId}/projects")
    public List<ProjectEmployee> getAssignmentByEmployee(@PathVariable Long employeeId){
        return proS.getAssignmentByEmployee(employeeId);

    }
}
