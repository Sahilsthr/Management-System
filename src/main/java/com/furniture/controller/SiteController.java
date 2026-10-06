package com.furniture.controller;

import com.furniture.entity.Site;
import com.furniture.service.SiteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
public class SiteController {
    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @PostMapping("/api/sites")
    public Site createSite(@RequestBody Site site) {
        return siteService.createSite(site);
    }

    @GetMapping("/api/sites")
    public List<Site> getAllSite() {
        return siteService.getAllSite();
    }

    @GetMapping("/api/sites/{id}")
    public Optional<Site> getSiteById(@PathVariable Long id) {
        return siteService.getSiteById(id);
    }

    @GetMapping("/api/projects/{projectId}/site")
    public Optional<Site> getSiteByProjectId(@PathVariable Long projectId) {
        return siteService.getSiteByProjectId(projectId);
    }
    
}
