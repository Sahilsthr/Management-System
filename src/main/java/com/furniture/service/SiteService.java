package com.furniture.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.Project;
import com.furniture.entity.Site;
import com.furniture.repository.ProjectRepository;
import com.furniture.repository.SiteRepository;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.exception.ResourceNotFoundException;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final ProjectRepository projectRepository;

    public SiteService(SiteRepository siteRepository, ProjectRepository projectRepository) {
        this.siteRepository = siteRepository;
        this.projectRepository = projectRepository;
    }

    public Site createSite(Site site) {
    if (site.getAddress() == null || site.getAddress().isBlank()) {
        throw new BadRequestException("address is required");
    }
    if (site.getCity() == null || site.getCity().isBlank()) {
        throw new BadRequestException("city is required");
    }
    if (site.getState() == null || site.getState().isBlank()) {
        throw new BadRequestException("state is required");
    }
    if (site.getPincode() == null || !site.getPincode().matches("\\d{6}")) {
        throw new BadRequestException("pincode must be 6 digits");
    }
    if (site.getLatitude() != null
            && (site.getLatitude() < -90 || site.getLatitude() > 90)) {
        throw new BadRequestException("latitude must be between -90 and 90");
    }
    if (site.getLongitude() != null
            && (site.getLongitude() < -180 || site.getLongitude() > 180)) {
        throw new BadRequestException("longitude must be between -180 and 180");
    }
    if (site.getProject() == null || site.getProject().getId() == null) {
        throw new BadRequestException("project.id is required");
    }

    Long projectId = site.getProject().getId();
    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));

    if (siteRepository.existsByProjectId(projectId)) {
        throw new DuplicateResourceException("Site already exists for this project");
    }

    site.setProject(project);
    return siteRepository.save(site);
}

    public List<Site> getAllSite() {
        return siteRepository.findAll();
    }

    public Site getSiteById(Long id) {
        return siteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Site not found with id " + id ));
    }

    public Optional<Site> getSiteByProjectId(Long projectId) {
        return siteRepository.findByProjectId(projectId);
    }
}