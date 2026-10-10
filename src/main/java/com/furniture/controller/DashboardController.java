package com.furniture.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.service.DashboardService;


@RestController
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }
    @GetMapping("/api/dashboard")
    public  Map<String,Object> getAdminDashboard(){
        return dashboardService.getAdminDashboard();
    }
    @GetMapping("/api/projects/{projectId}/dashboard")
    public Map<String,Object> getProjectDashboard(@PathVariable Long projectId) {
        return dashboardService.getProjectDashboard(projectId);
    }
    
}
    

