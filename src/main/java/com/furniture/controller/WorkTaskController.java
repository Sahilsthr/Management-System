package com.furniture.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.dto.CreateTaskRequest;
import com.furniture.dto.WorkTaskResponse;
import com.furniture.entity.TaskStatus;
import com.furniture.entity.WorkTask;
import com.furniture.service.WorkTaskService;

import jakarta.validation.Valid;

@RestController
public class WorkTaskController {

    private final WorkTaskService workTaskService;

    public WorkTaskController(WorkTaskService workTaskService) {
        this.workTaskService = workTaskService;
    }

    @PostMapping("/api/tasks")
    public WorkTask createTask(@Valid @RequestBody CreateTaskRequest request) {
        return workTaskService.createTask(request);
    }

    @GetMapping("/api/tasks")
    public List<WorkTask> getAllTask() {
        return workTaskService.getAllTask();
    }

    @GetMapping("/api/tasks/{id}")
    public WorkTask getTaskById(@PathVariable Long id) {
        return workTaskService.getTaskById(id);
    }

    @PutMapping("/api/tasks/{id}/status")
    public WorkTaskResponse updateTaskStatus(
            @PathVariable Long id,
            @RequestBody TaskStatus status,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        return workTaskService.updateTaskStatus(
                id, status, authentication.getName(), isAdmin);
    }

    @GetMapping("/api/projects/{projectId}/progress")
    public Map<String, Object> getProjectProgress(@PathVariable Long projectId) {
        return workTaskService.getProjectProgress(projectId);
    }

    @GetMapping("/api/tasks/my")
    public List<WorkTaskResponse> getMyTasks(Authentication authentication) {
        return workTaskService.getMyTasks(authentication.getName());
    }
}
