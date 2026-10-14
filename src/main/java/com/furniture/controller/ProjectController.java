package com.furniture.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.dto.CreateProjectRequest;
import com.furniture.entity.Project;
import com.furniture.service.ProjectService;

import jakarta.validation.Valid;



@RestController 
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService){
        this.projectService = projectService;

    }
    @PostMapping("/api/projects")
    public Project createProject(@Valid @RequestBody CreateProjectRequest request){
        return projectService.createProject(request);
    }
    
    @GetMapping("/api/projects")
    public List<Project> getAllProject(){
        return projectService.getAllProject();
    }
    @GetMapping("/api/projects/{id}")
    public Project getProjectById(@PathVariable Long id){
        return projectService.getProjectById(id);
    }
    
}
