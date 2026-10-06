package com.furniture.service;

import com.furniture.entity.Project;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import com.furniture.repository.ProjectRepository;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository){
        this.projectRepository = projectRepository;
    }
    public Project createProject(Project project){
        return projectRepository.save(project);
    }

    public List<Project> getAllProject(){
        return projectRepository.findAll();

    }
    public Optional<Project> getProjectById(Long id){
        return projectRepository.findById(id);

    }
    
}
