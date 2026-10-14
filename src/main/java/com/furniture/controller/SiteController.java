package com.furniture.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.dto.CreateSiteRequest;
import com.furniture.entity.Site;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.service.SiteService;

import jakarta.validation.Valid;

@RestController
public class SiteController {
    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @PostMapping("/api/sites")
    public Site createSite(@Valid @RequestBody CreateSiteRequest request) {
        return siteService.createSite(request);
    }

    @GetMapping("/api/sites")
    public List<Site> getAllSite() {
        return siteService.getAllSite();
    }

    @GetMapping("/api/sites/{id}")
    public Site getSiteById(@PathVariable Long id) {
        return siteService.getSiteById(id);
    }

    @GetMapping("/api/projects/{projectId}/site")
    public Site getSiteByProjectId(@PathVariable Long projectId) {
        return siteService.getSiteByProjectId(projectId).orElseThrow(() -> new ResourceNotFoundException("Site not found for project " + projectId));
    }
    
}
