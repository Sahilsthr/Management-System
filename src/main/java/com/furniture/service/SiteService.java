package com.furniture.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.Project;
import com.furniture.entity.Site;
import com.furniture.repository.ProjectRepository;
import com.furniture.repository.SiteRepository;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final ProjectRepository projectRepository;

    public SiteService(SiteRepository siteRepository, ProjectRepository projectRepository) {
        this.siteRepository = siteRepository;
        this.projectRepository = projectRepository;
    }

    public Site createSite(Site site) {
        Project project = projectRepository.findById(site.getProject().getId()).orElse(null);
        site.setProject(project);
        return siteRepository.save(site);
    }

    public List<Site> getAllSite() {
        return siteRepository.findAll();
    }

    public Optional<Site> getSiteById(Long id) {
        return siteRepository.findById(id);
    }

    public Optional<Site> getSiteByProjectId(Long projectId) {
        return siteRepository.findByProjectId(projectId);
    }
}