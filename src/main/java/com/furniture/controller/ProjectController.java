package com.furniture.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.furniture.service.ProjectService;
import com.furniture.entity.Project;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController 
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService){
        this.projectService = projectService;

    }
    @PostMapping("/api/projects")
    public Project createProject(@RequestBody Project project){
        return projectService.createProject(project);
    }
    
    @GetMapping("/api/projects")
    public List<Project> getAllProject(){
        return projectService.getAllProject();
    }
    @GetMapping("/api/projects/{id}")
    public Optional<Project> getProjectById(@PathVariable Long id){
        return projectService.getProjectById(id);
    }
    
}
