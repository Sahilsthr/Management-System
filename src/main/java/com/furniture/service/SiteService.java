package com.furniture.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.dto.CreateSiteRequest;
import com.furniture.entity.Project;
import com.furniture.entity.Site;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.exception.ResourceNotFoundException;
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

    public Site createSite(CreateSiteRequest request) {
    Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Project not found with id " + request.getProjectId()));

    if (siteRepository.existsByProjectId(request.getProjectId())) {
        throw new DuplicateResourceException("Site already exists for this project");
    }

    Site site = new Site();
    site.setAddress(request.getAddress());
    site.setCity(request.getCity());
    site.setState(request.getState());
    site.setPincode(request.getPincode());
    site.setLatitude(request.getLatitude());
    site.setLongitude(request.getLongitude());
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